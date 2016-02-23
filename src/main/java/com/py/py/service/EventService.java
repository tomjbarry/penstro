package com.py.py.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.out.FeedDTO;
import com.py.py.dto.out.FollowDTO;
import com.py.py.dto.out.NotificationDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.service.exception.ServiceException;

public interface EventService {
	void eventMessage(CachedUsername source, CachedUsername target) throws ServiceException;

	Page<NotificationDTO> getNotificationDTOs(ObjectId id,
			List<EVENT_TYPE> types, Pageable pageable) throws ServiceException;
	
	// specific events
	void eventAppreciationPosting(CachedUsername source,
			CachedUsername beneficiary, ObjectId postingId,
			CachedUsername postingAuthor, String postingTitle,
			double appreciation) throws ServiceException;
	
	void eventOffer(CachedUsername source, CachedUsername target, long amount)
			throws ServiceException;
	
	void eventPosting(CachedUsername source, CachedUsername beneficiary,
			String title, ObjectId postingId, long cost) throws ServiceException;

	void eventOfferAccept(CachedUsername source, CachedUsername target)
			throws ServiceException;

	void eventOfferDeny(CachedUsername source, CachedUsername target) 
			throws ServiceException;

	void eventOfferWithdraw(CachedUsername source, CachedUsername target)
			throws ServiceException;

	void eventBackingCancel(CachedUsername source, CachedUsername target)
			throws ServiceException;

	void eventFollowAdd(CachedUsername source, CachedUsername target) 
			throws ServiceException;

	void eventFollowRemove(CachedUsername source, CachedUsername target)
			throws ServiceException;

	Page<FollowDTO> getFollowerEvents(ObjectId id, Pageable pageable)
			throws ServiceException;

	long getNotificationCount(ObjectId id, List<EVENT_TYPE> types,
			Date lastChecked) throws ServiceException;

	void createEvent(CachedUsername author, CachedUsername target,
			CachedUsername beneficiary, ObjectId baseId, String baseString,
			COMMENT_TYPE baseType, ObjectId primaryId, ObjectId parentId,
			EVENT_TYPE type, Map<String, String> targets)
			throws ServiceException;

	void eventAppreciationComment(CachedUsername source,
			CachedUsername beneficiary, ObjectId commentId, ObjectId baseId,
			String baseString, COMMENT_TYPE type, CachedUsername commentAuthor,
			String title, double cost) throws ServiceException;

	void eventSubComment(CachedUsername source, CachedUsername beneficiary,
			ObjectId commentId, ObjectId baseId, String baseString,
			COMMENT_TYPE type, CachedUsername parentAuthor,
			ObjectId parentCommentId, String title, long cost)
			throws ServiceException;

	void eventComment(CachedUsername source, CachedUsername beneficiary,
			ObjectId commentId, ObjectId baseId, String baseString,
			COMMENT_TYPE type, String title, CachedUsername baseTarget,
			long cost) throws ServiceException;

	void eventPromotionPosting(CachedUsername source,
			CachedUsername beneficiary, ObjectId postingId,
			CachedUsername postingAuthor, String postingTitle, long promotion)
			throws ServiceException;

	void eventPromotionComment(CachedUsername source,
			CachedUsername beneficiary, ObjectId commentId, ObjectId baseId,
			String baseString, COMMENT_TYPE type, CachedUsername commentAuthor,
			String title, long promotion) throws ServiceException;

	void removeUser(ObjectId userId) throws ServiceException;

	long getFeedCount(ObjectId userId, List<ObjectId> ids,
			List<EVENT_TYPE> types, Date lastChecked) throws ServiceException;

	Page<FeedDTO> getFeedDTOs(List<ObjectId> ids, List<EVENT_TYPE> types,
			boolean hidden, Pageable pageable) throws ServiceException;

	Page<FeedDTO> checkFeedDTOs(ObjectId userId, List<ObjectId> ids,
			List<EVENT_TYPE> types, boolean hidden, Pageable pageable)
			throws ServiceException;

	void eventBackingWithdraw(CachedUsername source, CachedUsername target)
			throws ServiceException;

	void rename(ObjectId userId, String username, String replacement)
			throws ServiceException;

	void eventPostingInfringement(CachedUsername target,
			CachedUsername beneficiary, String title, ObjectId postingId)
			throws ServiceException;

	void eventCommentInfringement(CachedUsername target,
			CachedUsername beneficiary, ObjectId commentId, ObjectId baseId,
			String baseString, COMMENT_TYPE type) throws ServiceException;

	void eventAppreciationAttempt(CachedUsername source, CachedUsername target, double appreciation)
		throws ServiceException;
}
