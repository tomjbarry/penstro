package com.py.py.service;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.dto.in.SubmitMessageDTO;
import com.py.py.dto.out.ConversationDTO;
import com.py.py.dto.out.MessageDTO;
import com.py.py.service.exception.ServiceException;

public interface MessageService {
	/*
	Page<MessagePreviewDTO> getTargetMessageDTOs(User targetUser, Pageable pageable)
			throws ServiceException;
	
	Page<MessagePreviewDTO> getSourceMessageDTOs(User sourceUser, Pageable pageable)
			throws ServiceException;
	*/
	long getMessageCount(User targetUser, Boolean read)
			throws ServiceException;
	
	ObjectId createMessage(User sourceUser, User targetUser, SubmitMessageDTO dto)
			throws ServiceException;
	
	void flagConversation(User targetUser, UserInfo targetUserInfo,
			User sourceUser, UserInfo sourceUserInfo) throws ServiceException;

	void toggleShowConversation(User user, User otherUser, boolean show)
			throws ServiceException;

	Page<ConversationDTO> getConversationDTOs(User sourceUser, Pageable pageable, boolean preview)
			throws ServiceException;

	void removeExpiredCorrespondences() throws ServiceException;

	ConversationDTO getConversationDTO(User sourceUser, User targetUser)
			throws ServiceException;

	Page<MessageDTO> getMessageDTOs(User user, User otherUser, Pageable pageable, boolean preview)
		throws ServiceException;

}
