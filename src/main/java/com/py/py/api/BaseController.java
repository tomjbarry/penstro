package com.py.py.api;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.mongodb.MongoException;
import com.py.py.api.exception.ApiException;
import com.py.py.api.exception.PageableException;
import com.py.py.constants.ParamValues;
import com.py.py.constants.ResponseCodes;
import com.py.py.constants.ServiceValues;
import com.py.py.dao.constants.CacheNames;
import com.py.py.dao.constants.DaoValues;
import com.py.py.dao.exception.DaoException;
import com.py.py.domain.Comment;
import com.py.py.domain.Posting;
import com.py.py.domain.Tag;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.subdomain.TagId;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.ValidationErrorDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.enumeration.FLAG_REASON;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.security.UserAuthenticationToken;
import com.py.py.security.exception.AuthenticationTheftException;
import com.py.py.service.CommentService;
import com.py.py.service.PostingService;
import com.py.py.service.TagService;
import com.py.py.service.UserService;
import com.py.py.service.exception.ActionNotAllowedException;
import com.py.py.service.exception.AuthenticationException;
import com.py.py.service.exception.BackerNotFoundException;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.BalanceException;
import com.py.py.service.exception.BlockedException;
import com.py.py.service.exception.EmailExistsException;
import com.py.py.service.exception.ExistsException;
import com.py.py.service.exception.ExternalServiceException;
import com.py.py.service.exception.FeatureDisabledException;
import com.py.py.service.exception.FinanceException;
import com.py.py.service.exception.LimitException;
import com.py.py.service.exception.LoginLockedException;
import com.py.py.service.exception.NotFoundException;
import com.py.py.service.exception.ObjectLockedException;
import com.py.py.service.exception.PaymentException;
import com.py.py.service.exception.PaymentNotificationException;
import com.py.py.service.exception.PaymentTargetException;
import com.py.py.service.exception.RestrictedException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.exception.TagCountException;
import com.py.py.service.exception.TagLockedException;
import com.py.py.service.exception.UsernameExistsException;
import com.py.py.service.util.ArgCheck;
import com.py.py.service.util.DefaultsFactory;
import com.py.py.service.util.Mapper;
import com.py.py.service.util.ServiceUtils;
import com.py.py.util.PyLogger;
import com.py.py.util.PyUtils;
import com.py.py.validation.util.Validation;

@Controller
public class BaseController {

	protected static final PyLogger logger = PyLogger.getLogger(BaseController.class);
	
	@Autowired
	protected DefaultsFactory defaultsFactory;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected PostingService postingService;
	
	@Autowired
	protected CommentService commentService;
	
	@Autowired
	protected TagService tagService;
	
	@Autowired
	protected EhCacheCacheManager cacheManager;
	
	protected User getUserOrNull(Principal p) {
		try {
			return getUser(p);
		} catch(Exception e) {
			return null;
		}
	}
	
	protected User getUserOrNull(String username) {
		try {
			return getUser(username);
		} catch(Exception e) {
			return null;
		}
	}
	
	protected ObjectId getUserIdOrNull(String username) {
		User user = getUserOrNull(username);
		if(user != null) {
			return user.getId();
		}
		return null;
	}
	
	protected ObjectId getUserIdOrNull(User user) throws ServiceException {
		if(user != null) {
			return user.getId();
		}
		return null;
	}
	
	protected UserInfo getUserInfoOrNull(String username) throws ServiceException {
		return getUserInfoOrNull(getUserOrNull(username));
	}
	
	protected UserInfo getUserInfoOrNull(Principal p) throws ServiceException {
		return getUserInfoOrNull(getUserOrNull(p));
	}
	
	protected UserInfo getUserInfoOrNull(User user) {
		if(user == null) {
			return null;
		}
		try {
			return getUserInfo(user);
		} catch(Exception e) {
			return null;
		}
	}
	
	protected User getUser(Principal p) throws ServiceException {
		if(p == null || p.getName() == null) {
			throw new AuthenticationException();
		}
		UserAuthenticationToken a = (UserAuthenticationToken)p;
		if(!a.isAuthenticated() || a.getUser() == null) {
			throw new AuthenticationException();
		}
		return a.getUser();
	}
	
	protected User getUser(ObjectId userId) throws ServiceException {
		return userService.findUser(userId);
	}
	
