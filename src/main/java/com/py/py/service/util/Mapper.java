package com.py.py.service.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bson.types.ObjectId;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import com.py.py.constants.PathVariables;
import com.py.py.domain.AdminAction;
import com.py.py.domain.Comment;
import com.py.py.domain.Correspondence;
import com.py.py.domain.Event;
import com.py.py.domain.Feedback;
import com.py.py.domain.FlagData;
import com.py.py.domain.Message;
import com.py.py.domain.Posting;
import com.py.py.domain.Restricted;
import com.py.py.domain.Subscription;
import com.py.py.domain.Tag;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.domain.subdomain.Balance;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.FlagInfo;
import com.py.py.domain.subdomain.FollowInfo;
import com.py.py.domain.subdomain.ImageLink;
import com.py.py.domain.subdomain.RestrictedWord;
import com.py.py.domain.subdomain.Settings;
import com.py.py.domain.subdomain.TagId;
import com.py.py.domain.subdomain.Tally;
import com.py.py.domain.subdomain.TallyApproximation;
import com.py.py.dto.in.ChangeSettingsDTO;
import com.py.py.dto.in.RegisterUserDTO;
import com.py.py.dto.in.SubmitCommentDTO;
import com.py.py.dto.in.SubmitFeedbackDTO;
import com.py.py.dto.in.SubmitMessageDTO;
import com.py.py.dto.in.SubmitPostingDTO;
import com.py.py.dto.out.AppreciationResponseDTO;
import com.py.py.dto.out.BackerDTO;
import com.py.py.dto.out.BalanceDTO;
import com.py.py.dto.out.CommentDTO;
import com.py.py.dto.out.ConversationDTO;
import com.py.py.dto.out.CurrentUserDTO;
import com.py.py.dto.out.FeedDTO;
import com.py.py.dto.out.FlagDataDTO;
import com.py.py.dto.out.FollowDTO;
import com.py.py.dto.out.MessageDTO;
import com.py.py.dto.out.NotificationDTO;
import com.py.py.dto.out.PostingDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.dto.out.RoleSetDTO;
import com.py.py.dto.out.SettingsDTO;
import com.py.py.dto.out.SubscriptionDTO;
import com.py.py.dto.out.TagDTO;
import com.py.py.dto.out.TallyDTO;
import com.py.py.dto.out.TotalValueDTO;
import com.py.py.dto.out.UserDTO;
import com.py.py.dto.out.UsernameDTO;
import com.py.py.dto.out.ValidationErrorDTO;
import com.py.py.dto.out.admin.AdminActionDTO;
import com.py.py.dto.out.admin.CacheStatisticsDTO;
import com.py.py.dto.out.admin.FeedbackDTO;
import com.py.py.dto.out.admin.RestrictedDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.enumeration.FEEDBACK_STATE;
import com.py.py.enumeration.SETTING_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.generic.Filter;
import com.py.py.generic.ValidationError;
import com.py.py.service.exception.BadParameterException;
import com.py.py.util.PyUtils;

/* This class is intended for simple mappings between
 * domain objects and the corresponding dto. All mappings
 * should be static, and recursive mapping is possible and
 * recommended for sub-objects which are domain/dto.
 */

/* General notes:
 * Avoid using constructors when possible. Explicit mappings
 * make the Mapper class easier to read, and less likely to
 * be a source of bugs with frequent updates.
 */

public class Mapper {
	
	public static UserDetails mapUserDetails(User user, 
			Collection<? extends GrantedAuthority> authorities, boolean credentialsNonExpired) 
			throws BadParameterException {
		ArgCheck.nullCheck(user, authorities);
		Date now = new Date();
		
		// credentialsNonExpired: irrelevant on login, but relevant on authentication!
		
		
		Date lockedUntil = user.getLockedUntil();
		boolean accountNonLocked = false;
		if(lockedUntil == null || now.after(lockedUntil)) {
			accountNonLocked = true;
		}
		
		return ModelFactory.constructUserDetails(
				user.getUsername(), 
				user.getPassword(), 
				true, 
				user.isAccountNonExpired(), 
				credentialsNonExpired, 
				accountNonLocked, 
				authorities);
	}
	
	public static User mapUser(RegisterUserDTO userdto) throws BadParameterException {
		ArgCheck.nullCheck(userdto);
		User user = new User();
		user.setUsername(userdto.getUsername());
		user.setEmail(userdto.getEmail());
		user.setPassword(userdto.getPassword());
		return user;
	}
	
	public static UsernameDTO mapUsernameDTO(CachedUsername cu) 
			throws BadParameterException {
		ArgCheck.nullCheck(cu);
		UsernameDTO dto = new UsernameDTO();
		dto.setExists(false);
		if(cu.getId() != null && cu.isExists()) {
			dto.setExists(true);
		}
		dto.setUsername(cu.getUsername());
		return dto;
	}
	
	public static UsernameDTO mapUsernameDTO(ObjectId userId, String username) 
			throws BadParameterException {
		// do not check userId, that is checked later
		ArgCheck.nullCheck(username);
		UsernameDTO dto = new UsernameDTO();
		dto.setExists(false);
		if(userId != null) {
			dto.setExists(true);
		}
		dto.setUsername(username);
		return dto;
	}
	
