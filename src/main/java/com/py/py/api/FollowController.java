package com.py.py.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.FollowDTO;
import com.py.py.dto.out.SubscriptionDTO;
import com.py.py.service.FollowService;
import com.py.py.service.constants.PermissionNames;

@Controller
public class FollowController extends BaseController {

	@Autowired
	protected FollowService followService;

	// having separate endpoints for viewing self and other followers/followees allows
	// to enable/disable based on permissions
	@PreAuthorize("hasAuthority('" + PermissionNames.FOLLOWEES_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FOLLOWEES, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FollowDTO> followees(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(followService.getFolloweeDTOs(getUserId(p), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FOLLOWEES_USER_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USER_FOLLOWEES, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FollowDTO> userFollowees(
			@PathVariable(PathVariables.USER_ID) String username,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(followService.getFolloweeDTOs(getUserId(username), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FOLLOWERS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FOLLOWERS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FollowDTO> followers(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(followService.getFollowerDTOs(getUserId(p), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FOLLOWERS_USER_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USER_FOLLOWERS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FollowDTO> userFollowers(
			@PathVariable(PathVariables.USER_ID) String username,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(followService.getFollowerDTOs(getUserId(username), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FOLLOWER_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FOLLOWERS_ID, method = RequestMethod.GET)
	public APIResponse<FollowDTO> follower(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(followService.getFollowerDTO(getUserId(p), getUserId(username)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FOLLOWEE_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FOLLOWEES_ID, method = RequestMethod.GET)
	public APIResponse<FollowDTO> followee(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(followService.getFolloweeDTO(getUserId(p), getUserId(username)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FOLLOWEE_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FOLLOWEES_ID, method = RequestMethod.POST)
	public APIResponse<FollowDTO> addFollowee(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		followService.addFollowee(getUser(p), getUser(username));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FOLLOWEE_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FOLLOWEES_ID, method = RequestMethod.DELETE)
	public APIResponse<FollowDTO> removeFollowee(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		String constructedUsername = constructUsername(username);
		followService.removeFollowee(getUser(p), 
				getUserOrNull(constructedUsername), 
				constructedUsername);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.SUBSCRIPTION_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.SUBSCRIPTION, method = RequestMethod.GET)
	public APIResponse<SubscriptionDTO> subscription(Principal p) throws Exception {
		return Success(followService.getSubscription(getUserId(p)));
	}
}