	protected ObjectId getUserId(Principal p) throws ServiceException {
		return getUser(p).getId();
	}
	
	protected ObjectId getUserId(User user) throws ServiceException {
		return user.getId();
	}
	
	protected ObjectId getUserId(String username) throws ServiceException {
		return userService.findUserIdByUsername(username);
	}
	
	protected User getUser(String username) throws ServiceException {
		if(cacheManager.getCache(CacheNames.USER_ID_USERNAME) != null) {
			return getUser(getUserId(username));
		} else {
			return userService.findUserByUsername(username);
		}
	}
	
	protected String constructUsername(String username) throws ServiceException {
		return ServiceUtils.getName(username);
	}
	
	protected String constructEmail(String email) throws ServiceException {
		return ServiceUtils.getEmail(email);
	}
	
	protected User getUserByEmail(String email) throws ServiceException {
		/*if(cacheManager.getCache(CacheNames.USER_ID_EMAIL) != null) {
			return getUser(getUserIdByEmail(email));
		} else {
			return userService.findUserByEmail(email);
		}*/
		return userService.findUserByEmail(email);
	}
	
	protected User getUserByEmailOrNull(String email) {
		try {
			return getUserByEmail(email);
			
		} catch(Exception nfe) {
			return null;
		}
	}
	
	protected ObjectId getUserIdByEmail(String email) throws ServiceException {
		return userService.findUserIdByEmail(email);
	}
	
	protected UserInfo getUserInfo(User user) throws ServiceException {
		return userService.findUserInfo(user);
	}
	
	protected UserInfo getCachedUserInfo(User user) throws ServiceException {
		return userService.findCachedUserInfo(user);
	}
	
	protected Posting getPosting(String postingId) throws ServiceException {
		ArgCheck.objectIdCheck(postingId);
		return getPosting(new ObjectId(postingId));
	}
	
	protected Posting getPosting(ObjectId postingId) throws ServiceException {
		return postingService.getPosting(postingId);
	}

	protected Posting getCachedPosting(String postingId) throws ServiceException {
		ArgCheck.objectIdCheck(postingId);
		return getCachedPosting(new ObjectId(postingId));
	}
	
	protected Posting getCachedPosting(ObjectId postingId) throws ServiceException {
		return postingService.getCachedPosting(postingId);
	}
	
	protected ObjectId getPostingId(String postingId) throws ServiceException {
		return getPosting(postingId).getId();
	}
	
	protected ObjectId getCachedPostingId(String postingId) throws ServiceException {
		return getCachedPosting(postingId).getId();
	}
	
	protected Comment getComment(String commentId) throws ServiceException {
		ArgCheck.objectIdCheck(commentId);
		return getComment(new ObjectId(commentId));
	}
	
	protected Comment getComment(ObjectId commentId) throws ServiceException {
		return commentService.getComment(commentId);
	}
	
	protected Comment getCachedComment(String commentId) throws ServiceException {
		ArgCheck.objectIdCheck(commentId);
		return getCachedComment(new ObjectId(commentId));
	}
	
	protected Comment getCachedComment(ObjectId commentId) throws ServiceException {
		return commentService.getCachedComment(commentId);
	}
	
	protected ObjectId getCommentId(String commentId) throws ServiceException {
		return getComment(commentId).getId();
	}
	
	protected Tag getTag(String tagId) throws ServiceException {
		TagId id = new TagId();
		id.fromString(tagId);
		return tagService.getTag(id);
	}
	
	protected Tag getCachedTag(String tagId) throws ServiceException {
		TagId id = new TagId();
		id.fromString(tagId);
		return tagService.getCachedTag(id);
	}
	
	protected Tag getTag(String tag, String language) throws ServiceException {
		return tagService.getTag(tag, language);
	}
	
	protected Tag getCachedTag(String tag, String language) throws ServiceException {
		return tagService.getCachedTag(tag, language);
	}
	
	protected ObjectId constructObjectId(String id) throws ServiceException {
		ArgCheck.objectIdCheck(id);
		return new ObjectId(id);
	}
	
	protected ObjectId constructObjectIdOrNull(String id) throws ServiceException {
		if(id == null || id.isEmpty()) {
			return null;
		}
		return constructObjectId(id);
	}
	
