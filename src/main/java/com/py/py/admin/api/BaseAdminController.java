package com.py.py.admin.api;

import java.security.Principal;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;

import com.py.py.api.BaseController;
import com.py.py.domain.Comment;
import com.py.py.domain.Feedback;
import com.py.py.domain.Posting;
import com.py.py.domain.User;
import com.py.py.enumeration.ADMIN_STATE;
import com.py.py.enumeration.ADMIN_TYPE;
import com.py.py.enumeration.FEEDBACK_CONTEXT;
import com.py.py.enumeration.FEEDBACK_STATE;
import com.py.py.enumeration.FEEDBACK_TYPE;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.enumeration.RESTRICTED_TYPE;
import com.py.py.service.AdminService;
import com.py.py.service.FeedbackService;
import com.py.py.service.constants.ProfileNames;
import com.py.py.service.exception.BadParameterException;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.util.PyUtils;

@Controller
@Profile({ProfileNames.ADMIN})
public class BaseAdminController extends BaseController {

	@Autowired
	protected FeedbackService feedbackService;
	
	@Autowired
	protected AdminService adminService;
	
	public User getUser(Principal p, String username) 
			throws ServiceException {
		if(p == null) {
			throw new AccessDeniedException(username);
		}
		if(username != null) {
			return getUser(username);
		} else {
			throw new BadParameterException();
			//return getUser(p);
		}
	}
	
	public ObjectId getUserId(Principal p, String username) throws ServiceException {
		return getUserId(username);
	}
	
	public Posting getPostingAdmin(String postingId) throws ServiceException {
		ArgCheck.objectIdCheck(postingId);
		return adminService.getPosting(new ObjectId(postingId));
	}
	
	public ObjectId getPostingAdminId(String postingId) throws ServiceException {
		return getPostingAdmin(postingId).getId();
	}
	
	public Comment getCommentAdmin(String commentId) throws ServiceException {
		ArgCheck.objectIdCheck(commentId);
		return adminService.getComment(new ObjectId(commentId));
	}
	
	public ObjectId getCommentAdminId(String commentId) throws ServiceException {
		return getCommentAdmin(commentId).getId();
	}
	
	public Feedback getFeedback(String feedbackId) throws ServiceException {
		ArgCheck.objectIdCheck(feedbackId);
		return feedbackService.getFeedback(new ObjectId(feedbackId));
	}
	
	public ObjectId getFeedbackId(String feedbackId) throws ServiceException {
		return getFeedback(feedbackId).getId();
	}
	
	public FEEDBACK_TYPE constructFeedbackType(String type) {
		return PyUtils.getFeedbackType(type, defaultsFactory.getFeedbackType());
	}
	
	public FEEDBACK_STATE constructFeedbackState(String state) {
		return PyUtils.getFeedbackState(state, defaultsFactory.getFeedbackState());
	}
	
	public FEEDBACK_CONTEXT constructFeedbackContext(String context) {
		return PyUtils.getFeedbackContext(context, defaultsFactory.getFeedbackContext());
	}
	
	public ADMIN_TYPE constructAdminType(String type) {
		return PyUtils.getAdminType(type, defaultsFactory.getAdminActionType());
	}
	
	public ADMIN_STATE constructAdminState(String state) {
		return PyUtils.getAdminState(state, defaultsFactory.getAdminActionState());
	}
	
	public RESTRICTED_TYPE constructRestrictedType(String type) {
		return PyUtils.getRestrictedType(type, defaultsFactory.getRestrictedType());
	}
	
	public FLAG_TYPE constructFlagType(String type) {
		return PyUtils.getFlagType(type, defaultsFactory.getFlagType());
	}
}
