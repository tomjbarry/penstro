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
import com.mongodb.DuplicateKeyException;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.constants.CacheNames;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.UserInfoDaoCustom;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.dao.util.DBObjectConverter;
import com.py.py.domain.UserInfo;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.constants.IndexNames;
import com.py.py.domain.subdomain.AppreciationDate;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.Settings;
import com.py.py.domain.subdomain.TallyApproximation;
import com.py.py.domain.subdomain.TimeSumAggregate;
import com.py.py.enumeration.SETTING_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.util.PyUtils;

public class UserInfoDaoImpl implements UserInfoDaoCustom {

	private static final String ID = UserInfo.ID;
	private static final String USERNAME = UserInfo.USERNAME;
	private static final String LAST_CHECKED_NOTIFICATIONS = UserInfo.LAST_CHECKED_NOTIFICATIONS;
	private static final String LAST_CHECKED_FEED = UserInfo.LAST_CHECKED_FEED;
	private static final String FOLLOWER_COUNT = UserInfo.FOLLOWER_COUNT;
	private static final String FOLLOWEE_COUNT = UserInfo.FOLLOWEE_COUNT;
	private static final String DESCRIPTION = UserInfo.DESCRIPTION;
	private static final String APPRECIATION_RESPONSE = UserInfo.APPRECIATION_RESPONSE;
	private static final String APPRECIATION_RESPONSE_WARNING = UserInfo.APPRECIATION_RESPONSE_WARNING;
	
	private static final String COMMENT_COUNT = UserInfo.COMMENT_COUNT;
	private static final String COMMENT_TALLY = UserInfo.COMMENT_TALLY;
	private static final String COMMENT_TALLY_VALUE = COMMENT_TALLY + "." + TallyApproximation.VALUE;
	private static final String COMMENT_TALLY_COST = COMMENT_TALLY + "." + TallyApproximation.COST;
	private static final String COMMENT_TALLY_APPRECIATION = COMMENT_TALLY + "." + TallyApproximation.APPRECIATION;
	private static final String COMMENT_TALLY_PROMOTION = COMMENT_TALLY + "." + TallyApproximation.PROMOTION;
	
	private static final String APPRECIATION_COUNT = UserInfo.APPRECIATION_COUNT;
	private static final String APPRECIATION = UserInfo.APPRECIATION;
	private static final String PROMOTION_COUNT = UserInfo.PROMOTION_COUNT;
	private static final String PROMOTION = UserInfo.PROMOTION;
	
	private static final String CONTRIBUTION_POSTINGS_COUNT = UserInfo.CONTRIBUTED_POSTINGS;
	private static final String CONTRIBUTION_COMMENTS_COUNT = UserInfo.CONTRIBUTED_COMMENTS;
	private static final String CONTRIBUTION_APPRECIATION_COUNT = UserInfo.CONTRIBUTED_APPRECIATION_COUNT;
	private static final String CONTRIBUTION_PROMOTION_COUNT = UserInfo.CONTRIBUTED_PROMOTION_COUNT;
	
	private static final String CONTRIBUTION_TALLY = UserInfo.CONTRIBUTION_TALLY;
	private static final String CONTRIBUTION_TALLY_VALUE = CONTRIBUTION_TALLY + "." + TallyApproximation.VALUE;
	private static final String CONTRIBUTION_TALLY_COST = CONTRIBUTION_TALLY + "." + TallyApproximation.COST;
	private static final String CONTRIBUTION_TALLY_APPRECIATION = CONTRIBUTION_TALLY + "." + TallyApproximation.APPRECIATION;
	private static final String CONTRIBUTION_TALLY_PROMOTION = CONTRIBUTION_TALLY + "." + TallyApproximation.PROMOTION;

	public static final String AGGREGATE = UserInfo.AGGREGATE;
	public static final String AGGREGATE_HOUR = AGGREGATE + "." + TimeSumAggregate.HOUR;
	public static final String AGGREGATE_DAY = AGGREGATE + "." + TimeSumAggregate.DAY;
	public static final String AGGREGATE_MONTH = AGGREGATE + "." + TimeSumAggregate.MONTH;
	public static final String AGGREGATE_YEAR = AGGREGATE + "." + TimeSumAggregate.YEAR;
	public static final String LAST_PROMOTION = UserInfo.LAST_PROMOTION;
	
