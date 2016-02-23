package com.py.py.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.CurrencyNames;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.DealDao;
import com.py.py.dao.ReferenceDao;
import com.py.py.dao.WalletDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Deal;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.DEAL_STATE;
import com.py.py.domain.enumeration.DEAL_TYPE;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.domain.subdomain.FinanceDescription;
import com.py.py.dto.in.PurchaseCurrencyDTO;
import com.py.py.service.FinanceService;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.BalanceException;
import com.py.py.service.exception.FinanceException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;
import com.py.py.validation.util.Validation;

public class FinanceServiceImpl implements FinanceService {

	protected static final PyLogger logger = PyLogger.getLogger(FinanceServiceImpl.class);
	
	@Autowired
	private DealDao dealDao;
	
	@Autowired
	private WalletDao walletDao;
	
	@Autowired
	private ReferenceDao referenceDao;
	
	//@Autowired
	//private EventService eventService;
	
	//@Autowired
	//private UserService userService;
	
	@Override
	public BigDecimal getCurrencyCost(long amount) {
		if(amount <= 0L) {
			return PyUtils.formatBDDown(0.0d);
		}
		// tax will apply this
		//return formatCurrency(amount / ServiceValues.CURRENCY_CONVERSION);
		return PyUtils.formatBDDown(amount / ServiceValues.CURRENCY_CONVERSION);
	}
	
	@Override
	public long getCurrencyFromCost(BigDecimal amount) {
		if(amount == null || amount.compareTo(PyUtils.formatBDDown(0.0d)) <= 0) {
			return 0l;
		}
		return PyUtils.formatBDDown(amount.multiply(PyUtils.formatBDDown(ServiceValues.CURRENCY_CONVERSION))).longValueExact();
	}
	
	@Override
	public BigDecimal getTaxFromCost(BigDecimal amount) {
		if(amount == null || amount.compareTo(PyUtils.formatBDDown(0.0d)) <= 0) {
			return PyUtils.formatBDDown(0.0d);
		}
		return PyUtils.formatBDHalfUp(PyUtils.formatBDDown(amount).multiply(new BigDecimal(ServiceValues.TAX_RATE_TOTAL)));
	}
	
	@Override
	public void addCurrency(ObjectId id, long amount) throws ServiceException {
		ArgCheck.nullCheck(id);
		if(amount <= 0L) {
			throw new BadParameterException();
		}
		
		List<FinanceDescription> targets = new ArrayList<FinanceDescription>();
		FinanceDescription fd = new FinanceDescription();
		fd.setUserId(id);
		fd.setCurrency(CurrencyNames.GOLD);
		fd.setAmount(amount);
		targets.add(fd);
		
		transaction(DEAL_TYPE.ADMIN_ADD, null, targets, null, false, null, null, amount, 
				null);
		logger.debug("Added currency for user id {" + id.toHexString() + "}.");
	}
	
	@Override
	public void removeCurrency(ObjectId id, long amount) throws ServiceException {
		ArgCheck.nullCheck(id);
		if(amount <= 0L) {
			throw new BadParameterException();
		}
		
		FinanceDescription fd = new FinanceDescription();
		fd.setUserId(id);
		fd.setCurrency(CurrencyNames.GOLD);
		fd.setAmount(amount);
		
		transaction(DEAL_TYPE.ADMIN_REMOVE, fd, null, null, false, null, null, amount, null);
		logger.debug("Removed currency for user id {" + id.toHexString() + "}.");
	}
	
	@Override
	public void purchaseCurrency(ObjectId paymentId, ObjectId id, PurchaseCurrencyDTO dto) 
			throws ServiceException {
		ArgCheck.nullCheck(paymentId, id, dto);
		long amount = dto.getAmount();
		if(amount <= 0L) {
			throw new BadParameterException();
		}
		
		List<FinanceDescription> targets = new ArrayList<FinanceDescription>();
		FinanceDescription fd = new FinanceDescription();
		fd.setUserId(id);
		fd.setCurrency(CurrencyNames.GOLD);
		fd.setAmount(amount);
		targets.add(fd);
		
		transaction(DEAL_TYPE.PURCHASE, null, targets, null, false, null, null, amount, 
				 paymentId);
		
		//userService.removeOverrideRole(id, OverrideRoleNames.UNPAID);
		logger.debug("Purchased currency for paymentId {" + paymentId.toHexString() 
				+ "} for user id {" + id.toHexString() + "}.");
	}
	
