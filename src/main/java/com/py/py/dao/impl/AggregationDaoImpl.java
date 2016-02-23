package com.py.py.dao.impl;

import java.util.Arrays;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.AggregationOutput;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.mongodb.QueryBuilder;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.custom.AggregationDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.ValueAggregation;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.subdomain.AggregationInfo;
import com.py.py.enumeration.TIME_OPTION;

public class AggregationDaoImpl implements AggregationDaoCustom {

	private static final String ID = ValueAggregation.ID;
	private static final String TYPE = ID + "." + AggregationInfo.TYPE;
	private static final String REFERENCE_ID = ID + "." + AggregationInfo.REFERENCE_ID;
	private static final String SEGMENT = ID + "." + AggregationInfo.SEGMENT;
	private static final String STARTTIME = ID + "." + AggregationInfo.START_TIME;
	private static final String VALUE = ValueAggregation.VALUE;
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	private DBObject createTypeSegment(AGGREGATION_TYPE type, TIME_OPTION segment) {
		DBObject obj = new BasicDBObject();
		if(type != null) {
			obj.put(TYPE, type.toString());
		}
		if(segment != null) {
			obj.put(SEGMENT, segment.toString());
		}
		return obj;
	}
	
	private Date calculateStartTime(Date now, long interval) {
		long extra = now.getTime() % interval;
		return new Date(now.getTime() - extra);
	}
	
	/*
	private Date calculateEndTime(Date now, long interval) {
		Date startTime = calculateStartTime(now, interval);
		return new Date(startTime.getTime() + interval);
	}*/
	
