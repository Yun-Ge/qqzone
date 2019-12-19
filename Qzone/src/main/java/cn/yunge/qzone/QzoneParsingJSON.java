package cn.yunge.qzone;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openqa.selenium.Cookie;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

//<i QQ空间说说解析
@SuppressWarnings("rawtypes")
public class QzoneParsingJSON {
	
	//<i 解析QQ空间说说
	public static JSONObject parsingQzoneSay(String id, String s_key, Set<Cookie> cookies, String html) {
		JSONArray resultJSONArray = new JSONArray();
		
		Integer g_tk = getGtk(s_key); //<i 获取g_tk
		String qzonetoken = getQzonetoken(html); //<i 获取qzonetoken
		
		String json1 = sendRequest(id, cookies, g_tk, qzonetoken, 0);//<i 发出请求
		//<i 判断是否有权限查看空间说说
		if(isQzonePower(json1)) {
			return CommonParsingJSON.error_response(201, " 对不起,主人设置了保密,您没有权限查看");
		}else if(isSay(json1)) {//<i 判断空间是否有说说
			return CommonParsingJSON.response_ok(200, id, "佛说：一切皆为虚幻，梦幻繁花，眼前的世界也不过是霓虹灯下的一处幻影",
					resultJSONArray);
		}
		
		resultJSONArray = parsingSay(json1, resultJSONArray); //<i 解析说说
		
		JSONObject jsonObject1 = JSONObject.fromObject(json1);//<i 转换为JSON对象字符串
		Integer total = jsonObject1.getInt("total"); //<i 获取说说总数
		Integer page = total / 40; //<i 每一次请求最多只能获取40条说说，所以要分次请求所有
		for(int i = 1; i <= page; i++) {
			String json = sendRequest(id, cookies, g_tk, qzonetoken, i * 40);//<i 发出请求
			resultJSONArray = parsingSay(json, resultJSONArray); //<i 解析说说
		}	

		return CommonParsingJSON.response_ok(200, id, "佛说：一切皆为虚幻，梦幻繁花，眼前的世界也不过是霓虹灯下的一处幻影",
				resultJSONArray);
	}
	//<i 解析说说  获取说说JSON
	public static JSONArray parsingSay(String json, JSONArray resultJSONArray) {
		
		JSONObject jsonObject = JSONObject.fromObject(json);//<i 转换为JSON对象字符串
		JSONArray msglist = jsonObject.getJSONArray("msglist"); //<i 解析msglist
		
		Iterator it = msglist.iterator(); //<i 遍历所有说说
		while(it.hasNext()) {
			JSONObject say_json = new JSONObject();
			JSONObject qzone_say = JSONObject.fromObject(it.next()); //<i 获取当前说说的JSON
			
			int cmtnum =  qzone_say.getInt("cmtnum");
			say_json.put("cmtnum", cmtnum);//<i 获取评论总回复数

			//<i 判断是否有评论，如果有则获取评论列表
			if(cmtnum > 0) {say_json.put("commentlist", getComment(qzone_say));}
			
			say_json.put("content", qzone_say.getString("content"));//<i 获取发的说说
			say_json.put("createTime", qzone_say.getString("createTime"));//<i 获取创建说说时间
			say_json.put("name", qzone_say.getString("name")); //<i 获取名字
			say_json.put("source_name", qzone_say.getString("source_name"));//<i 获取发说说的来源，也就是用什么手机发说说的
			
			//<i 判断发的说说是否携带了图片，如果存在则 获取说说携带的图片
			if(qzone_say.has("pic")) {say_json.put("pic", getPic(qzone_say));}
			//<i 判断是否有转发
			if(qzone_say.has("rt_con")) {say_json.put("rt_con", getRt_con(qzone_say));}
			resultJSONArray.add(say_json);
		}
		return resultJSONArray;
	}
	
	//<i 获取转发内容
	public static JSONObject getRt_con(JSONObject qzone_say) {
		JSONObject rt_con = qzone_say.getJSONObject("rt_con");
		JSONObject rt_conJSON = new JSONObject();
		rt_conJSON.put("content", rt_con.getString("content"));
		return rt_conJSON;
	}
	
	//<i 获取说说评论列表
	public static JSONArray getComment(JSONObject qzone_say) {
		JSONArray commentlist = qzone_say.getJSONArray("commentlist"); //<i 获取回复信息
		Iterator commentIt = commentlist.iterator();
		JSONArray commentArray = new JSONArray();
		while(commentIt.hasNext()) {
			JSONObject comment = new JSONObject();
			JSONObject commentJSON = JSONObject.fromObject(commentIt.next());
			comment.put("content", commentJSON.getString("content"));
			comment.put("createTime", commentJSON.getString("createTime"));
			comment.put("createTime2", commentJSON.getString("createTime2"));
			comment.put("name", commentJSON.getString("name"));
			comment.put("reply_num", commentJSON.getString("reply_num"));
			comment.put("source_name", commentJSON.getString("source_name"));
			comment.put("uin", commentJSON.getString("uin"));
			//<i 获取评论图片
			if(commentJSON.has("pic")) {comment.put("pic", getCommentPic(commentJSON));}
			//<i 获取评论回复
			if(commentJSON.has("list_3")) {comment.put("replylist", getCommentReply(commentJSON));}
			commentArray.add(comment);
		}
		return commentArray;
	}
	
