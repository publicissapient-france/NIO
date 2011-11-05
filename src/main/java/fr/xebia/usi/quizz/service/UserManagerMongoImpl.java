package fr.xebia.usi.quizz.service;

import java.net.UnknownHostException;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.MongoException;
import com.mongodb.MongoOptions;
import com.mongodb.WriteResult;
import fr.xebia.usi.quizz.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserManagerMongoImpl implements UserManager{
	
	private static final Logger LOG = LoggerFactory.getLogger("mongo");
	private final Mongo mongo;
	private final DB db;
	private final DBCollection coll;

	
	public UserManagerMongoImpl() {

		try {
			MongoOptions opts = new MongoOptions();
			opts.threadsAllowedToBlockForConnectionMultiplier = 10;
			opts.connectionsPerHost = 20;
			mongo  = new Mongo("localhost", opts);

			db = mongo.getDB("users");
			coll = db.getCollection("users");
		} catch (UnknownHostException e) {
			LOG.error("Host Not Found on localhost while connecting to MongoDb", e);
			throw new RuntimeException("No connection to Db",e);
		} catch (MongoException e) {
			LOG.error("Host Not Found on localhost while connecting to MongoDb", e);
			throw new RuntimeException("No connection to Db",e);
		}
	}
	
	@Override
	public User getUser(String mail) {
		
		User res = null;
		DBObject obj = new BasicDBObject();
		obj.put("mail", mail);
		obj = coll.findOne(obj);
		
		LOG.debug("User found in mongo : {}", obj);
		if (obj != null){
			res = new User();
			res.setFirstname(obj.get("firstname").toString());
			res.setLastname(obj.get("lastname").toString());
			res.setMail(mail);
			res.setPassword(obj.get("password").toString());			
		}
		
		return res;
	}
	
	@Override
	public User save(User usr) {

        DBObject obj = new BasicDBObject();
        obj.put("mail", usr.getMail());
        obj.put("firstname",usr.getFirstname());
		obj.put("lastname", usr.getLastname());
		obj.put("password", usr.getPassword());
		
		WriteResult wr = coll.insert(obj);

		return usr;
	}

}
