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
import com.py.py.dto.in.PromotePostingDTO;
import com.py.py.service.PostingService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.PromotePostingValidator;

@Controller
public class PromotePostingController extends BaseController {

	@Autowired
	protected PostingService postingService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new PromotePostingValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTING_PROMOTE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_PROMOTE, method = RequestMethod.POST)
	public APIResponse<DTO> promotePosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid,
			@RequestBody @Validated PromotePostingDTO dto) throws Exception {
		postingService.promotePosting(getUser(p), getPosting(pid), dto);
		return Success();
	}
}
