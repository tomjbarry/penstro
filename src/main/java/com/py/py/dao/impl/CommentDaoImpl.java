package com.py.py.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
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
import com.py.py.constants.ServiceValues;
import com.py.py.dao.constants.CacheNames;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.CommentDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Comment;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.constants.IndexNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.Tally;
import com.py.py.domain.subdomain.TallyApproximation;
import com.py.py.domain.subdomain.TimeSumAggregate;
import com.py.py.enumeration.SORT_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.util.PyUtils;

public class CommentDaoImpl implements CommentDaoCustom {

	public static final String ID = Comment.ID;
	public static final String PAID = Comment.PAID;
	public static final String INITIALIZED = Comment.INITIALIZED;
	public static final String CONTENT = Comment.CONTENT;
	public static final String ENABLED = Comment.ENABLED;
	public static final String REMOVED = Comment.REMOVED;
	public static final String AUTHOR = Comment.AUTHOR;
	public static final String AUTHOR_ID = AUTHOR + "." + CachedUsername.OID;
	public static final String AUTHOR_NAME = AUTHOR + "." + CachedUsername.USERNAME;
	public static final String AUTHOR_EXISTS = AUTHOR + "." + CachedUsername.EXISTS;
	public static final String BENEFICIARY = Comment.BENEFICIARY;
	public static final String BENEFICIARY_ID = BENEFICIARY + "." + CachedUsername.OID;
	public static final String BENEFICIARY_NAME = BENEFICIARY + "." + CachedUsername.USERNAME;
	public static final String BENEFICIARY_EXISTS = BENEFICIARY + "." + CachedUsername.EXISTS;
	public static final String CREATED = Comment.CREATED;
	public static final String BASE_ID = Comment.BASE_ID;
	public static final String BASE_STRING = Comment.BASE_STRING;
	public static final String TYPE = Comment.TYPE;
	public static final String PARENT = Comment.PARENT;
	public static final String VALUE = Comment.TALLY + "." + Tally.VALUE;
	public static final String PROMOTION = Comment.TALLY + "." + Tally.PROMOTION;
	public static final String COST = Comment.TALLY + "." + Tally.COST;
	public static final String REPLY_COUNT = Comment.REPLY_COUNT;
	public static final String APPRECIATION_COUNT = Comment.APPRECIATION_COUNT;
	public static final String PROMOTION_COUNT = Comment.PROMOTION_COUNT;
	public static final String REPLY_TALLY = Comment.REPLY_TALLY;
	public static final String REPLY_TALLY_VALUE = REPLY_TALLY + "." + TallyApproximation.VALUE;
	public static final String REPLY_TALLY_COST = REPLY_TALLY + "." + TallyApproximation.COST;
	public static final String REPLY_TALLY_APPRECIATION = REPLY_TALLY + "." + TallyApproximation.APPRECIATION;
	public static final String REPLY_TALLY_PROMOTION = REPLY_TALLY + "." + TallyApproximation.PROMOTION;
	public static final String AGGREGATE = Comment.AGGREGATE;
	public static final String AGGREGATE_HOUR = AGGREGATE + "." + TimeSumAggregate.HOUR;
	public static final String AGGREGATE_DAY = AGGREGATE + "." + TimeSumAggregate.DAY;
	public static final String AGGREGATE_MONTH = AGGREGATE + "." + TimeSumAggregate.MONTH;
	public static final String AGGREGATE_YEAR = AGGREGATE + "." + TimeSumAggregate.YEAR;
	public static final String LAST_PROMOTION = Comment.LAST_PROMOTION;

	public static final String ARCHIVED = Comment.ARCHIVED;
	public static final String WARNING = Comment.WARNING;
	public static final String WARNING_VALUE = Comment.WARNING_VALUE;
	public static final String FLAGGED = Comment.FLAGGED;
	public static final String VOTES = Comment.VOTES;
	public static final String FLAG_VALUE = Comment.FLAG_VALUE;
	
	public static final String LANGUAGE = Comment.LANGUAGE;
	
