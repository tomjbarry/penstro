package com.py.py.service;

import java.math.BigInteger;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.py.py.domain.Tag;
import com.py.py.domain.subdomain.TagId;
import com.py.py.dto.out.TagDTO;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.service.exception.ServiceException;

public interface TagService {
	
	TagDTO getTagDTO(Tag tag) throws ServiceException;

	Page<TagDTO> getTags(String language, Pageable pageable, TIME_OPTION time)
			throws ServiceException;

	void updateAggregate(TagId id, long value, TIME_OPTION segment)
			throws ServiceException;

	void aggregateTags(TIME_OPTION segment) throws ServiceException;

	boolean canPromote(Tag tag) throws ServiceException;

	boolean canComment(Tag tag) throws ServiceException;
	
	Map<TIME_OPTION, BigInteger> getAggregateTotals() throws ServiceException;

	void aggregateTotals() throws ServiceException;

	void incrementCommentCount(TagId id, boolean increment)
			throws ServiceException;

	void incrementCommentTallyApproximation(TagId id,
			Long appreciationIncrement, Long promotionIncrement, Long cost)
			throws ServiceException;

	Tag getTag(TagId id) throws ServiceException;

	Tag getTag(String name, String language) throws ServiceException;

	Tag getCachedTag(String name, String language) throws ServiceException;

	Tag getCachedTag(TagId id) throws ServiceException;

	void incrementTag(String name, String language, long value,
			Long appreciation) throws ServiceException;

}
