package com.py.py.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.FlagDataDaoCustom;
import com.py.py.domain.FlagData;
import com.py.py.domain.subdomain.FlagInfo;

public interface FlagDataDao extends MongoRepository<FlagData, FlagInfo>,
		FlagDataDaoCustom {

}
