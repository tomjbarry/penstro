package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.SubscriptionDaoCustom;
import com.py.py.domain.Subscription;

public interface SubscriptionDao extends MongoRepository<Subscription, ObjectId>, SubscriptionDaoCustom {

}
