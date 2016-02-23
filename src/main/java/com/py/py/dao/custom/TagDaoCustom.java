package com.py.py.dao.custom;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Tag;
import com.py.py.domain.subdomain.TagId;
import com.py.py.enumeration.TIME_OPTION;

public interface TagDaoCustom {

	void incrementCommentCount(TagId id, long count) throws DaoException;

	void incrementCommentTallyCost(TagId id, Long cost) 
			throws DaoException;

	Page<Tag> findSorted(String language, Pageable pageable, TIME_OPTION time)
			throws DaoException;

	void updateAggregation(TagId id, long value, TIME_OPTION segment)
			throws DaoException;

	void emptyAggregations(TIME_OPTION segment) throws DaoException;

	void updateTag(String name, String language, boolean locked) throws DaoException;

	void incrementCommentTallyAppreciationPromotion(TagId id, Long appreciation,
			Long promotion) throws DaoException;

	Tag findTag(String name, String language) throws DaoException;

	Tag findCachedTag(String name, String language) throws DaoException;

	void incrementTag(String name, String language, Long amount,
			Long appreciation) throws DaoException;

}