	protected Pageable constructPageable(Integer page, Integer size) throws PageableException {
		if(page == null) {
			page = defaultsFactory.getPage();
		}
		if(size == null) {
			size = defaultsFactory.getSize();
		}
		if(size > ServiceValues.PAGEABLE_SIZE_MAX) {
			throw new PageableException();
		}
		
		try {
			return new PageRequest(page, size);
		} catch(Exception e) {
			throw new PageableException();
		}
	}
	
	protected int constructDirection(String direction) throws BadParameterException {
		if(direction == null || direction.isEmpty()) {
			return defaultsFactory.getDirection();
		}
		if(PyUtils.stringCompare(direction.toLowerCase(), ParamValues.DIRECTION_ASCENDING)) {
			return DaoValues.SORT_ASCENDING;
		} else if(PyUtils.stringCompare(direction.toLowerCase(), ParamValues.DIRECTION_DESCENDING)) {
			return DaoValues.SORT_DESCENDING;
		}
		return defaultsFactory.getDirection();
	}
	
	protected List<COMMENT_TYPE> constructCommentTypes(List<String> commentTypes) 
			throws BadParameterException {
		List<COMMENT_TYPE> types = new ArrayList<COMMENT_TYPE>();
		if(commentTypes == null || commentTypes.isEmpty()) {
			return null;
		}
		for(String s : commentTypes) {
			try {
				COMMENT_TYPE type = COMMENT_TYPE.valueOf(s.toUpperCase());
				if(!types.contains(type)) {
					types.add(type);
				}
			} catch(Exception ex) {
				// do nothing
			}
		}
		if(types.isEmpty()) {
			return null;
		}
		return types;
	}
	
	protected List<String> constructTags(List<String> tags) throws BadParameterException {
		return constructTagList(tags, defaultsFactory.getTagsSearchCount());
	}
	
	protected List<String> constructTagList(List<String> tags, int max) 
			throws BadParameterException {
		if(tags == null) {
			return tags;
		} else {
			if(tags.size() > max) {
				if(max <= 0) {
					return new ArrayList<String>();
				}
				return tags.subList(0, max - 1);
			}
			List<String> tagList = new ArrayList<String>();
			for(String tag : tags) {
				// strip whitespaces
				String correctTag = PyUtils.getTag(tag);
				if(correctTag == null) {
					throw new BadParameterException();
				}
				tagList.add(correctTag);
			}
			return tagList;
		}
	}
	
	protected TIME_OPTION constructTime(String time) throws BadParameterException {
		return PyUtils.convertTimeOption(time, defaultsFactory.getTime());
	}
	
	protected boolean constructPreview(Boolean preview) throws BadParameterException {
		if(preview != null && preview) {
			return true;
		}
		return false;
	}
	
	/*protected String constructLanguage(UserInfo userInfo, String language) throws BadParameterException {
		if(language == null) {
			if(userInfo != null) {
				try {
					Settings settings = userInfo.getSettings();
					if(settings != null) {
						return ServiceUtils.getLanguageOrNull(settings.getLanguage());
					}
				} catch(Exception e) {
					// continue
				}
			}
		} else {
			return ServiceUtils.getLanguageOrNull(language);
		}
		return ServiceUtils.getLanguageOrNull(defaultsFactory.getLanguage());
	}*/
	
	protected String constructLanguage(String language) throws BadParameterException {
		return ServiceUtils.getLanguageOrNull(defaultsFactory.getLanguage());
	}
	
	protected Filter constructFilter(String sort, String time,
			Boolean warning) throws BadParameterException {
		return constructFilter(sort, time, warning, null);
	}
	/*
	protected Boolean constructWarning(UserInfo userInfo, Boolean warning) throws BadParameterException {
		if(warning == null) {
			if(userInfo != null) {
				try {
					Boolean allow = userService.option(userInfo, SETTING_OPTION.ALLOW_WARNING_CONTENT);
					
					if(allow != null && allow) {
						return null;
					} else {
						return defaultsFactory.getWarning();
					}
				} catch(Exception e) {
					return defaultsFactory.getWarning();
				}
			} else {
				return defaultsFactory.getWarning();
			}
		}
		if(warning) {
			// do not specifically search for warning content
			return null;
		} else {
			return false;
		}
	}*/
	