	public static List<UsernameDTO> mapUsernameDTOList(List<CachedUsername> cuList)
			throws BadParameterException {
		ArgCheck.nullCheck(cuList);
		List<UsernameDTO> list = new ArrayList<UsernameDTO>();
		for(CachedUsername cu : cuList) {
			if(cu != null) {
				list.add(mapUsernameDTO(cu));
			}
		}
		return list;
	}
	
	public static SettingsDTO mapSettingsDTO(User user, UserInfo userInfo, 
			List<EVENT_TYPE> hiddenFeedEvents) throws BadParameterException {
		ArgCheck.nullCheck(user, userInfo, hiddenFeedEvents);
		SettingsDTO settingsdto = new SettingsDTO();
		settingsdto.setUsername(mapUsernameDTO(user.getId(), user.getUsername()));
		settingsdto.setEmail(user.getEmail());
		settingsdto.setPaymentId(user.getPaymentId());
		settingsdto.setHiddenFeedEvents(hiddenFeedEvents);
		if(userInfo.getSettings() != null) {
			Settings settings = userInfo.getSettings();
			Map<SETTING_OPTION, Boolean> options = new HashMap<SETTING_OPTION, Boolean>();
			settingsdto.setLanguage(settings.getLanguage());
			settingsdto.setInterfaceLanguage(settings.getInterfaceLanguage());
			if(settings.getOptions() != null) {
				for(Map.Entry<String, Boolean> entry : userInfo.getSettings().getOptions().entrySet()) {
					try {
						options.put(SETTING_OPTION.valueOf(entry.getKey()), entry.getValue());
					} catch(Exception e) {
						// do nothing
					}
				}
				settingsdto.setOptions(options);
			}
			
			List<EVENT_TYPE> hiddenNotifications = new ArrayList<EVENT_TYPE>();
			if(settings.getHiddenNotifications() != null) {
				for(String type : userInfo.getSettings().getHiddenNotifications()) {
					try {
						hiddenNotifications.add(EVENT_TYPE.valueOf(type));
					} catch(Exception e) {
						// do nothing
					}
				}
				settingsdto.setHiddenNotificationEvents(hiddenNotifications);
			}
			Map<Integer, Filter> filters = new HashMap<Integer, Filter>();
			if(settings.getFilters() != null) {
				for(Map.Entry<String, Filter> entry : settings.getFilters().entrySet()) {
					try {
						if(PyUtils.isInteger(entry.getKey())) {
							filters.put(Integer.parseInt(entry.getKey()), entry.getValue());
						}
					} catch(Exception ex) {
						// do nothing
					}
				}
			}
			settingsdto.setFilters(filters);
		}
		return settingsdto;
	}
	
	public static AppreciationResponseDTO mapAppreciationResponseDTO(UserInfo userInfo, String appreciationResponseReplacement) throws BadParameterException {
		ArgCheck.nullCheck(userInfo);
		AppreciationResponseDTO dto = new AppreciationResponseDTO();
		dto.setUsername(mapUsernameDTO(userInfo.getId(), userInfo.getUsername()));
		dto.setAppreciationResponseWarning(userInfo.isAppreciationResponseWarning());
		if(appreciationResponseReplacement != null && userInfo.isAppreciationResponseWarning()) {
			dto.setAppreciationResponse(appreciationResponseReplacement);
		} else {
			dto.setAppreciationResponse(userInfo.getAppreciationResponse());
		}
		return dto;
	}
	
	public static AppreciationResponseDTO mapAppreciationResponseDTOFull(UserInfo userInfo) throws BadParameterException {
		ArgCheck.nullCheck(userInfo);
		AppreciationResponseDTO dto = new AppreciationResponseDTO();
		dto.setUsername(mapUsernameDTO(userInfo.getId(), userInfo.getUsername()));
		dto.setAppreciationResponseWarning(userInfo.isAppreciationResponseWarning());
		dto.setAppreciationResponse(userInfo.getAppreciationResponse());
		return dto;
	}
	
	public static UserDTO mapUserDTO(UserInfo userInfo, String descriptionReplacement, 
			boolean canComment, boolean preview, boolean appreciationResponse) throws BadParameterException {
		ArgCheck.nullCheck(userInfo);
		UserDTO dto = new UserDTO();
		dto.setUsername(mapUsernameDTO(userInfo.getId(), userInfo.getUsername()));
		dto.setCanComment(canComment);
		dto.setReplyTally(mapTallyDTO(userInfo.getCommentTally()));
		dto.setReplyCount(userInfo.getCommentCount());
		dto.setContributionTally(mapTallyDTO(userInfo.getContributionTally()));
		dto.setContributedPostings(userInfo.getContributedPostings());
		dto.setContributedComments(userInfo.getContributedComments());
		dto.setContributedAppreciationCount(userInfo.getContributedAppreciationCount());
		dto.setAppreciation(PyUtils.convertToAppreciation(userInfo.getAppreciation()));
		dto.setAppreciationCount(userInfo.getAppreciationCount());
		dto.setPromotion(userInfo.getPromotion());
		dto.setPromotionCount(userInfo.getPromotionCount());
		dto.setFollowerCount(userInfo.getFollowerCount());
		dto.setFolloweeCount(userInfo.getFolloweeCount());
		Settings settings = userInfo.getSettings();
		if(settings != null) {
			dto.setLanguage(settings.getLanguage());
		}
		/*
		List<UsernameDTO> at = new ArrayList<UsernameDTO>();
		if(userInfo.getAppreciationDates() != null) {
			for(AppreciationDate ad : userInfo.getAppreciationDates()) {
				if(at.size() >= ServiceValues.APPRECIATION_TARGET_MAX) {
					break;
				}
				at.add(mapUsernameDTO(ad.getCachedUsername()));
			}
		}
		dto.setAppreciationTargets(at);
		*/
		dto.setWarning(userInfo.isWarning());
		if(userInfo.getFlagged() != null && (new Date()).before(userInfo.getFlagged())) {
			dto.setRemoved(true);
			return dto;
		} else {
			dto.setRemoved(false);
		}
		if(!preview) {
			if(descriptionReplacement != null && userInfo.isWarning()) {
				dto.setDescription(descriptionReplacement);
			} else {
				dto.setDescription(userInfo.getDescription());
			}
		}
		if(appreciationResponse) {
			dto.setAppreciationResponseWarning(userInfo.isAppreciationResponseWarning());
			if(descriptionReplacement != null && userInfo.isAppreciationResponseWarning()) {
				dto.setAppreciationResponse(descriptionReplacement);
			} else {
				dto.setAppreciationResponse(userInfo.getAppreciationResponse());
			}
		}
		return dto;
	}
	
