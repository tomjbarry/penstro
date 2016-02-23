package com.py.py.service;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.types.ObjectId;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.mongodb.DuplicateKeyException;
import com.py.py.constants.OverrideRoleNames;
import com.py.py.constants.PendingActions;
import com.py.py.dao.UserDao;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.subdomain.AuthenticationInformation;
import com.py.py.domain.subdomain.Balance;
import com.py.py.domain.subdomain.LoginAttempt;
import com.py.py.domain.subdomain.Permission;
import com.py.py.dto.in.ChangeEmailDTO;
import com.py.py.dto.in.ChangePasswordDTO;
import com.py.py.dto.in.ChangePasswordUnauthedDTO;
import com.py.py.dto.in.ChangePaymentDTO;
import com.py.py.dto.in.RegisterUserDTO;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.security.AuthenticationRepositoryImpl;
import com.py.py.security.UserAuthenticationToken;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.google.GoogleManager;
import com.py.py.service.impl.AuthenticationServiceImpl;
import com.py.py.service.util.DefaultsFactory;

public class AuthenticationServiceImplTest extends BaseServiceTest {

	@Autowired
	@Qualifier("authenticationService")
	private AuthenticationServiceImpl authService;

	@Autowired
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserService userService;
	
	@Autowired
	private RoleService roleService;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private MessageService messageService;
	
	@Autowired
	private FollowService followService;
	
	@Autowired
	private UserDao userDao;
	
	//@Autowired
	//private UserInfoDao userInfoDao;
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private DefaultsFactory defaultsFactory;
	
	@Autowired
	private AuthenticationRepositoryImpl repository;
	
	@Autowired
	private GoogleManager googleManager;

	private String validAddress = "127.0.0.1";
	private String validToken = "NTM1NDFjMDRlNGIwNzBhYzU4Y2EzZTgw" + ":NTM1NDFjMDRlNGIwNzBhYzU4Y2EzZTgw";
	//private String validTokenDecoded = "53541c04e4b070ac58ca3e80";
	private byte[] validTokenDecodedEncoded = {81, -128, -62, 16, 1, 107, -64, 16, -1, -33, 35, -97, 119, -98, -59, 84, 54, -5, -28, 86, -119, 112, -81, -114, -121, 91, -70, -107, -36, 32, 97, -34};
	private String validHashedPassword = "hashedPassword";
	private String validOtherHashedPassword = "otherHashedPassword";
	
	@Before
	public void setUp() {
		reset(authenticationManager, userService, roleService, eventService, 
				passwordEncoder, messageService, followService, userDao, 
				emailService, defaultsFactory, repository, googleManager);
	}
	
