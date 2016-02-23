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
import com.py.py.dto.in.admin.ChangeRolesDTO;
import com.py.py.dto.out.RoleSetDTO;
import com.py.py.service.AdminService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeRolesValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class RolesAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeRolesValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_ROLES_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_ROLES, method = RequestMethod.POST)
	public APIResponse<DTO> changeRoles(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@Validated @RequestBody ChangeRolesDTO dto) throws Exception {
		adminService.setRoles(getUser(p), getUserId(username), dto);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_ROLES_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_ROLES, method = RequestMethod.GET)
	public APIResponse<RoleSetDTO> getRoles(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String username) throws Exception {
		return Success(adminService.getUserRoleSetDTO(getUser(username)));
	}
}
