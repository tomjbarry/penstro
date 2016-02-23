package com.py.py.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.AggregationDaoCustom;
import com.py.py.domain.ValueAggregation;
import com.py.py.domain.subdomain.AggregationInfo;

public interface AggregationDao extends 
	MongoRepository<ValueAggregation, AggregationInfo>, 
	AggregationDaoCustom {

}
