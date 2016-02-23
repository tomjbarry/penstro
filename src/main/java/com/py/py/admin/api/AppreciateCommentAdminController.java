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
import com.py.py.dto.in.AppreciateCommentDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.PaymentService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.AppreciateCommentValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class AppreciateCommentAdminController extends BaseAdminController {

	@Autowired
	protected PaymentService paymentService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new AppreciateCommentValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_APPRECIATE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_APPRECIATE, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> appreciateComment(HttpServletRequest request, 
			Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.COMMENT_ID) String cid,
			@RequestBody @Validated AppreciateCommentDTO dto) throws Exception {
		return Success(paymentService.adminAppreciateComment(getUser(p, atid), getComment(cid), dto, 
				constructIpAddress(request)));
	}
}
