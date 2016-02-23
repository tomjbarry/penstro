package com.py.py.service;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.Escrow;
import com.py.py.domain.User;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.exception.ServiceException;

public interface EscrowService {

	EscrowSourceTarget getBackerEscrowId(ObjectId source, ObjectId target)
			throws ServiceException;

	void addOffer(User sourceUser, User targetUser, long amount) throws ServiceException;

	void addEmailOffer(User user, User targetUser, String targetEmail, long amount)
			throws ServiceException;

	Page<BackerDTO> getBackersOutstanding(ObjectId sourceId, Pageable pageable)
			throws ServiceException;

	Page<BackerDTO> getOffersOutstanding(ObjectId sourceId, Pageable pageable)
			throws ServiceException;

	Page<BackerDTO> getEmailOffersOutstanding(ObjectId sourceId,
			Pageable pageable) throws ServiceException;

	Page<BackerDTO> getBackers(ObjectId targetId, Pageable pageable)
			throws ServiceException;

	Page<BackerDTO> getOffers(User user, Pageable pageable)
			throws ServiceException;

	void acceptOffer(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException;

	void denyOffer(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException;

	void withdrawOffer(User sourceUser, User targetUser, String targetName) 
			throws ServiceException;

	void withdrawEmailOffer(User sourceUser, User targetUser, String targetEmail) 
			throws ServiceException;

	void cancelBacking(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException;

	BackerDTO getBackerDTO(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException;

	BackerDTO getOfferDTO(User targetUser, User sourceUser, String sourceName) 
			throws ServiceException;

	BackerDTO getEmailOfferOutstandingDTO(User sourceUser, String targetEmail)
			throws ServiceException;

	void withdrawBacking(User sourceUser, User targetUser, String targetName) 
			throws ServiceException;

	BackerDTO getBackerOutstandingDTO(User sourceUser, User targetUser, String targetName)
			throws ServiceException;

	BackerDTO getOfferOutstandingDTO(User sourceUser, User targetUser, String targetName)
			throws ServiceException;

	Escrow getBackerEscrow(ObjectId sourceId, String sourceName,
			ObjectId targetId, String targetName) throws ServiceException;

	Escrow getOfferEscrow(ObjectId sourceId, String sourceName,
			ObjectId targetId, String targetName) throws ServiceException;

	Escrow getEmailOfferEscrow(ObjectId sourceId, String sourceName,
			String email) throws ServiceException;

	void cleanupInvalid() throws ServiceException;

	void acceptEmailOffer(User targetUser, User sourceUser, String sourceName)
			throws ServiceException;

	void denyEmailOffer(User targetUser, User sourceUser, String sourceName)
			throws ServiceException;

	void refundExpiredOffers() throws ServiceException;

}
