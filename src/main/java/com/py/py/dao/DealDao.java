package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.DealDaoCustom;
import com.py.py.domain.Deal;

public interface DealDao extends MongoRepository<Deal, ObjectId>, DealDaoCustom {

}