	protected Boolean constructWarning(Boolean warning) throws BadParameterException {
		if(warning == null) {
			return defaultsFactory.getWarning();
		}
		if(warning) {
			return null;
		} else {
			return false;
		}
	}
	
	protected FLAG_REASON constructFlagReason(String reason) throws BadParameterException {
		if(reason == null) {
			throw new BadParameterException();
		}
		FLAG_REASON r = PyUtils.getFlagReason(reason, null);
		if(r == null) {
			throw new BadParameterException();
		}
		return r;
	}
	
	protected Filter constructFilter(String sort, String time, Boolean warning, List<String> tags) 
					throws BadParameterException {
		Filter filter = PyUtils.constructFilter(PyUtils.convertSortOption(sort, 
				defaultsFactory.getSort()),
				PyUtils.convertTimeOption(time, defaultsFactory.getTime()),
				constructWarning(warning),
				constructTags(tags));
		
		if(!Validation.validFilter(filter)) {
			throw new BadParameterException();
		}
		
		return filter;
	}
	
	protected List<EVENT_TYPE> constructEventTypes(List<String> events) {
		if(events == null) {
			return null;
		}
		List<EVENT_TYPE> types = new ArrayList<EVENT_TYPE>();
		try {
			for(String event : events) {
				types.add(EVENT_TYPE.valueOf(event.toUpperCase()));
			}
			return types;
		} catch(Exception e) {
			return null;
		}
	}
	
	protected String constructEmailToken(String emailToken) {
		if(emailToken == null || emailToken.isEmpty()) {
			return null;
		}
		return emailToken;
	}
	
	protected <T extends DTO> APIResponse<T> Success() {
		return new APIResponse<T>(ResponseCodes.SUCCESS);
	}
	
	protected <T extends DTO> APIResponse<T> Success(int code) {
		return new APIResponse<T>(code);
	}
	
	protected <T extends DTO> APIResponse<T> Success(T dto) {
		return new APIResponse<T>(dto);
	}
	
	protected <T extends DTO> APIResponse<T> Success(int code, T dto) {
		APIResponse<T> r = new APIResponse<T>(dto);
		r.setCode(code);
		return r;
	}
	
	protected <P extends DTO> APIPagedResponse<DTO, P> Success(Page<P> page) {
		return new APIPagedResponse<DTO, P>(page);
	}
	
	protected <T extends DTO, P extends DTO> APIPagedResponse<T, P> Success(T dto, Page<P> page) {
		return new APIPagedResponse<T, P>(dto, page);
	}
	
	protected <P extends DTO> APIPagedResponse<DTO, P> Success(int code, Page<P> page) {
		APIPagedResponse<DTO, P> r = new APIPagedResponse<DTO, P>(page);
		r.setCode(code);
		return r;
	}
	
	protected <T extends DTO, P extends DTO> APIPagedResponse<T, P> Success(int code, T dto, Page<P> page) {
		APIPagedResponse<T, P> r = new APIPagedResponse<T, P>(dto, page);
		r.setCode(code);
		return r;
	}
	
	protected <T extends DTO> APIResponse<T> Failure() {
		return new APIResponse<T>();
	}
	
	protected <T extends DTO> APIResponse<T> Failure(int code) {
		return new APIResponse<T>(code);
	}
	
	protected <T extends DTO> APIResponse<T> Failure(int code, T dto) {
		APIResponse<T> r = new APIResponse<T>(dto);
		r.setCode(code);
		return r;
	}
	
	protected <T extends DTO, P extends DTO> APIPagedResponse<T, P> FailurePaged() {
		return new APIPagedResponse<T, P>();
	}
	
	protected <T extends DTO, P extends DTO> APIPagedResponse<T, P> FailurePaged(int code) {
		return new APIPagedResponse<T, P>(code);
	}
	
	protected <T extends DTO> APIResponse<T> Error() {
		return new APIResponse<T>(ResponseCodes.INVALID);
	}
	
	protected <T extends DTO, P extends DTO> APIPagedResponse<T, P> ErrorPaged() {
		return new APIPagedResponse<T, P>(ResponseCodes.INVALID);
	}
	
	protected String constructIpAddress(HttpServletRequest request) throws BadParameterException {
		return ServiceUtils.getClientAddress(request);
	}
	
