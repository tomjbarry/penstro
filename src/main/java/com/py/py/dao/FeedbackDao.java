package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.FeedbackDaoCustom;
import com.py.py.domain.Feedback;

public interface FeedbackDao extends MongoRepository<Feedback, ObjectId>, FeedbackDaoCustom {

}
