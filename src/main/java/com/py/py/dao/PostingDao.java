package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.PostingDaoCustom;
import com.py.py.domain.Posting;

public interface PostingDao extends MongoRepository<Posting, ObjectId>, PostingDaoCustom {

}
