package com.py.py.api;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

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
import com.py.py.constants.ResponseCodes;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.RegisterUserDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.RegisterUserValidator;

@Controller
public class AuthenticationController extends BaseController {

	@Autowired
	protected AuthenticationService authService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new RegisterUserValidator());
	}
	
	@ResponseBody
	@RequestMapping(value = APIUrls.DENIED)
	public APIResponse<DTO> denied() {
		return Failure(ResponseCodes.DENIED);
	}
	
	@ResponseBody
	@RequestMapping(value = APIUrls.LOGIN_LOCKED)
	public APIResponse<DTO> loginLocked() {
		return Failure(ResponseCodes.LOGIN_LOCKED);
	}
	
	@ResponseBody
	@RequestMapping(value = APIUrls.LOCKED)
	public APIResponse<DTO> locked() {
		return Failure(ResponseCodes.LOCKED);
	}

	@ResponseBody
	@RequestMapping(value = APIUrls.EXPIRED)
	public APIResponse<DTO> expired() {
		return Failure(ResponseCodes.EXPIRED);
	}

	@ResponseBody
	@RequestMapping(value = APIUrls.THEFT)
	public APIResponse<DTO> theft() {
		return Failure(ResponseCodes.THEFT);
	}
	
	@ResponseBody
	@RequestMapping(value = APIUrls.LOGIN_SUCCESS)
	public APIResponse<DTO> loginSuccess() {
		// should not get here! custom response should be sent elsewhere
		return Failure();
	}
	
	@ResponseBody
	@RequestMapping(value = APIUrls.LOGOUT_SUCCESS)
	public APIResponse<DTO> logoutSuccess() {
		return Success();
	}
	
	@ResponseBody
	@RequestMapping(value = APIUrls.LOGOUT_FAILURE)
	public APIResponse<DTO> logoutFailure() {
		return Failure(ResponseCodes.DENIED);
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.REGISTER + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.REGISTER, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> register(HttpServletRequest request,
			Principal p, 
			@Validated @RequestBody RegisterUserDTO dto, 
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language) 
					throws Exception {
		return Success(ResponseCodes.CREATED, 
				authService.registerUser(dto, constructLanguage(language), constructIpAddress(request), true));
	}
	
}
