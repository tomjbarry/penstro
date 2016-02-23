package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.CommentDaoCustom;
import com.py.py.domain.Comment;

public interface CommentDao extends MongoRepository<Comment, ObjectId>, CommentDaoCustom {

}
