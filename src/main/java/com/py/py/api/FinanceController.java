package com.py.py.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.dto.APIResponse;
import com.py.py.dto.out.BalanceDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.FinanceService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.PurchaseCurrencyValidator;

@Controller
public class FinanceController extends BaseController {

	@Autowired
	protected FinanceService financeService;
	
	@Autowired
	protected AuthenticationService authenticationService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new PurchaseCurrencyValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.FINANCES_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.FINANCES, method = RequestMethod.GET)
	public APIResponse<BalanceDTO> balance(Principal p) throws Exception {
		BalanceDTO dto = authenticationService.getBalanceDTO(getUserInfo(getUser(p)));
		return Success(dto);
	}
}
