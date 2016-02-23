package com.py.py.service.impl;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.Escrow;
import com.py.py.domain.User;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.EscrowService;
import com.py.py.service.exception.FeatureDisabledException;
import com.py.py.service.exception.ServiceException;

public class EscrowServiceDisabledImpl implements EscrowService {

	@Override
	public EscrowSourceTarget getBackerEscrowId(ObjectId source, ObjectId target) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void addOffer(User sourceUser, User targetUser, long amount) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void addEmailOffer(User user, User targetUser, String targetEmail, long amount) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public Page<BackerDTO> getBackersOutstanding(ObjectId sourceId, Pageable pageable) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public Page<BackerDTO> getOffersOutstanding(ObjectId sourceId, Pageable pageable) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public Page<BackerDTO> getEmailOffersOutstanding(ObjectId sourceId, Pageable pageable) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public Page<BackerDTO> getBackers(ObjectId targetId, Pageable pageable) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public Page<BackerDTO> getOffers(User user, Pageable pageable) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void acceptOffer(User targetUser, User sourceUser, String sourceName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void denyOffer(User targetUser, User sourceUser, String sourceName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void withdrawOffer(User sourceUser, User targetUser, String targetName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void withdrawEmailOffer(User sourceUser, User targetUser, String targetEmail) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void cancelBacking(User targetUser, User sourceUser, String sourceName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public BackerDTO getBackerDTO(User targetUser, User sourceUser, String sourceName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public BackerDTO getOfferDTO(User targetUser, User sourceUser, String sourceName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public BackerDTO getEmailOfferOutstandingDTO(User sourceUser, String targetEmail) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void withdrawBacking(User sourceUser, User targetUser, String targetName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public BackerDTO getBackerOutstandingDTO(User sourceUser, User targetUser, String targetName)
			throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public BackerDTO getOfferOutstandingDTO(User sourceUser, User targetUser, String targetName)
			throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public Escrow getBackerEscrow(ObjectId sourceId, String sourceName, ObjectId targetId, String targetName)
			throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public Escrow getOfferEscrow(ObjectId sourceId, String sourceName, ObjectId targetId, String targetName)
			throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public Escrow getEmailOfferEscrow(ObjectId sourceId, String sourceName, String email) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void cleanupInvalid() throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void acceptEmailOffer(User targetUser, User sourceUser, String sourceName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void denyEmailOffer(User targetUser, User sourceUser, String sourceName) throws ServiceException {
		throw new FeatureDisabledException();
	}

	@Override
	public void refundExpiredOffers() throws ServiceException {
		throw new FeatureDisabledException();
	}

}
