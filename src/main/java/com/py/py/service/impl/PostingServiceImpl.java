package com.py.py.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.mongodb.DBObject;
import com.py.py.constants.ArchivalTimes;
import com.py.py.constants.DomainRegex;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.PostingDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Escrow;
import com.py.py.domain.Posting;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.domain.subdomain.ImageLink;
import com.py.py.dto.in.PromotePostingDTO;
import com.py.py.dto.in.SubmitEditPostingDTO;
import com.py.py.dto.in.SubmitPostingDTO;
import com.py.py.dto.out.PostingDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.dto.out.TotalValueDTO;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.service.CommentService;
import com.py.py.service.EscrowService;
import com.py.py.service.EventService;
import com.py.py.service.FinanceService;
import com.py.py.service.FlagService;
import com.py.py.service.PostingService;
import com.py.py.service.TagService;
import com.py.py.service.UserService;
import com.py.py.service.base.BaseAggregator;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BackerNotFoundException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.BalanceException;
import com.py.py.service.exception.FinanceException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.exception.TagCountException;
import com.py.py.service.exception.TagLockedException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;

public class PostingServiceImpl extends BaseAggregator implements PostingService {

	protected static final PyLogger logger = PyLogger.getLogger(PostingServiceImpl.class);
	
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
	private FlagService flagService;
	/*
	@Autowired
	private DefaultsFactory defaultsFactory;
	*/

