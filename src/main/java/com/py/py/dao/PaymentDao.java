package com.py.py.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.py.py.dao.custom.PaymentDaoCustom;
import com.py.py.domain.Payment;

public interface PaymentDao extends MongoRepository<Payment, ObjectId>, PaymentDaoCustom {

}