	@ResponseBody
	@ExceptionHandler(DaoException.class)
	public APIResponse<DTO> handleDaoException() {
		return Error();
	}
	
	@ResponseBody
	@ExceptionHandler(RestrictedException.class)
	public APIResponse<DTO> handleRestrictedException(RestrictedException re){
		RESTRICTED_TYPE type = re.getType();
		int responseCode = ResponseCodes.RESTRICTED;
		if(type == null) {
			// use default response code
		} else if(type == RESTRICTED_TYPE.USERNAME) {
			responseCode = ResponseCodes.RESTRICTED_USERNAME;
		} else if(type == RESTRICTED_TYPE.PASSWORD) {
			responseCode = ResponseCodes.RESTRICTED_PASSWORD;
		} else if(type == RESTRICTED_TYPE.EMAIL) {
			responseCode = ResponseCodes.RESTRICTED_EMAIL;
		}
		
		return Failure(responseCode);
	}
	
	@ResponseBody
	@ExceptionHandler(BalanceException.class)
	public APIResponse<DTO> handleBalanceException() {
		return Failure(ResponseCodes.BALANCE);
	}
	
	@ResponseBody
	@ExceptionHandler(FinanceException.class)
	public APIResponse<DTO> handleFinanceException() {
		return Failure(ResponseCodes.FINANCE);
	}
	
	@ResponseBody
	@ExceptionHandler(AuthenticationException.class)
	public APIResponse<DTO> handleAuthenticationException() {
		return Failure(ResponseCodes.DENIED);
	}

	@ResponseBody
	@ExceptionHandler(ActionNotAllowedException.class)
	public APIResponse<DTO> handleActionNotAllowedException() {
		return Failure(ResponseCodes.NOT_ALLOWED);
	}

	@ResponseBody
	@ExceptionHandler(LimitException.class)
	public APIResponse<DTO> handleLimitException() {
		return Failure(ResponseCodes.LIMIT);
	}
	
	@ResponseBody
	@ExceptionHandler(UsernameExistsException.class)
	public APIResponse<DTO> handleUsernameExistsException() {
		return Failure(ResponseCodes.EXISTS_USERNAME);
	}
	
	@ResponseBody
	@ExceptionHandler(EmailExistsException.class)
	public APIResponse<DTO> handleEmailExistsException() {
		return Failure(ResponseCodes.EXISTS_EMAIL);
	}
	
	@ResponseBody
	@ExceptionHandler(ExistsException.class)
	public APIResponse<DTO> handleExistsException() {
		return Failure(ResponseCodes.EXISTS);
	}
	
	@ResponseBody
	@ExceptionHandler(LoginLockedException.class)
	public APIResponse<DTO> handleLoginLockedException() {
		return Failure(ResponseCodes.LOGIN_LOCKED);
	}
	
	@ResponseBody
	@ExceptionHandler(ObjectLockedException.class)
	public APIResponse<DTO> handleObjectLockedException() {
		return Failure(ResponseCodes.OBJECT_LOCKED);
	}
	
	@ResponseBody
	@ExceptionHandler(BlockedException.class)
	public APIResponse<DTO> handleBlockedException() {
		return Failure(ResponseCodes.NOT_ALLOWED);
	}
	
	@ResponseBody
	@ExceptionHandler(TagCountException.class)
	public APIResponse<DTO> handleTagCountException() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(TagLockedException.class)
	public APIResponse<DTO> handleTagLockedException() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(BackerNotFoundException.class)
	public APIResponse<DTO> handleBackerFoundException(NotFoundException e) {
		return Failure(ResponseCodes.NOT_FOUND_BACKER);
	}
	
	@ResponseBody
	@ExceptionHandler(NotFoundException.class)
	public APIResponse<DTO> handleNotFoundException(NotFoundException e) {
		return Failure(ResponseCodes.NOT_FOUND);
	}
	
	@ResponseBody
	@ExceptionHandler(BadParameterException.class)
	public APIResponse<DTO> handleBadParameterException() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(ServiceException.class)
	public APIResponse<DTO> handleServiceException() {
		return Failure();
	}
	
	@ResponseBody
	@ExceptionHandler(PageableException.class)
	public APIResponse<DTO> handlePageableException() {
		return Failure(ResponseCodes.PAGEABLE);
	}
	
