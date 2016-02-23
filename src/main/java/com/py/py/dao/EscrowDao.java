package com.py.py.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.EscrowDaoCustom;
import com.py.py.domain.Escrow;
import com.py.py.domain.subdomain.EscrowSourceTarget;

public interface EscrowDao extends MongoRepository<Escrow, EscrowSourceTarget>, EscrowDaoCustom {

}
