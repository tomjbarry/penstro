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
import com.mongodb.DuplicateKeyException;
import com.mongodb.QueryBuilder;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.EscrowDaoCustom;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Escrow;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.constants.IndexNames;
import com.py.py.domain.enumeration.ESCROW_TYPE;
import com.py.py.domain.subdomain.Balance;
import com.py.py.domain.subdomain.EscrowSourceTarget;

public class EscrowDaoImpl implements EscrowDaoCustom {

	private static final String ID = Escrow.ID;
	private static final String SOURCE = ID + "." + EscrowSourceTarget.SOURCE;
	private static final String TARGET = ID + "." + EscrowSourceTarget.TARGET;
	private static final String SOURCE_NAME = Escrow.SOURCE_NAME;
	private static final String SOURCE_UNIQUE_NAME = Escrow.SOURCE_UNIQUE_NAME;
	private static final String TARGET_NAME = Escrow.TARGET_NAME;
	private static final String TARGET_UNIQUE_NAME = Escrow.TARGET_UNIQUE_NAME;
	private static final String CREATED = Escrow.CREATED;
	private static final String TYPE = ID + "." + EscrowSourceTarget.TYPE;
	private static final String BALANCE = Escrow.BALANCE;
	private static final String GOLD = BALANCE + "." + Balance.GOLD;
	private static final String PENDING_TRANSACTIONS = BALANCE + "." + Balance.PENDING_TRANSACTIONS;
	private static final String SOURCE_EXISTS = Escrow.SOURCE_EXISTS;
	private static final String TARGET_EXISTS = Escrow.TARGET_EXISTS;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	private String getEscrowType(ESCROW_TYPE type) {
		if(type != null) {
			return type.toString();
		} else {
			return null;
		}
	}
	
	private String checkTarget(ESCROW_TYPE type, String target) {
		if(type != null && ESCROW_TYPE.EMAIL_OFFER.equals(type)) {
			return target.toLowerCase();
		}
		return target;
	}
	
	private DBObject createEmptyBalance() {
		DBObject obj = new BasicDBObject();
		// assume min gold value, if ever used, is 0 before cleanup
		obj.put(GOLD, new BasicDBObject(DaoQueryStrings.LESS_THAN_EQUAL, 0));
		obj.put(PENDING_TRANSACTIONS, new BasicDBObject(DaoQueryStrings.SIZE, 0));
		return obj;
	}
	
	@Override
	public void initializeEscrow(ESCROW_TYPE type, String source, String sourceName, 
			String target, String targetName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		CheckUtil.nullCheck(source, target, type);
		
		DBObject query = new BasicDBObject();
		query.put(SOURCE, source);
		query.put(TARGET, checkTarget(type, target));
		query.put(TYPE, getEscrowType(type));
		
		DBObject setMap = new BasicDBObject();
		setMap.put(CREATED, new Date());
		if(sourceName != null) {
			setMap.put(SOURCE_NAME, sourceName);
			setMap.put(SOURCE_UNIQUE_NAME, sourceName.toLowerCase());
			setMap.put(SOURCE_EXISTS, true);
		}
		if(targetName != null) {
			setMap.put(TARGET_NAME, targetName);
			setMap.put(TARGET_UNIQUE_NAME, targetName.toLowerCase());
			setMap.put(TARGET_EXISTS, true);
		}
		
		DBObject incrementMap = new BasicDBObject();
		incrementMap.put(GOLD, 0L);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET_ON_INSERT, setMap);
		update.put(DaoQueryStrings.INCREMENT, incrementMap);

