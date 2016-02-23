package com.py.py.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.UserDaoCustom;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.User;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.subdomain.EmailToken;
import com.py.py.domain.subdomain.LoginAttempt;
import com.py.py.enumeration.LOCK_REASON;

public class UserDaoImpl implements UserDaoCustom {
	
	private static final String ID = User.ID;
	private static final String EMAIL = User.EMAIL;
	private static final String UNIQUE_NAME = User.UNIQUE_NAME;
	private static final String USERNAME = User.USERNAME;
	private static final String UNIQUE_EMAIL = User.UNIQUE_EMAIL;
	private static final String PASSWORD = User.PASSWORD;
	private static final String PAYMENT_ID = User.PAYMENT_ID;
	
	private static final String ROLES = User.ROLES;
	private static final String OVERRIDE_ROLES = User.OVERRIDE_ROLES;

	private static final String LOGIN_ATTEMPTS = User.LOGIN_ATTEMPTS;
	private static final String LOGIN_ATTEMPT_TIME = LoginAttempt.TIME;
	private static final String LOGIN_ATTEMPT_SUCCESS = LoginAttempt.SUCCESS;
	private static final String LOCATION = LoginAttempt.LOCATION;
	//private static final String LOCATION_NAME = Location.NAME;
	//private static final String LOCATION_IP = Location.IP;
	private static final String SAVED_LOCATIONS = User.SAVED_LOCATIONS;
	
	private static final String EMAIL_TOKENS = User.EMAIL_TOKENS;
	private static final String EMAIL_TOKENS_CREATED = EmailToken.CREATED;
	private static final String EMAIL_TOKENS_TOKEN = EmailToken.TOKEN;
	private static final String EMAIL_TOKENS_TYPE = EmailToken.TYPE;
	
	private static final String LOCKED_UNTIL = User.LOCKED_UNTIL;
	private static final String SUSPENSIONS = User.SUSPENSIONS;
	private static final String REASON = User.LOCKREASON;
	
	private static final String DELETED = User.DELETED;
	private static final String RENAME = User.RENAME;
	
	private static final String PENDING_SCHEMA_UPDATES = User.PENDING_SCHEMA_UPDATES;
	