	public static UserDTO mapUserDTOFull(UserInfo userInfo, boolean canComment) 
			throws BadParameterException {
		ArgCheck.nullCheck(userInfo);
		UserDTO dto = new UserDTO();
		dto.setUsername(mapUsernameDTO(userInfo.getId(), userInfo.getUsername()));
		dto.setCanComment(canComment);
		dto.setReplyTally(mapTallyDTO(userInfo.getCommentTally()));
		dto.setReplyCount(userInfo.getCommentCount());
		dto.setContributionTally(mapTallyDTO(userInfo.getContributionTally()));
		dto.setContributedPostings(userInfo.getContributedPostings());
		dto.setContributedComments(userInfo.getContributedComments());
		dto.setContributedAppreciationCount(userInfo.getContributedAppreciationCount());
		dto.setAppreciation(PyUtils.convertToAppreciation(userInfo.getAppreciation()));
		dto.setAppreciationCount(userInfo.getAppreciationCount());
		dto.setPromotion(userInfo.getPromotion());
		dto.setPromotionCount(userInfo.getPromotionCount());
		dto.setFollowerCount(userInfo.getFollowerCount());
		dto.setFolloweeCount(userInfo.getFolloweeCount());
		Settings settings = userInfo.getSettings();
		if(settings != null) {
			dto.setLanguage(settings.getLanguage());
		}
		dto.setWarning(userInfo.isWarning());
		if(userInfo.getFlagged() != null && (new Date()).before(userInfo.getFlagged())) {
			dto.setRemoved(true);
		} else {
			dto.setRemoved(false);
		}
		dto.setDescription(userInfo.getDescription());
		dto.setAppreciationResponseWarning(userInfo.isAppreciationResponseWarning());
		dto.setAppreciationResponse(userInfo.getAppreciationResponse());
		return dto;
	}
	
	public static ImageLink mapImageLink(String link, Integer width, Integer height) throws BadParameterException {
		if(link == null) {
			return null;
		} else {
			ImageLink i = new ImageLink();
			i.setLink(link);
			i.setWidth(width);
			i.setHeight(height);
			return i;
		}
	}
	
	public static Posting mapPosting(SubmitPostingDTO postingdto, CachedUsername author, 
			CachedUsername backer, Date created, String language) throws BadParameterException {
		ArgCheck.nullCheck(postingdto, author, created);
		Posting posting = new Posting();
		posting.setTitle(postingdto.getTitle());
		posting.setContent(postingdto.getContent());
		posting.setPreview(postingdto.getPreview());
		posting.setAuthor(author);
		posting.setBeneficiary(backer);
		posting.setCreated(created);
		posting.setWarning(postingdto.isWarning());
		posting.setLanguage(language);
		// do not map costs or tag promotions, those are separate to allow financial services
		// do map initial tags
		// also convert to lowercase!
		List<String> tags = postingdto.getTags();
		if(tags != null && !tags.isEmpty()) {
			posting.setTags(mapTags(postingdto.getTags()));
		}
		posting.setImage(mapImageLink(postingdto.getImageLink(), postingdto.getImageWidth(), postingdto.getImageHeight()));
		return posting;
	}
	
	public static List<String> mapTags(List<String> tags) throws BadParameterException {
		ArgCheck.nullCheck(tags);
		List<String> list = new ArrayList<String>();
		for(String t : tags) {
			String tag = PyUtils.getTag(t);
			if(tag != null && !tag.isEmpty() && !list.contains(tag)) {
				list.add(tag);
			}
		}
		return list;
	}
	
	public static TallyDTO mapTallyDTO(Tally tally) throws BadParameterException {
		ArgCheck.nullCheck(tally);
		TallyDTO dto = new TallyDTO();
		long appreciation = 0l;
		long promotion = 0l;
		long cost = 0l;
		if(tally.getAppreciation() != null) {
			appreciation = tally.getAppreciation();
		}
		if(tally.getPromotion() != null) {
			promotion = tally.getPromotion();
		}
		if(tally.getCost() != null) {
			cost = tally.getCost();
		}
		dto.setAppreciation(PyUtils.convertToAppreciation(appreciation));
		dto.setCost(cost);
		dto.setPromotion(promotion);
		return dto;
	}
	
