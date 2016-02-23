package com.py.py.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.EscrowService;
import com.py.py.service.constants.PermissionNames;

@Controller
public class BackingController extends BaseController {

	@Autowired
	protected EscrowService escrowService;
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BACKING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BACKINGS_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> backingId(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(escrowService.getBackerDTO(getUser(p), 
				getUserOrNull(username), username));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BACKINGS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BACKINGS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> backings(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(escrowService.getBackers(getUserId(p), 
				constructPageable(page, size)));
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.BACKING_OUTSTANDING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BACKINGS_OUTSTANDING_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> backingOutstandingId(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(escrowService.getBackerOutstandingDTO(getUser(p), 
				getUserOrNull(username), username));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BACKINGS_OUTSTANDING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BACKINGS_OUTSTANDING, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> backingsOutstanding(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) throws Exception {
		return Success(escrowService.getBackersOutstanding(getUserId(p), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BACKING_WITHDRAW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BACKINGS_OUTSTANDING_ID, method = RequestMethod.DELETE)
	public APIResponse<DTO> cancelOutstandingBacking(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		escrowService.withdrawBacking(getUser(p), 
				getUserOrNull(username), username);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.BACKING_END + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.BACKINGS_ID, method = RequestMethod.DELETE)
	public APIResponse<DTO> cancelBacking(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		escrowService.cancelBacking(getUser(p), 
				getUserOrNull(username), username);
		return Success();
	}
}
