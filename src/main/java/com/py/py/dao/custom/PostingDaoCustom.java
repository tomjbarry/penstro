package com.py.py.dao.custom;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Posting;
import com.py.py.domain.subdomain.ImageLink;
import com.py.py.domain.subdomain.TimeSumAggregate;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;

public interface PostingDaoCustom {

	void incrementCommentCount(ObjectId id, long count) throws DaoException;

	void incrementCommentTallyCost(ObjectId id, Long cost) throws DaoException;

	void setAggregate(ObjectId id, TimeSumAggregate agg) throws DaoException;

	Posting findPosting(ObjectId id) throws DaoException;

	void updateAggregation(ObjectId id, long value, TIME_OPTION segment)
			throws DaoException;

	void emptyAggregations(TIME_OPTION segment) throws DaoException;

	void addWarn(ObjectId id, Boolean warn, long amount) throws DaoException;

	void addVote(ObjectId id, ObjectId userId, long weight) throws DaoException;

	void incrementCommentTallyAppreciationPromotion(ObjectId id,
			Long appreciation, Long promotion) throws DaoException;

	void incrementAppreciationPromotionCount(ObjectId id,
			Long appreciationCount, Long promotionCount) throws DaoException;

	void rename(ObjectId userId, String replacement, boolean asAuthor)
			throws DaoException;

	void removeUser(ObjectId userId, boolean asAuthor) throws DaoException;

	Page<Posting> getSortedPostings(String language, ObjectId authorId, Pageable pageable,
			Filter filter) throws DaoException;

	Page<Posting> getUserPostings(ObjectId authorId, ObjectId beneficiaryId,
			Pageable pageable, boolean showDisabled, List<String> tags, Boolean warning)
			throws DaoException;

	void setEnabled(ObjectId id, Boolean enabled, Boolean removed,
			Boolean flagged, Boolean warning, Boolean paid, Boolean initialized)
			throws DaoException;

	Page<Posting> getPostingsByLastPromotion(Date lastPromotion,
			Pageable pageable) throws DaoException;

	void incrementTagValues(ObjectId id, Map<String, Long> tags)
			throws DaoException;

	void setTags(ObjectId id, List<String> tags) throws DaoException;

	Posting findCachedPosting(ObjectId id) throws DaoException;

	void markArchived(Date olderThanCreated) throws DaoException;

	void updatePosting(ObjectId id, String title, String content, String preview, ImageLink imageLink)
			throws DaoException;


}
