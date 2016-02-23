package com.py.py.admin.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIAdminUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.FollowDTO;
import com.py.py.service.FollowService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;

@Controller
@Profile({ProfileNames.ADMIN})
public class BlockAdminController extends BaseAdminController {
	
	@Autowired
	protected FollowService followService;
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BLOCKEDS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BLOCKED, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FollowDTO> blockedList(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size)
					throws Exception {
		return Success(followService.getBlockedDTOs(getUserId(p, atid), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BLOCKED_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BLOCKED_ID, method = RequestMethod.GET)
	public APIResponse<FollowDTO> blocked(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(followService.getBlockedDTO(getUserId(p, atid), getUserId(username)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BLOCKED_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BLOCKED_ID, method = RequestMethod.POST)
	public APIResponse<FollowDTO> addBlocked(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		followService.addBlocked(getUser(p, atid), getUser(username));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BLOCKED_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BLOCKED_ID, method = RequestMethod.DELETE)
	public APIResponse<FollowDTO> removeBlocked(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		followService.removeBlocked(getUserId(p, atid), constructUsername(username));
		return Success();
	}
}
