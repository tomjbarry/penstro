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
import com.py.py.domain.Comment;
import com.py.py.domain.Posting;
import com.py.py.domain.Tag;
import com.py.py.domain.User;
import com.py.py.domain.UserInfo;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.SubmitCommentDTO;
import com.py.py.dto.out.CommentDTO;
import com.py.py.dto.out.ResultSuccessDTO;
import com.py.py.enumeration.COMMENT_TYPE;
import com.py.py.service.CommentService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.service.exception.ServiceException;
import com.py.py.service.util.ArgCheck;
import com.py.py.validation.SubmitCommentValidator;

@Controller
public class CommentController extends BaseController {

	@Autowired
	protected CommentService commentService;
	
	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new SubmitCommentValidator());
	}
	
	protected COMMENT_TYPE getCommentType(Comment comment) throws ServiceException {
		ArgCheck.nullCheck(comment);
		return comment.getType();
	}
	
	protected Posting getCommentPosting(Comment comment) throws ServiceException {
		if(COMMENT_TYPE.POSTING.equals(getCommentType(comment))) {
			ObjectId baseId = comment.getBaseId();
			if(baseId != null) {
				return getPosting(baseId.toHexString());
			}
		}
		return null;
	}
	
	protected UserInfo getCommentUserInfo(Comment comment) throws ServiceException {
		if(COMMENT_TYPE.USER.equals(getCommentType(comment))) {
			ObjectId baseId = comment.getBaseId();
			if(baseId != null) {
				return getUserInfo(getUser(baseId));
			}
		}
		return null;
	}
	
	protected Tag getCommentTag(Comment comment) throws ServiceException {
		if(COMMENT_TYPE.TAG.equals(getCommentType(comment))) {
			String baseString = comment.getBaseString();
			if(baseString != null && !baseString.isEmpty()) {
				return getTag(baseString);
			}
		}
		return null;
	}
	
	public ObjectId getAuthorIdOrNull(String username) throws Exception {
		if(username == null || username.isEmpty()) {
			return null;
		} else {
			return getUserId(username);
		}
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> comments(Principal p,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@RequestParam(value = ParamNames.USER, required = false) String username, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.SORT, required = false) String sort,
			@RequestParam(value = ParamNames.TIME, required = false) String time,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview,
			@RequestParam(value = ParamNames.COMMENT_TYPE, required = false) List<String> commentTypes) 
					throws Exception{
		return Success(commentService.getCommentPreviewDTOs(
				constructLanguage(language), 
				getAuthorIdOrNull(username),
				constructPageable(page, size),
				constructFilter(sort, time, warning),
				constructCommentTypes(commentTypes),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_AUTHOR_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_AUTHOR, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> commentsByAuthor(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview) 
					throws Exception{
		return Success(commentService.getAuthorPreviewDTOs(getUserId(username), 
				constructPageable(page, size), 
				constructWarning(warning),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_BENEFICIARY_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_BENEFICIARY, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> commentsByBeneficiary(Principal p,
			@PathVariable(PathVariables.USER_ID) String username,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview) 
					throws Exception{
		return Success(commentService.getBeneficiaryPreviewDTOs(getUserId(username), 
				constructPageable(page, size), 
				constructWarning(warning),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_SELF_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_SELF, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> commentsSelf(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview) 
					throws Exception{
		return Success(commentService.getSelfPreviewDTOs(getUserId(p), 
				constructPageable(page, size),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_POSTING_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_COMMENTS, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> createPostingsComment(Principal p,
			@RequestBody @Validated SubmitCommentDTO dto,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		return Success(ResponseCodes.CREATED, commentService.createCommentDTO(getUser(p), 
				null, getPosting(pid), null, null, COMMENT_TYPE.POSTING, dto, 
				constructLanguage(language)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_USER_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_COMMENTS, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> createUsersComment(Principal p,
			@RequestBody @Validated SubmitCommentDTO dto,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(ResponseCodes.CREATED, commentService.createCommentDTO(getUser(p), 
				null, null, getUserInfo(getUser(username)), null, COMMENT_TYPE.USER, dto, 
				constructLanguage(language)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_TAG_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.TAGS_COMMENTS, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> createTagsComment(Principal p,
			@RequestBody @Validated SubmitCommentDTO dto,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@PathVariable(PathVariables.TAG_ID) String tag) throws Exception {
		String constructedLanguage = constructLanguage(language);
		return Success(ResponseCodes.CREATED, commentService.createCommentDTO(getUser(p), 
				null, null, null, getTag(tag, constructedLanguage), 
				COMMENT_TYPE.TAG, dto, constructedLanguage));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_COMMENT_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_COMMENTS, method = RequestMethod.POST)
	public APIResponse<ResultSuccessDTO> createSubComment(Principal p,
			@RequestBody @Validated SubmitCommentDTO dto,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		Comment comment = getComment(cid);
		return Success(ResponseCodes.CREATED, commentService.createCommentDTO(getUser(p), 
				comment, getCommentPosting(comment), getCommentUserInfo(comment), 
				getCommentTag(comment), getCommentType(comment), dto, 
				constructLanguage(language)));
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_POSTING_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.POSTINGS_COMMENTS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> postingComments(Principal p,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.SORT, required = false) String sort,
			@RequestParam(value = ParamNames.TIME, required = false) String time,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview,
			@PathVariable(PathVariables.POSTING_ID) String pid) throws Exception {
		return Success(commentService.getCommentDTOs(getCachedPostingId(pid), null, 
				COMMENT_TYPE.POSTING, constructLanguage(language), 
				constructPageable(page, size),
				constructFilter(sort, time, warning),
				constructPreview(preview)));
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_USER_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.USERS_COMMENTS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> userComments(Principal p,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.SORT, required = false) String sort,
			@RequestParam(value = ParamNames.TIME, required = false) String time,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview,
			@PathVariable(PathVariables.USER_ID) String username) throws Exception {
		return Success(commentService.getCommentDTOs(getUserId(username), null,
				COMMENT_TYPE.USER, constructLanguage(language), 
				constructPageable(page, size),
				constructFilter(sort, time, warning),
				constructPreview(preview)));
	}

	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_TAG_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.TAGS_COMMENTS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> tagComments(Principal p,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.SORT, required = false) String sort,
			@RequestParam(value = ParamNames.TIME, required = false) String time,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview,
			@PathVariable(PathVariables.TAG_ID) String tag) throws Exception {
		String constructedLanguage = constructLanguage(language);
		return Success(commentService.getCommentDTOs(null, 
				getCachedTag(tag, constructedLanguage).getId().toString(), COMMENT_TYPE.TAG, 
				constructedLanguage, constructPageable(page, size),
				constructFilter(sort, time, warning),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENT_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_ID, method = RequestMethod.GET)
	public APIResponse<CommentDTO> comment(Principal p,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		return Success(commentService.getCommentDTO(getUserIdOrNull(getUserOrNull(p)), 
				getCachedComment(cid), constructWarning(warning)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENTS_COMMENT_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_COMMENTS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, CommentDTO> subComments(Principal p,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.SORT, required = false) String sort,
			@RequestParam(value = ParamNames.TIME, required = false) String time,
			@RequestParam(value = ParamNames.WARNING, required = false) Boolean warning,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		return Success(commentService.getSubCommentDTOs(getCommentId(cid), 
				constructLanguage(language), constructPageable(page, size),
				constructFilter(sort, time, warning),
				constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENT_ENABLE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_ID_ENABLE, method = RequestMethod.POST)
	public APIResponse<DTO> enable(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		commentService.enableComment(getUser(p), getComment(cid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENT_DISABLE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_ID_DISABLE, method = RequestMethod.POST)
	public APIResponse<DTO> disable(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid) throws Exception {
		commentService.disableComment(getUser(p), getComment(cid));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.COMMENT_FLAG + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.COMMENTS_ID_FLAG, method = RequestMethod.POST)
	public APIResponse<DTO> flag(Principal p,
			@PathVariable(PathVariables.COMMENT_ID) String cid,
			@RequestParam(value = ParamNames.FLAG_REASON, required = false) String reason) throws Exception {
		User user = getUser(p);
		commentService.flag(user, getUserInfo(user), getComment(cid), constructFlagReason(reason));
		return Success();
	}
}
