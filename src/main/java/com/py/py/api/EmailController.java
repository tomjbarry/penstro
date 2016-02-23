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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.domain.User;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.ChangeEmailDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.ChangeEmailValidator;

@Controller
public class EmailController extends BaseController {

	@Autowired
	protected AuthenticationService authService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeEmailValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.EMAIL_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.EMAIL_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changeEmail(Principal p,
			@Validated @RequestBody ChangeEmailDTO dto,
			@RequestParam(value = ParamNames.EMAIL_TOKEN, required = false) String emailToken)
					throws Exception {
		User user = getUser(p);
		authService.changeEmail(user, getUserInfo(user), dto, constructEmailToken(emailToken));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.EMAIL_PENDING_ACTION_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.EMAIL_PENDING_ACTION_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changeEmailPendingAction(Principal p,
			@Validated @RequestBody ChangeEmailDTO dto)
					throws Exception {
		User user = getUser(p);
		authService.changeEmailPendingAction(user, getUserInfo(user), dto);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.EMAIL_CONFIRMATION_SEND + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.CONFIRMATION_SEND, method = RequestMethod.POST)
	public APIResponse<DTO> sendConfirmation(Principal p) throws Exception {
		User user = getUser(p);
		authService.sendConfirmation(user, getUserInfo(user));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.EMAIL_CONFIRMATION_CONFIRM + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.CONFIRMATION, method = RequestMethod.POST)
	public APIResponse<DTO> confirmation(Principal p, 
			@RequestParam(value = ParamNames.EMAIL_TOKEN, required = false) String emailToken)
				throws Exception {
		User user = getUser(p);
		authService.confirmation(user, getUserInfo(user), constructEmailToken(emailToken));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.EMAIL_CHANGE_REQUEST + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.EMAIL_CHANGE_REQUEST, method = RequestMethod.POST)
	public APIResponse<DTO> changeEmail(Principal p)
					throws Exception {
		authService.changeEmailRequest(getUser(p));
		return Success();
	}
}
