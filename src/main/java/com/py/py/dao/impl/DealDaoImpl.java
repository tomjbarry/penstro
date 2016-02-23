package com.py.py.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
import com.py.py.dao.custom.DealDaoCustom;
import com.py.py.dao.exception.DaoException;
import com.py.py.dao.util.CheckUtil;
import com.py.py.domain.Deal;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.DEAL_STATE;
import com.py.py.domain.enumeration.DEAL_TYPE;
import com.py.py.domain.subdomain.FinanceDescription;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.util.PyUtils;

public class DealDaoImpl implements DealDaoCustom {

	private static final String ID = Deal.ID;
	private static final String STATE = Deal.STATE;
	private static final String LAST_MODIFIED = Deal.LAST_MODIFIED;
	private static final String PAYMENT_ID = Deal.PAYMENT_ID;
	
	private static final String SOURCE_ADDED = Deal.SOURCE_ADDED;
	private static final String TARGETS_ADDED = Deal.TARGETS_ADDED;
	private static final String REFERENCE_ADDED = Deal.REFERENCE_ADDED;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	private boolean compareFinanceDescription(FinanceDescription a, FinanceDescription b) {
		if(a == null && b == null) {
			return true;
		}
		if(a == null || b == null) {
			return false;
		}
		EscrowSourceTarget ae = a.getEscrow();
		EscrowSourceTarget be = b.getEscrow();
		if(!(ae == null && be == null)) {
			if(ae == null) {
				return false;
			}
			if(be == null) {
				return false;
			}
			if(!PyUtils.stringCompare(ae.getSource(), be.getSource()) 
					|| !PyUtils.stringCompare(ae.getTarget(), be.getTarget())) {
				return false;
			}
		}
		if(a.getAmount() == b.getAmount() &&
				((a.getUserId() == null && b.getUserId() == null) 
						|| (a.getUserId() != null && b.getUserId() != null
								&& a.getUserId().equals(b.getUserId()))) &&
				PyUtils.stringCompare(a.getCurrency(), b.getCurrency())) {
			return true;
		}
		return false;
	}
	
	@Override
	public Deal initializeDeal(DEAL_TYPE type, FinanceDescription source, 
			List<FinanceDescription> targets, ObjectId reference, 
			boolean createReferenceCost, String referenceCollection, 
			Long primaryAmount, Long secondaryAmount, ObjectId paymentId)
					throws DaoException {
		
		Date created = new Date();
		Deal deal = new Deal();
		deal.setSource(source);
		deal.setTargets(targets);
		deal.setReference(reference);
		deal.setCreateReferenceCost(createReferenceCost);
		deal.setReferenceCollection(referenceCollection);
		deal.setCreated(created);
		deal.setLastModified(created);
		deal.setType(type);
		deal.setState(DEAL_STATE.INITIAL);
		deal.setPrimaryAmount(primaryAmount);
		deal.setSecondaryAmount(secondaryAmount);
		deal.setPaymentId(paymentId);
		try {
			mongoTemplate.insert(deal, CollectionNames.DEAL);
		} catch(Exception e) {
			throw new DaoException(e);
		}
		return deal;
	}
	
