package com.py.py.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.py.py.constants.APIUrls;
import com.py.py.dto.APIResponse;
import com.py.py.dto.out.TotalValueDTO;
import com.py.py.service.PostingService;

@Controller
public class StatisticsController extends BaseController {

	@Autowired
	protected PostingService postingService;
	
	@ResponseBody
	@RequestMapping(value = APIUrls.STATS_TOTALS)
	public APIResponse<TotalValueDTO> totals() throws Exception {
		return Success(postingService.getTotalValueDTO());
	}
}
