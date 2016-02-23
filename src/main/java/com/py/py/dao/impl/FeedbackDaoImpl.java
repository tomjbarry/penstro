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
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.FeedbackDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Feedback;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.constants.IndexNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.enumeration.FEEDBACK_CONTEXT;
import com.py.py.enumeration.FEEDBACK_STATE;
import com.py.py.enumeration.FEEDBACK_TYPE;

public class FeedbackDaoImpl implements FeedbackDaoCustom {

	public static final String ID = Feedback.ID;
	public static final String TYPE = Feedback.TYPE;
	public static final String STATE = Feedback.STATE;
	public static final String CONTEXT = Feedback.CONTEXT;
	public static final String AUTHOR = Feedback.AUTHOR;
	public static final String AUTHOR_ID = AUTHOR + "." + CachedUsername.OID;
	public static final String AUTHOR_NAME = AUTHOR + "." + CachedUsername.USERNAME;
	public static final String AUTHOR_EXISTS = AUTHOR + "." + CachedUsername.EXISTS;
	public static final String CREATED = Feedback.CREATED;
	public static final String LAST_MODIFIED = Feedback.LAST_MODIFIED;
	public static final String SUMMARY = Feedback.SUMMARY;
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	@Override
	public Page<Feedback> getFeedbacks(FEEDBACK_TYPE type, FEEDBACK_STATE state, 
			FEEDBACK_CONTEXT context, ObjectId author, Pageable pageable, int direction) 
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FEEDBACK);
		
		CheckUtil.nullCheck(pageable);
		
		DBObject query = new BasicDBObject();
		if(state != null) {
			query.put(STATE, state.toString());
		}
		if(type != null) {
			query.put(TYPE, type.toString());
		}
		if(context != null) {
			query.put(CONTEXT, context.toString());
		}
		if(author != null) {
			query.put(AUTHOR_ID, author);
		}
		
		int sort = direction;
		if(sort != DaoValues.SORT_ASCENDING && sort != DaoValues.SORT_DESCENDING) {
			sort = DaoValues.SORT_DESCENDING;
		}
		
		try {
			DBCursor cursor = collection.find(query)
					.sort(new BasicDBObject(LAST_MODIFIED, sort))
					.hint(IndexNames.FEEDBACK_STATE_TYPE_CONTEXT_LAST_MODIFIED)
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());

			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Feedback> feedbacks = new ArrayList<Feedback>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Feedback feedback = mongoTemplate.getConverter().read(Feedback.class,  obj);
				feedbacks.add(feedback);
			}
			
			return new PageImpl<Feedback>(feedbacks, pageable, cursor.count());
			
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void updateFeedback(List<ObjectId> ids, FEEDBACK_TYPE type, FEEDBACK_STATE state, 
			FEEDBACK_CONTEXT context, String summary) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FEEDBACK);
		
		CheckUtil.nullCheck(ids);
		
		DBObject query = new BasicDBObject();
		query.put(ID, new BasicDBObject(DaoQueryStrings.IN, ids));
		
		DBObject set = new BasicDBObject();
		if(type != null) {
			set.put(TYPE, type.toString());
		}
		if(state != null) {
			set.put(STATE, state.toString());
		}
		if(context != null) {
			set.put(CONTEXT, context.toString());
		}
		if(summary != null) {
			set.put(SUMMARY, summary);
		}
		set.put(LAST_MODIFIED, new Date());
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void rename(ObjectId userId, String replacement) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FEEDBACK);
		
		CheckUtil.nullCheck(userId);
		
		DBObject query = new BasicDBObject();
		query.put(AUTHOR_ID, userId);
		
		DBObject set = new BasicDBObject();
		set.put(AUTHOR_NAME, replacement);
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void removeUser(ObjectId userId) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FEEDBACK);
		
		CheckUtil.nullCheck(userId);
		
		DBObject query = new BasicDBObject();
		query.put(AUTHOR_ID, userId);
		
		DBObject set = new BasicDBObject();
		set.put(AUTHOR_EXISTS, false);
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
