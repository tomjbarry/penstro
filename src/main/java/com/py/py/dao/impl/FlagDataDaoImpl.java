package com.py.py.dao.impl;

import java.util.ArrayList;
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
import com.mongodb.DuplicateKeyException;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.FlagDataDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.FlagData;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.subdomain.FlagInfo;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.FLAG_TYPE;

public class FlagDataDaoImpl implements FlagDataDaoCustom {

	//private static final String ID = FlagData.ID;
	private static final String REFERENCE_ID = FlagData.ID + "." + FlagInfo.REFERENCE_ID;
	private static final String TYPE = FlagData.ID + "." + FlagInfo.TYPE;
	private static final String TARGET = FlagData.TARGET;
	private static final String VALUE = FlagData.VALUE;
	private static final String TOTAL = FlagData.TOTAL;
	private static final String REASONS = FlagData.REASONS;
	
	@Autowired
	private MongoTemplate mongoTemplate;

	protected String getFlagReason(FLAG_REASON reason) throws DaoException {
		CheckUtil.nullCheck(reason);
		return REASONS + "." + reason.toString();
	}
	
	@Override
	public Page<FlagData> findSorted(FLAG_TYPE type, Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FLAG_DATA);

		CheckUtil.nullCheck(pageable);
		
		DBObject query = new BasicDBObject();
		if(type != null) {
			query.put(TYPE, type.toString());
		}
		
		try {
			DBCursor cursor = collection.find(query)
					.sort(new BasicDBObject(VALUE, DaoValues.SORT_DESCENDING))
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());

			if(cursor == null) {
				throw new DaoException();
			}
			
			List<FlagData> datas = new ArrayList<FlagData>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				FlagData fd = mongoTemplate.getConverter().read(FlagData.class,  obj);
				datas.add(fd);
			}
			
			return new PageImpl<FlagData>(datas, pageable, cursor.count());
			
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void addData(ObjectId id, FLAG_TYPE type, String target, long weight, FLAG_REASON reason) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FLAG_DATA);
		
		CheckUtil.nullCheck(id, type, target, reason);
		String typeString = type.toString();
		String reasonString = getFlagReason(reason);
		
		DBObject query = new BasicDBObject();
		query.put(REFERENCE_ID, id);
		query.put(TYPE, typeString);

		DBObject increment = new BasicDBObject();
		increment.put(VALUE, weight);
		increment.put(TOTAL, weight);
		increment.put(reasonString, weight);
		
		DBObject update = new BasicDBObject(DaoQueryStrings.INCREMENT, increment);
		DBObject updateObject = new BasicDBObject();
		updateObject.putAll(update);
		DBObject setOnInsert = new BasicDBObject();
		setOnInsert.putAll(query);
		setOnInsert.put(TARGET, target);
		updateObject.put(DaoQueryStrings.SET_ON_INSERT, setOnInsert);
		
		try {
			collection.update(query, updateObject, true, false);
		} catch(DuplicateKeyException dke) {
			try {
				DBObject insert = new BasicDBObject();
				insert.put(REFERENCE_ID, id);
				insert.put(TYPE, type.toString());
				insert.put(TARGET, target);
				insert.putAll(increment);
				collection.insert(insert);
			} catch(DuplicateKeyException d) {
				collection.update(query, update, false, false);
			} catch(Exception e) {
				throw new DaoException(e);
			}
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void decrement(long amount) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FLAG_DATA);
		
		DBObject increment = new BasicDBObject();
		increment.put(VALUE, 0 - amount);
		
		try {
			collection.update(new BasicDBObject(), new BasicDBObject(DaoQueryStrings.INCREMENT, increment), false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void remove(ObjectId id, FLAG_TYPE type) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FLAG_DATA);
		CheckUtil.nullCheck(id, type);
		
		DBObject query = new BasicDBObject();
		query.put(REFERENCE_ID, id);
		query.put(TYPE, type.toString());
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void remove(long threshold) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FLAG_DATA);
		
		DBObject query = new BasicDBObject(VALUE, new BasicDBObject(DaoQueryStrings.LESS_THAN, threshold));
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void rename(ObjectId userId, String replacement) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.FLAG_DATA);
		
		CheckUtil.nullCheck(userId, replacement);

		DBObject query = new BasicDBObject();
		DBObject set = new BasicDBObject();
		
		query.put(REFERENCE_ID, userId);
		query.put(TYPE, FLAG_TYPE.USER.toString());
		set.put(TARGET, replacement);
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
