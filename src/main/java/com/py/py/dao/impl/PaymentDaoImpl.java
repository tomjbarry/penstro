package com.py.py.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.py.py.dao.constants.DaoQueryStrings;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.custom.PaymentDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Payment;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.PAYMENT_MARK;
import com.py.py.domain.enumeration.PAYMENT_STATE;
import com.py.py.domain.enumeration.PAYMENT_TYPE;
import com.py.py.dto.DTO;
import com.py.py.util.PyUtils;

public class PaymentDaoImpl implements PaymentDaoCustom {

	private static final String ID = Payment.ID;
	private static final String STATE = Payment.STATE;
	private static final String TYPE = Payment.TYPE;
	private static final String LAST_MODIFIED = Payment.LAST_MODIFIED;
	private static final String PAYKEY = Payment.PAY_KEY;
	private static final String MARKED = Payment.MARKED;
	
	@Autowired
	protected MongoTemplate mongoTemplate;
	
	/*
	@Override
	public <T> T getRequestInformationAsType(Map<String, Object> requestInformation, 
			Class<T> type) {
		if(requestInformation == null) {
			return null;zd
		}
		
		DBObject dbo = new BasicDBObject();
		dbo.putAll(requestInformation);
		return mongoTemplate.getConverter().read(type, dbo);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> getRequestInformationAsMap(Object requestInformation) {
		try {
			if(requestInformation == null) {
				return null;
			}
			DBObject dbo = new BasicDBObject();
			mongoTemplate.getConverter().write(requestInformation, dbo);
			return dbo.toMap();
		} catch(Exception e) {
			// do nothing, return null
		}
		return null;
	}*/
	
