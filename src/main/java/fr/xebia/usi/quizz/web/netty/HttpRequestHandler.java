package fr.xebia.usi.quizz.web.netty;


import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class HttpRequestHandler extends SimpleChannelUpstreamHandler {

    private RestRequestHandler restRequestHandler;

    private CachedResourcesRequestHandler staticRequestHandler;

    public HttpRequestHandler(RestRequestHandler restRequestHandler, CachedResourcesRequestHandler staticRequestHandler){
        this.restRequestHandler = restRequestHandler;
        this.staticRequestHandler = staticRequestHandler;
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

        HttpRequest request = (HttpRequest) e.getMessage();
        /**
         *  if pattern /api then use RestHandler
         *  if pattern /static then use static handler
         *  if pattern / redirect to /static/html/index.html
         */
        String uri = request.getUri();
        if(uri != null){
            if(uri.startsWith("/api/")){
                restRequestHandler.messageReceived(uri.substring(5), ctx, e);
            }
            else if(uri.startsWith("/static/")){
                staticRequestHandler.messageReceived(ctx, e);
            }
            else if(uri.equals("/")){

            }
        }
    }

}
