package cn.yunge.qzone;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/api")
public class QzoneController {
	
	@Autowired
	QzoneService qzoneService;
	
	//<i 查询所有说说记录
	@RequestMapping("/qzone/so/link")
	public @ResponseBody JSONObject qzone(String id, String key, 
			@RequestParam(required=false, defaultValue="xx")String qqaccount, 
			@RequestParam(required=false, defaultValue="xx") String password) {
		return qzoneService.getQzone(id, qqaccount, password);
	}
}
