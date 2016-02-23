package com.py.py.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.ServiceValues;
import com.py.py.dao.AdminDao;
import com.py.py.dao.CommentDao;
import com.py.py.dao.CorrespondenceDao;
import com.py.py.dao.EscrowDao;
import com.py.py.dao.FeedbackDao;
import com.py.py.dao.FlagDataDao;
import com.py.py.dao.MessageDao;
import com.py.py.dao.PostingDao;
import com.py.py.dao.ReferenceDao;
import com.py.py.dao.RestrictedDao;
import com.py.py.dao.SubscriptionDao;
import com.py.py.dao.TagDao;
import com.py.py.dao.UserDao;
import com.py.py.dao.UserInfoDao;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.AdminAction;
import com.py.py.domain.Comment;
import com.py.py.domain.Posting;
import com.py.py.domain.Restricted;
import com.py.py.domain.User;
import com.py.py.domain.constants.CollectionNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.FollowInfo;
import com.py.py.dto.DTO;
import com.py.py.dto.in.admin.ChangeBalanceDTO;
import com.py.py.dto.in.admin.ChangeEmailAdminDTO;
import com.py.py.dto.in.admin.ChangePendingActionsDTO;
import com.py.py.dto.in.admin.ChangeRestrictedDTO;
import com.py.py.dto.in.admin.ChangeRolesDTO;
import com.py.py.dto.in.admin.ChangeTallyDTO;
import com.py.py.dto.in.admin.ChangeUsernameDTO;
import com.py.py.dto.in.admin.LockUserDTO;
import com.py.py.dto.in.admin.SetPasswordDTO;
import com.py.py.dto.out.CommentDTO;
import com.py.py.dto.out.PostingDTO;
import com.py.py.dto.out.RoleSetDTO;
import com.py.py.dto.out.admin.AdminActionDTO;
import com.py.py.dto.out.admin.RestrictedDTO;
import com.py.py.enumeration.ADMIN_STATE;
import com.py.py.enumeration.ADMIN_TYPE;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.enumeration.LOCK_REASON;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.service.AdminService;
import com.py.py.service.AuthenticationService;
import com.py.py.service.CommentService;
import com.py.py.service.EventService;
import com.py.py.service.FinanceService;
import com.py.py.service.FlagService;
import com.py.py.service.FollowService;
import com.py.py.service.PostingService;
import com.py.py.service.RestrictedService;
import com.py.py.service.UserService;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.RestrictedException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.DefaultsFactory;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;

public class AdminServiceImpl implements AdminService {

	protected static final PyLogger logger = PyLogger.getLogger(AdminServiceImpl.class);

	@Autowired
	protected AuthenticationService authService;
	
	@Autowired
	protected FinanceService financeService;
	
	@Autowired
	protected UserDao userDao;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected PostingService postingService;
	
	@Autowired
	protected PostingDao postingDao;
	
	@Autowired
	protected CommentService commentService;
	
	@Autowired
	protected CommentDao commentDao;
	
	@Autowired
	protected TagDao tagDao;
	
	@Autowired
	protected ReferenceDao referenceDao;
	
	@Autowired
	protected RestrictedService restrictedService;
	
	@Autowired
	protected RestrictedDao restrictedDao;
	
	@Autowired
	protected AdminDao adminDao;
	
	@Autowired
	protected FollowService followService;
	
	@Autowired
	protected SubscriptionDao subscriptionDao;
	
	@Autowired
	protected FeedbackDao feedbackDao;
	
	@Autowired
	protected UserInfoDao userInfoDao;
	
	@Autowired
	protected EscrowDao escrowDao;
	
	@Autowired
	protected EventService eventService;
	
	@Autowired
	protected CorrespondenceDao correspondenceDao;
	
	@Autowired
	protected MessageDao messageDao;
	
	@Autowired
	protected FlagService flagService;
	
	@Autowired
	protected FlagDataDao flagDataDao;
	
	@Autowired
	protected DefaultsFactory defaultsFactory;
	