	private static final String LAST_ATTEMPT = User.LAST_ATTEMPT;
	private static final String PASSWORD_FAILS = User.PASSWORD_FAILS;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER, key = "#p0.getId()"),
			@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0.getId()")})
	*/
	@Override
	public User create(User user) throws DaoException {
		CheckUtil.nullCheck(user);
		try {
			mongoTemplate.insert(user, CollectionNames.USER);
			return user;
		} catch(DuplicateKeyException dk) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public User findByUniqueName(String username) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(username);
		
		try {
			DBObject obj = collection.findOne(
					new BasicDBObject(UNIQUE_NAME, username.toLowerCase()));
			
			if(obj == null) {
				return null;
			}
			
			User user = mongoTemplate.getConverter().read(User.class, obj);
			return user;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public User findByEmail(String email) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(email);
		
		try {
			DBObject obj = collection.findOne(
					new BasicDBObject(UNIQUE_EMAIL, email.toLowerCase()));
			
			if(obj == null) {
				return null;
			}
			
			User user = mongoTemplate.getConverter().read(User.class, obj);
			return user;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER, key = "#p0"), 
			@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0"), 
			@CacheEvict(value = CacheNames.USER_ID_EMAIL, key = "#p1")})
	*/
	@Override
	public void updateUser(ObjectId id, String email, String password) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject set = new BasicDBObject();
		if(email != null) {
			set.put(EMAIL, email);
			set.put(UNIQUE_EMAIL, email.toLowerCase());
		}
		if(password != null) {
			set.put(PASSWORD, password);
		}
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, set);
		
		try {
			collection.update(query, update);
		} catch(DuplicateKeyException dk) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER, key = "#p0"), 
			@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0")})
	*/
	@Override
	public void updatePayment(ObjectId id, String paymentId) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject set = new BasicDBObject();
		set.put(PAYMENT_ID, paymentId);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, set);
		
		try {
			collection.update(query, update);
		} catch(DuplicateKeyException dk) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER, key = "#p0"), 
			@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0")})
	*/
	@Override
	public void addLoginAttempt(ObjectId id, LoginAttempt loginAttempt)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, loginAttempt);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject attempt = new BasicDBObject();
		attempt.put(LOGIN_ATTEMPT_TIME, loginAttempt.getTime());
		attempt.put(LOGIN_ATTEMPT_SUCCESS, loginAttempt.isSuccess());
		attempt.put(LOCATION, loginAttempt.getLocation());
		
		List<DBObject> attemptList = new ArrayList<DBObject>();
		attemptList.add(attempt);
		
		DBObject push = new BasicDBObject();
		push.put(DaoQueryStrings.EACH, attemptList);
		//push.put(DaoQueryStrings.SORT, new BasicDBObject(LOGIN_ATTEMPT_TIME, DaoValues.SORT_ASCENDING));
		push.put(DaoQueryStrings.SLICE, (0 - ServiceValues.MAX_LOGIN_COUNT));
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PUSH, new BasicDBObject(LOGIN_ATTEMPTS, push));
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER, key = "#p0"), 
			@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0")})
	*/
	@Override
	public void clearLoginAttempts(ObjectId id, Date date, Boolean success)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject update = new BasicDBObject();
		if(success != null) {
			update.put(LOGIN_ATTEMPT_SUCCESS, success);
		}
		if(date != null) {
			update.put(LOGIN_ATTEMPT_TIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, date));
		}
			
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.PULL, 
							new BasicDBObject(LOGIN_ATTEMPTS, update)));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	/*
	@Override
	public void addAction(ObjectId id, Date date) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, date);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, 
				new BasicDBObject(LAST_ACTION, date));
			
		try {
			collection.update(query, update, false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	*/
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void addEmailToken(ObjectId id, String token, EMAIL_TYPE type) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, token);
		
		Date now = new Date();
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject tokenObject = new BasicDBObject();
		tokenObject.put(EMAIL_TOKENS_CREATED, now);
		tokenObject.put(EMAIL_TOKENS_TOKEN, token);
		if(type != null) {
			tokenObject.put(EMAIL_TOKENS_TYPE, type.toString());
		}
		
		List<DBObject> eachList = new ArrayList<DBObject>();
		eachList.add(tokenObject);
		
		DBObject pushObject = new BasicDBObject();
		pushObject.put(DaoQueryStrings.EACH, eachList);
		// no longer care about the order, push appends to the end
		//pushObject.put(DaoQueryStrings.SORT, new BasicDBObject(EMAIL_TOKENS_CREATED, DaoValues.SORT_DESCENDING));
		pushObject.put(DaoQueryStrings.SLICE, (0 - ServiceValues.MAX_EMAIL_TOKENS));
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PUSH, new BasicDBObject(EMAIL_TOKENS, pushObject));
			
		try {
			collection.update(query, update, false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public boolean hasEmailToken(ObjectId id, String token, EMAIL_TYPE type) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, token);
		
		Date now = new Date();
		Date then = new Date(now.getTime() - ServiceValues.EMAIL_TOKEN_EXPIRATION);
		
		DBObject query = new BasicDBObject(ID, id);
		query.put(EMAIL_TOKENS + "." + EMAIL_TOKENS_CREATED, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, then));
		query.put(EMAIL_TOKENS + "." + EMAIL_TOKENS_TOKEN, token);
		if(type != null) {
			query.put(EMAIL_TOKENS + "." + EMAIL_TOKENS_TYPE, type.toString());
		}	
		
		try {
			DBObject obj = collection.findOne(query);
			if(obj == null) {
				return false;
			}
			User user = mongoTemplate.getConverter().read(User.class, obj);
			if(user != null) {
				return true;
			}
			return false;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void removeEmailToken(ObjectId id, String token, EMAIL_TYPE type) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, token);
		
		DBObject query = new BasicDBObject(ID, id);
		query.put(EMAIL_TOKENS + "." + EMAIL_TOKENS_TOKEN, token);
		if(type != null) {
			query.put(EMAIL_TOKENS + "." + EMAIL_TOKENS_TYPE, type.toString());
		}
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PULL, new BasicDBObject(EMAIL_TOKENS, 
				new BasicDBObject(EMAIL_TOKENS_TOKEN, token)));
			
		try {
			collection.update(query, update, false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void updateRoles(ObjectId id, List<String> roles, List<String> overrideRoles) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
			
		if(roles != null) {
			set.put(ROLES, roles);
		}
		if(overrideRoles != null) {
			set.put(OVERRIDE_ROLES, overrideRoles);
		}
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void addRole(ObjectId id, String role, String overrideRole) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		// use add to set instead of push in case multiple roles are 
		// pushed and only 1 exists
		DBObject add = new BasicDBObject();
			
		if(role != null) {
			add.put(ROLES, role);
		}
		if(overrideRole != null) {
			add.put(OVERRIDE_ROLES, overrideRole);
		}
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.ADD_TO_SET, add));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void removeRole(ObjectId id, String role, String overrideRole) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject pull = new BasicDBObject();
			
		if(role != null) {
			pull.put(ROLES, role);
		}
		if(overrideRole != null) {
			pull.put(OVERRIDE_ROLES, overrideRole);
		}
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.PULL, pull));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER, key = "#p0"), 
			@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0")})
	*/
	@Override
	public void addLocation(ObjectId id, String location) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, location);
		
		DBObject query = new BasicDBObject(ID, id);
		/*query.put(SAVED_LOCATIONS + "." + LOCATION_NAME, 
				new BasicDBObject(DaoQueryStrings.NOT_EQUAL, location.getName()));
		*/
		/*
		DBObject locationObject = new BasicDBObject();
		locationObject.put(LOCATION_IP, location.getIp());
		locationObject.put(LOCATION_NAME, location.getName());
		*/
		
		BasicDBList eachList = new BasicDBList();
		eachList.add(location);
		
		DBObject pushObject = new BasicDBObject();
		pushObject.put(DaoQueryStrings.EACH, eachList);
		pushObject.put(DaoQueryStrings.SLICE, ServiceValues.LOCATIONS_MAX);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PUSH, new BasicDBObject(SAVED_LOCATIONS, pushObject));
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER, key = "#p0"), 
			@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0")})
	*//*
	@Override
	public void removeLocation(ObjectId id, Location location) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, location);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject locationObject = new BasicDBObject(LOCATION_NAME, location.getName());
		DBObject pull = new BasicDBObject(SAVED_LOCATIONS, locationObject);
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.PULL, pull));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}*/
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void updateStatus(ObjectId id, Date lockedUntil, Long suspensions,
			LOCK_REASON reason, boolean noLocked) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		if(noLocked) {
			set.put(LOCKED_UNTIL, null);
		} else if(lockedUntil != null) {
			set.put(LOCKED_UNTIL, lockedUntil);
		}
		if(suspensions != null) {
			set.put(SUSPENSIONS, suspensions);
		}
		if(reason != null) {
			set.put(REASON, reason.toString());
		}
	
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void setDeleted(ObjectId id, Date deletedDate) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		set.put(DELETED, deletedDate);
	
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void rename(ObjectId id, String rename, String replacement) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		DBObject unset = null;
		if(rename != null) {
			set.put(RENAME, rename);
		}
		if(replacement != null) {
			set.put(USERNAME, replacement);
			set.put(UNIQUE_NAME, replacement.toLowerCase());
			unset = new BasicDBObject(RENAME, 1);
		}
		DBObject update = new BasicDBObject(DaoQueryStrings.SET, set);
		if(unset != null) {
			update.put(DaoQueryStrings.UNSET, unset);
		}
		
		try {
			collection.update(query, update);
		} catch(DuplicateKeyException dke) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<User> getRename(Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(pageable);
		
		DBObject query = new BasicDBObject(RENAME, 
				new BasicDBObject(DaoQueryStrings.EXISTS, 1));
		
		try {
			DBCursor cursor = collection.find(query)
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());

			if(cursor == null) {
				throw new DaoException();
			}
			
			List<User> users = new ArrayList<User>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				User user = mongoTemplate.getConverter().read(User.class,  obj);
				users.add(user);
			}
			
			return new PageImpl<User>(users, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<User> getDeleted(Date olderThanDeleted, Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(olderThanDeleted, pageable);
		
		DBObject query = new BasicDBObject(DELETED, 
				new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanDeleted));
		
		try {
			DBCursor cursor = collection.find(query)
					.sort(new BasicDBObject(DELETED, DaoValues.SORT_DESCENDING))
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());

			if(cursor == null) {
				throw new DaoException();
			}
			
			List<User> users = new ArrayList<User>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				User user = mongoTemplate.getConverter().read(User.class,  obj);
				users.add(user);
			}
			
			return new PageImpl<User>(users, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void doPendingSchemaUpdate(ObjectId id, String field, String value, String addPending, String removePending) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id, field);
		
		DBObject query = new BasicDBObject(ID, id);
		DBObject set = new BasicDBObject(field, value);
		DBObject update = new BasicDBObject(DaoQueryStrings.SET, set);
		if(addPending != null && !addPending.isEmpty()) {
			update.put(DaoQueryStrings.ADD_TO_SET, new BasicDBObject(PENDING_SCHEMA_UPDATES, addPending));
		}
		if(removePending != null && !removePending.isEmpty()) {
			update.put(DaoQueryStrings.PULL, new BasicDBObject(PENDING_SCHEMA_UPDATES, removePending));
		}
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void setPasswordAttempts(ObjectId id, int fails, Date when, boolean setNotIncrement) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		DBObject update = new BasicDBObject();
		if(setNotIncrement) {
			DBObject set = new BasicDBObject();
			if(when == null) {
				update.put(DaoQueryStrings.UNSET, new BasicDBObject(LAST_ATTEMPT, ""));
			} else {
				set.put(LAST_ATTEMPT, when);
			}
			set.put(PASSWORD_FAILS, fails);
			update.put(DaoQueryStrings.SET, set);
		} else {
			if(fails == 0 && when == null) {
				return;
			}
			if(fails != 0) {
				update.put(DaoQueryStrings.INCREMENT, new BasicDBObject(PASSWORD_FAILS, fails));
			}
			if(when != null) {
				update.put(DaoQueryStrings.SET, new BasicDBObject(LAST_ATTEMPT, when));
			}
		}
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

}
