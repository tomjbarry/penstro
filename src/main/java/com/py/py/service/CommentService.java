package com.py.py.service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.Comment;
import com.py.py.domain.Posting;
import com.py.py.domain.Tag;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.dto.in.PromoteCommentDTO;
import com.py.py.dto.in.SubmitCommentDTO;
import com.py.py.dto.in.SubmitEditCommentDTO;
import com.py.py.dto.out.CommentDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.service.exception.ServiceException;

public interface CommentService {

	Comment getComment(ObjectId cid) throws ServiceException;

	void incrementReplyCount(ObjectId id, boolean increment) throws ServiceException;

	void updateAggregate(ObjectId cid, long value, TIME_OPTION segment)
			throws ServiceException;

	void aggregateComments(TIME_OPTION segment) throws ServiceException;

	void enableComment(User user, Comment comment) throws ServiceException;

	void disableComment(User user, Comment comment) throws ServiceException;

	boolean canAppreciate(Comment comment) throws ServiceException;

	boolean canComment(Comment comment) throws ServiceException;

	CommentDTO getCommentDTO(ObjectId authorId, Comment comment, Boolean warning)
			throws ServiceException;

	void incrementReplyTallyApproximation(ObjectId id,
			Long appreciationIncrement, Long promotionIncrement, Long cost)
			throws ServiceException;

	void promoteComment(User user, Comment comment, PromoteCommentDTO dto)
			throws ServiceException;

	void incrementAppreciationPromotionCount(ObjectId id, boolean appreciation,
			boolean increment) throws ServiceException;

	void appreciateComment(ObjectId paymentId, User user, Comment comment,
			long appreciationAmount, long promotionAmount, boolean warning)
			throws ServiceException;

	Map<TIME_OPTION, BigInteger> getAggregateTotals() throws ServiceException;

	void aggregateTotals() throws ServiceException;

	ResultSuccessDTO createCommentDTO(User user, Comment parent,
			Posting basePosting, UserInfo baseUserInfo, Tag baseTag,
			COMMENT_TYPE type, SubmitCommentDTO dto, String language)
			throws ServiceException;

	Comment getCachedComment(ObjectId id) throws ServiceException;

	void markArchived() throws ServiceException;

	void flag(User user, UserInfo userInfo, Comment comment, FLAG_REASON reason)
			throws ServiceException;

	void unremoveComment(Comment comment) throws ServiceException;

	void removeComment(Comment comment, boolean sendEvent)
			throws ServiceException;

	Page<CommentDTO> getCommentPreviewDTOs(String language, ObjectId authorId, Pageable pageable, Filter filter,
		List<COMMENT_TYPE> commentTypes, boolean preview) throws ServiceException;

	Page<CommentDTO> getAuthorPreviewDTOs(ObjectId authorId, Pageable pageable, Boolean warning, boolean preview)
		throws ServiceException;

	Page<CommentDTO> getBeneficiaryPreviewDTOs(ObjectId beneficiaryId, Pageable pageable, Boolean warning,
		boolean preview) throws ServiceException;

	Page<CommentDTO> getSelfPreviewDTOs(ObjectId userId, Pageable pageable, boolean preview) throws ServiceException;

	Page<CommentDTO> getCommentDTOs(ObjectId baseId, String baseString, COMMENT_TYPE type, String language,
		Pageable pageable, Filter filter, boolean preview) throws ServiceException;

	Page<CommentDTO> getSubCommentDTOs(ObjectId parentId, String language, Pageable pageable, Filter filter,
		boolean preview) throws ServiceException;

	Page<CommentDTO> getCommentDTOs(ObjectId baseId, String baseString, COMMENT_TYPE type, ObjectId parentId,
		String language, Pageable pageable, Filter filter, boolean preview) throws ServiceException;

	void editComment(ObjectId userId, Comment comment, SubmitEditCommentDTO dto) throws ServiceException;

}
