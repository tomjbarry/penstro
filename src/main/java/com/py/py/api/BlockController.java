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
import com.py.py.service.FollowService;
import com.py.py.service.constants.PermissionNames;

@Controller
public class BlockController extends BaseController {
	
	@Autowired
	protected FollowService followService;
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BLOCKEDS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BLOCKED, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FollowDTO> blockedList(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(followService.getBlockedDTOs(getUserId(p), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BLOCKED_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BLOCKED_ID, method = RequestMethod.GET)
	public APIResponse<FollowDTO> blocked(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(followService.getBlockedDTO(getUserId(p), getUserId(username)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BLOCKED_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BLOCKED_ID, method = RequestMethod.POST)
	public APIResponse<FollowDTO> addBlocked(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		followService.addBlocked(getUser(p), getUser(username));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BLOCKED_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BLOCKED_ID, method = RequestMethod.DELETE)
	public APIResponse<FollowDTO> removeBlocked(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		followService.removeBlocked(getUserId(p), constructUsername(username));
		return Success();
	}
}
