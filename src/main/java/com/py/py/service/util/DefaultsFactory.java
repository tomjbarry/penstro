package com.py.py.service.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

import com.py.py.constants.SettingsDefaults;
import com.py.py.dao.constants.DaoValues;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.Settings;
import com.py.py.enumeration.ADMIN_STATE;
import com.py.py.enumeration.ADMIN_TYPE;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.enumeration.EVENT_TYPE;
import com.py.py.enumeration.FEEDBACK_CONTEXT;
import com.py.py.enumeration.FEEDBACK_STATE;
import com.py.py.enumeration.FEEDBACK_TYPE;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.enumeration.SETTING_OPTION;
import com.py.py.enumeration.SORT_OPTION;
import com.py.py.enumeration.TIME_OPTION;
import com.py.py.util.GenericDefaults;
import com.py.py.util.PyUtils;

public class DefaultsFactory {
	
	@Value("${py.defaults.page:}")
	private String pageValue;
	private Integer page;
	
	@Value("${py.defaults.size:}") 
	private String sizeValue;
	private Integer size;
	
	@Value("${py.defaults.direction:}")
	private String directionValue;
	private Integer direction;
	
	@Value("${py.defaults.tagsSearchCount:}")
	private String tagsSearchCountValue;
	private Integer tagsSearchCount;
	
	@Value("${py.defaults.tagsList:}")
	private String tagsString;
	private List<String> tagsList;
	
	@Value("${py.defaults.sort:}")
	private String sortValue;
	private SORT_OPTION sort;
	
	@Value("${py.defaults.time:}")
	private String timeValue;
	private TIME_OPTION time;
	
	@Value("${py.defaults.warning:}")
	private String warningValue;
	private Boolean warning;
	
	@Value("${py.defaults.warningContentReplacement:}")
	private String warningContentReplacementValue;
	private String warningContentReplacement;
	
	@Value("${py.defaults.warningDescriptionReplacement:}")
	private String warningDescriptionReplacementValue;
	private String warningDescriptionReplacement;
	
	@Value("${py.defaults.warningAppreciationResponseReplacement:}")
	private String warningAppreciationResponseReplacementValue;
	private String warningAppreciationResponseReplacement;
	
	@Value("${py.defaults.language:}")
	private String languageValue;
	private String language;
	
	@Value("${py.defaults.segments.hour:}")
	private String segmentHourValue;
	private long segmentHour;
	
	@Value("${py.defaults.segments.day:}")
	private String segmentDayValue;
	private long segmentDay;
	
	@Value("${py.defaults.segments.month:}")
	private String segmentMonthValue;
	private long segmentMonth;
	
	@Value("${py.defaults.segments.year:}")
	private String segmentYearValue;
	private long segmentYear;
	
	@Value("${py.defaults.segments.alltime:}")
	private String segmentAlltimeValue;
	private long segmentAlltime;
	
	@Value("${py.defaults.followeeList:}")
	private String followeeString;
	private List<String> followeeList;
	private List<CachedUsername> followeeCachedUsernameList;
	
	@Value("${py.defaults.feed.events:}")
	private String feedEventsString;
	private List<EVENT_TYPE> feedEventsList;
	private List<String> feedEventsStringList;
	
	@Value("${py.defaults.notifications.events:}")
	private String notificationsEventsString;
	private List<EVENT_TYPE> notificationsEventsList;
	private List<String> notificationsEventsStringList;
	
	@Value("${py.defaults.comments.previews.types:}")
	private String commentsPreviewsTypesString;
	private List<COMMENT_TYPE> commentsPreviewsTypesList;
	private List<String> commentsPreviewsTypesStringList;
	
	private Map<String, Boolean> settingsMap = new HashMap<String, Boolean>();

	@Value("${py.defaults.settings.allow.profile.comments:}")
	private String settingsAllowProfileComments;
	
	@Value("${py.defaults.settings.allow.warning.content:}")
	private String settingsAllowWarningContent;
	
	@Value("${py.defaults.settings.hide.user.profile:}")
	private String settingsHideUserProfile;
	
	// admin
	@Value("${py.defaults.admin.feedback.type:}")
	private String feedbackTypeValue;
	private FEEDBACK_TYPE feedbackType;
	
	@Value("${py.defaults.admin.feedback.state:}")
	private String feedbackStateValue;
	private FEEDBACK_STATE feedbackState;
	
	@Value("${py.defaults.admin.feedback.context:}")
	private String feedbackContextValue;
	private FEEDBACK_CONTEXT feedbackContext;
	
