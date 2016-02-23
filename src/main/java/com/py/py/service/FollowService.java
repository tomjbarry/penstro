package com.py.py.service;

import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.User;
import com.py.py.domain.subdomain.FollowInfo;
import com.py.py.dto.out.FeedDTO;
import com.py.py.dto.out.FollowDTO;
import com.py.py.dto.out.SubscriptionDTO;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.service.exception.ServiceException;

public interface FollowService {

	void addFollowee(User user, User followee) throws ServiceException;

	void create(User user) throws ServiceException;

	Page<FollowDTO> getFolloweeDTOs(ObjectId id, Pageable pageable)
			throws ServiceException;

	Page<FollowDTO> getFollowerDTOs(ObjectId id, Pageable pageable)
			throws ServiceException;

	List<EVENT_TYPE> getHiddenFeedEvents(ObjectId id) throws ServiceException;

	FollowDTO getFolloweeDTO(ObjectId id, ObjectId followeeId)
			throws ServiceException;

	FollowDTO getFollowerDTO(ObjectId id, ObjectId followerId)
			throws ServiceException;

	Page<FeedDTO> getFeedDTOs(ObjectId id, List<EVENT_TYPE> types,
			Pageable pageable) throws ServiceException;

	FollowInfo getFollowee(ObjectId id, ObjectId followeeId)
			throws ServiceException;

	void addBlocked(User user, User blocked) throws ServiceException;

	void removeBlocked(ObjectId id, String blockedName) throws ServiceException;

	boolean isBlocked(ObjectId id, ObjectId blockedId) throws ServiceException;

	Page<FollowDTO> getBlockedDTOs(ObjectId id, Pageable pageable)
			throws ServiceException;

	List<FollowInfo> getBlockedList(ObjectId id) throws ServiceException;

	FollowDTO getBlockedDTO(ObjectId id, ObjectId blockedId)
			throws ServiceException;

	FollowInfo getBlocked(ObjectId id, ObjectId blockedId)
			throws ServiceException;

	void updateHiddenFeed(ObjectId id, List<EVENT_TYPE> hiddenFeed)
			throws ServiceException;

	void removeFollowee(User user, User followee, String followName)
			throws ServiceException;

	List<FollowInfo> getFolloweeList(ObjectId id) throws ServiceException;

	List<ObjectId> getFolloweeIds(ObjectId id) throws ServiceException;

	Page<FeedDTO> getUserFeedDTOs(ObjectId targetId, List<EVENT_TYPE> types,
			Pageable pageable) throws ServiceException;

	long getFolloweeCount(ObjectId id) throws ServiceException;

	long getBlockedCount(ObjectId id) throws ServiceException;

	SubscriptionDTO getSubscription(ObjectId id) throws ServiceException;

}
