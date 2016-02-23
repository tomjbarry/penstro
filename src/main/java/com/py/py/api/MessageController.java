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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.constants.PathVariables;
import com.py.py.domain.User;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.in.SubmitMessageDTO;
import com.py.py.dto.out.ConversationDTO;
import com.py.py.dto.out.MessageDTO;
import com.py.py.service.MessageService;
import com.py.py.service.constants.PermissionNames;
import com.py.py.validation.SubmitMessageValidator;

@Controller
public class MessageController extends BaseController {

	@Autowired
	protected MessageService messageService;

	@InitBinder
	protected void initBinder(WebDataBinder binder) {
		binder.setValidator(new SubmitMessageValidator());
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.MESSAGES_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.MESSAGES, method = RequestMethod.GET)
	public APIPagedResponse<DTO, ConversationDTO> messages(Principal p,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview)
					throws Exception {
		return Success(messageService.getConversationDTOs(getUser(p), constructPageable(page, size), constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.MESSAGES_CONVERSATION_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.MESSAGES_CONVERSATION, method = RequestMethod.GET)
	public APIResponse<ConversationDTO> conversation(Principal p,
			@PathVariable(PathVariables.USER_ID) String target)
					throws Exception {
		return Success(messageService.getConversationDTO(getUser(p), getUser(target)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.MESSAGES_CONVERSATION_MESSAGES_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.MESSAGES_CONVERSATION_MESSAGES, method = RequestMethod.GET)
	public APIPagedResponse<DTO, MessageDTO> conversationMessages(Principal p,
			@PathVariable(PathVariables.USER_ID) String target,
			@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
			@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
			@RequestParam(value = ParamNames.PREVIEW, required = false) Boolean preview)
					throws Exception {
		return Success(messageService.getMessageDTOs(getUser(p), 
				getUser(target), constructPageable(page, size), constructPreview(preview)));
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.MESSAGE_SUBMIT + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.MESSAGES_CONVERSATION, method = RequestMethod.POST)
	public APIResponse<DTO> createMessage(Principal p,
			@PathVariable(PathVariables.USER_ID) String target,
			@Validated @RequestBody SubmitMessageDTO dto) throws Exception {
		messageService.createMessage(getUser(p), getUser(target), dto);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.CONVERSATION_FLAG + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.MESSAGES_CONVERSATION_FLAG, method = RequestMethod.POST)
	public APIResponse<DTO> flagMessage(Principal p,
			@PathVariable(PathVariables.USER_ID) String author) throws Exception {
		User user = getUser(p);
		User targetUser = getUser(author);
		
		messageService.flagConversation(user, getUserInfo(user), 
				targetUser, getUserInfo(targetUser));
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.CONVERSATION_HIDE + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.MESSAGES_CONVERSATION_SHOW, method = RequestMethod.DELETE)
	public APIResponse<DTO> hideConversation(Principal p,
			@PathVariable(PathVariables.USER_ID) String author) throws Exception {
		User user = getUser(p);
		User targetUser = getUser(author);
		
		messageService.toggleShowConversation(user, targetUser, false);
		return Success();
	}
	
	@PreAuthorize("hasAuthority('" + PermissionNames.CONVERSATION_SHOW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.MESSAGES_CONVERSATION_SHOW, method = RequestMethod.POST)
	public APIResponse<DTO> showConversation(Principal p,
			@PathVariable(PathVariables.USER_ID) String author) throws Exception {
		User user = getUser(p);
		User targetUser = getUser(author);
		
		messageService.toggleShowConversation(user, targetUser, true);
		return Success();
	}
}