	@Autowired
	protected MongoTemplate mongoTemplate;

	private DBObject createQuery() {
		DBObject query = new BasicDBObject();
		query.put(INITIALIZED, true);
		return query;
	}
	
	private static String getSortString(SORT_OPTION sort, TIME_OPTION time) {
		if(sort == null) {
			return VALUE;
		} else if(SORT_OPTION.VALUE.equals(sort)) {
			if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
				return VALUE;
			} else {
				return AGGREGATE + "." + PyUtils.convertTimeOption(time);
			}
		} else if(SORT_OPTION.PROMOTION.equals(sort)) {
			return PROMOTION;
		} else if(SORT_OPTION.COST.equals(sort)) {
			return COST;
		} else {
			if(TIME_OPTION.ALLTIME.equals(time)) {
				return VALUE;
			} else {
				return AGGREGATE + "." + PyUtils.convertTimeOption(time);
			}
		}
	}
	
	private static String getHint(SORT_OPTION sort, TIME_OPTION time) {
		if(sort == null) {
			return IndexNames.COMMENT_VALUE;
		} else if(SORT_OPTION.VALUE.equals(sort)) {
			if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
				return IndexNames.COMMENT_VALUE;
			} else if(TIME_OPTION.HOUR.equals(time)) {
				return IndexNames.COMMENT_AGGREGATE_HOUR;
			} else if(TIME_OPTION.DAY.equals(time)) {
				return IndexNames.COMMENT_AGGREGATE_DAY;
			} else if(TIME_OPTION.MONTH.equals(time)) {
				return IndexNames.COMMENT_AGGREGATE_MONTH;
			} else if(TIME_OPTION.YEAR.equals(time)) {
				return IndexNames.COMMENT_AGGREGATE_YEAR;
			}
		} else if(SORT_OPTION.PROMOTION.equals(sort)) {
			return IndexNames.COMMENT_PROMOTION;
		} else if(SORT_OPTION.COST.equals(sort)) {
			return IndexNames.COMMENT_COST;
		} else {
			if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
				return IndexNames.COMMENT_VALUE;
			} else if(TIME_OPTION.HOUR.equals(time)) {
				return IndexNames.COMMENT_AGGREGATE_HOUR;
			} else if(TIME_OPTION.DAY.equals(time)) {
				return IndexNames.COMMENT_AGGREGATE_DAY;
			} else if(TIME_OPTION.MONTH.equals(time)) {
				return IndexNames.COMMENT_AGGREGATE_MONTH;
			} else if(TIME_OPTION.YEAR.equals(time)) {
				return IndexNames.COMMENT_AGGREGATE_YEAR;
			}
		}
		return IndexNames.COMMENT_VALUE;
	}
	
	/*
	@Override
	public int getCommentCount(ObjectId baseId, String baseString, COMMENT_TYPE type)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(type);
		if(baseId == null && baseString == null) {
			throw new DaoException();
		}
		
		DBObject query = new BasicDBObject();
		if(baseId != null) {
			query.put(BASE_ID, baseId);
		}
		if(baseString != null) {
			query.put(BASE_STRING, baseString);
		}
		query.put(TYPE, type);
		
		try {
			return collection.find(query).count();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	*/
	
	@Override
	@Cacheable(value = CacheNames.COMMENT, key = "#p0")
	public Comment findCachedComment(ObjectId id) throws DaoException {
		return findComment(id);
	}
	
	@Override
	public Comment findComment(ObjectId id) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		query.putAll(createQuery());
		
		try {
			DBObject obj = collection.findOne(query);
			Comment comment = mongoTemplate.getConverter().read(Comment.class, obj);
			return comment;
		} catch(Exception e) {
			throw new DaoException();
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.COMMENT_PAGED, key = "#p0?.toString()"
			+"+':'+"+ "#p1" 
			+"+':'+"+ "#p2" 
			+"+':'+"+ "#p3?.getPageNumber()"
			+"+':'+"+ "#p3?.getPageSize()" 
			+"+':'+"+ "#p4?.getSort()?.toString()"
			+"+':'+"+ "#p4?.getTime()?.toString()"
			+"+':'+"+ "#p4?.getWarning()")
	public Page<Comment> getSortedComments(List<String> types, String language, 
			ObjectId authorId, Pageable pageable, Filter filter) throws DaoException {
		return getSortedComments(null, null, types, null, false, language, authorId, pageable, filter);
	}
	
	@Override
	@Cacheable(value = CacheNames.COMMENT_REPLY_PAGED, key = "#p0?.toStringMongod()"
			+"+':'+"+ "#p1" 
			+"+':'+"+ "#p2?.toString()"
			+"+':'+"+ "#p3?.toStringMongod()" 
			+"+':'+"+ "#p4"
			+"+':'+"+ "#p5" 
			+"+':'+"+ "#p6?.getPageNumber()"
			+"+':'+"+ "#p6?.getPageSize()" 
			+"+':'+"+ "#p7?.getSort()?.toString()"
			+"+':'+"+ "#p7?.getTime()?.toString()"
			+"+':'+"+ "#p7?.getWarning()")
	public Page<Comment> getSortedReplyComments(ObjectId baseId, String baseString, 
			List<String> types, ObjectId parentId, boolean noSubComments, String language, 
			Pageable pageable, Filter filter) throws DaoException {
		return getSortedComments(baseId, baseString, types, parentId, noSubComments, language, null, pageable, filter);
	}
	
	private Page<Comment> getSortedComments(ObjectId baseId, String baseString, 
			List<String> types, ObjectId parentId, boolean noSubComments, String language, 
			ObjectId authorId, Pageable pageable, Filter filter) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);

		CheckUtil.nullCheck(pageable);
		
		DBCursor cursor = null;

		Date lastAggregate = null;
		String sort = VALUE;
		TIME_OPTION timeOption = TIME_OPTION.ALLTIME;
		SORT_OPTION sortOption = SORT_OPTION.VALUE;
		String timeField = CREATED;
		DBObject query = new BasicDBObject();
		if(filter != null) {
			sortOption = filter.getSort();
			timeOption = filter.getTime();
			sort = getSortString(sortOption, timeOption);
			lastAggregate = PyUtils.getTime(timeOption);
		}
		query.putAll(createQuery());
		query.put(ENABLED, true);
		query.put(REMOVED, false);
		query.put(FLAGGED, false);
		if(lastAggregate != null) {
			if(sort.contains(AGGREGATE)) {
				timeField = LAST_PROMOTION;
			} else {
				timeField = CREATED;
			}
			query.put(timeField, 
					new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, lastAggregate));
		} else {
			query.put(timeField, new BasicDBObject(DaoQueryStrings.EXISTS, true));
		}
		if(types != null && !types.isEmpty()) {
			query.put(TYPE, new BasicDBObject(DaoQueryStrings.IN, types));
		} else {
			query.put(TYPE, new BasicDBObject(DaoQueryStrings.EXISTS, true));
		}
		if(language != null) {
			query.put(LANGUAGE, language);
		} else {
			query.put(LANGUAGE, new BasicDBObject(DaoQueryStrings.EXISTS, true));
		}
		if(authorId != null) {
			query.put(AUTHOR_ID, authorId);
		} else {
			query.put(AUTHOR_ID, new BasicDBObject(DaoQueryStrings.EXISTS, true));
		}
		if(noSubComments) {
			query.put(PARENT, null);
		} else if(parentId != null) {
			query.put(PARENT, parentId);
		}
		if(baseId != null) {
			query.put(BASE_ID, baseId);
		}
		if(baseString != null) {
			query.put(BASE_STRING, baseString);
		}
		if(filter != null && filter.getWarning() != null) {
			query.put(WARNING, filter.getWarning());
		}
		
		try {
			DBObject sortObject = new BasicDBObject(sort, DaoValues.SORT_DESCENDING);
			sortObject.put(timeField, DaoValues.SORT_DESCENDING);
			cursor = collection.find(query)
							.sort(sortObject)
							.hint(getHint(sortOption, timeOption))
							.skip(pageable.getOffset())
							.limit(pageable.getPageSize());
			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Comment> comments = new ArrayList<Comment>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Comment comment = mongoTemplate.getConverter().read(Comment.class,  obj);
				comments.add(comment);
			}
			
			return new PageImpl<Comment>(comments, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.COMMENT_USER_PAGED, key = "#p0?.toStringMongod()"
			+"+':'+"+ "#p1?.toStringMongod()"
			+"+':'+"+ "#p2?.getPageNumber()"
			+"+':'+"+ "#p2?.getPageSize()" 
			+"+':'+"+ "#p3"
			+"+':'+"+ "#p4")
	public Page<Comment> getUserComments(ObjectId authorId, ObjectId beneficiaryId, 
			Pageable pageable, boolean showDisabled, Boolean warning) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);

		CheckUtil.nullCheck(pageable);
		
		DBCursor cursor = null;

		DBObject query = new BasicDBObject();
		String hint = IndexNames.COMMENT_BENEFICIARY;
		if(authorId != null) {
			query.put(AUTHOR_ID, authorId);
			hint = IndexNames.COMMENT_AUTHOR;
		}
		if(beneficiaryId != null) {
			query.put(BENEFICIARY_ID, beneficiaryId);
		}
		query.putAll(createQuery());
		if(!showDisabled) {
			query.put(ENABLED, true);
			query.put(REMOVED, false);
			query.put(FLAGGED, false);
		}
		if(warning != null) {
			query.put(WARNING, warning);
		}
		
		try {
			cursor = collection.find(query)
						.sort(new BasicDBObject(LAST_PROMOTION, DaoValues.SORT_DESCENDING))
						.hint(hint)
						.skip(pageable.getOffset())
						.limit(pageable.getPageSize());
			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Comment> comments = new ArrayList<Comment>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Comment comment = mongoTemplate.getConverter().read(Comment.class,  obj);
				comments.add(comment);
			}
			
			return new PageImpl<Comment>(comments, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, key = "#p0")
	*/
	@Override
	public void incrementReplyCount(ObjectId id, long count) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT,
							new BasicDBObject(REPLY_COUNT, count)), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, key = "#p0")
	*/
	@Override
	public void incrementAppreciationPromotionCount(ObjectId id, Long appreciationCount, 
			Long promotionCount) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id);
		
		DBObject update = new BasicDBObject();
		DBObject increment = new BasicDBObject();
		
		if(appreciationCount != null) {
			increment.put(APPRECIATION_COUNT, appreciationCount);
		}
		if(promotionCount != null) {
			increment.put(PROMOTION_COUNT, promotionCount);
		}
		
		if(appreciationCount != null || promotionCount != null) {
			update.put(DaoQueryStrings.INCREMENT, increment);
		}
		update.put(DaoQueryStrings.SET, 
				new BasicDBObject(LAST_PROMOTION, new Date()));
		
		try {
			collection.update(new BasicDBObject(ID, id), update, false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, key = "#p0")
	*/
	@Override
	public void incrementReplyTallyAppreciationPromotion(ObjectId id, Long appreciation, 
			Long promotion) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id);

		DBObject update = new BasicDBObject();
		if(appreciation != null) {
			update.put(REPLY_TALLY_APPRECIATION, appreciation);
		}
		if(promotion != null) {
			update.put(REPLY_TALLY_PROMOTION, promotion);
			update.put(REPLY_TALLY_VALUE, promotion);
		}
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, update), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, key = "#p0")
	*/
	@Override
	public void incrementReplyTallyCost(ObjectId id, Long cost) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id, cost);

		DBObject update = new BasicDBObject();
		update.put(REPLY_TALLY_COST, cost);
		update.put(REPLY_TALLY_VALUE, cost);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, update), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void setAggregate(ObjectId id, TimeSumAggregate agg) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id, agg);
		
		DBObject update = new BasicDBObject();
		update.put(AGGREGATE_HOUR, agg.getHour());
		update.put(AGGREGATE_DAY, agg.getDay());
		update.put(AGGREGATE_MONTH, agg.getMonth());
		update.put(AGGREGATE_YEAR, agg.getYear());
		
		try {
			collection.update(new BasicDBObject(ID, id),
					new BasicDBObject(DaoQueryStrings.SET, update), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void updateAggregation(ObjectId id, long value, TIME_OPTION segment)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id, segment);

		DBObject update = new BasicDBObject();
		update.put(AGGREGATE + "." + PyUtils.convertTimeOption(segment), value);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.SET, update));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void emptyAggregations(TIME_OPTION segment) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);

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
	
	@Override
	public void updateComment(ObjectId id, String content) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject update = new BasicDBObject();
		DBObject set = new BasicDBObject();
		set.put(CONTENT, content);
		
		update.put(DaoQueryStrings.SET, set);
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, key = "#p0")
	*/
	@Override
	public void setEnabled(ObjectId id, Boolean enabled, Boolean removed, 
			Boolean flagged, Boolean warning, Boolean paid, Boolean initialized) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id);

		DBObject query = new BasicDBObject();
		query.put(ID, id);

		DBObject set = new BasicDBObject();
		if(enabled != null) {
			set.put(ENABLED, enabled);
		}
		if(flagged != null) {
			set.put(FLAGGED, flagged);
		}
		if(removed != null) {
			set.put(REMOVED, removed);
		}
		if(warning != null) {
			set.put(WARNING, warning);
		}
		if(paid != null) {
			set.put(PAID, paid);
		}
		if(initialized != null) {
			set.put(INITIALIZED, initialized);
		}
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, set);
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, key = "#p0")
	*/
	@Override
	public void addVote(ObjectId id, ObjectId userId, long weight) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(id, userId);

		DBObject query = new BasicDBObject();
		query.put(ID, id);
		query.put(VOTES, new BasicDBObject(DaoQueryStrings.NOT_EQUAL, userId));
		
		List<ObjectId> eachList = new ArrayList<ObjectId>();
		eachList.add(userId);
		
		DBObject pushObject = new BasicDBObject();
		pushObject.put(DaoQueryStrings.EACH, eachList);
		pushObject.put(DaoQueryStrings.SLICE, (0 - ServiceValues.MAX_VOTES));
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.PUSH, new BasicDBObject(VOTES, pushObject));
		update.put(DaoQueryStrings.INCREMENT, new BasicDBObject(FLAG_VALUE, weight));
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, key = "#p0")
	*/
	@Override
	public void addWarn(ObjectId id, Boolean warn, long amount) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);

		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, new BasicDBObject(WARNING_VALUE, amount));
		if(warn != null) {
			update.put(DaoQueryStrings.SET, new BasicDBObject(WARNING, warn));
		}
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void markArchived(Date olderThanCreated) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(olderThanCreated);

		DBObject query = new BasicDBObject();
		query.put(CREATED, new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanCreated));
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, new BasicDBObject(ARCHIVED, new Date()));
		
		try {
			collection.update(query, update, false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, allEntries = true)
	*/
	@Override
	public void rename(ObjectId userId, String replacement, boolean asAuthor) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(userId);

		DBObject query = new BasicDBObject();
		DBObject set = new BasicDBObject();
		
		if(asAuthor) {
			query.put(AUTHOR_ID, userId);
			set.put(AUTHOR_NAME, replacement);
		} else {
			query.put(BENEFICIARY_ID, userId);
			set.put(BENEFICIARY_NAME, replacement);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.COMMENT, allEntries = true)
	*/
	@Override
	public void removeUser(ObjectId userId, boolean asAuthor) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.COMMENT);
		
		CheckUtil.nullCheck(userId);

		DBObject query = new BasicDBObject();
		DBObject set = new BasicDBObject();
		
		if(asAuthor) {
			query.put(AUTHOR_ID, userId);
			set.put(AUTHOR_EXISTS, false);
		} else {
			query.put(BENEFICIARY_ID, userId);
			set.put(BENEFICIARY_EXISTS, false);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

}
