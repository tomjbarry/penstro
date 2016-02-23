package com.py.py.dao.impl;

import java.util.ArrayList;
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
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.MessageDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Message;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.constants.IndexNames;
import com.py.py.domain.subdomain.CachedUsername;

public class MessageDaoImpl implements MessageDaoCustom {

	private static final String CREATED = Message.CREATED;
	private static final String AUTHOR = Message.AUTHOR;
	private static final String AUTHOR_USERNAME = AUTHOR + "." + CachedUsername.USERNAME;
	private static final String AUTHOR_EXISTS = AUTHOR + "." + CachedUsername.EXISTS;
	private static final String AUTHOR_ID = AUTHOR + "." + CachedUsername.OID;
	private static final String TARGET = Message.TARGET;
	private static final String TARGET_USERNAME = TARGET + "." + CachedUsername.USERNAME;
	private static final String TARGET_EXISTS = TARGET + "." + CachedUsername.EXISTS;
	private static final String TARGET_ID = TARGET + "." + CachedUsername.OID;
	private static final String READ = Message.READ;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	protected DBObject createQueryObject() {
		DBObject obj = new BasicDBObject();
		obj.put(AUTHOR_EXISTS, true);
		obj.put(TARGET_EXISTS, true);
		return obj;
	}
	
	protected String getHint(ObjectId authorId, ObjectId targetId) {
		if(authorId != null) {
			if(targetId != null) {
				return IndexNames.MESSAGE_AUTHOR_TARGET_CREATED;
			} else {
				return IndexNames.MESSAGE_AUTHOR_CREATED;
			}
		}
		if(targetId != null){
			return IndexNames.MESSAGE_TARGET_CREATED;
		}
		return IndexNames.MESSAGE_AUTHOR_TARGET_CREATED;
	}
	
	@Override
	public Page<Message> getMessages(ObjectId authorId, ObjectId targetId, 
			Pageable pageable, Boolean read) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.MESSAGE);
		
		CheckUtil.nullCheck(pageable);
		
		DBCursor cursor = null;
		
		DBObject query = new BasicDBObject();
		if(authorId != null) {
			query.put(AUTHOR_ID, authorId);
		}
		if(targetId != null) {
			query.put(TARGET_ID, targetId);
		}
		query.putAll(createQueryObject());
		if(read != null) {
			query.put(READ, read);
		}
		
		try {
			cursor = collection.find(query)
				.sort(new BasicDBObject(CREATED, DaoValues.SORT_DESCENDING))
				.hint(getHint(authorId, targetId))
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize());
		} catch(Exception e) {
			throw new DaoException(e);
		}
		if(cursor == null) {
			throw new DaoException();
		}
		
		List<Message> messages = new ArrayList<Message>();
		
		while(cursor.hasNext()) {
			DBObject obj = cursor.next();
			Message message = mongoTemplate.getConverter().read(Message.class,  obj);
			messages.add(message);
		}
		
		return new PageImpl<Message>(messages, pageable, cursor.count());
	}
	
	@Override
	public Page<Message> getMessagesAll(ObjectId authorId, ObjectId targetId, Pageable pageable)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.MESSAGE);
		
		CheckUtil.nullCheck(pageable);
		
		DBCursor cursor = null;
		
		DBObject query = new BasicDBObject();
		query.put(AUTHOR_ID, authorId);
		query.put(TARGET_ID, targetId);
		
		DBObject queryInverse = new BasicDBObject();
		queryInverse.put(AUTHOR_ID, targetId);
		queryInverse.put(TARGET_ID, authorId);

		query.putAll(createQueryObject());
		queryInverse.putAll(createQueryObject());
		
		BasicDBList queryList = new BasicDBList();
		queryList.add(query);
		queryList.add(queryInverse);
		
		try {
			cursor = collection.find(new BasicDBObject(DaoQueryStrings.OR, queryList))
				.sort(new BasicDBObject(CREATED, DaoValues.SORT_DESCENDING))
				.hint(IndexNames.MESSAGE_AUTHOR_TARGET_CREATED)
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize());
		} catch(Exception e) {
			throw new DaoException(e);
		}
		if(cursor == null) {
			throw new DaoException();
		}
		
		List<Message> messages = new ArrayList<Message>();
		
		while(cursor.hasNext()) {
			DBObject obj = cursor.next();
			Message message = mongoTemplate.getConverter().read(Message.class,  obj);
			messages.add(message);
		}
		
		return new PageImpl<Message>(messages, pageable, cursor.count());
	}
	
	@Override
	public void mark(ObjectId authorId, ObjectId targetId, boolean read) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.MESSAGE);
		
		DBObject query = new BasicDBObject();
		if(authorId != null) {
			query.put(AUTHOR_ID, authorId);
		}
		if(targetId != null) {
			query.put(TARGET_ID, targetId);
		}
		query.put(READ, !read);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, new BasicDBObject(READ, read));
		
		try {
			collection.update(query, update, false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	public void updateStatus(ObjectId authorId, ObjectId targetId, boolean flagged) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.MESSAGE);
		
		DBObject query = new BasicDBObject();
		if(authorId != null) {
			query.put(AUTHOR_ID, authorId);
		}
		if(targetId != null) {
			query.put(TARGET_ID, targetId);
		}
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, new BasicDBObject(FLAGGED, flagged));
		
		try {
			collection.update(query, update, false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}*/
	
	@Override
	public void rename(ObjectId userId, String replacement, boolean asAuthor) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.MESSAGE);
		
		CheckUtil.nullCheck(userId, replacement);
		DBObject query = new BasicDBObject();
		
		DBObject set = new BasicDBObject();
		
		if(asAuthor) {
			query.put(AUTHOR_ID, userId);
			set.put(AUTHOR_USERNAME, replacement);
		} else {
			query.put(TARGET_ID, userId);
			set.put(TARGET_USERNAME, replacement);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void removeUser(ObjectId userId, boolean asAuthor) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.MESSAGE);
		
		CheckUtil.nullCheck(userId);
		DBObject query = new BasicDBObject();
		
		DBObject set = new BasicDBObject();
		
		if(asAuthor) {
			query.put(AUTHOR_ID, userId);
			set.put(AUTHOR_EXISTS, false);
		} else {
			query.put(TARGET_ID, userId);
			set.put(TARGET_EXISTS, false);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

}
