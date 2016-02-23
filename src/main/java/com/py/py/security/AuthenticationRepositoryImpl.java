package com.py.py.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.dao.util.DBObjectConverter;
import com.py.py.domain.User;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.subdomain.AuthenticationInformation;

public class AuthenticationRepositoryImpl {

	private static final String ID = User.ID;
	//private static final String UNIQUE_TOKEN = User.TOKEN;
	private static final String AUTHENTICATION_INFORMATION = User.AUTHENTICATION_INFORMATION;
	private static final String SAVED_LOCATIONS = User.SAVED_LOCATIONS;
	
	private static final String TOKEN = AUTHENTICATION_INFORMATION + "." + AuthenticationInformation.TOKEN;
	private static final String EXPIRY = AUTHENTICATION_INFORMATION + "." + AuthenticationInformation.EXPIRY;
	
	@Autowired
	protected MongoTemplate mongoTemplate;

	/*
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	public void addToken(ObjectId id, byte[] token, Date expiry, long inactivity) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, token, expiry);
		
		AuthenticationInformation aI = new AuthenticationInformation(token, expiry, inactivity);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);

		List<DBObject> addList = new ArrayList<DBObject>();
		addList.add(DBObjectConverter.convertAuthenticationInformation(aI));
		
		DBObject push = new BasicDBObject();
		push.put(DaoQueryStrings.EACH, addList);
		push.put(DaoQueryStrings.SORT, new BasicDBObject(EXPIRY, DaoValues.SORT_ASCENDING));
		push.put(DaoQueryStrings.SLICE, (0 - ServiceValues.MAX_CONCURRENT_LOGINS));
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PUSH, new BasicDBObject(AUTHENTICATION_INFORMATION, push));
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	public void updateToken(ObjectId id, byte[] token, Date expiry) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, token, expiry);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(TOKEN, token);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, new BasicDBObject(AUTHENTICATION_INFORMATION + ".$." + AuthenticationInformation.EXPIRY, expiry));
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	public String getToken(ObjectId id) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put(ID, id);
		
		try {
			DBObject obj = collection.findOne(new BasicDBObject(queryMap));
			User user = mongoTemplate.getConverter().read(User.class, obj);
			if(user == null) {
				return null;
			}
			return user.getToken();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	*/

	/*
	public User getUserByToken(ObjectId id, String token) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(token);
		
		Map<String, Object> queryMap = new HashMap<String, Object>();
		queryMap.put(ID, id);
		queryMap.put(UNIQUE_TOKEN, token);
		
		try {
			DBObject obj = collection.findOne(new BasicDBObject(queryMap));
			User user = mongoTemplate.getConverter().read(User.class, obj);
			if(user == null) {
				return null;
			}
			return user;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	*/

	public void removeUserToken(ObjectId id, byte[] token) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, token);
		
		DBObject query = new BasicDBObject(ID, id);
	
		DBObject update = new BasicDBObject(DaoQueryStrings.PULL, 
				new BasicDBObject(AUTHENTICATION_INFORMATION, 
						new BasicDBObject(AuthenticationInformation.TOKEN, token)));

		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	public void removeAllUserTokens(ObjectId id) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.UNSET, new BasicDBObject(AUTHENTICATION_INFORMATION, ""));
		update.put(DaoQueryStrings.SET, new BasicDBObject(SAVED_LOCATIONS, new BasicDBList()));
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
		
	}
}
