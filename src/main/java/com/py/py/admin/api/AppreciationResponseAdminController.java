package com.py.py.admin.api;

import java.security.Principal;

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
import com.py.py.dto.in.ChangeAppreciationResponseDTO;
import com.py.py.dto.out.AppreciationResponseDTO;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeAppreciationResponseValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class AppreciationResponseAdminController extends BaseAdminController {
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeAppreciationResponseValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_APPRECIATION_RESPONSE_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_APPRECIATION_RESPONSE, method = RequestMethod.POST)
	public APIResponse<DTO> changeAppreciationResponse(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@Validated @RequestBody ChangeAppreciationResponseDTO dto) throws Exception {
		userService.changeAppreciationResponse(getUserId(p, atid), dto);
		return Success();
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_USER_APPRECIATION_RESPONSE_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_USERS_APPRECIATION_RESPONSE, method = RequestMethod.GET)
	public APIResponse<AppreciationResponseDTO> viewAppreciationResponse(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid) 
			throws Exception {
		return Success(userService.getAppreciationResponseDTOSelf(getUserInfo(getUser(p, atid))));
	}
}
