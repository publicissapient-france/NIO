package fr.xebia.usi.quizz.web.netty.rest;



import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

import fr.xebia.usi.quizz.model.User;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.UserManager;


public class UserRestService extends RestService{
	private final UserManager manager;
	private final JsonMapper mapper;
	private final Executor executor;
	
	public UserRestService(UserManager _manager, JsonMapper _mapper ) {
		this.manager = _manager;
		this.mapper = _mapper;
		this.executor = Executors.newFixedThreadPool(10);
	}
	
	
	@Override
	public void post(String path, ChannelHandlerContext ctx, MessageEvent e) {

		HttpRequest request = (HttpRequest) e.getMessage();
		
		final User user = mapper.mapJsonUser(request.getContent().array());
		final ChannelHandlerContext _ctx = ctx; 
		final MessageEvent _e = e;

		if (isUserValid(user)) {

					if (manager.getUser(user.getMail()) == null) {
						manager.save(user);
						writeResponse("OK User saved :)", HttpResponseStatus.CREATED, _ctx, _e, false);
					}
					else {
						writeResponse("Bad User Request", HttpResponseStatus.BAD_REQUEST, _ctx, _e, false);
					}
				}

			
		
		else {
			writeResponse("Bad User Request", HttpResponseStatus.BAD_REQUEST, ctx, e, false);
		}
	}


	private boolean isUserValid(User user) {
		return user != null && user.isValidForSave();
	}	
	
}
