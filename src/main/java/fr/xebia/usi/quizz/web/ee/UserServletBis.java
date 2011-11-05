package fr.xebia.usi.quizz.web.ee;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutorService;

import fr.xebia.usi.quizz.model.User;
import fr.xebia.usi.quizz.service.JsonMapper;
import fr.xebia.usi.quizz.service.JsonMapperImpl;
import fr.xebia.usi.quizz.service.UserManager;
import fr.xebia.usi.quizz.service.UserManagerMemoryImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebServlet(name = "user-servlet-bis", asyncSupported = true, urlPatterns = { "/api/user-bis" })
public class UserServletBis extends HttpServlet {

	private ExecutorService executor;

	private static final Logger LOG = LoggerFactory.getLogger("api/user-bis");
	private JsonMapper mapper;
	private UserManager manager;

	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		mapper = new JsonMapperImpl();
		manager = new UserManagerMemoryImpl();
	//	executor = Executors.newFixedThreadPool(5);
		LOG.info("Instance loading of the UserServiceBis");
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		super.destroy();
		executor.shutdownNow();
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		this.doPost(req, resp);

	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

//		if (null != req.getAttribute("error")) {

//			if ((Boolean) req.getAttribute("error")) {
				
//			}
//		} else if (!req.isAsyncStarted()) {

	//		AsyncContext ctx = req.startAsync(req, resp);
			// Parse JSON
			User user = null;
			try {
				user = mapper.mapJsonUser(req
						.getInputStream());
			} catch (IOException e1) {
				LOG.error("Failed to write success response", e1);
			}
//			executor.execute(new AsyncUserSave(manager, mapper, ctx, user) {
//
//				@Override
//				public void run() {


					if (user != null && user.isValidForSave()) {
						if (manager.getUser(user.getMail()) == null) {
							manager.save(user);
							try {

								resp.getWriter().print(
										"OK User saved :)");
							} catch (IOException e) {
								LOG
										.error(
												"Failed to write success response",
												e);
							}
						resp.flushBuffer();
						} else {
							resp.sendError(400, "Bad User Request");
						
						}

					} else {
						resp.sendError(400, "Bad User Request");
					}

//				}
//			});
//		}

	}

	abstract class AsyncUserSave implements Runnable {

		final UserManager manager;
		final JsonMapper jsonMapper;
		final AsyncContext ctx;
		final User usr;

		public AsyncUserSave(UserManager manager, JsonMapper jsonMapper,
				AsyncContext ctx, User usr) {
			this.manager = manager;
			this.jsonMapper = jsonMapper;
			this.ctx = ctx;
			this.usr = usr;
		}

	}

}
