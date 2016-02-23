package com.py.py.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.py.py.dao.constants.CacheNames;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.RestrictedDaoCustom;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Restricted;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.subdomain.RestrictedWord;
import com.py.py.enumeration.RESTRICTED_TYPE;

public class RestrictedDaoImpl implements RestrictedDaoCustom {

	private static final String ID = Restricted.ID;
	private static final String ID_WORD = ID + "." + RestrictedWord.WORD;
	private static final String ID_TYPE = ID + "." + RestrictedWord.TYPE;
	private static final String CREATED = Restricted.CREATED;
	
	protected String getTypeString(RESTRICTED_TYPE type) {
		if(type == null) {
			return null;
		}
		return type.toString();
	}
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	/*
	@Override
	@CacheEvict(value = CacheNames.RESTRICTED, key = "#p0" +"+':'+"+ "#p1?.toString()")
	*/
	@Override
	public Restricted add(String word, RESTRICTED_TYPE type) throws DaoException {
		CheckUtil.nullCheck(word, type);
		String correctWord = word.toLowerCase();
		
		Restricted restricted = new Restricted();
		RestrictedWord id = new RestrictedWord();
		id.setWord(correctWord);
		id.setType(type);
		restricted.setId(id);
		restricted.setCreated(new Date());
		
		try {
			mongoTemplate.insert(restricted, CollectionNames.RESTRICTED);
		} catch(DuplicateKeyException dk) {
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
		return restricted;
	}
	/*
	@Override
	@CacheEvict(value = CacheNames.RESTRICTED, key = "#p0" +"+':'+"+ "#p1?.toString()")
	*/
	@Override
	public void remove(String word, RESTRICTED_TYPE type) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.RESTRICTED);

		CheckUtil.nullCheck(word, type);
		String correctWord = word.toLowerCase();
		
		DBObject query = new BasicDBObject();
		query.put(ID_WORD, correctWord);
		query.put(ID_TYPE, getTypeString(type));
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.RESTRICTED, key = "#p0" +"+':'+"+ "#p1?.toString()")
	public Restricted getRestricted(String word, RESTRICTED_TYPE type) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.RESTRICTED);
		
		CheckUtil.nullCheck(word, type);
		String correctWord = word.toLowerCase();
		
		DBObject query = new BasicDBObject();
		query.put(ID_WORD, correctWord);
		query.put(ID_TYPE, getTypeString(type));
		
		try {
			DBObject obj = collection.findOne(query);
			if(obj == null) {
				return null;
			}
			return mongoTemplate.getConverter().read(Restricted.class, obj);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<Restricted> findRestricteds(RESTRICTED_TYPE type, Pageable pageable) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.RESTRICTED);
		CheckUtil.nullCheck(pageable);
		
		DBObject query = new BasicDBObject();
		if(type != null) {
			query.put(ID_TYPE, getTypeString(type));
		}
		
		try {
			DBCursor cursor = collection.find(query)
					.sort(new BasicDBObject(CREATED, DaoValues.SORT_DESCENDING))
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());
			
			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Restricted> restricteds = new ArrayList<Restricted>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Restricted r = mongoTemplate.getConverter().read(Restricted.class,  obj);
				restricteds.add(r);
			}
			
			return new PageImpl<Restricted>(restricteds, pageable, cursor.count());
			
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
