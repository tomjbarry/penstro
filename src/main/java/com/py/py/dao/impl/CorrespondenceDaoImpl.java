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

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.CorrespondenceDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.dao.util.DBObjectConverter;
import com.py.py.domain.Correspondence;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.subdomain.BinaryUserId;
import com.py.py.domain.subdomain.CachedUsername;

public class CorrespondenceDaoImpl implements CorrespondenceDaoCustom {

	private static final String ID = Correspondence.ID;
	private static final String CREATED = Correspondence.CREATED;
	private static final String LAST_MODIFIED = Correspondence.LAST_MODIFIED;
	private static final String FIRST = Correspondence.FIRST;
	private static final String FIRST_ID = FIRST + "." + CachedUsername.OID;
	private static final String FIRST_USERNAME = FIRST + "." + CachedUsername.USERNAME;
	private static final String FIRST_EXISTS = FIRST + "." + CachedUsername.EXISTS;
	private static final String FIRST_HIDDEN = Correspondence.FIRST_HIDDEN;
	private static final String SECOND = Correspondence.SECOND;
	private static final String SECOND_ID = SECOND + "." + CachedUsername.OID;
	private static final String SECOND_USERNAME = SECOND + "." + CachedUsername.USERNAME;
	private static final String SECOND_EXISTS = SECOND + "." + CachedUsername.EXISTS;
	private static final String SECOND_HIDDEN = Correspondence.SECOND_HIDDEN;
	private static final String AUTHOR = Correspondence.AUTHOR;
	private static final String TARGET = Correspondence.TARGET;
	private static final String MESSAGE = Correspondence.MESSAGE;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Override
	public Page<Correspondence> getCorrespondences(ObjectId a, Pageable pageable)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.CORRESPONDENCE);
		
		CheckUtil.nullCheck(a, pageable);
		
		DBCursor cursor = null;
		
		DBObject query = new BasicDBObject();
		query.put(FIRST_ID, a);
		query.put(FIRST_HIDDEN, new BasicDBObject(DaoQueryStrings.NOT_EQUAL, true));
		
		DBObject queryInverse = new BasicDBObject();
		queryInverse.put(SECOND_ID, a);
		queryInverse.put(SECOND_HIDDEN, new BasicDBObject(DaoQueryStrings.NOT_EQUAL, true));
		
		BasicDBList queryList = new BasicDBList();
		queryList.add(query);
		queryList.add(queryInverse);
		
		// no hint for indexing, as it is an or and needs to use two indexes
		try {
			cursor = collection.find(new BasicDBObject(DaoQueryStrings.OR, queryList))
				.sort(new BasicDBObject(LAST_MODIFIED, DaoValues.SORT_DESCENDING))
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize());
		} catch(Exception e) {
			throw new DaoException(e);
		}
		if(cursor == null) {
			throw new DaoException();
		}
		
		List<Correspondence> correspondences = new ArrayList<Correspondence>();
		
		while(cursor.hasNext()) {
			DBObject obj = cursor.next();
			Correspondence c = mongoTemplate.getConverter().read(Correspondence.class, obj);
			correspondences.add(c);
		}
		
		return new PageImpl<Correspondence>(correspondences, pageable, cursor.count());
	}
	
	@Override
	public void update(CachedUsername author, CachedUsername target, String lastMessagePreview) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.CORRESPONDENCE);
		
		CheckUtil.nullCheck(author, target);
		Date now = new Date();
		
		ObjectId aId = author.getId();
		ObjectId tId = target.getId();
		
		CheckUtil.nullCheck(aId, tId);
		
		BinaryUserId buId = new BinaryUserId(aId, tId);
		
		// assume a is first, reverse if not true
		CachedUsername first = author;
		CachedUsername second = target;
		if(!aId.equals(buId.getFirst())) {
			first = target;
			second = author;
		}
		
		DBObject query = new BasicDBObject();
		query.put(ID, DBObjectConverter.convertBinaryUserId(buId));
		query.put(FIRST, DBObjectConverter.convertCachedUsername(first));
		query.put(SECOND, DBObjectConverter.convertCachedUsername(second));
		
		DBObject set = new BasicDBObject();
		set.put(LAST_MODIFIED, now);
		set.put(AUTHOR, aId);
		set.put(TARGET, tId);
		set.put(MESSAGE, lastMessagePreview);
		
		DBObject setOnInsert = new BasicDBObject();
		setOnInsert.put(CREATED, now);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, set);
		update.put(DaoQueryStrings.SET_ON_INSERT, setOnInsert);

		try {
			collection.update(query, update, true, false);
		} catch(DuplicateKeyException dke) {
			try {
				DBObject insert = new BasicDBObject();
				insert.putAll(query);
				insert.putAll(set);
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
	public void updateStatus(ObjectId uId, ObjectId otherId, boolean hidden) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.CORRESPONDENCE);
		
		CheckUtil.nullCheck(uId, otherId);
		
		BinaryUserId buId = new BinaryUserId(uId, otherId);
		
		DBObject set = new BasicDBObject();
		if(uId.equals(buId.getFirst())) {
			set.put(FIRST_HIDDEN, hidden);
		} else {
			set.put(SECOND_HIDDEN, hidden);
		}
		
		DBObject query = new BasicDBObject();
		query.put(ID, DBObjectConverter.convertBinaryUserId(buId));
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, set);

		try {
			collection.update(query, update, false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void rename(ObjectId userId, String replacement, boolean asFirst) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.CORRESPONDENCE);
		
		CheckUtil.nullCheck(userId, replacement);
		DBObject query = new BasicDBObject();
		
		DBObject set = new BasicDBObject();
		
		if(asFirst) {
			query.put(FIRST_ID, userId);
			set.put(FIRST_USERNAME, replacement);
		} else {
			query.put(SECOND_ID, userId);
			set.put(SECOND_USERNAME, replacement);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void removeUser(ObjectId userId, boolean asFirst) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.CORRESPONDENCE);
		
		CheckUtil.nullCheck(userId);
		DBObject query = new BasicDBObject();
		
		DBObject set = new BasicDBObject();
		
		if(asFirst) {
			query.put(FIRST_ID, userId);
			set.put(FIRST_EXISTS, false);
		} else {
			query.put(SECOND_ID, userId);
			set.put(SECOND_EXISTS, false);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void removeExpired(Date olderThanModified) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.CORRESPONDENCE);
		
		CheckUtil.nullCheck(olderThanModified);
		DBObject query = new BasicDBObject(LAST_MODIFIED, new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanModified));
		query.put(FIRST_HIDDEN, false);
		query.put(SECOND_HIDDEN, false);
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

}
