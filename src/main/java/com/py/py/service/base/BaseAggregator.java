package com.py.py.service.base;

import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.AggregationOutput;
import com.mongodb.DBObject;
import com.py.py.dao.AggregationDao;
import com.py.py.dao.StatisticDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.DefaultsFactory;

public class BaseAggregator {
	
	@Autowired
	protected AggregationDao aggregationDao;
	
	@Autowired
	protected StatisticDao statisticDao;
	
	@Autowired
	protected DefaultsFactory defaultsFactory;

	/*
	protected ConcurrentMap<TIME_OPTION, BigInteger> aggregateTotals = 
			constructAggregateMap();
	
	private ConcurrentMap<TIME_OPTION, BigInteger> constructAggregateMap() {
		ConcurrentMap<TIME_OPTION, BigInteger> m = 
				new ConcurrentHashMap<TIME_OPTION, BigInteger>();
		m.putIfAbsent(TIME_OPTION.HOUR, BigInteger.valueOf(0L));
		m.putIfAbsent(TIME_OPTION.DAY, BigInteger.valueOf(0L));
		m.putIfAbsent(TIME_OPTION.MONTH, BigInteger.valueOf(0L));
		m.putIfAbsent(TIME_OPTION.YEAR, BigInteger.valueOf(0L));
		m.putIfAbsent(TIME_OPTION.ALLTIME, BigInteger.valueOf(0L));
		return m;
	}
	*/
	
	public void updateAggregates(String id, AGGREGATION_TYPE type, long amount)
			throws ServiceException {
		ArgCheck.nullCheck(id);
		
		Date now = new Date();
		
		try {
			aggregationDao.add(type, id, amount, now, 
					defaultsFactory.getSegmentHour(), TIME_OPTION.HOUR);
	
			aggregationDao.add(type, id, amount, now, 
					defaultsFactory.getSegmentDay(), TIME_OPTION.DAY);
	
			aggregationDao.add(type, id, amount, now, 
					defaultsFactory.getSegmentMonth(), TIME_OPTION.MONTH);
	
			aggregationDao.add(type, id, amount, now, 
					defaultsFactory.getSegmentYear(), TIME_OPTION.YEAR);
	
			/* unnecessary
			aggregationDao.add(type, id, amount, now, 
					defaultsFactory.getSegmentAlltime(), TIME_OPTION.ALLTIME);
			*/
		} catch(DaoException de) {
			// should be upserting, so cancel everything in this case
			throw new ServiceException(de);
		}
	}
	
	public Iterable<DBObject> aggregate(AGGREGATION_TYPE type, TIME_OPTION segment)
			throws ServiceException {
		Long interval = null;
		if(segment == TIME_OPTION.ALLTIME) {
			interval = null;
		} else if(segment == TIME_OPTION.HOUR) {
			interval = defaultsFactory.getSegmentDay();
		} else if(segment == TIME_OPTION.DAY) {
			interval = defaultsFactory.getSegmentMonth();
		} else if(segment == TIME_OPTION.MONTH) {
			interval = defaultsFactory.getSegmentYear();
		} else if(segment == TIME_OPTION.YEAR) {
			interval = defaultsFactory.getSegmentAlltime();
		}
		Date now = new Date();
		
		try {
			AggregationOutput output = aggregationDao.getAggregation(type, now, segment, 
					interval);
			if(output == null) {
				throw new ServiceException();
			}
			return output.results();
		} catch(DaoException de) {
			throw new ServiceException(de);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	private Iterable<DBObject> aggregateTotals(AGGREGATION_TYPE type) 
			throws ServiceException {
		long hourInterval = defaultsFactory.getSegmentDay();
		long dayInterval = defaultsFactory.getSegmentMonth();
		long monthInterval = defaultsFactory.getSegmentYear();
		long yearInterval = defaultsFactory.getSegmentAlltime();
		Date now = new Date();
		
		try {
			AggregationOutput output = aggregationDao.getAggregationTotals(type, now,
					hourInterval, dayInterval, monthInterval, yearInterval);
			if(output == null) {
				throw new ServiceException();
			}
			return output.results();
		} catch(DaoException de) {
			throw new ServiceException(de);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	private Iterable<DBObject> aggregateTotalsAlltime(AGGREGATION_TYPE type) 
			throws ServiceException {
		
		try {
			AggregationOutput output = aggregationDao.getAggregationTotalsAlltime(type);
			if(output == null) {
				throw new ServiceException();
			}
			return output.results();
		} catch(DaoException de) {
			throw new ServiceException(de);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	public void updateAggregateTotals(AGGREGATION_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(type);
		Iterable<DBObject> results = aggregateTotals(type);
		Iterable<DBObject> alltimeResults = aggregateTotalsAlltime(type);
		ArgCheck.nullCheck(results, alltimeResults);
		Map<TIME_OPTION, BigInteger> totals = new HashMap<TIME_OPTION, BigInteger>();
		
		for(DBObject obj : results) {
			@SuppressWarnings("rawtypes")
			Map map = obj.toMap();
			TIME_OPTION segment = TIME_OPTION.valueOf((String)map.get("_id"));
			BigInteger total = BigInteger.valueOf((Long)map.get("total"));
			totals.put(segment, total);
			//updateAggregateTotals(segment, total);
		}
		
		for(DBObject obj : alltimeResults) {
			@SuppressWarnings("rawtypes")
			Map map = obj.toMap();
			BigInteger total = BigInteger.valueOf((Long)map.get("total"));
			totals.put(TIME_OPTION.ALLTIME, total);
			//updateAggregateTotals(TIME_OPTION.ALLTIME, total);
		}
		
		try {
			statisticDao.updateAggregationTotals(type, totals);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	public Map<TIME_OPTION, BigInteger> getAggregateTotals(AGGREGATION_TYPE type) throws ServiceException {
		ArgCheck.nullCheck(type);
		Map<TIME_OPTION, BigInteger> totals;
		Map<TIME_OPTION, BigInteger> result = new HashMap<TIME_OPTION, BigInteger>();
		
		try {
			totals = statisticDao.getAggregationTotals(type);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		if(totals == null) {
			throw new NotFoundException(type.toString());
		}
		
		BigInteger i;
		for(TIME_OPTION segment : TIME_OPTION.values()) {
			i = totals.get(segment);
			if(i == null) {
				i = new BigInteger("0");
			}
			result.put(segment, i);
		}
		return result;
	}
	
	public void removeExpired(AGGREGATION_TYPE type) throws ServiceException {
		Date now = new Date();
		try {
			aggregationDao.removeAllExpired(type, now, defaultsFactory.getSegmentHour(), 
					defaultsFactory.getSegmentDay(), defaultsFactory.getSegmentMonth(), 
					defaultsFactory.getSegmentYear());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
}
