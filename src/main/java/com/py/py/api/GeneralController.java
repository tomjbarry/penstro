package com.py.py.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.constants.ResponseCodes;
import com.py.py.dto.APIResponse;
import com.py.py.dto.DTO;

@Controller
public class GeneralController extends BaseController {

	@ResponseBody
	@RequestMapping(value = APIUrls.INDEX, method = RequestMethod.GET)
	public APIResponse<DTO> index() {
		return Success();
	}
	
	@ResponseBody
	@RequestMapping(value = APIUrls.INVALID)
	public APIResponse<DTO> invalid() {
		return Failure(ResponseCodes.INVALID);
	}
	
	@ResponseBody
	@RequestMapping(value = APIUrls.FAILURE)
	public APIResponse<DTO> failure() {
		return Failure(ResponseCodes.FAILURE);
	}
}
