package com.py.py.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.TagDaoCustom;
import com.py.py.domain.Tag;
import com.py.py.domain.subdomain.TagId;

public interface TagDao extends MongoRepository<Tag, TagId>, TagDaoCustom {

}
