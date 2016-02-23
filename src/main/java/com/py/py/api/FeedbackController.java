package com.py.py.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ResponseCodes;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.SubmitFeedbackDTO;
import com.py.py.service.FeedbackService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.SubmitFeedbackValidator;

@Controller
public class FeedbackController extends BaseController {

	@Autowired
	protected FeedbackService feedbackService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new SubmitFeedbackValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FEEDBACK_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FEEDBACKS, method = RequestMethod.POST)
	public APIResponse<DTO> bug(Principal p,
			@RequestBody @Validated SubmitFeedbackDTO dto) throws Exception{
		feedbackService.createFeedback(getUser(p), dto);
		return Success(ResponseCodes.CREATED);
	}
}
