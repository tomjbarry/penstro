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
import com.py.py.dto.DTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.AuthenticationService;
import com.py.py.service.PaymentService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;

@Controller
@Profile({ProfileNames.ADMIN})
public class PaymentAdminController extends BaseAdminController {

	@Autowired
	protected AuthenticationService authenticationService;
	
	@Autowired
	protected PaymentService paymentService;
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_PAYMENT_CHANGE_REQUEST + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_PAYMENT_CHANGE_REQUEST, method = RequestMethod.POST)
	public APIResponse<DTO> sendPaymentChange(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid)
					throws Exception {
		authenticationService.sendPaymentIdChange(getUser(p, atid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_PAYMENT_CHECK + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_PAYMENT, method = RequestMethod.GET)
	public APIResponse<ResultSuccessDTO> checkPayment(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.TRACKING_ID, required = false) String trackingId,
			@RequestParam(value = ParamNames.PAYKEY, required = false) String payKey) throws Exception {
		paymentService.checkPayment(getUserId(p, atid), constructObjectIdOrNull(trackingId), payKey);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_PAYMENT_MARK + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_PAYMENT, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> markPayment(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.TRACKING_ID, required = false) String trackingId,
			@RequestParam(value = ParamNames.PAYKEY, required = false) String payKey) throws Exception {
		paymentService.markPayment(getUserId(p, atid), constructObjectIdOrNull(trackingId), payKey);
		return Success();
	}
}
