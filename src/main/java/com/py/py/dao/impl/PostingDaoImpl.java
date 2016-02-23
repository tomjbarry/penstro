package com.py.py.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
import com.py.py.dao.custom.PostingDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.dao.util.DBObjectConverter;
import com.py.py.domain.Posting;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.constants.IndexNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.ImageLink;
import com.py.py.domain.subdomain.Tally;
import com.py.py.domain.subdomain.TallyApproximation;
import com.py.py.domain.subdomain.TimeSumAggregate;
import com.py.py.enumeration.SORT_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.util.PyUtils;

public class PostingDaoImpl implements PostingDaoCustom {

	public static final String ID = Posting.ID;
	public static final String PAID = Posting.PAID;
	public static final String INITIALIZED = Posting.INITIALIZED;
	public static final String TITLE = Posting.TITLE;
	public static final String CONTENT = Posting.CONTENT;
	public static final String PREVIEW = Posting.PREVIEW;
	public static final String IMAGE = Posting.IMAGE;
	public static final String ENABLED = Posting.ENABLED;
	public static final String REMOVED = Posting.REMOVED;
	public static final String AUTHOR = Posting.AUTHOR;
	public static final String AUTHOR_ID = AUTHOR + "." + CachedUsername.OID;
	public static final String AUTHOR_NAME = AUTHOR + "." + CachedUsername.USERNAME;
	public static final String AUTHOR_EXISTS = AUTHOR + "." + CachedUsername.EXISTS;
	public static final String BENEFICIARY = Posting.BENEFICIARY;
	public static final String BENEFICIARY_ID = BENEFICIARY + "." + CachedUsername.OID;
	public static final String BENEFICIARY_NAME = BENEFICIARY + "." + CachedUsername.USERNAME;
	public static final String BENEFICIARY_EXISTS = BENEFICIARY + "." + CachedUsername.EXISTS;
	public static final String CREATED = Posting.CREATED;
	public static final String VALUE = Posting.TALLY + "." + Tally.VALUE;
	public static final String PROMOTION = Posting.TALLY + "." + Tally.PROMOTION;
	public static final String COST = Posting.TALLY + "." + Tally.COST;
	public static final String COMMENT_COUNT = Posting.COMMENT_COUNT;
	public static final String APPRECIATION_COUNT = Posting.APPRECIATION_COUNT;
	public static final String PROMOTION_COUNT = Posting.PROMOTION_COUNT;
	public static final String COMMENT_TALLY = Posting.COMMENT_TALLY;
	public static final String COMMENT_TALLY_VALUE = COMMENT_TALLY + "." + TallyApproximation.VALUE;
	public static final String COMMENT_TALLY_COST = COMMENT_TALLY + "." + TallyApproximation.COST;
	public static final String COMMENT_TALLY_APPRECIATION = COMMENT_TALLY + "." + TallyApproximation.APPRECIATION;
	public static final String COMMENT_TALLY_PROMOTION = COMMENT_TALLY + "." + TallyApproximation.PROMOTION;
	public static final String TAGS = Posting.TAGS;
	public static final String TAG_VALUES = Posting.TAG_VALUES;
	public static final String TAGS_SUM = Posting.TAGS_SUM;
	public static final String AGGREGATE = Posting.AGGREGATE;
	public static final String AGGREGATE_HOUR = AGGREGATE + "." + TimeSumAggregate.HOUR;
	public static final String AGGREGATE_DAY = AGGREGATE + "." + TimeSumAggregate.DAY;
	public static final String AGGREGATE_MONTH = AGGREGATE + "." + TimeSumAggregate.MONTH;
	public static final String AGGREGATE_YEAR = AGGREGATE + "." + TimeSumAggregate.YEAR;
	public static final String LAST_PROMOTION = Posting.LAST_PROMOTION;
	public static final String WARNING = Posting.WARNING;
	public static final String WARNING_VALUE = Posting.WARNING_VALUE;
	public static final String FLAGGED = Posting.FLAGGED;
	public static final String VOTES = Posting.VOTES;
	public static final String FLAG_VALUE = Posting.FLAG_VALUE;
	public static final String LANGUAGE = Posting.LANGUAGE;
	public static final String ARCHIVED = Posting.ARCHIVED;
	
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
			if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
				return VALUE;
			} else {
				return AGGREGATE + "." + PyUtils.convertTimeOption(time);
			}
		}
	}
	
	private static String getHint(SORT_OPTION sort, TIME_OPTION time) {
		if(sort == null) {
			return IndexNames.POSTING_VALUE;
		} else if(SORT_OPTION.VALUE.equals(sort)) {
			if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
				return IndexNames.POSTING_VALUE;
			} else if(TIME_OPTION.HOUR.equals(time)) {
				return IndexNames.POSTING_AGGREGATE_HOUR;
			} else if(TIME_OPTION.DAY.equals(time)) {
				return IndexNames.POSTING_AGGREGATE_DAY;
			} else if(TIME_OPTION.MONTH.equals(time)) {
				return IndexNames.POSTING_AGGREGATE_MONTH;
			} else if(TIME_OPTION.YEAR.equals(time)) {
				return IndexNames.POSTING_AGGREGATE_YEAR;
			}
		} else if(SORT_OPTION.PROMOTION.equals(sort)) {
			return IndexNames.POSTING_PROMOTION;
		} else if(SORT_OPTION.COST.equals(sort)) {
			return IndexNames.POSTING_COST;
		} else {
			if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
				return IndexNames.POSTING_VALUE;
			} else if(TIME_OPTION.HOUR.equals(time)) {
				return IndexNames.POSTING_AGGREGATE_HOUR;
			} else if(TIME_OPTION.DAY.equals(time)) {
				return IndexNames.POSTING_AGGREGATE_DAY;
			} else if(TIME_OPTION.MONTH.equals(time)) {
				return IndexNames.POSTING_AGGREGATE_MONTH;
			} else if(TIME_OPTION.YEAR.equals(time)) {
				return IndexNames.POSTING_AGGREGATE_YEAR;
			}
		}
		return IndexNames.POSTING_VALUE;
	}
	
	@Override
	@Cacheable(value = CacheNames.POSTING, key = "#p0")
	public Posting findCachedPosting(ObjectId id) throws DaoException {
		return findPosting(id);
	}
	
	@Override
	public Posting findPosting(ObjectId id) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		query.putAll(createQuery());
		
		try {
			DBObject obj = collection.findOne(query);
			Posting posting = mongoTemplate.getConverter().read(Posting.class, obj);
			return posting;
		} catch(Exception e) {
			throw new DaoException();
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.POSTING_PAGED, key = "#p0"
			+"+':'+"+ "#p1"
			+"+':'+"+ "#p2?.getPageNumber()"
			+"+':'+"+ "#p2?.getPageSize()" 
			+"+':'+"+ "#p3?.getSort()?.toString()"
			+"+':'+"+ "#p3?.getTime()?.toString()"
			+"+':'+"+ "#p3?.getWarning()"
			+"+':'+"+ "#p3?.getTags()?.toString()")
	public Page<Posting> getSortedPostings(String language, ObjectId authorId, 
			Pageable pageable, Filter filter) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
		}
		/*
		if(filter != null) {
			List<String> excluded = new ArrayList<String>();
			if(filter.getExcludeTags() != null) {
				if(filter.getExcludeTags().isEmpty()) {
					// do nothing, no way to look for something with ALL the tags!
				} else {
					for(String tag : filter.getExcludeTags()) {
						query.put(POSTING_TAGS + "." + tag, null);
						excluded.add(tag);
					}
				}
			}
			if(filter.getTags() != null) {
				if(filter.getTags().isEmpty()) {
					query.put(POSTING_TAGS, null);
				} else {
					for(String tag : filter.getTags()) {
						if(!excluded.contains(tag)) {
							query.put(POSTING_TAGS + "." + tag, 
									new BasicDBObject(DaoQueryStrings.NOT_EQUAL, null));
						}
					}
				}
			}
		}*/

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
		if(filter != null) {
			if(filter.getTags() != null) {
				if(filter.getTags().isEmpty()) {
					query.put(TAGS, null);
				} else {
					query.put(TAGS, new BasicDBObject(DaoQueryStrings.IN, filter.getTags()));
				}
			}
			if(filter.getWarning() != null) {
				query.put(WARNING, filter.getWarning());
			}
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
			
			List<Posting> postings = new ArrayList<Posting>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Posting posting = mongoTemplate.getConverter().read(Posting.class,  obj);
				postings.add(posting);
			}
			
			return new PageImpl<Posting>(postings, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.POSTING_USER_PAGED, key = "#p0?.toStringMongod()"
			+"+':'+"+ "#p1?.toStringMongod()"
			+"+':'+"+ "#p2?.getPageNumber()"
			+"+':'+"+ "#p2?.getPageSize()"
			+"+':'+"+ "#p3"
			+"+':'+"+ "#p4?.toString()"
			+"+':'+"+ "#p5")
	public Page<Posting> getUserPostings(ObjectId authorId, ObjectId beneficiaryId, 
			Pageable pageable, boolean showDisabled, List<String> tags, Boolean warning) 
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(pageable);
		
		DBCursor cursor = null;
		
		DBObject query = new BasicDBObject();
		String hint = IndexNames.POSTING_BENEFICIARY;
		if(authorId != null) {
			query.put(AUTHOR_ID, authorId);
			hint = IndexNames.POSTING_AUTHOR;
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
		if(tags != null) {
			if(tags.isEmpty()) {
				query.put(TAGS, null);
			} else {
				query.put(TAGS, new BasicDBObject(DaoQueryStrings.IN, tags));
			}
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
			
			List<Posting> postings = new ArrayList<Posting>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Posting posting = mongoTemplate.getConverter().read(Posting.class,  obj);
				postings.add(posting);
			}
			
			return new PageImpl<Posting>(postings, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void incrementCommentCount(ObjectId id, long count) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(id);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT,
							new BasicDBObject(COMMENT_COUNT, count)), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void incrementAppreciationPromotionCount(ObjectId id, Long appreciationCount, 
			Long promotionCount) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void incrementCommentTallyAppreciationPromotion(ObjectId id, Long appreciation, 
			Long promotion) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, update), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void incrementCommentTallyCost(ObjectId id, Long cost) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(id, cost);
		
		DBObject update = new BasicDBObject();
		update.put(COMMENT_TALLY_COST, cost);
		update.put(COMMENT_TALLY_VALUE, cost);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, update), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void incrementTagValues(ObjectId id, Map<String, Long> tags) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(id, tags);
		
		DBObject increment = new BasicDBObject();
		long sum = 0;
		for(Map.Entry<String, Long> entry : tags.entrySet()) {
			sum += entry.getValue();
			increment.put(TAG_VALUES + "." + entry.getKey(),  entry.getValue());
		}
		increment.put(TAGS_SUM, sum);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, increment), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void setTags(ObjectId id, List<String> tags) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(id, tags);
		
		DBObject set = new BasicDBObject();
		set.put(TAGS, tags);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.SET, set), false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void setAggregate(ObjectId id, TimeSumAggregate agg) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
	public void updateAggregation(ObjectId id, long value, TIME_OPTION segment) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(id);

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
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void updatePosting(ObjectId id, String title, String content, String preview, ImageLink imageLink) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject image = null;
		DBObject update = new BasicDBObject();
		DBObject set = new BasicDBObject();
		set.put(TITLE, title);
		set.put(CONTENT, content);
		set.put(PREVIEW, preview);
		if(imageLink != null) {
			image = DBObjectConverter.convertImageLink(imageLink);
			set.put(IMAGE, image);
		} else {
			update.put(DaoQueryStrings.UNSET, new BasicDBObject(IMAGE, ""));
		}
		
		update.put(DaoQueryStrings.SET, set);
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<Posting> getPostingsByLastPromotion(Date lastPromotion, Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(pageable, lastPromotion);
		
		DBObject query = new BasicDBObject();
		query.put(LAST_PROMOTION, new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, lastPromotion));
		query.put(TAG_VALUES, new BasicDBObject(DaoQueryStrings.NOT_EQUAL, null));
		
		try {
			DBCursor cursor = collection.find(query)
				.hint(IndexNames.POSTING_LAST_PROMOTION)
				.skip(pageable.getOffset())
				.limit(pageable.getPageSize());

			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Posting> postings = new ArrayList<Posting>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Posting posting = mongoTemplate.getConverter().read(Posting.class,  obj);
				postings.add(posting);
			}
			
			return new PageImpl<Posting>(postings, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void setEnabled(ObjectId id, Boolean enabled, Boolean removed,
			Boolean flagged, Boolean warning, Boolean paid, Boolean initialized) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void addVote(ObjectId id, ObjectId userId, long weight) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
	@CacheEvict(value = CacheNames.POSTING, key = "#p0")
	*/
	@Override
	public void addWarn(ObjectId id, Boolean warn, long amount) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
	@CacheEvict(value = CacheNames.POSTING, allEntries = true)
	*/
	@Override
	public void rename(ObjectId userId, String replacement, boolean asAuthor) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
	@CacheEvict(value = CacheNames.POSTING, allEntries = true)
	*/
	@Override
	public void removeUser(ObjectId userId, boolean asAuthor) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
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
