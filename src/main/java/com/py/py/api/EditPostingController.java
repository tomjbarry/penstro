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
import com.py.py.dto.in.SubmitEditPostingDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.PostingService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.SubmitEditPostingValidator;

@Controller
public class EditPostingController extends BaseController {

	@Autowired
	protected PostingService postingService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new SubmitEditPostingValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTING_EDIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_ID_EDIT, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> editPosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid,
			@RequestBody @Validated SubmitEditPostingDTO dto)
					throws Exception {
		postingService.editPosting(getUserId(p), getPosting(pid), dto);
		return Success();
	}
}
