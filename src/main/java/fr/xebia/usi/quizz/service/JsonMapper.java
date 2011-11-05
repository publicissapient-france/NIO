package fr.xebia.usi.quizz.service;

import java.io.InputStream;

import org.glassfish.grizzly.http.server.io.NIOReader;

import fr.xebia.usi.quizz.model.User;

public interface JsonMapper {
	
	/**
	 * 
	 * @param stream
	 * @return Null on error or the User object
	 */
	User mapJsonUser(InputStream stream);
	
	/**
	 * Deserialize a User from Json text inside the byte array bt
	 * @param bt
	 * @return
	 */
	User mapJsonUser (byte[ ] bt);
	
	/**
	 * 
	 * @param stream
	 * @return Null on error or the User object
	 */
	User mapJsonUser(NIOReader stream);

}