	public static final String PENDING_ACTIONS = UserInfo.PENDING_ACTIONS;
	
	public static final String SETTINGS = UserInfo.SETTINGS;
	public static final String SETTINGS_OPTIONS = SETTINGS + "." + Settings.OPTIONS;
	public static final String SETTINGS_HIDDEN_NOTIFICATIONS = SETTINGS + "." + Settings.HIDDEN_NOTIFICATIONS;
	public static final String SETTINGS_FILTERS = SETTINGS + "." + Settings.FILTERS;
	public static final String SETTINGS_LANGUAGE = SETTINGS + "." + Settings.LANGUAGE;
	public static final String SETTINGS_INTERFACE_LANGUAGE = SETTINGS + "." + Settings.INTERFACE_LANGUAGE;
	
	public static final String FILTER_SORT = Filter.SORT;
	public static final String FILTER_TIME = Filter.TIME;
	public static final String FILTER_TAGS = Filter.TAGS;
	//public static final String FILTER_TAGS_EXCLUDE = "excludeTags";
	
	public static final String WARNING = UserInfo.WARNING;
	public static final String FLAGGED = UserInfo.FLAGGED;
	public static final String VOTES = UserInfo.VOTES;
	public static final String FLAG_VALUE = UserInfo.FLAG_VALUE;
	
	public static final String DATE = AppreciationDate.DATE;
	public static final String CACHED_USERNAME = AppreciationDate.CACHED_USERNAME;
	public static final String APPRECIATION_DATES = UserInfo.APPRECIATION_DATES;
	public static final String CACHED_USERNAME_EXISTS = CACHED_USERNAME + "." + CachedUsername.EXISTS;
	public static final String CACHED_USERNAME_OID = CACHED_USERNAME + "." + CachedUsername.OID;
	
	@Autowired
	private MongoTemplate mongoTemplate;

	private DBObject createFilterList(List<Filter> filters) {
		DBObject map = new BasicDBObject();
		Integer num = 0;
		for(Filter f : filters) {
			DBObject obj = new BasicDBObject();
			if(f.getSort() != null) {
				obj.put(FILTER_SORT, f.getSort().toString());
			}
			if(f.getTime() != null) {
				obj.put(FILTER_TIME, f.getTime().toString());
			}
			obj.put(FILTER_TAGS, f.getTags());
			//obj.put(FILTER_TAGS_EXCLUDE, f.getExcludeTags());
			map.put(num.toString(), obj);
			num++;
		}
		return map;
	}
	
	private DBObject createQuery() {
		DBObject query = new BasicDBObject();
		query.put(SETTINGS_OPTIONS + "." + SETTING_OPTION.HIDE_USER_PROFILE.toString(), 
				new BasicDBObject(DaoQueryStrings.NOT_EQUAL, true));
		return query;
	}
	
