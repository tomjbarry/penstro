package com.py.py.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.mongodb.AggregationOutput;
import com.mongodb.DBObject;
import com.py.py.dao.AggregationDao;
import com.py.py.dao.TagDao;
import com.py.py.domain.Tag;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.subdomain.TagId;
import com.py.py.domain.subdomain.TallyApproximation;
import com.py.py.domain.subdomain.TimeSumAggregate;
import com.py.py.dto.out.TagDTO;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;

public class TagServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("tagService")
	private TagService tagService;
	
	@Autowired
	private TagDao tagDao;
	
	@Autowired
	private AggregationDao aggregationDao;
	
	private List<Tag> validTags = Arrays.asList(createValidTag(), createValidTag(), createValidTag());
	private Page<Tag> validTagPage = new PageImpl<Tag>(validTags);
	
	@SuppressWarnings("unchecked")
	@Before
	public void setUp() {
		Mockito.reset(tagDao, aggregationDao);
	}

	protected Tag createValidTag() {
		Tag tag = new Tag();
		tag.setId(createValidTagId());
		tag.setLocked(false);
		tag.setAggregate(new TimeSumAggregate());
		tag.setCommentTally(new TallyApproximation());
		tag.setValue(0L);
		return tag;
	}
	
	protected TagId createValidTagId() {
		TagId tagId = new TagId();
		tagId.setName(validTag);
		tagId.setLanguage(validLanguage);
		return tagId;
	}
	
	@Test(expected = BadParameterException.class)
	public void getTagsNull2() throws Exception {
		tagService.getTags(validLanguage, null, TIME_OPTION.ALLTIME);
	}

	@Test(expected = BadParameterException.class)
	public void getTagsNull3() throws Exception {
		tagService.getTags(validLanguage, constructPageable(), null);
	}

	@Test(expected = BadParameterException.class)
	public void getTagsInvalid() throws Exception {
		tagService.getTags(invalidLanguage, constructPageable(), TIME_OPTION.ALLTIME);
	}
	
	@Test
	public void getTagsInvalidList() throws Exception {
		Page<Tag> invalidTagPage = new PageImpl<Tag>(addNullToList(validTags));
		Mockito.when(tagDao.findSorted(Mockito.anyString(), Mockito.<Pageable>any(), 
				Mockito.<TIME_OPTION>any()))
			.thenReturn(invalidTagPage);
		
		Page<TagDTO> result = tagService.getTags(validLanguage, constructPageable(), 
				TIME_OPTION.DAY);
		Assert.assertEquals(result.getContent().size(), validTags.size());
	}
	
	@Test
	public void getTags() throws Exception {
		Mockito.when(tagDao.findSorted(Mockito.anyString(), Mockito.<Pageable>any(), 
				Mockito.<TIME_OPTION>any()))
			.thenReturn(validTagPage);
		
		Page<TagDTO> result = tagService.getTags(validLanguage, constructPageable(), 
				TIME_OPTION.DAY);
		Assert.assertEquals(result.getContent().size(), validTags.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getTagByIdNull() throws Exception {
		tagService.getTag(null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getTagByIdNotFound() throws Exception {
		Mockito.when(tagDao.findTag(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(null);
		tagService.getTag(createValidTagId());
	}
	
	@Test
	public void getTagById() throws Exception {
		Mockito.when(tagDao.findTag(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(createValidTag());
		tagService.getTag(createValidTagId());
	}
	
	@Test(expected = BadParameterException.class)
	public void getTagNull1() throws Exception {
		tagService.getTag(null, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void getTagNull2() throws Exception {
		tagService.getTag(validTag, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getTagInvalid1() throws Exception {
		tagService.getTag(invalidTag, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void getTagInvalid2() throws Exception {
		tagService.getTag(validTag, invalidLanguage);
	}
	
	@Test(expected = NotFoundException.class)
	public void getTagNotFound() throws Exception {
		Mockito.when(tagDao.findTag(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(null);
		
		tagService.getTag(validTag, validLanguage);
	}
	
	@Test
	public void getTag() throws Exception {
		Tag tag = createValidTag();
		Mockito.when(tagDao.findTag(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(tag);
	
		tagService.getTag(validTag, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementTagNull1() throws Exception {
		tagService.incrementTag(null, validLanguage, 1L, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementTagNull2() throws Exception {
		tagService.incrementTag(validTag, null, 1L, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementTagInvalid1() throws Exception {
		tagService.incrementTag(invalidTag, validLanguage, 1L, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementTagInvalid2() throws Exception {
		tagService.incrementTag(validTag, invalidLanguage, 1L, 1L);
	}
	
	@Test(expected = ServiceException.class)
	public void incrementTagNotFound() throws Exception {
		Mockito.when(tagDao.findTag(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(null);
		tagService.incrementTag(validTag, validLanguage, 1L, 1L);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void incrementTagNotAllowed() throws Exception {
		Tag tag = createValidTag();
		tag.setLocked(true);
		Mockito.when(tagDao.findTag(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(tag);
		tagService.incrementTag(validTag, validLanguage, 1L, 1L);
	}
	
	@Test
	public void incrementTag() throws Exception {
		Tag tag = createValidTag();
		tag.setLocked(false);
		Mockito.when(tagDao.findTag(Mockito.anyString(), Mockito.anyString()))
			.thenReturn(tag);
		tagService.incrementTag(validTag, validLanguage, 1L, 1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementCommentCountNull() throws Exception {
		tagService.incrementCommentCount(null, randomBoolean());
	}
	
	@Test
	public void incrementCommentCount() throws Exception {
		TagId tagId = createValidTagId();
		tagService.incrementCommentCount(tagId, true);
		tagService.incrementCommentCount(tagId, false);
		Mockito.verify(tagDao, Mockito.atLeastOnce())
			.incrementCommentCount(tagId, 1);
		Mockito.verify(tagDao, Mockito.atLeastOnce())
			.incrementCommentCount(tagId, -1);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementCommentTallyApproximationNull() throws Exception {
		tagService.incrementCommentTallyApproximation(null, validAppreciation, validCost, validCost);
	}
	
	@Test
	public void incrementCommentTallyApproximation() throws Exception {
		tagService.incrementCommentTallyApproximation(createValidTagId(), null, null, null);
		tagService.incrementCommentTallyApproximation(createValidTagId(), null, null, validCost);
		tagService.incrementCommentTallyApproximation(createValidTagId(), null, validCost, null);
		tagService.incrementCommentTallyApproximation(createValidTagId(), null, validCost, validCost);
		tagService.incrementCommentTallyApproximation(createValidTagId(), validAppreciation, null, null);
		tagService.incrementCommentTallyApproximation(createValidTagId(), validAppreciation, null, validCost);
		tagService.incrementCommentTallyApproximation(createValidTagId(), validAppreciation, validCost, 
				null);
		tagService.incrementCommentTallyApproximation(createValidTagId(), validAppreciation, validCost, 
				validCost);
		Mockito.verify(tagDao, Mockito.times(6)).incrementCommentTallyAppreciationPromotion(
				Mockito.<TagId>any(), Mockito.anyLong(), Mockito.anyLong());
		Mockito.verify(tagDao, Mockito.times(4)).incrementCommentTallyCost(
				Mockito.<TagId>any(), Mockito.anyLong());
	}
	
	@Test(expected = BadParameterException.class)
	public void updateAggregateNull1() throws Exception {
		tagService.updateAggregate(null, 1L, TIME_OPTION.ALLTIME);
	}
	
	@Test(expected = BadParameterException.class)
	public void updateAggregateNull3() throws Exception {
		tagService.updateAggregate(createValidTagId(), 1L, null);
	}
	
	@Test
	public void updateAggregate() throws Exception {
		tagService.updateAggregate(createValidTagId(), 1L, TIME_OPTION.ALLTIME);
	}
	
	@Test(expected = BadParameterException.class)
	public void aggregateTagsNull() throws Exception {
		tagService.aggregateTags(null);
	}
	
	@Test
	public void aggregateTags() throws Exception {
		AggregationOutput output = Mockito.mock(AggregationOutput.class);
		Mockito.when(output.results()).thenReturn(new ArrayList<DBObject>());
		Mockito.when(aggregationDao.getAggregation(Mockito.<AGGREGATION_TYPE>any(), 
				Mockito.<Date>any(), Mockito.<TIME_OPTION>any(), Mockito.anyLong()))
				.thenReturn(output);
		tagService.aggregateTags(TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void canPromoteNull() throws Exception {
		tagService.canPromote(null);
	}
	
	@Test
	public void canPromote() throws Exception {
		Tag tag = createValidTag();
		tag.setLocked(true);
		Assert.assertFalse(tagService.canPromote(tag));
		tag.setLocked(false);
		Assert.assertTrue(tagService.canPromote(tag));
	}
	
	@Test(expected = BadParameterException.class)
	public void canCommentNull() throws Exception {
		tagService.canComment(null);
	}
	
	@Test
	public void canComment() throws Exception {
		Tag tag = createValidTag();
		tag.setLocked(true);
		Assert.assertFalse(tagService.canComment(tag));
		tag.setLocked(false);
		Assert.assertTrue(tagService.canComment(tag));
	}
}
