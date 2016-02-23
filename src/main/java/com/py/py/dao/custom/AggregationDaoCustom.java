package com.py.py.dao.custom;

import java.util.Date;

import com.mongodb.AggregationOutput;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.enumeration.TIME_OPTION;

public interface AggregationDaoCustom {

	void add(AGGREGATION_TYPE type, String rid, long amount, Date now,
			long interval, TIME_OPTION segment) throws DaoException;

	void removeAllExpired(AGGREGATION_TYPE type, Date now, long hourInterval,
			long dayInterval, long monthInterval, long yearInterval) throws DaoException;

	AggregationOutput getAggregation(AGGREGATION_TYPE type, Date now,
			TIME_OPTION segment, Long aggregationInterval)
			throws DaoException;

	AggregationOutput getAggregationTotals(AGGREGATION_TYPE type, Date now,
			long hourInterval, long dayInterval, long monthInterval,
			long yearInterval) throws DaoException;

	AggregationOutput getAggregationTotalsAlltime(AGGREGATION_TYPE type)
			throws DaoException;


}
