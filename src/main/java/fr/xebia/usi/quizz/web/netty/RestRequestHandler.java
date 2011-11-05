package fr.xebia.usi.quizz.web.netty;


import java.util.HashMap;
import java.util.Map;

import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMemoryImpl;
import fr.xebia.usi.quizz.web.netty.rest.RestService;
import fr.xebia.usi.quizz.web.netty.rest.UserRestService;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;

public class RestRequestHandler {

    private Map<String, RestService> restMapping = new HashMap<String, RestService>();
	private final UserManager manager;
	private final JsonMapper mapper;

    public RestRequestHandler() {
    	mapper = new JsonMapperImpl();
    	manager = new UserManagerMemoryImpl();
        // Create all reste resources
        restMapping.put("user", new UserRestService(manager, mapper));
        // ...
    }

    public void messageReceived(String path, ChannelHandlerContext ctx, MessageEvent e) {
        HttpRequest request = (HttpRequest) e.getMessage();
        // Define rest service to use
        String serviceToUse = path;
        if (path.indexOf("/") > 0) {
            serviceToUse = path.substring(0, path.indexOf("/"));
        }
        if (request.getMethod().equals(HttpMethod.GET)) {
            restMapping.get(serviceToUse).get(path, ctx, e);
        }
        else if (request.getMethod().equals(HttpMethod.POST)) {
            restMapping.get(serviceToUse).post(path, ctx, e);
        }
        
    }
}