	@Override
	public void add(AGGREGATION_TYPE type, String rid, long amount, Date now, 
			long interval, TIME_OPTION segment) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.VALUE_AGGREGATION);
		
		CheckUtil.nullCheck(rid, now);
		
		Date startTime = calculateStartTime(now, interval);
		//Date endTime = calculateEndTime(now, interval);
		
		DBObject query = createTypeSegment(type, segment);
		query.put(STARTTIME, startTime);
		query.put(REFERENCE_ID, rid);
		
		DBObject increment = new BasicDBObject();
		increment.put(VALUE, amount);
		
		DBObject update = new BasicDBObject();
		update.put(DaoQueryStrings.INCREMENT, increment);

		try {
			collection.update(query, update, true, false);
		} catch(DuplicateKeyException dke) {
			try {
				DBObject insert = new BasicDBObject();
				insert.putAll(query);
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
	/*
	@Override
	public List<ValueAggregation> getForId(AGGREGATION_TYPE type, 
			String rid, Date now, long hourInterval, long dayInterval, 
			long monthInterval, long yearInterval, long alltimeInterval) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.VALUE_AGGREGATION);
		
		CheckUtil.nullCheck(rid, now);
		
		QueryBuilder builder = QueryBuilder.start();
		
		Date hourTime = new Date(now.getTime() - dayInterval - hourInterval);
		Date dayTime = new Date(now.getTime() - monthInterval - dayInterval);
		Date monthTime = new Date(now.getTime() - yearInterval - monthInterval);
		Date yearTime = new Date(now.getTime() - alltimeInterval - yearInterval);
		
		DBObject hourMap = createTypeSegment(type, TIME_OPTION.HOUR);
		hourMap.put(REFERENCE_ID, rid);
		hourMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, 
				hourTime));
		
		DBObject dayMap = createTypeSegment(type, TIME_OPTION.DAY);
		dayMap.put(REFERENCE_ID, rid);
		dayMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, 
				dayTime));
		
		DBObject monthMap = createTypeSegment(type, TIME_OPTION.MONTH);
		monthMap.put(REFERENCE_ID, rid);
		monthMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, 
				monthTime));
		
		DBObject yearMap = createTypeSegment(type, TIME_OPTION.YEAR);
		yearMap.put(REFERENCE_ID, rid);
		yearMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, 
				yearTime));
		
		builder.or(hourMap, dayMap, monthMap, yearMap);
		
		try {
			DBCursor cursor = collection.find(builder.get())
					.sort(new BasicDBObject(STARTTIME, DaoValues.SORT_DESCENDING))
					.limit(0);

			if(cursor == null) {
				throw new DaoException();
			}
			
			List<ValueAggregation> aggregations = 
					new ArrayList<ValueAggregation>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				ValueAggregation a = mongoTemplate.getConverter().read(
						ValueAggregation.class,  obj);
				aggregations.add(a);
			}
			
			return aggregations;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	*/
	/*
	@Override
	public void removeForId(AGGREGATION_TYPE type, String rid, Date now, 
			long hourSegment, long daySegment, long monthSegment, long yearSegment, 
			long alltimeSegment) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.VALUE_AGGREGATION);
		
		CheckUtil.nullCheck(rid, now);
		
		QueryBuilder builder = QueryBuilder.start();
		
		Date hourTime = new Date(now.getTime() - daySegment - hourSegment);
		Date dayTime = new Date(now.getTime() - monthSegment - daySegment);
		Date monthTime = new Date(now.getTime() - yearSegment - monthSegment);
		Date yearTime = new Date(now.getTime() - alltimeSegment - yearSegment);
		
		DBObject hourMap = createTypeSegment(type, TIME_OPTION.HOUR);
		hourMap.put(REFERENCE_ID, rid);
		hourMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, 
				hourTime));
		
		DBObject dayMap = createTypeSegment(type, TIME_OPTION.DAY);
		dayMap.put(REFERENCE_ID, rid);
		dayMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, 
				dayTime));
		
		DBObject monthMap = createTypeSegment(type, TIME_OPTION.MONTH);
		monthMap.put(REFERENCE_ID, rid);
		monthMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, 
				monthTime));
		
		DBObject yearMap = createTypeSegment(type, TIME_OPTION.YEAR);
		yearMap.put(REFERENCE_ID, rid);
		yearMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, 
				yearTime));
		
		builder.or(hourMap, dayMap, monthMap, yearMap);
		
		try {
			collection.remove(builder.get());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	*/
	@Override
	public AggregationOutput getAggregation(AGGREGATION_TYPE type, Date now, 
			TIME_OPTION segment, Long aggregationInterval) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.VALUE_AGGREGATION);

		CheckUtil.nullCheck(now);
		
		DBObject match = createTypeSegment(type, segment);
		if(aggregationInterval != null) {
			Date then = new Date(now.getTime() - aggregationInterval - aggregationInterval);
			match.put(STARTTIME, new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, then));
		}
		
		DBObject group = new BasicDBObject();
		group.put("_id", "$" + REFERENCE_ID);
		group.put("total", new BasicDBObject(DaoQueryStrings.SUM, "$" + VALUE));
		
		try {
			AggregationOutput output = collection.aggregate(Arrays.asList(new BasicDBObject(DaoQueryStrings.MATCH, match),
					new BasicDBObject(DaoQueryStrings.GROUP, group)));
			return output;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public AggregationOutput getAggregationTotals(AGGREGATION_TYPE type, Date now, 
			long hourInterval, long dayInterval, long monthInterval, long yearInterval)
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.VALUE_AGGREGATION);

		CheckUtil.nullCheck(now);
		
		QueryBuilder builder = QueryBuilder.start();
		
		DBObject hourMatch = createTypeSegment(type, TIME_OPTION.HOUR);
		Date then1 = new Date(now.getTime() - hourInterval - hourInterval);
		hourMatch.put(STARTTIME, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, then1));
		
		DBObject dayMatch = createTypeSegment(type, TIME_OPTION.DAY);
		Date then2 = new Date(now.getTime() - dayInterval - dayInterval);
		dayMatch.put(STARTTIME, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, then2));
		
		DBObject monthMatch = createTypeSegment(type, TIME_OPTION.MONTH);
		Date then3 = new Date(now.getTime() - monthInterval - monthInterval);
		monthMatch.put(STARTTIME, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, then3));
		
		DBObject yearMatch = createTypeSegment(type, TIME_OPTION.YEAR);
		Date then4 = new Date(now.getTime() - yearInterval - yearInterval);
		yearMatch.put(STARTTIME, 
				new BasicDBObject(DaoQueryStrings.GREATER_THAN_EQUAL, then4));

		builder.or(hourMatch, dayMatch, monthMatch, yearMatch);
		
		DBObject group = new BasicDBObject();
		group.put("_id", "$" + SEGMENT);
		group.put("total", new BasicDBObject(DaoQueryStrings.SUM, "$" + VALUE));
		
		try {
			AggregationOutput output = collection.aggregate(Arrays.asList(
					new BasicDBObject(DaoQueryStrings.MATCH, builder.get()), 
					new BasicDBObject(DaoQueryStrings.GROUP, group)));
			return output;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

	@Override
	public AggregationOutput getAggregationTotalsAlltime(AGGREGATION_TYPE type) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.VALUE_AGGREGATION);
		
		DBObject alltimeMatch = createTypeSegment(type, TIME_OPTION.YEAR);
		
		DBObject group = new BasicDBObject();
		group.put("_id", "$" + SEGMENT);
		group.put("total", new BasicDBObject(DaoQueryStrings.SUM, "$" + VALUE));
		
		try {
			AggregationOutput output = collection.aggregate(Arrays.asList(
					new BasicDBObject(DaoQueryStrings.MATCH, alltimeMatch), 
					new BasicDBObject(DaoQueryStrings.GROUP, group)));
			return output;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	// do not remove year segments, these are aggregated for alltime
	@Override
	public void removeAllExpired(AGGREGATION_TYPE type, Date now, long hourInterval, 
			long dayInterval, long monthInterval, long yearInterval)
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(
				CollectionNames.VALUE_AGGREGATION);
		
		CheckUtil.nullCheck(now);

		QueryBuilder builder = QueryBuilder.start();

		Date hourTime = new Date(now.getTime() - dayInterval - hourInterval - ServiceValues.EXPIRY_GRACE_PERIOD_HOUR);
		Date dayTime = new Date(now.getTime() - monthInterval - dayInterval - ServiceValues.EXPIRY_GRACE_PERIOD_DAY);
		Date monthTime = new Date(now.getTime() - yearInterval - monthInterval - ServiceValues.EXPIRY_GRACE_PERIOD_MONTH);
		//Date yearTime = new Date(now.getTime() - alltimeSegment - yearSegment - ServiceValues.EXPIRY_GRACE_PERIOD_YEAR);
		
		DBObject hourMap = createTypeSegment(type, TIME_OPTION.HOUR);
		hourMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, 
				hourTime));
		
		DBObject dayMap = createTypeSegment(type, TIME_OPTION.DAY);
		dayMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, 
				dayTime));
		
		DBObject monthMap = createTypeSegment(type, TIME_OPTION.MONTH);
		monthMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, 
				monthTime));
		
		/*
		DBObject yearMap = createTypeSegment(type, TIME_OPTION.YEAR);
		yearMap.put(STARTTIME, new BasicDBObject(DaoQueryStrings.LESS_THAN, 
				yearTime));
		*/
		//builder.or(hourMap, dayMap, monthMap, yearMap);
		builder.or(hourMap, dayMap, monthMap);
		try {
			collection.remove(builder.get());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
}