	@ResponseBody
	@ExceptionHandler(ApiException.class)
	public APIResponse<DTO> handleApiException() {
		return Failure();
	}
	
	@ResponseBody
	@ExceptionHandler(PaymentException.class)
	public APIResponse<DTO> handlePaymentException() {
		return Failure(ResponseCodes.PAYMENT);
	}
	
	@ResponseBody
	@ExceptionHandler(PaymentTargetException.class)
	public APIResponse<DTO> handlePaymentTargetException() {
		return Failure(ResponseCodes.PAYMENT_TARGET);
	}
	
	@ResponseBody
	@ExceptionHandler(FeatureDisabledException.class)
	public APIResponse<DTO> handleFeatureDisabledException() {
		return Failure(ResponseCodes.FEATURE_DISABLED);
	}
	
	@ResponseBody
	@ExceptionHandler(ExternalServiceException.class)
	public APIResponse<DTO> handleExternalServiceTargetException() {
		return Failure(ResponseCodes.EXTERNAL_SERVICE);
	}
	
	@ResponseBody
	@ExceptionHandler(org.springframework.security.authentication.LockedException.class)
	public APIResponse<DTO> handleSpringLockedException() {
		return Failure(ResponseCodes.LOCKED);
	}
	
	@ResponseBody
	@ExceptionHandler(AuthenticationTheftException.class)
	public APIResponse<DTO> handleAuthenticationTheftException() {
		return Failure(ResponseCodes.EXPIRED);
	}
	
	@ResponseBody
	@ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
	public APIResponse<DTO> handleSpringAuthenticationException() {
		return Failure(ResponseCodes.DENIED);
	}
	
	@ResponseBody
	@ExceptionHandler(AccessDeniedException.class)
	public APIResponse<DTO> handleAccessDeniedException() {
		return Failure(ResponseCodes.DENIED);
	}
	
	@ResponseBody
	@ExceptionHandler(NumberFormatException.class)
	public APIResponse<DTO> handleNumberFormatException() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(MissingServletRequestParameterException.class)
	public APIResponse<DTO> handleMissingServletRequestParameterException() {
		return Failure(ResponseCodes.PARAMETER);
	}
	
	@ResponseBody
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public APIResponse<ValidationErrorDTO> handleMethodParameterNotValidException(MethodArgumentNotValidException e) {
		try {
			return Failure(ResponseCodes.INVALID, Mapper.mapValidationErrorDTO(
					e.getBindingResult()));
		} catch(Exception ex) {
			return Failure(ResponseCodes.INVALID);
		}
	}
	
	@ResponseBody
	@ExceptionHandler(ServletRequestBindingException.class)
	public APIResponse<DTO> handleServletRequestBindingException() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(HttpMediaTypeNotSupportedException.class)
	public APIResponse<DTO> handleHttpMediaTypeNotSupportedException() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(UnrecognizedPropertyException.class)
	public APIResponse<DTO> handleUnrecognizedPropertyException() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(TypeMismatchException.class)
	public APIResponse<DTO> handleTypeMismatchException() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public APIResponse<DTO> handleHttpMessageNotReadableException(Exception e) {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@ExceptionHandler(DuplicateKeyException.class)
	public APIResponse<DTO> handleDuplicateKeyException() {
		return Failure(ResponseCodes.EXISTS);
	}
	
	@ResponseBody
	@ExceptionHandler(com.mongodb.DuplicateKeyException.class)
	public APIResponse<DTO> handleDuplicateKey() {
		return Failure(ResponseCodes.EXISTS);
	}
	
	
	@ResponseBody
	@ExceptionHandler(MongoException.class)
	public APIResponse<DTO> handleMongoException(MongoException me) {
		// catching of runtime errors that are not properly caught
		logger.debug("Uncaught MongoException in base controller!", me);
		return Error();
	}
	
	@ResponseBody
	@ExceptionHandler(Exception.class)
	public APIResponse<DTO> handleAllException(Exception e) {
		logger.debug("Uncaught exception in base controller!", e);
		return Error();
	}
	
	// Handlers that do not return JSON, but server response codes
	@ResponseBody
	@ExceptionHandler(PaymentNotificationException.class)
	public ResponseEntity<String> handlePaymentNotificationException(PaymentNotificationException pne) {
		return new ResponseEntity<String>(HttpStatus.BAD_REQUEST);
	}
	
}