	@Override
	public Payment findPayment(ObjectId id, String payKey) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);
		
		DBObject query = new BasicDBObject();
		if(id != null) {
			query.put(ID, id);
		}
		if(payKey != null) {
			query.put(PAYKEY, payKey);
		}
		
		try {
			DBObject obj = collection.findOne(query);
			Payment payment = mongoTemplate.getConverter().read(Payment.class, obj);
			return payment;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Payment initializePayment(PAYMENT_TYPE type, ObjectId referenceId, 
			ObjectId sourceId, ObjectId targetId, String targetPaymentId, 
			Map<ObjectId, String> beneficiaries, long amount, DTO dto) throws DaoException {
		Payment payment = new Payment();
		payment.setState(PAYMENT_STATE.INITIAL);
		payment.setType(type);
		payment.setReferenceId(referenceId);
		payment.setSourceId(sourceId);
		payment.setTargetId(targetId);
		payment.setTargetPaymentId(targetPaymentId);
		payment.setBeneficiaries(beneficiaries);
		payment.setAmount(amount);
		
		payment.setId(new ObjectId());
		payment.setPayKey(null);
		
		Date created = new Date();
		payment.setCreated(created);
		payment.setLastModified(created);
		
		//payment.setRequestInformation(requestInformation);
		payment.setDto(dto);
		
		try {
			mongoTemplate.insert(payment, CollectionNames.PAYMENT);
		} catch(Exception e) {
			throw new DaoException(e);
		}
		return payment;
	}
	
	@Override
	public boolean verifyPayment(ObjectId id, PAYMENT_STATE state, PAYMENT_TYPE type, 
			ObjectId referenceId, ObjectId sourceId, ObjectId targetId, 
			String targetPaymentId, Map<ObjectId, String> beneficiaries, 
			long amount, DTO dto) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);
		
		try {
			DBObject obj = collection.findOne(new BasicDBObject(ID, id));
			Payment payment = mongoTemplate.getConverter().read(Payment.class, obj);
			if(payment == null) {
				return false;
			}
			if(!PyUtils.objectIdCompare(payment.getSourceId(), sourceId)) {
				return false;
			}
			if(!PyUtils.objectIdCompare(payment.getTargetId(), targetId)) {
				return false;
			}
			if(!PyUtils.stringCompare(payment.getTargetPaymentId(), targetPaymentId)) {
				return false;
			}
			if(payment.getBeneficiaries() != null 
					&& !payment.getBeneficiaries().isEmpty()) {
				if(beneficiaries == null) {
					return false;
				}
				for(Map.Entry<ObjectId, String> entry : 
						payment.getBeneficiaries().entrySet()) {
					boolean match = false;
					for(Map.Entry<ObjectId, String> e : beneficiaries.entrySet()) {
						if(PyUtils.objectIdCompare(entry.getKey(), e.getKey())
								&& PyUtils.stringCompare(entry.getValue(), e.getValue())) {
							match = true;
						}
					}
					if(!match) {
						return false;
					}
				}
			} else {
				if(beneficiaries != null && !beneficiaries.isEmpty()) {
					return false;
				}
			}
			
			if(payment.getReferenceId() == null && referenceId != null) {
				return false;
			}
			if(payment.getReferenceId() != null 
					&& !payment.getReferenceId().equals(referenceId)) {
				return false;
			}
			if(payment.getAmount() != amount) {
				return false;
			}
			// do not compare if its the same, just assume
			if(payment.getDto() == null && dto != null) {
				return false;
			} else if(dto == null && payment.getDto() != null) {
				return false;
			}
			/*
			if(payment.getRequestInformation() != null 
					&& !payment.getRequestInformation().isEmpty()) {
				if(requestInformation == null || requestInformation.isEmpty()) {
					return false;
				}
				// only check keys, do not check values or run into class casting issues
				for(Map.Entry<String, Object> entry : payment.getRequestInformation()
						.entrySet()) {
					if(!requestInformation.containsKey(entry.getKey())) {
						return false;
					}
				}
			} else if(requestInformation != null && !requestInformation.isEmpty()) {
				return false;
			}*/
			return true;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void updatePaymentState(ObjectId id, PAYMENT_STATE state)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);

		CheckUtil.nullCheck(id, state);
		
		Date modified = new Date();
		DBObject set = new BasicDBObject();
		set.put(STATE, state.toString());
		set.put(LAST_MODIFIED, modified);
		
		try {
			collection.update(new BasicDBObject(ID, id),
					new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void markPayment(ObjectId id, PAYMENT_MARK marked, PAYMENT_MARK newMark) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);

		CheckUtil.nullCheck(id, newMark);
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		if(marked != null) {
			query.put(MARKED, marked.toString());
		}
		
		DBObject set = new BasicDBObject();
		set.put(MARKED, newMark.toString());
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void updatePaymentState(ObjectId id, PAYMENT_STATE state, PAYMENT_MARK marked, PAYMENT_MARK newMark) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);

		CheckUtil.nullCheck(id);
		if(state == null && newMark == null) {
			throw new DaoException();
		}
		
		DBObject query = new BasicDBObject();
		query.put(ID, id);
		if(marked != null) {
			query.put(MARKED, marked.toString());
		}

		Date modified = new Date();
		DBObject set = new BasicDBObject();
		if(state != null) {
			set.put(STATE, state.toString());
		}
		if(newMark != null) {
			set.put(MARKED, newMark.toString());
		}
		set.put(LAST_MODIFIED, modified);
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public boolean checkPaymentState(ObjectId id, PAYMENT_STATE state)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);
		
		CheckUtil.nullCheck(id, state);
		
		try {
			DBObject obj = collection.findOne(new BasicDBObject(ID, id));
			Payment payment = mongoTemplate.getConverter().read(Payment.class, obj);
			if(payment == null) {
				return false;
			}
			if(payment.getState() != state) {
				return false;
			}
			return true;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void setPayKey(ObjectId id, PAYMENT_STATE state, String payKey) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);

		CheckUtil.nullCheck(id, payKey);
		
		Date modified = new Date();
		DBObject set = new BasicDBObject();
		set.put(PAYKEY, payKey);
		set.put(STATE, state.toString());
		set.put(LAST_MODIFIED, modified);
		
		try {
			collection.update(new BasicDBObject(ID, id),
					new BasicDBObject(DaoQueryStrings.SET, set));
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public boolean checkPayKey(ObjectId id, PAYMENT_STATE state, String payKey) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);
		
		CheckUtil.nullCheck(id, payKey, state);
		
		try {
			DBObject obj = collection.findOne(new BasicDBObject(ID, id));
			Payment payment = mongoTemplate.getConverter().read(Payment.class, obj);
			if(payment == null) {
				return false;
			}
			if(!PyUtils.stringCompare(payment.getPayKey(), payKey)) {
				return false;
			}
			if(payment.getState() != state) {
				return false;
			}
			
			return true;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<Payment> getPayments(List<PAYMENT_TYPE> types, List<PAYMENT_STATE> states, 
			Date olderThanModified, PAYMENT_MARK marked, Pageable pageable)
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);
		
		DBObject query = new BasicDBObject();
		if(marked != null) {
			query.put(MARKED, marked.toString());
		}
		if(states != null && !states.isEmpty()) {
			query.put(STATE, new BasicDBObject(DaoQueryStrings.IN, 
					PyUtils.stringifiedList(states)));
		}
		if(olderThanModified != null) {
			query.put(LAST_MODIFIED, 
					new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanModified));
		}
		if(types != null && !types.isEmpty()) {
			query.put(TYPE, new BasicDBObject(DaoQueryStrings.IN, 
					PyUtils.stringifiedList(types)));
		}
		
		try {
			DBCursor cursor = collection.find(query)
						.sort(new BasicDBObject(LAST_MODIFIED, DaoValues.SORT_DESCENDING))
						.skip(pageable.getOffset())
						.limit(pageable.getPageSize());
			
			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Payment> payments = new ArrayList<Payment>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Payment payment = mongoTemplate.getConverter().read(Payment.class,  obj);
				payments.add(payment);
			}
			
			return new PageImpl<Payment>(payments, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void markPayments(List<PAYMENT_TYPE> types, List<PAYMENT_STATE> states, 
			Date olderThanModified, PAYMENT_MARK marked, PAYMENT_MARK newMark)
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);
		
		DBObject query = new BasicDBObject();
		if(marked != null) {
			query.put(MARKED, marked.toString());
		}
		if(states != null && !states.isEmpty()) {
			query.put(STATE, new BasicDBObject(DaoQueryStrings.IN, 
					PyUtils.stringifiedList(states)));
		}
		if(olderThanModified != null) {
			query.put(LAST_MODIFIED, 
					new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanModified));
		}
		if(types != null && !types.isEmpty()) {
			query.put(TYPE, new BasicDBObject(DaoQueryStrings.IN, 
					PyUtils.stringifiedList(types)));
		}
		
		DBObject set = new BasicDBObject(MARKED, newMark.toString());
		
		try {
			collection.update(query, new BasicDBObject(DaoQueryStrings.SET, set), false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	/*
	@Override
	public Page<Payment> getSpecificPayments(List<ObjectId> ids, Pageable pageable)
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);
		
		CheckUtil.nullCheck(ids);
		
		DBObject query = new BasicDBObject();
		query.put(ID, new BasicDBObject(DaoQueryStrings.IN, ids));
		
		try {
			DBCursor cursor = collection.find(query)
						.skip(pageable.getOffset())
						.limit(pageable.getPageSize());
			
			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Payment> payments = new ArrayList<Payment>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Payment payment = mongoTemplate.getConverter().read(Payment.class,  obj);
				payments.add(payment);
			}
			
			return new PageImpl<Payment>(payments, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	*/
	@Override
	public void remove(List<PAYMENT_STATE> states, Date olderThanModified)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.PAYMENT);
		
		DBObject query = new BasicDBObject();
		if(states != null && !states.isEmpty()) {
			query.put(STATE, new BasicDBObject(DaoQueryStrings.IN, 
					PyUtils.stringifiedList(states)));
		}
		if(olderThanModified != null) {
			query.put(LAST_MODIFIED, 
					new BasicDBObject(DaoQueryStrings.LESS_THAN, olderThanModified));
		}
		
		try {
			collection.remove(query);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}

}
