package com.py.py.dao.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.custom.ReferenceDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.constants.SharedFields;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.util.PyUtils;

public class ReferenceDaoImpl implements ReferenceDaoCustom {

	protected static String ID = SharedFields.ID;
	protected static String PENDING_TRANSACTIONS = SharedFields.TALLY + "." + SharedFields.PENDING_TRANSACTIONS;
	protected static String APPRECIATION = SharedFields.TALLY + "." + SharedFields.APPRECIATION;
	protected static String PROMOTION = SharedFields.TALLY + "." + SharedFields.PROMOTION;
	protected static String COST = SharedFields.TALLY + "." + SharedFields.COST;
	protected static String VALUE = SharedFields.TALLY + "." + SharedFields.VALUE;
	protected static String PAID = SharedFields.PAID;
	protected static String INITIALIZED = SharedFields.INITIALIZED;
	protected static String AGGREGATE = SharedFields.AGGREGATE;
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	// used when updating value so all are counted immediately
	private DBObject getValueObject(long amount, String collectionName) {
		DBObject map = new BasicDBObject();
		for(TIME_OPTION time : TIME_OPTION.values()) {
			if(!TIME_OPTION.ALLTIME.equals(time)) {
				map.put(AGGREGATE + "." + PyUtils.convertTimeOption(time), amount);
			}
		}
		map.put(VALUE, amount);
		return map;
	}
	
	@Override
	public void chargeTally(ObjectId id, ObjectId tid, long amount, 
			String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, tid, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(PENDING_TRANSACTIONS, 
				new BasicDBObject(DaoQueryStrings.NOT_EQUAL, tid));
		
		DBObject increment = getValueObject(amount, collectionName);
		increment.put(COST, amount);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, increment);
		update.put(DaoQueryStrings.SET, 
				new BasicDBObject(PAID, true));
		update.put(DaoQueryStrings.PUSH,
				new BasicDBObject(PENDING_TRANSACTIONS, tid));
			
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void addTally(ObjectId id, ObjectId tid, Long appreciation, Long promotion, 
			String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, tid, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(INITIALIZED, true);
		query.put(PENDING_TRANSACTIONS, 
				new BasicDBObject(DaoQueryStrings.NOT_EQUAL, tid));
		
		DBObject increment = new BasicDBObject();
		if(promotion != null) {
			increment.put(PROMOTION, promotion);
			increment.putAll(getValueObject(promotion, collectionName));
		}
		if(appreciation != null) {
			increment.put(APPRECIATION, appreciation);
		}
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, increment);
		update.put(DaoQueryStrings.PUSH,
				new BasicDBObject(PENDING_TRANSACTIONS, tid));
		update.put(DaoQueryStrings.SET, new BasicDBObject(PAID, true));
			
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	// could use multiple evicts and conditional for good practice, but using shortcut
	// here for readability, simplicity, and evaluation speed
	/*
	@Override
	@CacheEvict(value = {CacheNames.POSTING, CacheNames.COMMENT}, key = "#p0")
	*/
	@Override
	public void completeTally(ObjectId id, ObjectId tid, 
			String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, tid, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(PENDING_TRANSACTIONS, tid);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, new BasicDBObject(INITIALIZED, true));
		update.put(DaoQueryStrings.PULL,
				new BasicDBObject(PENDING_TRANSACTIONS, tid));
			
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public boolean verifyTallyCostCompleted(ObjectId id, ObjectId tid, long amount, 
			String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, tid, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(PENDING_TRANSACTIONS, new BasicDBObject(DaoQueryStrings.NOT_EQUAL, tid));
		query.put(COST, amount);
		query.put(PAID, true);
		query.put(INITIALIZED, true);
		
		try {
			DBObject obj = collection.findOne(query);
			if(obj == null) {
				return false;
			}
			return true;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public boolean verifyTallyAdded(ObjectId id, ObjectId tid, 
			String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, tid, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(PENDING_TRANSACTIONS, tid);
		
		try {
			DBObject obj = collection.findOne(query);
			if(obj == null) {
				return false;
			}
			return true;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public boolean verifyTallyCompleted(ObjectId id, ObjectId tid, 
			String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, tid, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(PENDING_TRANSACTIONS, 
				new BasicDBObject(DaoQueryStrings.NOT_EQUAL, tid));
		
		try {
			DBObject obj = collection.findOne(query);
			
			if(obj == null) {
				return false;
			}
			
			return true;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	// could use multiple evicts and conditional for good practice, but using shortcut
	// here for readability, simplicity, and evaluation speed
	/*
	@Override
	@CacheEvict(value = {CacheNames.POSTING, CacheNames.COMMENT}, key = "#p0")
	*/
	@Override
	public void revertPendingTally(ObjectId id, ObjectId tid, Long appreciation, 
			Long promotion, String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, tid, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(PENDING_TRANSACTIONS, tid);
		
		DBObject increment = new BasicDBObject();
		if(promotion != null) {
			increment.put(PROMOTION, promotion);
			increment.putAll(getValueObject(promotion, collectionName));
		}
		if(appreciation != null) {
		increment.put(APPRECIATION, appreciation);
		}
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, increment);
		update.put(DaoQueryStrings.PULL,
				new BasicDBObject(PENDING_TRANSACTIONS, tid));
			
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	// could use multiple evicts and conditional for good practice, but using shortcut
	// here for readability, simplicity, and evaluation speed
	/*
	@Override
	@CacheEvict(value = {CacheNames.POSTING, CacheNames.COMMENT}, key = "#p0")
	*/
	@Override
	public void revertPendingCost(ObjectId id, ObjectId tid, String collectionName) 
			throws DaoException {
		CheckUtil.nullCheck(id, tid, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(PENDING_TRANSACTIONS, tid);
		
		DBObject update = new BasicDBObject();
		DBObject set = new BasicDBObject();
		set.put(PAID, false);
		set.put(INITIALIZED, false);
		update.put(DaoQueryStrings.SET, set);
		update.put(DaoQueryStrings.PULL,
				new BasicDBObject(PENDING_TRANSACTIONS, tid));
			
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	// could use multiple evicts and conditional for good practice, but using shortcut
	// here for readability, simplicity, and evaluation speed
	/*
	@Override
	@CacheEvict(value = {CacheNames.POSTING, CacheNames.COMMENT}, key = "#p0")
	*/
	@Override
	public void adminIncrement(ObjectId id, Long cost, Long appreciation, Long promotion, 
			String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, collectionName);
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		if(cost == null && appreciation == null && promotion == null) {
			return;
		}
		
		DBObject increment = new BasicDBObject();
		long value = 0L;
		if(cost != null) {
			increment.put(COST, cost);
			value += cost;
		}
		if(appreciation != null) {
			increment.put(APPRECIATION, appreciation);
		}
		if(promotion != null) {
			increment.put(PROMOTION, promotion);
			value += promotion;
		}
		if(value != 0L) {
			increment.put(VALUE, value);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.INCREMENT, increment));
		} catch(Exception e) {
			throw new DaoException(e);
		}
		
	}
}
