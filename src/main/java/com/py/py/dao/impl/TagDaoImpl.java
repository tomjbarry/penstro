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
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.TagDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.dao.util.DBObjectConverter;
import com.py.py.domain.Tag;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.constants.IndexNames;
import com.py.py.domain.subdomain.TagId;
import com.py.py.domain.subdomain.TallyApproximation;
import com.py.py.domain.subdomain.TimeSumAggregate;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.util.PyUtils;

public class TagDaoImpl implements TagDaoCustom {

	public static final String ID = Tag.ID;
	public static final String VALUE = Tag.VALUE;
	public static final String APPRECIATION = Tag.APPRECIATION;
	
	public static final String NAME = TagId.NAME;
	public static final String LANGUAGE = TagId.LANGUAGE;
	
	public static final String TAG_ID_NAME = ID + "." + TagId.NAME;
	public static final String TAG_ID_LANGUAGE = ID + "." + TagId.LANGUAGE;
	
	public static final String COMMENT_COUNT = Tag.COMMENT_COUNT;
	public static final String COMMENT_TALLY = Tag.COMMENT_TALLY;
	public static final String COMMENT_TALLY_VALUE = COMMENT_TALLY + "." + TallyApproximation.VALUE;
	public static final String COMMENT_TALLY_APPRECIATION = COMMENT_TALLY + "." + TallyApproximation.APPRECIATION;
	public static final String COMMENT_TALLY_PROMOTION = COMMENT_TALLY + "." + TallyApproximation.PROMOTION;
	public static final String COMMENT_TALLY_COST = COMMENT_TALLY + "." + TallyApproximation.COST;
	
	public static final String AGGREGATE = Tag.AGGREGATE;
	public static final String AGGREGATE_HOUR = AGGREGATE + "." + TimeSumAggregate.HOUR;
	public static final String AGGREGATE_DAY = AGGREGATE + "." + TimeSumAggregate.DAY;
	public static final String AGGREGATE_MONTH = AGGREGATE + "." + TimeSumAggregate.MONTH;
	public static final String AGGREGATE_YEAR = AGGREGATE + "." + TimeSumAggregate.YEAR;
	public static final String LAST_PROMOTION = Tag.LAST_PROMOTION;
	
	public static final String LOCKED = Tag.LOCKED;
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	protected DBObject createQuery(String name, String language) {
		DBObject query = new BasicDBObject();
		if(name != null) {
			query.put(TAG_ID_NAME, name);
		}
		if(language != null) {
			query.put(TAG_ID_LANGUAGE, language);
		}
		return query;
	}
	
	protected DBObject createQuery(TagId id) {
		if(id != null) {
			return createQuery(id.getName(), id.getLanguage());
		}
		return new BasicDBObject();
	}
	