	protected void completeAdminAction(User admin, ADMIN_TYPE type, String target, DTO dto) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		try {
			adminDao.createAction(new CachedUsername(admin.getId(), admin.getUsername()), 
					ADMIN_STATE.COMPLETE, type, target, dto, null);
		} catch(DaoException de) {
			logger.warn("Admin action was not completed!", de);
		}
	}
	
	@Override
	public void unlockUser(User admin, ObjectId targetId) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(targetId);
		
		try {
			userDao.updateStatus(targetId, null, null, LOCK_REASON.UNLOCKED, true);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		completeAdminAction(admin, ADMIN_TYPE.USER_UNLOCK, targetId.toHexString(), 
				null);
		logger.info("Unlocked user id {" + targetId.toHexString() + "}.");
	}
	
	@Override
	public void lockUser(User admin, ObjectId targetId, LockUserDTO dto) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(targetId, dto);
		
		lockUser(targetId, dto.getLockedUntil(), dto.getSuspensions(), dto.getLockReason());
		
		completeAdminAction(admin, ADMIN_TYPE.USER_LOCK, targetId.toHexString(), dto);
	}
	
	protected void lockUser(ObjectId targetId, Date lockedUntil, Long suspensions, 
			LOCK_REASON lockReason) throws ServiceException {
		ArgCheck.nullCheck(targetId);
		
		try {
			userDao.updateStatus(targetId, lockedUntil, suspensions, lockReason, false);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		logger.info("Locked user id {" + targetId.toHexString() + "}.");
	}
	
	@Override
	public void lockTag(User admin, String name, String language) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(name, language);
		String correctName = ServiceUtils.getTag(name);
		String correctLanguage = ServiceUtils.getLanguage(language);
		
		try {
			tagDao.updateTag(correctName, correctLanguage, true);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		completeAdminAction(admin, ADMIN_TYPE.TAG_LOCK, correctName + ":" + language, null);
		logger.info("Locked tag {" + name + "," + language + "}.");
	}
	
	@Override
	public void unlockTag(User admin, String name, String language) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(name, language);
		String correctName = ServiceUtils.getTag(name);
		String correctLanguage = ServiceUtils.getLanguage(language);
		
		try {
			tagDao.updateTag(correctName, correctLanguage, false);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		completeAdminAction(admin, ADMIN_TYPE.TAG_UNLOCK, correctName + ":" + language, null);
		logger.info("Unlocked tag {" + name + "," + language + "}.");
	}
	
	@Override
	public void changePassword(User admin, User target, SetPasswordDTO dto) throws ServiceException {
		ArgCheck.userCheck(admin, target);
		ArgCheck.nullCheck(dto);
		
		if(restrictedService.isRestricted(dto.getPassword(), RESTRICTED_TYPE.PASSWORD)) {
			throw new RestrictedException(RESTRICTED_TYPE.PASSWORD);
		}
		
		authService.changeUserPassword(target, dto.getPassword(), dto.getPassword());

		
		completeAdminAction(admin, ADMIN_TYPE.PASSWORD_SET, target.getId().toHexString(), dto);
		logger.info("Changing password of target {" + target.getId().toHexString() 
				+ "}, password not shown.");
	}
	
	@Override
	public void setPendingActions(User admin, ObjectId targetId, ChangePendingActionsDTO dto)
		throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(targetId, dto);
		
		try {
			userInfoDao.updatePendingActions(targetId, dto.getPendingActions());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		completeAdminAction(admin, ADMIN_TYPE.PENDING_ACTIONS_SET, targetId.toHexString(), dto);
		logger.info("Changing pending actions of target {" + targetId.toHexString() 
				+ "}, actions: [" + dto.getPendingActions() + "].");
	}
	
	@Override
	public void setRoles(User admin, ObjectId targetId, ChangeRolesDTO dto)
		throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(targetId, dto);
		
		try {
			userDao.updateRoles(targetId, dto.getRoles(), dto.getOverrideRoles());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		completeAdminAction(admin, ADMIN_TYPE.ROLES_SET, targetId.toHexString(), dto);
		logger.info("Changing roles of target {" + targetId.toHexString() 
				+ "}, roles: [" + dto.getRoles() + "], overrideRoles: [" 
				+ dto.getOverrideRoles() + "].");
	}
	
	@Override
	public void setPostingRemove(User admin, Posting posting, boolean remove) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(posting);
		
		ADMIN_TYPE type = ADMIN_TYPE.POSTING_REMOVE;
		if(remove) {
			postingService.removePosting(posting, true);
		} else {
			postingService.unremovePosting(posting);
			type = ADMIN_TYPE.POSTING_UNREMOVE;
		}
		
		completeAdminAction(admin, type, posting.getId().toHexString(), null);
		logger.info("Changed remove status of posting id {" 
				+ posting.getId().toHexString() + "} to be " + remove + ".");
	}
	
	@Override
	public void setCommentRemove(User admin, Comment comment, boolean remove) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(comment);
		
		ADMIN_TYPE type = ADMIN_TYPE.COMMENT_REMOVE;
		if(remove) {
			commentService.removeComment(comment, true);
		} else {
			commentService.unremoveComment(comment);
			type = ADMIN_TYPE.COMMENT_UNREMOVE;
		}
		
		completeAdminAction(admin, type, comment.getId().toHexString(), null);
		logger.info("Changed remove status of comment id {" 
				+ comment.getId().toHexString() + "} to be " + remove + ".");
	}
	
	@Override
	public void setPostingWarning(User admin, ObjectId postingId, boolean warning) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(postingId);
		
		try {
			postingDao.setEnabled(postingId, null, null, null, warning, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		ADMIN_TYPE type = ADMIN_TYPE.POSTING_WARNING;
		if(!warning) {
			type = ADMIN_TYPE.POSTING_UNWARNING;
		}
		
		completeAdminAction(admin, type, postingId.toHexString(), null);
		logger.info("Changed warning status of posting id {" 
				+ postingId.toHexString() + "} to be " + warning + ".");
	}
	
	@Override
	public void setCommentWarning(User admin, ObjectId commentId, boolean warning) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(commentId);
		
		try {
			commentDao.setEnabled(commentId, null, null, null, warning, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		ADMIN_TYPE type = ADMIN_TYPE.COMMENT_WARNING;
		if(!warning) {
			type = ADMIN_TYPE.COMMENT_UNWARNING;
		}
		
		completeAdminAction(admin, type, commentId.toHexString(), null);
		logger.info("Changed warning status of comment id {" 
				+ commentId.toHexString() + "} to be " + warning + ".");
	}
	
	@Override
	public void setPostingFlag(User admin, ObjectId postingId, boolean flag) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(postingId);

		try {
			postingDao.setEnabled(postingId, null, null, flag, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		ADMIN_TYPE type = ADMIN_TYPE.POSTING_FLAG;
		if(!flag) {
			type = ADMIN_TYPE.POSTING_UNFLAG;
		}
		
		completeAdminAction(admin, type, postingId.toHexString(), null);
		logger.info("Changed flag status of posting id {" 
				+ postingId.toHexString() + "} to be " + flag + ".");
	}
	
	@Override
	public void setCommentFlag(User admin, ObjectId commentId, boolean flag) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(commentId);
		
		try {
			commentDao.setEnabled(commentId, null, null, flag, null, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		ADMIN_TYPE type = ADMIN_TYPE.COMMENT_FLAG;
		if(!flag) {
			type = ADMIN_TYPE.COMMENT_UNFLAG;
		}
		
		completeAdminAction(admin, type, commentId.toHexString(), null);
		logger.info("Changed flag status of comment id {" 
				+ commentId.toHexString() + "} to be " + flag + ".");
	}
	
	@Override
	public void setPostingPaid(User admin, ObjectId postingId, boolean paid) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(postingId);

		try {
			postingDao.setEnabled(postingId, null, null, null, null, paid, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		ADMIN_TYPE type = ADMIN_TYPE.POSTING_PAID;
		if(!paid) {
			type = ADMIN_TYPE.POSTING_UNPAID;
		}
		
		completeAdminAction(admin, type, postingId.toHexString(), null);
		logger.info("Changed paid status of posting id {" 
				+ postingId.toHexString() + "} to be " + paid + ".");
	}
	
	@Override
	public void setCommentPaid(User admin, ObjectId commentId, boolean paid) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(commentId);
		
		try {
			commentDao.setEnabled(commentId, null, null, null, null, paid, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		ADMIN_TYPE type = ADMIN_TYPE.COMMENT_PAID;
		if(!paid) {
			type = ADMIN_TYPE.COMMENT_UNPAID;
		}
		
		completeAdminAction(admin, type, commentId.toHexString(), null);
		logger.info("Changed paid status of comment id {" 
				+ commentId.toHexString() + "} to be " + paid + ".");
	}
	
	@Override
	public void addBalance(User admin, ObjectId targetId, ChangeBalanceDTO dto) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(targetId, dto);
		
		financeService.addCurrency(targetId, dto.getAmount());
		
		completeAdminAction(admin, ADMIN_TYPE.BALANCE_ADD, targetId.toHexString(), dto);
		logger.info("Added balance (" + dto.getAmount() + ") of user id {" 
				+ targetId.toHexString() + "}.");
	}
	
	@Override
	public void removeBalance(User admin, ObjectId targetId, ChangeBalanceDTO dto) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(targetId, dto);
		
		financeService.removeCurrency(targetId, dto.getAmount());
		
		completeAdminAction(admin, ADMIN_TYPE.BALANCE_REMOVE, targetId.toHexString(), 
				dto);
		logger.info("Removed balance (" + dto.getAmount() + ") of user id {" 
				+ targetId.toHexString() + "}.");
	}
	
	@Override
	public void clearLoginAttempts(User admin, ObjectId targetId) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(targetId);
		
		try {
			userDao.clearLoginAttempts(targetId, null, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		completeAdminAction(admin, ADMIN_TYPE.LOGINS_CLEAR, targetId.toHexString(), null);
		logger.info("Cleared login attempts of user id {" + targetId.toHexString() + "}.");
	}
	
	@Override
	public RoleSetDTO getUserRoleSetDTO(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		return Mapper.mapRoleSetDTO(user);
	}
	
	@Override
	public Posting getPosting(ObjectId postingId) throws ServiceException {
		ArgCheck.nullCheck(postingId);
		
		try {
			Posting posting = postingDao.findOne(postingId);
			if(posting == null) {
				throw new NotFoundException(postingId.toHexString());
			}
			return posting;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public PostingDTO getPostingDTO(Posting posting) throws ServiceException {
		ArgCheck.nullCheck(posting);
		return Mapper.mapPostingDTOFull(posting, postingService.canAppreciate(posting), 
				postingService.canComment(posting));
	}
	
	@Override
	public Comment getComment(ObjectId commentId) throws ServiceException {
		ArgCheck.nullCheck(commentId);
		
		try {
			Comment comment = commentDao.findOne(commentId);
			if(comment == null) {
				throw new NotFoundException(commentId.toHexString());
			}
			return comment;
		} catch(NotFoundException nfe) {
			throw nfe;
		} catch(Exception e) {
			throw new ServiceException(e);
		}
	}
	
	@Override
	public CommentDTO getCommentDTO(Comment comment) throws ServiceException {
		ArgCheck.nullCheck(comment);
		return Mapper.mapCommentDTOFull(comment, commentService.canAppreciate(comment), 
				commentService.canComment(comment));
	}
	
	@Override
	public void changePostingTally(User admin, ObjectId referenceId, ChangeTallyDTO dto) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(referenceId, dto);
		
		addReferenceTally(referenceId, dto.getCost(), PyUtils.convertFromAppreciation(dto.getAppreciation()), 
				dto.getPromotion(), CollectionNames.POSTING);
		
		completeAdminAction(admin, ADMIN_TYPE.POSTING_TALLY_CHANGE, 
				referenceId.toHexString(), dto);
		logger.info("Changed tally of posting with id {" + referenceId.toHexString() 
				+ "} by cost: (" + dto.getCost() + "), promotion: (" + dto.getPromotion() 
				+ "), and appreciation: (" + dto.getAppreciation() + ").");
	}
	
	@Override
	public void changeCommentTally(User admin, ObjectId referenceId, ChangeTallyDTO dto) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(referenceId, dto);
		
		addReferenceTally(referenceId, dto.getCost(), PyUtils.convertFromAppreciation(dto.getAppreciation()), 
				dto.getPromotion(), CollectionNames.COMMENT);
		
		completeAdminAction(admin, ADMIN_TYPE.COMMENT_TALLY_CHANGE, 
				referenceId.toHexString(), dto);
		logger.info("Changed tally of comment with id {" + referenceId.toHexString() 
				+ "} by cost: (" + dto.getCost() + "), promotion: (" + dto.getPromotion() 
				+ "), and appreciation: (" + dto.getAppreciation() + ").");
	}
	
	protected void addReferenceTally(ObjectId referenceId, long cost, long appreciation, 
			long promotion, String collectionName) throws ServiceException {
		ArgCheck.nullCheck(referenceId, collectionName);
		
		Long actualCost = null;
		if(cost != 0) {
			actualCost = cost;
		}
		Long actualAppreciation = null;
		if(appreciation != 0) {
			actualAppreciation = appreciation;
		}
		Long actualPromotion = null;
		if(promotion != 0) {
			actualPromotion = promotion;
		}
		
		if(actualCost == null && actualAppreciation == null && actualPromotion == null) {
			throw new BadParameterException();
		}
		
		try {
			referenceDao.adminIncrement(referenceId, actualCost, actualAppreciation, 
					actualPromotion, collectionName);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void changeEmail(User admin, ObjectId userId, ChangeEmailAdminDTO dto) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(userId, dto);
		
		String email = ServiceUtils.getEmail(dto.getEmail());
		
		try {
			userDao.updateUser(userId, email, null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		completeAdminAction(admin, ADMIN_TYPE.USER_EMAIL_CHANGE, 
				userId.toHexString(), dto);
		logger.info("Changed email of user with id {" + userId.toHexString() 
				+ "} to (" + dto.getEmail() + ").");
	}
	
	@Override
	public void addRestricted(User admin, ChangeRestrictedDTO dto) throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(dto);
		
		String word = ServiceUtils.getWord(dto.getWord());
		restrictedService.addRestricted(word, dto.getType());
		
		completeAdminAction(admin, ADMIN_TYPE.RESTRICTED_ADD, null, dto);
		logger.info("Added restricted word (" + dto.getWord() + ") with type: " 
				+ dto.getType() + ".");
	}
	
	@Override
	public void removeRestricted(User admin, String word, RESTRICTED_TYPE type) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(word, type);
		
		String correctWord = ServiceUtils.getWord(word);
		restrictedService.removeRestricted(word, type);
		
		ChangeRestrictedDTO dto = new ChangeRestrictedDTO();
		dto.setWord(correctWord);
		dto.setType(type);
		
		completeAdminAction(admin, ADMIN_TYPE.RESTRICTED_REMOVE, null, dto);
		logger.info("Removed restricted word (" + word + ") with type: " 
				+ type + ".");
	}
	
	@Override
	public void removeFlagData(User admin, ObjectId id, FLAG_TYPE type) 
			throws ServiceException {
		ArgCheck.userCheck(admin);
		ArgCheck.nullCheck(id, type);
		
		flagService.removeOne(id, type);
		
		completeAdminAction(admin, ADMIN_TYPE.FLAG_DATA_CLEAR, id.toHexString() + ":" + type.toString(), null);
		logger.info("Removed flag data id (" + id.toHexString() + ") with type: " 
				+ type.toString() + ".");
	}
	
	@Override
	public Page<RestrictedDTO> getRestrictedDTOs(RESTRICTED_TYPE type, Pageable pageable) 
			throws ServiceException {
		ArgCheck.nullCheck(type, pageable);
		
		Page<Restricted> restricteds = null;
		try {
			restricteds = restrictedDao.findRestricteds(type, pageable);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		
		if(restricteds == null) {
			throw new ServiceException();
		}
		
		List<RestrictedDTO> dtos = ModelFactory.<RestrictedDTO>constructList();
		
		for(Restricted r : restricteds.getContent()) {
			try {
				RestrictedDTO dto = Mapper.mapRestrictedDTO(r);
				dtos.add(dto);
			} catch(BadParameterException bpe) {
				// do not add to list
			} catch(Exception e) {
				// continue
			}
		}
		
		return new PageImpl<RestrictedDTO>(dtos, pageable, restricteds.getTotalElements());
	}
	
	@Override
	public Page<AdminActionDTO> getAdminDTOs(ObjectId adminId, ADMIN_STATE state, ADMIN_TYPE type, 
			String target, Pageable pageable, int direction) throws ServiceException {
		ArgCheck.nullCheck(pageable);
		
		Page<AdminAction> actions = null;
		List<ADMIN_STATE> states = null;
		if(state != null) {
			states = new ArrayList<ADMIN_STATE>();
			states.add(state);
		}
		
		try {
			actions = adminDao.findSortedActions(adminId, states, type, target, 
					null, pageable, direction);
		} catch(Exception e) {
			throw new ServiceException(e);
		}
		
		if(actions == null) {
			throw new ServiceException();
		}
		
		List<AdminActionDTO> dtos = ModelFactory.<AdminActionDTO>constructList();
		
		for(AdminAction a : actions.getContent()) {
			try {
				AdminActionDTO dto = Mapper.mapAdminActionDTO(a);
				dtos.add(dto);
			} catch(BadParameterException bpe) {
				// do not add to list
			} catch(Exception e) {
				// continue
			}
		}
		
		return new PageImpl<AdminActionDTO>(dtos, pageable, actions.getTotalElements());
	}
	
	@Override
	public void renameUser(User user, ChangeUsernameDTO dto) throws ServiceException {
		ArgCheck.userCheck(user);
		ArgCheck.nullCheck(dto);
		
		String username = ServiceUtils.getName(dto.getUsername());
		if(restrictedService.isRestricted(username, RESTRICTED_TYPE.USERNAME)) {
			throw new RestrictedException(RESTRICTED_TYPE.USERNAME);
		}
		
		try {
			userService.findUserByUsername(username);
			throw new ExistsException(username);
		} catch(NotFoundException nfe) {
			// expected
		}
		
		try {
			userDao.rename(user.getId(), username, null);
		} catch(CollisionException dk) {
			throw new ExistsException(username);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		logger.info("Set user with id {" + user.getId() + "} to be renamed (" 
				+ dto.getUsername() + ").");
	}
	
	/*
	@Override
	@CacheEvict(value = {CacheNames.USER, CacheNames.USER_ID_USERNAME, CacheNames.USER_ID_EMAIL}, allEntries = true)
	*/
	@Override
	public void renameUser(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		String newUsername = ServiceUtils.getName(user.getRename());
		ObjectId userId = user.getId();
		
		// check availability
		if(restrictedService.isRestricted(newUsername, RESTRICTED_TYPE.USERNAME)) {
			throw new RestrictedException(RESTRICTED_TYPE.USERNAME);
		}
		
		/*
		// redundant to find this here, the rename will simply fail
		try {
			User foundUser = userService.findUserByUsername(newUsername);
			if(foundUser != null) {
				throw new ExistsException(newUsername);
			}
		} catch(NotFoundException nfe) {
			// this is expected, continue
		}
		*/ 
		
		AdminAction action = null;
		
		try {
			action = adminDao.createAction(null, 
					ADMIN_STATE.INITIAL, ADMIN_TYPE.RENAME, newUsername, null, user);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(action == null) {
			throw new ServiceException();
		}
		
		ObjectId actionId = action.getId();
		if(actionId == null) {
			throw new ServiceException();
		}
		
		// user
		try {
			userDao.rename(userId, null, newUsername);
		} catch(CollisionException dk) {
			// do not revert, but fail instead
			try {
				adminDao.updateAction(actionId, ADMIN_STATE.FAILURE);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			revertRename(action);
			throw new ServiceException(de);
		}
		
		try {
			continueRename(userId, user.getUsername(), newUsername);
		} catch(Exception e) {
			revertRename(action);
			throw e;
		}
		
		try {
			adminDao.updateAction(action.getId(), ADMIN_STATE.ONCE);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		logger.info("Renamed user with id {" + user.getId() + "} to (" 
				+ newUsername + ").");
	}
	
	@Override
	public void continueRename(ObjectId userId, String oldUsername, String newUsername) throws ServiceException {
		ArgCheck.nullCheck(userId, oldUsername, newUsername);
		
		// userInfo
		try {
			userInfoDao.rename(userId, newUsername);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		try {
			userInfoDao.renameUserInAppreciationDates(userId, newUsername);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		// escrow
		try {
			escrowDao.rename(userId, newUsername, true);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		try {
			escrowDao.rename(userId, newUsername, false);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		// follower
		try {
			subscriptionDao.rename(userId, newUsername, true);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		try {
			subscriptionDao.rename(userId, newUsername, false);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		// event
		try {
			eventService.rename(userId, oldUsername, newUsername);
		} catch(ServiceException de) {
			throw new ServiceException(de);
		}
		
		// postings
		try {
			postingDao.rename(userId, newUsername, true);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		try {
			postingDao.rename(userId, newUsername, false);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		// comments
		try {
			commentDao.rename(userId, newUsername, true);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		try {
			commentDao.rename(userId, newUsername, false);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		// correspondence
		try {
			correspondenceDao.rename(userId, newUsername, true);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		try {
			correspondenceDao.rename(userId, newUsername, false);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}

		// messages
		try {
			messageDao.rename(userId, newUsername, true);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		try {
			messageDao.rename(userId, newUsername, false);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		// feedback
		try {
			feedbackDao.rename(userId, newUsername);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		// flags
		try {
			flagDataDao.rename(userId, newUsername);
		} catch(CollisionException dk) {
			throw new ExistsException(newUsername);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public void deleteUser(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		ObjectId userId = user.getId();
		String userIdString = ServiceUtils.getIdString(userId);
		
		Date then = new Date((new Date()).getTime()
				- ServiceValues.USER_DELETED_EXPIRATION_PERIOD);
		if(user.getDeleted() == null) {
			throw new ActionNotAllowedException();
		} else if(user.getDeleted().after(then)) {
			return;
		}
		
		AdminAction action = null;
		
		try {
			action = adminDao.createAction(null, 
					ADMIN_STATE.INITIAL, ADMIN_TYPE.DELETE, 
					userIdString, null, user);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(action == null) {
			throw new ServiceException();
		}
		
		ObjectId actionId = action.getId();
		if(actionId == null) {
			throw new ServiceException();
		}
		
		try {
			delete(action);
		} catch(Exception e) {
			logger.warn("Delete once of user with id {" + userId + "} was not successful!");
			throw e;
		}
		
		try {
			adminDao.updateAction(action.getId(), ADMIN_STATE.ONCE);
		} catch(DaoException de) {
			logger.warn("Admin action was not completed!", de);
			throw new ServiceException(de);
		}
		logger.info("Deleted user once with id {" + userId + "}.");
	}
	
	@Override
	public void completeDelete(AdminAction action) throws ServiceException {
		ArgCheck.nullCheck(action);

		ObjectId userId = new ObjectId(action.getTarget());
		
		try {
			delete(action);
		} catch(Exception e) {
			logger.warn("Delete completion of user with id {" + userId + "} was not successful!");
			throw e;
		}
		
		try {
			adminDao.updateAction(action.getId(), ADMIN_STATE.COMPLETE);
		} catch(DaoException de) {
			logger.warn("Admin action was not completed!", de);
			throw new ServiceException(de);
		}
		logger.info("Deleted user complete with id {" + userId + "}.");
	}
	
	/*
	@CacheEvict(value = {CacheNames.USER, CacheNames.USER_ID_USERNAME, CacheNames.USER_ID_EMAIL}, allEntries = true)
	*/
	protected void delete(AdminAction action) throws ServiceException {
		ArgCheck.nullCheck(action);
		ArgCheck.nullCheck(action.getId(), action.getTarget());
		ObjectId userId = new ObjectId(action.getTarget());
		
		ServiceException lastException = null;
		
		// user
		try {
			userDao.delete(userId);
		} catch(Exception e) {
			// user was not deleted, or does not exist
			logger.info("User id of {" + userId.toHexString() + "} was not deleted!", e);
			lastException = new ServiceException(e);
		}
		
		// userInfo
		try {
			userInfoDao.removeUserInAppreciationDates(userId);
		} catch(Exception e) {
			logger.info("Appreciation dates in user info of user with id of {" + userId.toHexString() + "} was not deleted!", e);
			lastException = new ServiceException(e);
		}
		try {
			userInfoDao.delete(userId);
		} catch(Exception e) {
			logger.info("User info of user with id of {" + userId.toHexString() + "} was not deleted!", e);
			lastException = new ServiceException(e);
		}
		
		// escrow
		try {
			escrowDao.markExists(userId, false, true);
		} catch(Exception e) {
			logger.info("Escrows of user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		try {
			escrowDao.markExists(userId, false, false);
		} catch(Exception e) {
			logger.info("Escrows of user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		try {
			escrowDao.cleanupInvalid(null);
		} catch(Exception e) {
			lastException = new ServiceException(e);
		}
		
		// follower
		try {
			List<FollowInfo> list = followService.getFolloweeList(userId);
			if(list != null) {
				for(FollowInfo fi : list) {
					try {
						subscriptionDao.removeSubscription(
							userId, fi.getUsername().getUsername());
						userService.decrementFollowerCount(fi.getUsername().getId());
					} catch(Exception e) {
						logger.info("Subscription of user with id of {" + userId.toHexString() + "} to user with cached username {" + fi + "} was not removed!", e);
						lastException = new ServiceException(e);
					}
				}
			}
		} catch(Exception e) {
			lastException = new ServiceException(e);
		}
		try {
			subscriptionDao.delete(userId);
		} catch(Exception e) {
			logger.info("Subscription of user with id of {" + userId.toHexString() + "} was not deleted!", e);
			lastException = new ServiceException(e);
		}
		try {
			subscriptionDao.removeUser(userId);
		} catch(Exception e) {
			logger.info("Subscriptions with user with id of {" + userId.toHexString() + "} were not deleted!", e);
			lastException = new ServiceException(e);
		}
		
		// event
		try {
			eventService.removeUser(userId);
		} catch(ServiceException se) {
			logger.info("Events with user with id of {" + userId.toHexString() + "} were not deleted!", se);
			lastException = se;
		}
		
		// postings
		try {
			postingDao.removeUser(userId, true);
		} catch(Exception e) {
			logger.info("Postings with user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		try {
			postingDao.removeUser(userId, false);
		} catch(Exception e) {
			logger.info("Postings with user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		
		// comments
		try {
			commentDao.removeUser(userId, true);
		} catch(Exception e) {
			logger.info("Comments with user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		try {
			commentDao.removeUser(userId, false);
		} catch(Exception e) {
			logger.info("Comments with user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		
		// correspondence
		try {
			correspondenceDao.removeUser(userId, true);
		} catch(Exception e) {
			logger.info("Correspondences with user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		try {
			correspondenceDao.removeUser(userId, false);
		} catch(Exception e) {
			logger.info("Correspondences with user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		
		// messages
		try {
			messageDao.removeUser(userId, true);
		} catch(Exception e) {
			logger.info("Messages with user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		try {
			messageDao.removeUser(userId, false);
		} catch(Exception e) {
			logger.info("Messages with user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}
		
		// feedback
		try {
			feedbackDao.removeUser(userId);
		} catch(Exception e) {
			logger.info("Feedback from user with id of {" + userId.toHexString() + "} were not marked deleted!", e);
			lastException = new ServiceException(e);
		}

		if(lastException != null) {
			throw lastException;
		}
	}

	@Override
	public void completeRename(AdminAction action) throws ServiceException {
		ArgCheck.nullCheck(action);
		ArgCheck.nullCheck(action.getId(), action.getTarget(), action.getDto());
		
		User user = (User)action.getReference();
		ObjectId userId = user.getId();
		// use whatever the username is now
		String oldUsername = ServiceUtils.getName(user.getUsername());
		String newUsername = ServiceUtils.getName(action.getTarget());
		
		try {
			continueRename(userId, oldUsername, newUsername);
		} catch(Exception e) {
			// do not revert!
			logger.info("Error while completing rename of user with id {" + userId.toHexString() + "}.", e);
			throw e;
		}
		
		try {
			adminDao.updateAction(action.getId(), ADMIN_STATE.COMPLETE);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		logger.info("Completed rename of user with id {" + user.getId() + "} to (" 
				+ newUsername + ").");
	}
	
	/*
	@CacheEvict(value = {CacheNames.USER, CacheNames.USER_ID_USERNAME, CacheNames.USER_ID_EMAIL}, allEntries = true)
	*/
	protected void revertRename(AdminAction action) throws ServiceException {
		ArgCheck.nullCheck(action);
		ArgCheck.nullCheck(action.getId(), action.getTarget(), action.getDto());
		
		User user = (User)action.getReference();
		ObjectId userId = user.getId();
		// use whatever the username is now
		String correctUsername = ServiceUtils.getName(user.getUsername());
		String attemptedUsername = ServiceUtils.getName(action.getTarget());
		
		logger.info("Reverting rename of user with id {" + userId.toHexString() + "}.");
		
		ServiceException lastException = null;
		try {
			adminDao.updateAction(action.getId(), ADMIN_STATE.PENDING_FAILURE);
		} catch(DaoException de) {
			logger.warn("Admin action was not completed!", de);
			throw new ServiceException(de);
		}
		
		// user
		try {
			userDao.rename(userId, null, correctUsername);
		} catch(CollisionException dk) {
			logger.info(
					"User with id {" + userId.toHexString() + "} was not rename reverted!", dk);
			lastException = new ServiceException(dk);
		} catch(DaoException de) {
			logger.info(
					"User with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// userInfo
		try {
			userInfoDao.rename(userId, correctUsername);
		} catch(DaoException de) {
			logger.info(
					"UserInfo of user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		try {
			userInfoDao.renameUserInAppreciationDates(userId, correctUsername);
		} catch(DaoException de) {
			logger.info("Appreciation dates containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// escrow
		try {
			escrowDao.rename(userId, correctUsername, true);
		} catch(DaoException de) {
			logger.info("Escrows containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		try {
			escrowDao.rename(userId, correctUsername, false);
		} catch(DaoException de) {
			logger.info("Escrows containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// follower
		try {
			subscriptionDao.rename(userId, correctUsername, true);
		} catch(DaoException de) {
			logger.info("Subscription containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		try {
			subscriptionDao.rename(userId, correctUsername, false);
		} catch(DaoException de) {
			logger.info("Subscription containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// event
		try {
			eventService.rename(userId, attemptedUsername, correctUsername);
		} catch(ServiceException de) {
			logger.info("Events containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// postings
		try {
			postingDao.rename(userId, correctUsername, true);
		} catch(DaoException de) {
			logger.info("Postings containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		try {
			postingDao.rename(userId, correctUsername, false);
		} catch(DaoException de) {
			logger.info("Postings containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// comments
		try {
			commentDao.rename(userId, correctUsername, true);
		} catch(DaoException de) {
			logger.info("Comments containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		try {
			commentDao.rename(userId, correctUsername, false);
		} catch(DaoException de) {
			logger.info("Comments containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// correspondence
		try {
			correspondenceDao.rename(userId, correctUsername, true);
		} catch(DaoException de) {
			logger.info("Corerspondences containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		try {
			correspondenceDao.rename(userId, correctUsername, false);
		} catch(DaoException de) {
			logger.info("Correspondences containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// message
		try {
			messageDao.rename(userId, correctUsername, true);
		} catch(DaoException de) {
			logger.info("Messages containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		try {
			messageDao.rename(userId, correctUsername, false);
		} catch(DaoException de) {
			logger.info("Messages containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// feedback
		try {
			feedbackDao.rename(userId, correctUsername);
		} catch(DaoException de) {
			logger.info("Feedbacks containing user with id {" + userId.toHexString() + "} was not rename reverted!", de);
			lastException = new ServiceException(de);
		}
		
		// rename should not be done until startup, so this is redundant
		/*
		try {
			boolean contained = false;
			List<CachedUsername> followeeList = 
					defaultsFactory.getFolloweeCachedUsernameList();
			if(followeeList != null) {
				for(CachedUsername cu : followeeList) {
					if(PyUtils.objectIdCompare(cu.getId(), userId)) {
						contained = true;
					}
				}
				if(contained) {
					startupJobs.loadFollowees();
				}
			}
		} catch(Exception e) {
			lastException = new ServiceException(e);
		}
		*/
		
		if(lastException == null) {
			// successful failure, no need to try again
			try {
				adminDao.updateAction(action.getId(), ADMIN_STATE.FAILURE);
			} catch(DaoException de) {
				logger.warn("Admin action was not completed!", de);
				throw new ServiceException(de);
			}
		} else {
			throw lastException;
		}
		logger.info("Reverted rename of user with id {" + userId + "}.");
	}
	
	@Override
	public void checkBatchActions(List<ADMIN_STATE> states) throws ServiceException {
		if(states != null && states.isEmpty()) {
			states = null;
		}
		
		Pageable pageable = new PageRequest(0, ServiceValues.ADMIN_ACTION_BATCH_SIZE);
		Date then = new Date((new Date()).getTime()
				- ServiceValues.ADMIN_ACTION_EXPIRATION_PERIOD);
		Page<AdminAction> actions = null;

		try {
			actions = adminDao.findSortedActions(null, states, null, null, then, pageable, 
					DaoValues.SORT_DESCENDING);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(actions == null) {
			throw new ServiceException();
		}
		ServiceException lastException = null;
		for(AdminAction action: actions.getContent()) {
			try {
				checkActionStatus(action);
			} catch(ServiceException se) {
				lastException = se;
				logger.info("Checking action status threw exception!", se);
			}
		}
		if(lastException != null) {
			throw lastException;
		}
		// do not loop through other pages, just one batch per call
	}
	
	@Override
	public void removeFailedActions() throws ServiceException {
		List<ADMIN_STATE> states = new ArrayList<ADMIN_STATE>();
		states.add(ADMIN_STATE.FAILURE);
		Date then = new Date((new Date()).getTime()
				- ServiceValues.ADMIN_ACTION_EXPIRATION_PERIOD);
		try {
			adminDao.remove(states, then);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	protected void checkActionStatus(AdminAction action) throws ServiceException {
		ArgCheck.nullCheck(action);
		ArgCheck.nullCheck(action.getState(), action.getType());
		ADMIN_STATE state = action.getState();
		
		if(ADMIN_STATE.COMPLETE.equals(state) || ADMIN_STATE.FAILURE.equals(state)) {
			return;
		}
		
		if(action.getLastModified() != null) {
			Date then = new Date((new Date()).getTime()
					- ServiceValues.ADMIN_ACTION_EXPIRATION_PERIOD);
			if(action.getLastModified().after(then)) {
				return;
			}
		}
		
		ADMIN_TYPE type = action.getType();
		if(ADMIN_TYPE.RENAME.equals(type)) {
			revertRename(action);
		} else if(ADMIN_TYPE.DELETE.equals(type)) {
			delete(action);
		} else {
			// it is not being dealt with, so fail it
			try {
				adminDao.updateAction(action.getId(), ADMIN_STATE.FAILURE);
			} catch(DaoException de) {
				logger.warn("Admin action was not failed!", de);
				throw new ServiceException(de);
			}
		}
	}
	
	@Override
	public void renameUsers() throws ServiceException {
		Pageable pageable = new PageRequest(0, ServiceValues.USER_RENAME_BATCH_SIZE);
		Page<User> users = null;

		try {
			users = userDao.getRename(pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(users == null) {
			throw new ServiceException();
		}
		ServiceException lastException = null;
		for(User user: users.getContent()) {
			try {
				renameUser(user);
			} catch(ServiceException se) {
				lastException = se;
				logger.warn("Exception renaming for user with id {" 
						+ user.getId().toHexString() + "}!", se);
			}
		}
		if(lastException != null) {
			throw lastException;
		}
		// do not loop through other pages, just one batch per call
	}
	
	@Override
	public void completeRenameUsers() throws ServiceException {
		Pageable pageable = new PageRequest(0, ServiceValues.USER_RENAME_BATCH_SIZE);
		Date then = new Date((new Date()).getTime()
				- ServiceValues.USER_RENAME_COMPLETION_PERIOD);
		Page<AdminAction> actions = null;
		List<ADMIN_STATE> states = new ArrayList<ADMIN_STATE>();
		states.add(ADMIN_STATE.ONCE);

		try {
			actions = adminDao.findSortedActions(null, states, ADMIN_TYPE.RENAME, null, then, pageable, DaoValues.SORT_DESCENDING);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(actions == null) {
			throw new ServiceException();
		}
		ServiceException lastException = null;
		for(AdminAction action: actions.getContent()) {
			try {
				completeRename(action);
			} catch(ServiceException se) {
				lastException = se;
			}
		}
		if(lastException != null) {
			throw lastException;
		}
		// do not loop through other pages, just one batch per call
	}
	
	@Override
	public void deleteUsers() throws ServiceException {
		Pageable pageable = new PageRequest(0, ServiceValues.USER_DELETED_BATCH_SIZE);
		Date then = new Date((new Date()).getTime() - ServiceValues.USER_DELETED_EXPIRATION_PERIOD);
		Page<User> users = null;

		try {
			users = userDao.getDeleted(then, pageable);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(users == null) {
			throw new ServiceException();
		}
		ServiceException lastException = null;
		for(User user: users.getContent()) {
			try {
				deleteUser(user);
			} catch(ServiceException se) {
				lastException = se;
				logger.warn("Exception deleting for user with id {" 
						+ user.getId().toHexString() + "}!", se);
			}
		}
		if(lastException != null) {
			throw lastException;
		}
		// do not loop through other pages, just one batch per call
	}
	
	@Override
	public void completeDeleteUsers() throws ServiceException {
		Pageable pageable = new PageRequest(0, ServiceValues.USER_DELETED_BATCH_SIZE);
		Date then = new Date((new Date()).getTime() - ServiceValues.USER_DELETED_COMPLETION_PERIOD);
		Page<AdminAction> actions = null;
		List<ADMIN_STATE> states = new ArrayList<ADMIN_STATE>();
		states.add(ADMIN_STATE.ONCE);

		try {
			actions = adminDao.findSortedActions(null, states, ADMIN_TYPE.DELETE, null, then, pageable, DaoValues.SORT_DESCENDING);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(actions == null) {
			throw new ServiceException();
		}
		ServiceException lastException = null;
		for(AdminAction action: actions.getContent()) {
			try {
				completeDelete(action);
			} catch(ServiceException se) {
				lastException = se;
			}
		}
		if(lastException != null) {
			throw lastException;
		}
		// do not loop through other pages, just one batch per call
	}
}
