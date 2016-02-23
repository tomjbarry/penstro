package com.py.py.dao.impl;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.DuplicateKeyException;
import com.py.py.dao.constants.CacheNames;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.custom.StatisticDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Statistic;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.enumeration.TIME_OPTION;

public class StatisticDaoImpl implements StatisticDaoCustom {

	private static final String ID = Statistic.ID;
	private static final String LAST_MODIFIED = Statistic.LAST_MODIFIED;
	private static final String DATA = Statistic.DATA;
	//private static final String VALUE = Statistic.VALUE;
	
	private static final String TYPE_AGGREGATION = Statistic.TYPE_AGGREGATION;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	private String getTypeAggregation(AGGREGATION_TYPE type) throws DaoException {
		CheckUtil.nullCheck(type);
		return TYPE_AGGREGATION + type.toString();
	}
	
	private Map<TIME_OPTION, BigInteger> mapAggregationData(Map<String, Object> data) throws DaoException {
		CheckUtil.nullCheck(data);
		Map<TIME_OPTION, BigInteger> result = new HashMap<TIME_OPTION, BigInteger>();
		for(Map.Entry<String, Object> entry : data.entrySet()) {
			if(entry.getValue() != null) {
				try {
					result.put(TIME_OPTION.valueOf(entry.getKey()), new BigInteger((String)entry.getValue()));
				} catch(Exception e) {
					throw new DaoException(e);
				}
			}
		}
		return result;
	}
	
	private DBObject createAggregationData(Map<TIME_OPTION, BigInteger> data) throws DaoException {
		CheckUtil.nullCheck(data);
		DBObject result = new BasicDBObject();
		for(Map.Entry<TIME_OPTION, BigInteger> entry : data.entrySet()) {
			if(entry.getValue() != null) {
				result.put(entry.getKey().toString(), entry.getValue().toString());
			}
		}
		return result;
	}
	
	@Override
	public void updateAggregationTotals(AGGREGATION_TYPE type, Map<TIME_OPTION, BigInteger> totals)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.STATISTIC);
		
		CheckUtil.nullCheck(type);
		
		DBObject query = new BasicDBObject(ID, getTypeAggregation(type));
		
		DBObject set = new BasicDBObject(LAST_MODIFIED, new Date());
		set.put(DATA, createAggregationData(totals));
		
		DBObject update = new BasicDBObject(DaoQueryStrings.SET, set);
		
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
	@Cacheable(value = CacheNames.STATISTIC, key = "#p0?.toString()")
	public Map<TIME_OPTION, BigInteger> getAggregationTotals(AGGREGATION_TYPE type)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.STATISTIC);

		DBObject query = new BasicDBObject(ID, getTypeAggregation(type));
		
		try {
			DBObject obj = collection.findOne(query);
			Statistic stat = mongoTemplate.getConverter().read(Statistic.class, obj);
			if(stat == null) {
				return null;
			}
			return mapAggregationData(stat.getData());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

}
