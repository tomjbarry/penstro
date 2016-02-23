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
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.ChangePaymentDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.ChangePaymentValidator;

@Controller
public class ChangePaymentController extends BaseController {

	@Autowired
	protected AuthenticationService authenticationService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangePaymentValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.PAYMENT_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PAYMENT_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changePayment(Principal p,
			@Validated @RequestBody ChangePaymentDTO dto,
			@RequestParam(value = ParamNames.EMAIL_TOKEN, required = false) String emailToken)
					throws Exception {
		authenticationService.changePaymentId(getUser(p), dto, 
				constructEmailToken(emailToken));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.PAYMENT_CHANGE_REQUEST + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PAYMENT_CHANGE_REQUEST, method = RequestMethod.POST)
	public APIResponse<DTO> changePaymentRequest(Principal p)throws Exception {
		authenticationService.sendPaymentIdChange(getUser(p));
		return Success();
	}
	
}
