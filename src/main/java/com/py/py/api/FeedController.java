package com.py.py.api;

import java.security.Principal;
import java.util.List;

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
import com.py.py.dto.DTO;
import com.py.py.dto.out.FeedDTO;
import com.py.py.service.FollowService;
import com.py.py.service.constants.PermissionNames;

@Controller
public class FeedController extends BaseController {

	@Autowired
	protected FollowService followService;
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FEED_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FEED, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FeedDTO> feed(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.EVENT, required = false) List<String> event) throws Exception {
		return Success(followService.getFeedDTOs(getUserId(p), 
				constructEventTypes(event), constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_ACTIVITY_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_ACTIVITY, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FeedDTO> userActivity(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.EVENT, required = false) List<String> event) throws Exception {
		return Success(followService.getUserFeedDTOs(getUserId(username), 
				constructEventTypes(event), constructPageable(page, size)));
	}
}
