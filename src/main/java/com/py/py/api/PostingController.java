package com.py.py.api;

import java.security.Principal;
import java.util.List;

import org.bson.types.ObjectId;
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
import com.py.py.constants.ResponseCodes;
import com.py.py.domain.User;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.SubmitPostingDTO;
import com.py.py.dto.out.PostingDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.service.PostingService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.SubmitPostingValidator;

@Controller
public class PostingController extends BaseController {

	@Autowired
	protected PostingService postingService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new SubmitPostingValidator());
	}
	
	public ObjectId getAuthorIdOrNull(String username) throws Exception {
		if(username == null || username.isEmpty()) {
			return null;
		} else {
			return getUserId(username);
		}
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTINGS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, PostingDTO> postings(Principal p,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@RequestParam(value = ParamNames.USER, required = false) String username, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.SORT, required = false) String sort,
			@RequestParam(value = ParamNames.TIME, required = false) String time,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview,
			@RequestParam(value = ParamNames.TAGS, required = false) List<String> tags) 
					throws Exception {
		return Success(postingService.getPostingPreviews(
				constructLanguage(language),
				getAuthorIdOrNull(username),
				constructPageable(page, size),
				constructFilter(sort, time, warning, tags),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTINGS_AUTHOR_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_AUTHOR, method = RequestMethod.GET)
	public APIPagedResponse<DTO, PostingDTO> postingsByAuthor(Principal p,
			@PathVariable(PathVariables.USER_ID) String username, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview,
			@RequestParam(value = ParamNames.TAGS, required = false) List<String> tags) 
					throws Exception {
		return Success(postingService.getAuthorPreviews(getUserId(username), 
				constructPageable(page, size), 
				constructTags(tags), 
				constructWarning(warning),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTINGS_BENEFICIARY_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_BENEFICIARY, method = RequestMethod.GET)
	public APIPagedResponse<DTO, PostingDTO> postingsByBeneficiary(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview,
			@RequestParam(value = ParamNames.TAGS, required = false) List<String> tags) 
					throws Exception {
		return Success(postingService.getBeneficiaryPreviews(getUserId(username), 
				constructPageable(page, size), 
				constructTags(tags), 
				constructWarning(warning),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTINGS_SELF_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_SELF, method = RequestMethod.GET)
	public APIPagedResponse<DTO, PostingDTO> postingsSelf(Principal p, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.TAGS, required = false) List<String> tags,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview) 
					throws Exception {
		return Success(postingService.getSelfPreviews(getUserId(p), 
				constructPageable(page, size),
				constructTags(tags),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTING_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> createPosting(Principal p,
			@RequestBody @Validated SubmitPostingDTO dto, 
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language)
					throws Exception {
		return Success(ResponseCodes.CREATED,postingService.createPostingDTO(
				getUser(p), dto, constructLanguage(language)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_ID, method = RequestMethod.GET)
	public APIResponse<PostingDTO> postingId(Principal p,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		return Success(postingService.getPostingDTO(getUserIdOrNull(getUserOrNull(p)), 
				getCachedPosting(pid), constructWarning(warning)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTING_ENABLE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_ID_ENABLE, method = RequestMethod.POST)
	public APIResponse<DTO> enable(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		postingService.enablePosting(getUser(p), getPosting(pid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTING_DISABLE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_ID_DISABLE, method = RequestMethod.POST)
	public APIResponse<DTO> disable(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		postingService.disablePosting(getUser(p), getPosting(pid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.POSTING_FLAG + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_ID_FLAG, method = RequestMethod.POST)
	public APIResponse<DTO> flag(Principal p,
			@PathVariable(PathVariables.POSTING_ID) String pid,
			@RequestParam(value = ParamNames.FLAG_REASON, required = false) String reason) throws Exception {
		User user = getUser(p);
		postingService.flag(user, getUserInfo(user), getPosting(pid), constructFlagReason(reason));
		return Success();
	}
}
