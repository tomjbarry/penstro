package com.py.py.api;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.dto.APIResponse;
import com.py.py.dto.in.PurchaseCurrencyDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.PaymentService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.PurchaseCurrencyValidator;

@Controller
public class PaymentController extends BaseController {
	
	@Autowired
	protected PaymentService paymentService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new PurchaseCurrencyValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.CURRENCY_PURCHASE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.CURRENCY_PURCHASE, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> purchase(HttpServletRequest request, Principal p, 
			@Validated @RequestBody PurchaseCurrencyDTO dto) throws Exception {
		return Success(paymentService.purchaseCurrency(getUser(p), dto, 
				constructIpAddress(request)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.PAYMENT_CHECK + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PAYMENT, method = RequestMethod.GET)
	public APIResponse<ResultSuccessDTO> checkPayment(Principal p,
			@RequestParam(value = ParamNames.TRACKING_ID, required = false) String trackingId,
			@RequestParam(value = ParamNames.PAYKEY, required = false) String payKey) throws Exception {
		paymentService.checkPayment(getUserId(p), constructObjectIdOrNull(trackingId), payKey);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.PAYMENT_MARK + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PAYMENT, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> markPayment(Principal p,
			@RequestParam(value = ParamNames.TRACKING_ID, required = false) String trackingId,
			@RequestParam(value = ParamNames.PAYKEY, required = false) String payKey) throws Exception {
		paymentService.markPayment(getUserId(p), constructObjectIdOrNull(trackingId), payKey);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.PAYMENT_NOTIFICATION + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.PAYMENT_NOTIFICATION, method = RequestMethod.POST)
	public ResponseEntity<String> paymentNotification(HttpServletRequest request, Principal p) throws Exception {
		paymentService.paymentNotification(request);
		return new ResponseEntity<String>(HttpStatus.OK);
	}
}
