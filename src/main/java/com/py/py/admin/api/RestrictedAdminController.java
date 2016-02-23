package com.py.py.admin.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIAdminUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.constants.ResponseCodes;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.admin.ChangeRestrictedDTO;
import com.py.py.dto.out.admin.RestrictedDTO;
import com.py.py.service.AdminService;
import com.py.py.service.RestrictedService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeRestrictedValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class RestrictedAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@Autowired
	protected RestrictedService restrictedService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeRestrictedValidator());
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_RESTRICTED_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_RESTRICTEDS_ID, method = RequestMethod.GET)
	public APIResponse<RestrictedDTO> restricted(Principal p,
			@PathVariable(PathVariables.RESTRICTED_ID) String restricted, 
			@RequestParam(value = ParamNames.RESTRICTED_TYPE, required = true) String type) 
					throws Exception {
		return Success(restrictedService.getRestrictedDTO(restricted, 
				constructRestrictedType(type)));
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_RESTRICTED_ADD + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_RESTRICTEDS, method = RequestMethod.POST)
	public APIResponse<DTO> addRestricted(Principal p,
			@Validated @RequestBody ChangeRestrictedDTO dto) throws Exception {
		adminService.addRestricted(getUser(p), dto);
		return Success(ResponseCodes.CREATED);
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_RESTRICTED_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_RESTRICTEDS_ID, method = RequestMethod.DELETE)
	public APIResponse<DTO> removeRestricted(Principal p,
			@PathVariable(PathVariables.RESTRICTED_ID) String restricted, 
			@RequestParam(value = ParamNames.RESTRICTED_TYPE, required = true) String type) 
					throws Exception {
		adminService.removeRestricted(getUser(p), restricted, constructRestrictedType(type));
		return Success(ResponseCodes.DELETED);
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_RESTRICTEDS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_RESTRICTEDS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, RestrictedDTO> restricteds(Principal p,
			@RequestParam(value = ParamNames.RESTRICTED_TYPE, required = false) String type,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size
			) throws Exception {
		return Success(adminService.getRestrictedDTOs(constructRestrictedType(type), 
				constructPageable(page, size)));
	}
}
