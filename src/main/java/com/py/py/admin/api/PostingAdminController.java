package com.py.py.admin.api;

import java.security.Principal;
import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIAdminUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.admin.ChangeTallyDTO;
import com.py.py.dto.out.PostingDTO;
import com.py.py.service.AdminService;
import com.py.py.service.PostingService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeTallyValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class PostingAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@Autowired
	protected PostingService postingService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeTallyValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTINGS_SELF_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_SELF, method = RequestMethod.GET)
	public APIPagedResponse<DTO, PostingDTO> postingsSelf(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.TAGS, required = false) List<String> tags,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview) 
					throws Exception {
		return Success(postingService.getSelfPreviews(getUserId(p, atid), 
				constructPageable(page, size),
				constructTags(tags),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID, method = RequestMethod.GET)
	public APIResponse<PostingDTO> viewPosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		return Success(adminService.getPostingDTO(getPostingAdmin(pid)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_ENABLE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_ENABLE, method = RequestMethod.POST)
	public APIResponse<DTO> enablePosting(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		postingService.enablePosting(getUser(p, atid), getPosting(pid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_DISABLE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_DISABLE, method = RequestMethod.POST)
	public APIResponse<DTO> disablePosting(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		postingService.disablePosting(getUser(p, atid), getPosting(pid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_REMOVE, method = RequestMethod.DELETE)
	public APIResponse<DTO> removePosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		adminService.setPostingRemove(getUser(p), getPostingAdmin(pid), true);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_UNREMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_REMOVE, method = RequestMethod.POST)
	public APIResponse<DTO> unremovePosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		adminService.setPostingRemove(getUser(p), getPostingAdmin(pid), false);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_WARNING + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_WARNING, method = RequestMethod.DELETE)
	public APIResponse<DTO> warningPosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		adminService.setPostingWarning(getUser(p), getPostingAdminId(pid), true);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_UNWARNING + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_WARNING, method = RequestMethod.POST)
	public APIResponse<DTO> unwarningPosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		adminService.setPostingWarning(getUser(p), getPostingAdminId(pid), false);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_FLAG + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_FLAG, method = RequestMethod.DELETE)
	public APIResponse<DTO> flagPosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		adminService.setPostingFlag(getUser(p), getPostingAdminId(pid), true);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_UNFLAG + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_FLAG, method = RequestMethod.POST)
	public APIResponse<DTO> unflagPosting(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		adminService.setPostingFlag(getUser(p), getPostingAdminId(pid), false);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_POSTING_TALLY_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_POSTINGS_ID_TALLY_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changeTally(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid,
			@Validated @RequestBody ChangeTallyDTO dto) throws Exception {
		adminService.changePostingTally(getUser(p), getPostingAdminId(pid), dto);
		return Success();
	}
}
