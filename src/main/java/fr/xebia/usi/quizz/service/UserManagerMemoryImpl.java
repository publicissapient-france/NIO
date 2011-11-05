package fr.xebia.usi.quizz.service;

import java.util.HashMap;
import java.util.Map;

import fr.xebia.usi.quizz.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManagerMemoryImpl implements UserManager{

	private static final Logger LOG = LoggerFactory.getLogger(UserManagerMemoryImpl.class);


    private final Map<String, User> userDb;


	public UserManagerMemoryImpl() {
        userDb = new HashMap<String, User>(100000);
	}
	
	@Override
	public User getUser(String mail) {
		
		User res = null;

        res = userDb.get(mail);
		
		return res;
	}
	
	@Override
	public User save(User usr) {

        userDb.put(usr.getMail(), usr);
		return usr;
	}

}