	@Override
	public void charge(ObjectId sourceId, ObjectId referenceId, 
			boolean createReferenceCost, String referenceCollection, long amount) 
					throws ServiceException {
		charge(sourceId, null, referenceId, createReferenceCost, 
				referenceCollection, amount);
		logger.debug("Charged user with id {" + sourceId.toHexString() 
				+ "} for reference {" + referenceId.toHexString() + "}.");
	}
	
	@Override
	public void charge(EscrowSourceTarget escrow, ObjectId referenceId,
			boolean createReferenceCost, String referenceCollection, long amount)
				throws ServiceException {
		charge(null, escrow, referenceId, createReferenceCost, 
				referenceCollection, amount);
		logger.debug("Charged escrow with id {" + escrow
				+ "} for reference {" + referenceId.toHexString() + "}.");
	}
	
	protected void charge(ObjectId sourceId, EscrowSourceTarget escrow, ObjectId referenceId, 
			boolean createReferenceCost, String referenceCollection, 
			long amount) throws ServiceException {
		ArgCheck.nullCheck(referenceId, referenceCollection);
		if(amount <= 0L) {
			throw new BadParameterException();
		}
		
		if(sourceId == null && escrow == null) {
			throw new BadParameterException();
		}
		
		FinanceDescription fd = new FinanceDescription();
		fd.setAmount(amount);
		fd.setUserId(sourceId);
		fd.setCurrency(CurrencyNames.GOLD);
		fd.setEscrow(escrow);
		List<FinanceDescription> targets = new ArrayList<FinanceDescription>();
		transaction(DEAL_TYPE.CHARGE, fd, targets, referenceId, createReferenceCost, 
				referenceCollection, null, amount, null);
	}
	
	@Override
	public void chargeForEscrow(ObjectId sourceId, EscrowSourceTarget escrow, long amount) throws ServiceException {
		ArgCheck.nullCheck(sourceId, escrow);
		if(amount <= 0L) {
			throw new BadParameterException();
		}
		
		String currency = CurrencyNames.GOLD;
		
		FinanceDescription fd = new FinanceDescription();
		fd.setAmount(amount);
		fd.setUserId(sourceId);
		fd.setCurrency(currency);
		
		List<FinanceDescription> targets = new ArrayList<FinanceDescription>();
		FinanceDescription target = new FinanceDescription();
		target.setAmount(amount);
		target.setCurrency(CurrencyNames.GOLD);
		target.setEscrow(escrow);
		targets.add(target);
		
		transaction(DEAL_TYPE.OFFER, fd, targets, null, false, CollectionNames.POSTING, 
				null, amount, null);
		logger.debug("Charged for escrow to user id {" + sourceId.toHexString() + "}.");
	}
	
	@Override
	public void refundEscrow(EscrowSourceTarget escrow, ObjectId sourceId, long amount) 
			throws ServiceException {
		ArgCheck.nullCheck(escrow, sourceId);
		if(amount <= 0L) {
			throw new BadParameterException();
		}
		
		FinanceDescription fd = new FinanceDescription();
		fd.setAmount(amount);
		fd.setEscrow(escrow);
		fd.setCurrency(CurrencyNames.GOLD);
		
		List<FinanceDescription> targets = new ArrayList<FinanceDescription>();
		FinanceDescription target = new FinanceDescription();
		target.setAmount(amount);
		target.setCurrency(CurrencyNames.GOLD);
		target.setUserId(sourceId);
		targets.add(target);
		
		transaction(DEAL_TYPE.ESCROW_REFUND, fd, targets, null, false, 
				CollectionNames.POSTING, null, amount, null);
		logger.debug("Refunded for escrow to user id {" + sourceId.toHexString() + "}.");
	}
	
	@Override
	public void transferEscrow(EscrowSourceTarget source, EscrowSourceTarget target, long amount)
		throws ServiceException {
		ArgCheck.nullCheck(source, target);
		if(amount <= 0L) {
			throw new BadParameterException();
		}
		
		FinanceDescription fd = new FinanceDescription();
		fd.setAmount(amount);
		fd.setEscrow(source);
		fd.setCurrency(CurrencyNames.GOLD);
		
		List<FinanceDescription> targets = new ArrayList<FinanceDescription>();
		FinanceDescription tfd = new FinanceDescription();
		tfd.setAmount(amount);
		tfd.setCurrency(CurrencyNames.GOLD);
		tfd.setEscrow(target);
		targets.add(tfd);
		
		transaction(DEAL_TYPE.ESCROW_TRANSFER, fd, targets, null, false, 
				CollectionNames.POSTING, null, amount, null);
		logger.debug("Transferred escrow from {" + source + "} to {" + target + "}.");
	}

