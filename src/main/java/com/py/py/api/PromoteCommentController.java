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
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.PromoteCommentDTO;
import com.py.py.service.CommentService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.PromoteCommentValidator;

@Controller
public class PromoteCommentController extends BaseController {

	@Autowired
	protected CommentService commentService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new PromoteCommentValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENT_PROMOTE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_PROMOTE, method = RequestMethod.POST)
	public APIResponse<DTO> promoteComment(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid,
			@RequestBody @Validated PromoteCommentDTO dto) throws Exception {
		commentService.promoteComment(getUser(p), getComment(cid), dto);
		return Success();
	}
}
