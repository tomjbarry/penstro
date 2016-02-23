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
import com.py.py.constants.ResponseCodes;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.FlagDataDTO;
import com.py.py.enumeration.FLAG_TYPE;
import com.py.py.service.AdminService;
import com.py.py.service.FlagService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;

@Controller
@Profile({ProfileNames.ADMIN})
public class FlagAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@Autowired
	protected FlagService flagService;
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FLAG_DATAS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FLAG_DATA_POSTINGS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FlagDataDTO> getPostingsFlagData(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(flagService.getFlagDataDTOs(FLAG_TYPE.POSTING, constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FLAG_DATAS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FLAG_DATA_COMMENTS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FlagDataDTO> getCommentsFlagData(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(flagService.getFlagDataDTOs(FLAG_TYPE.COMMENT, constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FLAG_DATAS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FLAG_DATA_USERS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, FlagDataDTO> getUsersFlagData(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(flagService.getFlagDataDTOs(FLAG_TYPE.USER, constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FLAG_DATA_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FLAG_DATA_POSTINGS_ID, method = RequestMethod.DELETE)
	public APIResponse<DTO> removePostingFlagData(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) 
					throws Exception {
		adminService.removeFlagData(getUser(p), getPostingId(pid), FLAG_TYPE.POSTING);
		return Success(ResponseCodes.DELETED);
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FLAG_DATA_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FLAG_DATA_COMMENTS_ID, method = RequestMethod.DELETE)
	public APIResponse<DTO> removeCommentFlagData(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) 
					throws Exception {
		adminService.removeFlagData(getUser(p), getCommentId(cid), FLAG_TYPE.COMMENT);
		return Success(ResponseCodes.DELETED);
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FLAG_DATA_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FLAG_DATA_USERS_ID, method = RequestMethod.DELETE)
	public APIResponse<DTO> removeUserFlagData(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) 
					throws Exception {
		adminService.removeFlagData(getUser(p), getUserId(username), FLAG_TYPE.USER);
		return Success(ResponseCodes.DELETED);
	}
}