	protected String getHint(TIME_OPTION time) {
		if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
			return IndexNames.TAG_VALUE_TIME_LANGUAGE;
		} else if(TIME_OPTION.YEAR.equals(time)) {
			return IndexNames.TAG_AGGREGATE_YEAR_TIME_LANGUAGE;
		} else if(TIME_OPTION.MONTH.equals(time)) {
			return IndexNames.TAG_AGGREGATE_MONTH_TIME_LANGUAGE;
		} else if(TIME_OPTION.DAY.equals(time)) {
			return IndexNames.TAG_AGGREGATE_DAY_TIME_LANGUAGE;
		} else if(TIME_OPTION.HOUR.equals(time)) {
			return IndexNames.TAG_AGGREGATE_HOUR_TIME_LANGUAGE;
		}
		return IndexNames.TAG_VALUE_TIME_LANGUAGE;
	}
	
	protected DBObject createSetOnInsert(String name, String language) {
		DBObject map = new BasicDBObject();
		map.put(TAG_ID_NAME, name);
		map.put(TAG_ID_LANGUAGE, language);
		return map;
	}
	
	protected DBObject createId(String name, String language) {
		DBObject map = new BasicDBObject();
		map.put(NAME, name);
		map.put(LANGUAGE, language);
		return map;
	}
	
	private String getSortString(TIME_OPTION time) {
		if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
			return VALUE;
		} else {
			return AGGREGATE + "." + PyUtils.convertTimeOption(time);
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.TAG, key = "#p0" +"+':'+"+ "#p1")
	public Tag findCachedTag(String name, String language) throws DaoException {
		return findTag(name, language);
	}
	
	@Override
	public Tag findTag(String name, String language) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		CheckUtil.nullCheck(name, language);
		
		try {
			DBObject obj = collection.findOne(createQuery(name, language));
			Tag tag = mongoTemplate.getConverter().read(Tag.class, obj);
			return tag;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.TAG_PAGED, key = "#p0" 
			+"+':'+"+ "#p1?.getPageNumber()"
			+"+':'+"+ "#p1?.getPageSize()" 
			+"+':'+"+ "#p2?.toString()")
	public Page<Tag> findSorted(String language, Pageable pageable, TIME_OPTION time) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		
		CheckUtil.nullCheck(pageable);
		
		DBCursor cursor = null;
		
		String sort = getSortString(time);
		DBObject query = new BasicDBObject();
		if(time != null && time != TIME_OPTION.ALLTIME) {
			query.put(LAST_PROMOTION, 
					new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, 
							PyUtils.getTime(time)));
		}
		if(language != null) {
			query.put(TAG_ID_LANGUAGE, language);
		}
		
		try {
			DBObject sortObject = new BasicDBObject(sort, DaoValues.SORT_DESCENDING);
			sortObject.put(LAST_PROMOTION, DaoValues.SORT_DESCENDING);
			cursor = collection.find(query)
					.sort(sortObject)
					.hint(getHint(time))
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());
			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Tag> tags = new ArrayList<Tag>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Tag tag = mongoTemplate.getConverter().read(Tag.class,  obj);
				tags.add(tag);
			}
			
			return new PageImpl<Tag>(tags, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.TAG, key = "#p0" +"+':'+"+ "#p1")
	*/
	@Override
	public void incrementTag(String name, String language, Long amount, Long appreciation) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		
		CheckUtil.nullCheck(name, language);
		
		if(amount == null) {
			amount = 0L;
		}
		if(appreciation == null) {
			appreciation = 0L;
		}
		Date now = new Date();
		
		DBObject query = createQuery(name, language);
		query.put(LOCKED, false);
		
		DBObject set = new BasicDBObject();
		set.put(LAST_PROMOTION, now);
		
		DBObject increment = new BasicDBObject();
		increment.put(AGGREGATE_HOUR, amount);
		increment.put(AGGREGATE_DAY, amount);
		increment.put(AGGREGATE_MONTH, amount);
		increment.put(AGGREGATE_YEAR, amount);
		increment.put(VALUE, amount);
		increment.put(APPRECIATION, appreciation);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, increment);
		update.put(DaoQueryStrings.SET, set);
		
		DBObject updateObject = new BasicDBObject();
		updateObject.putAll(update);
		updateObject.put(DaoQueryStrings.SET_ON_INSERT, createSetOnInsert(name, language));
		
		try {
			collection.update(query, updateObject, true, false);
		} catch(DuplicateKeyException dke) {
			try {
				DBObject insert = new BasicDBObject();
				insert.put(ID, createId(name, language));
				insert.putAll(set);
				insert.put(VALUE, amount);
				insert.put(APPRECIATION, appreciation);
				insert.put(AGGREGATE, DBObjectConverter.convertTimeSumAggregate(amount));
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
	@CacheEvict(value = CacheNames.TAG, key = "#p0?.getName()" +"+':'+"+ "#p0?.getLanguage()")
	*/
	@Override
	public void incrementCommentCount(TagId id, long count) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		CheckUtil.nullCheck(id);
		
		try {
			collection.update(createQuery(id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT,
							new BasicDBObject(COMMENT_COUNT, count)), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.TAG, key = "#p0?.getName()" +"+':'+"+ "#p0?.getLanguage()")
	*/
	@Override
	public void incrementCommentTallyAppreciationPromotion(TagId id, 
			Long appreciation, Long promotion) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		CheckUtil.nullCheck(id);
		
		DBObject update = new BasicDBObject();
		if(appreciation != null) {
			update.put(COMMENT_TALLY_APPRECIATION, appreciation);
		}
		if(promotion != null) {
			update.put(COMMENT_TALLY_PROMOTION, promotion);
			update.put(COMMENT_TALLY_VALUE, promotion);
		}
		
		try {
			collection.update(createQuery(id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, update), 
					false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.TAG, key = "#p0?.getName()" +"+':'+"+ "#p0?.getLanguage()")
	*/
	@Override
	public void incrementCommentTallyCost(TagId id, Long cost) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		CheckUtil.nullCheck(id, cost);
		
		DBObject update = new BasicDBObject();
		update.put(COMMENT_TALLY_COST, cost);
		update.put(COMMENT_TALLY_VALUE, cost);
		
		try {
			collection.update(createQuery(id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, update), 
					false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void updateAggregation(TagId id, long value, TIME_OPTION segment) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		
		CheckUtil.nullCheck(id, segment);
		
		DBObject updateObject = new BasicDBObject();
		updateObject.put(AGGREGATE + "." + PyUtils.convertTimeOption(segment), value);
		
		try {
			collection.update(createQuery(id), 
					new BasicDBObject(DaoQueryStrings.SET, updateObject));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void emptyAggregations(TIME_OPTION segment) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		
		CheckUtil.nullCheck(segment);
		
		Date then = PyUtils.getTime(segment);
		String field = AGGREGATE + "." + PyUtils.convertTimeOption(segment);
		DBObject query = new BasicDBObject();
		query.put(LAST_PROMOTION, 
				new BasicDBObject(DaoQueryStrings.LESS_THAN, then));
		query.put(field, new BasicDBObject(DaoQueryStrings.NOT_EQUAL, 0L));
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, new BasicDBObject(field, 0L));
		
		try {
			collection.update(query, update, false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.TAG, key = "#p0" +"+':'+"+ "#p1")
	*/
	@Override
	public void updateTag(String name, String language, boolean locked) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.TAG);
		CheckUtil.nullCheck(name, language);
		
		Date now = new Date();
		
		DBObject set = new BasicDBObject();
		set.put(LAST_PROMOTION, now);
		set.put(LOCKED, locked);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, set);
		
		DBObject updateObject = update;
		updateObject.put(DaoQueryStrings.SET_ON_INSERT, createSetOnInsert(name, language));
		try {
			collection.update(createQuery(name, language), updateObject, true, false);
		} catch(DuplicateKeyException dke) {
			try {
				DBObject insert = new BasicDBObject();
				insert.put(ID, createId(name, language));
				insert.putAll(set);
				collection.insert(insert);
			} catch(DuplicateKeyException d) {
				collection.update(createQuery(name, language), update, false, false);
			} catch(Exception e) {
				throw new DaoException(e);
			}
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
}
