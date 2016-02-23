package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.WalletDaoCustom;
import com.py.py.domain.subdomain.Wallet;

public interface WalletDao extends MongoRepository<Wallet, ObjectId>, WalletDaoCustom {

}
