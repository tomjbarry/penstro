package com.py.py.service.impl;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.mongodb.DBObject;
import com.py.py.constants.ArchivalTimes;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.CommentDao;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Comment;
import com.py.py.domain.Escrow;
import com.py.py.domain.Posting;
import com.py.py.domain.Tag;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.enumeration.AGGREGATION_TYPE;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.EscrowSourceTarget;
import com.py.py.domain.subdomain.TagId;
import com.py.py.dto.in.PromoteCommentDTO;
import com.py.py.dto.in.SubmitCommentDTO;
import com.py.py.dto.in.SubmitEditCommentDTO;
import com.py.py.dto.out.CommentDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.service.CommentService;
import com.py.py.service.EscrowService;
import com.py.py.service.EventService;
import com.py.py.service.FinanceService;
import com.py.py.service.FlagService;
import com.py.py.service.FollowService;
import com.py.py.service.PostingService;
import com.py.py.service.TagService;
import com.py.py.service.UserService;
import com.py.py.service.base.BaseAggregator;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BackerNotFoundException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.BalanceException;
import com.py.py.service.exception.BlockedException;
import com.py.py.service.exception.FinanceException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.DefaultsFactory;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;

public class CommentServiceImpl extends BaseAggregator implements CommentService {

	protected static final PyLogger logger = PyLogger.getLogger(CommentServiceImpl.class);
	
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
	private FlagService flagService;
	
	// used instead of null field for index purposes
	private static final List<String> allCommentTypes = 
			PyUtils.stringifiedList(Arrays.asList(COMMENT_TYPE.values()));
	
