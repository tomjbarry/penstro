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
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.EscrowService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;

@Controller
@Profile({ProfileNames.ADMIN})
public class BackingAdminController extends BaseAdminController {

	@Autowired
	protected EscrowService escrowService;
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BACKING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BACKINGS_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> backingId(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String target) throws Exception {
		return Success(escrowService.getBackerDTO(getUser(p, atid), getUser(target), 
				target));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BACKINGS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BACKINGS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> backings(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(escrowService.getBackers(getUserId(p, atid), 
				constructPageable(page, size)));
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BACKING_OUTSTANDING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BACKINGS_OUTSTANDING_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> backingOutstandingId(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String target) throws Exception {
		return Success(escrowService.getBackerOutstandingDTO(getUser(p, atid), 
				getUser(target), target));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BACKINGS_OUTSTANDING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BACKINGS_OUTSTANDING, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> backingsOutstanding(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(escrowService.getBackersOutstanding(getUserId(p, atid),
				constructPageable(page, size)));
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BACKING_WITHDRAW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BACKINGS_OUTSTANDING_ID, method = RequestMethod.DELETE)
	public APIResponse<DTO> cancelOutstandingBacking(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String source) throws Exception {
		escrowService.withdrawBacking(getUser(p, atid), getUser(source), source);
		return Success();
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_BACKING_END + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_BACKINGS_ID, method = RequestMethod.DELETE)
	public APIResponse<DTO> cancelBacking(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String target) throws Exception {
		escrowService.cancelBacking(getUser(p, atid), getUser(target), target);
		return Success();
	}
}
