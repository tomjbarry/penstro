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
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIAdminUrls;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.admin.ChangeEmailAdminDTO;
import com.py.py.service.AdminService;
import com.py.py.service.AuthenticationService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeEmailAdminValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class EmailAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@Autowired
	protected AuthenticationService authService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeEmailAdminValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_EMAIL_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_EMAIL_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changeEmail(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@Validated @RequestBody ChangeEmailAdminDTO dto)
					throws Exception {
		adminService.changeEmail(getUser(p), getUserId(username), dto);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_EMAIL_CHANGE_REQUEST + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_EMAIL_CHANGE_REQUEST, method = RequestMethod.POST)
	public APIResponse<DTO> sendEmailChange(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid)
					throws Exception {
		authService.changeEmailRequest(getUser(p, atid));
		return Success();
	}
}