	@Test(expected = BadParameterException.class)
	public void registerUserNull1() throws Exception {
		authService.registerUser(null, validLanguage, validAddress, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void registerUserNull2() throws Exception {
		RegisterUserDTO dto = new RegisterUserDTO();
		dto.setUsername(validName);
		dto.setPassword(validName);
		dto.setEmail(validEmail);
		when(userDao.findByUniqueName(anyString())).thenReturn(createValidUser());
		authService.registerUser(dto, null, validAddress, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void registerUserInvalid() throws Exception {
		RegisterUserDTO dto = new RegisterUserDTO();
		dto.setUsername(validName);
		dto.setPassword(validName);
		dto.setEmail(validEmail);
		when(userDao.findByUniqueName(anyString())).thenReturn(createValidUser());
		authService.registerUser(dto, invalidLanguage, validAddress, randomBoolean());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void registerUserActionNotAllowed() throws Exception {
		RegisterUserDTO dto = new RegisterUserDTO();
		dto.setUsername(validName);
		dto.setPassword(validName);
		dto.setEmail(validEmail);
		when(roleService.getDefaultRoles()).thenReturn(null);
		when(roleService.getDefaultOverrideRoles()).thenReturn(null);
		when(googleManager.verifyRecaptchaResponse(anyString(), anyString())).thenReturn(false);
		DuplicateKeyException dk = mock(DuplicateKeyException.class);
		when(userDao.create(any(User.class))).thenThrow(dk);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		authService.registerUser(dto, validLanguage, validAddress, true);
		verify(googleManager).verifyRecaptchaResponse(anyString(), anyString());
	}
	
	@Test(expected = ExistsException.class)
	public void registerUserExists() throws Exception {
		RegisterUserDTO dto = new RegisterUserDTO();
		dto.setUsername(validName);
		dto.setPassword(validName);
		dto.setEmail(validEmail);
		when(roleService.getDefaultRoles()).thenReturn(null);
		when(roleService.getDefaultOverrideRoles()).thenReturn(null);
		when(googleManager.verifyRecaptchaResponse(anyString(), anyString())).thenReturn(true);
		DuplicateKeyException dk = mock(DuplicateKeyException.class);
		when(userDao.create(any(User.class))).thenThrow(dk);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		authService.registerUser(dto, validLanguage, validAddress, true);
		verify(googleManager).verifyRecaptchaResponse(anyString(), anyString());
	}
	
	@Test
	public void registerUser() throws Exception {
		RegisterUserDTO dto = new RegisterUserDTO();
		dto.setUsername(validName);
		dto.setPassword(validName);
		dto.setEmail(validEmail);
		when(roleService.getDefaultRoles()).thenReturn(null);
		when(roleService.getDefaultOverrideRoles()).thenReturn(null);
		when(googleManager.verifyRecaptchaResponse(anyString(), anyString())).thenReturn(true);
		when(userDao.findByUniqueName(anyString())).thenReturn(null);
		when(userDao.findByEmail(anyString())).thenReturn(null);
		when(userDao.create(any(User.class))).thenReturn(createValidUser());
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		authService.registerUser(dto, validLanguage, validAddress, true);
		verify(googleManager).verifyRecaptchaResponse(anyString(), anyString());
		verify(roleService).getDefaultRoles();
		verify(roleService).getDefaultOverrideRoles();
		verify(userService).createUserInfo(any(ObjectId.class), anyString(), anyString());
		verify(followService).create(any(User.class));
		verify(emailService).confirmation(anyString(), anyString());
		verify(repository).addToken(any(ObjectId.class), any(byte[].class), any(Date.class), anyLong());
	}
	
	@Test
	public void registerUserNoRecaptcha() throws Exception {
		RegisterUserDTO dto = new RegisterUserDTO();
		dto.setUsername(validName);
		dto.setPassword(validName);
		dto.setEmail(validEmail);
		when(roleService.getDefaultRoles()).thenReturn(null);
		when(roleService.getDefaultOverrideRoles()).thenReturn(null);
		when(googleManager.verifyRecaptchaResponse(anyString(), anyString())).thenReturn(false);
		when(userDao.findByUniqueName(anyString())).thenReturn(null);
		when(userDao.findByEmail(anyString())).thenReturn(null);
		when(userDao.create(any(User.class))).thenReturn(createValidUser());
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		authService.registerUser(dto, validLanguage, validAddress, false);
		verify(roleService).getDefaultRoles();
		verify(roleService).getDefaultOverrideRoles();
		verify(userService).createUserInfo(any(ObjectId.class), anyString(), anyString());
		verify(followService).create(any(User.class));
		verify(emailService).confirmation(anyString(), anyString());
		verify(repository).addToken(any(ObjectId.class), any(byte[].class), any(Date.class), anyLong());
	}
	
	@Test(expected = BadParameterException.class)
	public void getLastLoginNull() throws Exception {
		authService.getLastLogin(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getLastLoginInvalid() throws Exception {
		authService.getLastLogin(createInvalidUser());
	}
	
	@Test
	public void getLastLogin() throws Exception {
		User user = createValidUser();
		user.setLoginAttempts(null);
		authService.getLastLogin(user);
		List<LoginAttempt> attempts = new ArrayList<LoginAttempt>();
		user.setLoginAttempts(attempts);
		authService.getLastLogin(user);
		LoginAttempt attempt = new LoginAttempt();
		attempt.setLocation(validAddress);
		attempt.setSuccess(true);
		attempt.setTime(new Date());
		attempts.add(attempt);
		authService.getLastLogin(user);
	}
	
	@Test(expected = BadParameterException.class)
	public void getCurrentUserDTONull1() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(validUserId);
		userInfo.setUsername(validName);
		authService.getCurrentUserDTO(null, userInfo);
	}
	
	@Test(expected = BadParameterException.class)
	public void getCurrentUserDTONull2() throws Exception {
		authService.getCurrentUserDTO(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getCurrentUserDTOInvalid1() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(validUserId);
		userInfo.setUsername(validName);
		authService.getCurrentUserDTO(createInvalidUser(), userInfo);
	}
	
	@Test
	public void getCurrentUserDTO() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(validUserId);
		userInfo.setUsername(validName);
		when(eventService.getNotificationCount(any(ObjectId.class), 
				anyListOf(EVENT_TYPE.class), any(Date.class))).thenReturn(0L);
		when(eventService.getFeedCount(any(ObjectId.class), anyListOf(ObjectId.class), 
				anyListOf(EVENT_TYPE.class), any(Date.class))).thenReturn(0L);
		when(messageService.getMessageCount(any(User.class), anyBoolean())).thenReturn(0L);
		authService.getCurrentUserDTO(createValidUser(), userInfo);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBalanceDTONull() throws Exception {
		authService.getBalanceDTO(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getBalanceDTOInvalid() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(validUserId);
		userInfo.setUsername(validName);
		userInfo.setBalance(null);
		authService.getBalanceDTO(userInfo);
	}
	
	@Test
	public void getBalanceDTO() throws Exception {
		UserInfo userInfo = new UserInfo();
		userInfo.setId(validUserId);
		userInfo.setUsername(validName);
		Balance balance = new Balance();
		balance.setGold(10L);
		userInfo.setBalance(balance);
		authService.getBalanceDTO(userInfo);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void loadUserByUsernameNull() throws Exception {
		authService.loadUserByUsername(null);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void loadUserByUsernameInvalid() throws Exception {
		authService.loadUserByUsername(invalidName);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void loadUserByUsernameNotFound() throws Exception {
		when(userService.findUserByUsername(anyString()))
			.thenThrow(new NotFoundException(validName));
		authService.loadUserByUsername(validName);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void loadUserByUsernameFoundNull() throws Exception {
		when(userService.findUserByUsername(anyString()))
			.thenReturn(null);
		authService.loadUserByUsername(validName);
	}
	
	@Test(expected = UsernameNotFoundException.class)
	public void loadUserByUsernameInvalidRoles() throws Exception {
		when(userService.findUserByUsername(anyString()))
			.thenReturn(createValidUser());
		when(roleService.getPermissionsFromRoleNames(anyListOf(String.class), 
				anyListOf(String.class))).thenThrow(new BadParameterException());
		authService.loadUserByUsername(validName);
	}
	
	@Test
	public void loadUserByUsername() throws Exception {
		User user = createValidUser();
		user.setUsername(validName);
		user.setPassword(validName);
		when(userService.findUserByUsername(anyString()))
			.thenReturn(user);
		when(roleService.getPermissionsFromRoleNames(anyListOf(String.class), 
				anyListOf(String.class))).thenReturn(new ArrayList<Permission>());
		authService.loadUserByUsername(validName);
	}
	
	@Test(expected = BadParameterException.class)
	public void recordLoginAttemptNull1() throws Exception {
		authService.recordLoginAttempt(null, validAddress, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void recordLoginAttemptNull2() throws Exception {
		authService.recordLoginAttempt(createValidUser(), null, randomBoolean());
	}
	
	@Test(expected = BadParameterException.class)
	public void recordLoginAttemptInvalid() throws Exception {
		authService.recordLoginAttempt(createInvalidUser(), validAddress, randomBoolean());
	}
	
	@Test
	public void recordLoginAttempt() throws Exception {
		User user = createValidUser();
		user.setSavedLocations(null);
		authService.recordLoginAttempt(user, validAddress, true);
		authService.recordLoginAttempt(user, validAddress, false);
		List<String> locations = new ArrayList<String>();
		user.setSavedLocations(locations);
		authService.recordLoginAttempt(user, validAddress, true);
		authService.recordLoginAttempt(user, validAddress, false);
		locations.add(validAddress);
		authService.recordLoginAttempt(user, validAddress, true);
		authService.recordLoginAttempt(user, validAddress, false);
	}
	
	@Test(expected = BadParameterException.class)
	public void getLoginFailureCountNull() throws Exception {
		authService.getLoginFailureCount(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void getLoginFailureCountInvalid() throws Exception {
		authService.getLoginFailureCount(createInvalidUser());
	}
	
	@Test
	public void getLoginFailureCount() throws Exception {
		User user = createValidUser();
		user.setLoginAttempts(null);
		authService.getLoginFailureCount(user);
		List<LoginAttempt> attempts = new ArrayList<LoginAttempt>();
		user.setLoginAttempts(attempts);
		authService.getLoginFailureCount(user);
		attempts.add(new LoginAttempt());
		authService.getLoginFailureCount(user);
	}
	
	@Test(expected = BadParameterException.class)
	public void isLoginLockedNull1() throws Exception {
		authService.isLoginLocked(null, validAddress);
	}
	
	@Test(expected = BadParameterException.class)
	public void isLoginLockedInvalid() throws Exception {
		authService.isLoginLocked(createInvalidUser(), validAddress);
	}
	
	@Test
	public void isLoginLocked() throws Exception {
		User user = createValidUser();
		user.setSavedLocations(null);
		authService.isLoginLocked(user, null);
		authService.isLoginLocked(user, validAddress);
		List<String> locations = new ArrayList<String>();
		user.setSavedLocations(locations);
		authService.isLoginLocked(user, validAddress);
		locations.add(validAddress);
		authService.isLoginLocked(user, validAddress);
	}
	
	@Test
	public void authenticate() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		UserDetails userDetails = mock(UserDetails.class);
		User user = createValidUser();
		user.setUsername(validName);
		user.setPassword(validName);
		AuthenticationInformation aI = new AuthenticationInformation(validTokenDecodedEncoded, new Date(new Date().getTime() + 10000l), 10000l);
		user.setAuthenticationInformation(Arrays.asList(aI));
		when(userService.findUserByUsername(anyString()))
			.thenReturn(user);
		when(roleService.getPermissionsFromRoleNames(anyListOf(String.class), 
				anyListOf(String.class))).thenReturn(new ArrayList<Permission>());
		when(userDetails.getUsername()).thenReturn(validName);
		when(userService.findUser(any(ObjectId.class))).thenReturn(user);
		when(request.getHeader(anyString())).thenReturn(null).thenReturn(validToken);
		Assert.assertNull(authService.authenticate(request, response));
		Assert.assertNotNull(authService.authenticate(request, response));
	}
	
	@Test
	public void logout() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		UserAuthenticationToken authentication = mock(UserAuthenticationToken.class);
		when(authentication.getUser()).thenReturn(createValidUser());
		authService.logout(request, response, authentication);
	}
	
	@Test(expected = BadParameterException.class)
	public void onLoginSuccessNull1() throws Exception {
		HttpServletResponse response = mock(HttpServletResponse.class);
		authService.onLoginSuccess(null, response, createValidUser(), authService.generateTokenData(), 10000l, true);
	}
	
	@Test(expected = BadParameterException.class)
	public void onLoginSuccessNull2() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		authService.onLoginSuccess(request, null, createValidUser(), authService.generateTokenData(), 10000l, true);
	}
	
	@Test(expected = BadParameterException.class)
	public void onLoginSuccessNull3() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		authService.onLoginSuccess(request, response, null, authService.generateTokenData(), 10000l, true);
	}
	
	@Test(expected = BadParameterException.class)
	public void onLoginSuccessNull4() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		authService.onLoginSuccess(request, response, createValidUser(), null, 10000l, true);
	}
	
	@Test
	public void onLoginSuccess() throws Exception {
		HttpServletRequest request = mock(HttpServletRequest.class);
		HttpServletResponse response = mock(HttpServletResponse.class);
		PrintWriter writer = mock(PrintWriter.class);
		when(response.getWriter()).thenReturn(writer);
		authService.onLoginSuccess(request, response, createValidUser(), authService.generateTokenData(), 10000l, true);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailRequestNull1() throws Exception {
		authService.changeEmailRequest(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailRequestInvalid1() throws Exception {
		authService.changeEmailRequest(createInvalidUser());
	}
	
	@Test
	public void changeEmailRequest() throws Exception {
		User user = createValidUser();
		//List<String> overrideRoles = new ArrayList<String>();
		//overrideRoles.add(OverrideRoleNames.UNCONFIRMED);
		//user.setOverrideRoles(overrideRoles);
		user.setPassword(validHashedPassword);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changeEmailRequest(user);
		
		verify(emailService).changeEmail(anyString(), anyString());
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailNull1() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		authService.changeEmail(null, userInfo, dto, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailNull2() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		authService.changeEmail(createValidUser(), null, dto, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailNull3() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
		authService.changeEmail(createValidUser(), userInfo, null, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailNull4() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		authService.changeEmail(createValidUser(), userInfo, dto, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailInvalid1() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		authService.changeEmail(createInvalidUser(), userInfo, dto, validToken);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void changeEmailNotAllowed() throws Exception {
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validOtherEmail);
		dto.setPassword(validName);
		User user = createValidUser();
		user.setPassword(validOtherHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(false);
		authService.changeEmail(user, userInfo, dto, validToken);
	}
	
	@Test(expected = ExistsException.class)
	public void changeEmailExists() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		User user = createValidUser();
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
		user.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changeEmail(user, userInfo, dto, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailInvalidToken() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validOtherEmail);
		dto.setPassword(validName);
		User user = createValidUser();
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
		user.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(false);
		authService.changeEmail(user, userInfo, dto, validToken);
	}
	
	@Test
	public void changeEmail() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validOtherEmail);
		dto.setPassword(validName);
		User user = createValidUser();
		UserInfo userInfo = createValidUserInfo();
		List<String> l = new ArrayList<String>();
		l.add(PendingActions.UNCONFIRMED_EMAIL);
		userInfo.setPendingActions(l);
		user.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changeEmail(user, userInfo, dto, validToken);

		verify(userDao).removeEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class));
		verify(userDao).updateUser(any(ObjectId.class), anyString(), anyString());
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailPendingActionNull1() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		authService.changeEmailPendingAction(null, createValidUserInfo(), dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailPendingActionNull2() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		authService.changeEmailPendingAction(createValidUser(), null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailPendingActionNull3() throws Exception {
		authService.changeEmailPendingAction(createValidUser(), createValidUserInfo(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailPendingActionInvalid1() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		authService.changeEmailPendingAction(createInvalidUser(), createValidUserInfo(), dto);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void changeEmailPendingActionNotAllowed() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validOtherEmail);
		dto.setPassword(validName);
		User user = createValidUser();
		user.setPassword(validOtherHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(false);
		authService.changeEmailPendingAction(user, createValidUserInfo(), dto);
	}
	
	@Test(expected = ExistsException.class)
	public void changeEmailPendingActionExists() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validEmail);
		dto.setPassword(validName);
		User user = createValidUser();
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
		user.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changeEmailPendingAction(user, userInfo, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changeEmailPendingActionInvalidPendingAction() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validOtherEmail);
		dto.setPassword(validName);
		User user = createValidUser();
		List<String> overrideRoles = new ArrayList<String>();
		//overrideRoles.add(OverrideRoleNames.UNCONFIRMED);
		user.setOverrideRoles(overrideRoles);
		user.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(false);
		authService.changeEmailPendingAction(user, createValidUserInfo(), dto);
	}
	
	@Test
	public void changeEmailPendingAction() throws Exception {
		ChangeEmailDTO dto = new ChangeEmailDTO();
		dto.setEmail(validOtherEmail);
		dto.setPassword(validName);
		User user = createValidUser();
		user.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
		authService.changeEmailPendingAction(user, userInfo, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordUnauthedNull1() throws Exception {
		ChangePasswordUnauthedDTO dto = new ChangePasswordUnauthedDTO();
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		authService.changePasswordUnauthed(null, dto, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordUnauthedNull2() throws Exception {
		authService.changePasswordUnauthed(createValidUser(), null, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordUnauthedNull3() throws Exception {
		ChangePasswordUnauthedDTO dto = new ChangePasswordUnauthedDTO();
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		authService.changePasswordUnauthed(createValidUser(), dto, null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordUnauthedInvalid1() throws Exception {
		ChangePasswordUnauthedDTO dto = new ChangePasswordUnauthedDTO();
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		authService.changePasswordUnauthed(createInvalidUser(), dto, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordUnauthedInvalidToken() throws Exception {
		ChangePasswordUnauthedDTO dto = new ChangePasswordUnauthedDTO();
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(false);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		authService.changePasswordUnauthed(createValidUser(), dto, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordUnauthedNoMatch() throws Exception {
		ChangePasswordUnauthedDTO dto = new ChangePasswordUnauthedDTO();
		dto.setNewPassword(validName);
		dto.setConfirmNewPassword(validOtherName);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		
		authService.changePasswordUnauthed(createValidUser(), dto, validToken);
		
		verify(userDao).removeEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class));
	}
	
	@Test
	public void changePasswordUnauthed() throws Exception {
		ChangePasswordUnauthedDTO dto = new ChangePasswordUnauthedDTO();
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		
		authService.changePasswordUnauthed(createValidUser(), dto, validToken);
		
		verify(userDao).removeEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class));
		verify(userDao).updateUser(any(ObjectId.class), anyString(), anyString());
		verify(repository).removeAllUserTokens(any(ObjectId.class));
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordNull1() throws Exception {
		ChangePasswordDTO dto = new ChangePasswordDTO();
		dto.setOldPassword(validName);
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		authService.changePassword(null, dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordNull2() throws Exception {
		authService.changePassword(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordInvalid1() throws Exception {
		ChangePasswordDTO dto = new ChangePasswordDTO();
		dto.setOldPassword(validName);
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		authService.changePassword(createInvalidUser(), dto);
	}
	
	@Test(expected = BadParameterException.class)
	public void changePasswordNoMatch() throws Exception {
		ChangePasswordDTO dto = new ChangePasswordDTO();
		dto.setOldPassword(validName);
		dto.setNewPassword(validName);
		dto.setConfirmNewPassword(validOtherName);
		User user = createValidUser();
		user.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		authService.changePassword(user, dto);
		
		verify(userDao).updateUser(any(ObjectId.class), anyString(), anyString());
		verify(repository).removeAllUserTokens(any(ObjectId.class));
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void changePasswordNotAllowed() throws Exception {
		ChangePasswordDTO dto = new ChangePasswordDTO();
		dto.setOldPassword(validName);
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		User user = createValidUser();
		user.setPassword(validOtherHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(false);
		authService.changePassword(user, dto);
	}
	
	@Test
	public void changePassword() throws Exception {
		ChangePasswordDTO dto = new ChangePasswordDTO();
		dto.setOldPassword(validName);
		dto.setNewPassword(validOtherName);
		dto.setConfirmNewPassword(validOtherName);
		User user = createValidUser();
		user.setPassword(validHashedPassword);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		authService.changePassword(user, dto);
		
		verify(userDao).updateUser(any(ObjectId.class), anyString(), anyString());
		// do not invalidate all logins, or the user gets logged out!
	}
	
	// NO EXCEPTION! hide whether email in use
	@Test
	public void resetPasswordNull() throws Exception {
		authService.resetPassword(null);
		verify(emailService, times(0)).resetPassword(anyString(), anyString());
	}
	
	@Test
	public void resetPassword() throws Exception {
		User user = createValidUser();
		user.setEmail(validEmail);
		authService.resetPassword(user);
		verify(emailService, times(1)).resetPassword(anyString(), anyString());
	}
	
	@Test(expected = BadParameterException.class)
	public void sendConfirmationNull1() throws Exception {
		authService.sendConfirmation(null, createValidUserInfo());
	}
	
	@Test(expected = BadParameterException.class)
	public void sendConfirmationNull2() throws Exception {
		authService.sendConfirmation(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void sendConfirmationInvalid() throws Exception {
		authService.sendConfirmation(createInvalidUser(), createValidUserInfo());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void sendConfirmationNotAllowed() throws Exception {
		authService.sendConfirmation(createValidUser(), createValidUserInfo());
	}
	
	@Test
	public void sendConfirmation() throws Exception {
		User user = createValidUser();
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
		authService.sendConfirmation(user, userInfo);
	}
	
	@Test(expected = BadParameterException.class)
	public void deleteNull1() throws Exception {
		authService.delete(null, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void deleteNull2() throws Exception {
		authService.delete(createValidUser(), null);
	}
	
	@Test(expected = BadParameterException.class)
	public void deleteInvalid() throws Exception {
		authService.delete(createInvalidUser(), validToken);
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void deleteNotAllowed() throws Exception {
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		
		User user = createValidUser();
		user.setDeleted(new Date());
		authService.delete(user, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void deleteInvalidToken() throws Exception {
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(false);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		
		authService.delete(createValidUser(), validToken);
		
		verify(userService, never()).addOverrideRole(validUserId, 
				OverrideRoleNames.DELETED);
	}
	
	@Test
	public void delete() throws Exception {
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		
		authService.delete(createValidUser(), validToken);
		
		verify(userDao).removeEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class));
		verify(userService).addOverrideRole(validUserId, OverrideRoleNames.DELETED);
	}
	
	@Test(expected = BadParameterException.class)
	public void undeleteNull() throws Exception {
		authService.undelete(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void undeleteInvalid() throws Exception {
		authService.undelete(createInvalidUser());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void undeleteNotAllowed() throws Exception {
		authService.undelete(createValidUser());
	}
	
	@Test
	public void undelete() throws Exception {
		User user = createValidUser();
		user.setDeleted(new Date());
		authService.undelete(user);
		verify(userService).removeOverrideRole(validUserId, OverrideRoleNames.DELETED);
	}
	
	@Test(expected = BadParameterException.class)
	public void sendDeleteNull() throws Exception {
		authService.sendDelete(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void sendDeleteInvalid() throws Exception {
		authService.sendDelete(createInvalidUser());
	}
	
	@Test(expected = ActionNotAllowedException.class)
	public void sendDeleteNotAllowed() throws Exception {
		User user = createValidUser();
		user.setDeleted(new Date());
		authService.sendDelete(user);
	}
	
	@Test
	public void sendDelete() throws Exception {
		User user = createValidUser();
		user.setDeleted(null);
		authService.sendDelete(user);
	}
	
	@Test(expected = BadParameterException.class)
	public void confirmationNull1() throws Exception {
		authService.confirmation(null, createValidUserInfo(), validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void confirmationNull2() throws Exception {
		authService.confirmation(createValidUser(), null, validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void confirmationInvalid() throws Exception {
		authService.confirmation(createInvalidUser(), createValidUserInfo(), validToken);
	}
	
	@Test
	public void confirmationNotAllowed() throws Exception {
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		
		User user = createValidUser();
		authService.confirmation(user, createValidUserInfo(), validToken);
	}
	
	@Test(expected = BadParameterException.class)
	public void confirmationInvalidToken() throws Exception {
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(false);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		
		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
		authService.confirmation(createValidUser(), userInfo, validToken);
		verify(userService, never()).removePendingActions(validUserId, Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
	}
	
	@Test
	public void confirmation() throws Exception {
		User user = createValidUser();
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);

		UserInfo userInfo = createValidUserInfo();
		userInfo.setPendingActions(Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
		
		authService.confirmation(user, userInfo, validToken);
		
		verify(userDao).removeEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class));
		verify(userService).removePendingActions(validUserId, Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptNull() throws Exception {
		authService.accept(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void acceptInvalid() throws Exception {
		authService.accept(createInvalidUser());
	}
	
	@Test
	public void accept() throws Exception {
		User user = createValidUser();
		List<String> overrideRoles = new ArrayList<String>();
		overrideRoles.add(OverrideRoleNames.UNACCEPTED);
		user.setOverrideRoles(overrideRoles);
		
		authService.accept(user);
		
		verify(userService).removeOverrideRole(validUserId, OverrideRoleNames.UNACCEPTED);
	}
	
	@Test(expected = BadParameterException.class)
	public void sendPaymentIdNull() throws Exception {
		authService.sendPaymentIdChange(null);
	}
	
	@Test(expected = BadParameterException.class)
	public void sendPaymentIdInvalid() throws Exception {
		authService.sendPaymentIdChange(createInvalidUser());
	}
	
	@Test
	public void sendPaymentId() throws Exception {
		authService.sendPaymentIdChange(createValidUser());
		
		verify(emailService).changePaymentId(anyString(), anyString());
	}

	@Test(expected = BadParameterException.class)
	public void changePaymentIdNull1() throws Exception {
		ChangePaymentDTO dto = new ChangePaymentDTO();
		dto.setPassword(validHashedPassword);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changePaymentId(null, dto, validToken);
	}

	@Test(expected = BadParameterException.class)
	public void changePaymentIdNull2() throws Exception {
		User user = createValidUser();
		user.setPassword(validHashedPassword);
		ChangePaymentDTO dto = new ChangePaymentDTO();
		dto.setPassword(validHashedPassword);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changePaymentId(user, null, validToken);
	}

	@Test(expected = BadParameterException.class)
	public void changePaymentIdNull3() throws Exception {
		User user = createValidUser();
		user.setPassword(validHashedPassword);
		ChangePaymentDTO dto = new ChangePaymentDTO();
		dto.setPassword(validHashedPassword);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changePaymentId(user, dto, null);
	}

	@Test(expected = BadParameterException.class)
	public void changePaymentIdInvalid1() throws Exception {
		ChangePaymentDTO dto = new ChangePaymentDTO();
		dto.setPassword(validHashedPassword);
		when(passwordEncoder.encode(any(CharSequence.class)))
			.thenReturn(validHashedPassword);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changePaymentId(createInvalidUser(), dto, validToken);
	}

	@Test(expected = ActionNotAllowedException.class)
	public void changePaymentIdNotAllowed2() throws Exception {
		User user = createValidUser();
		user.setPassword(validHashedPassword);
		ChangePaymentDTO dto = new ChangePaymentDTO();
		dto.setPassword(validOtherHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(false);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changePaymentId(user, dto, validToken);
	}

	@Test(expected = BadParameterException.class)
	public void changePaymentIdNotAllowed3() throws Exception {
		User user = createValidUser();
		user.setPassword(validHashedPassword);
		ChangePaymentDTO dto = new ChangePaymentDTO();
		dto.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(false);
		authService.changePaymentId(user, dto, validToken);
	}

	@Test
	public void changePaymentId() throws Exception {
		User user = createValidUser();
		user.setPassword(validHashedPassword);
		ChangePaymentDTO dto = new ChangePaymentDTO();
		dto.setPassword(validHashedPassword);
		when(passwordEncoder.matches(any(CharSequence.class), any(String.class)))
			.thenReturn(true);
		when(userDao.hasEmailToken(any(ObjectId.class), anyString(), 
				any(EMAIL_TYPE.class))).thenReturn(true);
		authService.changePaymentId(user, dto, validToken);
		
		verify(userDao).updatePayment(any(ObjectId.class), anyString());
	}
}
