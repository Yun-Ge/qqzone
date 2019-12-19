package cn.yunge.qzone;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class CommonParsingJSON {
	//<i 响应错误信息
	public static JSONObject error_response(Integer code, String errorMesage) {
		JSONArray resultJSONArray = new JSONArray();
		JSONObject resultJSONObject = new JSONObject();
		resultJSONObject.put("code", code);
		resultJSONObject.put("errorMesage", errorMesage);
		resultJSONObject.put("data", resultJSONArray);
		return resultJSONObject;
	}
	
	//<i 包装响应的JSON数据
	public static JSONObject response_ok(Integer code, String keyword, String saying, JSONArray data) {
		JSONObject resultJSONObject = new JSONObject();
		resultJSONObject.put("code", code);
		resultJSONObject.put("keyword", keyword);
		resultJSONObject.put("saying", saying);
		resultJSONObject.put("data", data);
		return resultJSONObject;
	}
}
