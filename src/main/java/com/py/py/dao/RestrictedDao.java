package com.py.py.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.RestrictedDaoCustom;
import com.py.py.domain.Restricted;
import com.py.py.domain.subdomain.RestrictedWord;

public interface RestrictedDao extends MongoRepository<Restricted, RestrictedWord>, 
	RestrictedDaoCustom {

}
