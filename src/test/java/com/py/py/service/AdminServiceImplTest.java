package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doThrow;
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
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.py.py.constants.ServiceValues;
import com.py.py.dao.AdminDao;
import com.py.py.dao.CommentDao;
import com.py.py.dao.EscrowDao;
import com.py.py.dao.FeedbackDao;
import com.py.py.dao.MessageDao;
import com.py.py.dao.PostingDao;
import com.py.py.dao.RestrictedDao;
import com.py.py.dao.SubscriptionDao;
import com.py.py.dao.UserDao;
import com.py.py.dao.UserInfoDao;
import com.py.py.dao.exception.CollisionException;
import com.py.py.domain.AdminAction;
import com.py.py.domain.Comment;
import com.py.py.domain.Posting;
import com.py.py.domain.Restricted;
import com.py.py.domain.User;
import com.py.py.domain.enumeration.ESCROW_TYPE;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.RestrictedWord;
import com.py.py.dto.DTO;
import com.py.py.dto.in.admin.ChangeBalanceDTO;
import com.py.py.dto.in.admin.ChangeEmailAdminDTO;
import com.py.py.dto.in.admin.ChangeRestrictedDTO;
import com.py.py.dto.in.admin.ChangeRolesDTO;
import com.py.py.dto.in.admin.ChangeTallyDTO;
import com.py.py.dto.in.admin.ChangeUsernameDTO;
import com.py.py.dto.in.admin.LockUserDTO;
import com.py.py.dto.out.admin.AdminActionDTO;
import com.py.py.dto.out.admin.RestrictedDTO;
import com.py.py.enumeration.ADMIN_STATE;
import com.py.py.enumeration.ADMIN_TYPE;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.RestrictedException;
import com.py.py.service.exception.ServiceException;

// most methods are pretty much pass-through and cannot be unit tested
public class AdminServiceImplTest extends BaseServiceTest {
	
	@Autowired
	@Qualifier("adminService")
	private AdminService adminService;
	
	@Autowired
	protected AdminDao adminDao;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected UserDao userDao;
	
	@Autowired
	protected UserInfoDao userInfoDao;
	
	@Autowired
	protected EscrowDao escrowDao;
	
	@Autowired
	protected MessageDao messageDao;
	
	@Autowired
	protected EventService eventService;
	
	@Autowired
	protected SubscriptionDao subscriptionDao;
	
	@Autowired
	protected PostingDao postingDao;
	
	@Autowired
	protected CommentDao commentDao;
	
	@Autowired
	protected RestrictedDao restrictedDao;
	
	@Autowired
	protected RestrictedService restrictedService;
	
	@Autowired
	protected FeedbackDao feedbackDao;
	
	private ObjectId userId = new ObjectId();
	
	private ChangeTallyDTO validChangeTallyDTO = new ChangeTallyDTO();
	private List<Restricted> validRestricteds = new ArrayList<Restricted>();
	private List<AdminAction> validList = new ArrayList<AdminAction>();
	private User validAdminUser = createValidUser();
	private CachedUsername cachedAdmin = new CachedUsername(validAdminUser.getId(), validAdminUser.getUsername());
	
	@Before
	public void setUp() {
		reset(adminDao, userService, userDao, userInfoDao, escrowDao, eventService, 
				messageDao, subscriptionDao, postingDao, commentDao, feedbackDao, 
				restrictedDao, restrictedService);

		validList = new ArrayList<AdminAction>();
		validList.add(new AdminAction());
		validList.add(new AdminAction());
		validList.add(new AdminAction());
		
		validChangeTallyDTO.setAppreciation(Long.toString(0l));
		validChangeTallyDTO.setPromotion(10L);
		validChangeTallyDTO.setCost(10L);
		
		Restricted r1 = new Restricted();
		RestrictedWord rw1 = new RestrictedWord();
		rw1.setType(RESTRICTED_TYPE.USERNAME);
		rw1.setWord(validName);
		r1.setId(rw1);
		validRestricteds.add(r1);

		Restricted r2 = new Restricted();
		RestrictedWord rw2 = new RestrictedWord();
		rw2.setType(RESTRICTED_TYPE.PASSWORD);
		rw2.setWord(validName);
		r2.setId(rw2);
		validRestricteds.add(r2);

		Restricted r3 = new Restricted();
		RestrictedWord rw3 = new RestrictedWord();
		rw3.setType(RESTRICTED_TYPE.EMAIL);
		rw3.setWord(validEmail);
		r3.setId(rw3);
		validRestricteds.add(r3);
		
	}
	
