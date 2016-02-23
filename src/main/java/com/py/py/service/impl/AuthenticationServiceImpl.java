package com.py.py.service.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Hex;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AccountStatusException;
import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.rememberme.RememberMeAuthenticationException;
import org.springframework.util.Assert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.DuplicateKeyException;
import com.py.py.constants.DomainRegex;
import com.py.py.constants.HttpContentTypes;
import com.py.py.constants.OverrideRoleNames;
import com.py.py.constants.PendingActions;
import com.py.py.constants.RoleNames;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.UserDao;
import com.py.py.dao.exception.CollisionException;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.constants.SchemaUpdates;
import com.py.py.domain.enumeration.EMAIL_TYPE;
import com.py.py.domain.subdomain.AuthenticationInformation;
import com.py.py.domain.subdomain.LoginAttempt;
import com.py.py.domain.subdomain.Permission;
import com.py.py.dto.APIResponse;
import com.py.py.dto.in.ChangeEmailDTO;
import com.py.py.dto.in.ChangePasswordDTO;
import com.py.py.dto.in.ChangePasswordUnauthedDTO;
import com.py.py.dto.in.ChangePaymentDTO;
import com.py.py.dto.in.RegisterUserDTO;
import com.py.py.dto.out.BalanceDTO;
import com.py.py.dto.out.CurrentUserDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.security.AuthenticationRepositoryImpl;
import com.py.py.security.UserAuthenticationToken;
import com.py.py.security.exception.AuthenticationExpiredException;
import com.py.py.security.exception.AuthenticationTheftException;
import com.py.py.security.exception.InvalidHeaderException;
import com.py.py.service.AuthenticationService;
import com.py.py.service.EmailService;
import com.py.py.service.EventService;
import com.py.py.service.FollowService;
import com.py.py.service.MessageService;
import com.py.py.service.RestrictedService;
import com.py.py.service.RoleService;
import com.py.py.service.UserService;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.AuthenticationException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.EmailExistsException;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.ExternalServiceException;
import com.py.py.service.exception.LoginLockedException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.RestrictedException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.exception.UsernameExistsException;
import com.py.py.service.exception.constants.ExceptionMessages;
import com.py.py.service.google.GoogleManager;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.DefaultsFactory;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ModelFactory;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;
import com.py.py.util.Tuples;

public class AuthenticationServiceImpl implements AuthenticationService {

	protected static final PyLogger logger = PyLogger.getLogger(AuthenticationServiceImpl.class);
  public static final int DEFAULT_TOKEN_LENGTH = 16;
	protected static final String DELIMITER = ":";
  protected SecureRandom random;
	
  protected int tokenLength = DEFAULT_TOKEN_LENGTH;
	protected String securityTokenHeader = "Authentication-Token";

  protected GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();
  protected AuthenticationDetailsSource<HttpServletRequest, ?> 
  	authenticationDetailsSource = new WebAuthenticationDetailsSource();

	protected String key;
  protected UserDetailsChecker userDetailsChecker = new AccountStatusUserDetailsChecker();
	protected AuthenticationRepositoryImpl repository = new AuthenticationRepositoryImpl();
	
	//@Autowired
	//private AuthenticationManager authenticationManager;
	
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
	
	@Autowired
	private EmailService emailService;
	
	@Autowired
	private RestrictedService restrictedService;
	
	@Autowired
	private DefaultsFactory defaultsFactory;
	
	@Autowired
	private GoogleManager googleManager;

	public AuthenticationServiceImpl(String key, 
			AuthenticationRepositoryImpl tokenRepository) {
		this.key = key;
		this.repository = tokenRepository;
		this.random = new SecureRandom();
	}
	
