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
import com.py.py.dao.custom.EmailDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.EmailTask;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.enumeration.TASK_STATE;

public class EmailDaoImpl implements EmailDaoCustom {

	public static final String ID = EmailTask.ID;
	public static final String COMPLETED = EmailTask.COMPLETED;
	public static final String CREATED = EmailTask.CREATED;
	public static final String STATE = EmailTask.STATE;
	public static final String TYPE = EmailTask.TYPE;
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	@Override
	public Page<EmailTask> findNonCompleteTasks(Pageable pageable, TASK_STATE state, 
			EMAIL_TYPE type) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TASK_EMAIL);
		
		CheckUtil.nullCheck(pageable);
		
		DBObject query = new BasicDBObject();
		query.put(COMPLETED, null);
		if(state != null) {
			query.put(STATE, state.toString());
		}
		if(type != null) {
			query.put(TYPE, type.toString());
		}
		
		try {
			DBCursor cursor = collection.find(query)
					.sort(new BasicDBObject(CREATED, DaoValues.SORT_ASCENDING))
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());

			if(cursor == null) {
				throw new DaoException();
			}
			
			List<EmailTask> tasks = new ArrayList<EmailTask>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				EmailTask task = mongoTemplate.getConverter().read(EmailTask.class,  obj);
				tasks.add(task);
			}
			
			return new PageImpl<EmailTask>(tasks, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void updateTask(ObjectId id, Date completed, TASK_STATE state) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TASK_EMAIL);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		if(state != null) {
			set.put(STATE, state.toString());
		}
		
		if(completed != null) {
			set.put(COMPLETED, completed);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void cleanupTasks(Date created, Date completed, TASK_STATE state, 
			EMAIL_TYPE type) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TASK_EMAIL);
		
		DBObject query = new BasicDBObject();
		if(state != null) {
			query.put(STATE, state.toString());
		}
		if(type != null) {
			query.put(TYPE, type.toString());
		}
		if(completed != null) {
			query.put(COMPLETED, new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, completed));
		}
		if(created != null) {
			query.put(CREATED, new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, created));
		}
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
