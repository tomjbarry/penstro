package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.UserDaoCustom;
import com.py.py.domain.User;

public interface UserDao extends MongoRepository<User, ObjectId>, UserDaoCustom {

}
