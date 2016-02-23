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
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.ChangeAppreciationResponseDTO;
import com.py.py.dto.out.AppreciationResponseDTO;
import com.py.py.service.UserService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.ChangeAppreciationResponseValidator;

@Controller
public class AppreciationResponseController extends BaseController {

	@Autowired
	protected UserService userService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeAppreciationResponseValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.USER_APPRECIATION_RESPONSE_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.APPRECIATION_RESPONSE, method = RequestMethod.POST)
	public APIResponse<DTO> changeAppreciationResponse(Principal p,
			@Validated @RequestBody ChangeAppreciationResponseDTO dto) throws Exception {
		userService.changeAppreciationResponse(getUserId(p), dto);
		return Success();
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.USER_APPRECIATION_RESPONSE_SELF_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.APPRECIATION_RESPONSE, method = RequestMethod.GET)
	public APIResponse<AppreciationResponseDTO> appreciationResponse(Principal p) 
			throws Exception {
		return Success(userService.getAppreciationResponseDTOSelf(getUserInfo(getUser(p))));
	}
}
