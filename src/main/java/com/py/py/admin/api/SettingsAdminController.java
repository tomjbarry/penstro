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
import com.py.py.domain.User;
import com.py.py.dto.APIResponse;
import com.py.py.dto.out.SettingsDTO;
import com.py.py.service.UserService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;

@Controller
@Profile({ProfileNames.ADMIN})
public class SettingsAdminController extends BaseAdminController {


	@Autowired
	protected UserService userService;
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_SETTINGS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_SETTINGS, method = RequestMethod.GET)
	public APIResponse<SettingsDTO> settings(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid) throws Exception {
		User user = getUser(p, atid);
		return Success(userService.getSettingsDTO(user, getUserInfo(user)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_SETTINGS_RESET + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_SETTINGS, method = RequestMethod.DELETE)
	public APIResponse<SettingsDTO> resetSettings(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid, 
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language) 
					throws Exception {
		userService.resetSettings(getUserId(p, atid), constructLanguage(language));
		return Success();
	}
}
