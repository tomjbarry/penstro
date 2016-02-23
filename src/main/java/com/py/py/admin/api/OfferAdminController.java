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
public class OfferAdminController extends BaseAdminController {

	@Autowired
	protected EscrowService escrowService;

	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_OFFER_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_OFFERS_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> offersId(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String target) throws Exception {
		return Success(escrowService.getOfferDTO(getUser(p, atid), 
				getUserOrNull(target), target));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_OFFERS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_OFFERS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> offers(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(escrowService.getOffers(getUser(p, atid), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_OFFER_OUTSTANDING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_OFFERS_OUTSTANDING_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> offersOutstandingId(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String source) throws Exception {
		return Success(escrowService.getOfferOutstandingDTO(getUser(p, atid), 
				getUserOrNull(source), source));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_OFFERS_OUTSTANDING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_OFFERS_OUTSTANDING, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> offersOutstanding(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(escrowService.getOffersOutstanding(getUserId(p, atid), 
				constructPageable(page, size)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_OFFER_WITHDRAW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_OFFERS_WITHDRAW, method = RequestMethod.DELETE)
	public APIResponse<DTO> offerWithdraw(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.USER_ID) String target) throws Exception {
		escrowService.withdrawOffer(getUser(p, atid), getUserOrNull(target), 
				target);
		return Success();
	}

	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_OFFER_EMAIL_WITHDRAW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_OFFERS_EMAIL_WITHDRAW, method = RequestMethod.DELETE)
	public APIResponse<DTO> offerEmailWithdraw(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.EMAIL_ID) String email) throws Exception {
		escrowService.withdrawEmailOffer(getUser(p, atid), 
				getUserByEmailOrNull(email), constructEmail(email));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_OFFER_OUTSTANDING_EMAIL_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_OFFERS_OUTSTANDING_EMAIL_ID, method = RequestMethod.GET)
	public APIResponse<BackerDTO> offerEmail(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String username,
			@PathVariable(PathVariables.EMAIL_ID) String email) throws Exception {
		return Success(escrowService.getEmailOfferOutstandingDTO(
				getUser(p, username), constructEmail(email)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_OFFERS_OUTSTANDING_EMAIL_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_OFFERS_OUTSTANDING_EMAIL, method = RequestMethod.GET)
	public APIPagedResponse<DTO, BackerDTO> offerEmailsOutstanding(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size) 
					throws Exception {
		return Success(escrowService.getEmailOffersOutstanding(
				getUserId(p, atid), constructPageable(page, size)));
	}
}
