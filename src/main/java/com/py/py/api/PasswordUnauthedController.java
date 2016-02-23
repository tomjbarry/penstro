package com.py.py.api;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.ChangePasswordUnauthedDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.ChangePasswordUnauthedValidator;

@Controller
public class PasswordUnauthedController extends BaseController {


	@Autowired
	protected AuthenticationService authService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangePasswordUnauthedValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.PASSWORD_UNAUTHED_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PASSWORD_UNAUTHED_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changePassword(
			@PathVariable(PathVariables.USER_ID) String username,
			@Validated @RequestBody ChangePasswordUnauthedDTO dto,
			@RequestParam(value = ParamNames.EMAIL_TOKEN, required = false) String emailToken)
		throws Exception {
		authService.changePasswordUnauthed(getUser(username),
				dto, constructEmailToken(emailToken));
		return Success();
	}
}
