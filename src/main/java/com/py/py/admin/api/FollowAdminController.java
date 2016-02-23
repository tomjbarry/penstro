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
import com.py.py.dto.out.SubscriptionDTO;
import com.py.py.service.FollowService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;

@Controller
@Profile({ProfileNames.ADMIN})
public class FollowAdminController extends BaseAdminController {

	@Autowired
	protected FollowService followService;
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FOLLOWEES_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FOLLOWEES, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FollowDTO> followees(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(followService.getFolloweeDTOs(getUserId(p, atid), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FOLLOWERS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FOLLOWERS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FollowDTO> followers(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(followService.getFollowerDTOs(getUserId(p, atid), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FOLLOWER_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FOLLOWERS_ID, method = RequestMethod.GET)
	public APIResponse<FollowDTO> follower(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(followService.getFollowerDTO(getUserId(p, atid), 
				getUserId(username)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FOLLOWEE_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FOLLOWEES_ID, method = RequestMethod.GET)
	public APIResponse<FollowDTO> followee(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(followService.getFolloweeDTO(getUserId(p, atid), 
				getUserId(username)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FOLLOWEE_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FOLLOWEES_ID, method = RequestMethod.POST)
	public APIResponse<FollowDTO> addFollowee(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		followService.addFollowee(getUser(p, atid), getUser(username));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FOLLOWEE_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FOLLOWEES_ID, method = RequestMethod.DELETE)
	public APIResponse<FollowDTO> removeFollowee(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		String constructedUsername = constructUsername(username);
		followService.removeFollowee(getUser(p, atid), 
				getUserOrNull(constructedUsername), constructedUsername);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_SUBSCRIPTION_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_SUBSCRIPTION, method = RequestMethod.GET)
	public APIResponse<SubscriptionDTO> subscription(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid) throws Exception {
		return Success(followService.getSubscription(getUserId(p, atid)));
	}
}