	@Value("${py.defaults.admin.restricted.type:}")
	private String restrictedTypeValue;
	private RESTRICTED_TYPE restrictedType;
	
	@Value("${py.defaults.admin.flag.type:}")
	private String flagTypeValue;
	private FLAG_TYPE flagType;
	
	@Value("${py.defaults.admin.action.type:}")
	private String adminActionTypeValue;
	private ADMIN_TYPE adminActionType;
	
	@Value("${py.defaults.admin.action.state:}")
	private String adminActionStateValue;
	private ADMIN_STATE adminActionState;
	
	public DefaultsFactory() {
	}
	
	// used in bean
	@SuppressWarnings("unused")
	private void populateDefaults() {
		page = parseInteger(pageValue, GenericDefaults.PAGEABLE_PAGE);
		size = parseInteger(sizeValue, GenericDefaults.PAGEABLE_SIZE);
		direction = parseInteger(directionValue, GenericDefaults.DIRECTION);
		if(direction == null || 
				(direction != DaoValues.SORT_ASCENDING
				&& direction != DaoValues.SORT_DESCENDING)) {
			// just in case
			direction = DaoValues.SORT_DESCENDING;
		}
		
		language = parseString(languageValue, GenericDefaults.LANGUAGE);
				
		tagsSearchCount = parseInteger(tagsSearchCountValue, GenericDefaults.TAGS_SEARCH_COUNT);
		tagsList = parseStringList(tagsString, null);
		followeeList = parseStringList(followeeString, new ArrayList<String>());
		sort = parseSort(sortValue, GenericDefaults.SORT);
		time = parseTime(timeValue, GenericDefaults.TIME);
		warning = parseNullPermittedBoolean(warningValue, GenericDefaults.WARNING);
		warningContentReplacement = parseString(warningContentReplacementValue, 
				GenericDefaults.WARNING_CONTENT_REPLACEMENT);
		setWarningDescriptionReplacement(parseString(warningDescriptionReplacementValue, 
				GenericDefaults.WARNING_DESCRIPTION_REPLACEMENT));
		setWarningAppreciationResponseReplacement(parseString(
				warningAppreciationResponseReplacementValue, 
				GenericDefaults.WARNING_APPRECIATION_RESPONSE_REPLACEMENT));
		
		feedEventsList = parseEnumList(feedEventsString, GenericDefaults.FEED_EVENTS, EVENT_TYPE.class);
		feedEventsStringList = PyUtils.<EVENT_TYPE>stringifiedList(feedEventsList);
		notificationsEventsList = parseEnumList(notificationsEventsString, 
				GenericDefaults.NOTIFICATION_EVENTS, EVENT_TYPE.class);
		notificationsEventsStringList = PyUtils.<EVENT_TYPE>stringifiedList(notificationsEventsList);
		
		commentsPreviewsTypesList = parseEnumList(commentsPreviewsTypesString, 
				GenericDefaults.COMMENT_PREVIEW_TYPES, COMMENT_TYPE.class);
		setCommentsPreviewsTypesStringList(PyUtils.<COMMENT_TYPE>stringifiedList(commentsPreviewsTypesList));
		
		segmentHour = parseMilliseconds(segmentHourValue, GenericDefaults.SEGMENT_HOUR);
		segmentDay = parseMilliseconds(segmentDayValue, GenericDefaults.SEGMENT_DAY);
		segmentMonth = parseMilliseconds(segmentMonthValue, GenericDefaults.SEGMENT_MONTH);
		segmentYear = parseMilliseconds(segmentYearValue, GenericDefaults.SEGMENT_YEAR);
		segmentAlltime = parseMilliseconds(segmentAlltimeValue, GenericDefaults.SEGMENT_ALLTIME);
		
		feedbackType = PyUtils.getFeedbackType(feedbackTypeValue, GenericDefaults.FEEDBACK_FEEDBACK_TYPE);
		feedbackState = PyUtils.getFeedbackState(feedbackStateValue, GenericDefaults.FEEDBACK_FEEDBACK_STATE);
		feedbackContext = PyUtils.getFeedbackContext(feedbackContextValue, GenericDefaults.FEEDBACK_FEEDBACK_CONTEXT);
		
		restrictedType = PyUtils.getRestrictedType(restrictedTypeValue, GenericDefaults.RESTRICTED_RESTRICTED_TYPE);
		
		flagType = PyUtils.getFlagType(flagTypeValue, GenericDefaults.FLAG_DATA_TYPE);
		
		setAdminActionType(PyUtils.getAdminType(adminActionTypeValue, GenericDefaults.ADMIN_ACTION_TYPE));
		setAdminActionState(PyUtils.getAdminState(adminActionStateValue, GenericDefaults.ADMIN_ACTION_STATE));
		
		parseSettings();
	}