	public static TallyDTO mapTallyDTO(TallyApproximation tally) throws BadParameterException {
		ArgCheck.nullCheck(tally);
		TallyDTO dto = new TallyDTO();
		long appreciation = 0l;
		long promotion = 0l;
		long cost = 0l;
		if(tally.getAppreciation() != null) {
			appreciation = tally.getAppreciation();
		}
		if(tally.getPromotion() != null) {
			promotion = tally.getPromotion();
		}
		if(tally.getCost() != null) {
			cost = tally.getCost();
		}
		dto.setAppreciation(PyUtils.convertToAppreciation(appreciation));
		dto.setCost(cost);
		dto.setPromotion(promotion);
		return dto;
	}
	
	public static PostingDTO mapPostingDTO(Posting posting, String contentReplacement, 
			boolean canAppreciate, boolean canComment, boolean preview) 
			throws BadParameterException {
		ArgCheck.nullCheck(posting);
		ArgCheck.nullCheck(posting.getId());
		PostingDTO dto = new PostingDTO();
		dto.setId(posting.getId().toHexString());
		dto.setCanAppreciate(canAppreciate);
		dto.setCanComment(canComment);
		dto.setAuthor(mapUsernameDTO(posting.getAuthor()));
		if(posting.getImage() != null) {
			dto.setImageLink(posting.getImage().getLink());
			dto.setImageHeight(posting.getImage().getHeight());
			dto.setImageWidth(posting.getImage().getWidth());
		}
		if(posting.getBeneficiary() != null) {
			dto.setBeneficiary(mapUsernameDTO(posting.getBeneficiary()));
		}
		dto.setCreated(posting.getCreated());
		dto.setWarning(posting.isWarning());
		dto.setTally(mapTallyDTO(posting.getTally()));
		dto.setReplyCount(posting.getCommentCount());
		dto.setAppreciationCount(posting.getAppreciationCount());
		dto.setPromotionCount(posting.getPromotionCount());
		dto.setReplyTally(mapTallyDTO(posting.getCommentTally()));
		dto.setLanguage(posting.getLanguage());
		dto.setTags(posting.getTags());
		dto.setFlagged(posting.isFlagged());
		dto.setArchived(posting.getArchived() != null);
		dto.setDisabled(!posting.isEnabled());
		if(!posting.isEnabled() || posting.isFlagged() || posting.isRemoved()) {
			dto.setRemoved(true);
			return dto;
		} else {
			dto.setRemoved(false);
		}
		dto.setTitle(posting.getTitle());
		if(!preview) {
			if(contentReplacement != null && posting.isWarning()) {
				dto.setContent(contentReplacement);
			} else {
				dto.setContent(posting.getContent());
				dto.setPreview(posting.getPreview());
			}
		}
		return dto;
	}
	
	public static PostingDTO mapPostingDTOFull(Posting posting, boolean canAppreciate, 
			boolean canComment) throws BadParameterException {
		ArgCheck.nullCheck(posting);
		ArgCheck.nullCheck(posting.getId());
		PostingDTO dto = new PostingDTO();
		dto.setId(posting.getId().toHexString());
		dto.setCanAppreciate(canAppreciate);
		dto.setCanComment(canComment);
		dto.setFlagged(posting.isFlagged());
		dto.setAuthor(mapUsernameDTO(posting.getAuthor()));
		if(posting.getImage() != null) {
			dto.setImageLink(posting.getImage().getLink());
			dto.setImageHeight(posting.getImage().getHeight());
			dto.setImageWidth(posting.getImage().getWidth());
		}
		if(posting.getBeneficiary() != null) {
			dto.setBeneficiary(mapUsernameDTO(posting.getBeneficiary()));
		}
		dto.setCreated(posting.getCreated());
		dto.setWarning(posting.isWarning());
		dto.setTally(mapTallyDTO(posting.getTally()));
		dto.setReplyCount(posting.getCommentCount());
		dto.setAppreciationCount(posting.getAppreciationCount());
		dto.setPromotionCount(posting.getPromotionCount());
		dto.setReplyTally(mapTallyDTO(posting.getCommentTally()));
		dto.setLanguage(posting.getLanguage());
		dto.setTags(posting.getTags());
		dto.setArchived(posting.getArchived() != null);
		dto.setDisabled(!posting.isEnabled());
		if(!posting.isEnabled() || posting.isFlagged() || posting.isRemoved()) {
			dto.setRemoved(true);
		} else {
			dto.setRemoved(false);
		}
		dto.setTitle(posting.getTitle());
		dto.setContent(posting.getContent());
		dto.setPreview(posting.getPreview());
		return dto;
	}
	
	public static String mapCommentBase(COMMENT_TYPE type, ObjectId baseId, String baseString) throws BadParameterException {
		ArgCheck.nullCheck(type);
		if(COMMENT_TYPE.POSTING.equals(type)) {
			return baseId.toHexString();
		} else if(COMMENT_TYPE.USER.equals(type)) {
			return baseString;
		} else if(COMMENT_TYPE.TAG.equals(type)) {
			TagId tagId = new TagId();
			tagId.fromString(baseString);
			return tagId.getName();
		}
		return baseString;
	}
	
