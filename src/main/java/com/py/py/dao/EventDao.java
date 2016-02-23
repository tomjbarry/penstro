package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.EventDaoCustom;
import com.py.py.domain.Event;

public interface EventDao extends MongoRepository<Event, ObjectId>, EventDaoCustom {

}
