package com.py.py.api;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ParamNames;
import com.py.py.dto.APIPagedResponse;
import com.py.py.dto.DTO;
import com.py.py.dto.out.NotificationDTO;
import com.py.py.service.EventService;
import com.py.py.service.constants.PermissionNames;

@Controller
public class NotificationController extends BaseController {

	@Autowired
	protected EventService eventService;
	
	@PreAuthorize("hasAuthority('" + PermissionNames.NOTIFICATIONS_VIEW + "')")
	@ResponseBody
	@RequestMapping(value = APIUrls.NOTIFICATIONS, method = RequestMethod.GET)
	public APIPagedResponse<DTO, NotificationDTO> notifications(Principal p,
				@RequestParam(value = ParamNames.PAGE, required = false) Integer page,
				@RequestParam(value = ParamNames.SIZE, required = false) Integer size,
				@RequestParam(value = ParamNames.EVENT, required = false) List<String> notification)
						throws Exception {
		return Success(eventService.getNotificationDTOs(getUserId(p), 
				constructEventTypes(notification), 
				constructPageable(page, size)));
	}
	
}
