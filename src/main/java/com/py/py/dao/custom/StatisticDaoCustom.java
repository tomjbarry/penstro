package com.py.py.dao.custom;

import java.math.BigInteger;
import java.util.Map;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.enumeration.TIME_OPTION;

public interface StatisticDaoCustom {
	
	void updateAggregationTotals(AGGREGATION_TYPE type, Map<TIME_OPTION, BigInteger> totals) throws DaoException;
	
	Map<TIME_OPTION, BigInteger> getAggregationTotals(AGGREGATION_TYPE type) throws DaoException;

}
