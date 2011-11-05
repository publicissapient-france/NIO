package fr.xebia.usi.quizz.service;

import java.io.IOException;
import java.io.InputStream;

import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.util.TokenBuffer;
import org.glassfish.grizzly.http.server.io.NIOReader;
import org.glassfish.grizzly.http.server.io.ReadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.xebia.usi.quizz.model.User;

public class JsonMapperImpl implements JsonMapper {

	private final ObjectMapper mapper;
	private final JsonFactory factory;
	
	private final Logger LOG = LoggerFactory.getLogger("json");
	
	public JsonMapperImpl() {
		mapper = new ObjectMapper();
		factory = new JsonFactory();
		DeserializationConfig conf = mapper.getDeserializationConfig();
		conf.disable(Feature.FAIL_ON_UNKNOWN_PROPERTIES);
		
		
	}
	
	
	public User mapJsonUser (byte[ ] bt){

		try {
			return mapUserInternal(factory.createJsonParser(bt));
		} catch (JsonParseException e) {
			LOG.error("JsonParser creation failed on stream", e);
		} catch (IOException e) {
			LOG.error("I/O Error while creating JsonParser from stream", e);
		}
		
		return null;
	}
	
	@Override
	public User mapJsonUser(InputStream stream) {
		
		try {
			return mapUserInternal(factory.createJsonParser(stream));
		} catch (JsonParseException e) {
			LOG.error("JsonParser creation failed on stream", e);
		} catch (IOException e) {
			LOG.error("I/O Error while creating JsonParser from stream", e);
		}
		
		return null;
	}
	
	private User mapUserInternal(JsonParser jp) {
		
		User res = null;
		try {
			res = mapper.readValue(jp, User.class);

		} catch (JsonParseException e) {
			LOG.warn("Problem parsing JSON user", e);
		} catch (JsonMappingException e) {
			LOG.warn("Problem mapping JSON to User object", e);
		} catch (IOException e) {
			LOG.error("I/O Error while reading user in stream", e);
		}
		return res;
	}
	

	@Override
	public User mapJsonUser(NIOReader stream) {

		return null;
	}
}
