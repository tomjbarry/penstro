package com.py.py.admin.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIAdminUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.admin.ChangeFeedbackDTO;
import com.py.py.dto.out.admin.FeedbackDTO;
import com.py.py.service.FeedbackService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeFeedbackValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class FeedbackAdminController extends BaseAdminController {

	@Autowired
	protected FeedbackService feedbackService;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeFeedbackValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FEEDBACK_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FEEDBACKS_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changeFeedback(Principal p,
			@Validated @RequestBody ChangeFeedbackDTO dto)
					throws Exception {
		feedbackService.updateFeedback(dto);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FEEDBACKS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FEEDBACKS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FeedbackDTO> feedbacks(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.FEEDBACK_TYPE, required = false) String type,
			@RequestParam(value = ParamNames.FEEDBACK_STATE, required = false) String state,
			@RequestParam(value = ParamNames.FEEDBACK_CONTEXT, required = false) String context,
			@RequestParam(value = ParamNames.USER, required = false) String username,
			@RequestParam(value = ParamNames.DIRECTION, required = false) String direction)
					throws Exception {
		return Success(feedbackService.getFeedbackDTOs(
				constructFeedbackType(type), constructFeedbackState(state), 
				constructFeedbackContext(context), getUserIdOrNull(username), 
				constructPageable(page, size), constructDirection(direction)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FEEDBACK_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FEEDBACKS_ID, method = RequestMethod.GET)
	public APIResponse<FeedbackDTO> feedback(Principal p,
			@PathVariable(PathVariables.FEEDBACK_ID) String fid) 
					throws Exception {
		return Success(feedbackService.getFeedbackDTO(getFeedback(fid)));
	}
}

