package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.UserInfoDaoCustom;
import com.py.py.domain.UserInfo;

public interface UserInfoDao extends MongoRepository<UserInfo, ObjectId>, UserInfoDaoCustom {

}