	protected Page<PostingDTO> getPreviewDTOs(String language, ObjectId authorId, 
			Pageable pageable, Filter filter, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(pageable, filter);
		// do not check author or beneficiary
		String correctLanguage = ServiceUtils.getLanguageOrNull(language);
		List<PostingDTO> dtolist = ModelFactory.<PostingDTO>constructList();
		Page<Posting> page = new PageImpl<Posting>(new ArrayList<Posting>(), pageable, 0);
		
		try {
			page = postingDao.getSortedPostings(correctLanguage, authorId, pageable, filter);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(page == null) {
			throw new ServiceException();
		}
		
		//Boolean warning = filter.getWarning();
		String replacement = null;
		/*if(warning != null && !warning) {
			replacement = defaultsFactory.getWarningContentReplacement();
		}*/
		
		for(Posting p : page.getContent()) {
			try {
				dtolist.add(Mapper.mapPostingDTO(p, replacement, canAppreciate(p), canComment(p), preview));
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of posting preview!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping of posting preview!", e);
			}
		}
		
		return new PageImpl<PostingDTO>(dtolist, pageable, page.getTotalElements());
	}

	protected Page<PostingDTO> getPreviewByUserDTOs(ObjectId authorId, 
			ObjectId beneficiaryId, Pageable pageable, boolean showDisabled, 
			List<String> tags, Boolean warning, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		if(authorId == null && beneficiaryId == null) {
			throw new BadParameterException(); 
		}
		
		List<PostingDTO> dtolist = ModelFactory.<PostingDTO>constructList();
		Page<Posting> page = new PageImpl<Posting>(new ArrayList<Posting>(), pageable, 0);
		
		try {
			page = postingDao.getUserPostings(authorId, beneficiaryId, pageable, 
					showDisabled, tags, warning);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(page == null) {
			throw new ServiceException();
		}
		
		String replacement = null;
		/*
		if(warning != null && !warning) {
			replacement = defaultsFactory.getWarningContentReplacement();
		}*/
		
		for(Posting p : page.getContent()) {
			try {
				dtolist.add(Mapper.mapPostingDTO(p, replacement, canAppreciate(p), canComment(p), preview));
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of user posting preview!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping of user posting preview!", e);
			}
		}
		
		return new PageImpl<PostingDTO>(dtolist,pageable,page.getTotalElements());
	}
	
	@Override
	public Page<PostingDTO> getPostingPreviews(String language, ObjectId authorId, 
			Pageable pageable, Filter filter, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(language, pageable, filter);
		return getPreviewDTOs(language, authorId, pageable, filter, preview);
	}
	
	@Override
	public Page<PostingDTO> getAuthorPreviews(ObjectId authorId,
			Pageable pageable, List<String> tags, Boolean warning, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(authorId, pageable);
		return getPreviewByUserDTOs(authorId, null, pageable, false, tags, warning, preview);
	}
	
	@Override
	public Page<PostingDTO> getBeneficiaryPreviews(ObjectId beneficiaryId, 
			Pageable pageable, List<String> tags, Boolean warning, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(beneficiaryId, pageable);
		return getPreviewByUserDTOs(null, beneficiaryId, pageable, false, tags, warning, preview);
	}
	
	@Override
	public Page<PostingDTO> getSelfPreviews(ObjectId userId, 
			Pageable pageable, List<String> tags, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(userId, pageable);
		return getPreviewByUserDTOs(userId, null, pageable, true, tags, null, preview);		
	}

	@Override
	public ObjectId createPosting(User user, SubmitPostingDTO dto, String language) 
			throws ServiceException {
		ArgCheck.nullCheck(dto, language);
		ArgCheck.userCheck(user);
		
		String correctLanguage = ServiceUtils.getLanguage(language);

		CachedUsername author = new CachedUsername(user.getId(), user.getUsername());
		Date created = new Date();

		EscrowSourceTarget escrowId = null;
		
		String backerName = dto.getBacker();
		CachedUsername cachedBacker = null;
		if(backerName != null && !backerName.isEmpty()) {
			try {
				Escrow escrow = escrowService.getBackerEscrow(null, backerName, 
						user.getId(), user.getUsername());
				escrowId = escrow.getId();
				ObjectId backerId = null;
				if(ObjectId.isValid(escrow.getSource())) {
					backerId = new ObjectId(escrow.getSource());
				}
				cachedBacker = new CachedUsername(backerId, escrow.getSourceName());
			} catch(NotFoundException nfe) {
				throw new BackerNotFoundException(backerName);
			}
		}
		
		Posting posting = Mapper.mapPosting(dto, author, cachedBacker, 
				created, correctLanguage);
		
		ObjectId postingId = null;
		
		try {
			// no need to evict posting from cache, it is done when charged
			posting = postingDao.save(posting);
			if(posting == null) {
				throw new ServiceException();
			}
			postingId = posting.getId();
			long cost = dto.getCost();
			boolean paid = false;
			if(cost > 0) {
				paid = true;
			} else if(escrowId != null) {
				// cant back for 0
				throw new ActionNotAllowedException();
			}
			
			
			if(paid) {
				if(escrowId == null) {
					financeService.charge(user.getId(), postingId, 
						true, CollectionNames.POSTING, cost);
				} else {
					financeService.charge(escrowId,  postingId, 
						true, CollectionNames.POSTING, cost);
				}
				try {
					updateAggregates(postingId.toHexString(), AGGREGATION_TYPE.POSTING, cost);
				} catch(ServiceException se) {
					// do nothing, user has been charged
					logger.warn("Could not update aggregates for posting id {" + postingId.toHexString() + "}.", se);
				}
			} else {
				try {
					postingDao.setEnabled(postingId, null, null, null, null, null, true);
				} catch(DaoException de) {
					// do nothing, user has been charged
					logger.warn("Could not update initialized status for posting id {" + postingId.toHexString() + "}.", de);
				}
			}
			
			Map<String, Long> tags = ServiceUtils.getTagMap(dto.getTags(), cost);
			try {
				incrementPostingTags(posting.getId(), tags.keySet(), cost, null, correctLanguage);
			} catch(TagLockedException tle) {
				// do nothing, silent for user
			}
			
			// if cost is 0, it only increments the posting count
			try {
				userService.incrementContributedCost(user.getId(), cost, true);
			} catch(ServiceException se) {
				// do nothing
				logger.warn("Could not increment contributed cost for posting id {" + postingId.toHexString() + "}.", se);
			}
			try {
				eventService.eventPosting(author, cachedBacker, posting.getTitle(), 
						postingId, cost);
			} catch(Exception e) {
				// do nothing, user has been charged
				logger.warn("Could not create event posting id {" + postingId.toHexString() + "}.", e);
			}
			logger.info("Posting with id {" + postingId + "} was created!");
			return postingId;
		} catch(BalanceException be) {
			createPostingError(postingId);
			throw be;
		} catch(FinanceException fe) {
			createPostingError(postingId);
			throw fe;
		} catch(BadParameterException bpe) {
			createPostingError(postingId);
			throw bpe;
		} catch(NotFoundException nfe) {
			createPostingError(postingId);
			throw nfe;
		} catch(Exception e) {
			createPostingError(postingId);
			throw new ServiceException(e);
		}
	}
	
	@Override
	public void editPosting(ObjectId userId, Posting posting, SubmitEditPostingDTO dto) throws ServiceException {
		ArgCheck.nullCheck(userId, posting, dto);
		
		if(!PyUtils.objectIdCompare(posting.getAuthor().getId(), userId)) {
			throw new ActionNotAllowedException();
		}
		
		String title = ServiceUtils.getTitle(dto.getTitle());
		String content = ServiceUtils.getContent(dto.getContent());
		String preview = ServiceUtils.getPreview(dto.getPreview());
		ImageLink imageLink = Mapper.mapImageLink(dto.getImageLink(), dto.getImageWidth(), dto.getImageHeight());
		
		try {
			postingDao.updatePosting(posting.getId(), title, content, preview, imageLink);
		} catch(DaoException de) {
			throw new ServiceException(de);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	protected void createPostingError(ObjectId pid) {
		if(pid != null) {
			try {
				postingDao.delete(pid);
			} catch(Exception e) {
				// do nothing!
				logger.warn("Could not delete created posting {" + pid + "}.", e);
			}
		}
	}
	
	@Override
	public ResultSuccessDTO createPostingDTO(User user, SubmitPostingDTO dto, 
			String language) throws ServiceException {
		ObjectId result = createPosting(user, dto, language);
		return Mapper.mapResultSuccessDTO(result.toHexString());
	}
	
	@Override
	public PostingDTO getPostingDTO(ObjectId authorId, Posting posting, Boolean warning) 
			throws ServiceException {
		// do not check authorId
		ArgCheck.nullCheck(posting);
		
		String replacement = null;
		/*
		if(warning != null && !warning) {
			replacement = defaultsFactory.getWarningContentReplacement();
		}*/
		
		if(authorId != null && posting.getAuthor() != null 
				&& posting.getAuthor().getId() != null
				&& PyUtils.objectIdCompare(authorId, posting.getAuthor().getId())) {
			return Mapper.mapPostingDTOFull(posting, canAppreciate(posting), 
					canComment(posting));
		} else {
			return Mapper.mapPostingDTO(posting, replacement, canAppreciate(posting), 
				canComment(posting), false);
		}
	}
	
	@Override
	public Posting getCachedPosting(ObjectId postingId) throws ServiceException {
		return getPosting(postingId, true);
	}
	
	@Override
	public Posting getPosting(ObjectId postingId) throws ServiceException {
		return getPosting(postingId, false);
	}
	
	private Posting getPosting(ObjectId postingId, boolean cached) throws ServiceException {
		ArgCheck.nullCheck(postingId);
		
		try {
			Posting posting;
			if(cached) {
				posting = postingDao.findCachedPosting(postingId);
			} else {
				posting = postingDao.findPosting(postingId);
			}
			if(posting == null) {
				throw new NotFoundException(postingId.toHexString());
			}
			return posting;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void appreciatePosting(ObjectId paymentId, User user, Posting posting, 
			long appreciationAmount, long promotionAmount, List<String> tags, 
			boolean warning) throws ServiceException {
		ArgCheck.nullCheck(paymentId, posting);
		ArgCheck.userCheck(user);
		promotePosting(user, posting, appreciationAmount, promotionAmount, tags, 
				warning, paymentId);
	}
	
	@Override
	public void promotePosting(User user, Posting posting, PromotePostingDTO dto)
			throws ServiceException {
		ArgCheck.nullCheck(posting, dto);
		ArgCheck.userCheck(user);
		
		if(!canAppreciate(posting)) {
			throw new ActionNotAllowedException();
		}
		
		promotePosting(user, posting, null, dto.getPromotion(), dto.getTags(), 
				dto.isWarning(), null);
	}
	
	protected void promotePosting(User user, Posting posting, Long appreciationAmount, 
			long promotionAmount, List<String> tags, boolean warning, ObjectId paymentId)
					throws ServiceException {
		ArgCheck.nullCheck(posting);
		ArgCheck.userCheck(user);
		if(promotionAmount <= 0L) {
			throw new BadParameterException();
		}
		
		ObjectId postingId = posting.getId();
		
		CachedUsername cachedTarget = posting.getAuthor();
		ObjectId target = null;
		if(cachedTarget != null) {
			target = cachedTarget.getId();
		}
		
		boolean isAppreciation = false;
		if(appreciationAmount != null) {
			isAppreciation = true;
		}
		
		boolean tagFailure = false;
		TagLockedException tagLocked = null;
		String tagFailureString = "";
		
		Map<String,Long> postingTags = posting.getTagValues();
		if(postingTags == null) {
			postingTags = new HashMap<String, Long>();
		}
		
		CachedUsername beneficiary = null;
		if(posting.getBeneficiary() != null && posting.getBeneficiary().getId() != null) {
			beneficiary = posting.getBeneficiary();
		}

		ObjectId source = user.getId();
		
		if(isAppreciation) {
			financeService.appreciate(source, postingId, CollectionNames.POSTING, 
					appreciationAmount, promotionAmount, paymentId);
		} else {
			financeService.promote(source, postingId, CollectionNames.POSTING, 
					promotionAmount);
		}
		
		try {
			CachedUsername appreciationTarget = null;
			if(isAppreciation) {
				appreciationTarget = cachedTarget;
			}
			userService.incrementContributedAppreciationPromotion(source, 
					appreciationTarget, appreciationAmount, promotionAmount);
		} catch(ServiceException se) {
			// do nothing
			logger.warn("Could not increment contributed promotion for source {" + source + "} on posting {" + postingId + "} !", se);
		}
		try {
			userService.incrementAppreciationPromotion(target, appreciationAmount, 
					promotionAmount);
		} catch(ServiceException se) {
			// do nothing
			logger.warn("Could not increment promotion for target {" + target + "} on posting {" + postingId + "} !", se);
		}
		try {
			incrementAppreciationPromotionCount(postingId, isAppreciation, true);
		} catch(ServiceException se) {
			// do nothing
			logger.warn("Could not increment promotion count for posting {" + postingId + "} !", se);
		}
		try {
			updateAggregates(postingId.toHexString(), AGGREGATION_TYPE.POSTING, 
					promotionAmount);
		} catch(ServiceException se) {
			// do nothing
			logger.warn("Could not update aggregates on posting {" + postingId + "} !", se);
		}
		try {
			Map<String, Long> totalTags = new HashMap<String, Long>();
			Map<String, Long> tagsMap = new HashMap<String, Long>();
			Map<String, Long> desiredTags = ServiceUtils.getTagMap(tags, promotionAmount);
			if(postingTags != null && !postingTags.isEmpty()) {
				totalTags.putAll(postingTags);
				if(tags == null || tags.isEmpty()) {
					for(Map.Entry<String, Long> entry : postingTags.entrySet()) {
						desiredTags.put(entry.getKey(), entry.getValue());
					}
				}
			}
			if(!desiredTags.isEmpty()) {
				totalTags.putAll(desiredTags);
				if(totalTags.size() > ServiceValues.POSTING_TAGS_TOTAL_MAX) {
					// only add specific tags
					for(Map.Entry<String, Long> entry: desiredTags.entrySet()) {
						if(postingTags.containsKey(entry.getKey())) {
							tagsMap.put(entry.getKey(), entry.getValue());
						} else {
							if(tagFailureString.isEmpty()) {
								tagFailureString = entry.getKey();
							} else {
								tagFailureString = tagFailureString.concat(
										DomainRegex.TAG_DELIMITER_STRING + entry.getKey());
							}
						}
					}
					tagFailure = true;
				} else {
					tagsMap = desiredTags;
				}
				try {
					incrementPostingTags(postingId, tagsMap.keySet(), promotionAmount, appreciationAmount, posting.getLanguage());
				} catch(TagLockedException tle) {
					tagLocked = tle;
				}
			}
			if(warning) {
				warn(posting, promotionAmount);
			}
			
			CachedUsername cachedSource = new CachedUsername(user.getId(), 
					user.getUsername());
			
			if(isAppreciation) {
				eventService.eventAppreciationPosting(cachedSource, beneficiary, postingId, 
					cachedTarget, posting.getTitle(), appreciationAmount);
			} else {
				eventService.eventPromotionPosting(cachedSource, beneficiary, postingId,
					cachedTarget, posting.getTitle(), promotionAmount);
			}
			if(tagFailure) {
				// silent exception, no response currently
				logger.info("Tag could not be incremented '" + tagFailureString + "' !");
				//throw new TagCountException(tagFailureString);
			}
			if(tagLocked != null) {
				// silent exception, no response currently
				//throw tagLocked;
			}
		} catch(TagLockedException tle) {
			//throw tle;
		} catch(TagCountException tce) {
			//throw tce;
		} catch(ServiceException se) {
			// do nothing, user has been charged
			logger.warn("Could not create appreciation or promotion event for posting {" + postingId + "} !", se);
		} catch(Exception e) {
			// do nothing, user has been charged
			logger.warn("Could not create appreciation or promotion event for posting {" + postingId + "} !", e);
		}
	}
	
	@Override
	public void incrementPostingTags(ObjectId postingId, Set<String> tags, long amount, Long appreciationAmount, String language)
			throws ServiceException {
		ArgCheck.nullCheck(postingId, tags);
		Map<String, Long> finalTags = new HashMap<String, Long>();
		String correctLanguage = ServiceUtils.getLanguage(language);

		String tagLocked = null;
		for(String tag : tags) {
			String name = null;
			try {
				// this is expensive. keep the max added short!
				name = ServiceUtils.getTag(tag);
				tagService.incrementTag(name, correctLanguage, amount, appreciationAmount);
				if(amount > 0) {
					finalTags.put(name, amount);
				}
			} catch(ActionNotAllowedException anae) {
				logger.debug("Tag '" + name + "' was locked and could not be incremented.", anae);
				tagLocked = name;
			} catch(BadParameterException bpe) {
				logger.debug("Tag '" + name + "' was not valid and could not be incremented.", bpe);
			}
		}
		if(finalTags.size() > 0) {
			try {
				postingDao.incrementTagValues(postingId, finalTags);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
		}
		if(tagLocked != null) {
			throw new TagLockedException(tagLocked);
		}
	}

	@Override
	public void incrementCommentCount(ObjectId postingId, boolean increment)
			throws ServiceException {
		ArgCheck.nullCheck(postingId);
		
		try {
			if(increment) {
				postingDao.incrementCommentCount(postingId, 1);
			} else {
				postingDao.incrementCommentCount(postingId, -1);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementAppreciationPromotionCount(ObjectId postingId, boolean appreciation, 
			boolean increment) throws ServiceException {
		ArgCheck.nullCheck(postingId);
		
		Long i = 1L;
		if(increment) {
			i = 1L;
		} else {
			i = -1L;
		}
		
		Long appreciationIncrement = null;
		Long promotionIncrement = i;
		if(appreciation) {
			appreciationIncrement = i;
		}
		
		try {
			postingDao.incrementAppreciationPromotionCount(postingId, appreciationIncrement, 
					promotionIncrement);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementCommentTallyApproximation(ObjectId postingId, 
			Long appreciationIncrement, Long promotionIncrement, Long cost)
					throws ServiceException {
		// arguments may be null, do not update nulls
		ArgCheck.nullCheck(postingId);
		
		try {
			if(cost != null) {
				postingDao.incrementCommentTallyCost(postingId, cost);
			}
			if(appreciationIncrement != null || promotionIncrement != null) {
				postingDao.incrementCommentTallyAppreciationPromotion(postingId, 
						appreciationIncrement, promotionIncrement);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void updateAggregate(ObjectId postingId, long value, TIME_OPTION segment)
			throws ServiceException {
		ArgCheck.nullCheck(postingId, segment);
		
		try {
			postingDao.updateAggregation(postingId, value, segment);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void aggregatePostings(TIME_OPTION segment) throws ServiceException {
		ArgCheck.nullCheck(segment);
		
		Iterable<DBObject> results = aggregate(AGGREGATION_TYPE.POSTING, segment);

		for(DBObject obj : results) {
			@SuppressWarnings("rawtypes")
			Map map = obj.toMap();
			ObjectId id = new ObjectId((String) map.get("_id"));
			long total = (Long) map.get("total");
			updateAggregate(id, total, segment);
		}
		
		try {
			postingDao.emptyAggregations(segment);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void aggregateTotals() throws ServiceException {
		updateAggregateTotals(AGGREGATION_TYPE.POSTING);
	}
	
	@Override
	public void sortTags(Date lastPromotion) throws ServiceException {
		ArgCheck.nullCheck(lastPromotion);
		
		for(int i = 0; i < ServiceValues.POSTING_TAG_SORT_MAX_BATCHES; i++) {
			try {
				Pageable pageable = new PageRequest(i, ServiceValues.POSTING_TAG_SORT_BATCH_SIZE);
				Page<Posting> postings = postingDao.getPostingsByLastPromotion(lastPromotion, pageable);
				if(postings.hasContent()) {
					for(Posting posting : postings.getContent()) {
						List<Map.Entry<String, Long> > list = PyUtils.sortByValueDescending(posting.getTagValues());
						List<String> tags = new ArrayList<String>();
						int j = 0;
						for(Map.Entry<String, Long> entry : list) {
							if(j >= ServiceValues.POSTING_TAGS_TOTAL) {
								break;
							}
							tags.add(entry.getKey());
							j++;
						}
						try {
							postingDao.setTags(posting.getId(), tags);
						} catch(DaoException de) {
							logger.info("Could not set tags of posting id {" + posting.getId().toHexString() + "}.", de);
						}
					}
				}
				if(!postings.hasNext()) {
					return;
				}
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
		}
	}
	
	@Override
	public void enablePosting(User user, Posting posting) throws ServiceException {
		ArgCheck.nullCheck(posting);
		ArgCheck.userCheck(user);
		
		CachedUsername author = posting.getAuthor();
		
		if(author == null || !PyUtils.objectIdCompare(user.getId(), author.getId())) {
			throw new ActionNotAllowedException();
		}

		if(posting.isEnabled()) {
			return;
		}
		
		try {
			postingDao.setEnabled(posting.getId(), true, null, null, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void disablePosting(User user, Posting posting) throws ServiceException {
		ArgCheck.nullCheck(posting);
		ArgCheck.userCheck(user);
		
		CachedUsername author = posting.getAuthor();
		
		if(author == null || !PyUtils.objectIdCompare(user.getId(), author.getId())) {
			throw new ActionNotAllowedException();
		}
		
		if(!posting.isEnabled()) {
			return;
		}
		try {
			postingDao.setEnabled(posting.getId(), false, null, null, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void unremovePosting(Posting posting) throws ServiceException {
		ArgCheck.nullCheck(posting);
		
		if(!posting.isRemoved()) {
			return;
		}
		try {
			postingDao.setEnabled(posting.getId(), null, false, null, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removePosting(Posting posting, boolean sendEvent) throws ServiceException {
		ArgCheck.nullCheck(posting);
		
		if(posting.isRemoved()) {
			return;
		}
		try {
			postingDao.setEnabled(posting.getId(), null, true, null, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		if(sendEvent) {
			eventService.eventPostingInfringement(posting.getAuthor(), posting.getBeneficiary(), posting.getTitle(), posting.getId());
		}
	}
	
	protected void warn(Posting posting, long amount) throws ServiceException {
		ArgCheck.nullCheck(posting);
		if(posting.isWarning()) {
			// no need
			return;
		}
		long appreciation = 0l;
		if(posting.getTally() != null) {
			appreciation = posting.getTally().getAppreciation();
		}
		ObjectId postingId = posting.getId();
		
		Boolean warn = null;
		if(PyUtils.overWarnThreshold(appreciation, posting.getWarningValue() + amount)) {
			warn = true;
		}

		try {
			postingDao.addWarn(postingId, warn, amount);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void flag(User user, UserInfo userInfo, Posting posting, FLAG_REASON reason) throws ServiceException {
		ArgCheck.nullCheck(userInfo, posting, reason);
		ArgCheck.userCheck(user);
		
		long weight = userService.getWeight(user, userInfo);
		
		if(posting.isFlagged()) {
			// no need
			return;
		}
		if(posting.getVotes() != null 
				&& posting.getVotes().contains(user.getId())) {
			checkFlag(user, posting, 0L);
			return;
		} else {
			checkFlag(user, posting, weight);
			try {
				flagService.addData(posting.getId(), FLAG_TYPE.POSTING, posting.getTitle(), weight, reason);
			} catch(ServiceException se) {
				//continue
			}
			try {
				if(posting.getAuthor() != null && posting.getAuthor().getId() != null) {
					User targetUser = userService.findUser(posting.getAuthor().getId());
					UserInfo targetUserInfo = userService.findUserInfo(targetUser);
					userService.flag(user, userInfo, targetUser, targetUserInfo, null);
				}
			} catch(NotFoundException nfe) {
				// do nothing, just cant flag the user
			}
		}
		
		try {
			postingDao.addVote(posting.getId(), user.getId(), weight);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected void checkFlag(User user, Posting posting, long addedWeight)
			throws ServiceException {
		Date now = new Date();
		Date inactiveSince = new Date(now.getTime() - ServiceValues.POSTING_INACTIVE);
		Date created = new Date();
		if(posting.getCreated() != null) {
			created = posting.getCreated();
		}
		if(inactiveSince.after(created)) {
			// it went inactive since being created
			return;
		}
		
		long flagValue = posting.getFlagValue() + addedWeight;
		int flagCount = 0;
		if(posting.getVotes() != null) {
			flagCount = posting.getVotes().size();
		}
		long value = 0;
		if(posting.getTally() != null) {
			value = posting.getTally().getValue();
		}
		if(PyUtils.overFlagThreshold(value, posting.getAppreciationCount(), 
				flagValue, flagCount)) {
			try {
				postingDao.setEnabled(posting.getId(), null, null, true, null, null, null);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
		}
	}
	
	@Override
	public void markArchived() throws ServiceException {
		Date then = PyUtils.getOldDate(ArchivalTimes.POSTING_START);
		try {
			postingDao.markArchived(then);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public boolean canAppreciate(Posting posting) throws ServiceException {
		ArgCheck.nullCheck(posting);
		
		if(posting.isEnabled() && !posting.isLocked() && posting.isInitialized() 
				&& !posting.isFlagged() && !posting.isRemoved() && posting.getArchived() == null) {
			if(!ServiceUtils.isUsernameDeleted(posting.getAuthor())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean canComment(Posting posting) throws ServiceException {
		ArgCheck.nullCheck(posting);
		
		if(posting.isEnabled() && !posting.isLocked() && posting.isInitialized() 
				&& !posting.isFlagged() && !posting.isRemoved() && posting.getArchived() == null) {
			return true;
		}
		return false;
	}
	
	@Override
	public Map<TIME_OPTION, BigInteger> getAggregateTotals() throws ServiceException {
		return getAggregateTotals(AGGREGATION_TYPE.POSTING);
	}
	
	@Override
	public TotalValueDTO getTotalValueDTO() throws ServiceException {
		Map<TIME_OPTION, BigInteger> allTotals = new HashMap<TIME_OPTION, BigInteger>();
		Map<TIME_OPTION, BigInteger> postingTotals = getAggregateTotals(); 
		Map<TIME_OPTION, BigInteger> commentTotals = commentService.getAggregateTotals();
		ArgCheck.nullCheck(allTotals, postingTotals, commentTotals);
		for(Map.Entry<TIME_OPTION, BigInteger> entry : postingTotals.entrySet()) {
			BigInteger total = BigInteger.valueOf(0L);
			BigInteger pTotal = entry.getValue();
			BigInteger cTotal = commentTotals.get(entry.getKey());
			if(pTotal != null) {
				total = total.add(pTotal);
			}
			if(cTotal != null) {
				total = total.add(cTotal);
			}
			allTotals.put(entry.getKey(), total);
		}
		return Mapper.mapTotalValueDTO(postingTotals, 
				commentTotals, 
				userService.getAggregateTotals(), 
				tagService.getAggregateTotals(), 
				allTotals);
	}
	
}