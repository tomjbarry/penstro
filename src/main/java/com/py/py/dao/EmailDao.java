package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.EmailDaoCustom;
import com.py.py.domain.EmailTask;

public interface EmailDao extends MongoRepository<EmailTask, ObjectId>, EmailDaoCustom {

}
