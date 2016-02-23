package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.mongodb.AggregationOutput;
import com.mongodb.DBObject;
import com.py.py.dao.AggregationDao;
import com.py.py.dao.CommentDao;
import com.py.py.domain.Comment;
import com.py.py.domain.Escrow;
import com.py.py.domain.Posting;
import com.py.py.domain.Tag;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.enumeration.ESCROW_TYPE;
import com.py.py.domain.subdomain.Balance;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.TagId;
import com.py.py.dto.in.PromoteCommentDTO;
import com.py.py.dto.in.SubmitCommentDTO;
import com.py.py.dto.in.SubmitEditCommentDTO;
import com.py.py.dto.out.CommentDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.SORT_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BackerNotFoundException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.BalanceException;
import com.py.py.service.exception.FinanceException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.util.DefaultsFactory;


public class CommentServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("commentService")
	private CommentService commentService;

	@Autowired
	private EventService eventService;
	
	@Autowired
	private FinanceService financeService;
	
	@Autowired
	private EscrowService escrowService;
	
	@Autowired
	private CommentDao commentDao;
	
	@Autowired
	private PostingService postingService;
	
	@Autowired
	private DefaultsFactory defaultsFactory;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private TagService tagService;
	
	@Autowired
	private FollowService followService;
	
	@Autowired
	private AggregationDao aggregationDao;
	
	private Filter validFilter = new Filter();
	private List<Comment> validComments = new ArrayList<Comment>();
	private List<Comment> invalidComments = new ArrayList<Comment>();
	private Comment validComment = createValidComment();
	private Tag tag = new Tag();
	private List<COMMENT_TYPE> commentTypes = new ArrayList<COMMENT_TYPE>();
	private String warningReplacement = "Replacement";

	protected Comment createValidComment() {
		Comment comment =  new Comment();
		comment.setId(new ObjectId());
		comment.setAuthor(new CachedUsername(new ObjectId(), validOtherName));
		comment.setContent(validContent);
		comment.setEnabled(true);
		comment.setFlagged(false);
		comment.setLocked(false);
		comment.setRemoved(false);
		comment.setPaid(true);
		comment.setInitialized(true);
		comment.setType(COMMENT_TYPE.TAG);
		TagId tagId = new TagId();
		tagId.setName(validTag);
		tagId.setLanguage(validLanguage);
		comment.setBaseString(tagId.toString());
		return comment;
	}
	
	@Before
	public void setUp() {

		reset(commentDao, postingService, eventService, financeService, escrowService, 
				tagService, userService, defaultsFactory, followService, aggregationDao);
		
		TagId tagId = new TagId();
		tagId.setLanguage(validLanguage);
		tagId.setName(validTag);
		tag.setId(tagId);
		tag.setLocked(false);
		tag.setValue(0);
		
		validFilter.setSort(SORT_OPTION.VALUE);
		validFilter.setTags(new ArrayList<String>());
		validFilter.setTime(TIME_OPTION.DAY);
		validFilter.setWarning(false);
		
		validComments.add(createValidComment());
		validComments.add(createValidComment());
		invalidComments.addAll(validComments);
		invalidComments.add(null);
		
		validComment = createValidComment();
		
		Comment c3 = new Comment();
		c3.setId(null);
		c3.setAuthor(null);
		c3.setContent(null);
		c3.setEnabled(false);
		invalidComments.add(c3);
		
		commentTypes.add(COMMENT_TYPE.POSTING);
		commentTypes.add(COMMENT_TYPE.TAG);
		commentTypes.add(COMMENT_TYPE.USER);
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentPreviewDTOsNull2() throws Exception {
		commentService.getCommentPreviewDTOs(validLanguage, validUserId, null, 
				validFilter, commentTypes, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentPreviewDTOsNull3() throws Exception {
		commentService.getCommentPreviewDTOs(validLanguage, validUserId, 
				constructPageable(), null, commentTypes, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentPreviewDTOsInvalid1() throws Exception {
		commentService.getCommentPreviewDTOs(invalidLanguage, validUserId, 
				constructPageable(), validFilter, commentTypes, randomBoolean());
	}
	
	@Test
	public void getCommentPreviewsInvalidList() throws Exception {
		when(commentDao.getSortedComments(anyListOf(String.class), 
				anyString(), any(ObjectId.class), 
				any(Pageable.class), any(Filter.class)))
			.thenReturn(new PageImpl<Comment>(invalidComments));
		Page<CommentDTO> result = commentService.getCommentPreviewDTOs(
				validLanguage, validUserId, constructPageable(), validFilter, commentTypes, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getCommentPreviewDTOs(
				validLanguage, null, constructPageable(), validFilter, null, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test
	public void getCommentPreviews() throws Exception {
		when(commentDao.getSortedComments(anyListOf(String.class), 
				anyString(), any(ObjectId.class), 
				any(Pageable.class), any(Filter.class)))
			.thenReturn(new PageImpl<Comment>(validComments));
		Page<CommentDTO> result = commentService.getCommentPreviewDTOs(
				validLanguage, null, constructPageable(), validFilter, commentTypes, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getCommentPreviewDTOs(
				validLanguage, validUserId, constructPageable(), validFilter, null, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getAuthorPreviewDTOsNull1() throws Exception {
		commentService.getAuthorPreviewDTOs(null, constructPageable(), randomBoolean(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getAuthorPreviewDTOsNull2() throws Exception {
		commentService.getAuthorPreviewDTOs(validUserId, null, randomBoolean(), randomBoolean());
	}
	
	@Test
	public void getAuthorPreviewsInvalidList() throws Exception {
		when(commentDao.getUserComments(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyBoolean()))
			.thenReturn(new PageImpl<Comment>(invalidComments));
		Page<CommentDTO> result = commentService.getAuthorPreviewDTOs(validUserId, 
				constructPageable(), true, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getAuthorPreviewDTOs(validUserId, 
				constructPageable(), false, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getAuthorPreviewDTOs(validUserId, 
				constructPageable(), null, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test
	public void getAuthorPreviews() throws Exception {
		when(commentDao.getUserComments(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyBoolean()))
			.thenReturn(new PageImpl<Comment>(validComments));
		Page<CommentDTO> result = commentService.getAuthorPreviewDTOs(validUserId, 
				constructPageable(), true, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getAuthorPreviewDTOs(validUserId, 
				constructPageable(), false, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getAuthorPreviewDTOs(validUserId, 
				constructPageable(), null, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBeneficiaryPreviewDTOsNull1() throws Exception {
		commentService.getBeneficiaryPreviewDTOs(null, constructPageable(), randomBoolean(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBeneficiaryPreviewDTOsNull2() throws Exception {
		commentService.getBeneficiaryPreviewDTOs(validUserId, null, randomBoolean(), randomBoolean());
	}
	
	@Test
	public void getBeneficiaryPreviewsInvalidList() throws Exception {
		when(commentDao.getUserComments(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyBoolean()))
			.thenReturn(new PageImpl<Comment>(invalidComments));
		Page<CommentDTO> result = commentService.getBeneficiaryPreviewDTOs(
				validUserId, constructPageable(), true, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getBeneficiaryPreviewDTOs(
				validUserId, constructPageable(), false, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getBeneficiaryPreviewDTOs(
				validUserId, constructPageable(), null, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test
	public void getBeneficiaryPreviews() throws Exception {
		when(commentDao.getUserComments(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyBoolean()))
			.thenReturn(new PageImpl<Comment>(validComments));
		Page<CommentDTO> result = commentService.getBeneficiaryPreviewDTOs(
				validUserId, constructPageable(), true, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getBeneficiaryPreviewDTOs(
				validUserId, constructPageable(), false, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getBeneficiaryPreviewDTOs(
				validUserId, constructPageable(), null, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSelfPreviewDTOsNull1() throws Exception {
		commentService.getSelfPreviewDTOs(null, constructPageable(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSelfPreviewDTOsNull2() throws Exception {
		commentService.getSelfPreviewDTOs(validUserId, null, randomBoolean());
	}
	
	@Test
	public void getSelfPreviewsInvalidList() throws Exception {
		when(commentDao.getUserComments(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyBoolean()))
			.thenReturn(new PageImpl<Comment>(invalidComments));
		Page<CommentDTO> result = commentService.getSelfPreviewDTOs(validUserId, 
				constructPageable(), randomBoolean());
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test
	public void getSelfPreviews() throws Exception {
		when(commentDao.getUserComments(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyBoolean()))
			.thenReturn(new PageImpl<Comment>(validComments));
		Page<CommentDTO> result = commentService.getSelfPreviewDTOs(validUserId, 
				constructPageable(), randomBoolean());
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentDTOsNull3() throws Exception {
		commentService.getCommentDTOs(null, validTag, null, validLanguage, 
				constructPageable(), validFilter, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentDTOsNull5() throws Exception {
		commentService.getCommentDTOs(null, validTag, COMMENT_TYPE.TAG, validLanguage, 
				null, validFilter, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentDTOsNull6() throws Exception {
		commentService.getCommentDTOs(null, validTag, COMMENT_TYPE.TAG, validLanguage, 
				constructPageable(), null, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentDTOsInvalid2() throws Exception {
		commentService.getCommentDTOs(null, null, COMMENT_TYPE.TAG, validLanguage, 
				constructPageable(), validFilter, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentDTOsInvalid4() throws Exception {
		commentService.getCommentDTOs(null, validTag, COMMENT_TYPE.TAG, invalidLanguage, 
				constructPageable(), validFilter, randomBoolean());
	}
	
	@Test
	public void getCommentDTOsInvalidList() throws Exception {
		when(commentDao.getSortedReplyComments(any(ObjectId.class), anyString(), 
				anyListOf(String.class), any(ObjectId.class), anyBoolean(), 
				anyString(), any(Pageable.class), any(Filter.class)))
			.thenReturn(new PageImpl<Comment>(invalidComments));
		Page<CommentDTO> result = commentService.getCommentDTOs(null, 
				validTag, COMMENT_TYPE.TAG, validLanguage, constructPageable(), validFilter, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test
	public void getCommentDTOs() throws Exception {
		when(commentDao.getSortedReplyComments(any(ObjectId.class), anyString(), 
				anyListOf(String.class), any(ObjectId.class), anyBoolean(), 
				anyString(), any(Pageable.class), any(Filter.class)))
			.thenReturn(new PageImpl<Comment>(validComments));
		Page<CommentDTO> result = commentService.getCommentDTOs(null, 
				validTag, COMMENT_TYPE.TAG, validLanguage, constructPageable(), validFilter, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getCommentDTOs(validObjectId, 
				null, COMMENT_TYPE.POSTING, validLanguage, constructPageable(), validFilter, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
		result = commentService.getCommentDTOs(validUserId, 
				null, COMMENT_TYPE.USER, validLanguage, constructPageable(), validFilter, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSubCommentDTOsNull1() throws Exception {
		commentService.getSubCommentDTOs(null, validLanguage, constructPageable(), 
				validFilter, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSubCommentDTOsNull3() throws Exception {
		commentService.getSubCommentDTOs(validObjectId, validLanguage, null, validFilter, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSubCommentDTOsNull4() throws Exception {
		commentService.getSubCommentDTOs(validObjectId, validLanguage, constructPageable(), 
				null, randomBoolean());
	}
	
	@Test
	public void getSubCommentDTOsInvalidList() throws Exception {
		when(commentDao.getSortedReplyComments(any(ObjectId.class), anyString(), 
				anyListOf(String.class), any(ObjectId.class), anyBoolean(), 
				anyString(), any(Pageable.class), any(Filter.class)))
			.thenReturn(new PageImpl<Comment>(invalidComments));
		Page<CommentDTO> result = commentService.getSubCommentDTOs(
				validObjectId, validLanguage, constructPageable(), validFilter, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test
	public void getSubCommentDTOs() throws Exception {
		when(commentDao.getSortedReplyComments(any(ObjectId.class), anyString(), 
				anyListOf(String.class), any(ObjectId.class), anyBoolean(), 
				anyString(), any(Pageable.class), any(Filter.class)))
			.thenReturn(new PageImpl<Comment>(validComments));
		Page<CommentDTO> result = commentService.getSubCommentDTOs(
				validObjectId, validLanguage, constructPageable(), validFilter, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSpecificCommentDTOsNull6() throws Exception {
		commentService.getCommentDTOs(null, validTag, COMMENT_TYPE.TAG, validObjectId, 
				validLanguage, null, validFilter, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSpecificCommentDTOsNull7() throws Exception {
		commentService.getCommentDTOs(null, validTag, COMMENT_TYPE.TAG, validObjectId, 
				validLanguage, constructPageable(), null, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSpecificCommentDTOsInvalid5() throws Exception {
		commentService.getCommentDTOs(null, validTag, COMMENT_TYPE.TAG, validObjectId, 
				invalidLanguage, constructPageable(), validFilter, randomBoolean());
	}
	
	@Test
	public void getSpecificCommentDTOsInvalidList() throws Exception {
		when(commentDao.getSortedReplyComments(any(ObjectId.class), anyString(), 
				anyListOf(String.class), any(ObjectId.class), anyBoolean(), 
				anyString(), any(Pageable.class), any(Filter.class)))
			.thenReturn(new PageImpl<Comment>(invalidComments));
		Page<CommentDTO> result = commentService.getCommentDTOs(null, validTag, 
				COMMENT_TYPE.TAG, validObjectId, validLanguage, constructPageable(), 
				validFilter, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test
	public void getSpecificCommentDTOs() throws Exception {
		when(commentDao.getSortedReplyComments(any(ObjectId.class), anyString(), 
				anyListOf(String.class), any(ObjectId.class), anyBoolean(), 
				anyString(), any(Pageable.class), any(Filter.class)))
			.thenReturn(new PageImpl<Comment>(validComments));
		Page<CommentDTO> result = commentService.getCommentDTOs(null, validTag, 
				COMMENT_TYPE.TAG, validObjectId, validLanguage, constructPageable(), 
				validFilter, false);
		Assert.assertEquals(result.getContent().size(), validComments.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void createCommentNull1() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		commentService.createCommentDTO(null, null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createCommentNull5() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				null, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createCommentNull6() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, null, dto, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createCommentNull7() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, null, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createCommentNull8() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void createCommentInvalid1() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		commentService.createCommentDTO(createInvalidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createCommentInvalid4() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		baseTag.setId(null);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createCommentInvalid7() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, invalidLanguage);
	}
	
	@Test(expected = FinanceException.class)
	public void createCommentFinance() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		when(commentDao.save(any(Comment.class))).thenReturn(createValidComment());
		doThrow(new FinanceException()).when(financeService).charge(any(ObjectId.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong());
		when(tagService.getTag(anyString(), anyString())).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test(expected = BalanceException.class)
	public void createCommentBalance() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		when(commentDao.save(any(Comment.class))).thenReturn(createValidComment());
		doThrow(new BalanceException()).when(financeService).charge(any(ObjectId.class), 
				any(ObjectId.class), anyBoolean(), anyString(), anyLong());
		when(tagService.getTag(anyString(), anyString())).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void createCommentNotAllowed() throws Exception {
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		when(commentDao.save(any(Comment.class))).thenReturn(createValidComment());
		when(escrowService.getBackerEscrow(any(ObjectId.class), anyString(), 
				any(ObjectId.class), anyString()))
			.thenThrow(new NotFoundException(validName));
		when(tagService.getTag(anyString(), anyString())).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(false);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test(expected = BackerNotFoundException.class)
	public void createCommentBackedInvalid() throws Exception {
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(validName);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		when(commentDao.save(any(Comment.class))).thenReturn(createValidComment());
		when(escrowService.getBackerEscrow(any(ObjectId.class), anyString(), 
				any(ObjectId.class), anyString()))
			.thenThrow(new NotFoundException(validName));
		when(tagService.getTag(anyString(), anyString())).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test
	public void createCommentBacked() throws Exception {
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(validName);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		
		Escrow escrow = new Escrow();
		Balance balance = new Balance();
		balance.setGold(25L);
		escrow.setBalance(balance);
		escrow.setSourceName(validName);
		escrow.setTargetName(validName);
		escrow.setSource(validUserId.toHexString());
		escrow.setTarget(validObjectId.toHexString());
		escrow.setType(ESCROW_TYPE.BACKING);
		
		when(commentDao.save(any(Comment.class))).thenReturn(createValidComment());
		when(escrowService.getBackerEscrow(any(ObjectId.class), anyString(), 
				any(ObjectId.class), anyString()))
			.thenReturn(escrow);
		when(tagService.getTag(anyString(), anyString())).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.createCommentDTO(createValidUser(), null, null, null, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test
	public void createComment() throws Exception {
		Posting basePosting = new Posting();
		basePosting.setId(validObjectId);
		basePosting.setTitle(validTitle);
		Tag baseTag = new Tag();
		TagId tagId = new TagId();
		tagId.setName(validName);
		tagId.setLanguage(validLanguage);
		baseTag.setId(tagId);
		UserInfo baseUser = createValidUserInfo();
		baseUser.setId(new ObjectId());
		baseUser.setUsername(validOtherName);
		SubmitCommentDTO dto = new SubmitCommentDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setWarning(false);
		when(commentDao.save(any(Comment.class))).thenReturn(createValidComment());
		when(userService.canComment(any(UserInfo.class))).thenReturn(true);
		when(postingService.canComment(any(Posting.class))).thenReturn(true);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.createCommentDTO(createValidUser(), null, basePosting, baseUser, 
				baseTag, COMMENT_TYPE.POSTING, dto, validLanguage);
		commentService.createCommentDTO(createValidUser(), null, basePosting, baseUser, 
				baseTag, COMMENT_TYPE.USER, dto, validLanguage);
		commentService.createCommentDTO(createValidUser(), null, basePosting, baseUser, 
				baseTag, COMMENT_TYPE.TAG, dto, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void editCommentNull1() throws Exception {
		SubmitEditCommentDTO dto = new SubmitEditCommentDTO();
		dto.setContent(validContent);
		commentService.editComment(null, validComment, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void editCommentNull2() throws Exception {
		SubmitEditCommentDTO dto = new SubmitEditCommentDTO();
		dto.setContent(validContent);
		commentService.editComment(validUserId, null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void editCommentNull3() throws Exception {
		ObjectId uId = validComment.getAuthor().getId();
		commentService.editComment(uId, validComment, null);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void editCommentNotAllowed() throws Exception {
		SubmitEditCommentDTO dto = new SubmitEditCommentDTO();
		dto.setContent(validContent);
		
		ObjectId uId = new ObjectId();
		commentService.editComment(uId, validComment, dto);
	}
	
	@Test
	public void editPosting() throws Exception {
		SubmitEditCommentDTO dto = new SubmitEditCommentDTO();
		dto.setContent(validContent);
		
		ObjectId uId = validComment.getAuthor().getId();
		commentService.editComment(uId, validComment, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentDTONull() throws Exception {
		commentService.getCommentDTO(validUserId, null, randomBoolean());
	}
	
	// warning differences should no longer alter content
	@Test
	public void getCommentDTONoWarning() throws Exception {
		when(defaultsFactory.getWarningContentReplacement()).thenReturn(warningReplacement);
		Comment comment = createValidComment();
		String content = comment.getContent();
		comment.setWarning(true);
		CommentDTO result = commentService.getCommentDTO(null, comment, true);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertTrue(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = commentService.getCommentDTO(validUserId, comment, true);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertTrue(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		
		comment.setWarning(false);
		result = commentService.getCommentDTO(null, comment, true);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = commentService.getCommentDTO(null, comment, false);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = commentService.getCommentDTO(null, comment, null);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = commentService.getCommentDTO(validUserId, comment, true);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = commentService.getCommentDTO(validUserId, comment, false);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = commentService.getCommentDTO(validUserId, comment, null);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentNull() throws Exception {
		commentService.getComment(null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getCommentNotFound() throws Exception {
		when(commentDao.findComment(any(ObjectId.class))).thenReturn(null);
		commentService.getComment(validObjectId);
	}
	
	@Test
	public void getComment() throws Exception {
		when(commentDao.findComment(any(ObjectId.class))).thenReturn(validComment);
		commentService.getComment(validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateCommentNull1() throws Exception {
		commentService.appreciateComment(null, createValidUser(), createValidComment(), 
				validCost, validCost, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateCommentNull2() throws Exception {
		commentService.appreciateComment(validObjectId, null, createValidComment(), 
				validCost, validCost, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateCommentNull3() throws Exception {
		commentService.appreciateComment(validObjectId, createValidUser(), null, 
				validCost, validCost, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciateCommentInvalid() throws Exception {
		when(tagService.getTag(anyString(), anyString())).thenReturn(tag);
		commentService.appreciateComment(validObjectId, createInvalidUser(), 
				createValidComment(), validCost, validCost, false);
	}
	
	@Test
	public void appreciateCommentSelf() throws Exception {
		User user = createValidUser();
		user.setUsername(validName);
		user.setId(validUserId);
		Comment comment = createValidComment();
		comment.setAuthor(new CachedUsername(validUserId, validName));
		when(tagService.getTag(any(TagId.class))).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.appreciateComment(validObjectId, user, comment, validCost, 
				validCost, false);
		
		comment.setLocked(true);
		commentService.appreciateComment(validObjectId, user, comment, validCost, 
				validCost, false);
	}
	
	@Test
	public void appreciateComment() throws Exception {
		when(tagService.getTag(any(TagId.class))).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.appreciateComment(validObjectId, createValidUser(), 
				createValidComment(), validCost, validCost, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteCommentNull1() throws Exception {
		PromoteCommentDTO dto = new PromoteCommentDTO();
		dto.setPromotion(validCost);
		dto.setWarning(false);
		commentService.promoteComment(null, createValidComment(), dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteCommentNull2() throws Exception {
		PromoteCommentDTO dto = new PromoteCommentDTO();
		dto.setPromotion(validCost);
		dto.setWarning(false);
		commentService.promoteComment(createValidUser(), null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteCommentNull3() throws Exception {
		commentService.promoteComment(createValidUser(), createValidComment(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void promoteCommentInvalid() throws Exception {
		PromoteCommentDTO dto = new PromoteCommentDTO();
		dto.setPromotion(validCost);
		dto.setWarning(false);
		when(tagService.getTag(anyString(), anyString())).thenReturn(tag);
		commentService.promoteComment(createInvalidUser(), createValidComment(), dto);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void promoteCommmentLocked() throws Exception {
		PromoteCommentDTO dto = new PromoteCommentDTO();
		dto.setPromotion(validCost);
		dto.setWarning(false);
		Comment comment = createValidComment();
		comment.setLocked(true);
		when(tagService.getTag(anyString(), anyString())).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(false);
		commentService.promoteComment(createValidUser(), comment, dto);
	}
	
	@Test
	public void promoteCommmentSelf() throws Exception {
		PromoteCommentDTO dto = new PromoteCommentDTO();
		dto.setPromotion(validCost);
		dto.setWarning(false);
		User user = createValidUser();
		user.setUsername(validName);
		user.setId(validUserId);
		Comment comment = createValidComment();
		comment.setAuthor(new CachedUsername(validUserId, validName));
		when(tagService.getTag(any(TagId.class))).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.promoteComment(user, comment, dto);
	}
	
	@Test
	public void promoteComment() throws Exception {
		PromoteCommentDTO dto = new PromoteCommentDTO();
		dto.setPromotion(validCost);
		dto.setWarning(false);
		when(tagService.getTag(any(TagId.class))).thenReturn(tag);
		when(tagService.canComment(any(Tag.class))).thenReturn(true);
		commentService.promoteComment(createValidUser(), createValidComment(), dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementReplyCountNull() throws Exception {
		commentService.incrementReplyCount(null, randomBoolean());
	}
	
	@Test
	public void incrementReplyCountIncrement() throws Exception {
		commentService.incrementReplyCount(validObjectId, true);
		verify(commentDao).incrementReplyCount(validObjectId, 1L);
	}
	
	@Test
	public void incrementReplyCountDecrement() throws Exception {
		commentService.incrementReplyCount(validObjectId, false);
		verify(commentDao).incrementReplyCount(validObjectId, -1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementAppreciationPromotionCountNull() throws Exception {
		commentService.incrementAppreciationPromotionCount(null, randomBoolean(), 
				randomBoolean());
	}
	
	@Test
	public void incrementAppreciationPromotionCountIncrement() throws Exception {
		commentService.incrementAppreciationPromotionCount(validObjectId, false, true);
		verify(commentDao).incrementAppreciationPromotionCount(validObjectId, null, 1L);
	}
	
	@Test
	public void incrementAppreciationPromotionCountDecrement() throws Exception {
		commentService.incrementAppreciationPromotionCount(validObjectId, true, false);
		verify(commentDao).incrementAppreciationPromotionCount(validObjectId, -1L, -1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementReplyTallyApproximationNull1() throws Exception {
		commentService.incrementReplyTallyApproximation(null, validAppreciation, validCost, 
				validCost);
	}
	
	@Test
	public void incrementReplyTallyApproximation() throws Exception {
		commentService.incrementReplyTallyApproximation(validObjectId, null, null, null);
		commentService.incrementReplyTallyApproximation(validObjectId, null, null, 
				validCost);
		commentService.incrementReplyTallyApproximation(validObjectId, null, validCost,
				null);
		commentService.incrementReplyTallyApproximation(validObjectId, null, validCost, 
				validCost);
		commentService.incrementReplyTallyApproximation(validObjectId, validAppreciation, 
				null, null);
		commentService.incrementReplyTallyApproximation(validObjectId, validAppreciation, 
				null, validCost);
		commentService.incrementReplyTallyApproximation(validObjectId, validAppreciation, 
				validCost, null);
		commentService.incrementReplyTallyApproximation(validObjectId, validAppreciation, 
				validCost, validCost);
		verify(commentDao, times(4)).incrementReplyTallyCost(validObjectId, validCost);
		verify(commentDao, times(6)).incrementReplyTallyAppreciationPromotion(
				any(ObjectId.class), anyLong(), anyLong());
	}
	
	@Test(expected = BadParameterException.class)
	public void updateAggregateNull1() throws Exception {
		commentService.updateAggregate(null, validCost, TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void updateAggregateNull3() throws Exception {
		commentService.updateAggregate(validObjectId, validCost, null);
	}
	
	@Test
	public void updateAggregate() throws Exception {
		commentService.updateAggregate(validObjectId, validCost, TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void aggregateCommentsNull() throws Exception {
		commentService.aggregateComments(null);
	}
	
	@Test
	public void aggregateComments() throws Exception {
		AggregationOutput output = mock(AggregationOutput.class);
		when(output.results()).thenReturn(new ArrayList<DBObject>());
		when(aggregationDao.getAggregation(any(AGGREGATION_TYPE.class), 
				any(Date.class),any(TIME_OPTION.class), anyLong()))
				.thenReturn(output);
		commentService.aggregateComments(TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void enableCommentNull1() throws Exception {
		commentService.enableComment(null, validComment);
	}
	
	@Test(expected = BadParameterException.class)
	public void enableCommentNull2() throws Exception {
		commentService.enableComment(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void enableCommentInvalid() throws Exception {
		commentService.enableComment(createInvalidUser(), validComment);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void enableCommentNotAllowed() throws Exception {
		Comment comment = createValidComment();
		comment.setEnabled(false);
		comment.setAuthor(new CachedUsername(new ObjectId(), validOtherName));
		User user = createValidUser();
		user.setUsername(validName);
		user.setId(validUserId);
		commentService.enableComment(user, comment);
	}
	
	@Test
	public void enableComment() throws Exception {
		Comment comment = createValidComment();
		comment.setEnabled(false);
		comment.setAuthor(new CachedUsername(validUserId, validName));
		User user = createValidUser();
		user.setUsername(validName);
		user.setId(validUserId);
		commentService.enableComment(user, comment);
		comment.setEnabled(true);
		commentService.enableComment(user, comment);
	}
	
	@Test(expected = BadParameterException.class)
	public void disableCommentNull1() throws Exception {
		commentService.disableComment(null, validComment);
	}
	
	@Test(expected = BadParameterException.class)
	public void disableCommentNull2() throws Exception {
		commentService.disableComment(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void disableCommentInvalid() throws Exception {
		commentService.disableComment(createInvalidUser(), validComment);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void disableCommentNotAllowed() throws Exception {
		Comment comment = createValidComment();
		comment.setEnabled(true);
		comment.setAuthor(new CachedUsername(new ObjectId(), validOtherName));
		User user = createValidUser();
		user.setUsername(validName);
		user.setId(validUserId);
		commentService.disableComment(user, comment);
	}
	
	@Test
	public void disableComment() throws Exception {
		Comment comment = createValidComment();
		comment.setEnabled(true);
		comment.setAuthor(new CachedUsername(validUserId, validName));
		User user = createValidUser();
		user.setUsername(validName);
		user.setId(validUserId);
		commentService.disableComment(user, comment);
		comment.setEnabled(false);
		commentService.disableComment(user, comment);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull1() throws Exception {
		commentService.flag(null, createValidUserInfo(), validComment, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull2() throws Exception {
		commentService.flag(createValidUser(), null, validComment, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull3() throws Exception {
		commentService.flag(createValidUser(), createValidUserInfo(), null, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull4() throws Exception {
		commentService.flag(createValidUser(), createValidUserInfo(), validComment, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagInvalid1() throws Exception {
		commentService.flag(createInvalidUser(), createValidUserInfo(), validComment, FLAG_REASON.ILLICIT);
	}
	
	@Test
	public void flag() throws Exception {
		Comment comment = createValidComment();
		comment.setFlagged(true);
		commentService.flag(createValidUser(), createValidUserInfo(), comment, FLAG_REASON.ILLICIT);
		comment.setFlagged(false);
		commentService.flag(createValidUser(), createValidUserInfo(), comment, FLAG_REASON.ILLICIT);
		User user = createValidUser();
		user.setId(validUserId);
		List<ObjectId> votes = new ArrayList<ObjectId>();
		votes.add(validUserId);
		comment.setVotes(votes);
		commentService.flag(user, createValidUserInfo(), comment, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void canAppreciateNull() throws Exception {
		commentService.canAppreciate(null);
	}
	
	@Test
	public void canAppreciate() throws Exception {
		Comment comment = createValidComment();
		Assert.assertTrue(commentService.canAppreciate(comment));
		comment.setAuthor(null);
		Assert.assertFalse(commentService.canAppreciate(comment));
		comment.setAuthor(new CachedUsername(null, validName));
		Assert.assertFalse(commentService.canAppreciate(comment));
		comment.setAuthor(new CachedUsername(validObjectId, validName));
		Assert.assertTrue(commentService.canAppreciate(comment));
		comment.setLocked(true);
		Assert.assertFalse(commentService.canAppreciate(comment));
		comment.setLocked(false);
		comment.setEnabled(false);
		Assert.assertFalse(commentService.canAppreciate(comment));
		comment.setEnabled(true);
		comment.setInitialized(false);
		Assert.assertFalse(commentService.canAppreciate(comment));
		comment.setInitialized(true);
		comment.setFlagged(true);
		Assert.assertFalse(commentService.canAppreciate(comment));
		comment.setFlagged(false);
		comment.setRemoved(true);
		Assert.assertFalse(commentService.canAppreciate(comment));
		comment.setRemoved(false);
		Assert.assertTrue(commentService.canAppreciate(comment));
	}
	
	@Test(expected = BadParameterException.class)
	public void canCommentNull() throws Exception {
		commentService.canAppreciate(null);
	}
	
	@Test
	public void canComment() throws Exception {
		Comment comment = createValidComment();
		Assert.assertTrue(commentService.canComment(comment));
		comment.setLocked(true);
		Assert.assertFalse(commentService.canComment(comment));
		comment.setLocked(false);
		comment.setEnabled(false);
		Assert.assertFalse(commentService.canComment(comment));
		comment.setEnabled(true);
		comment.setInitialized(false);
		Assert.assertFalse(commentService.canComment(comment));
		comment.setInitialized(true);
		comment.setFlagged(true);
		Assert.assertFalse(commentService.canComment(comment));
		comment.setFlagged(false);
		comment.setRemoved(true);
		Assert.assertFalse(commentService.canComment(comment));
		comment.setRemoved(false);
		Assert.assertTrue(commentService.canComment(comment));
	}
}