	/*
	@Override
	@Caching(evict = {@CacheEvict(value = CacheNames.USER_ID_USERNAME, key = "#p0?.getUsername()"),
		@CacheEvict(value = CacheNames.USER_ID_EMAIL, key = "#p0?.getEmail()")})
	*/
	@Override
	public ResultSuccessDTO registerUser(RegisterUserDTO userdto, String language, String address, boolean requireRecaptcha) throws ServiceException {
		ArgCheck.nullCheck(userdto);
		User user = Mapper.mapUser(userdto);
		
		String correctLanguage = ServiceUtils.getLanguage(language);
		
		/*
		// redundant, user will simply not be created
		try {
			if(userDao.findByUniqueName(
					ServiceUtils.getIdName(userdto.getUsername())) != null) {
				throw new ExistsException(userdto.getUsername());
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}*/
		
		if(restrictedService.isRestricted(user.getUsername(), RESTRICTED_TYPE.USERNAME)) {
			throw new RestrictedException(RESTRICTED_TYPE.USERNAME);
		}
		if(restrictedService.isRestricted(user.getPassword(), RESTRICTED_TYPE.PASSWORD)) {
			throw new RestrictedException(RESTRICTED_TYPE.PASSWORD);
		}
		if(restrictedService.isRestricted(user.getEmail(), RESTRICTED_TYPE.EMAIL)) {
			throw new RestrictedException(RESTRICTED_TYPE.EMAIL);
		}
		
		user.setPassword(encodePassword(user.getPassword()));
		user.addRoles(roleService.getDefaultRoles());
		user.addOverrideRoles(roleService.getDefaultOverrideRoles());

		Date date = new Date();
		user.setCreated(date);
		user.setLastModified(date);
		
		try {
			User u = userDao.findByUniqueName(user.getUniqueName());
			if(u != null) {
				throw new UsernameExistsException(userdto.getUsername());
			}
		} catch(DaoException de) {
			// continue
		}
		try {
			User u = userDao.findByEmail(user.getEmail());
			if(u != null) {
				throw new EmailExistsException(userdto.getEmail());
			}
		} catch(DaoException de) {
			// continue
		}
		
		try {
			if(requireRecaptcha && !googleManager.verifyRecaptchaResponse(userdto.getRecaptchaResponse(), null)) {
				throw new ActionNotAllowedException();
			}
		} catch(ActionNotAllowedException anae) {
			throw anae;
		} catch(BadParameterException bpe) {
			throw new ActionNotAllowedException();
		} catch(ExternalServiceException ese) {
			throw ese;
		} catch(ServiceException e) {
			throw new ExternalServiceException();
		}
		
		try {
			User created = userDao.create(user);
			
			userService.createUserInfo(created.getId(), created.getUsername(), 
					correctLanguage);
			followService.create(created);
			emailService.confirmation(created.getEmail(), created.getUsername());
			
			Boolean rememberMe = false;
			String location = null;
			if(rememberMe != null && rememberMe && address != null && !address.isEmpty()) {
				location = address;
			}
			long inactivity = generateInactivity(rememberMe);
			String encodedToken = loginSuccess(created, generateTokenData(), inactivity, location);
			
			logger.info("Username (" + created.getUsername() + ") created with id {" 
					+ created.getId().toHexString() + "}.");
            return Mapper.mapResultSuccessDTO(encodedToken);
		} catch(CollisionException ce) {
			throw new ExistsException(userdto.getUsername());
		} catch(DuplicateKeyException dke) {
			throw new ExistsException(userdto.getUsername());
		} catch(NotFoundException nfe) {
			// this should extremely rarely ever happen, 
			// but will probably take the username. very bad!
			logger.warn("Create user failed and username now taken for username '" + user.getUsername() + "'.", nfe);
			throw new ExistsException(userdto.getEmail());
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public Date getLastLogin(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		List<LoginAttempt> attempts = user.getLoginAttempts();
		Date lastSuccess = null;
		if(attempts != null) {
			for(LoginAttempt attempt : attempts) {
				// may have more than one most successful attempt. Must find most recent
				if(attempt.isSuccess()) {
					Date time = attempt.getTime();
					if(lastSuccess == null) {
						lastSuccess = new Date();
						lastSuccess.setTime(time.getTime());
					} else {
						if(lastSuccess.before(time)) {
							lastSuccess.setTime(time.getTime());
						}
					}
				}
			}
		}
		if(lastSuccess == null) {
			lastSuccess = new Date();
		}
		return lastSuccess;
	}
	
	/*
	@Override
	@Cacheable(value = CacheNames.USER_CURRENT, key = "#p0?.getId()")
	*/
	@Override
	public CurrentUserDTO getCurrentUserDTO(User user, UserInfo userInfo) 
			throws ServiceException {
		ArgCheck.nullCheck(userInfo);
		ArgCheck.userCheck(user);
		
		Date lastCheckedNotifications = userInfo.getLastCheckedNotifications();
		Date lastCheckedFeed = userInfo.getLastCheckedFeed();
		Date lastLogin = null;
		if(lastCheckedNotifications == null) {
			if(lastLogin == null) {
				lastLogin = getLastLogin(user);
			}
			lastCheckedNotifications = lastLogin;
		}
		if(lastCheckedFeed == null) {
			if(lastLogin == null) {
				lastLogin = getLastLogin(user);
			}
			lastCheckedFeed = lastLogin;
		}

		List<EVENT_TYPE> notificationList = new ArrayList<EVENT_TYPE>();
		if(userInfo.getSettings() == null) {
			notificationList =  defaultsFactory.getNotificationsEventsList();
		} else {
			List<String> hiddenNotifications = 
					userInfo.getSettings().getHiddenNotifications();
			if(hiddenNotifications != null && !hiddenNotifications.isEmpty()) {
				for(EVENT_TYPE s: defaultsFactory.getNotificationsEventsList()) {
					if(!hiddenNotifications.contains(s.toString())) {
						notificationList.add(s);
					}
				}
			} else {
				notificationList = defaultsFactory.getNotificationsEventsList();
			}
		}
		
		List<ObjectId> feedIds = followService.getFolloweeIds(user.getId());
		List<EVENT_TYPE> feedTypes = followService.getHiddenFeedEvents(user.getId());
		
		long notificationCount = eventService.getNotificationCount(user.getId(), 
				notificationList, lastCheckedNotifications);
		long feedCount = eventService.getFeedCount(user.getId(), feedIds, 
				feedTypes, lastCheckedFeed);
		long messageCount = messageService.getMessageCount(user, false);
		long loginFailures = getLoginFailureCount(user);
		
		return Mapper.mapCurrentUserDTO(userInfo, user, notificationCount, 
				feedCount, messageCount, loginFailures);
	}
	
	@Override
	public BalanceDTO getBalanceDTO(UserInfo userInfo) throws ServiceException {
		ArgCheck.nullCheck(userInfo);
		return Mapper.mapBalanceDTO(userInfo.getBalance());
	}
	
	@Override
	public UserDetails loadUser(User user, boolean credentialsNonExpired) throws UsernameNotFoundException {
		try {
			ArgCheck.userCheck(user);
			
			List<Permission> authorities = ModelFactory.<Permission>constructList();
			// overrides the user roles if they have any override roles, allowing only
			// the permissions given by the override roles until removed
			List<String> roles = user.getRoles();
			List<String> overrideRoles = user.getOverrideRoles();
			if(overrideRoles == null) {
				overrideRoles = new ArrayList<String>();
			}
			List<Permission> permissions = roleService.getPermissionsFromRoleNames(
					roles, overrideRoles);
			if(permissions != null) {
				authorities.addAll(permissions);
			}
			//return Mapper.mapUserDetails(user, authorities, expiry);
			return Mapper.mapUserDetails(user, authorities, credentialsNonExpired);
		} catch(ServiceException se) {
			String username = "{unknown}";
			if(user != null && user.getUsername() != null) {
				username = user.getUsername();
			}
      throw new UsernameNotFoundException(String.format(ExceptionMessages.NOTFOUND, username));
		}
	}

	@Override
	public UserDetails loadUserByUsername(String username) 
			throws UsernameNotFoundException {
		if(username == null) {
			throw new UsernameNotFoundException(String.format(ExceptionMessages.NOTFOUND, username));
		}
		User user = null;
		try {
			user = userService.findUserByUsername(username);
		} catch(ServiceException se) {
			throw new UsernameNotFoundException(
					String.format(ExceptionMessages.NOTFOUND, username)); 
		}
		if(user == null) {
			throw new UsernameNotFoundException(
					String.format(ExceptionMessages.NOTFOUND, username));
		}
		try {
			return loadUser(user, true);
		} catch(UsernameNotFoundException se) {
			throw new UsernameNotFoundException(
					String.format(ExceptionMessages.NOTFOUND, username));
		}
	}

	@Override
	public String encodePassword(String raw) throws ServiceException {
		ArgCheck.nullCheck(raw);
		String cryptedPassword = null;
		try {
			cryptedPassword = passwordEncoder.encode(prehashPassword(raw));
		} catch(Exception e) {
			throw new AuthenticationException();
		}
		if(cryptedPassword == null) {
			throw new AuthenticationException();
		}
		return cryptedPassword;
	}
	/*
	protected Location constructLocation(String address, List<Location> savedLocations) {
		String name = null;
		if(savedLocations != null) {
			for(Location l : savedLocations) {
				if(PyUtils.stringCompare(l.getIp(), address)) {
					name = l.getName();
				}
			}
		}
		
		Location location = new Location();
		location.setIp(address);
		location.setName(name);
		return location;
	}
	*/
	
	@Override
	public void recordLoginAttempt(User user, String address, boolean success) 
			throws ServiceException {
		ArgCheck.nullCheck(address);
		ArgCheck.userCheck(user);
		
		Date now = new Date();
		Date then = new Date();
		then.setTime(then.getTime() - ServiceValues.LOGIN_ATTEMPT_TIME_THRESHOLD);
		
		//Location location = constructLocation(address, user.getSavedLocations());
		
		LoginAttempt attempt = new LoginAttempt();
		attempt.setSuccess(success);
		attempt.setTime(now);
		attempt.setLocation(address);
		
		ObjectId id = user.getId();
		
		try {
			// NOT immune to race conditions, but errors should only happen in case of 
			// simultaneous successful logins, and won't break
			if(success) {
				// remove all previous attempts
				userDao.clearLoginAttempts(id, now, null);
				userDao.addLoginAttempt(id, attempt);
			} else {
				// remove all irrelevant failures
				userDao.clearLoginAttempts(id, then, false);
				userDao.addLoginAttempt(id, attempt);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
	}
	
	@Override
	public long getLoginFailureCount(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		long count = 0;
		
		if(user.getLoginAttempts() != null) {
			for(LoginAttempt attempt : user.getLoginAttempts()) {
				if(!attempt.isSuccess()) {
					count++;
				}
			}
		}
		
		return count;
	}
	
	@Override
	public boolean isLoginLocked(User user, String address) throws ServiceException {
		ArgCheck.userCheck(user);
		
		int failedAttempts = 0;
		Date then = new Date();
		then.setTime(then.getTime() - ServiceValues.LOGIN_ATTEMPT_TIME_THRESHOLD);

		boolean saved = false;
		List<String> savedLocations = new ArrayList<String>();
		if(user.getSavedLocations() != null) {
			savedLocations = user.getSavedLocations();
			if(savedLocations.contains(address)) {
				saved = true;
			}
		}
		
		//Location currentLocation = constructLocation(address, user.getSavedLocations());
		//String locationName = currentLocation.getName();
		
		if(user.getLoginAttempts() != null) {
			for(LoginAttempt login : user.getLoginAttempts()) {
				//Location loginLocation = login.getLocation();
				String location = login.getLocation();
				if(!login.isSuccess() && login.getTime().after(then)) {
					if(saved) {
						if(PyUtils.stringCompare(address, location)) {
							failedAttempts++;
						}
					} else if(!savedLocations.contains(location)) {
						failedAttempts++;
					}
				}
			}
		}
		
		if(failedAttempts >= ServiceValues.USER_LOGIN_ATTEMPTS_MAX) {
			return true;
		}
		return false;
	}
	
	public Authentication authenticate(HttpServletRequest request, 
			HttpServletResponse response) {
    String authenticationToken = request.getHeader(securityTokenHeader);
    
    if (authenticationToken == null || authenticationToken.isEmpty()) {
      return null;
    }

    User user = null;
    UserDetails userDetails = null;
    ObjectId userId = null;

    try {
    	ObjectId id = decodeHeaderId(authenticationToken);
      String token = decodeHeaderToken(authenticationToken);
      Tuples.Pair<User, AuthenticationInformation> pair = processLoginAuthenticationHeader(id, token, request, response);
      user = pair.x;
      AuthenticationInformation authInfo = pair.y;
      if(user == null || authInfo == null) {
        throw new AuthenticationTheftException("FOUND TOKEN BUT IT DISSAPEARED : " + token);
      }
      
      Date now = new Date();
      Date expiry = authInfo.getExpiry();
      boolean credentialsNonExpired = false;
      if(expiry != null && now.before(expiry)) {
      	credentialsNonExpired = true;
      }
      userDetails = loadUser(user, credentialsNonExpired);
      userDetailsChecker.check(userDetails);

      userId = user.getId();
      if(userId == null) {
      	throw new UsernameNotFoundException(token);
      }
      byte[] encodedToken = encodeToken(token);
      completeAction(user, encodedToken, authInfo);
      long inactivity = authInfo.getInactivity();
      return createSuccessfulAuthentication(request, user, token, inactivity, userDetails);
    } catch (AuthenticationTheftException cte) {
    	// TODO: add exception handling
      throw cte;
    } catch (UsernameNotFoundException noUser) {
      logger.debug("Corresponding user not found.", noUser);
      throw noUser;
    } catch (InvalidHeaderException invalidHeader) {
      logger.debug("Invalid token header: " + invalidHeader.getMessage());
      throw invalidHeader;
    } catch (LockedException lee) {
    	logger.debug("Locked user: " + lee.getMessage());
    	throw lee;
    } catch (CredentialsExpiredException cee) {
    	logger.debug("Credentials expired: " + cee.getMessage());
    	throw new AuthenticationExpiredException("Credentials expired: "
    			+ cee.getMessage(), cee);
    } catch (AccountStatusException statusInvalid) {
      logger.debug("Invalid UserDetails: " + statusInvalid.getMessage());
      throw statusInvalid;
    } catch (RememberMeAuthenticationException e) {
      logger.debug(e.getMessage());
    } catch (DaoException de) {
    	logger.debug("Failed to update last action for user: " + user.getUsername(), de);
    } catch (BadParameterException bpe) {
    	logger.debug("Failed to encode token for last action for user: " + user.getUsername(), bpe);
    }
	  return null;
	}
	
	protected void completeAction(User user, byte[] token, AuthenticationInformation authInfo) throws DaoException {
		if(authInfo != null) {
			Date now = new Date();
			Date newExpiry = new Date(now.getTime() + authInfo.getInactivity());
			Date threshold = new Date(newExpiry.getTime() - ServiceValues.AUTHENTICATION_LAST_ACTION_UPDATE_THRESHOLD);
			if(threshold.after(authInfo.getExpiry())) {
				repository.updateToken(user.getId(), token, newExpiry);
				return;
			}
		}
	}
	
	protected void action(User user, byte[] token) throws DaoException {
		try {
			completeAction(user, token, findAuthenticationInformation(user, token));
		} catch(BadParameterException bpe) {
			throw new DaoException();
		}
	}
	
	protected AuthenticationInformation findAuthenticationInformation(User user, byte[] token) throws BadParameterException {
		ArgCheck.nullCheck(user, token);
		List<AuthenticationInformation> list = user.getAuthenticationInformation();
		if(list != null) {
			for(AuthenticationInformation aI : list) {
				if(compareEncodedTokens(aI.getToken(), token)) {
					return aI;
				}
			}
		}
		return null;
	}
	
	/*
	protected User updateAction(User user, Date now) throws DaoException {
		if(user == null) {
			throw new DaoException();
		}
		userDao.addAction(user.getId(), now);
		user.setLastAction(now);
		return user;
	}
	*/
	
	protected ObjectId decodeHeaderId(String header) throws InvalidHeaderException {
		try {
			String[] tokens = header.split(DomainRegex.TOKEN_DELIMITER);
			return new ObjectId(decodeHeader(tokens[0]));
		} catch(Exception e) {
			throw new InvalidHeaderException(
					"Authentication header token was not correctly split!");
		}
	}
	
	protected String decodeHeaderToken(String header) throws InvalidHeaderException {
		try {
			String[] tokens = header.split(DomainRegex.TOKEN_DELIMITER);
			return decodeHeader(tokens[1]);
		} catch(Exception e) {
			throw new InvalidHeaderException(
					"Authentication header token was not correctly split!");
		}
	}
	
	protected String decodeHeader(String headerValue) throws InvalidHeaderException {
        for (int j = 0; j < headerValue.length() % 4; j++) {
        	headerValue = headerValue + "=";
        }

        if (!Base64.isBase64(headerValue.getBytes())) {
            throw new InvalidHeaderException(
            		"Authentication header token was not Base64 encoded; value was '"
            				+ headerValue + "'");
        }

        String headerAsPlainText = new String(Base64.decode(headerValue.getBytes()));

        return headerAsPlainText;
    }
	
	protected String createEncodedHeader(ObjectId id, String token) {
		return encodeHeader(id.toHexString())
				+ DomainRegex.TOKEN_DELIMITER_STRING
				+ encodeHeader(token);
	}

  protected String encodeHeader(String headerToken) {
    StringBuilder sb = new StringBuilder(
    		new String(Base64.encode(headerToken.getBytes())));

    while (sb.charAt(sb.length() - 1) == '=') {
        sb.deleteCharAt(sb.length() - 1);
    }

    return sb.toString();
  }

	protected Tuples.Pair<User, AuthenticationInformation> processLoginAuthenticationHeader(ObjectId id, String token,
			HttpServletRequest request, HttpServletResponse response) {
		try {
			// user service supports caching
      User user = userService.findUser(id);
      
      if(token != null && user != null) {
      	AuthenticationInformation authInfo = findAuthenticationInformation(user, encodeToken(token));
      	if(authInfo != null) {
      		return new Tuples.Pair<User, AuthenticationInformation>(user, authInfo);
      	}
      }
      // No series match, so we can't authenticate using this cookie
      throw new AuthenticationTheftException("No persistent token found for : " + token);
		} catch(NotFoundException nfe) {
      throw new UsernameNotFoundException("No persistent token found for : " + token);
		} catch(ServiceException se) {
      throw new UsernameNotFoundException("No persistent token found for : " + token);
		}
  }

  protected Authentication createSuccessfulAuthentication(HttpServletRequest request, 
  		User user, String unencodedToken, long inactivity, UserDetails userDetails) {
    AbstractAuthenticationToken auth = new UserAuthenticationToken(user, 
    		userDetails.getUsername(), userDetails.getPassword(), unencodedToken, inactivity, null, 
            authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));
    auth.setDetails(authenticationDetailsSource.buildDetails(request));
    return auth;
  }
  
  @Override
  public long generateInactivity(Boolean rememberMe) {
		long inactivity = ServiceValues.AUTHENTICATION_INACTIVITY_DEFAULT;
		if(rememberMe != null && rememberMe) {
			inactivity = ServiceValues.AUTHENTICATION_INACTIVITY_REMEMBER_ME;
		}
		return inactivity;
  }
   
  @Override
  public String generateTokenData() {
    byte[] newToken = new byte[tokenLength];
    random.nextBytes(newToken);
    return new String(Base64.encode(newToken));
  }
	
  public void setUserDetailsChecker(UserDetailsChecker userDetailsChecker) {
    this.userDetailsChecker = userDetailsChecker;
  }

	@Override
	public void logout(HttpServletRequest request,
			HttpServletResponse response, Authentication authentication) {
		if(authentication != null) {
			UserAuthenticationToken uat = (UserAuthenticationToken)authentication;
			User user = uat.getUser();
			String unencodedToken = uat.getUnencodedToken();
			if(user != null) {
				try {
					repository.removeUserToken(user.getId(), encodeToken(unencodedToken));
				} catch(BadParameterException bpe) {
					// do nothing, but this really should never happen
					logger.warn("Error encoding token on logout for user {" + user.getUsername() + "}.", bpe);
				} catch(DaoException de) {
					// do nothing
				}
			}
		}
	}

	@Override
	public void afterPropertiesSet() throws Exception {
        Assert.hasLength(key);
	}

	public UserDetailsChecker getUserDetailsChecker() {
		return userDetailsChecker;
	}

	public void setTokenLength(int tokenLength) {
		this.tokenLength = tokenLength;
	}

	public void setSecurityTokenHeader(String securityTokenHeader) {
		this.securityTokenHeader = securityTokenHeader;
	}

	public void setAuthoritiesMapper(GrantedAuthoritiesMapper authoritiesMapper) {
		this.authoritiesMapper = authoritiesMapper;
	}

	public void setAuthenticationDetailsSource(
			AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource) {
		this.authenticationDetailsSource = authenticationDetailsSource;
	}
	
	protected byte[] encodeToken(String token) throws BadParameterException {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return digest.digest(token.getBytes("UTF-8"));
		} catch(Exception e) {
			throw new BadParameterException();
		}
	}
	
	protected boolean compareEncodedTokens(byte[] a, byte[] b) {
		try {
			return Arrays.equals(a, b);
		} catch(Exception e) {
			return false;
		}
	}
	
	protected String loginSuccess(User user, String unencodedToken, long inactivity, String location) throws DaoException {
		if(user == null || user.getId() == null || unencodedToken == null || unencodedToken.isEmpty()) {
			throw new DaoException();
		}
		
		Date expiry = new Date((new Date()).getTime() + inactivity);
		
		try {
			repository.addToken(user.getId(), encodeToken(unencodedToken), expiry, inactivity);
		} catch(BadParameterException bpe) {
			throw new DaoException(bpe);
		}
		
		if(location != null && !location.isEmpty()) {
			try {
				userDao.addLocation(user.getId(), location);
			} catch(Exception e) {
				logger.warn("Error recording saved location for user {" + user.getUsername() + "}.", e);
			}
		}

    return createEncodedHeader(user.getId(), unencodedToken);
	}
	
	@Override
	public void onLoginSuccess(HttpServletRequest request, HttpServletResponse response, 
			User user, String unencodedToken, long inactivity, boolean rememberMe) throws ServiceException {
		ArgCheck.nullCheck(request, response, user, unencodedToken);
		boolean hasException = false;
		try {
			String address = null;
			if(rememberMe) {
				try {
					address = ServiceUtils.getClientAddress(request);
				} catch(BadParameterException bpe) {
					// continue. successful login, but wont be remembered
				}
			}
			String token = loginSuccess(user, unencodedToken, inactivity, address);
			
			ResultSuccessDTO dto = Mapper.mapResultSuccessDTO(token);
			APIResponse<ResultSuccessDTO> responseDTO = 
					new APIResponse<ResultSuccessDTO>(dto);
      // if support for other types is added, change this to request accept type
      response.setContentType(HttpContentTypes.CURRENT_TYPE);
      if(HttpContentTypes.CURRENT_TYPE == HttpContentTypes.JSON) {
      	PrintWriter writer = response.getWriter();
      	ObjectMapper mapper = new ObjectMapper();
      	mapper.writeValue(writer, responseDTO);
      }
		} catch (IOException ioe) {
			logger.debug(ioe);
			hasException = true;
		} catch (BadParameterException bpe) {
			logger.debug(bpe);
			hasException = true;
		} catch (DataAccessException dae) {
			logger.debug(dae);
			hasException = true;
		} catch (DaoException de) {
			logger.debug(de);
			hasException = true;
		}
		if(hasException) {
			throw new ServiceException();
		}
	}
	
	@Override
	public void changeEmailRequest(User user) 
			throws ServiceException {
		ArgCheck.userCheck(user);

		try {
			emailService.changeEmail(user.getEmail(), user.getUsername());
		} catch(NotFoundException nfe) {
			// do nothing!
		}
	}
	
	@Override
	public void changeEmail(User user, UserInfo userInfo, ChangeEmailDTO dto, String emailToken) 
			throws ServiceException {
		ArgCheck.nullCheck(dto, userInfo, emailToken);
		ArgCheck.userCheck(user);

		String validEmail = ServiceUtils.getEmail(dto.getEmail());
		String validEmailToken = hashToken(emailToken);
		
		if(restrictedService.isRestricted(validEmail, RESTRICTED_TYPE.EMAIL)) {
			throw new RestrictedException(RESTRICTED_TYPE.EMAIL);
		}
		if(PyUtils.stringCompare(ServiceUtils.getEmail(user.getEmail()), validEmail)) {
			throw new ExistsException(validEmail);
		}
		
		ObjectId id = user.getId();
		
		boolean hasEmailToken = false;
		try {
			hasEmailToken = userDao.hasEmailToken(id, validEmailToken, EMAIL_TYPE.CHANGE);
			passwordAttempt(user, dto.getPassword());
			if(hasEmailToken) {
				userDao.removeEmailToken(id, validEmailToken, EMAIL_TYPE.CHANGE);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		if(hasEmailToken) {
			try {
				userDao.updateUser(id, validEmail, null);
			} catch(CollisionException ee) {
				throw new EmailExistsException(validEmail);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
			
			//userService.addOverrideRole(id, OverrideRoleNames.UNCONFIRMED);
			// the user is not retrieved from database after changing the roles
			//user.getOverrideRoles().add(OverrideRoleNames.UNCONFIRMED);
			try {
				List<String> add = new ArrayList<String>();
				add.add(PendingActions.UNCONFIRMED_EMAIL);
				userService.addPendingActions(userInfo.getId(), add);
				List<String> c = userInfo.getPendingActions();
				if(c == null) {
					userInfo.setPendingActions(add);
				} else {
					userInfo.getPendingActions().add(PendingActions.UNCONFIRMED_EMAIL);
				}
				sendConfirmation(user, userInfo);
			} catch(ServiceException se) {
				logger.info("Error sending confirmation and adding pending action after email change to user (" + user.getUsername() + ").", se);
			}
		} else {
			throw new BadParameterException();
		}
	}
	
	@Override
	public void changeEmailPendingAction(User user, UserInfo userInfo, ChangeEmailDTO dto) throws ServiceException {
		ArgCheck.nullCheck(userInfo, dto);
		ArgCheck.userCheck(user);

		String validEmail = ServiceUtils.getEmail(dto.getEmail());
		
		if(restrictedService.isRestricted(dto.getEmail(), RESTRICTED_TYPE.EMAIL)) {
			throw new RestrictedException(RESTRICTED_TYPE.EMAIL);
		}
		
		if(PyUtils.stringCompare(ServiceUtils.getEmail(user.getEmail()), validEmail)) {
			throw new ExistsException(validEmail);
		}
		
		passwordAttempt(user, dto.getPassword());
		
		ObjectId id = user.getId();
		
		if(!isConfirmed(userInfo)) {
			try {
				userDao.updateUser(id, validEmail, null);
			} catch(CollisionException ee) {
				throw new EmailExistsException(validEmail);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
			//userService.addOverrideRole(id, OverrideRoleNames.UNCONFIRMED);
			// the user is not retrieved from database after changing the roles
			//user.getOverrideRoles().add(OverrideRoleNames.UNCONFIRMED);
			sendConfirmation(user, userInfo);
			
			try {
				userService.addPendingActions(id, Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
			} catch(ServiceException se) {
				throw se;
			}
		} else {
			throw new BadParameterException();
		}
	}
	
	@Override
	public void changePasswordUnauthed(User user, ChangePasswordUnauthedDTO dto, 
			String emailToken) throws ServiceException {
		ArgCheck.nullCheck(dto, emailToken);
		ArgCheck.userCheck(user);
		
		if(emailToken == null || emailToken.isEmpty()) {
			throw new BadParameterException();
		}
		String validEmailToken = hashToken(emailToken);
		if(restrictedService.isRestricted(dto.getNewPassword(), RESTRICTED_TYPE.PASSWORD)) {
			throw new RestrictedException(RESTRICTED_TYPE.PASSWORD);
		}
		
		ObjectId id = user.getId();
		
		boolean hasEmailToken = false;
		
		try {
			hasEmailToken = userDao.hasEmailToken(id, validEmailToken, EMAIL_TYPE.RESET);
			if(hasEmailToken) {
				userDao.removeEmailToken(id, validEmailToken, EMAIL_TYPE.RESET);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		if(hasEmailToken) {
			changeUserPassword(user, dto.getNewPassword(), dto.getConfirmNewPassword());
			try {
				repository.removeAllUserTokens(id);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
		} else {
			throw new BadParameterException();
		}
	}
	
	@Override
	public void changePassword(User user, ChangePasswordDTO dto) throws ServiceException {
		ArgCheck.nullCheck(dto);
		ArgCheck.userCheck(user);

		if(restrictedService.isRestricted(dto.getNewPassword(), RESTRICTED_TYPE.PASSWORD)) {
			throw new RestrictedException(RESTRICTED_TYPE.PASSWORD);
		}
		
		passwordAttempt(user, dto.getOldPassword());
		
		changeUserPassword(user, dto.getNewPassword(), dto.getConfirmNewPassword());
	}
	
	@Override
	public void changeUserPassword(User user, String newPassword, 
			String confirmNewPassword) throws ServiceException {
		ArgCheck.nullCheck(newPassword, confirmNewPassword);
		ArgCheck.userCheck(user);
		
		if(!PyUtils.stringCompare(newPassword, confirmNewPassword)) {
			throw new BadParameterException();
		}
		
		ObjectId id = user.getId();

		String encodedChangedPassword = encodePassword(newPassword);
		
	    boolean needsBcryptUpdate = user.hasPendingSchemaUpdate(SchemaUpdates.USER_PASSWORD_BCRYPT);
	    
	    if(needsBcryptUpdate) {
	    	try {
	    		userService.doPendingSchemaUpdatePassword(user.getId(), encodedChangedPassword, SchemaUpdates.USER_PASSWORD_BCRYPT);
	    	} catch(Exception e) {
	    		logger.info("Cannot convert user {" + user.getUsername() + "} for USER_PASSWORD_BCRYPT schema update!", e);
	    	}
	    } else {
			try {
				userDao.updateUser(id, null, encodedChangedPassword);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
	    }
	}
	
	@Override
	public void resetPassword(User user) throws ServiceException {
		try {
			ArgCheck.userCheck(user);
			if(user != null) {
				emailService.resetPassword(user.getEmail(), user.getUsername());
			}
		} catch(Exception e) {
			// do not throw error here, silently do not send email
			// TODO: possibly send email stating an account is not registered for this email 
			// for case where user is not sure which email they used, but has access 
			// to specified email
			// user should not be able to determine if an email is in use if 
			// they do not own it
		}
	}
	
	@Override
	public void sendConfirmation(User user, UserInfo userInfo) throws ServiceException {
		ArgCheck.userCheck(user);
		ArgCheck.nullCheck(userInfo);
		
		if(!isConfirmed(userInfo)) {
			emailService.confirmation(user.getEmail(), userInfo.getUsername());
		} else {
			throw new ActionNotAllowedException();
		}
	}
	
	@Override
	public void adminCheck(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		if(!isAdmin(user)) {
			throw new ActionNotAllowedException();
		}
	}
	
	protected boolean isAdmin(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		if(user.getRoles() != null && user.getRoles().contains(RoleNames.ADMIN)){
			return true;
		}
		return false;
	}
	
	
	protected boolean isConfirmed(UserInfo userInfo) throws ServiceException {
		ArgCheck.nullCheck(userInfo);
		
		if(userInfo.getPendingActions() != null && userInfo.getPendingActions().contains(PendingActions.UNCONFIRMED_EMAIL)) {
			return false;
		}
		return true;
	}
	
	@Override
	public void confirmation(User user, UserInfo userInfo, String emailToken) 
			throws ServiceException {
		ArgCheck.nullCheck(userInfo, emailToken);
		ArgCheck.userCheck(user);
		// should check if confirmed already
		
		ObjectId id = user.getId();
		String validEmailToken = hashToken(emailToken);
		
		boolean hasEmailToken = false;
		try {
			hasEmailToken = userDao.hasEmailToken(id, validEmailToken, EMAIL_TYPE.CONFIRMATION);
			if(hasEmailToken) {
				userDao.removeEmailToken(id, validEmailToken, EMAIL_TYPE.CONFIRMATION);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		if(hasEmailToken) {
			if(isConfirmed(userInfo)) {
				return;
			}
			//userService.removeOverrideRole(id, OverrideRoleNames.UNCONFIRMED);
			userService.removePendingActions(id, Arrays.asList(PendingActions.UNCONFIRMED_EMAIL));
		} else {
			throw new BadParameterException();
		}
	}

	
	@Override
	public void accept(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		userService.removeOverrideRole(user.getId(), OverrideRoleNames.UNACCEPTED);
	}
	
	@Override
	public void delete(User user, String emailToken) throws ServiceException {
		ArgCheck.nullCheck(emailToken);
		ArgCheck.userCheck(user);
		
		ObjectId id = user.getId();
		String validEmailToken = hashToken(emailToken);
		
		boolean hasEmailToken = false;
		try {
			hasEmailToken = userDao.hasEmailToken(id, validEmailToken, EMAIL_TYPE.DELETE);
			if(hasEmailToken) {
				userDao.removeEmailToken(id, validEmailToken, EMAIL_TYPE.DELETE);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		if(hasEmailToken) {
			if(isDeleted(user)) {
				throw new ActionNotAllowedException();
			}
			userService.addOverrideRole(user.getId(), OverrideRoleNames.DELETED);
			
			try {
				userDao.setDeleted(user.getId(), new Date());
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
			logger.info("User (" + user.getUsername() + ") with id {" 
					+ user.getId().toHexString() + "} has set themself as deleted!");
		} else {
			throw new BadParameterException();
		}
	}
	
	@Override
	public void undelete(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		if(user.getDeleted() == null) {
			throw new ActionNotAllowedException();
		}

		userService.removeOverrideRole(user.getId(), OverrideRoleNames.DELETED);
		
		try {
			userDao.setDeleted(user.getId(), null);
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		logger.info("User (" + user.getUsername() + ") with id {" 
				+ user.getId().toHexString() + "} has reverted their deletion!");
	}
	
	@Override
	public void sendDelete(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		if(!isDeleted(user)) {
			emailService.delete(user.getEmail(), user.getUsername());
		} else {
			throw new ActionNotAllowedException();
		}
	}
	
	protected boolean isDeleted(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		if(user.getDeleted() != null) {
			return true;
		}
		return false;
	}
	
	@Override
	public void sendPaymentIdChange(User user) throws ServiceException {
		ArgCheck.userCheck(user);
		
		emailService.changePaymentId(user.getEmail(), user.getUsername());
	}
	
	@Override
	public void changePaymentId(User user, ChangePaymentDTO dto, String emailToken) 
			throws ServiceException {
		ArgCheck.nullCheck(dto, emailToken);
		ArgCheck.userCheck(user);
		
		String validEmailToken = hashToken(emailToken);
		
		ObjectId id = user.getId();
		String paymentId = ServiceUtils.getPaymentId(dto.getPaymentId());
		
		boolean hasEmailToken = false;
		try {
			hasEmailToken = userDao.hasEmailToken(id, validEmailToken, EMAIL_TYPE.PAYMENT_CHANGE);
			passwordAttempt(user, dto.getPassword());
			if(hasEmailToken) {
				userDao.removeEmailToken(id, validEmailToken, EMAIL_TYPE.PAYMENT_CHANGE);
			}
		} catch(DaoException de) {
			throw new ServiceException(de);
		}
		
		if(hasEmailToken) {
			if(paymentId == null) {
				//userService.addOverrideRole(id, OverrideRoleNames.UNLINKED);
				userService.addPendingActions(id, Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
			} else {
				//userService.removeOverrideRole(id, OverrideRoleNames.UNLINKED);
				userService.removePendingActions(id, Arrays.asList(PendingActions.UNLINKED_PAYMENT_ID));
			}
			try {
				userDao.updatePayment(id, paymentId);
			} catch(DaoException de) {
				throw new ServiceException(de);
			}
		} else {
			throw new BadParameterException();
		}
	}
	/*
	protected boolean passwordMatch(User user, String password) throws ServiceException {
		ArgCheck.nullCheck(password);
		ArgCheck.userCheck(user);
		ArgCheck.nullCheck(user.getPassword());

    if(passwordEncoder.matches(prehashPassword(password), user.getPassword())) {
			return true;
		}
		return false;
	}
	*/
	
	protected void passwordAttempt(User user, String password) throws ServiceException {
		ArgCheck.userCheck(user);
		ArgCheck.nullCheck(password, user.getPassword());
		
		Date threshold = new Date((new Date()).getTime() - ServiceValues.PASSWORD_ATTEMPT_THROTTLE_THRESHOLD);
		if(user.getLastAttempt() != null && user.getPasswordFails() >= ServiceValues.PASSWORD_ATTEMPT_COUNT && user.getLastAttempt().after(threshold)) {
			throw new LoginLockedException();
		}
		if(!passwordEncoder.matches(prehashPassword(password), user.getPassword())) {
			try {
				if(user.getLastAttempt() != null && user.getLastAttempt().after(threshold)) {
					userDao.setPasswordAttempts(user.getId(), 1, new Date(), false);
				} else {
					userDao.setPasswordAttempts(user.getId(), 1, new Date(), true);
				}
			} catch(DaoException de) {
				logger.info("Could not record password attempt fails for user {" + user.getUsername() + "}.");
			}
			throw new ActionNotAllowedException();
		}
		
		// password matched, log it
		try {
			userDao.setPasswordAttempts(user.getId(), 0, null, true);
		} catch(DaoException de) {
			logger.info("Could not record password attempt success for user {" + user.getUsername() + "}.");
		}
	}
	
	private String hash256(String in) throws NoSuchAlgorithmException, UnsupportedEncodingException{
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		byte[] digest = md.digest(in.getBytes("UTF-16"));
		return Hex.encodeHexString(digest);
	}
	
	@Override
	public String hashToken(String token) throws BadParameterException {
		if(token == null || token.isEmpty()) {
			throw new BadParameterException();
		}
		try {
			return new String(Base64.encode(hash256(token).getBytes()));
		} catch(Exception e) {
			throw new BadParameterException(e);
		}
	}
	
	@Override
	public String prehashPassword(String password) {
		try {
			return new String(Base64.encode(hash256(password).getBytes()));
		} catch(NoSuchAlgorithmException nsae) {
			logger.error("MAJOR PROBLEM! SHA-256 NOT FOUND FOR PREHASH!");
			return password;
		} catch(UnsupportedEncodingException uee) {
			logger.error("MAJOR PROBLEM! UTF-16 NOT FOUND FOR PREHASH!");
			return password;
		}
	}
}
