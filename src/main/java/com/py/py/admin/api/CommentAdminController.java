package com.py.py.admin.api;

import java.security.Principal;

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
import com.py.py.dto.out.CommentDTO;
import com.py.py.service.AdminService;
import com.py.py.service.CommentService;
import com.py.py.service.constants.AdminPermissionNames;
import com.py.py.service.constants.ProfileNames;
import com.py.py.validation.ChangeTallyValidator;

@Controller
@Profile({ProfileNames.ADMIN})
public class CommentAdminController extends BaseAdminController {

	@Autowired
	protected AdminService adminService;
	
	@Autowired
	protected CommentService commentService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new ChangeTallyValidator());
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENTS_SELF_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_SELF, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> commentsSelf(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview) 
					throws Exception {
		return Success(commentService.getSelfPreviewDTOs(getUserId(p, atid), 
				constructPageable(page, size), constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID, method = RequestMethod.GET)
	public APIResponse<CommentDTO> viewComment(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		return Success(adminService.getCommentDTO(getCommentAdmin(cid)));
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_ENABLE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_ENABLE, method = RequestMethod.POST)
	public APIResponse<DTO> enableComment(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String username,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		commentService.enableComment(getUser(p, username), getComment(cid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_DISABLE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_DISABLE, method = RequestMethod.POST)
	public APIResponse<DTO> disableComment(Principal p,
			@PathVariable(PathVariables.ADMIN_TARGET_ID) String atid,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		commentService.disableComment(getUser(p, atid), getComment(cid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_REMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_REMOVE, method = RequestMethod.DELETE)
	public APIResponse<DTO> removeComment(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		adminService.setCommentRemove(getUser(p), getCommentAdmin(cid), true);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_UNREMOVE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_REMOVE, method = RequestMethod.POST)
	public APIResponse<DTO> unremoveComment(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		adminService.setCommentRemove(getUser(p), getCommentAdmin(cid), false);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_WARNING + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_WARNING, method = RequestMethod.DELETE)
	public APIResponse<DTO> warningComment(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		adminService.setCommentWarning(getUser(p), getCommentAdminId(cid), true);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_UNWARNING + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_WARNING, method = RequestMethod.POST)
	public APIResponse<DTO> unwarningComment(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		adminService.setCommentWarning(getUser(p), getCommentAdminId(cid), false);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_FLAG + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_FLAG, method = RequestMethod.DELETE)
	public APIResponse<DTO> flagComment(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		adminService.setCommentFlag(getUser(p), getCommentAdminId(cid), true);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_UNFLAG + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_FLAG, method = RequestMethod.POST)
	public APIResponse<DTO> unflagComment(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		adminService.setCommentFlag(getUser(p), getCommentAdminId(cid), false);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + AdminPermissionNames.ADMIN_COMMENT_TALLY_CHANGE + "')")
	@ResponseBody
	@RequestMapping(value = APIAdminUrls.ADMIN_COMMENTS_ID_TALLY_CHANGE, method = RequestMethod.POST)
	public APIResponse<DTO> changeTally(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid,
			@Validated @RequestBody ChangeTallyDTO dto) throws Exception {
		adminService.changeCommentTally(getUser(p), getCommentAdminId(cid), dto);
		return Success();
	}
}
