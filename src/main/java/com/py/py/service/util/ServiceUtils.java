package com.py.py.service.util;

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.bson.types.ObjectId;

import com.py.py.constants.HeaderNames;
import com.py.py.domain.subdomain.CachedUsername;
import com.py.py.domain.subdomain.TagId;
import com.py.py.service.exception.BadParameterException;
import com.py.py.util.PyUtils;
import com.py.py.validation.util.Validation;

public class ServiceUtils {

	public static String getIdString(ObjectId id) throws BadParameterException {
		if(id == null) {
			throw new BadParameterException();
		}
		return id.toHexString();
	}
	
	public static String getName(String name) throws BadParameterException {
		if(!Validation.validUsername(name)) {
			throw new BadParameterException();
		}
		return name;
	}
	
	public static boolean isUsernameDeleted(CachedUsername cu)  {
		if(cu == null || cu.getId() == null || !cu.isExists()) {
			return true;
		}
		return false;
	}
	
	public static String getName(Principal p) throws BadParameterException {
		ArgCheck.nullCheck(p);
		return getName(p.getName());
	}
	
	public static String getIdName(String n) throws BadParameterException {
		return getName(n).toLowerCase();
	}
	
	public static String getEmail(String email) throws BadParameterException {
		if(!Validation.validEmail(email)) {
			throw new BadParameterException();
		}
		return email;
	}
	
	public static String getPaymentId(String paymentId) throws BadParameterException {
		if(!Validation.validPaymentId(paymentId)) {
			throw new BadParameterException();
		}
		return paymentId;
	}
	
	public static String getUniqueEmail(String email) throws BadParameterException {
		return getEmail(email).toLowerCase();
	}
	
	public static String getCurrency(String currency) throws BadParameterException {
		if(!Validation.validCurrency(currency)) {
			throw new BadParameterException();
		}
		return currency;
	}
	
	public static String getContent(String content) throws BadParameterException {
		if(!Validation.validContent(content)) {
			throw new BadParameterException();
		}
		return content;
	}
	
	public static String getCommentContent(String commentContent) throws BadParameterException {
		if(!Validation.validCommentContent(commentContent)) {
			throw new BadParameterException();
		}
		return commentContent;
	}
	
	public static String getEdit(String edit) throws BadParameterException {
		if(!Validation.validEdit(edit)) {
			throw new BadParameterException();
		}
		return edit;
	}
	
	public static String getPreview(String preview) throws BadParameterException {
		if(!Validation.validPreview(preview)) {
			throw new BadParameterException();
		}
		return preview;
	}
	
	public static String getDescription(String description) throws BadParameterException {
		if(!Validation.validDescription(description)) {
			throw new BadParameterException();
		}
		return description;
	}
	
	public static String getAppreciationResponse(String appreciationResponse)
			throws BadParameterException {
		if(!Validation.validAppreciationResponse(appreciationResponse)) {
			throw new BadParameterException();
		}
		return appreciationResponse;
	}
	
	public static String getTitle(String title) throws BadParameterException {
		if(!Validation.validTitle(title)) {
			throw new BadParameterException();
		}
		return title;
	}
	
	public static String getMessage(String message) throws BadParameterException {
		if(!Validation.validMessage(message)) {
			throw new BadParameterException();
		}
		return message;
	}
	
	public static String getRole(String role) throws BadParameterException {
		String validRole = role.toLowerCase();
		if(!Validation.validRole(validRole)) {
			throw new BadParameterException();
		}
		return validRole;
	}
	
	public static String getOverrideRole(String overrideRole) throws BadParameterException {
		String validOverrideRole = overrideRole.toLowerCase();
		if(!Validation.validOverrideRole(validOverrideRole)) {
			throw new BadParameterException();
		}
		return validOverrideRole;
	}
	
	public static String getLanguage(String language) throws BadParameterException {
		if(!Validation.validLanguage(language)) {
			throw new BadParameterException();
		}
		return language;
	}
	
	public static String getLanguageOrNull(String language) throws BadParameterException {
		if(language == null) {
			return null;
		} else if(!Validation.validLanguage(language)) {
			throw new BadParameterException();
		}
		return language;
	}
	
	public static String getTag(String tag) throws BadParameterException {
		if(!Validation.validTag(tag)) {
			throw new BadParameterException();
		}
		String checked = PyUtils.getTag(tag);
		if(checked == null || checked.isEmpty()) {
			throw new BadParameterException();
		}
		return checked;
	}
	
	public static Map<String, Long> getTagMap(List<String> tags, long amount) throws BadParameterException {
		Map<String, Long> tagMap = new HashMap<String, Long>();
		if(tags == null) {
			return tagMap;
		}
		
		for(String tag : tags) {
			tagMap.put(getTag(tag), amount);
		}
		return tagMap;
	}
	
	public static Map<String, Long> getLimitedTags(Map<String, Long> tags, int limit) throws BadParameterException {
		Map<String, Long> limitedTags = new LinkedHashMap<String, Long>();
		if(tags == null) {
			limitedTags = new HashMap<String, Long>();
		} else if(tags.size() > limit) {
			List<Map.Entry<String, Long> > sortedTags = PyUtils.sortByValueDescending(tags);
			for(int i = 1; i < sortedTags.size(); i++) {
				if(i > limit) {
					break;
				}
				Map.Entry<String, Long> entry = sortedTags.get(sortedTags.size() - i);
				limitedTags.put(getTag(entry.getKey()), entry.getValue());
			}
		} else {
			limitedTags = tags;
		}
		return limitedTags;
	}
	
	public static String getWord(String word) throws BadParameterException {
		if(!Validation.validWord(word)) {
			throw new BadParameterException();
		}
		if(word == null) {
			return null;
		}
		return word.toLowerCase();
	}
	
	public static TagId getTagId(TagId id) throws BadParameterException {
		ArgCheck.nullCheck(id);
		if(!Validation.validTag(id.getName()) 
				|| !Validation.validLanguage(id.getLanguage())) {
			throw new BadParameterException();
		}
		id.setName(getTag(id.getName()));
		return id;
	}
	
	public static String getClientAddress(HttpServletRequest request) throws BadParameterException {
		ArgCheck.nullCheck(request);
		String address = request.getHeader(HeaderNames.LB_ORIGINAL_REMOTE_ADDRESS);
		if(address == null || address.isEmpty()) {
			address = request.getRemoteAddr();
		}
		
		ArgCheck.nullCheck(address);
		return address;
	}
	
}