	@Override
	public void updateDealState(ObjectId id, DEAL_STATE state, Boolean sourceAdded,
			Boolean targetsAdded, Boolean referenceAdded) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.DEAL);

		CheckUtil.nullCheck(id);
		
		Date modified = new Date();
		DBObject set = new BasicDBObject();
		set.put(LAST_MODIFIED, modified);
		if(state != null) {
			set.put(STATE, state.toString());
		}
		if(sourceAdded != null) {
			set.put(SOURCE_ADDED, sourceAdded);
		}
		if(targetsAdded != null) {
			set.put(TARGETS_ADDED, targetsAdded);
		}
		if(referenceAdded != null) {
			set.put(REFERENCE_ADDED, referenceAdded);
		}
		
		try {
			// questionable move here. ideally the enum is automatically serialized
			collection.update(new BasicDBObject(ID, id),
					new BasicDBObject(DaoQueryStrings.SET, set), false, true);
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public boolean verifyDeal(ObjectId id, DEAL_TYPE type, FinanceDescription source, 
			List<FinanceDescription> targets, ObjectId reference, 
			boolean createReferenceCost, String referenceCollection, 
			Long primaryAmount, Long secondaryAmount, ObjectId paymentId)
					throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.DEAL);
		
		try {
			DBObject obj = collection.findOne(new BasicDBObject(ID, id));
			Deal deal = mongoTemplate.getConverter().read(Deal.class, obj);
			if(deal == null) {
				return false;
			}
			if(!compareFinanceDescription(deal.getSource(), source)) {
				return false;
			}
			if(deal.getTargets() != null && !deal.getTargets().isEmpty()) {
				if(targets == null) {
					return false;
				}
				for(FinanceDescription fd : deal.getTargets()) {
					boolean match = false;
					for(FinanceDescription t : targets) {
						if(compareFinanceDescription(fd, t)) {
							match = true;
						}
					}
					if(!match) {
						return false;
					}
				}
			} else {
				if(targets != null && !targets.isEmpty()) {
					return false;
				}
			}
			
			if(deal.getReference() == null && reference != null) {
				return false;
			}
			if(deal.getReference() != null && !deal.getReference().equals(reference)) {
				return false;
			}
			if(!PyUtils.stringCompare(deal.getReferenceCollection(), referenceCollection)) {
				return false;
			}
			if(deal.isCreateReferenceCost() != createReferenceCost) {
				return false;
			}
			if(!PyUtils.longCompare(deal.getPrimaryAmount(), primaryAmount)) {
				return false;
			}
			if(!PyUtils.longCompare(deal.getSecondaryAmount(),secondaryAmount)) {
				return false;
			}
			if(!PyUtils.objectIdCompare(deal.getPaymentId(), paymentId)) {
				return false;
			}
			return true;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public boolean checkDealState(ObjectId id, DEAL_STATE state) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.DEAL);
		
		CheckUtil.nullCheck(id);
		
		try {
			DBObject obj = collection.findOne(new BasicDBObject(ID, id));
			Deal deal = mongoTemplate.getConverter().read(Deal.class, obj);
			if(deal == null) {
				throw new DaoException();
			}
			if(deal.getState() == state) {
				return true;
			}
			return false;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Deal getDeal(ObjectId id, DEAL_STATE state, ObjectId paymentId) 
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.DEAL);
		
		DBObject query = new BasicDBObject();
		if(id != null) {
			query.put(ID, id);
		}
		if(paymentId != null) {
			query.put(PAYMENT_ID, paymentId);
		}
		if(state != null) {
			query.put(STATE, state.toString());
		}
		
		try {
			DBObject obj = collection.findOne(query);
			Deal deal = mongoTemplate.getConverter().read(Deal.class, obj);
			return deal;
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public Page<Deal> getDeals(List<DEAL_STATE> states, Date olderThanModified, 
			Pageable pageable) throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.DEAL);
		
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
			DBCursor cursor = collection.find(query)
						.sort(new BasicDBObject(LAST_MODIFIED, DaoValues.SORT_DESCENDING))
						.skip(pageable.getOffset())
						.limit(pageable.getPageSize());
			
			if(cursor == null) {
				throw new DaoException();
			}
			
			List<Deal> deals = new ArrayList<Deal>();
			
			while(cursor.hasNext()) {
				DBObject obj = cursor.next();
				Deal deal = mongoTemplate.getConverter().read(Deal.class,  obj);
				deals.add(deal);
			}
			
			return new PageImpl<Deal>(deals, pageable, cursor.count());
		} catch(Exception e) {
			throw new DaoException(e);
		}
	}
	
	@Override
	public void remove(List<DEAL_STATE> states, Date olderThanModified)
			throws DaoException {
		DBCollection collection = mongoTemplate.getCollection(CollectionNames.DEAL);
		
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