	public static CommentDTO mapCommentDTO(Comment comment, String contentReplacement, 
			boolean canAppreciate, boolean canComment, boolean preview) 
			throws BadParameterException {
		ArgCheck.nullCheck(comment);
		ArgCheck.nullCheck(comment.getId());
		CommentDTO dto = new CommentDTO();
		dto.setId(comment.getId().toHexString());
		dto.setFlagged(comment.isFlagged());
		dto.setBase(mapCommentBase(comment.getType(), comment.getBaseId(), comment.getBaseString()));
		dto.setType(comment.getType());
		dto.setAuthor(mapUsernameDTO(comment.getAuthor()));
		if(comment.getBeneficiary() != null) {
			dto.setBeneficiary(mapUsernameDTO(comment.getBeneficiary()));
		}
		dto.setCreated(comment.getCreated());
		dto.setWarning(comment.isWarning());
		dto.setCanAppreciate(canAppreciate);
		dto.setCanComment(canComment);
		dto.setTally(mapTallyDTO(comment.getTally()));
		dto.setReplyCount(comment.getReplyCount());
		dto.setAppreciationCount(comment.getAppreciationCount());
		dto.setPromotionCount(comment.getPromotionCount());
		dto.setReplyTally(mapTallyDTO(comment.getReplyTally()));
		dto.setLanguage(comment.getLanguage());
		dto.setParent(null);
		if(comment.getParent() != null) {
			dto.setParent(comment.getParent().toHexString());
		}
		dto.setArchived(comment.getArchived() != null);
		dto.setDisabled(!comment.isEnabled());
		if(!comment.isEnabled() || comment.isFlagged() || comment.isRemoved()) {
			dto.setRemoved(true);
			return dto;
		} else {
			dto.setRemoved(false);
		}
		if(!preview) {
			if(contentReplacement != null && comment.isWarning()) {
				dto.setContent(contentReplacement);
			} else {
				dto.setContent(comment.getContent());
			}
		}
		return dto;
	}
	
	public static CommentDTO mapCommentDTOFull(Comment comment, boolean canAppreciate, 
			boolean canComment) throws BadParameterException {
		ArgCheck.nullCheck(comment);
		ArgCheck.nullCheck(comment.getId());
		CommentDTO dto = new CommentDTO();
		dto.setId(comment.getId().toHexString());
		dto.setFlagged(comment.isFlagged());
		dto.setBase(mapCommentBase(comment.getType(), comment.getBaseId(), comment.getBaseString()));
		dto.setType(comment.getType());
		dto.setAuthor(mapUsernameDTO(comment.getAuthor()));
		if(comment.getBeneficiary() != null) {
			dto.setBeneficiary(mapUsernameDTO(comment.getBeneficiary()));
		}
		dto.setCreated(comment.getCreated());
		dto.setWarning(comment.isWarning());
		dto.setCanAppreciate(canAppreciate);
		dto.setCanComment(canComment);
		dto.setTally(mapTallyDTO(comment.getTally()));
		dto.setReplyCount(comment.getReplyCount());
		dto.setAppreciationCount(comment.getAppreciationCount());
		dto.setPromotionCount(comment.getPromotionCount());
		dto.setReplyTally(mapTallyDTO(comment.getReplyTally()));
		dto.setLanguage(comment.getLanguage());
		dto.setParent(null);
		if(comment.getParent() != null) {
			dto.setParent(comment.getParent().toHexString());
		}
		dto.setArchived(comment.getArchived() != null);
		dto.setDisabled(!comment.isEnabled());
		if(!comment.isEnabled() || comment.isFlagged() || comment.isRemoved()) {
			dto.setRemoved(true);
		} else {
			dto.setRemoved(false);
		}
		dto.setContent(comment.getContent());
		return dto;
	}
	
	public static Comment mapComment(SubmitCommentDTO dto, CachedUsername author, 
			CachedUsername backer, ObjectId baseId, String baseString, ObjectId parentId, 
			Date created, COMMENT_TYPE type, String language) throws BadParameterException {
		ArgCheck.nullCheck(dto, author);
		Comment comment = new Comment();
		comment.setAuthor(author);
		comment.setBeneficiary(backer);
		comment.setContent(dto.getContent());
		comment.setBaseId(baseId);
		comment.setBaseString(baseString);
		comment.setParent(parentId);
		comment.setCreated(created);
		comment.setType(type);
		comment.setLanguage(language);
		comment.setWarning(dto.isWarning());
		return comment;
	}
	
	public static CurrentUserDTO mapCurrentUserDTO(UserInfo userInfo, User user, 
			long notificationCount, long feedCount, long messageCount, long loginFailures) 
					throws BadParameterException {
		ArgCheck.nullCheck(userInfo, user);
		CurrentUserDTO userdto = new CurrentUserDTO();
		userdto.setUsername(mapUsernameDTO(user.getId(), user.getUsername()));
		userdto.setEmail(user.getEmail());
		userdto.setPaymentId(user.getPaymentId());
		userdto.setNotificationCount(notificationCount);
		userdto.setFeedCount(feedCount);
		userdto.setMessageCount(messageCount);
		userdto.setLoginFailureCount(loginFailures);
		/*List<Location> savedLocations = new ArrayList<Location>();
		if(user.getSavedLocations() != null) {
			savedLocations = user.getSavedLocations();
		}
		userdto.setSavedLocations(savedLocations);
		*/
		userdto.setPendingActions(userInfo.getPendingActions());
		return userdto;
	}
	