	@Override
	public void appreciate(ObjectId sourceId, ObjectId referenceId, 
			String referenceCollection, long amount, long promotionAmount, 
			ObjectId paymentId) throws ServiceException {
		ArgCheck.nullCheck(sourceId, referenceId, referenceCollection, paymentId);
		if(amount <= 0L || promotionAmount <= 0L) {
			throw new BadParameterException();
		}
		
		transaction(DEAL_TYPE.APPRECIATE, null, null, referenceId, 
				false, referenceCollection, amount, promotionAmount, paymentId);
		
		//userService.removeOverrideRole(sourceId, OverrideRoleNames.UNPAID);
		logger.debug("Appreciation from user id {" + sourceId.toHexString() 
				+ "} for the reference {" + referenceId.toHexString() 
				+ "} with paymentId {" + paymentId.toHexString() + "}.");
	}
	
	@Override
	public void promote(ObjectId sourceId, ObjectId referenceId, 
			String referenceCollection, long promotionAmount) throws ServiceException {
		ArgCheck.nullCheck(sourceId, referenceId, referenceCollection);
		if(promotionAmount <= 0L) {
			throw new BadParameterException();
		}

		FinanceDescription sourceFd = new FinanceDescription();
		sourceFd.setUserId(sourceId);
		sourceFd.setCurrency(CurrencyNames.GOLD);
		sourceFd.setAmount(promotionAmount);
		
		transaction(DEAL_TYPE.PROMOTE, sourceFd, null, referenceId, false, 
				referenceCollection, null, promotionAmount, null);
		logger.debug("Promotion from user id {" + sourceId.toHexString() 
				+ "} for the reference {" + referenceId.toHexString() + "}.");
	}
	
