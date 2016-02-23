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
import com.py.py.dao.custom.AdminDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.AdminAction;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.DTO;
import com.py.py.enumeration.ADMIN_STATE;
import com.py.py.enumeration.ADMIN_TYPE;
import com.py.py.util.PyUtils;

public class AdminDaoImpl implements AdminDaoCustom {

	private static final String ID = AdminAction.ID;
	private static final String ADMIN_ID = AdminAction.ADMIN + "." + CachedUsername.OID;
	private static final String STATE = AdminAction.STATE;
	private static final String TYPE = AdminAction.TYPE;
	private static final String TARGET = AdminAction.TARGET;
	//private static final String CREATED = "created";
	private static final String LAST_MODIFIED = AdminAction.LAST_MODIFIED;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public AdminAction createAction(CachedUsername admin, ADMIN_STATE state, 
			ADMIN_TYPE type, String target, DTO dto, Object reference) throws DaoException {

		Date created = new Date();
		AdminAction action = new AdminAction();
		action.setId(new ObjectId());
		action.setAdmin(admin);
		action.setCreated(created);
		action.setLastModified(created);
		if(state != null) {
			action.setState(state);
		}
		if(type != null) {
			action.setType(type);
		}
		if(reference != null) {
			action.setReference(reference);
		}
		action.setTarget(target);
		action.setDto(dto);
		try {
			mongoTemplate.insert(action, CollectionNames.ADMIN);
		} catch(Exception e) {
			throw new DaoException(e);
		}
		return action;
	}
	
	@Override
	public void updateAction(ObjectId id, ADMIN_STATE state) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ADMIN);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		set.put(STATE, state.toString());
		set.put(LAST_MODIFIED, new Date());
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<AdminAction> findSortedActions(ObjectId adminId, List<ADMIN_STATE> states, 
			ADMIN_TYPE type, String target, Date olderThanModified, Pageable pageable, 
			int direction) 
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ADMIN);

		CheckUtil.nullCheck(pageable);
		
		DBObject query = new BasicDBObject();
		if(states != null) {
			query.put(STATE, new BasicDBObject(DaoQueryStrings.IN, 
					PyUtils.stringifiedList(states)));
		}
		if(olderThanModified != null) {
			query.put(LAST_MODIFIED, 
					new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanModified));
		}
		if(adminId != null) {
			query.put(ADMIN_ID, adminId);
		}
		if(type != null) {
			query.put(TYPE, type.toString());
		}
		if(target != null) {
			query.put(TARGET, target);
		}
		
		int sort = direction;
		if(sort != DaoValues.SORT_ASCENDING && sort != DaoValues.SORT_DESCENDING) {
			sort = DaoValues.SORT_DESCENDING;
		}
		
		try {
			DBCursor cursor = collection.find(query)
					.sort(new BasicDBObject(LAST_MODIFIED, sort))
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());

			if(cursor == null) {
				throw new DaoException();
			}
			
			List<AdminAction> actions = new ArrayList<AdminAction>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				AdminAction a = mongoTemplate.getConverter().read(AdminAction.class,  obj);
				actions.add(a);
			}
			
			return new PageImpl<AdminAction>(actions, pageable, cursor.count());
			
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void remove(List<ADMIN_STATE> states, Date olderThanModified)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ADMIN);
		
		DBObject query = new BasicDBObject();
		if(states != null && !states.isEmpty()) {
			query.put(STATE, new BasicDBObject(DaoQueryStrings.IN, 
					PyUtils.stringifiedList(states)));
		}
		if(olderThanModified != null) {
			query.put(LAST_MODIFIED, 
					new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanModified));
		}
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