	public static BalanceDTO mapBalanceDTO(Balance balance) throws BadParameterException {
		ArgCheck.nullCheck(balance);
		BalanceDTO balancedto = new BalanceDTO();
		long gold = 0L;
		if(balance.getGold() != null) {
			gold = balance.getGold();
		}
		balancedto.setBalance(gold);
		return balancedto;
	}

	public static NotificationDTO mapNotificationDTO(Event event) throws BadParameterException {
		ArgCheck.nullCheck(event);
		NotificationDTO notificationdto = new NotificationDTO();
		notificationdto.setAuthor(mapUsernameDTO(event.getAuthor()));
		notificationdto.setOccurred(event.getCreated());
		notificationdto.setTargets(event.getTargets());
		Map<String, String> targetIds = new HashMap<String, String>();
		
		// checks if the specified user has been deleted or removed
		if(event.getAuthor() != null && event.getAuthor().getId() != null) {
			targetIds.put(PathVariables.SOURCE_ID, event.getAuthor().getUsername());
		}
		if(event.getTarget() != null && event.getTarget().getId() != null) {
			targetIds.put(PathVariables.TARGET_ID, event.getTarget().getUsername());
		}
		if(event.getBeneficiary() != null && event.getBeneficiary().getId() != null) {
			targetIds.put(PathVariables.BENEFICIARY_ID, 
					event.getBeneficiary().getUsername());
		}
		if(event.getPrimaryId() != null) {
			if(event.getBaseId() == null && event.getBaseString() == null) {
				targetIds.put(PathVariables.POSTING_ID, 
						event.getPrimaryId().toHexString());
			} else {
				targetIds.put(PathVariables.COMMENT_ID, 
						event.getPrimaryId().toHexString());
			}
		}
		if(event.getBaseId() != null || event.getBaseString() != null) {
			COMMENT_TYPE baseType = event.getBaseType();
			if(baseType == COMMENT_TYPE.POSTING) {
				targetIds.put(PathVariables.POSTING_ID, event.getBaseId().toHexString());
			} else if(baseType == COMMENT_TYPE.TAG) {
				targetIds.put(PathVariables.TAG_ID, event.getBaseString());
			} else if(baseType == COMMENT_TYPE.USER) {
				targetIds.put(PathVariables.USER_ID, event.getBaseString());
			}
		}
		if(event.getParentId() != null) {
			targetIds.put(PathVariables.PARENT_ID, event.getParentId().toHexString());
		}
		
		notificationdto.setTargetIds(targetIds);
		notificationdto.setType(event.getType());
		return notificationdto;
	}

	public static FeedDTO mapFeedDTO(Event event) throws BadParameterException {
		ArgCheck.nullCheck(event);
		FeedDTO dto = new FeedDTO();
		dto.setAuthor(mapUsernameDTO(event.getAuthor()));
		dto.setOccurred(event.getCreated());
		dto.setTargets(event.getTargets());
		Map<String, String> targetIds = new HashMap<String, String>();

		// checks if the specified user has been deleted or removed
		if(event.getAuthor() != null && event.getAuthor().getId() != null) {
			targetIds.put(PathVariables.SOURCE_ID, event.getAuthor().getUsername());
		}
		if(event.getTarget() != null && event.getTarget().getId() != null) {
			targetIds.put(PathVariables.TARGET_ID, event.getTarget().getUsername());
		}
		if(event.getBeneficiary() != null && event.getBeneficiary().getId() != null) {
			targetIds.put(PathVariables.BENEFICIARY_ID, 
					event.getBeneficiary().getUsername());
		}
		if(event.getPrimaryId() != null) {
			if(event.getBaseId() == null && event.getBaseString() == null) {
				targetIds.put(PathVariables.POSTING_ID, 
						event.getPrimaryId().toHexString());
			} else {
				targetIds.put(PathVariables.COMMENT_ID, 
						event.getPrimaryId().toHexString());
			}
		}
		if(event.getBaseId() != null || event.getBaseString() != null) {
			COMMENT_TYPE baseType = event.getBaseType();
			if(baseType == COMMENT_TYPE.POSTING) {
				targetIds.put(PathVariables.POSTING_ID, event.getBaseId().toHexString());
			} else if(baseType == COMMENT_TYPE.TAG) {
				targetIds.put(PathVariables.TAG_ID, event.getBaseString());
			} else if(baseType == COMMENT_TYPE.USER) {
				targetIds.put(PathVariables.USER_ID, event.getBaseString());
			}
		}
		if(event.getParentId() != null) {
			targetIds.put(PathVariables.PARENT_ID, event.getParentId().toHexString());
		}
		
		dto.setTargetIds(targetIds);
		dto.setType(event.getType());
		return dto;
	}
	
	public static Message mapMessage(SubmitMessageDTO dto, CachedUsername author, 
			CachedUsername target, boolean read, Date created) throws BadParameterException {
		ArgCheck.nullCheck(dto, author, created);
		Message message = new Message();
		message.setAuthor(author);
		message.setMessage(dto.getMessage());
		message.setTarget(target);
		message.setCreated(created);
		message.setRead(read);
		return message;
	}
	