	@Override
	public boolean completedPaymentTransaction(ObjectId paymentId) throws ServiceException {
		ArgCheck.nullCheck(paymentId);
		
		try {
			Deal deal = dealDao.getDeal(null, DEAL_STATE.SUCCESSFUL, paymentId);
			if(deal != null 
					&& PyUtils.objectIdCompare(deal.getPaymentId(), paymentId) 
					&& deal.getState() != null
					&& DEAL_STATE.SUCCESSFUL.equals(deal.getState())) {
				return true;
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		return false;
	}
	
	// NO OVERRIDE
	public void transaction(DEAL_TYPE type, FinanceDescription source, 
			List<FinanceDescription> targets, ObjectId referenceId, 
			boolean createReferenceCost, String referenceCollection, Long primaryAmount, 
			Long secondaryAmount, ObjectId paymentId) throws ServiceException {
		// no other null checks. source, target, or reference may be null
		ArgCheck.nullCheck(type);
		// if amount is less than 0, source/targets need to be switched
		
		// longs are already rounded, so no need for this any more
		/*
		if(primaryAmount != null) {
			primaryAmount = new Double(PyUtils.formatBDDown(primaryAmount.doubleValue()).doubleValue());
		}*/
		if(primaryAmount == null && secondaryAmount == null) {
			throw new BadParameterException();
		}
		if(referenceId != null) {
			if(referenceCollection == null || !(CollectionNames.POSTING.equals(referenceCollection) 
					|| CollectionNames.COMMENT.equals(referenceCollection))) {
				throw new BadParameterException();
			}
		}
		
		String sourceId = null;
		String sourceCollection = CollectionNames.USER_INFO;
		if(source != null) {
			sourceId = FinanceDescription.getId(source);
			if(sourceId == null) {
				throw new BadParameterException();
			}
			if(!Validation.validCurrency(source.getCurrency())) {
				throw new BadParameterException();
			}
			// no need to check, this class should guarantee it is correct
			sourceCollection = FinanceDescription.getCollection(source);
		}
		
		if(targets != null && targets.isEmpty()) {
			targets = null;
		}
		
		try {
			if(targets != null) {
				for(FinanceDescription fd : targets) {
					// just checking for bad parameters
					if(FinanceDescription.getId(fd) == null) {
						throw new BadParameterException();
					}
					if(!Validation.validCurrency(fd.getCurrency())) {
						throw new BadParameterException();
					}
				}
				
			}
		} catch(Exception e) {
			// just in case of list exceptions
			logger.info("Exception while checking finance descriptions.", e);
			throw new BadParameterException();
		}
		
		Deal deal = null;
		ObjectId id = null;
		
		try {
			if(source != null && !walletDao.verifyHasFunds(sourceId, 
					source.getCurrency(), source.getAmount(), sourceCollection)) {
				throw new BalanceException();
			}
		} catch(DaoException de) {
			logger.info("Could not verify funds correctly!", de);
			throw new FinanceException();
		}
		
		try {
			deal = dealDao.initializeDeal(type, source, targets, referenceId, 
					createReferenceCost, referenceCollection, primaryAmount, 
					secondaryAmount, paymentId);
			
			// verification
			if(deal == null) {
				logger.warn("A deal was not created!");
				throw new FinanceException();
			}
			
			id = deal.getId();
			
			if(id == null) {
				logger.warn("A deal was not given an id! {" + deal + "}.");
				throw new FinanceException();
			}
			
			// update deal state in case of cleanup
			deal.setState(DEAL_STATE.INITIAL);
			
			if(!dealDao.verifyDeal(id, type, source, targets, referenceId, 
					createReferenceCost, referenceCollection, primaryAmount, 
					secondaryAmount, paymentId)
					|| !dealDao.checkDealState(id, DEAL_STATE.INITIAL)) {
				cleanupDeal(deal);
				logger.info("Deal was not correctly verified {" + deal + "} !");
				throw new FinanceException();
			}
		} catch(DaoException de) {
			cleanupDeal(deal);
			logger.info("Deal was not successfully created {" + deal + "} !", de);
			throw new FinanceException(de);
		}

		try {
			dealDao.updateDealState(id, DEAL_STATE.PENDING, null, null, null);
			if(source != null) {
				walletDao.startTransaction(sourceId, id, 
						source.getCurrency(), 0 - source.getAmount(), sourceCollection);
			}
			if(targets != null) {
				// upon failure of any target, each must be 
				// individually checked and reverted
				for(FinanceDescription fd : targets) {
					String targetId = FinanceDescription.getId(fd);
					String targetCollection = FinanceDescription.getCollection(fd);
					walletDao.startTransaction(targetId, id, 
							fd.getCurrency(), fd.getAmount(), targetCollection);
				}
			}
			if(referenceId != null && referenceCollection != null) {
				if(createReferenceCost) {
					referenceDao.chargeTally(referenceId, id, secondaryAmount, 
							referenceCollection);
				} else {
					referenceDao.addTally(referenceId, id, primaryAmount, secondaryAmount, 
							referenceCollection);
				}
			}
			// verification
			if(!dealDao.checkDealState(id, DEAL_STATE.PENDING)) {
				cleanupDeal(deal);
				logger.info("Deal state was not successfully verified as pending {" + deal + "} !");
				throw new FinanceException();
			}
			
			deal.setState(DEAL_STATE.PENDING);
			
			if(source != null && !walletDao.verifyTransactionStarted(sourceId, id, sourceCollection)) {
				cleanupDeal(deal);
				logger.info("Deal could not verify source was added for source {" + sourceId + "} of deal {" + deal + "} !");
				throw new FinanceException();
			}
			
			deal.setSourceAdded(true);

			if(targets != null) {
				for(FinanceDescription fd : targets) {
					String targetId = FinanceDescription.getId(fd);
					String targetCollection = FinanceDescription.getCollection(fd);
					if(!walletDao.verifyTransactionStarted(targetId, id, targetCollection)) {
						cleanupDeal(deal);
						logger.info("Deal could not verify target was added for target {" + targetId + "} of deal {" + deal + "} !");
						throw new FinanceException();
					}
				}
			}
			
			deal.setTargetsAdded(true);
			
			if(referenceId != null && !referenceDao.verifyTallyAdded(referenceId, id, referenceCollection)) {
				cleanupDeal(deal);
				logger.info("Deal could not verify reference was added for reference {" + referenceId + "} of deal {" + deal + "} !");
				throw new FinanceException();
			}
			
			deal.setReferenceAdded(true);
			
			if(source != null && !walletDao.verifyHasFunds(sourceId, 
					source.getCurrency(), 0, sourceCollection)) {
				cleanupDeal(deal);
				logger.info("Deal could not verify source had correct funds remaining for source {" + source + "} of deal {" + deal + "} !");
				throw new BalanceException();
			}
			
		} catch(DaoException de) {
			cleanupDeal(deal);
			logger.info("Deal had an error while pending {" + deal + "} !", de);
			throw new FinanceException(de);
		}
		
		try {
			dealDao.updateDealState(id,  DEAL_STATE.COMMITTED, deal.isSourceAdded(), 
					deal.isTargetsAdded(), deal.isReferenceAdded());
			if(source != null) {
				walletDao.completeTransaction(sourceId, id, sourceCollection);
			}
			if(targets != null) {
				for(FinanceDescription fd : targets) {
					String targetId = FinanceDescription.getId(fd);
					String targetCollection = FinanceDescription.getCollection(fd);
					walletDao.completeTransaction(targetId, id, targetCollection);
				}
			}
			if(referenceId != null) {
				referenceDao.completeTally(referenceId, id, referenceCollection);
			}
			
			// verification
			if(source != null && !walletDao.verifyTransactionCompleted(sourceId, 
					id, sourceCollection)) {
				cleanupDeal(deal);
				logger.info("Deal could not verify source was completed for source {" + sourceId + "} of deal {" + deal + "} !");
				throw new FinanceException();
			}
			
			if(targets != null) {
				for(FinanceDescription fd : targets) {
					String targetId = FinanceDescription.getId(fd);
					String targetCollection = FinanceDescription.getCollection(fd);
					if(!walletDao.verifyTransactionCompleted(targetId, id, targetCollection)) {
						cleanupDeal(deal);
						logger.info("Deal could not verify target was completed for target {" + targetId + "} of deal {" + deal + "} !");
						throw new FinanceException();
					}
				}
			}
			
			if(referenceId != null) {
				if((createReferenceCost && !referenceDao.verifyTallyCostCompleted(referenceId, id, secondaryAmount, referenceCollection)) 
						|| (!createReferenceCost && !referenceDao.verifyTallyCompleted(referenceId, id, referenceCollection))) {
					cleanupDeal(deal);
					logger.info("Deal could not verify reference was completed for reference {" + referenceId + "} of deal {" + deal + "} !");
					throw new FinanceException();
				}
			}
			
			if(!dealDao.checkDealState(id, DEAL_STATE.COMMITTED)) {
				cleanupDeal(deal);
				logger.info("Deal state was not successfully verified as committed {" + deal + "} !");
				throw new FinanceException();
			}
		} catch(DaoException de) {
			cleanupDeal(deal);
			logger.info("Deal had an error while committing {" + deal + "} !", de);
			throw new FinanceException(de);
		}
		
		try {
			dealDao.updateDealState(id, DEAL_STATE.SUCCESSFUL, null, null, null);
			
			if(!dealDao.checkDealState(id, DEAL_STATE.SUCCESSFUL)) {
				cleanupDeal(deal);
				throw new FinanceException();
			}
		} catch(DaoException de) {
			cleanupDeal(deal);
			logger.info("Deal had an error while updating to successful {" + deal + "} !", de);
			throw new FinanceException(de);
		}
		logger.debug("Transaction with id {" + id.toHexString() 
				+ "} completed successfully!");
	}
	
	@Override
	public void cleanupDeal(Deal deal) throws ServiceException {
		ArgCheck.nullCheck(deal);
		ArgCheck.nullCheck(deal.getId());
		ObjectId id = deal.getId();
		
		DaoException anyException = null;
		
		logger.info("Cleanup deal with id {" + id + "}.");
		
		try {
			dealDao.updateDealState(id, DEAL_STATE.PENDING_FAILURE, null, null, null);
		} catch(DaoException de) {
			// in case of error here, continue attempting to roll back
			anyException = de;
			logger.warn("Could not update deal state to pending failure for deal id {" + id + "}.", de);
		}
		
		try {
			// not sufficient just to check if deal recorded it being added
			if(deal.getSource() != null) {
				if(deal.isSourceAdded() 
						|| walletDao.verifyTransactionStarted(
								FinanceDescription.getId(deal.getSource()), id, 
								FinanceDescription.getCollection(deal.getSource()))) {
					// credit source
					walletDao.revertPendingTransaction(FinanceDescription.getId(deal.getSource()), 
						id, deal.getSource().getCurrency(), deal.getSource().getAmount(), 
						FinanceDescription.getCollection(deal.getSource()));
				}
			}
		} catch(DaoException de) {
			// continue
			anyException = de;
			logger.warn("Could not revert source add for deal id {" + id + "}.", de);
		}
		
		if(deal.getTargets() != null) {
			// decredit targets
			List<FinanceDescription> targets = deal.getTargets();
			for(FinanceDescription fd : targets) {
				String targetId = FinanceDescription.getId(fd);
				String targetCollection = FinanceDescription.getCollection(fd);
				try {
					// try catch in case one fails, the others still must be checked and
					// reverted as necessary, although this may not be necessary if
					// order is preserved
					if(deal.isTargetsAdded() 
							|| walletDao.verifyTransactionStarted(targetId, 
									id, targetCollection)) {
						walletDao.revertPendingTransaction(targetId, 
							id, fd.getCurrency(), 0 - fd.getAmount(), targetCollection);
					}
				} catch(DaoException de) {
					// continue
					anyException = de;
					logger.warn("Could not revert target add for deal id {" + id + "} and targetId '" + targetId + "'.", de);
				}
			}
		}
		
		try {
			if(deal.getReference() != null) {
				if(deal.isReferenceAdded()
						|| referenceDao.verifyTallyAdded(deal.getReference(), id, 
								deal.getReferenceCollection())) {
					// de tally reference
					if(deal.isCreateReferenceCost()) {
						referenceDao.revertPendingCost(deal.getReference(), id, 
							deal.getReferenceCollection());
					} else {
						Long primary = null;
						Long secondary = null;
						if(deal.getPrimaryAmount() != null) {
							primary = 0 - deal.getPrimaryAmount();
						}
						if(deal.getSecondaryAmount() != null) {
							secondary = 0 - deal.getSecondaryAmount();
						}
						referenceDao.revertPendingTally(deal.getReference(), 
							id, primary, secondary, deal.getReferenceCollection());
					}
				}
			}
		} catch(DaoException de) {
			// continue
			anyException = de;
			logger.warn("Could not revert reference add for deal id {" + id + "}.", de);
		}
		
		try {
			dealDao.updateDealState(id , DEAL_STATE.FAILURE, null, null, null);
		} catch(DaoException de) {
			anyException = de;
			logger.warn("Could not update deal state to failure for deal id {" + id + "}.", de);
		}
		
		if(anyException != null) {
			logger.debug("Transaction with id {" + id.toHexString() 
					+ "} failed and was not successfully failed!");
			throw new FinanceException(anyException);
		}
		logger.debug("Transaction with id {" + id.toHexString() 
				+ "} failed!");
	}
	
	@Override
	public void checkBatchDeals(List<DEAL_STATE> states) throws ServiceException {
		if(states != null && states.isEmpty()) {
			states = null;
		}
		
		Pageable pageable = new PageRequest(0, ServiceValues.DEAL_BATCH_SIZE);
		Date then = new Date((new Date()).getTime()
				- ServiceValues.DEAL_EXPIRATION_PERIOD);
		Page<Deal> deals = null;
		try {
			deals = dealDao.getDeals(states, then, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(deals == null) {
			throw new ServiceException();
		}
		
		ServiceException lastException = null;
		for(Deal deal: deals.getContent()) {
			try {
				checkDealStatus(deal);
			} catch(ServiceException se) {
				lastException = se;
				logger.warn("Exception when checking status for deal {" + deal + "}!", se);
			}
		}
		if(lastException != null) {
			throw lastException;
		}
		// do not loop through other pages, just one batch per call
	}
	
	@Override
	public void removeFinishedDeals() throws ServiceException {
		List<DEAL_STATE> states = new ArrayList<DEAL_STATE>();
		states.add(DEAL_STATE.SUCCESSFUL);
		states.add(DEAL_STATE.FAILURE);
		Date then = new Date((new Date()).getTime()
				- ServiceValues.DEAL_EXPIRATION_PERIOD);
		try {
			dealDao.remove(states, then);
		} catch(DaoException de) {
			logger.info("Could not remove states for states [" + states + "] for time {" + then + "}.", de);
			throw new ServiceException(de);
		}
	}
	
	protected void checkDealStatus(Deal deal) throws ServiceException {
		ArgCheck.nullCheck(deal);
		
		DEAL_STATE state = deal.getState();
		Date then = new Date((new Date()).getTime()
				- ServiceValues.DEAL_EXPIRATION_PERIOD);
		// completely finished or reverted deals, or ones currently in progress, should
		// not be reverted
		if(state == DEAL_STATE.SUCCESSFUL || state == DEAL_STATE.FAILURE
				|| (deal.getLastModified() != null && deal.getLastModified().after(then))) {
			return;
		}
		
		// all others reverted
		cleanupDeal(deal);
	}
	
}
