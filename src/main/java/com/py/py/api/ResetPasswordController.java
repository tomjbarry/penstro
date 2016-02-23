package com.py.py.api;

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
import com.py.py.domain.User;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.ResetPasswordDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.ResetPasswordValidator;

@Controller
public class ResetPasswordController extends BaseController {

	@Autowired
	protected AuthenticationService authService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ResetPasswordValidator());
	}
	
	protected User getDTOUser(ResetPasswordDTO dto) {
		return getUserByEmailOrNull(dto.getEmail());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.PASSWORD_RESET + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PASSWORD_RESET, method = RequestMethod.POST)
	public APIResponse<DTO> resetPassword(@Validated @RequestBody ResetPasswordDTO dto)
			throws Exception {
		authService.resetPassword(getDTOUser(dto));
		return Success();
	}
}
