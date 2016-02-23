package com.py.py.api;

import java.security.Principal;

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
import com.py.py.domain.User;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.ChangeProfileDTO;
import com.py.py.dto.out.AppreciationResponseDTO;
import com.py.py.dto.out.CurrentUserDTO;
import com.py.py.dto.out.RoleSetDTO;
import com.py.py.dto.out.UserDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.UserService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.ChangeProfileValidator;

@Controller
public class UserController extends BaseController {
	
	@Autowired
	protected UserService userService;
	
	@Autowired
	protected AuthenticationService authenticationService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeProfileValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USERS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, UserDTO> users(Principal p, 
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
					@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
					@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
					@RequestParam(value = ParamNames.TIME, required = false) String time,
					@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
					@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview) 
							throws Exception {
		User user = getUserOrNull(p);
		return Success(userService.getUserPreviewDTOs(getUserInfoOrNull(user), constructLanguage(language), 
				constructPageable(page, size), constructWarning(warning), constructTime(time), constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_CURRENT_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_CURRENT, method = RequestMethod.GET)
	public APIResponse<CurrentUserDTO> currentUser(Principal p) throws Exception {
		User user = getUser(p);
		return Success(authenticationService.getCurrentUserDTO(user, getUserInfo(user)));
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.USER_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_ID, method = RequestMethod.GET)
	public APIResponse<UserDTO> usersId(Principal p, 
			@PathVariable(PathVariables.USER_ID) String username, 
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning) 
					throws Exception {
		User user = getUserOrNull(p);
		return Success(userService.getUserDTO(getUserInfoOrNull(user), getCachedUserInfo(getUser(username)), 
				constructWarning(warning)));
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.USER_APPRECIATION_RESPONSE_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_ID_APPRECIATION_RESPONSE, method = RequestMethod.GET)
	public APIResponse<AppreciationResponseDTO> usersIdAppreciationResponse(Principal p, 
			@PathVariable(PathVariables.USER_ID) String username, 
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning) 
					throws Exception {
		return Success(userService.getAppreciationResponseDTO(getUserInfo(getUser(p)), getCachedUserInfo(getUser(username)), 
				constructWarning(warning)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_ACCEPT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_ACCEPT, method = RequestMethod.POST)
	public APIResponse<DTO> accept(Principal p) throws Exception {
		authenticationService.accept(getUser(p));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_DELETE_SEND + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_DELETE_SEND, method = RequestMethod.POST)
	public APIResponse<DTO> sendDelete(Principal p) throws Exception {
		authenticationService.sendDelete(getUser(p));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_DELETE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_DELETE, method = RequestMethod.DELETE)
	public APIResponse<DTO> delete(Principal p,
			@RequestParam(value = ParamNames.EMAIL_TOKEN, required = false) String emailToken) throws Exception {
		authenticationService.delete(getUser(p), constructEmailToken(emailToken));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_UNDELETE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_DELETE, method = RequestMethod.POST)
	public APIResponse<DTO> undelete(Principal p) throws Exception {
		authenticationService.undelete(getUser(p));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_ROLES_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_ROLES, method = RequestMethod.GET)
	public APIResponse<RoleSetDTO> roles(Principal p) throws Exception {
		return Success(userService.getRoleSetDTO(getUser(p)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_FLAG + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_ID_FLAG, method = RequestMethod.POST)
	public APIResponse<DTO> flag(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@RequestParam(value = ParamNames.FLAG_REASON, required = false) String reason) throws Exception {
		User user = getUser(p);
		User targetUser = getUser(username);
		userService.flag(user, getUserInfo(user), targetUser, getUserInfo(targetUser), constructFlagReason(reason));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_PROFILE_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PROFILE, method = RequestMethod.POST)
	public APIResponse<DTO> changeProfile(Principal p,
			@Validated @RequestBody ChangeProfileDTO dto) throws Exception {
		userService.changeProfile(getUserId(p), dto);
		return Success();
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.USER_PROFILE_SELF_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PROFILE, method = RequestMethod.GET)
	public APIResponse<UserDTO> self(Principal p) 
			throws Exception {
		return Success(userService.getUserDTOSelf(getUserInfo(getUser(p))));
	}
}
