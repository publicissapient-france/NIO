package fr.xebia.usi.quizz.web.httpcore;


import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.util.Locale;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import fr.xebia.usi.quizz.model.User;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMemoryImpl;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.HttpStatus;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.nio.DefaultServerIOEventDispatch;
import org.apache.http.impl.nio.reactor.DefaultListeningIOReactor;
import org.apache.http.nio.NHttpConnection;
import org.apache.http.nio.entity.BufferingNHttpEntity;
import org.apache.http.nio.entity.ConsumingNHttpEntity;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.params.NIOReactorPNames;
import org.apache.http.nio.protocol.AsyncNHttpServiceHandler;
import org.apache.http.nio.protocol.EventListener;
import org.apache.http.nio.protocol.NHttpRequestHandler;
import org.apache.http.nio.protocol.NHttpRequestHandlerRegistry;
import org.apache.http.nio.protocol.NHttpResponseTrigger;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.ListeningIOReactor;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.ImmutableHttpProcessor;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic, yet fully functional and spec compliant, HTTP/1.1 server based on the non-blocking 
 * I/O model.
 * <p>
 * Please note the purpose of this application is demonstrate the usage of HttpCore APIs.
 * It is NOT intended to demonstrate the most efficient way of building an HTTP server. 
 *
 * 8k req/sec for 10k user and 50c
 *
 */
public class HttpServer {

	private static final Logger LOG = LoggerFactory.getLogger("httpcore-server");
	
	static AsyncNHttpServiceHandler handler;
	
    public static void main(String[] args) throws Exception {

        HttpParams params = new SyncBasicHttpParams();
        params
            .setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 30000)
            .setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024)
            .setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
            .setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
            .setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1")
            .setBooleanParameter(NIOReactorPNames.INTEREST_OPS_QUEUEING, true)
            .setLongParameter(NIOReactorPNames.SELECT_INTERVAL, 50);

        HttpProcessor httpproc = new ImmutableHttpProcessor(new HttpResponseInterceptor[] {
                new ResponseDate(),
                new ResponseServer(),
                new ResponseContent(),
                new ResponseConnControl()
        });
        
        handler = new AsyncNHttpServiceHandler(
                httpproc,
                new DefaultHttpResponseFactory(),
                new DefaultConnectionReuseStrategy(),
                params);

        // Set up request handlers
        NHttpRequestHandlerRegistry reqistry = new NHttpRequestHandlerRegistry();
        reqistry.register("/api/user*", new UserRequestHandler());

        handler.setHandlerResolver(reqistry);

        // Provide an event logger
        handler.setEventListener(new EventLogger());

        IOEventDispatch ioEventDispatch = new DefaultServerIOEventDispatch(handler, params);
        ListeningIOReactor ioReactor = new DefaultListeningIOReactor(2, params);
        try {
            ioReactor.listen(new InetSocketAddress(8080));
            ioReactor.execute(ioEventDispatch);
        } catch (InterruptedIOException ex) {
            LOG.error("Interrupted", ex);
        } catch (IOException e) {
        	 LOG.error("Interrupted", e);
        }
        LOG.warn("Shutdown");
    }

    static class UserRequestHandler implements NHttpRequestHandler  {

    	private final UserManager manager;
    	private final JsonMapper mapper;
    	private final Executor executor;
    	
    	public UserRequestHandler() {
    		this.manager = new UserManagerMemoryImpl();
    		this.mapper = new JsonMapperImpl();
    		this.executor = Executors.newFixedThreadPool(3);
		}

    	
    	@Override
    	public ConsumingNHttpEntity entityRequest(
    			HttpEntityEnclosingRequest request, HttpContext context)
    			throws HttpException, IOException {
    		
    		return new BufferingNHttpEntity(request.getEntity(),
                    new HeapByteBufferAllocator());
    	}
    	
    	@Override
    	public void handle(HttpRequest request, final HttpResponse response,
    			final NHttpResponseTrigger trigger, final HttpContext context)
    			throws HttpException, IOException {
    		
    		 
    		 String method = request.getRequestLine().getMethod().toUpperCase(Locale.ENGLISH);
             if (!"POST".equals(method)) {
                 response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                 response.addHeader("Content-Type", "text/plain");
                 response.setEntity(new NStringEntity("method not supported"));
                 trigger.submitResponse(response);
                 return;
             }
             
             User usr = null;
             if (request instanceof HttpEntityEnclosingRequest) {
                 HttpEntity entity = ((HttpEntityEnclosingRequest) request).getEntity();
                 usr = mapper.mapJsonUser(entity.getContent());
             }
             
             if (usr == null){
                 response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
                 response.addHeader("Content-Type", "text/plain");
                 response.setEntity(new NStringEntity("Bad User format"));
                 trigger.submitResponse(response);
                 return;
             }
             
/*				if (manager.getUser(usr.getMail()) == null) {
					manager.save(usr);
					response.setStatusCode(HttpStatus.SC_CREATED);
					response.addHeader("Content-Type", "text/plain");
					 try {
						 response.setEntity(new NStringEntity("User created"));
					} catch (UnsupportedEncodingException e) {
						LOG.error("Failed to set response string encoding", e);
					}
				}
				else {
	                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
	                response.addHeader("Content-Type", "text/plain");
	                try {
						response.setEntity(new NStringEntity("Bad User"));
					} catch (UnsupportedEncodingException e) {
						LOG.error("Failed to set response string encoding", e);
					}
				}
				trigger.submitResponse(response);*/

             executor.execute(new Operation(response, usr, trigger, manager, context));
    	}
    }
    
    static class Operation implements Runnable {
    	
    	HttpResponse response;
    	User user;
    	NHttpResponseTrigger trigger;
    	UserManager manager;
    	HttpContext context;
    	
    
		
			public Operation(HttpResponse response, User user,
				NHttpResponseTrigger trigger, UserManager manager, HttpContext context) {
			super();
			this.response = response;
			this.user = user;
			this.trigger = trigger;
			this.manager = manager;
			this.context = context;
		}




			@Override
			public void run() {
				// If user does not exist
				if (manager.getUser(user.getMail()) == null) {
					manager.save(user);
					response.setStatusCode(HttpStatus.SC_CREATED);
					response.addHeader("Content-Type", "text/plain");
					 try {
						 response.setEntity(new NStringEntity("User created"));
					} catch (UnsupportedEncodingException e) {
						LOG.error("Failed to set response string encoding", e);
					}
				}
				else {
	                response.setStatusCode(HttpStatus.SC_BAD_REQUEST);
	                response.addHeader("Content-Type", "text/plain");
	                try {
						response.setEntity(new NStringEntity("Bad User"));
					} catch (UnsupportedEncodingException e) {
						LOG.error("Failed to set response string encoding", e);
					}
				}
				trigger.submitResponse(response);
//				handler.responseReady((NHttpServerConnection)context.getAttribute(ExecutionContext.HTTP_CONNECTION));
			}
		}

     
     

 
    
    	


    static class EventLogger implements EventListener {

        public void connectionOpen(final NHttpConnection conn) {
            LOG.debug("Connection open: {}" , conn);
        }

        public void connectionTimeout(final NHttpConnection conn) {
        	LOG.warn("Connection timed out: {}" , conn);
        }

        public void connectionClosed(final NHttpConnection conn) {
        	LOG.debug("Connection closed: {}" , conn);
        }

        public void fatalIOException(final IOException ex, final NHttpConnection conn) {
        	LOG.error("I/O error" , ex);
        }

        public void fatalProtocolException(final HttpException ex, final NHttpConnection conn) {
        	LOG.error("HTTP Protocol error" , ex);
        }

    }

}
