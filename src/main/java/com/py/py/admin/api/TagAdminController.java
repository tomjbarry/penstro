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
import com.py.py.dto.APIResponse;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.AdminService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;

@Controller
@Profile({ProfileNames.ADMIN})
public class TagAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_TAG_LOCK + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_TAGS_ID_LOCK, method = RequestMethod.POST)
	public APIResponse<BackerDTO> lockTag(Principal p,
			@PathVariable(PathVariables.TAG_ID) String tag, 
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language)
					throws Exception {
		adminService.lockTag(getUser(p), tag, constructLanguage(language));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_TAG_UNLOCK + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_TAGS_ID_UNLOCK, method = RequestMethod.POST)
	public APIResponse<BackerDTO> unlockTag(Principal p,
			@PathVariable(PathVariables.TAG_ID) String tag, 
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language)
					throws Exception {
		adminService.unlockTag(getUser(p), tag, constructLanguage(language));
		return Success();
	}
}
