package com.py.py.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteResult;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.constants.CacheNames;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.custom.SubscriptionDaoCustom;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.exception.NoDocumentFoundException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.dao.util.DBObjectConverter;
import com.py.py.domain.Subscription;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.FollowInfo;
import com.py.py.util.PyUtils;

public class SubscriptionDaoImpl implements SubscriptionDaoCustom {

	private static final String ID = Subscription.ID;
	private static final String FOLLOWS = Subscription.FOLLOWS;
	private static final String BLOCKED = Subscription.BLOCKED;
	private static final String USERNAME = FollowInfo.USERNAME;
	private static final String USERNAME_ID = USERNAME + "." + CachedUsername.OID;
	private static final String USERNAME_USERNAME = USERNAME + "." + CachedUsername.USERNAME;
	private static final String USERNAME_EXISTS = USERNAME + "." + CachedUsername.EXISTS;
	private static final String ADDED = FollowInfo.ADDED;
	private static final String FOLLOWS_USERNAME_ID = FOLLOWS + "." + USERNAME_ID;
	private static final String FOLLOWS_USERNAME_USERNAME = FOLLOWS + "." + USERNAME_USERNAME;
	private static final String BLOCKED_USERNAME_ID = BLOCKED + "." + USERNAME_ID;
	private static final String BLOCKED_USERNAME_USERNAME = BLOCKED + "." + USERNAME_USERNAME;
	private static final String HIDDEN_FEED_EVENTS = Subscription.HIDDEN_FEED_EVENTS;
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	/*
	@Override
	@CacheEvict(value = CacheNames.SUBSCRIPTION, key = "#p0")
	*/
	@Override
	public void createSubscription(ObjectId id, List<CachedUsername> followIds) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);
		
		CheckUtil.nullCheck(id, followIds);
		
		List<DBObject> followsList = new ArrayList<DBObject>();
		Date now = new Date();
		
		for(CachedUsername cu : followIds) {
			// do not use converter to prevent unnecessary _class field
			DBObject map = new BasicDBObject();
			map.put(USERNAME, DBObjectConverter.convertCachedUsername(cu));
			map.put(ADDED, now);
			followsList.add(map);
		}
		
		DBObject insert = new BasicDBObject();
		insert.put(ID, id);
		insert.put(FOLLOWS, followsList);
		insert.put(HIDDEN_FEED_EVENTS, new ArrayList<DBObject>());
		
		try {
			collection.insert(insert);
		} catch(DuplicateKeyException dk) {
			// do nothing
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.SUBSCRIPTION, key = "#p0")
	*/
	@Override
	public void addSubscription(ObjectId id, CachedUsername followId) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);
		
		CheckUtil.nullCheck(id, followId);
		
		Date now = new Date();
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(FOLLOWS_USERNAME_ID, 
				new BasicDBObject(DaoQueryStrings.NOT_EQUAL, followId.getId()));
		
		DBObject follow = new BasicDBObject();
		follow.put(USERNAME, DBObjectConverter.convertCachedUsername(followId));
		follow.put(ADDED, now);
		
		List<DBObject> eachList = new ArrayList<DBObject>();
		eachList.add(follow);
		
		DBObject pushObject = new BasicDBObject();
		pushObject.put(DaoQueryStrings.EACH, eachList);
		// no longer sort. push adds to end, take first MAX_FOLLOWEES so this has no effect
		//pushObject.put(DaoQueryStrings.SORT, new BasicDBObject(ADDED, DaoValues.SORT_DESCENDING));
		pushObject.put(DaoQueryStrings.SLICE, ServiceValues.MAX_FOLLOWEES);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PUSH, new BasicDBObject(FOLLOWS, pushObject));
		
		try {
			collection.update(query, update, true, false);
		} catch(DuplicateKeyException dk) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.SUBSCRIPTION, key = "#p0")
	*/
	@Override
	public void addBlocked(ObjectId id, CachedUsername blockedId) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);
		
		CheckUtil.nullCheck(id, blockedId);
		
		Date now = new Date();
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(BLOCKED_USERNAME_ID, 
				new BasicDBObject(DaoQueryStrings.NOT_EQUAL, blockedId.getId()));
		
		DBObject follow = new BasicDBObject();
		follow.put(USERNAME, DBObjectConverter.convertCachedUsername(blockedId));
		follow.put(ADDED, now);
		
		List<DBObject> eachList = new ArrayList<DBObject>();
		eachList.add(follow);
		
		DBObject pushObject = new BasicDBObject();
		pushObject.put(DaoQueryStrings.EACH, eachList);
		//pushObject.put(DaoQueryStrings.SORT, new BasicDBObject(ADDED, DaoValues.SORT_DESCENDING));
		pushObject.put(DaoQueryStrings.SLICE, ServiceValues.MAX_BLOCKED);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PUSH, new BasicDBObject(BLOCKED, pushObject));
		
		try {
			collection.update(query, update, true, false);
		} catch(DuplicateKeyException dk) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	// must allow for potentially deleted followers which have no available objectId
	/*
	@Override
	@CacheEvict(value = CacheNames.SUBSCRIPTION, key = "#p0")
	*/
	@Override
	public void removeSubscription(ObjectId id, String followName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);
		
		CheckUtil.nullCheck(id, followName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(FOLLOWS_USERNAME_USERNAME, followName);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PULL, 
				new BasicDBObject(FOLLOWS, new BasicDBObject(USERNAME_USERNAME, followName)));
		
		try {
			WriteResult result = collection.update(query, update);
			if(result == null) {
				// do not throw an error in case write concern is strict
				// must manually check if its contained using another call in the service
				return;
			}
			if(result.getN() <= 0) {
				throw new NoDocumentFoundException();
			}
		} catch(NoDocumentFoundException ndfe) {
			throw ndfe;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	// must allow for potentially deleted followers which have no available objectId
	/*
	@Override
	@CacheEvict(value = CacheNames.SUBSCRIPTION, key = "#p0")
	*/
	@Override
	public void removeBlocked(ObjectId id, String blockedName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);
		
		CheckUtil.nullCheck(id, blockedName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(BLOCKED_USERNAME_USERNAME, blockedName);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PULL, 
				new BasicDBObject(BLOCKED, new BasicDBObject(USERNAME_USERNAME, blockedName)));
		
		try {
			WriteResult result = collection.update(query, update);
			if(result == null) {
				// do not throw an error in case write concern is strict
				// must manually check if its contained using another call in the service
				return;
			}
			if(result.getN() <= 0) {
				throw new NoDocumentFoundException();
			}
		} catch(NoDocumentFoundException ndfe) {
			throw ndfe;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<FollowInfo> getSortedSubscribed(ObjectId id, Pageable pageable) 
			throws DaoException {
		CheckUtil.nullCheck(id, pageable);
		
		Page<FollowInfo> page = new PageImpl<FollowInfo>(new ArrayList<FollowInfo>(), 
				pageable, 0);
		try {
			Subscription subscription = getCachedSubscription(id);
			if(subscription == null || subscription.getFollows() == null) {
				return page;
			}
			
			int start = pageable.getOffset();
			int end = start + pageable.getPageSize();
			int size = subscription.getFollows().size();

			if(end >= size) {
				end = size;
			}
			if(start > end || start > size) {
				return page;
			}
			
			List<FollowInfo> follows = subscription.getFollows().subList(start, end);
			
			return new PageImpl<FollowInfo>(follows, pageable, size);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<FollowInfo> getSortedBlocked(ObjectId id, Pageable pageable) 
			throws DaoException {
		CheckUtil.nullCheck(id, pageable);
		
		Page<FollowInfo> page = new PageImpl<FollowInfo>(new ArrayList<FollowInfo>(), 
				pageable, 0);
		try {
			Subscription subscription = getCachedSubscription(id);
			if(subscription == null || subscription.getBlocked() == null) {
				return page;
			}
			
			int start = pageable.getOffset();
			int end = start + pageable.getPageSize();
			int size = subscription.getBlocked().size();

			if(end >= size) {
				end = size;
			}
			if(start > end || start > size) {
				return page;
			}
			
			List<FollowInfo> blocked = subscription.getBlocked().subList(start, end);
			
			return new PageImpl<FollowInfo>(blocked, pageable, size);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public FollowInfo subscribed(ObjectId id, ObjectId followId) throws DaoException {
		CheckUtil.nullCheck(id, followId);
		
		try {
			Subscription subscription = getSubscription(id);
			List<FollowInfo> followInfo = subscription.getFollows();
			if(followInfo != null) {
				for(FollowInfo f : followInfo) {
					if(f != null && f.getUsername() != null
							&& PyUtils.objectIdCompare(followId, 
									f.getUsername().getId())) {
						return f;
					}
				}
			}
			return null;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public FollowInfo blocked(ObjectId id, ObjectId blockedId) throws DaoException {
		CheckUtil.nullCheck(id, blockedId);
		
		try {
			Subscription subscription = getSubscription(id);
			List<FollowInfo> followInfo = subscription.getBlocked();
			if(followInfo != null) {
				for(FollowInfo f : followInfo) {
					if(f != null && f.getUsername() != null
							&& PyUtils.objectIdCompare(blockedId, 
									f.getUsername().getId())) {
						return f;
					}
				}
			}
			return null;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.SUBSCRIPTION, key = "#p0")
	public Subscription getCachedSubscription(ObjectId id) throws DaoException {
		return getSubscription(id);
	}
	
	@Override
	public Subscription getSubscription(ObjectId id) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		try {
			DBObject obj = collection.findOne(query);
			if(obj != null) {
				Subscription subscription = mongoTemplate.getConverter()
						.read(Subscription.class, obj);
				return subscription;
			}
			return null;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.SUBSCRIPTION, key = "#p0")
	*/
	@Override
	public void setHiddenFeed(ObjectId id, List<String> hiddenFeed) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);
		
		CheckUtil.nullCheck(id, hiddenFeed);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject update = new BasicDBObject();
		if(hiddenFeed != null) {
			update.put(DaoQueryStrings.SET, 
					new BasicDBObject(HIDDEN_FEED_EVENTS, hiddenFeed));
		}
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.SUBSCRIPTION, allEntries = true)
	*/
	@Override
	public void rename(ObjectId userId, String replacement, boolean asFollows) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);

		CheckUtil.nullCheck(userId, replacement);
		
		DBObject query = new BasicDBObject();
		
		DBObject obj = new BasicDBObject();
		obj.put(USERNAME, DBObjectConverter.convertCachedUsername(
				new CachedUsername(userId, replacement)));
		obj.put(ADDED, new Date());
		
		DBObject set = new BasicDBObject();
		if(asFollows) {
			query.put(FOLLOWS_USERNAME_ID, userId);
			set.put(FOLLOWS + ".$", obj);
		} else {
			query.put(BLOCKED_USERNAME_ID, userId);
			set.put(BLOCKED + ".$", obj);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(DuplicateKeyException dk) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.SUBSCRIPTION, allEntries = true)
	*/
	@Override
	public void removeUser(ObjectId userId) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.SUBSCRIPTION);

		CheckUtil.nullCheck(userId);
		QueryBuilder builder = QueryBuilder.start();
		
		DBObject followerQuery = new BasicDBObject(FOLLOWS_USERNAME_ID, userId);
		
		DBObject blockedQuery = new BasicDBObject(BLOCKED_USERNAME_ID, userId);
		
		builder.or(followerQuery, blockedQuery);
		
		DBObject usernameExists = new BasicDBObject(USERNAME_EXISTS, false);
		
		DBObject pull = new BasicDBObject();
		pull.put(FOLLOWS, usernameExists);
		pull.put(BLOCKED, usernameExists);
		
		try {
			collection.update(builder.get(), new BasicDBObject(DaoQueryStrings.PULL, pull), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
