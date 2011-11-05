package fr.xebia.usi.quizz.web.grizzly;

import java.io.IOException;

import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.ServerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrizzlyHttpServer {

	private static final Logger LOG = LoggerFactory.getLogger("grizzly");
	
    public static void main(String[] args) {
  
         // create a basic server that listens on port 8080.
         final HttpServer server = HttpServer.createSimpleServer();
 
         final ServerConfiguration config = server.getServerConfiguration();
 
         // Map the path, /api/user, to the NonBlockingUserService
         config.addHttpService(new UserGrizzlyService(), "/api/user");
         config.setJmxEnabled(true);

 
        try {
             server.start();
             // Server start is not blocking :p
             Thread.currentThread().suspend();
         } catch (IOException ioe) {
             LOG.error("Failed to start the grizzly server", ioe);
         } 
         

         // For now never stop the server
     }
	
}
