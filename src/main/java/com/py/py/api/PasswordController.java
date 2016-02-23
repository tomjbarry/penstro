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
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.ChangePasswordDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.ChangePasswordValidator;

@Controller
public class PasswordController extends BaseController {

	@Autowired
	protected AuthenticationService authService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangePasswordValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.PASSWORD_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PASSWORD_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changePassword(Principal p,
			@Validated @RequestBody ChangePasswordDTO dto) throws Exception {
		authService.changePassword(getUser(p), dto);
		return Success();
	}
}
