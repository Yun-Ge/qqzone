package cn.yunge.qzone;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Set;

import javax.imageio.ImageIO;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.springframework.stereotype.Service;

import net.sf.json.JSONObject;

@Service
public class QzoneService {
	
	//<i 获取说说
	public JSONObject getQzone(String id, String qqaccount, String password) {
		System.setProperty("webdriver.chrome.driver", "C://chromedriver.exe");
		ChromeOptions options = new ChromeOptions(); // <i 创建chrome参数对象
		//options.addArguments("headless"); // <i 浏览器后台运行
		WebDriver webDriver = new ChromeDriver(options);

		Set<Cookie> cookies = mockUserLogin(webDriver, qqaccount, password);//<i 获取Cookie
		String s_key = isCookies(cookies); //<i 获取s_key，用来获取gtk
		if("-1".equals(s_key)) { //<i 判断Cookie是否获取到了，如果没有，则说明模拟登录失败，返回错误信息
			return CommonParsingJSON.error_response(-2, "可能发生了一些错误，请刷新重试");
		}
		
		return QzoneParsingJSON.parsingQzoneSay(id, s_key, cookies, webDriver.getPageSource());
	}
	
	//<i 模拟用户登录
	public static Set<Cookie> mockUserLogin(WebDriver webDriver, String qqaccount, String password) {

		webDriver.get("https://i.qq.com/"); //<i 发出请求
		webDriver.switchTo().frame("login_frame"); //<i 获取到登录区域
		webDriver.findElement(By.xpath("//*[@id=\"switcher_plogin\"]")).click(); // 模拟点击
		webDriver.findElement(By.xpath("//*[@id=\"u\"]")).sendKeys(qqaccount); //<i 设置登录账号
		webDriver.findElement(By.xpath("//*[@id=\"p\"]")).sendKeys(password); //<i 设置登录密码
		webDriver.findElement(By.xpath("//*[@id=\"login_button\"]")).click(); //<i 模拟点击登录

		delay(); // <i 时间延迟4秒

		WebDriver validate = webDriver.switchTo().frame(webDriver.findElement(By.tagName("iframe")));// <i 获取到验证区域
		WebElement slideBkg = validate.findElement(By.xpath("//*[@id=\"slideBlock\"]"));// 获取滑动验证图片
		WebElement tcOperationBkg = validate.findElement(By.xpath("//*[@id=\"tcOperation\"]"));// 获取整个验证图层

		String yinyingUrl = slideBkg.getAttribute("src"); // <i 获取阴影小图
		String newUrl = yinyingUrl.replace("hycdn_2_", "hycdn_1_"); //<i 获取新大图
		String oldUrl = yinyingUrl.replace("hycdn_2_", "hycdn_0_"); //<i 获取原始大图

		BufferedImage oldBmp = GetImg(oldUrl); //<i 获取新大图Image对象
		BufferedImage newBmp = GetImg(newUrl); //<i 获取原始大图Image对象
		
		int left = GetArgb(oldBmp, newBmp); // 得到阴影到图片左边界的像素
		double leftCount = (double) tcOperationBkg.getSize().getWidth() / (double) newBmp.getWidth() * left;
		int leftShift = (int) leftCount - 30; //<i 得到真实的偏移值
		
		WebElement slideBlock = validate.findElement(By.xpath("//*[@id=\"slideBlock\"]"));// 获取到滑块
		Actions actions = new Actions(webDriver);
		actions.dragAndDropBy(slideBlock, leftShift, 0).build().perform();// 单击并在指定的元素上按下鼠标按钮,然后移动到指定位置
		
		delay(); // <i 时间延迟4秒
		return webDriver.manage().getCookies();
	}

	// <i 时间延迟
	public static void delay() {
		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	// <i 根据Cookie判断是否验证成功
	public static String isCookies(Set<Cookie> cookies) {
		for (Cookie cookie : cookies) {
			if ("p_skey".equals(cookie.getName())) {
				return cookie.getValue();
			}
		}
		return "-1";
	}

	// <i 由于阴影图片四个角存在黑点(矩形1*1)
	public static int GetArgb(BufferedImage oldBmp, BufferedImage newBmp) {
		for (int i = 0; i < newBmp.getWidth(); i++) {
			for (int j = 0; j < newBmp.getHeight(); j++) {
				if ((i >= 0 && i <= 1)
						&& ((j >= 0 && j <= 1) || (j >= (newBmp.getHeight() - 2) && j <= (newBmp.getHeight() - 1)))) {
					continue;
				}
				if ((i >= (newBmp.getWidth() - 2) && i <= (newBmp.getWidth() - 1))
						&& ((j >= 0 && j <= 1) || (j >= (newBmp.getHeight() - 2) && j <= (newBmp.getHeight() - 1)))) {
					continue;
				}
				// <i 获取该点的像素的RGB的颜色
				Color oldColor = new Color(oldBmp.getRGB(i, j));
				Color newColor = new Color(newBmp.getRGB(i, j));
				if (Math.abs(oldColor.getRed() - newColor.getRed()) > 60
						|| Math.abs(oldColor.getGreen() - newColor.getGreen()) > 60
						|| Math.abs(oldColor.getBlue() - newColor.getBlue()) > 60) {
					return i;
				}
			}
		}
		return 0;
	}
	
	//<i 根据图片地址，获取图片对象
	public static BufferedImage GetImg(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (Exception e) {}
		return null;
	}
}
