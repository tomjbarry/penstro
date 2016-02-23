package com.py.py.dao.custom;

import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Comment;
import com.py.py.domain.subdomain.TimeSumAggregate;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;

public interface CommentDaoCustom {

	void incrementReplyCount(ObjectId id, long count) throws DaoException;

	void incrementReplyTallyCost(ObjectId id, Long cost) throws DaoException;

	void setAggregate(ObjectId id, TimeSumAggregate agg) throws DaoException;

	Comment findComment(ObjectId id) throws DaoException;

	void updateAggregation(ObjectId id, long value, TIME_OPTION segment)
			throws DaoException;

	void emptyAggregations(TIME_OPTION segment) throws DaoException;

	void addWarn(ObjectId id, Boolean warn, long amount) throws DaoException;

	void addVote(ObjectId id, ObjectId userId, long weight) throws DaoException;

	void incrementAppreciationPromotionCount(ObjectId id,
			Long appreciationCount, Long promotionCount) throws DaoException;

	void rename(ObjectId userId, String replacement, boolean asAuthor)
			throws DaoException;

	void removeUser(ObjectId userId, boolean asAuthor) throws DaoException;
	
	Page<Comment> getUserComments(ObjectId authorId, ObjectId beneficiaryId,
			Pageable pageable, boolean showDisabled, Boolean warning) throws DaoException;

	void setEnabled(ObjectId id, Boolean enabled, Boolean removed,
			Boolean flagged, Boolean warning, Boolean paid, Boolean initialized)
			throws DaoException;

	Page<Comment> getSortedComments(List<String> types, String language,
			ObjectId authorId, Pageable pageable, Filter filter)
			throws DaoException;

	Page<Comment> getSortedReplyComments(ObjectId baseId, String baseString,
			List<String> types, ObjectId parentId, boolean noSubComments,
			String language, Pageable pageable, Filter filter)
			throws DaoException;

	Comment findCachedComment(ObjectId id) throws DaoException;

	void markArchived(Date olderThanCreated) throws DaoException;

	void incrementReplyTallyAppreciationPromotion(ObjectId id,
			Long appreciation, Long promotion) throws DaoException;

	void updateComment(ObjectId id, String content) throws DaoException;

}
