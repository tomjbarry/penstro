package com.py.py.service.impl;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.ServiceValues;
import com.py.py.dao.CorrespondenceDao;
import com.py.py.dao.MessageDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Correspondence;
import com.py.py.domain.Message;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.subdomain.BinaryUserId;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.in.SubmitMessageDTO;
import com.py.py.dto.out.ConversationDTO;
import com.py.py.dto.out.MessageDTO;
import com.py.py.service.FollowService;
import com.py.py.service.MessageService;
import com.py.py.service.UserService;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.BlockedException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;

public class MessageServiceImpl implements MessageService {

	protected static final PyLogger logger = PyLogger.getLogger(MessageServiceImpl.class);
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private MessageDao messageDao;
	
	@Autowired
	private CorrespondenceDao correspondenceDao;
	
	@Autowired
	private FollowService followService;
	
	/*
	protected Page<MessagePreviewDTO> getMessagePreviewDTOs(User sourceUser, User targetUser,
			Pageable pageable) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		
		if(sourceUser == null && targetUser == null) {
			throw new BadParameterException();
		}
		
		ObjectId sourceId = null;
		if(sourceUser != null) {
			sourceId = sourceUser.getId();
		}
		ObjectId targetId = null;
		if(targetUser != null) {
			targetId = targetUser.getId();
		}
		
		Page<Message> messages = null;
		
		try {
			messages = messageDao.getMessages(sourceId, targetId, pageable, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		
		if(messages == null) {
			throw new ServiceException();
		}
		
		List<MessagePreviewDTO> messagedtos = ModelFactory.<MessagePreviewDTO>constructList();
		
		for(Message m : messages.getContent()) {
			try {
				MessagePreviewDTO dto = Mapper.mapMessagePreviewDTO(m);
				messagedtos.add(dto);
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping for message preview!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping for message preview!", e);
			}
		}
		
		return new PageImpl<MessagePreviewDTO>(messagedtos, pageable, messages.getTotalElements());
	}
	
	@Override
	public Page<MessagePreviewDTO> getTargetMessageDTOs(User targetUser, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(targetUser, pageable);
		ArgCheck.userCheck(targetUser);
		return getMessagePreviewDTOs(null, targetUser, pageable);
	}
	
	@Override
	public Page<MessagePreviewDTO> getSourceMessageDTOs(User sourceUser, Pageable pageable)
			throws ServiceException {
		ArgCheck.nullCheck(pageable);
		ArgCheck.userCheck(sourceUser);
		return getMessagePreviewDTOs(sourceUser, null, pageable);
	}
	*/
	
