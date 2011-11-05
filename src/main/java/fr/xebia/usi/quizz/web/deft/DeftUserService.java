package fr.xebia.usi.quizz.web.deft;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import fr.xebia.usi.quizz.model.User;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMemoryImpl;
import org.apache.deft.configuration.Configuration;
import org.apache.deft.web.Application;
import org.apache.deft.web.AsyncResult;
import org.apache.deft.web.HttpServer;
import org.apache.deft.web.handler.RequestHandler;
import org.apache.deft.web.http.HttpRequest;
import org.apache.deft.web.http.HttpResponse;
import org.apache.deft.web.http.HttpServerDescriptor;
import org.apache.deft.web.http.protocol.HttpStatus;


/**
 * Try it on Deft
 * @author slm
 * 33k req/s 100K user
 */
public class DeftUserService extends RequestHandler {

	private final UserManager manager;
	private final JsonMapper mapper;
	private final Executor executor;

	
	public DeftUserService() {
		this.manager = new UserManagerMemoryImpl();
		this.mapper = new JsonMapperImpl();
		this.executor = Executors.newFixedThreadPool(10);

	}
	
	
        @Override
        public void get(HttpRequest request, HttpResponse response) {
        	 response.setStatus(HttpStatus.CLIENT_ERROR_BAD_REQUEST);
            response.write("No GET Service for now !");
        }

        @Override
        public void post(final HttpRequest request, final HttpResponse response) {
        	
  
        	final User usr = mapper.mapJsonUser(request.getBody().getBytes());
        	
        	if (usr == null){
        		response.setStatus(HttpStatus.CLIENT_ERROR_BAD_REQUEST);
        		response.write("Bad user Json format !").finish();
        		return;
        	}
        	
        	final UserCallBack cb = new UserCallBack(request, response);
        	

            if (manager.getUser(usr.getMail()) == null) {
						manager.save(usr);
						response.setStatus(HttpStatus.SUCCESS_CREATED);
        		response.write("OK User saved :)");
					}
					else {
						response.setStatus(HttpStatus.CLIENT_ERROR_BAD_REQUEST);
        		response.write("Bad user");
					}

        }
        
        
        private class UserCallBack implements AsyncResult<User> {
        	final HttpRequest request;
        	final HttpResponse response;
        	
        	public UserCallBack(HttpRequest request, HttpResponse response) {
				this.request = request;
				this.response = response;
			}
        	
        	
        	@Override
        	public void onFailure(Throwable arg0) {
        	


        	}
        	
        	@Override
        	public void onSuccess(User arg0) {
        		

        	}
        }

    public static void main(String[] args) {
        final Map<String, RequestHandler> handlers = new HashMap<String, RequestHandler>();
        handlers.put("/", new RequestHandler() {
        	
        	@Override
        	public void get(HttpRequest request, HttpResponse response) {
        		super.get(request, response);
        	}
        	
		});
        handlers.put("/api/user", new DeftUserService());
        HttpServerDescriptor.KEEP_ALIVE_TIMEOUT = 30 * 1000;	// 30s
		HttpServerDescriptor.READ_BUFFER_SIZE = 1500;			// 1500 bytes 
		HttpServerDescriptor.WRITE_BUFFER_SIZE = 1500;			// 1500 bytes 

        try {
            Configuration configuration = new Configuration();
			HttpServer server = new HttpServer(configuration){
                @Override
                protected Application createApplication(String packageName) {
                    return new Application(handlers);    //To change body of overridden methods use File | Settings | File Templates.
                }
            };
			server.listen(8080);
			server.start(5);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
    }
	
	
}