	protected String getHint(TIME_OPTION time) {
		if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
			return IndexNames.USER_INFO_VALUE_TIME_HIDDEN_LANGUAGE;
		} else if(TIME_OPTION.YEAR.equals(time)) {
			return IndexNames.USER_INFO_AGGREGATE_YEAR_TIME_HIDDEN_LANGUAGE;
		} else if(TIME_OPTION.MONTH.equals(time)) {
			return IndexNames.USER_INFO_AGGREGATE_MONTH_TIME_HIDDEN_LANGUAGE;
		} else if(TIME_OPTION.DAY.equals(time)) {
			return IndexNames.USER_INFO_AGGREGATE_DAY_TIME_HIDDEN_LANGUAGE;
		} else if(TIME_OPTION.HOUR.equals(time)) {
			return IndexNames.USER_INFO_AGGREGATE_HOUR_TIME_HIDDEN_LANGUAGE;
		}
		return IndexNames.USER_INFO_VALUE_TIME_HIDDEN_LANGUAGE;
	}
	
	private String getSortString(TIME_OPTION time) {
		if(time == null || TIME_OPTION.ALLTIME.equals(time)) {
			return PROMOTION;
		} else {
			return AGGREGATE + "." + PyUtils.convertTimeOption(time);
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.USER_INFO, key = "#p0")
	public UserInfo findCachedUserInfo(ObjectId id) throws DaoException {
		return findUserInfo(id);
	}
	
	@Override
	public UserInfo findUserInfo(ObjectId id) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);
		
		// do not use create query map, need to get profiles that may be disabled
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		try {
			DBObject obj = collection.findOne(query);
			if(obj == null) {
				return null;
			}
			return mongoTemplate.getConverter().read(UserInfo.class, obj);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void updateProfile(ObjectId id, String description, 
			Boolean warning, boolean noDescription) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		if(noDescription) {
			set.put(DESCRIPTION, null);
			set.put(WARNING, false);
		} else if(description != null) {
			set.put(DESCRIPTION, description);
			set.put(WARNING, warning);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void updateAppreciationResponse(ObjectId id, String appreciationResponse, 
			Boolean warning, boolean noResponse) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		if(noResponse) {
			set.put(APPRECIATION_RESPONSE, null);
			set.put(APPRECIATION_RESPONSE_WARNING, false);
		} else if(appreciationResponse != null) {
			set.put(APPRECIATION_RESPONSE, appreciationResponse);
			set.put(APPRECIATION_RESPONSE_WARNING, warning);
		}
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	@Cacheable(value = CacheNames.USER_INFO_PAGED, key = "#p0" 
			+"+':'+"+ "#p1?.getPageNumber()"
			+"+':'+"+ "#p1?.getPageSize()"
			+"+':'+"+ "#p2?.toString()")
	public Page<UserInfo> findUserInfos(String language, Pageable pageable, TIME_OPTION time) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(pageable);
		
		String sort = getSortString(time);
		
		DBObject query = new BasicDBObject();
		if(time != null && time != TIME_OPTION.ALLTIME) {
			query.put(LAST_PROMOTION, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, PyUtils.getTime(time)));
		}
		query.putAll(createQuery());
		if(language != null) {
			query.put(SETTINGS_LANGUAGE, language);
		}
		query.put(FLAGGED, new BasicDBObject(DaoQueryStrings.LESS_THAN_EQUAL, new Date()));
		
		try {
			DBObject sortObject = new BasicDBObject(sort, DaoValues.SORT_DESCENDING);
			sortObject.put(LAST_PROMOTION, DaoValues.SORT_DESCENDING);
			DBCursor cursor = collection.find(query)
					.sort(sortObject)
					.hint(getHint(time))
					.skip(pageable.getOffset())
					.limit(pageable.getPageSize());
			
			if(cursor == null) {
				throw new DaoException();
			}
			
			List<UserInfo> infos = new ArrayList<UserInfo>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				UserInfo info = mongoTemplate.getConverter().read(UserInfo.class,  obj);
				infos.add(info);
			}
			
			return new PageImpl<UserInfo>(infos, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
		
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void incrementCommentCount(ObjectId id, long count) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
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
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void incrementCommentTallyAppreciationPromotion(ObjectId id, Long appreciation, 
			Long promotion) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
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
					new BasicDBObject(DaoQueryStrings.INCREMENT, 
							update), 
					false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void incrementCommentTallyCost(ObjectId id, Long cost) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id, cost);
		
		DBObject update = new BasicDBObject();
		update.put(COMMENT_TALLY_COST, cost);
		update.put(COMMENT_TALLY_VALUE, cost);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, 
							update), 
					false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void incrementContributionCost(ObjectId id, long cost, long postings, 
			long comments) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);
		
		DBObject update = new BasicDBObject();
		update.put(CONTRIBUTION_TALLY_COST, cost);
		update.put(CONTRIBUTION_TALLY_VALUE, cost);
		update.put(CONTRIBUTION_POSTINGS_COUNT, postings);
		update.put(CONTRIBUTION_COMMENTS_COUNT, comments);

		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.INCREMENT, 
							update), 
					false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void incrementContributionAppreciationPromotion(ObjectId id, 
			CachedUsername target, Long appreciation, Long appreciationCount, 
			Long promotion, Long promotionCount) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);
		
		DBObject update = new BasicDBObject();
		
		DBObject increment = new BasicDBObject();
		if(target != null) {
			AppreciationDate ad = new AppreciationDate(target, new Date());
			
			List<DBObject> eachList = new ArrayList<DBObject>();
			eachList.add(DBObjectConverter.convertAppreciationDate(ad));
			
			DBObject pushObject = new BasicDBObject();
			pushObject.put(DaoQueryStrings.EACH, eachList);
			//pushObject.put(DaoQueryStrings.SORT, new BasicDBObject(DATE, DaoValues.SORT_ASCENDING));
			pushObject.put(DaoQueryStrings.SLICE, (0 - ServiceValues.APPRECIATION_TARGET_MAX));
			
			update.put(DaoQueryStrings.PUSH, 
					new BasicDBObject(APPRECIATION_DATES, pushObject));
		}
		if(appreciation != null) {
			increment.put(CONTRIBUTION_TALLY_APPRECIATION, appreciation);
		}
		if(promotion != null) {
			increment.put(CONTRIBUTION_TALLY_PROMOTION, promotion);
			increment.put(CONTRIBUTION_TALLY_VALUE, promotion);
		}
		if(appreciationCount != null) {
			increment.put(CONTRIBUTION_APPRECIATION_COUNT, appreciationCount);
		}
		if(promotionCount != null) {
			increment.put(CONTRIBUTION_PROMOTION_COUNT, promotionCount);
		}
		update.put(DaoQueryStrings.INCREMENT, increment);

		try {
			collection.update(new BasicDBObject(ID, id), update, 
					false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void incrementAppreciationPromotion(ObjectId id, Long appreciation, 
			Long appreciationCount, Long promotion, Long promotionCount) 
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);
		
		DBObject incrementQuery = new BasicDBObject();
		if(appreciation != null) {
			incrementQuery.put(APPRECIATION, appreciation);
		}
		if(promotion != null) {
			incrementQuery.put(PROMOTION, promotion);
			incrementQuery.put(AGGREGATE_HOUR, promotion);
			incrementQuery.put(AGGREGATE_DAY, promotion);
			incrementQuery.put(AGGREGATE_MONTH, promotion);
			incrementQuery.put(AGGREGATE_YEAR, promotion);
		}
		if(appreciationCount != null) {
			incrementQuery.put(APPRECIATION_COUNT, appreciationCount);
		}
		if(promotionCount != null) {
			incrementQuery.put(PROMOTION_COUNT, promotionCount);
		}
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, incrementQuery);
		update.put(DaoQueryStrings.SET, new BasicDBObject(LAST_PROMOTION, new Date()));

		try {
			collection.update(new BasicDBObject(ID, id), 
							update, false, false);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void updateAggregation(ObjectId id, long value, TIME_OPTION segment) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id, segment);
		

		DBObject updateObject = new BasicDBObject();
		updateObject.put(AGGREGATE + "." + PyUtils.convertTimeOption(segment), value);
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.SET, updateObject));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER, key = "#p0")
	*/
	@Override
	public void updatePendingActions(ObjectId id, List<String> pendingActions) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id, pendingActions);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		set.put(PENDING_ACTIONS, pendingActions);
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void updateLastChecked(ObjectId id, Date notifications, Date feed) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);

		DBObject updateObject = new BasicDBObject();
		if(notifications != null) {
			updateObject.put(LAST_CHECKED_NOTIFICATIONS, notifications);
		}
		if(feed != null) {
			updateObject.put(LAST_CHECKED_FEED, feed);
		}
		
		try {
			collection.update(new BasicDBObject(ID, id), 
					new BasicDBObject(DaoQueryStrings.SET, updateObject));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void emptyAggregations(TIME_OPTION segment) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
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
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void updateSettings(ObjectId id, Map<String, Boolean> options, 
			List<String> hiddenNotifications, List<Filter> filters, String language, 
			String interfaceLanguage) 
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject fieldsObject = new BasicDBObject();
		if(options != null) {
			for(Map.Entry<String, Boolean> entry : options.entrySet()) {
				fieldsObject.put(SETTINGS_OPTIONS + "." + entry.getKey(), entry.getValue());
			}
		}
		if(hiddenNotifications != null) {
			fieldsObject.put(SETTINGS_HIDDEN_NOTIFICATIONS, hiddenNotifications);
		}
		if(filters != null) {
			fieldsObject.put(SETTINGS_FILTERS, createFilterList(filters));
		}
		if(language != null) {
			fieldsObject.put(SETTINGS_LANGUAGE, language);
		}
		if(interfaceLanguage != null) {
			fieldsObject.put(SETTINGS_INTERFACE_LANGUAGE, interfaceLanguage);
		}
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.SET, fieldsObject);
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void updatePendingActions(ObjectId id, List<String> add, List<String> remove) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		CheckUtil.nullCheck(id);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject update = new BasicDBObject();
		if(add != null && !add.isEmpty()) {
			update.put(DaoQueryStrings.ADD_TO_SET, new BasicDBObject(PENDING_ACTIONS, new BasicDBObject(DaoQueryStrings.EACH, add)));
		} else if(remove != null && !remove.isEmpty()) {
			update.put(DaoQueryStrings.PULL, new BasicDBObject(PENDING_ACTIONS, new BasicDBObject(DaoQueryStrings.IN, remove)));
		}
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void incrementFolloweeCount(ObjectId id, long increment) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);

		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, new BasicDBObject(FOLLOWEE_COUNT, increment));
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void incrementFollowerCount(ObjectId id, long increment) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);

		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, new BasicDBObject(FOLLOWER_COUNT, increment));
		
		try {
			collection.update(query, update);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void addVote(ObjectId id, ObjectId userId, long weight) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
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
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void setEnabled(ObjectId id, Date flagged, Boolean warning, boolean clearVotes) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id);

		DBObject query = new BasicDBObject();
		query.put(ID, id);
		
		DBObject set = new BasicDBObject();
		if(flagged != null) {
			set.put(FLAGGED, flagged);
		}
		if(warning != null) {
			set.put(WARNING, warning);
		}
		if(clearVotes) {
			set.put(FLAG_VALUE, 0l);
			set.put(VOTES, new ArrayList<ObjectId>());
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
	@CacheEvict(value = CacheNames.USER_INFO, key = "#p0")
	*/
	@Override
	public void rename(ObjectId id, String replacement) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(id, replacement);
		
		DBObject query = new BasicDBObject(ID, id);
		
		DBObject set = new BasicDBObject();
		set.put(USERNAME, replacement);
	
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(DuplicateKeyException dke) {
			// should never hit unless a unique index is added
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, allEntries = true)
	*/
	@Override
	public void renameUserInAppreciationDates(ObjectId userId, String replacement) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.USER_INFO);
		
		CheckUtil.nullCheck(userId, replacement);
		
		DBObject query = new BasicDBObject(
				APPRECIATION_DATES + "." + CACHED_USERNAME_OID, userId);

		DBObject appreciationTarget = DBObjectConverter.convertCachedUsername(
				new CachedUsername(userId, replacement));
		
		DBObject set = new BasicDBObject();
		set.put(APPRECIATION_DATES + ".$." + CACHED_USERNAME, appreciationTarget);
	
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(DuplicateKeyException dke) {
			// should never hit unless a unique index is added
			throw new CollisionException();
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_INFO, allEntries = true)
	*/
	@Override
	public void removeUserInAppreciationDates(ObjectId userId) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.POSTING);
		
		CheckUtil.nullCheck(userId);

		DBObject query = new BasicDBObject(
				APPRECIATION_DATES + "." + CACHED_USERNAME_OID, userId);
		DBObject set = new BasicDBObject();
		set.put(APPRECIATION_DATES + ".$." + CACHED_USERNAME_EXISTS, false);
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), 
					false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
