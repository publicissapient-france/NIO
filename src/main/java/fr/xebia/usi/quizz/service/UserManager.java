package fr.xebia.usi.quizz.service;

import fr.xebia.usi.quizz.model.User;

public interface UserManager {
	
	/**
	 * Return user if found or null
	 * 
	 * @param mail
	 * @return
	 */
	User getUser(String mail);
	

	/**
	 * Save the given user 
	 * 
	 * @param usr
	 * @return
	 */
	User save(User usr);

}