		try {
			collection.update(query, update, true, false);
		} catch(DuplicateKeyException dke) {
			try {
				DBObject insert = new BasicDBObject();
				insert.putAll(query);
				insert.putAll(setMap);
				insert.putAll(incrementMap);
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
	/*
	@Override
	public void cleanupAll(ESCROW_TYPE type, String source, String sourceName, 
			String target, String targetName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		CheckUtil.nullCheck(type);
		
		DBObject query = new BasicDBObject();
		query.put(TYPE, getEscrowType(type));

		if(source != null) {
			query.put(SOURCE, source);
		}
		if(sourceName != null) {
			query.put(SOURCE_UNIQUE_NAME, sourceName.toLowerCase());
		}
		if(target != null) {
			query.put(TARGET, checkTarget(type, target));
		}
		if(targetName != null) {
			query.put(TARGET_UNIQUE_NAME, targetName.toLowerCase());
		}
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}*/
	
	@Override
	public void cleanupEmpties(ESCROW_TYPE type, String source, String sourceName, 
			String target, String targetName, Date olderThanModified) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		CheckUtil.nullCheck(type);
		
		DBObject query1 = new BasicDBObject();
		DBObject query2 = new BasicDBObject();

		if(source != null) {
			query1.put(SOURCE, source);
			query2.put(SOURCE, source);
		}
		if(target != null) {
			query1.put(TARGET, checkTarget(type, target));
			query2.put(TARGET, checkTarget(type, target));
		}
		if(sourceName != null) {
			query1.put(SOURCE_UNIQUE_NAME, sourceName.toLowerCase());
			query2.put(SOURCE_UNIQUE_NAME, sourceName.toLowerCase());
		}
		if(targetName != null) {
			query1.put(TARGET_UNIQUE_NAME, targetName.toLowerCase());
			query2.put(TARGET_UNIQUE_NAME, targetName.toLowerCase());
		}

		query1.putAll(createEmptyBalance());
		query1.put(TYPE, getEscrowType(type));
		query2.put(BALANCE, null);
		query2.put(TYPE, getEscrowType(type));
		if(olderThanModified != null) {
			query1.put(CREATED, new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanModified));
			query2.put(CREATED, new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanModified));
		}
		
		QueryBuilder builder = QueryBuilder.start();
		builder.or(query1, query2);
		
