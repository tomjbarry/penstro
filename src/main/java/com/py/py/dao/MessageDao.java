package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.MessageDaoCustom;
import com.py.py.domain.Message;

public interface MessageDao extends MongoRepository<Message, ObjectId>, MessageDaoCustom {

}
