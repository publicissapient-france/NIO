package fr.xebia.usi.quizz.web.restlet;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import fr.xebia.usi.quizz.model.User;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMemoryImpl;
import org.restlet.Component;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.data.MediaType;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserServiceResource extends Restlet {

	private static final Logger LOG = LoggerFactory.getLogger("restlet");

	public static void main(String[] args) throws Exception {
		// Create the HTTP server and listen on port 8182
		// Create a new Restlet component and add a HTTP server connector to it
		Component component = new Component();
		
		component.getServers().add(Protocol.HTTP, 8080);
		component.getDefaultHost().attach("/api/user",
				new UserServiceResource());

		component.getServers().get(0).getContext().getParameters().add(
				"maxTotalConnections", "-1");
		component.getServers().get(0).getContext().getParameters().add(
				"maxThreads", "100");
		component.start();
	}

	private final UserManager manager;
	private final JsonMapper mapper;
	private final ExecutorService executor;

	public UserServiceResource() {
		mapper = new JsonMapperImpl();
		manager = new UserManagerMemoryImpl();
		executor = Executors.newFixedThreadPool(10);
	}

	@Override
	public void handle(Request request, Response response) {

		// super.handle(request, response);
		User usr = null;
		if (request.getMethod().equals(Method.POST)) {
			try {
				usr = mapper.mapJsonUser(request.getEntity().getStream());
			} catch (IOException e) {
				LOG.error("Failed to map request entity to a JSON User", e);
			}

			if (usr != null && usr.isValidForSave()) {
				final Response resp = response;
				final User user = usr;
				response.setAutoCommitting(false);
				executor.execute(new Runnable() {

					@Override
					public void run() {
						if (manager.getUser(user.getMail()) == null) {
							manager.save(user);

							resp.setStatus(Status.SUCCESS_OK);
							resp.setEntity("OK User saved :)",
									MediaType.TEXT_PLAIN);
							resp.commit();
							return;
						}
					}
				});
			}
		}
		response.setAutoCommitting(true);
		response.setStatus(Status.CLIENT_ERROR_BAD_REQUEST);
		response.setEntity("Bad request", MediaType.TEXT_PLAIN);
	}
}