	//<i 获取评论图片
	public static JSONArray getCommentPic(JSONObject commentJSON) {
		JSONArray commentPic = commentJSON.getJSONArray("pic");
		Iterator itPic = commentPic.iterator(); //<i 遍历所有说说
		JSONArray picArray = new JSONArray();
		while(itPic.hasNext()) {
			JSONObject picJSON = JSONObject.fromObject(itPic.next());
			picArray.add(picJSON.getString("b_url"));
		}
		return picArray;
	}
	
	//<i 获取评论回复
	public static JSONArray getCommentReply(JSONObject commentJSON) {
		JSONArray list_3 = commentJSON.getJSONArray("list_3");
		Iterator itList_3 = list_3.iterator(); //<i 遍历所有说说
		JSONArray replyArray = new JSONArray();
		while(itList_3.hasNext()) {
			JSONObject reply = new JSONObject();
			JSONObject replyJSON = JSONObject.fromObject(itList_3.next());
			reply.put("content", replyJSON.getString("content"));
			reply.put("createTime", replyJSON.getString("createTime"));
			reply.put("createTime2", replyJSON.getString("createTime2"));
			reply.put("name", replyJSON.getString("name"));
			reply.put("source_name", replyJSON.getString("source_name"));
			reply.put("uin", replyJSON.getString("uin"));
			replyArray.add(reply);
		}
		return replyArray;
	}
	
	//<i 获取用户发的说说图片
	public static JSONArray getPic(JSONObject qzone_say) {
		JSONArray picArrays = qzone_say.getJSONArray("pic");
		Iterator picIt = picArrays.iterator();
		JSONArray picArray = new JSONArray();
		while(picIt.hasNext()) {
			JSONObject picJSON = JSONObject.fromObject(picIt.next());
			picArray.add(picJSON.getString("url3"));//<i 获取说说携带的图片
		}
		return picArray;
	}
	
	//<i 判断空间是否设置权限
	public static boolean isQzonePower(String json) {
		JSONObject jsonObject = JSONObject.fromObject(json);//<i 转换为JSON对象字符串
		if(jsonObject.has("msglist")) {
			return false;
		}
		return true;
	}
	
	//<i 判断空间是否有说说
	public static boolean isSay(String json) {
		JSONObject jsonObject = JSONObject.fromObject(json);//<i 转换为JSON对象字符串
		if(!"null".equals(jsonObject.get("msglist").toString())) {
			return false;
		}
		return true;
	}
	
	
	//<i 获取gtk
	public static Integer getGtk(String p_skey) {
	     Integer hashes = 5381;
	     for(char p : p_skey.toCharArray()) {
	    	 hashes += (hashes << 5) + (int)p;
	     }
	     return hashes & 0x7fffffff;
	}
	
	//<i 获取qzonetoken
	public static String getQzonetoken(String html) {
		String qzonetoken = "";
		String regx = "\\(function\\(\\)\\{ try\\{return (.*?);\\} catch\\(e\\)";
		Pattern pattern = Pattern.compile(regx,Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(html);
		if(matcher.find()) {
			String token = matcher.group();
			qzonetoken = token.substring(token.indexOf("\"")+1, token.lastIndexOf("\""));
		}
		return qzonetoken;
	}
	
	//<i 发出请求  获取JSON格式的数据
	public static String sendRequest(String id, Set<Cookie> cookies,  Integer g_tk, String qzonetoken, Integer pos) {
		String url = "https://user.qzone.qq.com/proxy/domain/taotao.qq.com/cgi-bin/emotion_cgi_msglist_v6?uin="+id+"&ftype=0&sort=0&pos="+pos+"&num=40&replynum=100&g_tk="+g_tk+"&callback=_preloadCallback&code_version=1&format=json&need_private_comment=1&qzonetoken="+qzonetoken;
		String cookie = formatCookie(cookies);
		StringBuffer json = requestGetUrl(url, cookie);
		return json.toString();
	}
	
	//<i 格式化Cookie
	public static String formatCookie(Set<Cookie> cookies) {
		String str = "";
		for (Cookie cookie : cookies) {
			str += cookie.getName()+"="+cookie.getValue()+";";
		}
		return str;
	}
	
	//<i 请求  必须携带Cookie 因为只有携带了Cookie QQ空间才指定你是登录状态
	public static StringBuffer requestGetUrl(String url, String cookie) {
		String line = ""; // <i 循环读取
		StringBuffer buffer = new StringBuffer();// <i 访问返回结果
		BufferedReader read = null;
		try {
			URL readUrl = new URL(url); // i> 创建URL

			HttpURLConnection connection = (HttpURLConnection) readUrl.openConnection();// i> 打开连接

			// <i 设置通用的请求属性
			connection.setRequestProperty("accept", "*/*");
			connection.setRequestProperty("connection", "Keep-Alive");
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; …) Gecko/20100101 Firefox/57.0");
			connection.setRequestProperty("Cookie", cookie);
			connection.setRequestProperty("Host", "user.qzone.qq.com");
			connection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,zh-TW;q=0.7,zh-HK;q=0.5,en-US;q=0.3,en;q=0.2");
	
			read = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8")); 	// <i 定义BufferedReader读取URL的输出流
			while ((line = read.readLine()) != null) {
				buffer.append(line); // <i 读取返回结果
			}
			read.close();
		} catch (Exception e) {}
		return buffer; 
	}
}

