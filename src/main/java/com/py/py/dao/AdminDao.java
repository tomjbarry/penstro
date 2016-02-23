package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.AdminDaoCustom;
import com.py.py.domain.AdminAction;

public interface AdminDao extends MongoRepository<AdminAction, ObjectId>, AdminDaoCustom {

}