	public static ConversationDTO mapConversationDTO(Correspondence cor, boolean preview) throws BadParameterException {
		ArgCheck.nullCheck(cor);
		ConversationDTO dto = new ConversationDTO();
		if(cor.getFirst() != null && cor.getFirst().getId() == cor.getAuthor()) {
			dto.setAuthor(mapUsernameDTO(cor.getFirst()));
			dto.setTarget(mapUsernameDTO(cor.getSecond()));
		} else {
			dto.setAuthor(mapUsernameDTO(cor.getSecond()));
			dto.setTarget(mapUsernameDTO(cor.getFirst()));
		}
		dto.setLastMessage(cor.getLastModified());
		dto.setCreated(cor.getCreated());
		if(!preview) {
			dto.setPreview(cor.getMessage());
		}
		return dto;
	}
	
	public static MessageDTO mapMessageDTO(Message message, boolean preview) throws BadParameterException {
		ArgCheck.nullCheck(message);
		MessageDTO dto = new MessageDTO();
		dto.setAuthor(mapUsernameDTO(message.getAuthor()));
		dto.setTarget(mapUsernameDTO(message.getTarget()));
		dto.setCreated(message.getCreated());
		dto.setRead(message.isRead());
		if(!preview) {
			dto.setMessage(message.getMessage());
		}
		return dto;
	}

	public static ResultSuccessDTO mapResultSuccessDTO(String result) throws BadParameterException {
		ArgCheck.nullCheck(result);
		ResultSuccessDTO dto = new ResultSuccessDTO();
		dto.setResult(result);
		return dto;
	}
	
	public static TagDTO mapTagDTO(Tag tag, boolean canPromote, boolean canComment)
			throws BadParameterException {
		ArgCheck.tagCheck(tag);
		TagDTO dto = new TagDTO();
		dto.setName(tag.getId().getName());
		dto.setLanguage(tag.getId().getLanguage());
		dto.setCanPromote(canPromote);
		dto.setCanComment(canComment);
		dto.setLocked(tag.isLocked());
		dto.setValue(tag.getValue());
		dto.setAppreciation(PyUtils.convertToAppreciation(tag.getAppreciation()));
		dto.setLastPromotion(tag.getLastPromotion());
		dto.setReplyTally(mapTallyDTO(tag.getCommentTally()));
		dto.setReplyCount(tag.getCommentCount());
		return dto;
	}
	
	public static BackerDTO mapBackerDTO(CachedUsername source, CachedUsername target, 
			boolean isEmail, long amount) throws BadParameterException {
		ArgCheck.nullCheck(source, target);
		BackerDTO dto = new BackerDTO();
		dto.setSource(mapUsernameDTO(source));
		dto.setTarget(mapUsernameDTO(target));
		dto.setValue(amount);
		dto.setEmail(isEmail);
		return dto;
	}
	
	public static FollowDTO mapFollowDTO(FollowInfo followInfo) throws BadParameterException {
		ArgCheck.nullCheck(followInfo);
		FollowDTO dto = new FollowDTO();
		dto.setUsername(mapUsernameDTO(followInfo.getUsername()));
		dto.setAdded(followInfo.getAdded());
		return dto;
	}
	
	public static FollowDTO mapFollowDTO(Event event) throws BadParameterException {
		ArgCheck.nullCheck(event);
		FollowDTO dto = new FollowDTO();
		dto.setUsername(mapUsernameDTO(event.getAuthor()));
		dto.setAdded(event.getCreated());
		return dto;
	}
	
	public static SubscriptionDTO mapSubscriptionDTO(Subscription sub) throws BadParameterException {
		ArgCheck.nullCheck(sub);
		SubscriptionDTO dto = new SubscriptionDTO();
		List<FollowDTO> follows = new ArrayList<FollowDTO>();
		List<FollowDTO> blocked = new ArrayList<FollowDTO>();
		
		List<FollowInfo> f = sub.getFollows();
		if(f != null) {
			for(FollowInfo info : f) {
				follows.add(mapFollowDTO(info));
			}
		}
		List<FollowInfo> b = sub.getBlocked();
		if(b != null) {
			for(FollowInfo info : b) {
				blocked.add(mapFollowDTO(info));
			}
		}
		
		dto.setFollows(follows);
		dto.setBlocked(blocked);
		return dto;
	}
	
	public static Map<SETTING_OPTION, Boolean> mapOptions(ChangeSettingsDTO dto) throws BadParameterException {
		ArgCheck.nullCheck(dto);
		Map<SETTING_OPTION, Boolean> options = new HashMap<SETTING_OPTION, Boolean>();
		if(dto.getOptions() == null) {
			return null;
		}
		options.putAll(dto.getOptions());
		return options;
	}
	
	public static List<EVENT_TYPE> mapHiddenNotificationEvents(ChangeSettingsDTO dto) throws BadParameterException {
		ArgCheck.nullCheck(dto);
		List<EVENT_TYPE> list = new ArrayList<EVENT_TYPE>();
		if(dto.getHiddenNotificationEvents() == null) {
			return null;
		}
		list.addAll(dto.getHiddenNotificationEvents());
		return list;
	}
	
	public static List<EVENT_TYPE> mapHiddenFeedEvents(ChangeSettingsDTO dto) throws BadParameterException {
		ArgCheck.nullCheck(dto);
		List<EVENT_TYPE> list = new ArrayList<EVENT_TYPE>();
		if(dto.getHiddenFeedEvents() == null) {
			return null;
		}
		list.addAll(dto.getHiddenFeedEvents());
		return list;
	}
	
