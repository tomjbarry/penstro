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
import com.py.py.dto.in.admin.LockUserDTO;
import com.py.py.service.AdminService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.LockUserValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class LockUserController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new LockUserValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_UNLOCK + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_UNLOCK, method = RequestMethod.POST)
	public APIResponse<DTO> enable(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		adminService.unlockUser(getUser(p), getUserId(username));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_LOCK + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_LOCK, method = RequestMethod.POST)
	public APIResponse<DTO> disable(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@Validated @RequestBody LockUserDTO dto) throws Exception {
		adminService.lockUser(getUser(p), getUserId(username), dto);
		return Success();
	}
}
