package com.py.py.service.impl;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.mongodb.DBObject;
import com.py.py.dao.TagDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Tag;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.subdomain.TagId;
import com.py.py.dto.out.TagDTO;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.service.TagService;
import com.py.py.service.base.BaseAggregator;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;

public class TagServiceImpl extends BaseAggregator implements TagService {

	protected static final PyLogger logger = PyLogger.getLogger(TagServiceImpl.class);
	
	@Autowired
	private TagDao tagDao;
	
	@Override
	public Page<TagDTO> getTags(String language, Pageable pageable, TIME_OPTION time) throws ServiceException {
		ArgCheck.nullCheck(pageable, time);
		String correctLanguage = ServiceUtils.getLanguageOrNull(language);
		
		Page<Tag> tags = null;
		try {
			tags = tagDao.findSorted(correctLanguage, pageable, time);
		} catch(DaoException de) {
			throw new ServiceException();
		}
		if(tags == null) {
			throw new ServiceException();
		}
		
		List<TagDTO> tagdtos = ModelFactory.<TagDTO>constructList();
		
		for(Tag t : tags.getContent()) {
			try {
				TagDTO dto = Mapper.mapTagDTO(t, canPromote(t), canComment(t));
				tagdtos.add(dto);
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping for tag!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping for tag!", e);
			}
		}
		
		return new PageImpl<TagDTO>(tagdtos, pageable, tags.getTotalElements());
	}
	
	@Override
	public Tag getCachedTag(String name, String language) throws ServiceException {
		return getTag(name, language, true);
	}
	
	@Override
	public Tag getTag(String name, String language) throws ServiceException {
		return getTag(name, language, false);
	}

	private Tag getTag(String name, String language, boolean cached) throws ServiceException {
		ArgCheck.nullCheck(name, language);
		String tagName = ServiceUtils.getTag(name);
		String correctLanguage = ServiceUtils.getLanguage(language);
		
		TagId id = new TagId();
		id.setName(tagName);
		id.setLanguage(correctLanguage);
		return getTag(id, cached);
	}
	
	@Override
	public Tag getCachedTag(TagId id) throws ServiceException {
		return getTag(id, true);
	}
	
	@Override
	public Tag getTag(TagId id) throws ServiceException {
		return getTag(id, false);
	}
	
	private Tag getTag(TagId id, boolean cached) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		TagId tagId = ServiceUtils.getTagId(id);
		
		try {
			Tag retrieved;
			if(cached) {
				retrieved = tagDao.findCachedTag(tagId.getName(), tagId.getLanguage());
			} else {
				retrieved = tagDao.findTag(tagId.getName(), tagId.getLanguage());
			}
			if(retrieved == null) {
				throw new NotFoundException(tagId.getName());
			}
			return retrieved;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public TagDTO getTagDTO(Tag tag) throws ServiceException {
		return Mapper.mapTagDTO(tag, canPromote(tag), canComment(tag));
	}
	
	@Override
	public void incrementTag(String name, String language, long value, Long appreciation) 
			throws ServiceException {
		ArgCheck.nullCheck(name, language);
		
		String correctLanguage = ServiceUtils.getLanguage(language);
		String tagName = ServiceUtils.getTag(name);
		
		try {
			tagDao.incrementTag(tagName, correctLanguage, value, appreciation);
			if(value > 0) {
				TagId id = new TagId();
				id.setName(tagName);
				id.setLanguage(correctLanguage);
				Tag tag = getTag(id);
				if(!canPromote(tag)) {
					throw new ActionNotAllowedException();
				}
				updateAggregates(id.toString(), AGGREGATION_TYPE.TAG, value);
			}
		} catch(ActionNotAllowedException anae) {
			throw anae;
		} catch(NotFoundException nfe) {
			// this probably indicates some sort of error...
			throw new ServiceException(nfe);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		logger.debug("Tag incremented (" + tagName + ") for language (" 
				+ correctLanguage + ").");
	}

	@Override
	public void incrementCommentCount(TagId id, boolean increment) 
			throws ServiceException {
		ArgCheck.nullCheck(id);
		TagId tagId = ServiceUtils.getTagId(id);
		
		try {
			if(increment) {
				tagDao.incrementCommentCount(tagId, 1);
			} else {
				tagDao.incrementCommentCount(tagId, -1);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementCommentTallyApproximation(TagId id, 
			Long appreciationIncrement, Long promotionIncrement, Long cost) 
					throws ServiceException {
		ArgCheck.nullCheck(id);
		// arguments may be null, do not update nulls

		TagId tagId = ServiceUtils.getTagId(id);
		
		try {
			if(cost != null) {
				tagDao.incrementCommentTallyCost(tagId, cost);
			}
			if(appreciationIncrement != null || promotionIncrement != null) {
				tagDao.incrementCommentTallyAppreciationPromotion(tagId, 
						appreciationIncrement, promotionIncrement);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void updateAggregate(TagId id, long value, TIME_OPTION segment) 
			throws ServiceException {
		ArgCheck.nullCheck(id, segment);
		
		TagId tagId = ServiceUtils.getTagId(id);
		
		try {
			tagDao.updateAggregation(tagId, value, segment);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void aggregateTags(TIME_OPTION segment) throws ServiceException {
		ArgCheck.nullCheck(segment);
		Iterable<DBObject> results = aggregate(AGGREGATION_TYPE.TAG, segment);

		for(DBObject obj : results) {
			@SuppressWarnings("rawtypes")
			Map map = obj.toMap();
			TagId id = new TagId();
			id.fromString((String) map.get("_id"));
			long total = (Long) map.get("total");
			updateAggregate(id, total, segment);
		}
		
		try {
			tagDao.emptyAggregations(segment);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void aggregateTotals() throws ServiceException {
		updateAggregateTotals(AGGREGATION_TYPE.TAG);
	}
	
	@Override
	public boolean canPromote(Tag tag) throws ServiceException {
		ArgCheck.tagCheck(tag);
		if(!tag.isLocked()) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean canComment(Tag tag) throws ServiceException {
		ArgCheck.tagCheck(tag);
		if(!tag.isLocked()) {
			return true;
		}
		return false;
	}
	
	@Override
	public Map<TIME_OPTION, BigInteger> getAggregateTotals() throws ServiceException {
		return getAggregateTotals(AGGREGATION_TYPE.TAG);
	}
	
}
