package com.py.py.admin.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIAdminUrls;
import com.py.py.constants.ParamNames;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.admin.AdminActionDTO;
import com.py.py.dto.out.admin.CacheStatisticsDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.StatisticsService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;

@Controller
@Profile({ProfileNames.ADMIN})
public class GeneralAdminController extends BaseAdminController {

	@Autowired
	protected AuthenticationService authService;
	
	@Autowired
	protected StatisticsService statisticsService;
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_CHECK + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_CHECK, method = RequestMethod.GET)
	public APIResponse<DTO> confirmed(Principal p) throws Exception {
		authService.adminCheck(getUser(p));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_STATISTICS_CACHE_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_STATISTICS_CACHE, method = RequestMethod.GET)
	public APIResponse<CacheStatisticsDTO> cacheStats(Principal p) throws Exception {
		return Success(statisticsService.getCacheStatisticsDTO());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_ACTIONS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_ACTIONS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, AdminActionDTO> actions(Principal p, 
			@RequestParam(value = ParamNames.USER, required = false) String username,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.ADMIN_TYPE, required = false) String type,
			@RequestParam(value = ParamNames.ADMIN_STATE, required = false) String state,
			@RequestParam(value = ParamNames.TARGET, required = false) String target,
			@RequestParam(value = ParamNames.DIRECTION, required = false) String direction) 
					throws Exception {
		return Success(adminService.getAdminDTOs(getUserId(username), 
				constructAdminState(state), constructAdminType(type), target, 
				constructPageable(page, size), constructDirection(direction)));
	}
	
}
