package fr.xebia.usi.quizz.web.grizzly;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.xebia.usi.quizz.model.User;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMemoryImpl;
import org.glassfish.grizzly.http.server.HttpRequestProcessor;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple User service Grizzly implementation
 * 
 * @author slm
 * 
 */
public class UserGrizzlyService extends HttpRequestProcessor {

	private static final Logger LOG = LoggerFactory.getLogger("grizzly");
	private final JsonMapper mapper;
	private final UserManager manager;

	private final ExecutorService executor;

	public UserGrizzlyService() {
		mapper = new JsonMapperImpl();
		manager = new UserManagerMemoryImpl();
		executor = Executors.newFixedThreadPool(10);
	}

	@Override
	public void service(final Request request, final Response response)
			throws Exception {
		// Parse JSON
		User user = null;
		try {
			user = mapper.mapJsonUser(request.createInputStream());
		} catch (IOException e1) {
			LOG.error("Failed to write success response", e1);
		}
		
		if (user == null){
			response.sendError(400, "Bad user format");
		}
		else {
			response.getWriter();
			response.suspend();
			final User usr = user;
			executor.execute(new Runnable() {
	
				@Override
				public void run() {
	
					if (usr != null && usr.isValidForSave()) {
						if (manager.getUser(usr.getMail()) == null) {
							manager.save(usr);
							try {
	
								response.getWriter().append("OK User saved :)");
							} catch (IOException e) {
								LOG.error("Failed to write success response", e);
							}
	
							response.resume();
						} else {
							try {
								response.sendError(400, "User already exist !");
							} catch (IOException e) {
								LOG.error("Failed to write error response", e);
							}
						}
	
					} else {
						try {
							response.sendError(400, "Bad user Format !");
						} catch (IOException e) {
							LOG.error("Failed to write error response", e);
						}
					}
	
				}
			});
		}
	}

}