	protected Page<CommentDTO> getPreviewDTOs(String language, ObjectId authorId, 
			Pageable pageable, Filter filter, List<COMMENT_TYPE> commentTypes, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(pageable, filter);
		String correctLanguage = ServiceUtils.getLanguageOrNull(language);

		List<CommentDTO> dtolist = ModelFactory.<CommentDTO>constructList();
		Page<Comment> page = new PageImpl<Comment>(new ArrayList<Comment>(), pageable, 0);
		List<String> types = defaultsFactory.getCommentsPreviewsTypesStringList();

		if(commentTypes != null && !commentTypes.isEmpty()) {
			types = new ArrayList<String>();
			for(COMMENT_TYPE t : commentTypes) {
				if(!types.contains(t.toString())) {
					types.add(t.toString());
				}
			}
		} else {
			types = allCommentTypes;
		}
		
		String replacement = null;
		/*
		if(filter == null || (filter.getWarning() != null && !filter.getWarning())) {
			replacement = defaultsFactory.getWarningContentReplacement();
		}*/
		
		try {
			page = commentDao.getSortedComments(types, correctLanguage, authorId, pageable, filter);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(page == null) {
			throw new ServiceException();
		}
		
		for(Comment c : page.getContent()) {
			try {
				dtolist.add(Mapper.mapCommentDTO(c, replacement, canAppreciate(c), canComment(c), preview));
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of comment preview!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping of comment preview!", e);
			}
		}
		
		return new PageImpl<CommentDTO>(dtolist,pageable,page.getTotalElements());
	}
	
	protected Page<CommentDTO> getPreviewByUserDTOs(ObjectId authorId, 
			ObjectId beneficiaryId, Pageable pageable, boolean showDisabled, 
			Boolean warning, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		if(authorId == null && beneficiaryId == null) {
			throw new BadParameterException();
		}
		
		List<CommentDTO> dtolist = ModelFactory.<CommentDTO>constructList();
		Page<Comment> page = new PageImpl<Comment>(new ArrayList<Comment>(), pageable, 0);

		String replacement = null;
		/*
		if(warning != null && !warning) {
			replacement = defaultsFactory.getWarningContentReplacement();
		}*/
		
		try {
			page = commentDao.getUserComments(authorId, beneficiaryId, pageable, 
					showDisabled, warning);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(page == null) {
			throw new ServiceException();
		}
		
		for(Comment c : page.getContent()) {
			try {
				dtolist.add(Mapper.mapCommentDTO(c, replacement, canAppreciate(c), canComment(c), preview));
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of user comment preview!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping of user comment preview!", e);
			}
		}
		
		return new PageImpl<CommentDTO>(dtolist,pageable,page.getTotalElements());
	}
	
	@Override
	public Page<CommentDTO> getCommentPreviewDTOs(String language, ObjectId authorId, 
			Pageable pageable, Filter filter, List<COMMENT_TYPE> commentTypes, boolean preview) 
					throws ServiceException {
		ArgCheck.nullCheck(language, pageable, filter);
		return getPreviewDTOs(language, authorId, pageable, filter, commentTypes, preview);
	}
	
	@Override
	public Page<CommentDTO> getAuthorPreviewDTOs(ObjectId authorId, 
			Pageable pageable, Boolean warning, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(authorId, pageable);
		return getPreviewByUserDTOs(authorId, null, pageable, false, warning, preview);
	}
	
	@Override
	public Page<CommentDTO> getBeneficiaryPreviewDTOs(ObjectId beneficiaryId, 
			Pageable pageable, Boolean warning, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(beneficiaryId, pageable);
		return getPreviewByUserDTOs(null, beneficiaryId, pageable, false, warning, preview);
	}
	
	@Override
	public Page<CommentDTO> getSelfPreviewDTOs(ObjectId userId, Pageable pageable, boolean preview) 
			throws ServiceException {
		ArgCheck.nullCheck(userId, pageable);
		return getPreviewByUserDTOs(userId, null, pageable, true, null, preview);
	}
	
	@Override
	public Page<CommentDTO> getCommentDTOs(ObjectId baseId, String baseString, 
			COMMENT_TYPE type, String language, Pageable pageable, Filter filter, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(language, type, pageable, filter);
		if(baseId == null && baseString == null) {
			throw new BadParameterException();
		}
		return getCommentDTOs(baseId, baseString, type, null, language, pageable, filter, preview);
	}
	
	@Override
	public Page<CommentDTO> getSubCommentDTOs(ObjectId parentId, String language, 
			Pageable pageable, Filter filter, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(parentId, language, pageable, filter);
		return getCommentDTOs(null, null, null, parentId, language, pageable, filter, preview);
	}
	
	@Override
	public Page<CommentDTO> getCommentDTOs(ObjectId baseId, String baseString, 
			COMMENT_TYPE type, ObjectId parentId, String language, Pageable pageable, 
			Filter filter, boolean preview) throws ServiceException {
		ArgCheck.nullCheck(language, pageable, filter);
		String correctLanguage = ServiceUtils.getLanguageOrNull(language);
		
		List<String> typesList = new ArrayList<String>();
		if(type == null) {
			typesList = allCommentTypes;
		} else {
			typesList.add(type.toString());
		}

		String replacement = null;
		/*
		if(filter == null || (filter.getWarning() != null && !filter.getWarning())) {
			replacement = defaultsFactory.getWarningContentReplacement();
		}*/
		
		Page<Comment> comments = null;
		try {
			comments = commentDao.getSortedReplyComments(baseId, baseString, 
					typesList, parentId, parentId == null, correctLanguage, 
					pageable, filter);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(comments == null) {
			throw new ServiceException();
		}
		
		List<CommentDTO> commentdtos = ModelFactory.<CommentDTO>constructList();
		
		for(Comment c : comments.getContent()) {
			try {
				CommentDTO dto = Mapper.mapCommentDTO(c, replacement, canAppreciate(c), 
						canComment(c), preview);
				commentdtos.add(dto);
			} catch(BadParameterException bpe) {
				logger.info("Invalid mapping of comment!", bpe);
			} catch(Exception e) {
				logger.info("Invalid mapping of comment!", e);
			}
		}
		
		return new PageImpl<CommentDTO>(commentdtos, pageable, comments.getTotalElements());
	}
	
	@Override
	public ResultSuccessDTO createCommentDTO(User user, Comment parent, Posting basePosting, 
			UserInfo baseUserInfo, Tag baseTag, COMMENT_TYPE type, SubmitCommentDTO dto, 
			String language) throws ServiceException {
		ObjectId result = createComment(user, parent, basePosting, baseUserInfo, baseTag, 
				type, dto, language);
		return Mapper.mapResultSuccessDTO(result.toHexString());
	}
	
	protected ObjectId createComment(User user, Comment parent, Posting basePosting, 
			UserInfo baseUserInfo, Tag baseTag, COMMENT_TYPE type, SubmitCommentDTO dto, 
			String language) throws ServiceException {
		ArgCheck.nullCheck(dto, type);
		ArgCheck.userCheck(user);

		String title = null;
		CachedUsername targetName = null;
		String correctLanguage = ServiceUtils.getLanguage(language);
		ObjectId baseId = null;
		String baseString = null;
		if(COMMENT_TYPE.POSTING.equals(type)) {
			ArgCheck.nullCheck(basePosting);
			baseId = basePosting.getId();
			baseString = basePosting.getTitle();
			title = basePosting.getTitle();
			targetName = basePosting.getAuthor();
			if(!postingService.canComment(basePosting)) {
				throw new ActionNotAllowedException();
			}
		} else if(COMMENT_TYPE.USER.equals(type)) {
			ArgCheck.nullCheck(baseUserInfo);
			baseId = baseUserInfo.getId();
			baseString = baseUserInfo.getUsername();
			if(!userService.canComment(baseUserInfo)) {
				throw new ActionNotAllowedException();
			}
			if(followService.isBlocked(baseUserInfo.getId(), user.getId())) {
				throw new BlockedException();
			}
			title = baseUserInfo.getUsername();
			targetName = new CachedUsername(baseUserInfo.getId(), 
					baseUserInfo.getUsername());
		} else if(COMMENT_TYPE.TAG.equals(type)) {
			ArgCheck.tagCheck(baseTag);
			if(!tagService.canComment(baseTag)) {
				throw new ActionNotAllowedException();
			}
			title = baseTag.getId().getName();
			baseId = null;
			baseString = baseTag.getId().toString();
		}
		
		if(baseId == null && baseString == null) {
			throw new BadParameterException();
		}

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
		
		CachedUsername author = new CachedUsername(user.getId(), user.getUsername());
		Date created = new Date();
		
		ObjectId parentId = null;
		if(parent != null) {
			parentId = parent.getId();
			if(!canComment(parent)) {
				throw new ActionNotAllowedException();
			}
		}
		
		Comment comment = Mapper.mapComment(dto, author, cachedBacker, baseId, baseString, 
				parentId, created, type, correctLanguage);
		
		ObjectId cid = null;
		
		try {
			// no need to evict comment from cache, it is done when charged
			comment = commentDao.save(comment);
			if(comment == null) {
				throw new ServiceException();
			}
			cid = comment.getId();
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
					financeService.charge(user.getId(), cid, 
						true, CollectionNames.COMMENT, cost);
				} else {
					financeService.charge(escrowId, cid, 
						true, CollectionNames.COMMENT, cost);
				}
				
				try {
					updateAggregates(cid.toHexString(), AGGREGATION_TYPE.COMMENT, cost);
				} catch(ServiceException se) {
					// do nothing, user has been charged
					logger.warn("Could not update aggregates for comment id {" + cid.toHexString() + "}.", se);
				}
			} else {
				try {
					commentDao.setEnabled(cid, null, null, null, null, null, true);
				} catch(DaoException de) {
					// do nothing, user has been charged
					logger.warn("Could not update initialized status for comment id {" + cid.toHexString() + "}.", de);
				}
			}
			
			// if cost is 0, it only increments the comment count
			try {
				userService.incrementContributedCost(author.getId(), cost, false);
			} catch(ServiceException se) {
				// do nothing
				logger.warn("Could not increment contributed cost for comment id {" + cid.toHexString() + "}.", se);
			}
			try {
				if(parent == null) {
					if(basePosting != null) {
						if(paid) {
							postingService.incrementCommentTallyApproximation(basePosting.getId(), 
								null, null, cost);
						}
						postingService.incrementCommentCount(basePosting.getId(), true);
					} else if(baseTag != null) {
						if(paid) {
							tagService.incrementCommentTallyApproximation(
								baseTag.getId(), null, null, cost);
						}
						tagService.incrementCommentCount(baseTag.getId(), true);
					} else if(baseUserInfo != null) {
						if(paid) {
							userService.incrementCommentTallyApproximation(baseUserInfo.getId(), 
								null, null, cost);
						}
						userService.incrementCommentCount(baseUserInfo.getId(), true);
					}
				}
			} catch(ServiceException se) {
				// do nothing
				logger.warn("Could not increment comment tally and count for comment id {" + cid.toHexString() + "}.", se);
			}
			try {
				if(parent == null) {
					eventService.eventComment(author, cachedBacker, cid, baseId, baseString, 
							type, title, targetName, cost);
				} else {
					CachedUsername parentAuthor = parent.getAuthor();
					
					incrementReplyCount(parent.getId(), true);
					if(paid) {
						incrementReplyTallyApproximation(parent.getId(), null, null, cost);
					}
					
					eventService.eventSubComment(author, cachedBacker, cid, baseId, 
							baseString, type, parentAuthor, parent.getId(), title, cost);
				}
			} catch(Exception e) {
				// do nothing, user has been charged
				logger.warn("Could not create event for comment id {" + cid.toHexString() + "} or increment reply count for parent id {" + parent.getId() + "}.", e);
			}
			
			logger.info("Comment with id {" + cid + "} was created!");
			
			return cid;
		} catch(BalanceException be) {
			createCommentError(cid);
			throw be;
		} catch(FinanceException fe) {
			createCommentError(cid);
			throw fe;
		} catch(BadParameterException bpe) {
			createCommentError(cid);
			throw bpe;
		} catch(NotFoundException nfe) {
			createCommentError(cid);
			throw nfe;
		} catch(Exception e) {
			createCommentError(cid);
			throw new ServiceException(e);
		}
	}
	
	protected void createCommentError(ObjectId cid) {
		if(cid != null) {
			try {
				commentDao.delete(cid);
			} catch(Exception e) {
				// do nothing!
				logger.warn("Could not delete created comment {" + cid + "}.", e);
			}
		}
	}
	
	@Override
	public void editComment(ObjectId userId, Comment comment, SubmitEditCommentDTO dto) throws ServiceException {
		ArgCheck.nullCheck(userId, comment, dto);
		
		if(!PyUtils.objectIdCompare(comment.getAuthor().getId(), userId)) {
			throw new ActionNotAllowedException();
		}
		
		String content = ServiceUtils.getContent(dto.getContent());
		
		try {
			commentDao.updateComment(comment.getId(),content);
		} catch(DaoException de) {
			throw new ServiceException(de);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public CommentDTO getCommentDTO(ObjectId authorId, Comment comment, Boolean warning) 
			throws ServiceException {
		// do not check authorId
		ArgCheck.nullCheck(comment);
		String replacement = null;
		/*
		if(warning != null && !warning) {
			replacement = defaultsFactory.getWarningContentReplacement();
		}*/
		
		if(authorId != null && comment.getAuthor() != null 
				&& comment.getAuthor().getId() != null
				&& PyUtils.objectIdCompare(authorId, comment.getAuthor().getId())) {

			return Mapper.mapCommentDTOFull(comment, canAppreciate(comment), 
				canComment(comment));
		} else {
			return Mapper.mapCommentDTO(comment, replacement, canAppreciate(comment), 
				canComment(comment), false);
		}
	}
	
	@Override
	public Comment getCachedComment(ObjectId id) throws ServiceException {
		return getComment(id, true);
	}
	
	@Override
	public Comment getComment(ObjectId id) throws ServiceException {
		return getComment(id, false);
	}
	
	private Comment getComment(ObjectId id, boolean cached) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			Comment comment;
			if(cached) {
				comment = commentDao.findCachedComment(id);
			} else {
				comment = commentDao.findComment(id);
			}
			if(comment == null) {
				throw new NotFoundException(id.toHexString());
			}
			return comment;
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void appreciateComment(ObjectId paymentId, User user, Comment comment,
			long appreciationAmount, long promotionAmount, boolean warning)
					throws ServiceException {
		ArgCheck.nullCheck(paymentId, comment);
		ArgCheck.userCheck(user);
		promoteComment(user, comment, appreciationAmount, promotionAmount, 
				warning, paymentId);
	}
	
	@Override
	public void promoteComment(User user, Comment comment, PromoteCommentDTO dto)
			throws ServiceException {
		ArgCheck.nullCheck(comment, dto);
		ArgCheck.userCheck(user);
		
		if(!canAppreciate(comment)) {
			throw new ActionNotAllowedException();
		}
		
		promoteComment(user, comment, null, dto.getPromotion(), dto.isWarning(), null);
	}
	
	protected void promoteComment(User user, Comment comment, Long appreciationAmount, 
			long promotionAmount, boolean warning, ObjectId paymentId)
					throws ServiceException {
		ArgCheck.nullCheck(comment);
		ArgCheck.userCheck(user);
		if(promotionAmount <= 0L) {
			throw new BadParameterException();
		}
		
		boolean isAppreciation = false;
		if(appreciationAmount != null) {
			isAppreciation = true;
		}
		
		ObjectId commentId = comment.getId();
		
		COMMENT_TYPE type = comment.getType();
		ObjectId baseId = comment.getBaseId();
		String baseString = comment.getBaseString();
		
		Posting basePosting = null;
		Tag baseTag = null;
		User baseUser = null;
		String title = null;
		
		if(type == COMMENT_TYPE.POSTING) {
			basePosting = postingService.getPosting(baseId);
			if(basePosting == null) {
				throw new NotFoundException(baseId.toHexString());
			}
			baseId = basePosting.getId();
			title = basePosting.getTitle();
		} else if(type == COMMENT_TYPE.TAG) {
			if(baseString != null) {
				TagId tId = new TagId();
				tId.fromString(baseString);
				baseTag = tagService.getTag(tId);
			} else {
				throw new BadParameterException();
			}
			if(!tagService.canComment(baseTag)) {
				throw new ActionNotAllowedException();
			}
			ArgCheck.tagCheck(baseTag);
			baseString = baseTag.getId().toString();
			title = baseTag.getId().getName();
		} else if(type == COMMENT_TYPE.USER){
			// retrieve profile here instead
			if(baseId != null) {
				baseUser = userService.findUser(baseId);
			} else {
				throw new BadParameterException();
			}
			baseId = baseUser.getId();
			baseString = baseUser.getUsername();
			title = baseUser.getUsername();
		}
		
		CachedUsername cachedTarget = comment.getAuthor();
		ObjectId target = null;
		if(cachedTarget != null) {
			target = cachedTarget.getId();
		}
		
		CachedUsername beneficiary = null;
		if(comment.getBeneficiary() != null && comment.getBeneficiary().getId() != null) {
			beneficiary = comment.getBeneficiary();
		}
		
		ObjectId source = user.getId();
		
		if(isAppreciation) {
			financeService.appreciate(source, commentId, CollectionNames.COMMENT, 
					appreciationAmount, promotionAmount, paymentId);
		} else {
			financeService.promote(source, commentId, CollectionNames.COMMENT, 
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
			logger.warn("Could not increment contributed promotion for source {" + source + "} on comment {" + commentId + "} !", se);
		}
		try {
			userService.incrementAppreciationPromotion(target, 
					appreciationAmount, promotionAmount);
		} catch(ServiceException se) {
			// do nothing
			logger.warn("Could not increment promotion for target {" + target + "} on comment {" + commentId + "} !", se);
		}
		try {
			incrementAppreciationPromotionCount(commentId, isAppreciation, true);
		} catch(ServiceException se) {
			// do nothing
			logger.warn("Could not increment promotion count for comment {" + commentId + "} !", se);
		}
		try {
			updateAggregates(commentId.toHexString(), AGGREGATION_TYPE.COMMENT, 
					promotionAmount);
		} catch(ServiceException se) {
			// do nothing
			logger.warn("Could not update aggregates on comment {" + commentId + "} !", se);
		}
		try {
			if(comment.getParent() != null) {
				incrementReplyTallyApproximation(comment.getParent(), 
						appreciationAmount, promotionAmount, null);
			} else {
				if(basePosting != null) {
					postingService.incrementCommentTallyApproximation(basePosting.getId(), 
							appreciationAmount, promotionAmount, null);
				} else if(baseTag != null) {
					tagService.incrementCommentTallyApproximation(baseTag.getId(), 
							appreciationAmount, promotionAmount, null);
				} else if(baseUser != null) {
					userService.incrementCommentTallyApproximation(baseUser.getId(), 
							appreciationAmount, promotionAmount, null);
				}
			}
			
			if(warning) {
				warn(comment, promotionAmount);
			}
		} catch(ServiceException se) {
			// do nothing
			logger.warn("Could not increment comment tally on comment {" + commentId + "} !", se);
		}
		try {
			
			CachedUsername cachedSource = new CachedUsername(user.getId(), 
					user.getUsername());
			
			if(isAppreciation) {
				eventService.eventAppreciationComment(cachedSource, beneficiary, commentId, 
						baseId, baseString, type, cachedTarget, title, appreciationAmount);
			} else {
				eventService.eventPromotionComment(cachedSource, beneficiary, commentId, 
						baseId, baseString, type, cachedTarget, title, promotionAmount);
			}
		} catch(ServiceException se) {
			// do nothing, as user has already been charged
			logger.warn("Could not create appreciation or promotion event for comment {" + commentId + "} !", se);
		} catch(Exception e) {
			// do nothing, as user has already been charged
			logger.warn("Could not create appreciation or promotion event for comment {" + commentId + "} !", e);
		}
	}
	
	@Override
	public void incrementReplyCount(ObjectId id, boolean increment) throws ServiceException {
		ArgCheck.nullCheck(id);
		
		try {
			if(increment) {
				commentDao.incrementReplyCount(id, 1);
			} else {
				commentDao.incrementReplyCount(id, -1);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementAppreciationPromotionCount(ObjectId id, boolean appreciation, 
			boolean increment) throws ServiceException {
		ArgCheck.nullCheck(id);
		
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
			commentDao.incrementAppreciationPromotionCount(id, appreciationIncrement, 
					promotionIncrement);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void incrementReplyTallyApproximation(ObjectId id, 
			Long appreciationIncrement, Long promotionIncrement, Long cost)
					throws ServiceException {
		// arguments may be null, do not update nulls
		ArgCheck.nullCheck(id);
		
		try {
			if(cost != null) {
				commentDao.incrementReplyTallyCost(id, cost);
			}
			if(appreciationIncrement != null || promotionIncrement != null) {
				commentDao.incrementReplyTallyAppreciationPromotion(id, 
						appreciationIncrement, promotionIncrement);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void updateAggregate(ObjectId id, long value, TIME_OPTION segment) 
			throws ServiceException {
		ArgCheck.nullCheck(id, segment);
		
		try {
			commentDao.updateAggregation(id, value, segment);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void aggregateComments(TIME_OPTION segment) throws ServiceException {
		ArgCheck.nullCheck(segment);
		
		Iterable<DBObject> results = aggregate(AGGREGATION_TYPE.COMMENT, segment);

		for(DBObject obj : results) {
			@SuppressWarnings("rawtypes")
			Map map = obj.toMap();
			ObjectId id = new ObjectId((String) map.get("_id"));
			long total = (Long) map.get("total");
			updateAggregate(id, total, segment);
		}
		
		try {
			commentDao.emptyAggregations(segment);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void aggregateTotals() throws ServiceException {
		updateAggregateTotals(AGGREGATION_TYPE.COMMENT);
	}
	
	@Override
	public void enableComment(User user, Comment comment) throws ServiceException {
		ArgCheck.nullCheck(comment);
		ArgCheck.userCheck(user);
		
		CachedUsername author = comment.getAuthor();
		
		if(author == null || !PyUtils.objectIdCompare(user.getId(), author.getId())) {
			throw new ActionNotAllowedException();
		}

		if(comment.isEnabled()) {
			return;
		}
		
		try {
			commentDao.setEnabled(comment.getId(), true, null, null, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void disableComment(User user, Comment comment) throws ServiceException {
		ArgCheck.nullCheck(comment);
		ArgCheck.userCheck(user);
		
		CachedUsername author = comment.getAuthor();
		
		if(author == null || !PyUtils.objectIdCompare(user.getId(), author.getId())) {
			throw new ActionNotAllowedException();
		}
		
		if(!comment.isEnabled()) {
			return;
		}
		
		try {
			commentDao.setEnabled(comment.getId(), false, null, null, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void unremoveComment(Comment comment) throws ServiceException {
		ArgCheck.nullCheck(comment);
		
		if(!comment.isRemoved()) {
			return;
		}
		
		try {
			commentDao.setEnabled(comment.getId(), null, false, null, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void removeComment(Comment comment, boolean sendEvent) throws ServiceException {
		ArgCheck.nullCheck(comment);
		
		if(comment.isRemoved()) {
			return;
		}
		
		try {
			commentDao.setEnabled(comment.getId(), null, true, null, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		if(sendEvent) {
			eventService.eventCommentInfringement(comment.getAuthor(), comment.getBeneficiary(), comment.getId(), comment.getBaseId(), comment.getBaseString(), comment.getType());
		}
	}

	protected void warn(Comment comment, long amount) throws ServiceException {
		ArgCheck.nullCheck(comment);
		if(comment.isWarning()) {
			// no need
			return;
		}
		long appreciation = 0L;
		if(comment.getTally() != null) {
			appreciation = comment.getTally().getAppreciation();
		}
		ObjectId commentId = comment.getId();
		
		Boolean warn = null;
		if(PyUtils.overWarnThreshold(appreciation, comment.getWarningValue() + amount)) {
			warn = true;
		}

		try {
			commentDao.addWarn(commentId, warn, amount);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void flag(User user, UserInfo userInfo, Comment comment, FLAG_REASON reason) throws ServiceException {
		ArgCheck.nullCheck(userInfo, comment, reason);
		ArgCheck.userCheck(user);
		
		long weight = userService.getWeight(user, userInfo);
		if(comment.isFlagged()) {
			// no need
			return;
		}
		if(comment.getVotes() != null 
				&& comment.getVotes().contains(user.getId())) {
			checkFlag(user, comment, 0L);
			return;
		} else {
			checkFlag(user, comment, weight);
			try {
				flagService.addData(comment.getId(), FLAG_TYPE.COMMENT, comment.getBaseString(), weight, reason);
			} catch(ServiceException se) {
				//continue
			}
			try {
				if(comment.getAuthor() != null && comment.getAuthor().getId() != null) {
					User targetUser = userService.findUser(comment.getAuthor().getId());
					UserInfo targetUserInfo = userService.findUserInfo(targetUser);
					userService.flag(user, userInfo, targetUser, targetUserInfo, null);
				}
			} catch(NotFoundException nfe) {
				// do nothing, just cant flag the user
			}
		}
		
		try {
			commentDao.addVote(comment.getId(), user.getId(), weight);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected void checkFlag(User user, Comment comment, long addedWeight)
			throws ServiceException {
		Date now = new Date();
		Date inactiveSince = new Date(now.getTime() - ServiceValues.POSTING_INACTIVE);
		Date created = new Date();
		if(comment.getCreated() != null) {
			created = comment.getCreated();
		}
		if(inactiveSince.after(created)) {
			// it went inactive since being created
			return;
		}
		
		long flagValue = comment.getFlagValue() + addedWeight;
		int flagCount = 0;
		if(comment.getVotes() != null) {
			flagCount = comment.getVotes().size();
		}
		long value = 0;
		if(comment.getTally() != null) {
			value = comment.getTally().getValue();
		}
		if(PyUtils.overFlagThreshold(value, comment.getAppreciationCount(), 
				flagValue, flagCount)) {
			try {
				commentDao.setEnabled(comment.getId(), null, null, true, null, null, null);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
		}
	}
	
	@Override
	public void markArchived() throws ServiceException {
		Date then = PyUtils.getOldDate(ArchivalTimes.COMMENT_START);
		try {
			commentDao.markArchived(then);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}

	@Override
	public boolean canAppreciate(Comment comment) throws ServiceException {
		ArgCheck.nullCheck(comment);
		
		if(comment.isEnabled() && !comment.isLocked() && comment.isInitialized()
				&& !comment.isFlagged() && !comment.isRemoved() && comment.getArchived() == null) {
			if(!ServiceUtils.isUsernameDeleted(comment.getAuthor())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	public boolean canComment(Comment comment) throws ServiceException {
		ArgCheck.nullCheck(comment);
		
		if(comment.isEnabled() && !comment.isLocked() && comment.isInitialized() 
				&& !comment.isFlagged() && !comment.isRemoved() && comment.getArchived() == null) {
			return true;
		}
		return false;
	}
	
	@Override
	public Map<TIME_OPTION, BigInteger> getAggregateTotals() throws ServiceException {
		return getAggregateTotals(AGGREGATION_TYPE.COMMENT);
	}
}