	@Test(expected = BadParameterException.class)
	public void unlockUserNull1() throws Exception {
		adminService.unlockUser(null, userId);
	}
	
	@Test(expected = BadParameterException.class)
	public void unlockUserNull2() throws Exception {
 		adminService.unlockUser(validAdminUser, null);
	}
	
	@Test
	public void unlockUser() throws Exception {
		adminService.unlockUser(validAdminUser, userId);
	}
	
	@Test(expected = BadParameterException.class)
	public void lockUserNull1() throws Exception {
		adminService.lockUser(null, userId, new LockUserDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void lockUserNull2() throws Exception {
		adminService.lockUser(validAdminUser, null, new LockUserDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void lockUserNull3() throws Exception {
		adminService.lockUser(validAdminUser, userId, null);
	}
	
	@Test
	public void lockUser() throws Exception {
		adminService.lockUser(validAdminUser, userId, new LockUserDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void setRolesNull1() throws Exception {
		adminService.setRoles(null, userId, new ChangeRolesDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void setRolesNull2() throws Exception {
		adminService.setRoles(validAdminUser, null, new ChangeRolesDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void setRolesNull3() throws Exception {
		adminService.setRoles(validAdminUser, userId, null);
	}
	
	@Test
	public void setRoles() throws Exception {
		adminService.setRoles(validAdminUser, userId, new ChangeRolesDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void setPostingRemoveNull1() throws Exception {
		Posting posting = new Posting();
		posting.setAuthor(cachedAdmin);
		posting.setEnabled(true);
		posting.setPaid(true);
		posting.setRemoved(false);
		posting.setId(new ObjectId());
		posting.setLocked(false);
		adminService.setPostingRemove(null, posting, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setPostingRemoveNull2() throws Exception {
		adminService.setPostingRemove(validAdminUser, null, randomBoolean());
	}
	
	@Test
	public void setPostingRemove() throws Exception {
		Posting posting = new Posting();
		posting.setAuthor(cachedAdmin);
		posting.setEnabled(true);
		posting.setPaid(true);
		posting.setRemoved(false);
		posting.setId(new ObjectId());
		posting.setLocked(false);
		adminService.setPostingRemove(validAdminUser, posting, true);
		adminService.setPostingRemove(validAdminUser, posting, false);
		posting.setRemoved(true);
		adminService.setPostingRemove(validAdminUser, posting, true);
		adminService.setPostingRemove(validAdminUser, posting, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void setCommentRemoveNull1() throws Exception {
		Comment comment = new Comment();
		comment.setAuthor(cachedAdmin);
		comment.setEnabled(true);
		comment.setPaid(true);
		comment.setRemoved(false);
		comment.setId(new ObjectId());
		comment.setLocked(false);
		adminService.setCommentRemove(null, comment, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setCommentRemoveNull2() throws Exception {
		adminService.setCommentRemove(validAdminUser, null, randomBoolean());
	}
	
	@Test
	public void setCommentRemove() throws Exception {
		Comment comment = new Comment();
		comment.setAuthor(cachedAdmin);
		comment.setEnabled(true);
		comment.setPaid(true);
		comment.setRemoved(false);
		comment.setId(new ObjectId());
		comment.setLocked(false);
		adminService.setCommentRemove(validAdminUser, comment, true);
		adminService.setCommentRemove(validAdminUser, comment, false);
		comment.setRemoved(true);
		adminService.setCommentRemove(validAdminUser, comment, true);
		adminService.setCommentRemove(validAdminUser, comment, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void setPostingWarningNull1() throws Exception {
		adminService.setPostingWarning(null, userId, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setPostingWarningNull2() throws Exception {
		adminService.setPostingWarning(validAdminUser, null, randomBoolean());
	}
	
	@Test
	public void setPostingWarning() throws Exception {
		adminService.setPostingWarning(validAdminUser, userId, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setCommentWarningNull1() throws Exception {
		adminService.setCommentWarning(null, userId, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setCommentWarningNull2() throws Exception {
		adminService.setCommentWarning(validAdminUser, null, randomBoolean());
	}
	
	@Test
	public void setCommentWarning() throws Exception {
		adminService.setCommentWarning(validAdminUser, userId, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setPostingFlagNull1() throws Exception {
		adminService.setPostingFlag(null, userId, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setPostingFlagNull2() throws Exception {
		adminService.setPostingFlag(validAdminUser, null, randomBoolean());
	}
	
	@Test
	public void setPostingFlag() throws Exception {
		adminService.setPostingFlag(validAdminUser, userId, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setCommentFlagNull1() throws Exception {
		adminService.setCommentFlag(null, userId, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void setCommentFlagNull2() throws Exception {
		adminService.setCommentFlag(validAdminUser, null, randomBoolean());
	}
	
	@Test
	public void setCommentFlag() throws Exception {
		adminService.setCommentFlag(validAdminUser, userId, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void addBalanceNull1() throws Exception {
		adminService.addBalance(null, new ObjectId(), new ChangeBalanceDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void addBalanceNull2() throws Exception {
		adminService.addBalance(validAdminUser, null, new ChangeBalanceDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void addBalanceNull3() throws Exception {
		adminService.addBalance(validAdminUser, new ObjectId(), null);
	}
	
	@Test
	public void addBalance() throws Exception {
		adminService.addBalance(validAdminUser, userId, new ChangeBalanceDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void removeBalanceNull1() throws Exception {
		adminService.removeBalance(null, new ObjectId(), new ChangeBalanceDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void removeBalanceNull2() throws Exception {
		adminService.removeBalance(validAdminUser, null, new ChangeBalanceDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void removeBalanceNull3() throws Exception {
		adminService.removeBalance(validAdminUser, new ObjectId(), null);
	}
	
	@Test
	public void removeBalance() throws Exception {
		adminService.removeBalance(validAdminUser, userId, new ChangeBalanceDTO());
	}
	
	@Test(expected = BadParameterException.class)
	public void clearLoginAttemptsNull1() throws Exception {
		adminService.clearLoginAttempts(null, userId);
	}
	
	@Test(expected = BadParameterException.class)
	public void clearLoginAttemptsNull2() throws Exception {
		adminService.clearLoginAttempts(validAdminUser, null);
	}
	
	@Test
	public void clearLoginAttempts() throws Exception {
		adminService.clearLoginAttempts(validAdminUser, userId);
	}
	
	@Test(expected = BadParameterException.class)
	public void lockTagNull1() throws Exception {
		adminService.lockTag(null, validTag, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void lockTagNull2() throws Exception {
		adminService.lockTag(validAdminUser, null, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void lockTagNull3() throws Exception {
		adminService.lockTag(validAdminUser, validTag, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void lockTagInvalid2() throws Exception {
		adminService.lockTag(validAdminUser, invalidTag, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void lockTagInvalid3() throws Exception {
		adminService.lockTag(validAdminUser, validTag, invalidLanguage);
	}
	
	@Test
	public void lockTag() throws Exception {
		adminService.lockTag(validAdminUser, validTag, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void unlockTagNull1() throws Exception {
		adminService.unlockTag(null, validTag, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void unlockTagNull2() throws Exception {
		adminService.unlockTag(validAdminUser, null, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void unlockTagNull3() throws Exception {
		adminService.unlockTag(validAdminUser, validTag, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void unlockTagInvalid2() throws Exception {
		adminService.unlockTag(validAdminUser, invalidTag, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void unlockTagInvalid3() throws Exception {
		adminService.unlockTag(validAdminUser, validTag, invalidLanguage);
	}
	
	@Test
	public void unlockTag() throws Exception {
		adminService.unlockTag(validAdminUser, validTag, validLanguage);
	}
	
	@Test(expected = BadParameterException.class)
	public void getPostingNull() throws Exception {
		adminService.getPosting(null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getPostingNotFound() throws Exception {
		when(postingDao.findOne(any(ObjectId.class))).thenReturn(null);
		adminService.getPosting(validObjectId);
	}
	
	@Test
	public void getPosting() throws Exception {
		when(postingDao.findOne(any(ObjectId.class))).thenReturn(new Posting());
		adminService.getPosting(validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void getCommentNull() throws Exception {
		adminService.getComment(null);
	}
	
	@Test(expected = NotFoundException.class)
	public void getCommentNotFound() throws Exception {
		when(commentDao.findOne(any(ObjectId.class))).thenReturn(null);
		adminService.getComment(validObjectId);
	}
	
	@Test
	public void getComment() throws Exception {
		when(commentDao.findOne(any(ObjectId.class))).thenReturn(new Comment());
		adminService.getComment(validObjectId);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePostingTallyNull1() throws Exception {
		adminService.changePostingTally(null, validObjectId, validChangeTallyDTO);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePostingTallyNull2() throws Exception {
		adminService.changePostingTally(validAdminUser, null, validChangeTallyDTO);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePostingTallyNull3() throws Exception {
		adminService.changePostingTally(validAdminUser, validObjectId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePostingTallyInvalid() throws Exception {
		ChangeTallyDTO dto = new ChangeTallyDTO();
		dto.setAppreciation(Long.toString(0l));
		dto.setPromotion(0L);
		dto.setCost(0L);
		adminService.changePostingTally(validAdminUser, validObjectId, dto);
	}
	
	@Test
	public void changePostingTally() throws Exception {
		adminService.changePostingTally(validAdminUser, validObjectId, validChangeTallyDTO);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeCommentTallyNull1() throws Exception {
		adminService.changeCommentTally(null, validObjectId, validChangeTallyDTO);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeCommentTallyNull2() throws Exception {
		adminService.changeCommentTally(validAdminUser, null, validChangeTallyDTO);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeCommentTallyNull3() throws Exception {
		adminService.changeCommentTally(validAdminUser, validObjectId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeCommentTallyInvalid() throws Exception {
		ChangeTallyDTO dto = new ChangeTallyDTO();
		dto.setAppreciation(Long.toString(0l));
		dto.setPromotion(0L);
		dto.setCost(0L);
		adminService.changeCommentTally(validAdminUser, validObjectId, dto);
	}
	
	@Test
	public void changeCommentTally() throws Exception {
		adminService.changeCommentTally(validAdminUser, validObjectId, validChangeTallyDTO);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailNull1() throws Exception {
		ChangeEmailAdminDTO dto = new ChangeEmailAdminDTO();
		dto.setEmail(validEmail);
		adminService.changeEmail(null, validUserId, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailNull2() throws Exception {
		ChangeEmailAdminDTO dto = new ChangeEmailAdminDTO();
		dto.setEmail(validEmail);
		adminService.changeEmail(validAdminUser, null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailNull3() throws Exception {
		adminService.changeEmail(validAdminUser, validUserId, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailInvalid2() throws Exception {
		ChangeEmailAdminDTO dto = new ChangeEmailAdminDTO();
		dto.setEmail(null);
		adminService.changeEmail(validAdminUser, validUserId, dto);
	}
	
	@Test
	public void changeEmail() throws Exception {
		ChangeEmailAdminDTO dto = new ChangeEmailAdminDTO();
		dto.setEmail(validEmail);
		adminService.changeEmail(validAdminUser, validUserId, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void addRestrictedNull1() throws Exception {
		ChangeRestrictedDTO dto = new ChangeRestrictedDTO();
		dto.setWord(validName);
		dto.setType(RESTRICTED_TYPE.USERNAME);
		adminService.addRestricted(null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void addRestrictedNull2() throws Exception {
		adminService.addRestricted(validAdminUser, null);
	}
	
	@Test
	public void addRestricted() throws Exception {
		ChangeRestrictedDTO dto = new ChangeRestrictedDTO();
		dto.setWord(validName);
		dto.setType(RESTRICTED_TYPE.USERNAME);
		adminService.addRestricted(validAdminUser, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeRestrictedNull1() throws Exception {
		adminService.removeRestricted(null, validName, RESTRICTED_TYPE.USERNAME);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeRestrictedNull2() throws Exception {
		adminService.removeRestricted(validAdminUser, null, RESTRICTED_TYPE.USERNAME);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeRestrictedNull3() throws Exception {
		adminService.removeRestricted(validAdminUser, validName, null);
	}
	
	@Test
	public void removeRestricted() throws Exception {
		adminService.removeRestricted(validAdminUser, validName, RESTRICTED_TYPE.USERNAME);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeFlagDataNull1() throws Exception {
		adminService.removeFlagData(null, new ObjectId(), FLAG_TYPE.POSTING);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeFlagDataNull2() throws Exception {
		adminService.removeFlagData(validAdminUser, null, FLAG_TYPE.POSTING);
	}
	
	@Test(expected = BadParameterException.class)
	public void removeFlagDataNull3() throws Exception {
		adminService.removeFlagData(validAdminUser, new ObjectId(), null);
	}
	
	@Test
	public void removeFlagData() throws Exception {
		adminService.removeFlagData(validAdminUser, new ObjectId(), FLAG_TYPE.POSTING);
	}
	
	@Test(expected = BadParameterException.class)
	public void getRestrictedDTOsNull2() throws Exception {
		adminService.getRestrictedDTOs(RESTRICTED_TYPE.USERNAME, null);
	}
	
	@Test
	public void getRestrictedDTOsInvalid() throws Exception {
		Page<Restricted> invalidPage = new PageImpl<Restricted>(
				addNullToList(validRestricteds));
		Mockito.when(restrictedDao.findRestricteds(Mockito.any(RESTRICTED_TYPE.class), 
				Mockito.<Pageable>any()))
			.thenReturn(invalidPage);
		
		Page<RestrictedDTO> result = adminService.getRestrictedDTOs(
				RESTRICTED_TYPE.USERNAME, constructPageable());
		Assert.assertEquals(result.getContent().size(), validRestricteds.size());
	}
	
	@Test
	public void getRestrictedDTOs() throws Exception {
		Page<Restricted> validPage = new PageImpl<Restricted>(validRestricteds);
		Mockito.when(restrictedDao.findRestricteds(Mockito.any(RESTRICTED_TYPE.class), 
				Mockito.<Pageable>any()))
			.thenReturn(validPage);
		
		Page<RestrictedDTO> result = adminService.getRestrictedDTOs(
				RESTRICTED_TYPE.USERNAME, constructPageable());
		Assert.assertEquals(result.getContent().size(), validRestricteds.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void getAdminDTOsNull() throws Exception {
		adminService.getAdminDTOs(validUserId, ADMIN_STATE.INITIAL, ADMIN_TYPE.BALANCE_ADD, 
				validName, null, 1);
	}
	
	@Test
	public void getAdminDTOsInvalidList() throws Exception {
		List<AdminAction> invalidList = addNullToList(validList);
		when(adminDao.findSortedActions(any(ObjectId.class), anyListOf(ADMIN_STATE.class), 
				any(ADMIN_TYPE.class), anyString(), any(Date.class), any(Pageable.class), anyInt()))
			.thenReturn(new PageImpl<AdminAction>(invalidList));
		Page<AdminActionDTO> result = adminService.getAdminDTOs(validUserId, 
				ADMIN_STATE.INITIAL, ADMIN_TYPE.BALANCE_ADD, 
				validName, constructPageable(), 1);
		Assert.assertEquals(result.getContent().size(), validList.size());
	}
	
	@Test
	public void getAdminDTOs() throws Exception {
		when(adminDao.findSortedActions(any(ObjectId.class), anyListOf(ADMIN_STATE.class), 
				any(ADMIN_TYPE.class), anyString(), any(Date.class), 
				any(Pageable.class), anyInt()))
			.thenReturn(new PageImpl<AdminAction>(validList));
		Page<AdminActionDTO> result = adminService.getAdminDTOs(validUserId, 
				ADMIN_STATE.INITIAL, ADMIN_TYPE.BALANCE_ADD, 
				validName, constructPageable(), 1);
		Assert.assertEquals(result.getContent().size(), validList.size());
	}
	
	@Test(expected = BadParameterException.class)
	public void renameUserNull1() throws Exception {
		ChangeUsernameDTO dto = new ChangeUsernameDTO();
		dto.setUsername(validName);
		adminService.renameUser(null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void renameUserNull2() throws Exception {
		adminService.renameUser(createValidUser(), null);
	}
	
	@Test(expected = RestrictedException.class)
	public void renameUserRestricted() throws Exception {
		ChangeUsernameDTO dto = new ChangeUsernameDTO();
		dto.setUsername(validName);
		
		when(restrictedService.isRestricted(anyString(), any(RESTRICTED_TYPE.class)))
			.thenReturn(true);
		adminService.renameUser(createValidUser(), dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void renameUserInvalid() throws Exception {
		User user = createValidUser();
		user.setRename(null);
		adminService.renameUser(user);
	}
	
	@Test(expected = ExistsException.class)
	public void renameUserExists() throws Exception {
		User user = createValidUser();
		user.setRename(validOtherName);

		AdminAction action = new AdminAction();
		action.setId(validObjectId);
		
		when(restrictedService.isRestricted(anyString(), any(RESTRICTED_TYPE.class)))
			.thenReturn(false);
		
		when(adminDao.createAction(any(CachedUsername.class), any(ADMIN_STATE.class), 
				any(ADMIN_TYPE.class), anyString(), any(DTO.class), any()))
			.thenReturn(action);
		doThrow(new CollisionException()).when(userDao).rename(any(ObjectId.class), anyString(), anyString());
		adminService.renameUser(user);
	}
	
	@Test
	public void renameUser() throws Exception {
		User user = createValidUser();
		user.setRename(validOtherName);

		AdminAction action = new AdminAction();
		action.setId(validObjectId);
		
		when(adminDao.createAction(any(CachedUsername.class), any(ADMIN_STATE.class), 
				any(ADMIN_TYPE.class), anyString(), any(DTO.class), any()))
			.thenReturn(action);
		
		when(restrictedService.isRestricted(anyString(), any(RESTRICTED_TYPE.class)))
			.thenReturn(false);
		when(userService.findUserByUsername(anyString()))
			.thenThrow(new NotFoundException(validName));
		adminService.renameUser(user);
		
		verify(userDao).rename(any(ObjectId.class), anyString(), anyString());
		verify(userInfoDao).rename(any(ObjectId.class), anyString());
		verify(escrowDao, times(2)).rename(any(ObjectId.class), anyString(), anyBoolean());
		verify(subscriptionDao, times(2)).rename(any(ObjectId.class), anyString(), anyBoolean());
		verify(eventService).rename(any(ObjectId.class), anyString(), anyString());
		verify(messageDao, times(2)).rename(any(ObjectId.class), anyString(), anyBoolean());
		verify(postingDao, times(2)).rename(any(ObjectId.class), anyString(), anyBoolean());
		verify(commentDao, times(2)).rename(any(ObjectId.class), anyString(), anyBoolean());
		verify(feedbackDao).rename(any(ObjectId.class), anyString());
	}
	
	@Test(expected = BadParameterException.class)
	public void deleteUserNull() throws Exception {
		adminService.deleteUser(null);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void deleteUserNotAllowed() throws Exception {
		User user = createValidUser();
		user.setDeleted(null);
		adminService.deleteUser(user);
	}
	
	@Test
	public void deleteUser() throws Exception {
		User user = createValidUser();
		user.setDeleted(new Date());
		
		AdminAction action = new AdminAction();
		action.setId(validObjectId);
		action.setTarget(validObjectId.toHexString());
		when(adminDao.createAction(any(CachedUsername.class), any(ADMIN_STATE.class), 
				any(ADMIN_TYPE.class), anyString(), any(DTO.class), any()))
			.thenReturn(action);
		
		adminService.deleteUser(user);
		user.setDeleted(new Date((new Date()).getTime() - 
				(ServiceValues.USER_DELETED_EXPIRATION_PERIOD * 2)));
		adminService.deleteUser(user);
		
		verify(userDao, times(1)).delete(any(ObjectId.class));
		verify(userInfoDao, times(1)).delete(any(ObjectId.class));
		verify(escrowDao, times(2)).markExists(any(ObjectId.class), anyBoolean(), 
				anyBoolean());
		verify(escrowDao, times(1)).cleanupInvalid(any(ESCROW_TYPE.class));
		verify(subscriptionDao, times(1)).delete(any(ObjectId.class));
		verify(subscriptionDao, times(1)).removeUser(any(ObjectId.class));
		verify(eventService, times(1)).removeUser(any(ObjectId.class));
		verify(messageDao, times(2)).removeUser(any(ObjectId.class), anyBoolean());
		verify(postingDao, times(2)).removeUser(any(ObjectId.class), anyBoolean());
		verify(commentDao, times(2)).removeUser(any(ObjectId.class), anyBoolean());
		verify(feedbackDao, times(1)).removeUser(any(ObjectId.class));
		verify(adminDao).createAction(any(CachedUsername.class), any(ADMIN_STATE.class), 
				any(ADMIN_TYPE.class), anyString(), any(DTO.class), any());
		verify(adminDao).updateAction(any(ObjectId.class), any(ADMIN_STATE.class));
	}
	
	@Test
	public void checkBatchActions() throws Exception {
		when(adminDao.findSortedActions(any(ObjectId.class), 
				anyListOf(ADMIN_STATE.class), any(ADMIN_TYPE.class), anyString(), 
				any(Date.class), any(Pageable.class), anyInt()))
			.thenReturn(new PageImpl<AdminAction>(validList));
		adminService.checkBatchActions(null);
		List<ADMIN_STATE> states = new ArrayList<ADMIN_STATE>();
		adminService.checkBatchActions(states);
		states.add(ADMIN_STATE.COMPLETE);
		adminService.checkBatchActions(states);
	}
	
	@Test
	public void removeFailedActions() throws Exception {
		adminService.removeFailedActions();
	}
	
	@Test(expected = ServiceException.class)
	public void deleteUsersServiceException() throws Exception {
		List<User> userList = new ArrayList<User>();
		userList.add(createValidUser());
		userList.add(createValidUser());
		userList.add(createValidUser());
		when(userDao.getDeleted(any(Date.class), any(Pageable.class)))
			.thenReturn(null);
		
		adminService.deleteUsers();
	}
	
	@Test
	public void deleteUsers() throws Exception {
		List<User> userList = new ArrayList<User>();
		User user = createValidUser();
		user.setDeleted(new Date());
		userList.add(user);
		when(userDao.getDeleted(any(Date.class), any(Pageable.class)))
			.thenReturn(new PageImpl<User>(userList));
		
		adminService.deleteUsers();
	}
	
}
