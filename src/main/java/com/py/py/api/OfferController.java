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
import com.py.py.dto.in.BackingOfferDTO;
import com.py.py.dto.out.BackerDTO;
import com.py.py.service.EscrowService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.BackingOfferValidator;

@Controller
public class OfferController extends BaseController {

	@Autowired
	protected EscrowService escrowService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new BackingOfferValidator());
	}
	
	protected User getDTOUser(BackingOfferDTO dto) throws Exception {
		return getUser(dto.getUsername());
	}
	
	protected long getDTOAmount(BackingOfferDTO dto) {
		return dto.getAmount();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> offersId(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(escrowService.getOfferDTO(getUser(p), 
				getUserOrNull(username), username));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFERS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> offers(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(escrowService.getOffers(getUser(p), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_OUTSTANDING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_OUTSTANDING_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> offersOutstandingId(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(escrowService.getOfferOutstandingDTO(getUser(p), 
				getUserOrNull(username), username));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFERS_OUTSTANDING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_OUTSTANDING, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> offersOutstanding(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(escrowService.getOffersOutstanding(getUserId(p), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_ACCEPT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_ACCEPT, method = RequestMethod.POST)
	public APIResponse<DTO> offerAccept(Principal p, 
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		escrowService.acceptOffer(getUser(p), getUserOrNull(username), 
				username);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_DENY + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_DENY, method = RequestMethod.DELETE)
	public APIResponse<DTO> offerDeny(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		escrowService.denyOffer(getUser(p), getUserOrNull(username), 
				username);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_WITHDRAW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS_WITHDRAW, method = RequestMethod.DELETE)
	public APIResponse<DTO> offerWithdraw(Principal p,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		escrowService.withdrawOffer(getUser(p), getUserOrNull(username), 
				username);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.OFFER_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.OFFERS, method = RequestMethod.POST)
	public APIResponse<DTO> addOffer(Principal p,
			@Validated @RequestBody BackingOfferDTO dto) throws Exception {
		escrowService.addOffer(getUser(p), getDTOUser(dto), getDTOAmount(dto));
		return Success();
	}
}
