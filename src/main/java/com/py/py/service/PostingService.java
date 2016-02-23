package com.py.py.service;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.Posting;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.dto.in.PromotePostingDTO;
import com.py.py.dto.in.SubmitEditPostingDTO;
import com.py.py.dto.in.SubmitPostingDTO;
import com.py.py.dto.out.PostingDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.dto.out.TotalValueDTO;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.service.exception.ServiceException;

public interface PostingService {

	Posting getPosting(ObjectId postingId) throws ServiceException;

	void incrementCommentCount(ObjectId id, boolean increment)
			throws ServiceException;

	ResultSuccessDTO createPostingDTO(User user, SubmitPostingDTO dto, String language)
			throws ServiceException;

	void updateAggregate(ObjectId id, long value, TIME_OPTION segment)
			throws ServiceException;

	void aggregatePostings(TIME_OPTION segment) throws ServiceException;

	void enablePosting(User user, Posting posting) throws ServiceException;

	void disablePosting(User user, Posting posting) throws ServiceException;

	boolean canAppreciate(Posting posting) throws ServiceException;

	boolean canComment(Posting posting) throws ServiceException;

	PostingDTO getPostingDTO(ObjectId authorId, Posting posting, Boolean warning)
			throws ServiceException;

	void incrementCommentTallyApproximation(ObjectId postingId,
			Long appreciationIncrement, Long promotionIncrement, Long cost)
			throws ServiceException;

	void promotePosting(User user, Posting posting, PromotePostingDTO dto)
			throws ServiceException;

	void incrementAppreciationPromotionCount(ObjectId postingId,
			boolean appreciation, boolean increment) throws ServiceException;

	void appreciatePosting(ObjectId paymentId, User user, Posting posting,
			long appreciationAmount, long promotionAmount, List<String> tags,
			boolean warning) throws ServiceException;

	Map<TIME_OPTION, BigInteger> getAggregateTotals() throws ServiceException;

	TotalValueDTO getTotalValueDTO() throws ServiceException;

	void aggregateTotals() throws ServiceException;

	ObjectId createPosting(User user, SubmitPostingDTO dto, String language)
			throws ServiceException;

	void sortTags(Date lastPromotion) throws ServiceException;

	void editPosting(ObjectId userId, Posting posting, SubmitEditPostingDTO dto)
			throws ServiceException;

	Posting getCachedPosting(ObjectId postingId) throws ServiceException;

	void markArchived() throws ServiceException;

	void flag(User user, UserInfo userInfo, Posting posting, FLAG_REASON reason)
			throws ServiceException;

	void incrementPostingTags(ObjectId postingId, Set<String> tags,
			long amount, Long appreciationAmount, String language)
			throws ServiceException;

	void unremovePosting(Posting posting) throws ServiceException;

	void removePosting(Posting posting, boolean sendEvent)
			throws ServiceException;

	Page<PostingDTO> getPostingPreviews(String language, ObjectId authorId, Pageable pageable, Filter filter,
		boolean preview) throws ServiceException;

	Page<PostingDTO> getAuthorPreviews(ObjectId authorId, Pageable pageable, List<String> tags, Boolean warning,
		boolean preview) throws ServiceException;

	Page<PostingDTO> getBeneficiaryPreviews(ObjectId beneficiaryId, Pageable pageable, List<String> tags,
		Boolean warning, boolean preview) throws ServiceException;

	Page<PostingDTO> getSelfPreviews(ObjectId userId, Pageable pageable, List<String> tags, boolean preview)
		throws ServiceException;
	
}
