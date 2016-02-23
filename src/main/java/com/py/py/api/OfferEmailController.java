package com.py.py.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
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

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.domain.User;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.BackingEmailOfferDTO;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.EscrowService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.BackingEmailOfferValidator;

@Controller
public class OfferEmailController extends BaseController {
	
	@Autowired
	protected EscrowService escrowService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new BackingEmailOfferValidator());
	}
	
	protected User getDTOUser(BackingEmailOfferDTO dto) throws Exception {
		return getUserByEmailOrNull(dto.getEmail());
	}
	
	protected String getDTOEmail(BackingEmailOfferDTO dto) throws Exception {
		return constructEmail(dto.getEmail());
	}
	
	protected long getDTOAmount(BackingEmailOfferDTO dto) {
		return dto.getAmount();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_EMAIL_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_EMAIL, method = RequestMethod.POST)
	public APIResponse<DTO> backingOfferEmail(Principal p,
			@Validated @RequestBody BackingEmailOfferDTO dto) throws Exception {
		escrowService.addEmailOffer(getUser(p), getDTOUser(dto), getDTOEmail(dto), 
				getDTOAmount(dto));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_OUTSTANDING_EMAIL_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_OUTSTANDING_EMAIL_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> offerEmail(Principal p,
			@PathVariable(PathVariables.EMAIL_ID) String email) throws Exception {
		return Success(escrowService.getEmailOfferOutstandingDTO(getUser(p), 
				constructEmail(email)));
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_EMAIL_WITHDRAW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_EMAIL_WITHDRAW, method = RequestMethod.DELETE)
	public APIResponse<DTO> offerEmailWithdraw(Principal p,
			@PathVariable(PathVariables.EMAIL_ID) String email) throws Exception {
		escrowService.withdrawEmailOffer(getUser(p), getUserByEmailOrNull(email), 
				constructEmail(email));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFERS_OUTSTANDING_EMAIL_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_OUTSTANDING_EMAIL, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> offerEmailsOutstanding(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(escrowService.getEmailOffersOutstanding(getUserId(p), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_EMAIL_ACCEPT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_EMAIL_ACCEPT, method = RequestMethod.POST)
	public APIResponse<DTO> offerEmailAccept(Principal p, 
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		escrowService.acceptEmailOffer(getUser(p), getUserOrNull(username), 
				username);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_EMAIL_DENY + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_EMAIL_DENY, method = RequestMethod.DELETE)
	public APIResponse<DTO> offerEmailDeny(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		escrowService.denyEmailOffer(getUser(p), getUserOrNull(username), 
				username);
		return Success();
	}
}
