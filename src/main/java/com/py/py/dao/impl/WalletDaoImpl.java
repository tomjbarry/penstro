package com.py.py.dao.impl;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.custom.WalletDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.constants.SharedFields;
import com.py.py.domain.enumeration.ESCROW_TYPE;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.util.PyUtils;

public class WalletDaoImpl implements WalletDaoCustom {
	
	private static final String ID = SharedFields.ID;
	private static final String SOURCE = ID + "." + SharedFields.SOURCE;
	private static final String TARGET = ID + "." + SharedFields.TARGET;
	private static final String TYPE = ID + "." + SharedFields.TYPE;
	private static final String BALANCE = SharedFields.BALANCE;
	private static final String PENDING_TRANSACTIONS = BALANCE + "." + SharedFields.PENDING_TRANSACTIONS;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	private boolean useSourceTarget(String collectionName) {
		if(PyUtils.stringCompare(CollectionNames.ESCROW, collectionName)) {
			return true;
		}
		return false;
	}
	
	private DBObject createIdObject(String id, String collectionName) throws DaoException {
		CheckUtil.nullCheck(id, collectionName);
		DBObject query = new BasicDBObject();
		if(useSourceTarget(collectionName)) {
			EscrowSourceTarget st = new EscrowSourceTarget(null, null, ESCROW_TYPE.OFFER);
			st.setId(id);
			query.put(SOURCE, st.getSource());
			query.put(TARGET, st.getTarget());
			if(st.getType() != null) {
				query.put(TYPE, st.getType().toString());
			}
		} else {
			query.put(ID, new ObjectId(id));
		}
		return query;
	}
	
	@Override
	public void startTransaction(String id, ObjectId tid, String currency, 
			long amount, String collectionName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		CheckUtil.nullCheck(id, tid, currency, collectionName);
		
		DBObject query = createIdObject(id, collectionName);
		if(amount < 0) {
			query.put(BALANCE + "." + currency, 
					new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, 0 - amount));
		}
		// no checking of pending transactions in case starting same transaction on self,
		// for case of currency conversion
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, 
				new BasicDBObject(BALANCE + "." + currency, amount));
		update.put(DaoQueryStrings.PUSH,
				new BasicDBObject(PENDING_TRANSACTIONS, tid));
			
		try {
			collection.update(query, 
				update, 
				false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "new org.bson.types.ObjectId(#p0)", 
		condition = "T(com.py.py.domain.constants.CollectionNames).USER_INFO.equals(#collectionName)")
	*/
	@Override
	public void completeTransaction(String id, ObjectId tid, String collectionName) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		CheckUtil.nullCheck(id, tid, collectionName);
		
		DBObject query = createIdObject(id, collectionName);
		// no checking of current transaction
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PULL,
				new BasicDBObject(PENDING_TRANSACTIONS, tid));
			
		try {
			collection.update(query, 
				update, 
				false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public boolean verifyHasFunds(String id, String currency, 
			long amount, String collectionName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		CheckUtil.nullCheck(id, currency, collectionName);
		
		DBObject query = createIdObject(id, collectionName);
		query.put(BALANCE + "." + currency, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, amount));
		
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
	public boolean verifyTransactionStarted(String id, ObjectId tid, String collectionName) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		CheckUtil.nullCheck(id, tid, collectionName);
		
		DBObject query = createIdObject(id, collectionName);
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
	public boolean verifyTransactionCompleted(String id, ObjectId tid, 
			String collectionName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		CheckUtil.nullCheck(id, tid, collectionName);
		
		DBObject query = createIdObject(id, collectionName);
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

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "new org.bson.types.ObjectId(#p0)", 
		condition = "T(com.py.py.domain.constants.CollectionNames).USER_INFO.equals(#collectionName)")
	*/
	@Override
	public void revertPendingTransaction(String id, ObjectId tid, 
			String currency, long amount, String collectionName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(collectionName);
		
		CheckUtil.nullCheck(id, tid, currency, collectionName);
		
		DBObject query = createIdObject(id, collectionName);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, 
				new BasicDBObject(BALANCE + "." + currency, amount));
		update.put(DaoQueryStrings.PULL,
				new BasicDBObject(PENDING_TRANSACTIONS, tid));
			
		try {
			collection.update(query, update, false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
