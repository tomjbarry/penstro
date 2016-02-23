package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.ReferenceDaoCustom;
import com.py.py.domain.subdomain.Reference;

public interface ReferenceDao extends MongoRepository<Reference, ObjectId>, ReferenceDaoCustom {

}
