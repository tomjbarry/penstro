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

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.EventDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Event;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.enumeration.EVENT_TYPE;

public class EventDaoImpl implements EventDaoCustom {
	
	private static final String CREATED = Event.CREATED;
	private static final String AUTHOR_ID = Event.AUTHOR + "." + CachedUsername.OID;
	private static final String AUTHOR_NAME = Event.AUTHOR + "." + CachedUsername.USERNAME;
	private static final String AUTHOR_EXISTS = Event.AUTHOR + "." + CachedUsername.EXISTS;
	private static final String TARGET_ID = Event.TARGET + "." + CachedUsername.OID;
	private static final String TARGET_NAME = Event.TARGET + "." + CachedUsername.USERNAME;
	private static final String TARGET_EXISTS = Event.TARGET + "." + CachedUsername.EXISTS;
	private static final String BENEFICIARY_ID = Event.BENEFICIARY + "." + CachedUsername.OID;
	private static final String BENEFICIARY_NAME = Event.BENEFICIARY + "." + CachedUsername.USERNAME;
	private static final String BENEFICIARY_EXISTS = Event.BENEFICIARY + "." + CachedUsername.EXISTS;
	private static final String TARGETS = Event.TARGETS;
	private static final String TYPE = Event.TYPE;
	
	@Autowired
	private MongoTemplate mongoTemplate;

	protected String convertType(EVENT_TYPE type) {
		if(type != null) {
			return type.toString();
		}
		return null;
	}
	
	protected DBObject createQueryObject(ObjectId id) {
		QueryBuilder builder = QueryBuilder.start();
		builder.or(new BasicDBObject(TARGET_ID, id), new BasicDBObject(BENEFICIARY_ID, id));
		return builder.get();
	}
	
	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0?.getTarget().getId()", condition = "#event.getTarget() != null"), 
			@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0?.getBeneficiary().getId()", condition = "#event.getBeneficiary() != null")})
	*/
	@Override
	public void insertEvent(Event event) throws DaoException {
		CheckUtil.nullCheck(event);
		
		try {
			mongoTemplate.insert(event, CollectionNames.EVENT);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	// caching probably will not be beneficial here
	@Override
	public Page<Event> findEvents(ObjectId author, ObjectId target, 
			List<String> types, Date time, Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.EVENT);
		
		CheckUtil.nullCheck(pageable);
		
		DBCursor cursor = null;
		
		DBObject query = new BasicDBObject();
		if(target != null) {
			query = createQueryObject(target);
		}
		if(types != null && !types.isEmpty()) {
			query.put(TYPE, new BasicDBObject(DaoQueryStrings.IN, types));
		}
		if(time != null) {
			query.put(CREATED, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, time));
		}
		if(author != null) {
			query.put(AUTHOR_ID, author);
		}
		
		try {
			cursor = collection.find(query)
				.sort(new BasicDBObject(CREATED,DaoValues.SORT_DESCENDING))
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize());
		} catch(Exception e) {
			throw new DaoException(e);
		}
		if(cursor == null) {
			throw new DaoException();
		}
		
		List<Event> events = new ArrayList<Event>();
		
		while(cursor.hasNext()) {
			DBObject obj = cursor.next();
			Event event = mongoTemplate.getConverter().read(Event.class,  obj);
			events.add(event);
		}
		
		return new PageImpl<Event>(events, pageable, cursor.count());
	}
	
	// caching probably will not be beneficial here
	@Override
	public Page<Event> findEventsMultipleAuthors(List<ObjectId> authors, ObjectId target, 
			List<String> types, Date time, Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.EVENT);
		
		CheckUtil.nullCheck(pageable);
		
		DBCursor cursor = null;
		
		DBObject query = new BasicDBObject();
		if(authors != null) {
			if(authors.size() <= 0) {
				return new PageImpl<Event>(new ArrayList<Event>(), pageable, 0);
			} else if(authors.size() == 1) {
				query.put(AUTHOR_ID, authors.get(0));
			} else {
				query.put(AUTHOR_ID, new BasicDBObject(DaoQueryStrings.IN, authors));
			}
		}
		if(types != null && !types.isEmpty()) {
			query.put(TYPE, new BasicDBObject(DaoQueryStrings.IN, types));
		}
		if(time != null) {
			query.put(CREATED, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, time));
		}
		if(target != null) {
			query = createQueryObject(target);
		}
		
		try {
			cursor = collection.find(query)
				.sort(new BasicDBObject(CREATED,DaoValues.SORT_DESCENDING))
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize());
		} catch(Exception e) {
			throw new DaoException(e);
		}
		if(cursor == null) {
			throw new DaoException();
		}
		
		List<Event> events = new ArrayList<Event>();
		
		while(cursor.hasNext()) {
			DBObject obj = cursor.next();
			Event event = mongoTemplate.getConverter().read(Event.class,  obj);
			events.add(event);
		}
		
		return new PageImpl<Event>(events, pageable, cursor.count());
	}
	
	@Override
	public void rename(ObjectId userId, String replacement, EVENT_TYPE type, 
			boolean isAuthor, boolean isTarget, boolean isBeneficiary, String previous, 
			String targetsKey) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.EVENT);
		CheckUtil.nullCheck(userId);
		
		DBObject query = new BasicDBObject();
		DBObject set = new BasicDBObject();
		
		if(isAuthor) {
			query.put(AUTHOR_ID, userId);
			set.put(AUTHOR_NAME, replacement);
		} else if(isTarget) {
			query.put(TARGET_ID, userId);
			set.put(TARGET_NAME, replacement);
		} else if(isBeneficiary) {
			query.put(BENEFICIARY_ID, userId);
			set.put(BENEFICIARY_NAME, replacement);
		} else if(previous != null && targetsKey != null) {
			query.put(TARGETS + "." + targetsKey, previous);
		}
		if(type != null) {
			query.put(TYPE, convertType(type));
		}
		
		if(targetsKey != null) {
			set.put(TARGETS + "." + targetsKey, replacement);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void removeUser(ObjectId userId, boolean isAuthor, boolean isTarget, 
			boolean isBeneficiary) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.EVENT);
		CheckUtil.nullCheck(userId);
		
		DBObject query = new BasicDBObject();
		DBObject set = new BasicDBObject();
		
		if(isAuthor) {
			query.put(AUTHOR_ID, userId);
			set.put(AUTHOR_EXISTS, false);
		} else if(isTarget) {
			query.put(TARGET_ID, userId);
			set.put(TARGET_EXISTS, false);
		} else if(isBeneficiary) {
			query.put(BENEFICIARY_ID, userId);
			set.put(BENEFICIARY_EXISTS, false);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
}
