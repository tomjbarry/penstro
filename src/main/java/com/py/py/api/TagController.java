package com.py.py.api;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.TagDTO;
import com.py.py.service.TagService;
import com.py.py.service.constants.PermissionNames;

@Controller
public class TagController extends BaseController {

	@Autowired
	protected TagService tagService;
	
	@PreAuthorize("hasAuthority('" + PermissionNames.TAGS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.TAGS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, TagDTO> tags(Principal p, 
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language, 
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.TIME, required = false) String time) throws Exception {
		return Success(tagService.getTags(
				constructLanguage(language), 
				constructPageable(page, size), constructTime(time)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.TAG_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.TAGS_ID, method = RequestMethod.GET)
	public APIResponse<TagDTO> tag(Principal p, 
			@PathVariable(PathVariables.TAG_ID) String tag,
			@RequestParam(value = ParamNames.LANGUAGE, required = false) String language)
					throws Exception {
		return Success(tagService.getTagDTO(getCachedTag(tag, 
				constructLanguage(language))));
	}
}