	@Override
	public Page<ConversationDTO> getConversationDTOs(User sourceUser, Pageable pageable, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		ArgCheck.userCheck(sourceUser);

		
		Page<Correspondence> correspondences = null;
		
		try {
			correspondences = correspondenceDao.getCorrespondences(sourceUser.getId(), pageable);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		
		if(correspondences == null) {
			throw new ServiceException();
		}
		
		List<ConversationDTO> conversationdtos = ModelFactory.<ConversationDTO>constructList();
		
		for(Correspondence c : correspondences.getContent()) {
			try {
				ConversationDTO dto = Mapper.mapConversationDTO(c, preview);
				conversationdtos.add(dto);
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping for correspondence!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping for correspondence!", e);
			}
		}
		
		return new PageImpl<ConversationDTO>(conversationdtos, pageable, correspondences.getTotalElements());
	}
	
	@Override
	public ConversationDTO getConversationDTO(User sourceUser, User targetUser) throws ServiceException {
		ArgCheck.userCheck(sourceUser, targetUser);
		
		Correspondence correspondence;
		BinaryUserId buId = new BinaryUserId(sourceUser.getId(), targetUser.getId());
		
		try {
			correspondence = correspondenceDao.findOne(buId);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		
		if(correspondence == null) {
			throw new NotFoundException(targetUser.getId().toHexString());
		}
		
		boolean hidden = false;
		
		if(sourceUser.getId().equals(buId.getFirst())) {
			hidden = correspondence.isFirstHidden();
		} else {
			hidden = correspondence.isSecondHidden();
		}
		
		if(hidden) {
			throw new ActionNotAllowedException();
		}
		
		return Mapper.mapConversationDTO(correspondence, false);
	}
	
	/*
	@Override
	@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p0?.getId()")
	*/
	@Override
	public Page<MessageDTO> getMessageDTOs(User user, User otherUser, 
			Pageable pageable, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		ArgCheck.userCheck(user, otherUser);
		
		Page<Message> messages = null;
		
		try {
			messages = messageDao.getMessagesAll(user.getId(), otherUser.getId(), 
					pageable);
			messageDao.mark(otherUser.getId(), user.getId(), true);
		} catch(DaoException de) {
			throw new ServiceException(de);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		
		if(messages == null) {
			throw new ServiceException();
		}
		
		List<MessageDTO> messagedtos = ModelFactory.<MessageDTO>constructList();
		
		for(Message m : messages.getContent()) {
			try {
				MessageDTO dto = Mapper.mapMessageDTO(m, preview);
				messagedtos.add(dto);
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping for message!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping for message!", e);
			}
		}
		
		return new PageImpl<MessageDTO>(messagedtos, pageable, messages.getTotalElements());
	}

	/*
	@Override
	@CacheEvict(value = CacheNames.USER_CURRENT, key = "#p1?.getId()")
	*/
	@Override
	public ObjectId createMessage(User sourceUser, User targetUser, SubmitMessageDTO dto)
			throws ServiceException {
		ArgCheck.nullCheck(dto);
		ArgCheck.userCheck(sourceUser, targetUser);
		
		Date created = new Date();
		
		if(PyUtils.objectIdCompare(sourceUser.getId(), targetUser.getId())) {
			throw new ActionNotAllowedException();
		}
		
		if(followService.isBlocked(targetUser.getId(), sourceUser.getId())) {
			throw new BlockedException();
		}
		
		CachedUsername source = new CachedUsername(sourceUser.getId(), 
				sourceUser.getUsername());
		CachedUsername target = new CachedUsername(targetUser.getId(), 
				targetUser.getUsername());
		
		Message message = Mapper.mapMessage(dto, source, target, false, created);
		
		try {
			message = messageDao.save(message);
			logger.debug("Created message for user {" + sourceUser.getId().toHexString() 
					+ "} to user {" + targetUser.getId().toHexString() + "}.");
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		
		try {
			correspondenceDao.update(source, target, message.getMessage());
			logger.debug("Updated correspondence for user {" + sourceUser.getId().toHexString() 
					+ "} to user {" + targetUser.getId().toHexString() + "}.");
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		return message.getId();
	}
	
	@Override
	public long getMessageCount(User targetUser, Boolean read) throws ServiceException {
		ArgCheck.userCheck(targetUser);
		
		try {
			Page<Message> messages = messageDao.getMessages(null, targetUser.getId(),
					ModelFactory.constructEmptyPageable(), read);
			if(messages == null) {
				throw new ServiceException();
			}
			return messages.getTotalElements();
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void flagConversation(User targetUser, UserInfo targetUserInfo, 
			User sourceUser, UserInfo sourceUserInfo) throws ServiceException {
		ArgCheck.nullCheck(targetUserInfo, sourceUserInfo);
		ArgCheck.userCheck(targetUser, sourceUser);
		
		try {
			if(PyUtils.objectIdCompare(sourceUser.getId(), targetUser.getId())) {
				throw new ActionNotAllowedException();
			}
			
			userService.flag(targetUser, targetUserInfo, sourceUser, sourceUserInfo, null);
		} catch(NotFoundException nfe) {
			// user was deleted, exact name match as target needed
		}
		
		try {
			correspondenceDao.updateStatus(targetUser.getId(), sourceUser.getId(), false);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	// user order matters, but not whether it is source or target
	@Override
	public void toggleShowConversation(User user, User otherUser, boolean show) throws ServiceException {
		ArgCheck.userCheck(user, otherUser);

		// do not check for same id. If the option is changed to allow sending messages to self, allow user to hide
		
		try {
			correspondenceDao.updateStatus(user.getId(), otherUser.getId(), !show);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeExpiredCorrespondences() throws ServiceException {
		Date then = PyUtils.getOldDate(ServiceValues.CORRESPONDENCE_EXPIRY);
		
		try {
			correspondenceDao.removeExpired(then);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
}