	private void parseSettings() {
		settingsMap.put(SETTING_OPTION.ALLOW_PROFILE_COMMENTS.toString(), 
				parseBoolean(settingsAllowProfileComments, 
						SettingsDefaults.ALLOW_PROFILE_COMMENTS));
		settingsMap.put(SETTING_OPTION.ALLOW_WARNING_CONTENT.toString(), 
				parseBoolean(settingsAllowWarningContent,
						SettingsDefaults.ALLOW_WARNING_CONTENT));
		settingsMap.put(SETTING_OPTION.HIDE_USER_PROFILE.toString(), 
				parseBoolean(settingsHideUserProfile,
						SettingsDefaults.HIDE_USER_PROFILE));
	}
	
	private boolean parseBoolean(String str, boolean defaultValue) {
		try {
			if(str == null || str.isEmpty()) {
				return defaultValue;
			}
			return Boolean.parseBoolean(str);
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
	private Boolean parseNullPermittedBoolean(String str, Boolean defaultValue) {
		try {
			if(str == null || str.isEmpty()) {
				return defaultValue;
			}
			return Boolean.parseBoolean(str);
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
	private long parseMilliseconds(String str, long defaultValue) {
		try {
			if(str == null || str.isEmpty()) {
				return defaultValue;
			}
			return Long.parseLong(str);
		} catch(Exception e) {
			return defaultValue;
		}
	}

	private Integer parseInteger(String str, int defaultValue) {
		try {
			if(str == null || str.isEmpty()) {
				return defaultValue;
			}
			return Integer.parseInt(str);
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
	private String parseString(String str, String defaultValue) {
		try {
			if(str == null || str.isEmpty()) {
				return defaultValue;
			}
			return str;
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
	private List<String> parseStringList(String str, List<String> defaultValue) {
		try {
			if(str == null || str.isEmpty()) {
				return defaultValue;
			}
			if(str.contains(",")) {
				return Arrays.asList(str.split(","));
			} else {
				return Arrays.asList(str);
			}
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
	private <T extends Enum<T> > List<T> parseEnumList(String str, 
			List<T> defaultValue, Class<T> type) {
		try {
			if(str == null || str.isEmpty()) {
				return defaultValue;
			}
			List<String> list = new ArrayList<String>();
			List<T> events = new ArrayList<T>();
			if(str.contains(",")) {
				list = Arrays.asList(str.split(","));
			} else {
				list = Arrays.asList(str);
			}
			
			for(String s : list) {
				try {
					events.add(Enum.valueOf(type, s));
				} catch(Exception e) {
					// do nothing, its a bogus one
				}
			}
			return events;
		} catch(Exception e) {
			return defaultValue;
		}
	}
	
	private TIME_OPTION parseTime(String time, String defaultValue) {
		// all else fails, default is hour
		TIME_OPTION def = PyUtils.convertTimeOption(defaultValue, TIME_OPTION.HOUR);
		return PyUtils.convertTimeOption(time, def);
		
	}
	
	private SORT_OPTION parseSort(String sort, String defaultValue) {
		// all else fails, default is hour
		SORT_OPTION def = PyUtils.convertSortOption(defaultValue, SORT_OPTION.VALUE);
		return PyUtils.convertSortOption(sort, def);
		
	}
	
	public Integer getPage() {
		return page;
	}

	public Integer getSize() {
		return size;
	}
	
	public Integer getTagsSearchCount() {
		return tagsSearchCount;
	}
	
	public List<String> getTagsList() {
		return tagsList;
	}
	
	public SORT_OPTION getSort() {
		return sort;
	}
	
	public TIME_OPTION getTime() {
		return time;
	}

	public long getSegmentHour() {
		return segmentHour;
	}

	public long getSegmentDay() {
		return segmentDay;
	}

	public long getSegmentMonth() {
		return segmentMonth;
	}

	public long getSegmentYear() {
		return segmentYear;
	}

	public long getSegmentAlltime() {
		return segmentAlltime;
	}

	public List<String> getFolloweeList() {
		return followeeList;
	}

	public void setFolloweeList(List<String> followeeList) {
		this.followeeList = followeeList;
	}

	public List<EVENT_TYPE> getFeedEventsList() {
		return feedEventsList;
	}

	public void setFeedEventsList(List<EVENT_TYPE> feedEventsList) {
		this.feedEventsList = feedEventsList;
	}

	public List<EVENT_TYPE> getNotificationsEventsList() {
		return notificationsEventsList;
	}

	public void setNotificationsEventsList(List<EVENT_TYPE> notificationsEventsList) {
		this.notificationsEventsList = notificationsEventsList;
	}

	public List<String> getFeedEventsStringList() {
		return feedEventsStringList;
	}

	public void setFeedEventsStringList(List<String> feedEventsStringList) {
		this.feedEventsStringList = feedEventsStringList;
	}

	public List<String> getNotificationsEventsStringList() {
		return notificationsEventsStringList;
	}

	public void setNotificationsEventsStringList(
			List<String> notificationsEventsStringList) {
		this.notificationsEventsStringList = notificationsEventsStringList;
	}
	
	public List<COMMENT_TYPE> getCommentsPreviewsTypesList() {
		return commentsPreviewsTypesList;
	}

	public void setCommentsPreviewsTypesList(
			List<COMMENT_TYPE> commentsPreviewsTypesList) {
		this.commentsPreviewsTypesList = commentsPreviewsTypesList;
	}

	public List<String> getCommentsPreviewsTypesStringList() {
		return commentsPreviewsTypesStringList;
	}

	public void setCommentsPreviewsTypesStringList(
			List<String> commentsPreviewsTypesStringList) {
		this.commentsPreviewsTypesStringList = commentsPreviewsTypesStringList;
	}
	
	public Settings getSettings() {
		Settings settings = new Settings();
		settings.setOptions(Collections.unmodifiableMap(settingsMap));
		settings.setLanguage(this.language);
		settings.setInterfaceLanguage(this.language);
		return settings;
	}

	public Boolean getWarning() {
		return warning;
	}

	public void setWarning(Boolean warning) {
		this.warning = warning;
	}

	public String getWarningContentReplacement() {
		return warningContentReplacement;
	}

	public void setWarningContentReplacement(String warningContentReplacement) {
		this.warningContentReplacement = warningContentReplacement;
	}

	public FEEDBACK_TYPE getFeedbackType() {
		return feedbackType;
	}

	public void setFeedbackType(FEEDBACK_TYPE feedbackType) {
		this.feedbackType = feedbackType;
	}

	public FEEDBACK_STATE getFeedbackState() {
		return feedbackState;
	}

	public void setFeedbackState(FEEDBACK_STATE feedbackState) {
		this.feedbackState = feedbackState;
	}

	public FEEDBACK_CONTEXT getFeedbackContext() {
		return feedbackContext;
	}

	public void setFeedbackContext(FEEDBACK_CONTEXT feedbackContext) {
		this.feedbackContext = feedbackContext;
	}
	
	public RESTRICTED_TYPE getRestrictedType() {
		return restrictedType;
	}
	
	public void setRestrictedType(RESTRICTED_TYPE restrictedType) {
		this.restrictedType = restrictedType;
	}
	
	public FLAG_TYPE getFlagType() {
		return flagType;
	}
	
	public void setFlagType(FLAG_TYPE flagType) {
		this.flagType = flagType;
	}
	
	public Integer getDirection() {
		return direction;
	}

	public void setDirection(Integer direction) {
		this.direction = direction;
	}

	public String getWarningDescriptionReplacement() {
		return warningDescriptionReplacement;
	}

	public void setWarningDescriptionReplacement(
			String warningDescriptionReplacement) {
		this.warningDescriptionReplacement = warningDescriptionReplacement;
	}

	public String getWarningAppreciationResponseReplacement() {
		return warningAppreciationResponseReplacement;
	}

	public void setWarningAppreciationResponseReplacement(
			String warningAppreciationResponseReplacement) {
		this.warningAppreciationResponseReplacement = warningAppreciationResponseReplacement;
	}

	public ADMIN_TYPE getAdminActionType() {
		return adminActionType;
	}

	public void setAdminActionType(ADMIN_TYPE adminActionType) {
		this.adminActionType = adminActionType;
	}

	public ADMIN_STATE getAdminActionState() {
		return adminActionState;
	}

	public void setAdminActionState(ADMIN_STATE adminActionState) {
		this.adminActionState = adminActionState;
	}

	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public List<CachedUsername> getFolloweeCachedUsernameList() {
		return followeeCachedUsernameList;
	}

	public void setFolloweeCachedUsernameList(
			List<CachedUsername> followeeCachedUsernameList) {
		this.followeeCachedUsernameList = followeeCachedUsernameList;
	}
	
	

}
