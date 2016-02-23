package com.py.py.service.impl;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.ServiceValues;
import com.py.py.dao.EscrowDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Escrow;
import com.py.py.domain.User;
import com.py.py.domain.enumeration.ESCROW_TYPE;
import com.py.py.domain.subdomain.Balance;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.EmailService;
import com.py.py.service.EscrowService;
import com.py.py.service.EventService;
import com.py.py.service.FinanceService;
import com.py.py.service.UserService;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BackerNotFoundException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.BalanceException;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.FinanceException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;

public class EscrowServiceImpl implements EscrowService {

	protected static final PyLogger logger = PyLogger.getLogger(EscrowServiceImpl.class);
	
	@Autowired
	protected EscrowDao escrowDao;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected FinanceService financeService;
	
	@Autowired
	protected EventService eventService;
	
	@Autowired
	protected EmailService emailService;
	
	protected boolean usesObjectId(Escrow escrow) {
		if(escrow != null) {
			if(!ESCROW_TYPE.EMAIL_OFFER.equals(escrow.getType())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public EscrowSourceTarget getBackerEscrowId(ObjectId source, ObjectId target) 
			throws ServiceException {
		ArgCheck.nullCheck(source, target);
		Escrow escrow = getEscrow(ESCROW_TYPE.BACKING, source, null, 
				target.toHexString(), null);
		if(escrow == null) {
			throw new NotFoundException(target.toHexString());
		}
		return escrow.getSourceTarget();
	}
	
	protected Escrow getEscrow(ESCROW_TYPE type, ObjectId sourceId, String sourceName, 
			String target, String targetName) 
			throws ServiceException {
		ArgCheck.nullCheck(type);
		
		String sourceUsername = null;
		if(sourceName != null) {
			sourceUsername = ServiceUtils.getIdName(sourceName);
		}
		String targetUsername = null;
		if(targetName != null) {
			targetUsername = ServiceUtils.getIdName(targetName);
		}

		String source = null;
		if(sourceId != null) {
			source = sourceId.toHexString();
		}
		
		try {
			Escrow escrow = escrowDao.findEscrow(type, source, sourceUsername, target,
					targetUsername);
			
			if(escrow == null) {
				throw new NotFoundException(target);
			}
			
			return escrow;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public Escrow getBackerEscrow(ObjectId sourceId, String sourceName, 
			ObjectId targetId, String targetName) throws ServiceException {
		ArgCheck.nullCheck(sourceName, targetName);
		String sourceUsername = ServiceUtils.getIdName(sourceName);
		String targetUsername = ServiceUtils.getIdName(targetName);
		String target = null;
		if(targetId != null) {
			target = targetId.toHexString();
		}
		
		return getEscrow(ESCROW_TYPE.BACKING, sourceId, sourceUsername, target, 
				targetUsername);
	}
	
	@Override
	public Escrow getOfferEscrow(ObjectId sourceId, String sourceName, 
			ObjectId targetId, String targetName) throws ServiceException {
		ArgCheck.nullCheck(sourceName, targetName);
		String sourceUsername = ServiceUtils.getIdName(sourceName);
		String targetUsername = ServiceUtils.getIdName(targetName);
		String target = null;
		if(targetId != null) {
			target = targetId.toHexString();
		}
		
		return getEscrow(ESCROW_TYPE.OFFER, sourceId, sourceUsername, target, targetUsername);
	}
	
	@Override
	public Escrow getEmailOfferEscrow(ObjectId sourceId, String sourceName, String email) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceName, email);
		
		String target = ServiceUtils.getUniqueEmail(email);
		String sourceUsername = ServiceUtils.getIdName(sourceName);

		return getEscrow(ESCROW_TYPE.EMAIL_OFFER, sourceId, sourceUsername, target, null);
	}
	
	@Override
	public void addOffer(User sourceUser, User targetUser, long amount) 
			throws ServiceException {
		ArgCheck.userCheck(sourceUser, targetUser);
		if(amount <= 0L) {
			throw new BadParameterException();
		}
		
		String source = sourceUser.getId().toHexString();
		String sourceName = sourceUser.getUsername();
		CachedUsername cachedSource = new CachedUsername(sourceUser.getId(), 
				sourceUser.getUsername());
		String target = targetUser.getId().toHexString();
		String targetName = targetUser.getUsername();
		CachedUsername cachedTarget = new CachedUsername(targetUser.getId(), 
				targetUser.getUsername());
		
		if(PyUtils.stringCompare(source, target) 
				|| PyUtils.stringCompare(sourceName, targetName)) {
			throw new ActionNotAllowedException();
		}
		
		try {
			escrowDao.initializeEscrow(ESCROW_TYPE.OFFER, source, sourceName, 
					target, targetName);
			
			EscrowSourceTarget escrowId = new EscrowSourceTarget(source, target, ESCROW_TYPE.OFFER);
			
			financeService.chargeForEscrow(sourceUser.getId(), escrowId, amount);
			
			eventService.eventOffer(cachedSource, cachedTarget, amount);
			logger.debug("Offer added from {" + source + "} to {" + target + "}.");
			
		} catch(BalanceException be) {
			try {
				escrowDao.cleanupEmpties(ESCROW_TYPE.OFFER, source, null, target, null, null);
			} catch(Exception e) {
				// do nothing, we tried
			}
			throw be;
		} catch(FinanceException fe) {
			try {
				escrowDao.cleanupEmpties(ESCROW_TYPE.OFFER, source, null, target, null, null);
			} catch(Exception e) {
				// do nothing, we tried
			}
			throw fe;
		} catch(ExistsException ee) {
			throw new ServiceException(ee);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void addEmailOffer(User sourceUser, User targetUser, String targetEmail, 
			long amount) throws ServiceException {
		ArgCheck.nullCheck(targetEmail);
		ArgCheck.userCheck(sourceUser);
		if(amount <= 0L) {
			throw new BadParameterException();
		}

		CachedUsername cachedTarget = null;
		String targetName = null;
		String email = ServiceUtils.getUniqueEmail(targetEmail);
		if(targetUser != null) {
			ArgCheck.userCheck(targetUser);
			email = targetUser.getUniqueEmail();
			targetName = targetUser.getUsername();
			cachedTarget = new CachedUsername(targetUser.getId(), 
					targetUser.getUsername());
		}
		

		String source = sourceUser.getId().toHexString();
		String sourceName = sourceUser.getUsername();
		CachedUsername cachedSource = new CachedUsername(sourceUser.getId(), 
				sourceUser.getUsername());

		ESCROW_TYPE type = ESCROW_TYPE.EMAIL_OFFER;
		
		if(PyUtils.stringCompare(sourceUser.getUniqueEmail(), email) 
				|| PyUtils.stringCompare(sourceName, targetName)) {
			throw new ActionNotAllowedException();
		}
		
		try {
			// do not show if the email is in use, and definitely not the username associated
			//escrowDao.initializeEscrow(type, source, sourceName, email, targetName);
			escrowDao.initializeEscrow(type, source, sourceName, email, null);
			
			EscrowSourceTarget escrowId = new EscrowSourceTarget(source, email, type);
			
			financeService.chargeForEscrow(sourceUser.getId(), escrowId, amount);
			
			emailService.offerEmail(email, targetName);
			
			if(cachedTarget != null) {
				eventService.eventOffer(cachedSource, cachedTarget, amount);
			}
			logger.debug("Email offer added from {" + source + "} to email (" 
					+ email + ").");
			
		} catch(BalanceException be) {
			try {
				escrowDao.cleanupEmpties(ESCROW_TYPE.EMAIL_OFFER, source, null, email, null, null);
			} catch(Exception e) {
				// do nothing, we tried
			}
			throw be;
		} catch(FinanceException fe) {
			try {
				escrowDao.cleanupEmpties(ESCROW_TYPE.EMAIL_OFFER, source, null, email, null, null);
			} catch(Exception e) {
				// do nothing, we tried
			}
			throw fe;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(ExistsException ee) {
			throw ee;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected Page<BackerDTO> convertEscrows(Page<Escrow> escrows, Pageable pageable)
			throws ServiceException {
		if(escrows == null || pageable == null) {
			throw new ServiceException();
		}
		
		List<BackerDTO> dtos = ModelFactory.<BackerDTO>constructList();
		
		for(Escrow e : escrows.getContent()) {
			try {
				if(e != null && e.getBalance() != null) {
					Balance balance = e.getBalance();
					if(balance.getGold() != null && balance.getGold() > 0) {
						CachedUsername source = new CachedUsername(
								new ObjectId(e.getSource()), e.getSourceName());
						ObjectId tId = null;
						String targetName = e.getTargetName();
						if(usesObjectId(e)) {
							tId = new ObjectId(e.getTarget());
						} else {
							targetName = e.getId().getTarget();
						}
						CachedUsername target = new CachedUsername(tId, targetName);
						dtos.add(Mapper.mapBackerDTO(source, target, !usesObjectId(e), balance.getGold()));
					}
				}
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping for escrow!", bpe);
			} catch(Exception ex) {
				logger.info("Invalid mapping for escrow!", ex);
			}
		}
		
		return new PageImpl<BackerDTO>(dtos, pageable, escrows.getTotalElements());
	}
	
	protected Page<Escrow> getEscrows(ESCROW_TYPE type, ObjectId sourceId, 
			ObjectId targetId, Pageable pageable) throws ServiceException {
		ArgCheck.nullCheck(type, pageable);
		
		String source = null;
		if(sourceId != null) {
			source = sourceId.toHexString();
		}
		String target = null;
		if(targetId != null) {
			target = targetId.toHexString();
		}
		
		try {
			return escrowDao.findSorted(type, source, null, target, null, 
					pageable);
		} catch(DaoException de) {
			throw new ServiceException();
		}
	}
	
	protected Page<BackerDTO> getConvertedEscrows(ESCROW_TYPE type, ObjectId sourceId, 
			ObjectId targetId, Pageable pageable) throws ServiceException {
		return convertEscrows(getEscrows(type, sourceId, targetId, pageable), pageable);
	}
	
	@Override
	public Page<BackerDTO> getBackersOutstanding(ObjectId sourceId, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceId, pageable);
			
		return getConvertedEscrows(ESCROW_TYPE.BACKING, sourceId, null, pageable);
	}
	
	@Override
	public Page<BackerDTO> getOffersOutstanding(ObjectId sourceId, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceId, pageable);
		
		return getConvertedEscrows(ESCROW_TYPE.OFFER, sourceId, null, pageable);
	}
	
	@Override
	public Page<BackerDTO> getEmailOffersOutstanding(ObjectId sourceId, Pageable pageable) 
			throws ServiceException {
		
		ArgCheck.nullCheck(sourceId, pageable);
		
		return getConvertedEscrows(ESCROW_TYPE.EMAIL_OFFER, sourceId, null, pageable);
	}
	
	@Override
	public Page<BackerDTO> getBackers(ObjectId targetId, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(targetId, pageable);
		
		return getConvertedEscrows(ESCROW_TYPE.BACKING, null, targetId, pageable);
	}
	
	@Override
	public Page<BackerDTO> getOffers(User user, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(pageable);
		ArgCheck.userCheck(user);

		String email = ServiceUtils.getUniqueEmail(user.getEmail());
		
		try {
			Page<Escrow> escrows = escrowDao.findSortedMulti(ESCROW_TYPE.OFFER, null, null,
					user.getId().toHexString(), null, ESCROW_TYPE.EMAIL_OFFER, null, 
					null, email, null, pageable);
			return convertEscrows(escrows, pageable);
		} catch(DaoException de) {
			throw new ServiceException();
		} catch(ServiceException se) {
			throw se;
		}
	}
	
	@Override
	public void acceptOffer(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceName);
		ArgCheck.userCheck(targetUser);
		
		// sourceUser may be null
		ObjectId sourceId = null;
		String source = null;
		String sourceUsername = ServiceUtils.getName(sourceName);
		if(sourceUser != null) {
			ArgCheck.userCheck(sourceUser);
			sourceUsername = sourceUser.getUsername();
			sourceId = sourceUser.getId();
			source = sourceId.toHexString();
		}
		
		ObjectId targetId = targetUser.getId();
		String target = targetId.toHexString();
		String targetUsername = targetUser.getUsername();
		
		CachedUsername cachedSource = new CachedUsername(sourceId, sourceUsername);
		CachedUsername cachedTarget = new CachedUsername(targetUser.getId(), 
				targetUser.getUsername());
		
		// looking for not found exception
		Escrow e = getOfferEscrow(sourceId, sourceUsername, targetId, targetUsername);
		
		if(e.getBalance() == null || e.getBalance().getGold() <= 0) {
			throw new NotFoundException(target);
		}
		
		if(sourceUser == null) {
			deny(e, true);
			throw new BackerNotFoundException(sourceName);
		}
		
		// get backers and see if part of them, up to twice the allowed size in case they cheat and get more through race conditions
		Page<Escrow> backers = getEscrows(ESCROW_TYPE.BACKING, null, targetId, new PageRequest(0,ServiceValues.BACKING_TOTAL_USERS * 2));
		if(backers.getTotalElements() >= ServiceValues.BACKING_TOTAL_USERS) {
			boolean contained = false;
			for(Escrow escrow : backers.getContent()) {
				if(PyUtils.stringCompare(escrow.getSource(), source)) {
					contained = true;
				}
			}
			if(!contained) {
				throw new ActionNotAllowedException();
			}
		}
		
		try {
			escrowDao.initializeEscrow(ESCROW_TYPE.BACKING, source, sourceUsername, target, targetUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		EscrowSourceTarget targetST = new EscrowSourceTarget(source, target, ESCROW_TYPE.BACKING);
		
		try {
			financeService.transferEscrow(e.getSourceTarget(), targetST, e.getBalance().getGold());
		} catch(BalanceException be) {
			// it was probably accepted concurrently
			throw new ServiceException(be);
		} catch(FinanceException fe) {
			// it was probably accepted concurrently
			throw new ServiceException(fe);
		} catch(ServiceException se) {
			throw new ServiceException(se);
		}
		if(sourceId != null) {
			eventService.eventOfferAccept(cachedTarget, cachedSource);
		}
		logger.debug("Offer accepted from (" + sourceUsername 
				+ ") with id {" + sourceId + "} to (" + targetUsername + ") with id {" + target + "}.");
		
		try {
			escrowDao.cleanupEmpties(ESCROW_TYPE.OFFER, source, null, null, targetUsername, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void denyOffer(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceName);
		ArgCheck.userCheck(targetUser);

		ObjectId sourceId = null;
		String sourceUsername = ServiceUtils.getName(sourceName);
		if(sourceUser != null) {
			ArgCheck.userCheck(sourceUser);
			sourceUsername = sourceUser.getUsername();
			sourceId = sourceUser.getId();
		}
		
		String targetUsername = targetUser.getUsername();
		ObjectId targetId = targetUser.getId();

		Escrow o = getOfferEscrow(sourceId, sourceUsername, targetId, targetUsername);
		
		CachedUsername cachedSource = new CachedUsername(sourceId, sourceUsername);
		CachedUsername cachedTarget = new CachedUsername(targetUser.getId(), 
				targetUser.getUsername());
		
		deny(o, sourceId == null);
		if(sourceId != null) {
			eventService.eventOfferDeny(cachedTarget, cachedSource);
		}
		logger.debug("Offer denied from (" + sourceUsername 
				+ ") with id {" + sourceId + "} to {" + targetId + "}.");
	}
	
	@Override
	public void acceptEmailOffer(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceName);
		ArgCheck.userCheck(targetUser);
		
		// sourceUser may be null
		ObjectId sourceId = null;
		String source = null;
		String sourceUsername = ServiceUtils.getName(sourceName);
		if(sourceUser != null) {
			ArgCheck.userCheck(sourceUser);
			sourceUsername = sourceUser.getUsername();
			sourceId = sourceUser.getId();
			source = sourceId.toHexString();
		}
		
		String targetEmail = ServiceUtils.getUniqueEmail(targetUser.getEmail());
		ObjectId targetId = targetUser.getId();
		String target = targetId.toHexString();
		String targetUsername = targetUser.getUsername();
		
		CachedUsername cachedSource = new CachedUsername(sourceId, sourceUsername);
		CachedUsername cachedTarget = new CachedUsername(targetUser.getId(), 
				targetUser.getUsername());
		
		Escrow e = getEmailOfferEscrow(sourceId, sourceUsername, targetEmail);
		
		if(e.getBalance() == null || e.getBalance().getGold() <= 0) {
			throw new NotFoundException(target);
		}
		
		if(sourceUser == null) {
			deny(e, true);
			throw new BackerNotFoundException(sourceName);
		}
		
		// get backers and see if part of them, up to twice the allowed size in case they cheat and get more through race conditions
		Page<Escrow> backers = getEscrows(ESCROW_TYPE.BACKING, null, targetId, new PageRequest(0,ServiceValues.BACKING_TOTAL_USERS * 2));
		if(backers.getTotalElements() >= ServiceValues.BACKING_TOTAL_USERS) {
			boolean contained = false;
			for(Escrow escrow : backers.getContent()) {
				if(PyUtils.stringCompare(escrow.getSource(), source)) {
					contained = true;
				}
			}
			if(!contained) {
				throw new ActionNotAllowedException();
			}
		}
		
		try {
			escrowDao.initializeEscrow(ESCROW_TYPE.BACKING, source, sourceUsername, target, targetUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		EscrowSourceTarget targetST = new EscrowSourceTarget(source, target, ESCROW_TYPE.BACKING);
		
		try {
			financeService.transferEscrow(e.getSourceTarget(), targetST, e.getBalance().getGold());
		} catch(BalanceException be) {
			// it was probably accepted concurrently
			throw new ServiceException(be);
		} catch(FinanceException fe) {
			// it was probably accepted concurrently
			throw new ServiceException(fe);
		} catch(ServiceException se) {
			throw new ServiceException(se);
		}
		if(sourceId != null) {
			eventService.eventOfferAccept(cachedTarget, cachedSource);
		}
		logger.debug("Email offer accepted from (" + sourceUsername 
				+ ") with id {" + sourceId + "} to (" + targetEmail + ") with id {" + target + "}.");
		
		try {
			escrowDao.cleanupEmpties(ESCROW_TYPE.EMAIL_OFFER, source, null, null, targetUsername, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void denyEmailOffer(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceName);
		ArgCheck.userCheck(targetUser);

		ObjectId sourceId = null;
		String sourceUsername = ServiceUtils.getName(sourceName);
		if(sourceUser != null) {
			ArgCheck.userCheck(sourceUser);
			sourceUsername = sourceUser.getUsername();
			sourceId = sourceUser.getId();
		}
		
		ObjectId targetId = targetUser.getId();
		String targetEmail = ServiceUtils.getEmail(targetUser.getEmail());

		Escrow eo = getEmailOfferEscrow(sourceId, sourceUsername, targetEmail);
		
		//CachedUsername cachedSource = new CachedUsername(sourceId, sourceUsername);
		//CachedUsername cachedTarget = new CachedUsername(targetUser.getId(), 
		//		targetUser.getUsername());
		
		deny(eo, sourceId == null);
		
		// this may reveal username of associated email; do not send a notification!
		/*
		if(sourceId != null) {
			eventService.eventOfferDeny(cachedTarget, cachedSource);
		}
		*/
		logger.debug("Email offer denied from (" + sourceUsername 
				+ ") with id {" + sourceId + "} to (" + targetEmail + ") with id {" + targetId + "}.");
	}
	
	@Override
	public void withdrawOffer(User sourceUser, User targetUser, String targetName) 
			throws ServiceException {
		ArgCheck.nullCheck(targetName);
		ArgCheck.userCheck(sourceUser);

		ObjectId targetId = null;
		String targetUsername = ServiceUtils.getName(targetName);
		if(targetUser != null) {
			ArgCheck.userCheck(targetUser);
			targetUsername = targetUser.getUsername();
			targetId = targetUser.getId();
		}
		String sourceUsername = sourceUser.getUsername();
		ObjectId sourceId = sourceUser.getId();
		
		Escrow escrow = getOfferEscrow(sourceId, sourceUsername, targetId, targetUsername);
		
		// do not ever force this or the user may not receive their funds
		deny(escrow, false);
		
		CachedUsername cachedSource = new CachedUsername(sourceUser.getId(), 
				sourceUsername);
		CachedUsername cachedTarget = new CachedUsername(targetId, escrow.getTargetName());
		
		if(targetId != null) {
			eventService.eventOfferWithdraw(cachedSource, cachedTarget);
		}
		logger.debug("Offer withdrawn from {" + sourceId + "} to (" 
				+ targetUsername + ") with id {" + targetId + "}.");
	}
	
	@Override
	public void withdrawEmailOffer(User sourceUser, User targetUser, String targetEmail) 
			throws ServiceException {
		ArgCheck.nullCheck(targetEmail);
		ArgCheck.userCheck(sourceUser);

		String sourceUsername = sourceUser.getUsername();
		ObjectId sourceId = sourceUser.getId();
		String email = ServiceUtils.getUniqueEmail(targetEmail);

		CachedUsername cachedTarget = null;
		ObjectId targetId = null;
		if(targetUser != null) {
			ArgCheck.userCheck(targetUser);
			targetId = targetUser.getId();
			cachedTarget = new CachedUsername(targetId, targetUser.getUsername());
		}
		
		Escrow escrow = getEmailOfferEscrow(sourceId, sourceUsername, email);

		// do not ever force this or the user may not receive their funds
		deny(escrow, false);
		
		CachedUsername cachedSource = new CachedUsername(sourceId, 
				sourceUsername);
		
		if(cachedTarget != null) {
			eventService.eventOfferWithdraw(cachedSource, cachedTarget);
		}
		logger.debug("Email offer withdrawn from {" + sourceId + "} to (" 
				+ email + ") with id {" + targetId + "}.");
	}
	
	@Override
	public void cancelBacking(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceName);
		ArgCheck.userCheck(targetUser);

		String sourceUsername = ServiceUtils.getName(sourceName);
		String targetUsername = targetUser.getUsername();
		ObjectId targetId = targetUser.getId();
		
		ObjectId sourceId = null;
		if(sourceUser != null) {
			ArgCheck.userCheck(sourceUser);
			sourceUsername = sourceUser.getUsername();
			sourceId = sourceUser.getId();
		}

		Escrow escrow = getBackerEscrow(sourceId, sourceUsername, targetId, targetUsername);

		deny(escrow, sourceId == null);
		
		CachedUsername cachedSource = new CachedUsername(sourceId, escrow.getSourceName());
		CachedUsername cachedTarget = new CachedUsername(targetUser.getId(), targetUsername);
		
		if(sourceId != null) {
			eventService.eventBackingCancel(cachedSource, cachedTarget);
		}
		logger.debug("Backing cancelled from (" + sourceUsername + ") with id {" + sourceId 
				+ "} to {" + targetId + "}.");
	}
	
	@Override
	public void withdrawBacking(User sourceUser, User targetUser, String targetName) 
			throws ServiceException {
		ArgCheck.nullCheck(targetName);
		ArgCheck.userCheck(sourceUser);

		String sourceUsername = sourceUser.getUsername();
		String targetUsername = ServiceUtils.getName(targetName);
		ObjectId sourceId = sourceUser.getId();
		
		ObjectId targetId = null;
		if(targetUser != null) {
			ArgCheck.userCheck(targetUser);
			targetUsername = targetUser.getUsername();
			targetId = targetUser.getId();
		}
		
		Escrow escrow = getBackerEscrow(sourceId, sourceUsername, targetId, targetUsername);

		// do not ever force this or the user may not receive their funds
		deny(escrow, false);
		
		CachedUsername cachedSource = new CachedUsername(sourceUser.getId(), sourceUsername);
		CachedUsername cachedTarget = new CachedUsername(targetId, escrow.getTargetName());
		
		if(targetId != null) {
			eventService.eventBackingWithdraw(cachedSource, cachedTarget);
		}
		logger.debug("Backing withdrawn from {" + sourceId + "} to (" 
				+ targetUsername + ") with id {" + targetId + "}.");
	}
	
	protected void deny(Escrow escrow, boolean force) 
			throws ServiceException {
		ArgCheck.nullCheck(escrow);
		
		try {
			long gold = 0;
			if(escrow.getBalance() != null) {
				if(escrow.getBalance().getGold() != null) {
					gold = escrow.getBalance().getGold();
				}
				ObjectId id = new ObjectId(escrow.getSource());
				if(gold > 0) {
					financeService.refundEscrow(escrow.getSourceTarget(), id, gold);
				}
			}
			escrowDao.cleanupEmpties(escrow.getType(), escrow.getSource(), null, null, escrow.getTargetName(), null);
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(BalanceException be) {
			try {
				if(force) {
					escrowDao.delete(escrow.getSourceTarget());
				}
			} catch(Exception dee) {
			}
			throw be;
		} catch(FinanceException fe) {
			try {
				if(force) {
					escrowDao.delete(escrow.getSourceTarget());
				}
			} catch(Exception dee) {
			}
			throw fe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	/*
	protected void deny(ESCROW_TYPE type, ObjectId sourceId, String sourceName, 
			String target, String targetName, boolean force) throws ServiceException {
		ArgCheck.nullCheck(type);
		
		String source = null;
		if(sourceId != null) {
			sourceId.toHexString();
		}
		String sourceUsername = null;
		if(sourceName != null) {
			sourceUsername = ServiceUtils.getIdName(sourceName);
		}
		String targetUsername = null;
		if(targetName != null) {
			targetUsername = ServiceUtils.getIdName(targetName);
		}
		
		try {
			Escrow escrow = escrowDao.findEscrow(type, source, sourceUsername, target, 
					targetUsername);
			if(escrow == null) {
				throw new NotFoundException(target);
			}
			
			long silver = 0;
			if(escrow.getBalance() != null) {
				if(escrow.getBalance().getSilver() != null) {
					silver = escrow.getBalance().getSilver();
				}
				ObjectId id = new ObjectId(escrow.getSource());
				financeService.refundEscrow(escrow.getSourceTarget(), id, silver);
			}
			if(force) {
				escrowDao.delete(escrow.getSourceTarget());
			}
			escrowDao.cleanupEmpties(type, source, sourceUsername, target, targetUsername);
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(BalanceException be) {
			throw be;
		} catch(FinanceException fe) {
			throw fe;
		} catch(DaoException de) {
			throw new ServiceException(de);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	*/
	
	protected BackerDTO getDTO(Escrow escrow, User source, User target) 
			throws ServiceException {
		ArgCheck.nullCheck(escrow);
		if(escrow.getBalance() == null 
				|| escrow.getBalance().getGold() == null) {
			throw new NotFoundException(escrow.getTargetName());
		}
		long gold = escrow.getBalance().getGold();
		if(gold <= 0) {
			throw new NotFoundException(escrow.getTargetName());
		}
		
		String sourceName = escrow.getSourceName();
		String targetName = escrow.getTargetName();
		
		// must exist AND match
		ObjectId sourceId = null;
		ObjectId targetId = null;
		if(source != null) {
			ObjectId escrowSource = new ObjectId(escrow.getSource());
			if(PyUtils.objectIdCompare(escrowSource, source.getId())) {
				sourceId = source.getId();
			}
		}
		
		// exceptional case where there is no target name nor user, so just get the target
		// which is an email
		if(usesObjectId(escrow)) {
			if(target != null) {
				ObjectId escrowTarget = new ObjectId(escrow.getTarget());
				if(PyUtils.objectIdCompare(escrowTarget, target.getId())) {
					targetId = target.getId();
				}
			}
		} else {
			// email as username, does not matter if user matches
			targetName = escrow.getTarget();
		}
		
		CachedUsername cachedSource = new CachedUsername(sourceId, sourceName);
		CachedUsername cachedTarget = new CachedUsername(targetId, targetName);
		
		return Mapper.mapBackerDTO(cachedSource, cachedTarget, usesObjectId(escrow), gold);
	}
	
	@Override
	public BackerDTO getBackerOutstandingDTO(User sourceUser, User targetUser, 
			String targetName) throws ServiceException {
		ArgCheck.nullCheck(targetName);
		ArgCheck.userCheck(sourceUser);
		String targetUsername = ServiceUtils.getName(targetName);
		
		ObjectId targetId = null;
		if(targetUser != null) {
			ArgCheck.userCheck(targetUser);
			targetUsername = targetUser.getUsername();
			targetId = targetUser.getId();
		}
		Escrow escrow = getBackerEscrow(sourceUser.getId(), sourceUser.getUsername(), 
				targetId, targetUsername);
		
		return getDTO(escrow, sourceUser, targetUser);
	}
	
	@Override
	public BackerDTO getBackerDTO(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceName);
		ArgCheck.userCheck(targetUser);
		String sourceUsername = ServiceUtils.getName(sourceName);
		
		ObjectId sourceId = null;
		if(sourceUser != null) {
			ArgCheck.userCheck(sourceUser);
			sourceUsername = sourceUser.getUsername();
			sourceId = sourceUser.getId();
		}
		
		Escrow escrow = getBackerEscrow(sourceId, sourceUsername, targetUser.getId(), 
				targetUser.getUsername());
		
		return getDTO(escrow, sourceUser, targetUser);
	}
	
	@Override
	public BackerDTO getOfferDTO(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException {
		ArgCheck.nullCheck(sourceName);
		ArgCheck.userCheck(targetUser);

		String sourceUsername = ServiceUtils.getName(sourceName);
		String email = ServiceUtils.getUniqueEmail(targetUser.getEmail());
		
		ObjectId sourceId = null;
		if(sourceUser != null) {
			ArgCheck.userCheck(sourceUser);
			sourceUsername = sourceUser.getUsername();
			sourceId = sourceUser.getId();
		}
		
		// true if there is an email offer, rather than only when an email offer and no offer
		boolean isEmail = false;
		
		Escrow offerEscrow = null;
		try {
			offerEscrow = getOfferEscrow(sourceId, sourceUsername, targetUser.getId(), 
					targetUser.getUsername());
		} catch(NotFoundException nfe) {
		}
		
		Escrow emailEscrow = null;
		try {
			emailEscrow = getEmailOfferEscrow(sourceId, sourceUsername, email);
		} catch(NotFoundException nfe) {
		}
		
		if(sourceId == null) {
			// user was not found, use username from the escrows
			if(emailEscrow != null) {
				sourceUsername = emailEscrow.getSourceName();
			}
			if(offerEscrow != null) {
				sourceUsername = offerEscrow.getSourceName();
			}
		}
		
		long gold = 0;
		
		if(offerEscrow != null && offerEscrow.getBalance() != null 
				&& offerEscrow.getBalance().getGold() != null) {
			gold += offerEscrow.getBalance().getGold();
			isEmail = true;
		}
		// ensure its by the same user if both exist
		if(emailEscrow != null && (offerEscrow == null 
				|| PyUtils.stringCompare(
						emailEscrow.getSource(), offerEscrow.getSource()))) {
			
			if(emailEscrow.getBalance() != null 
					&& emailEscrow.getBalance().getGold() != null) {
				gold += emailEscrow.getBalance().getGold();
			}
		}
		
		if((offerEscrow == null && emailEscrow == null) || gold <= 0L) {
			throw new NotFoundException(sourceUsername);
		}
		
		CachedUsername cachedSource = new CachedUsername(sourceId, sourceUsername);
		CachedUsername cachedTarget = new CachedUsername(targetUser.getId(), 
				targetUser.getUsername());
		
		return Mapper.mapBackerDTO(cachedSource, cachedTarget, isEmail, gold);
	}
	
	@Override
	public BackerDTO getOfferOutstandingDTO(User sourceUser, User targetUser, 
			String targetName) throws ServiceException {
		ArgCheck.nullCheck(targetName);
		ArgCheck.userCheck(sourceUser);
		String targetUsername = ServiceUtils.getName(targetName);
		
		ObjectId targetId = null;
		if(targetUser != null) {
			ArgCheck.userCheck(targetUser);
			targetUsername = targetUser.getUsername();
			targetId = targetUser.getId();
		}
		
		Escrow escrow = getOfferEscrow(sourceUser.getId(), sourceUser.getUsername(), 
				targetId, targetUsername);
		
		return getDTO(escrow, sourceUser, targetUser);
	}
	
	@Override
	public BackerDTO getEmailOfferOutstandingDTO(User sourceUser, String targetEmail) 
			throws ServiceException {
		ArgCheck.nullCheck(targetEmail);
		ArgCheck.userCheck(sourceUser);
		String email = ServiceUtils.getUniqueEmail(targetEmail);
		
		// do not retrieve user, as the userId is not necessary
		
		Escrow escrow = getEmailOfferEscrow(sourceUser.getId(), sourceUser.getUsername(), 
				email);
		
		return getDTO(escrow, sourceUser, null);
	}
	
	@Override
	public void cleanupInvalid() throws ServiceException {
		try {
			escrowDao.cleanupInvalid(null);
		} catch(DaoException e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void refundExpiredOffers() throws ServiceException {
		Date olderThanCreated = PyUtils.getOldDate(ServiceValues.ESCROW_OFFER_EXPIRY);
		Pageable pageable = new PageRequest(0, ServiceValues.ESCROW_OFFER_REFUND_BATCH_SIZE);
		Page<Escrow> offers;
		try {
			offers = escrowDao.findOffersBeforeCreated(olderThanCreated, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		if(offers == null) {
			throw new NotFoundException("Expired offers");
		}
		
		ServiceException lastException = null;
		
		for(Escrow escrow : offers.getContent()) {
			try {
				deny(escrow, false);
			} catch(ServiceException se) {
				lastException = se;
			}
		}
		if(lastException != null) {
			throw lastException;
		}
	}
}
