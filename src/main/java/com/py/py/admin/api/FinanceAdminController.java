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
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIAdminUrls;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.admin.ChangeBalanceDTO;
import com.py.py.dto.out.BalanceDTO;
import com.py.py.service.AdminService;
import com.py.py.service.AuthenticationService;
import com.py.py.service.FinanceService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeBalanceValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class FinanceAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@Autowired
	protected FinanceService financeService;
	
	@Autowired
	protected AuthenticationService authenticationService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeBalanceValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FINANCES_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FINANCES, method = RequestMethod.GET)
	public APIResponse<BalanceDTO> balance(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid) throws Exception {
		BalanceDTO dto = authenticationService.getBalanceDTO(getUserInfo(getUser(p, atid)));
		return Success(dto);
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FINANCES_ADD + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FINANCES_ADD, method = RequestMethod.POST)
	public APIResponse<DTO> addBalance(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@Validated @RequestBody ChangeBalanceDTO dto) throws Exception {
		adminService.addBalance(getUser(p), getUserId(username), dto);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_FINANCES_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_FINANCES_REMOVE, method = RequestMethod.POST)
	public APIResponse<DTO> removeBalance(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@Validated @RequestBody ChangeBalanceDTO dto) throws Exception {
		adminService.removeBalance(getUser(p), getUserId(username), dto);
		return Success();
	}
}
