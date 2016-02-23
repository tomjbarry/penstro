package com.py.py.admin.api;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

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
import com.py.py.dto.in.AppreciatePostingDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.PaymentService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.AppreciatePostingValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class AppreciatePostingAdminController extends BaseAdminController {

	@Autowired
	protected PaymentService paymentService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new AppreciatePostingValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_APPRECIATE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_APPRECIATE, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> appreciatePosting(HttpServletRequest request, Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.POSTING_ID) String pid,
			@RequestBody @Validated AppreciatePostingDTO dto) throws Exception {
		return Success(paymentService.adminAppreciatePosting(getUser(p, atid), getPosting(pid), dto, 
				constructIpAddress(request)	));
	}
}
