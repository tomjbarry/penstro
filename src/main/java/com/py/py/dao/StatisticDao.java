package com.py.py.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.StatisticDaoCustom;
import com.py.py.domain.Statistic;

public interface StatisticDao extends MongoRepository<Statistic, String>, StatisticDaoCustom{

}
