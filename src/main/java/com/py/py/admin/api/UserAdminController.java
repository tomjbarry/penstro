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
import com.py.py.domain.User;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.ChangeProfileDTO;
import com.py.py.dto.out.CurrentUserDTO;
import com.py.py.dto.out.UserDTO;
import com.py.py.service.AdminService;
import com.py.py.service.AuthenticationService;
import com.py.py.service.UserService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeProfileValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class UserAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected AuthenticationService authenticationService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeProfileValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_CURRENT_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_CURRENT, method = RequestMethod.GET)
	public APIResponse<CurrentUserDTO> currentUser(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid) throws Exception {
		User user = getUser(p, atid);
		return Success(authenticationService.getCurrentUserDTO(user, getUserInfo(user)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_LOGIN_ATTEMPTS_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_LOGIN_ATTEMPTS, method = RequestMethod.DELETE)
	public APIResponse<DTO> clearLoginAttempts(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		adminService.clearLoginAttempts(getUser(p), getUserId(username));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_UNDELETE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_DELETE, method = RequestMethod.POST)
	public APIResponse<DTO> undeleteUser(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid) throws Exception {
		authenticationService.undelete(getUser(p, atid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_PROFILE_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_PROFILE, method = RequestMethod.GET)
	public APIResponse<UserDTO> viewProfile(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid) throws Exception {
		return Success(userService.getUserDTOSelf(getUserInfo(getUser(p, atid))));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_PROFILE_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_PROFILE, method = RequestMethod.POST)
	public APIResponse<DTO> changeProfile(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@Validated @RequestBody ChangeProfileDTO dto) throws Exception {
		userService.changeProfile(getUserId(p, atid), dto);
		return Success();
	}
	
}
