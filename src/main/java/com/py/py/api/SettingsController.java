package com.py.py.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.domain.User;
import com.py.py.dto.APIResponse;
import com.py.py.dto.in.ChangeSettingsDTO;
import com.py.py.dto.out.SettingsDTO;
import com.py.py.service.UserService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.ChangeSettingsValidator;

@Controller
public class SettingsController extends BaseController {

	@Autowired
	protected UserService userService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeSettingsValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.SETTINGS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.SETTINGS, method = RequestMethod.GET)
	public APIResponse<SettingsDTO> settings(Principal p) throws Exception {
		User user = getUser(p);
		return Success(userService.getSettingsDTO(user, getUserInfo(user)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.SETTINGS_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.SETTINGS, method = RequestMethod.POST)
	public APIResponse<SettingsDTO> changeSettings(Principal p,
			@RequestBody @Validated ChangeSettingsDTO dto) throws Exception {
		userService.changeSettings(getUserId(p), dto);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.SETTINGS_RESET + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.SETTINGS, method = RequestMethod.DELETE)
	public APIResponse<SettingsDTO> resetSettings(Principal p, 
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language) 
					throws Exception {
		userService.resetSettings(getUserId(p), 
				constructLanguage(language));
		return Success();
	}
}