	public static List<Filter> mapFilters(ChangeSettingsDTO dto) throws BadParameterException {
		ArgCheck.nullCheck(dto);
		List<Filter> list = new ArrayList<Filter>();
		if(dto.getFilters() == null) {
			return null;
		}
		list.addAll(dto.getFilters());
		return list;
	}
	
	public static FeedbackDTO mapFeedbackDTO(Feedback feedback) throws BadParameterException {
		ArgCheck.nullCheck(feedback);
		ArgCheck.nullCheck(feedback.getId());
		FeedbackDTO dto = new FeedbackDTO();
		dto.setId(feedback.getId().toHexString());
		dto.setType(feedback.getType());
		dto.setContext(feedback.getContext());
		dto.setState(feedback.getState());
		dto.setSummary(feedback.getSummary());
		dto.setCreated(feedback.getCreated());
		dto.setLastModified(feedback.getLastModified());
		return dto;
	}
	
	public static Feedback mapFeedback(SubmitFeedbackDTO dto, CachedUsername author) 
			throws BadParameterException {
		ArgCheck.nullCheck(dto, author);
		Feedback feedback = new Feedback();
		Date now = new Date();
		feedback.setCreated(now);
		feedback.setLastModified(now);
		feedback.setState(FEEDBACK_STATE.INITIAL);
		feedback.setAuthor(author);
		feedback.setType(dto.getType());
		feedback.setContext(dto.getContext());
		feedback.setSummary(dto.getSummary());
		return feedback;
	}
	
	public static RoleSetDTO mapRoleSetDTO(User user) throws BadParameterException {
		ArgCheck.userCheck(user);
		RoleSetDTO dto = new RoleSetDTO();
		List<String> roles = new ArrayList<String>();
		List<String> overrideRoles = new ArrayList<String>();
		if(user.getRoles() != null) {
			roles.addAll(user.getRoles());
		}
		if(user.getOverrideRoles() != null) {
			overrideRoles.addAll(user.getOverrideRoles());
		}
		dto.setRoles(roles);
		dto.setOverrideRoles(overrideRoles);
		return dto;
	}
	
	public static TotalValueDTO mapTotalValueDTO(Map<TIME_OPTION, BigInteger> postings, 
			Map<TIME_OPTION, BigInteger> comments, Map<TIME_OPTION, BigInteger> users, 
			Map<TIME_OPTION, BigInteger> tags, Map<TIME_OPTION, BigInteger> all)
					throws BadParameterException {
		ArgCheck.nullCheck(postings, comments, users, tags);
		TotalValueDTO dto = new TotalValueDTO();
		dto.setPostings(postings);
		dto.setComments(comments);
		dto.setUsers(users);
		dto.setTags(tags);
		dto.setAll(all);
		return dto;
	}
	
	public static RestrictedDTO mapRestrictedDTO(Restricted restricted) 
			throws BadParameterException {
		ArgCheck.nullCheck(restricted);
		RestrictedWord rw = restricted.getId();
		ArgCheck.nullCheck(rw);
		RestrictedDTO dto = new RestrictedDTO();
		dto.setWord(rw.getWord());
		dto.setType(rw.getType());
		dto.setCreated(restricted.getCreated());
		return dto;
	}
	
	public static AdminActionDTO mapAdminActionDTO(AdminAction action) 
			throws BadParameterException {
		ArgCheck.nullCheck(action);
		AdminActionDTO dto = new AdminActionDTO();
		dto.setCreated(action.getCreated());
		dto.setLastModified(action.getLastModified());
		dto.setState(action.getState());
		dto.setType(action.getType());
		dto.setTarget(action.getTarget());
		dto.setDto(action.getDto());
		return dto;
	}
	
	public static CacheStatisticsDTO mapCacheStatisticsDTO(
			Map<String, Map<String, Object> > stats) throws BadParameterException {
		ArgCheck.nullCheck(stats);
		CacheStatisticsDTO dto = new CacheStatisticsDTO();
		dto.setCaches(stats);
		return dto;
	}
	
	public static ValidationErrorDTO mapValidationErrorDTO(BindingResult bindingResult) 
			throws BadParameterException {
		ArgCheck.nullCheck(bindingResult);
		List<ValidationError> validationErrors = new ArrayList<ValidationError>();
		for(ObjectError error : bindingResult.getAllErrors()) {
			ValidationError vE = new ValidationError();
			vE.setArguments(error.getArguments());
			vE.setCode(error.getCode());
			vE.setDefaultCode(error.getDefaultMessage());
			if(error instanceof FieldError) {
				vE.setField(((FieldError)error).getField());
			}
			validationErrors.add(vE);
		}
		ValidationErrorDTO dto = new ValidationErrorDTO();
		dto.setErrors(validationErrors);
		return dto;
	}
	
	public static FlagDataDTO mapFlagDataDTO(FlagData flagData) throws BadParameterException {
		ArgCheck.nullCheck(flagData);
		FlagInfo fi = flagData.getId();
		ArgCheck.nullCheck(fi);
		FlagDataDTO dto = new FlagDataDTO();
		dto.setReferenceId(fi.getReferenceId().toHexString());
		dto.setTarget(flagData.getTarget());
		dto.setType(fi.getType());
		dto.setValue(flagData.getValue());
		dto.setTotal(flagData.getTotal());
		dto.setReasons(flagData.getReasons());
		return dto;
	}
}