		try {
			collection.remove(builder.get());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void cleanupInvalid(ESCROW_TYPE type) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		DBObject query = new BasicDBObject();
		if(type != null) {
			query.put(TYPE, getEscrowType(type));
		}
		query.put(SOURCE_EXISTS, false);
		query.put(TARGET_EXISTS, false);
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<Escrow> findSorted(ESCROW_TYPE type, String source, String sourceName, 
			String target, String targetName, Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		CheckUtil.nullCheck(pageable, type);
		
		DBObject query = new BasicDBObject();
		if(source != null) {
			query.put(SOURCE, source);
		}
		if(target != null) {
			query.put(TARGET, checkTarget(type, target));
		}
		query.put(TYPE, getEscrowType(type));
		if(sourceName != null) {
			query.put(SOURCE_UNIQUE_NAME, sourceName.toLowerCase());
		}
		if(targetName != null) {
			query.put(TARGET_UNIQUE_NAME, targetName.toLowerCase());
		}
		
		DBCursor cursor = null;
		try {
			cursor = collection.find(query)
								.sort(new BasicDBObject(GOLD, DaoValues.SORT_DESCENDING))
								.skip(pageable.getOffset())
								.limit(pageable.getPageSize());
			List<Escrow> escrows = new ArrayList<Escrow>();
			
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Escrow escrow = mongoTemplate.getConverter().read(Escrow.class,  obj);
				escrows.add(escrow);
			}
			
			return new PageImpl<Escrow>(escrows, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<Escrow> findSortedMulti(ESCROW_TYPE type, String source, String sourceName,
			String target, String targetName, ESCROW_TYPE typeAlternative, 
			String sourceAlternative, String sourceNameAlternative, 
			String targetAlternative, String targetNameAlternative, Pageable pageable) 
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		CheckUtil.nullCheck(pageable, type, typeAlternative);
		
		DBObject query1 = new BasicDBObject();
		if(source != null) {
			query1.put(SOURCE, source);
		}
		if(target != null) {
			query1.put(TARGET, checkTarget(type, target));
		}
		query1.put(TYPE, getEscrowType(type));
		if(sourceName != null) {
			query1.put(SOURCE_UNIQUE_NAME, sourceName.toLowerCase());
		}
		if(targetName != null) {
			query1.put(TARGET_UNIQUE_NAME, targetName.toLowerCase());
		}
		
		DBObject query2 = new BasicDBObject();
		if(sourceAlternative != null) {
			query2.put(SOURCE, sourceAlternative);
		}
		if(targetAlternative != null) {
			query2.put(TARGET, checkTarget(typeAlternative, targetAlternative));
		}
		query2.put(TYPE, getEscrowType(typeAlternative));
		if(sourceNameAlternative != null) {
			query2.put(SOURCE_UNIQUE_NAME, sourceNameAlternative.toLowerCase());
		}
		if(targetNameAlternative != null) {
			query2.put(TARGET_UNIQUE_NAME, targetNameAlternative.toLowerCase());
		}
		
		QueryBuilder builder = QueryBuilder.start();
		builder.or(query1, query2);
		DBCursor cursor = null;
		try {
			cursor = collection.find(builder.get())
								.sort(new BasicDBObject(GOLD, DaoValues.SORT_DESCENDING))
								.skip(pageable.getOffset())
								.limit(pageable.getPageSize());
			List<Escrow> escrows = new ArrayList<Escrow>();
			
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Escrow escrow = mongoTemplate.getConverter().read(Escrow.class,  obj);
				escrows.add(escrow);
			}
			
			return new PageImpl<Escrow>(escrows, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	/*
	@Override
	public void updateType(ESCROW_TYPE queryType, String source, String target, 
			ESCROW_TYPE updateType, String updateSource, String updateTarget) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		DBObject query = new BasicDBObject();
		query.put(TYPE, getEscrowType(queryType));

		if(source != null) {
			query.put(SOURCE, source);
		}
		if(target != null) {
			query.put(TARGET, target);
		}
		
		DBObject update = new BasicDBObject();
		update.put(TYPE, getEscrowType(updateType));
		if(updateSource != null) {
			update.put(SOURCE, updateSource);
		}
		if(updateTarget != null) {
			update.put(TARGET, updateTarget);
		}
		
		try {
			collection.update(query, 
					update, false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
		
	}
	*/
	
	@Override
	public Escrow findEscrow(ESCROW_TYPE type, String source, String sourceName, 
			String target, String targetName) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		CheckUtil.nullCheck(type);
		
		DBObject query = new BasicDBObject();
		if(source != null) {
			query.put(SOURCE, source);
		}
		if(target != null) {
			if(ESCROW_TYPE.EMAIL_OFFER.equals(type)) {
				target = target.toLowerCase();
			}
			query.put(TARGET, checkTarget(type, target));
		}
		query.put(TYPE, getEscrowType(type));
		if(sourceName != null) {
			query.put(SOURCE_UNIQUE_NAME, sourceName.toLowerCase());
		}
		if(targetName != null) {
			query.put(TARGET_UNIQUE_NAME, targetName.toLowerCase());
		}
		
		try {
			DBObject obj = collection.findOne(query);
			
			if(obj == null) {
				return null;
			}
			
			Escrow escrow = mongoTemplate.getConverter().read(Escrow.class, obj);
			
			return escrow;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void rename(ObjectId userId, String replacement, boolean asSource) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		CheckUtil.nullCheck(userId, replacement);
		
		DBObject query = new BasicDBObject();
		DBObject set = new BasicDBObject();
		
		if(asSource) {
			query.put(SOURCE, userId.toHexString());
			set.put(SOURCE_NAME, replacement);
			set.put(SOURCE_UNIQUE_NAME, replacement.toLowerCase());
		} else {
			query.put(TARGET, userId.toHexString());
			set.put(TARGET_NAME, replacement);
			set.put(TARGET_UNIQUE_NAME, replacement.toLowerCase());
		}
	
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(DuplicateKeyException dke) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void markExists(ObjectId userId, boolean exists, boolean asSource) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		//DBObject query = new BasicDBObject();
		DBObject query = new BasicDBObject();
		DBObject set = new BasicDBObject();
		
		if(asSource) {
			query.put(SOURCE, userId.toHexString());
			set.put(SOURCE_EXISTS, exists);
		} else {
			query.put(TARGET, userId.toHexString());
			set.put(TARGET_EXISTS, exists);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<Escrow> findOffersBeforeCreated(Date olderThanCreated, Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.ESCROW);
		
		CheckUtil.nullCheck(olderThanCreated, pageable);
		
		//DBObject query = new BasicDBObject();
		DBObject query1 = new BasicDBObject(TYPE, ESCROW_TYPE.OFFER.toString());
		DBObject query2 = new BasicDBObject(TYPE, ESCROW_TYPE.EMAIL_OFFER.toString());
		
		QueryBuilder builder = QueryBuilder.start();
		builder.or(query1, query2);
		
		try {
			DBCursor cursor = collection.find(builder.get())
					.hint(IndexNames.ESCROW_TYPE_CREATED)
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());
			
			List<Escrow> escrows = new ArrayList<Escrow>();
			
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Escrow escrow = mongoTemplate.getConverter().read(Escrow.class,  obj);
				escrows.add(escrow);
			}
			
			return new PageImpl<Escrow>(escrows, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
