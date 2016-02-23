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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import com.py.py.constants.ServiceValues;
import com.py.py.dao.AggregationDao;
import com.py.py.dao.PostingDao;
import com.py.py.domain.Escrow;
import com.py.py.domain.Posting;
import com.py.py.domain.User;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.enumeration.ESCROW_TYPE;
import com.py.py.domain.subdomain.Balance;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.dto.in.PromotePostingDTO;
import com.py.py.dto.in.SubmitEditPostingDTO;
import com.py.py.dto.in.SubmitPostingDTO;
import com.py.py.dto.out.PostingDTO;
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
import com.py.py.service.util.Mapper;

public class PostingServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("postingService")
	private PostingService postingService;
	
	@Autowired
	private PostingDao postingDao;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private FinanceService financeService;
	
	@Autowired
	private CommentService commentService;
	
	@Autowired
	private EscrowService escrowService;
	
	@Autowired
	private TagService tagService;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private DefaultsFactory defaultsFactory;
	
	@Autowired
	private AggregationDao aggregationDao;

	private Filter validFilter = new Filter();
	private List<Posting> validPostings = new ArrayList<Posting>();
	private List<Posting> invalidPostings = new ArrayList<Posting>();
	private Posting validPosting = createValidPosting();
	private String warningReplacement = "Replacement";
	private List<String> validTags = validFilter.getTags();
	
	protected Posting createValidPosting() {
		Posting posting =  new Posting();
		posting.setId(new ObjectId());
		posting.setAuthor(new CachedUsername(new ObjectId(), validOtherName));
		posting.setContent(validContent);
		posting.setEnabled(true);
		posting.setFlagged(false);
		posting.setLocked(false);
		posting.setRemoved(false);
		posting.setPaid(true);
		posting.setInitialized(true);
		return posting;
	}
	
	@Before
	public void setUp() {
		reset(postingDao, eventService, financeService, commentService, escrowService, 
				tagService, userService, defaultsFactory, aggregationDao);
		
		validFilter.setSort(SORT_OPTION.VALUE);
		validFilter.setTags(new ArrayList<String>());
		validFilter.setTime(TIME_OPTION.DAY);
		validFilter.setWarning(false);
		
		validPostings.add(createValidPosting());
		validPostings.add(createValidPosting());
		invalidPostings.addAll(validPostings);
		invalidPostings.add(null);
		
		validPosting = createValidPosting();
		
		Posting p3 = new Posting();
		p3.setId(null);
		p3.setAuthor(null);
		p3.setContent(null);
		p3.setEnabled(false);
		invalidPostings.add(p3);
	}
	
	@Test(expected = BadParameterException.class)
	public void getPostingPreviewsNull2() throws Exception {
		postingService.getPostingPreviews(validLanguage, validUserId, null, validFilter, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getPostingPreviewsNull3() throws Exception {
		postingService.getPostingPreviews(validLanguage, validUserId, 
				constructPageable(), null, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getPostingPreviewsInvalid1() throws Exception {
		postingService.getPostingPreviews(invalidLanguage, validUserId, 
				constructPageable(), validFilter, randomBoolean());
	}
	
	@Test
	public void getPostingPreviewsInvalidList() throws Exception {
		when(postingDao.getSortedPostings(anyString(), any(ObjectId.class), 
				any(Pageable.class), any(Filter.class)))
				.thenReturn(new PageImpl<Posting>(invalidPostings));
		Page<PostingDTO> result = postingService.getPostingPreviews(
				validLanguage, validUserId, constructPageable(), validFilter, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validPostings.size());
	}
	
	@Test
	public void getPostingPreviews() throws Exception {
		when(postingDao.getSortedPostings(anyString(), any(ObjectId.class), 
				any(Pageable.class), any(Filter.class)))
				.thenReturn(new PageImpl<Posting>(validPostings));
		Page<PostingDTO> result = postingService.getPostingPreviews(
				validLanguage, validUserId, constructPageable(), validFilter, false);
		Assert.assertEquals(result.getContent().size(), validPostings.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getAuthorPreviewsNull1() throws Exception {
		postingService.getAuthorPreviews(null, constructPageable(), 
				validTags, randomBoolean(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getAuthorPreviewsNull2() throws Exception {
		postingService.getAuthorPreviews(validUserId, null, 
				validTags, randomBoolean(), randomBoolean());
	}
	
	@Test
	public void getAuthorPreviewsInvalidList() throws Exception {
		when(postingDao.getUserPostings(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyListOf(String.class), anyBoolean()))
				.thenReturn(new PageImpl<Posting>(invalidPostings));
		Page<PostingDTO> result = postingService.getAuthorPreviews(validUserId, 
				constructPageable(), validTags, true, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validPostings.size());
		result = postingService.getAuthorPreviews(validUserId, 
				constructPageable(), validTags, false, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validPostings.size());
		result = postingService.getAuthorPreviews(validUserId, 
				constructPageable(), validTags, null, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validPostings.size());
	}
	
	@Test
	public void getAuthorPreviews() throws Exception {
		when(postingDao.getUserPostings(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyListOf(String.class), anyBoolean()))
				.thenReturn(new PageImpl<Posting>(validPostings));
		Page<PostingDTO> result = postingService.getAuthorPreviews(validUserId, 
				constructPageable(), validTags, true, false);
		Assert.assertEquals(result.getContent().size(), validPostings.size());
		result = postingService.getAuthorPreviews(validUserId, 
				constructPageable(), validTags, false, false);
		Assert.assertEquals(result.getContent().size(), validPostings.size());
		result = postingService.getAuthorPreviews(validUserId, 
				constructPageable(), validTags, null, false);
		Assert.assertEquals(result.getContent().size(), validPostings.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBeneficiaryPreviewsNull1() throws Exception {
		postingService.getBeneficiaryPreviews(null, constructPageable(), 
				validTags, randomBoolean(), randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getBeneficiaryPreviewsNull2() throws Exception {
		postingService.getBeneficiaryPreviews(validUserId, null, 
				validTags, randomBoolean(), randomBoolean());
	}
	
	@Test
	public void getBeneficiaryPreviewsInvalidList() throws Exception {
		when(postingDao.getUserPostings(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyListOf(String.class), anyBoolean()))
				.thenReturn(new PageImpl<Posting>(invalidPostings));
		Page<PostingDTO> result = postingService.getBeneficiaryPreviews(validUserId, 
				constructPageable(), validTags, true, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validPostings.size());
		result = postingService.getBeneficiaryPreviews(validUserId, 
				constructPageable(), validTags, false, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validPostings.size());
		result = postingService.getBeneficiaryPreviews(validUserId, 
				constructPageable(), validTags, null, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validPostings.size());
	}
	
	@Test
	public void getBeneficiaryPreviews() throws Exception {
		when(postingDao.getUserPostings(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyListOf(String.class), anyBoolean()))
				.thenReturn(new PageImpl<Posting>(validPostings));
		Page<PostingDTO> result = postingService.getBeneficiaryPreviews(validUserId, 
				constructPageable(), validTags, true, false);
		Assert.assertEquals(result.getContent().size(), validPostings.size());
		result = postingService.getBeneficiaryPreviews(validUserId, 
				constructPageable(), validTags, false, false);
		Assert.assertEquals(result.getContent().size(), validPostings.size());
		result = postingService.getBeneficiaryPreviews(validUserId, 
				constructPageable(), validTags, null, false);
		Assert.assertEquals(result.getContent().size(), validPostings.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSelfPreviewsNull1() throws Exception {
		postingService.getSelfPreviews(null, constructPageable(), validTags, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void getSelfPreviewsNull2() throws Exception {
		postingService.getSelfPreviews(validUserId, null, validTags, randomBoolean());
	}
	
	@Test
	public void getSelfPreviewsInvalidList() throws Exception {
		when(postingDao.getUserPostings(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyListOf(String.class), anyBoolean()))
				.thenReturn(new PageImpl<Posting>(invalidPostings));
		Page<PostingDTO> result = postingService.getSelfPreviews(validUserId, 
				constructPageable(), validTags, randomBoolean());
		Assert.assertEquals(result.getContent().size(), validPostings.size());
	}
	
	@Test
	public void getSelfPreviews() throws Exception {
		when(postingDao.getUserPostings(any(ObjectId.class), any(ObjectId.class), 
				any(Pageable.class), anyBoolean(), anyListOf(String.class), anyBoolean()))
				.thenReturn(new PageImpl<Posting>(validPostings));
		Page<PostingDTO> result = postingService.getSelfPreviews(validUserId, 
				constructPageable(), validTags, false);
		Assert.assertEquals(result.getContent().size(), validPostings.size());
	}

	@Test(expected = BadParameterException.class)
	public void createPostingNull1() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
		dto.setWarning(false);
		postingService.createPosting(null, dto, validLanguage);
	}

	@Test(expected = BadParameterException.class)
	public void createPostingNull2() throws Exception {
		postingService.createPosting(createValidUser(), null, validLanguage);
	}

	@Test(expected = BadParameterException.class)
	public void createPostingNull3() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
		dto.setWarning(false);
		postingService.createPosting(createValidUser(), dto, null);
	}

	@Test(expected = BadParameterException.class)
	public void createPostingInvalid3() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
		dto.setWarning(false);
		postingService.createPosting(createValidUser(), dto, invalidLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void createPostingInvalid1() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
		dto.setWarning(false);
		postingService.createPosting(createInvalidUser(), dto, validLanguage);
	}
	
	@Test(expected = FinanceException.class)
	public void createPostingFinance() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
		dto.setWarning(false);
		Posting posting = Mapper.mapPosting(dto, validSourceCU, null, new Date(), 
				validLanguage);
		when(postingDao.save(any(Posting.class))).thenReturn(posting);
		doThrow(new FinanceException()).when(financeService).charge(
				any(ObjectId.class), any(ObjectId.class), 
				anyBoolean(), anyString(), anyLong());
		postingService.createPosting(createValidUser(), dto, validLanguage);
	}
	
	@Test(expected = BalanceException.class)
	public void createPostingBalance() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
		dto.setWarning(false);
		Posting posting = Mapper.mapPosting(dto, validSourceCU, null, new Date(), 
				validLanguage);
		when(postingDao.save(any(Posting.class))).thenReturn(posting);
		doThrow(new BalanceException()).when(financeService).charge(
				any(ObjectId.class), any(ObjectId.class), 
				anyBoolean(), anyString(), anyLong());
		postingService.createPosting(createValidUser(), dto, validLanguage);
	}
	
	@Test(expected = BackerNotFoundException.class)
	public void createPostingBackedInvalid() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(validName);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
		dto.setWarning(false);
		Posting posting = Mapper.mapPosting(dto, validSourceCU, null, new Date(), 
				validLanguage);
		when(postingDao.save(any(Posting.class))).thenReturn(posting);
		when(escrowService.getBackerEscrow(any(ObjectId.class), anyString(), 
				any(ObjectId.class), anyString()))
			.thenThrow(new NotFoundException(validName));
		postingService.createPosting(createValidUser(), dto, validLanguage);
	}
	
	@Test
	public void createPostingBacked() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(validName);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
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
		Posting posting = Mapper.mapPosting(dto, validSourceCU, null, new Date(), 
				validLanguage);
		posting.setId(validObjectId);
		when(postingDao.save(any(Posting.class))).thenReturn(posting);
		when(escrowService.getBackerEscrow(any(ObjectId.class), anyString(), 
				any(ObjectId.class), anyString()))
			.thenReturn(escrow);
		postingService.createPosting(createValidUser(), dto, validLanguage);
	}
	
	@Test
	public void createPosting() throws Exception {
		SubmitPostingDTO dto = new SubmitPostingDTO();
		dto.setBacker(null);
		dto.setContent(validContent);
		dto.setCost(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setTitle(validContent);
		dto.setWarning(false);
		
		Posting posting = Mapper.mapPosting(dto, validSourceCU, null, new Date(), 
				validLanguage);
		posting.setId(validObjectId);
		when(postingDao.save(any(Posting.class))).thenReturn(posting);
		postingService.createPosting(createValidUser(), dto, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void editPostingNull1() throws Exception {
		SubmitEditPostingDTO dto = new SubmitEditPostingDTO();
		dto.setContent(validContent);
		postingService.editPosting(null, validPosting, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void editPostingNull2() throws Exception {
		SubmitEditPostingDTO dto = new SubmitEditPostingDTO();
		dto.setContent(validContent);
		postingService.editPosting(validUserId, null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void editPostingNull3() throws Exception {
		ObjectId uId = validPosting.getAuthor().getId();
		postingService.editPosting(uId, validPosting, null);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void editPostingNotAllowed() throws Exception {
		SubmitEditPostingDTO dto = new SubmitEditPostingDTO();
		dto.setContent(validContent);
		
		ObjectId uId = new ObjectId();
		postingService.editPosting(uId, validPosting, dto);
	}
	
	@Test
	public void editPosting() throws Exception {
		SubmitEditPostingDTO dto = new SubmitEditPostingDTO();
		dto.setContent(validContent);
		dto.setPreview(validContent);
		dto.setTitle(validTitle);
		
		ObjectId uId = validPosting.getAuthor().getId();
		postingService.editPosting(uId, validPosting, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void getPostingDTONull1() throws Exception {
		postingService.getPostingDTO(validUserId, null, randomBoolean());
	}
	
	// warning differences should no longer alter content
	@Test
	public void getPostingDTONoWarning() throws Exception {
		when(defaultsFactory.getWarningContentReplacement()).thenReturn(warningReplacement);
		Posting posting = createValidPosting();
		String content = posting.getContent();
		posting.setWarning(true);
		PostingDTO result = postingService.getPostingDTO(null, posting, true);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertTrue(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = postingService.getPostingDTO(validUserId, posting, true);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertTrue(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);

		posting.setWarning(false);
		result = postingService.getPostingDTO(null, posting, true);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = postingService.getPostingDTO(null, posting, false);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = postingService.getPostingDTO(null, posting, null);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = postingService.getPostingDTO(validUserId, posting, true);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = postingService.getPostingDTO(validUserId, posting, false);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		result = postingService.getPostingDTO(validUserId, posting, null);
		Assert.assertEquals(result.getContent(), content);
		Assert.assertFalse(result.isWarning());
		Assert.assertNotEquals(result.getContent(), warningReplacement);
		
	}
	
	@Test(expected = BadParameterException.class)
	public void getPostingNull() throws Exception {
		postingService.getPosting(null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getPostingNotFound() throws Exception {
		when(postingDao.findPosting(any(ObjectId.class)))
			.thenReturn(null);
		postingService.getPosting(validObjectId);
	}
	
	@Test
	public void getPosting() throws Exception {
		when(postingDao.findPosting(any(ObjectId.class))).thenReturn(validPosting);
		postingService.getPosting(validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciatePostingNull1() throws Exception {
		postingService.appreciatePosting(null, createValidUser(), validPosting, 
				validCost, validCost, new ArrayList<String>(), false);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciatePostingNull2() throws Exception {
		postingService.appreciatePosting(validObjectId, null, validPosting, 
				validCost, validCost, new ArrayList<String>(), false);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciatePostingNull3() throws Exception {
		postingService.appreciatePosting(validObjectId, createValidUser(), null, 
				validCost, validCost, new ArrayList<String>(), false);
	}
	
	@Test(expected = BadParameterException.class)
	public void appreciatePostingInvalid2() throws Exception {
		postingService.appreciatePosting(validObjectId, createInvalidUser(), 
				validPosting, validCost, validCost, new ArrayList<String>(), false);
	}
	
	@Test
	public void appreciatePostingSelf() throws Exception {
		User user = createValidUser();
		user.setUsername(validName);
		user.setId(validUserId);
		Posting posting = createValidPosting();
		posting.setAuthor(new CachedUsername(validUserId, validName));
		postingService.appreciatePosting(validObjectId, user, posting, validCost, 
				validCost, new ArrayList<String>(), false);
		
		posting.setLocked(true);
		postingService.appreciatePosting(validObjectId, user, posting, validCost, 
				validCost, new ArrayList<String>(), false);
	}
	
	@Test
	public void appreciatePostingTagCount() throws Exception {
		List<String> tags = new ArrayList<String>();
		for(int i = 0; i < (ServiceValues.POSTING_TAGS_TOTAL_MAX + 1); i++) {
			tags.add(validTag + i);
		}
		postingService.appreciatePosting(validObjectId, createValidUser(), validPosting, 
				validCost, validCost, tags, false);
	}
	
	@Test
	public void appreciatePosting() throws Exception {
		postingService.appreciatePosting(validObjectId, createValidUser(), validPosting, 
				validCost, validCost, new ArrayList<String>(), false);
	}
	
	@Test(expected = BadParameterException.class)
	public void promotePostingNull1() throws Exception {
		PromotePostingDTO dto = new PromotePostingDTO();
		dto.setPromotion(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setWarning(false);
		postingService.promotePosting(null, validPosting, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void promotePostingNull2() throws Exception {
		PromotePostingDTO dto = new PromotePostingDTO();
		dto.setPromotion(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setWarning(false);
		postingService.promotePosting(createValidUser(), null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void promotePostingNull3() throws Exception {
		postingService.promotePosting(createValidUser(), validPosting, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void promotePostingInvalid1() throws Exception {
		PromotePostingDTO dto = new PromotePostingDTO();
		dto.setPromotion(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setWarning(false);
		postingService.promotePosting(createInvalidUser(), validPosting, dto);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void promotePostingLocked() throws Exception {
		PromotePostingDTO dto = new PromotePostingDTO();
		dto.setPromotion(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setWarning(false);
		Posting posting = createValidPosting();
		posting.setLocked(true);
		postingService.promotePosting(createValidUser(), posting, dto);
	}
	
	@Test
	public void promotePostingSelf() throws Exception {
		PromotePostingDTO dto = new PromotePostingDTO();
		dto.setPromotion(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setWarning(false);
		User user = createValidUser();
		user.setUsername(validName);
		user.setId(validUserId);
		Posting posting = createValidPosting();
		posting.setAuthor(new CachedUsername(validUserId, validName));
		postingService.promotePosting(user, posting, dto);
	}
	
	@Test
	public void promotePostingTagCount() throws Exception {
		PromotePostingDTO dto = new PromotePostingDTO();
		dto.setPromotion(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setWarning(false);
		List<String> tags = new ArrayList<String>();
		for(int i = 0; i < (ServiceValues.POSTING_TAGS_TOTAL_MAX + 1); i++) {
			tags.add(validTag + i);
		}
		postingService.promotePosting(createValidUser(), validPosting, dto);
	}
	
	@Test
	public void promotePosting() throws Exception {
		PromotePostingDTO dto = new PromotePostingDTO();
		dto.setPromotion(validCost);
		dto.setTags(new ArrayList<String>());
		dto.setWarning(false);
		postingService.promotePosting(createValidUser(), validPosting, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementPostingTagsNull1() throws Exception {
		Map<String, Long> tags = new HashMap<String, Long>();
		tags.put(validTag, validCost);
		postingService.incrementPostingTags(null, tags.keySet(), validCost, 1L, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementPostingTagsNull2() throws Exception {
		postingService.incrementPostingTags(validObjectId, null, validCost, 1L, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementPostingTagsNull5() throws Exception {
		Map<String, Long> tags = new HashMap<String, Long>();
		tags.put(validTag, validCost);
		postingService.incrementPostingTags(validObjectId, tags.keySet(), validCost, 1L, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementPostingTagsInvalid5() throws Exception {
		Map<String, Long> tags = new HashMap<String, Long>();
		tags.put(validTag, validCost);
		postingService.incrementPostingTags(validObjectId, tags.keySet(), validCost, 1L, invalidLanguage);
	}

	@Test
	public void incrementPostingTags() throws Exception {
		Map<String, Long> tags = new HashMap<String, Long>();
		tags.put(validTag, validCost);
		postingService.incrementPostingTags(validObjectId, tags.keySet(), validCost, 1L, validLanguage);
		postingService.incrementPostingTags(validObjectId, tags.keySet(), 0L, 1L, validLanguage);
		postingService.incrementPostingTags(validObjectId, tags.keySet(), validCost, 0L, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementCommentCountNull() throws Exception {
		postingService.incrementCommentCount(null, randomBoolean());
	}

	@Test
	public void incrementCommentCountIncrement() throws Exception {
		postingService.incrementCommentCount(validObjectId, true);
		verify(postingDao).incrementCommentCount(validObjectId, 1L);
	}

	@Test
	public void incrementCommentCountDecrement() throws Exception {
		postingService.incrementCommentCount(validObjectId, false);
		verify(postingDao).incrementCommentCount(validObjectId, -1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementAppreciationPromotionCountNull() throws Exception {
		postingService.incrementAppreciationPromotionCount(null, randomBoolean(), 
				randomBoolean());
	}

	@Test
	public void incrementAppreciationPromotionCountIncrement() throws Exception {
		postingService.incrementAppreciationPromotionCount(validObjectId, false, true);
		verify(postingDao).incrementAppreciationPromotionCount(validObjectId, null, 1L);
	}

	@Test
	public void incrementAppreciationCountDecrement() throws Exception {
		postingService.incrementAppreciationPromotionCount(validObjectId, true, false);
		verify(postingDao).incrementAppreciationPromotionCount(validObjectId, -1L, -1L);
	}
	
	@Test(expected = BadParameterException.class)
	public void incrementCommentTallyApproximationNull1() throws Exception {
		postingService.incrementCommentTallyApproximation(null, validAppreciation, 
				validCost, validCost);
	}

	@Test
	public void incrementCommentTallyApproximation() throws Exception {
		postingService.incrementCommentTallyApproximation(validObjectId, null, null, null);
		postingService.incrementCommentTallyApproximation(validObjectId, null, null, 
				validCost);
		postingService.incrementCommentTallyApproximation(validObjectId, null, validCost, 
				null);
		postingService.incrementCommentTallyApproximation(validObjectId, null, validCost, 
				validCost);
		postingService.incrementCommentTallyApproximation(validObjectId, validAppreciation, 
				null, null);
		postingService.incrementCommentTallyApproximation(validObjectId, validAppreciation, 
				null, validCost);
		postingService.incrementCommentTallyApproximation(validObjectId, validAppreciation, 
				validCost, null);
		postingService.incrementCommentTallyApproximation(validObjectId, validAppreciation, 
				validCost, validCost);
		
		verify(postingDao, times(4)).incrementCommentTallyCost(validObjectId, validCost);
		verify(postingDao, times(6)).incrementCommentTallyAppreciationPromotion(
				any(ObjectId.class), anyLong(), anyLong());
	}
	
	@Test(expected = BadParameterException.class)
	public void updateAggregateNull1() throws Exception {
		postingService.updateAggregate(null, validCost, TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void updateAggregateNull3() throws Exception {
		postingService.updateAggregate(validObjectId, validCost, null);
	}
	
	@Test
	public void updateAggregate() throws Exception {
		postingService.updateAggregate(validObjectId, validCost, TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void aggregatePostingsNull() throws Exception {
		postingService.aggregatePostings(null);
	}
	
	@Test
	public void aggregatePostings() throws Exception {
		AggregationOutput output = mock(AggregationOutput.class);
		when(output.results()).thenReturn(new ArrayList<DBObject>());
		when(aggregationDao.getAggregation(any(AGGREGATION_TYPE.class), 
				any(Date.class),any(TIME_OPTION.class), anyLong()))
				.thenReturn(output);
		postingService.aggregatePostings(TIME_OPTION.DAY);
	}
	
	@Test(expected = BadParameterException.class)
	public void enablePostingNull1() throws Exception {
		postingService.enablePosting(null, validPosting);
	}
	
	@Test(expected = BadParameterException.class)
	public void enablePostingNull2() throws Exception {
		postingService.enablePosting(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void enablePostingInvalid() throws Exception {
		postingService.enablePosting(createInvalidUser(), validPosting);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void enablePostingNotAllowed() throws Exception {
		postingService.enablePosting(createValidUser(), validPosting);
	}
	
	@Test
	public void enablePosting() throws Exception {
		User user = createValidUser();
		user.setId(validUserId);
		user.setUsername(validName);
		Posting posting = createValidPosting();
		posting.setEnabled(false);
		posting.setAuthor(new CachedUsername(validUserId, validName));
		postingService.enablePosting(createValidUser(), posting);
		posting.setEnabled(true);
		postingService.enablePosting(createValidUser(), posting);
	}
	
	@Test(expected = BadParameterException.class)
	public void disablePostingNull1() throws Exception {
		postingService.disablePosting(null, validPosting);
	}
	
	@Test(expected = BadParameterException.class)
	public void disablePostingNull2() throws Exception {
		postingService.disablePosting(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void disablePostingInvalid() throws Exception {
		postingService.disablePosting(createInvalidUser(), validPosting);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void disablePostingNotAllowed() throws Exception {
		postingService.disablePosting(createValidUser(), validPosting);
	}
	
	@Test
	public void disablePosting() throws Exception {
		User user = createValidUser();
		user.setId(validUserId);
		user.setUsername(validName);
		Posting posting = createValidPosting();
		posting.setEnabled(true);
		posting.setAuthor(new CachedUsername(validUserId, validName));
		postingService.disablePosting(createValidUser(), posting);
		posting.setEnabled(false);
		postingService.disablePosting(createValidUser(), posting);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull1() throws Exception {
		postingService.flag(null, createValidUserInfo(), validPosting, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull2() throws Exception {
		postingService.flag(createValidUser(), null, validPosting, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull3() throws Exception {
		postingService.flag(createValidUser(), createValidUserInfo(), null, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagNull4() throws Exception {
		postingService.flag(createValidUser(), createValidUserInfo(), validPosting, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void flagInvalid1() throws Exception {
		postingService.flag(createInvalidUser(), createValidUserInfo(), validPosting, FLAG_REASON.ILLICIT);
	}
	
	@Test
	public void flag() throws Exception {
		Posting posting = createValidPosting();
		posting.setFlagged(true);
		postingService.flag(createValidUser(), createValidUserInfo(), posting, FLAG_REASON.ILLICIT);
		posting.setFlagged(false);
		postingService.flag(createValidUser(), createValidUserInfo(), posting, FLAG_REASON.ILLICIT);
		User user = createValidUser();
		user.setId(validUserId);
		List<ObjectId> votes = new ArrayList<ObjectId>();
		votes.add(validUserId);
		posting.setVotes(votes);
		postingService.flag(createValidUser(), createValidUserInfo(), posting, FLAG_REASON.ILLICIT);
	}
	
	@Test(expected = BadParameterException.class)
	public void canAppreciateNull() throws Exception {
		postingService.canAppreciate(null);
	}
	
	@Test
	public void canAppreciate() throws Exception {
		Posting posting = createValidPosting();
		Assert.assertTrue(postingService.canAppreciate(posting));
		posting.setAuthor(null);
		Assert.assertFalse(postingService.canAppreciate(posting));
		posting.setAuthor(new CachedUsername(null, validName));
		Assert.assertFalse(postingService.canAppreciate(posting));
		posting.setAuthor(new CachedUsername(validObjectId, validName));
		Assert.assertTrue(postingService.canAppreciate(posting));
		posting.setLocked(true);
		Assert.assertFalse(postingService.canAppreciate(posting));
		posting.setLocked(false);
		posting.setEnabled(false);
		Assert.assertFalse(postingService.canAppreciate(posting));
		posting.setEnabled(true);
		posting.setInitialized(false);
		Assert.assertFalse(postingService.canAppreciate(posting));
		posting.setInitialized(true);
		posting.setFlagged(true);
		Assert.assertFalse(postingService.canAppreciate(posting));
		posting.setFlagged(false);
		posting.setRemoved(true);
		Assert.assertFalse(postingService.canAppreciate(posting));
		posting.setRemoved(false);
		Assert.assertTrue(postingService.canAppreciate(posting));
	}
	
	@Test(expected = BadParameterException.class)
	public void canCommentNull() throws Exception {
		postingService.canAppreciate(null);
	}
	
	@Test
	public void canComment() throws Exception {
		Posting posting = createValidPosting();
		Assert.assertTrue(postingService.canComment(posting));
		posting.setLocked(true);
		Assert.assertFalse(postingService.canComment(posting));
		posting.setLocked(false);
		posting.setEnabled(false);
		Assert.assertFalse(postingService.canComment(posting));
		posting.setEnabled(true);
		posting.setInitialized(false);
		Assert.assertFalse(postingService.canComment(posting));
		posting.setInitialized(true);
		posting.setFlagged(true);
		Assert.assertFalse(postingService.canComment(posting));
		posting.setFlagged(false);
		posting.setRemoved(true);
		Assert.assertFalse(postingService.canComment(posting));
		posting.setRemoved(false);
		Assert.assertTrue(postingService.canComment(posting));
	}
}
