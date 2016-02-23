package com.py.py.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.CorrespondenceDaoCustom;
import com.py.py.domain.Correspondence;
import com.py.py.domain.subdomain.BinaryUserId;

public interface CorrespondenceDao extends MongoRepository<Correspondence, BinaryUserId>, CorrespondenceDaoCustom {

}
