package com.py.py.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import com.py.py.service.UserService;
import com.py.py.validation.SaveLocationValidator;

@Controller
public class LocationController extends BaseController {

	@Autowired
	protected UserService userService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new SaveLocationValidator());
	}
	/*
	@PreAuthorize("hasAuthority('" + PermissionNames.LOCATION_ADD + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.LOCATIONS, method = RequestMethod.POST)
	public APIResponse<DTO> addLocation(Principal p, HttpServletRequest request,
			@Validated @RequestBody SaveLocationDTO dto) throws Exception {
		userService.addLocation(getUserId(p), constructIpAddress(request), dto);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.LOCATION_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.LOCATIONS_REMOVE, method = RequestMethod.POST)
	public APIResponse<DTO> removeLocation(Principal p, HttpServletRequest request,
			@Validated @RequestBody SaveLocationDTO dto) throws Exception {
		userService.removeLocation(getUserId(p), constructIpAddress(request), dto);
		return Success();
	}
	*/
}
