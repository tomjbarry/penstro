package com.py.py.api;

import java.security.Principal;

import javax.servlet.http.HttpServletRequest;

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
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIResponse;
import com.py.py.dto.in.AppreciateCommentDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.PaymentService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.AppreciateCommentValidator;

@Controller
public class AppreciateCommentController extends BaseController {

	@Autowired
	protected PaymentService paymentService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new AppreciateCommentValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENT_APPRECIATE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_APPRECIATE, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> appreciateComment(HttpServletRequest request, 
			Principal p, @PathVariable(PathVariables.COMMENT_ID) String cid,
			@RequestBody @Validated AppreciateCommentDTO dto) throws Exception {
		return Success(paymentService.appreciateComment(getUser(p), getComment(cid), dto, 
				constructIpAddress(request)));
	}
}
