package com.vi.authen;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vi.utils.FileUtil;
import com.vi.utils.LogUtils;
 
public class ThaiVIAuthen implements Serializable{
 
  private static final long serialVersionUID = 8555673334143494964L;
  private List<String> cookies;
  private HttpURLConnection conn;
  private final String USER_AGENT = "Mozilla/5.0";
  LogUtils log = new LogUtils("ThaiVIAuthen");
  
  // .data("username", "neung001", "password", "84438218")
  public static void main(String[] args) throws Exception {
 
	String url = "http://board.thaivi.org/ucp.php";
	String pageGet = "http://board.thaivi.org/viewtopic.php?f=49&t=53486";
 
	ThaiVIAuthen http = new ThaiVIAuthen();
 
	// make sure cookies is turn on
	CookieHandler.setDefault(new CookieManager());
 
	// 1. Send a "GET" request, so that you can extract the form's data.
	String page = http.GetPageContent(url);
	String postParams = http.getFormParams(page, "wwww", "12345");
 
	// 2. Construct above post's content and then send a POST request for
	// authentication
	http.sendPost(url, postParams);
 
	// 3. success then go to gmail.
	String result = http.GetPageContent(pageGet);
	
	//FileUtil.writeFile("d:/logs/thavi.html", result);
	
	//log.debug(result);
  }
  
  public ThaiVIAuthen initSessionLogin(String userName,String password) {
	  ThaiVIAuthen httpSessionLogin = new ThaiVIAuthen();
	  try{
		  httpSessionLogin =  initSessionLoginModel(userName,password);
	  }catch(Exception e){
		  log.debug("Error init Thaivi login Fail: Retry conn");
		  e.printStackTrace();
		  try{
		     httpSessionLogin =  initSessionLoginModel(userName,password);
		  }catch(Exception ee){}
	  }
	  return httpSessionLogin;
  }
  
  public ThaiVIAuthen initSessionLoginModel(String userName,String password) throws Exception {
		ThaiVIAuthen httpSessionLogin = new ThaiVIAuthen();
		try{
			String loginUrl= "http://board.thaivi.org/ucp.php";
			
			// make sure cookies is turn on
			CookieHandler.setDefault(new CookieManager());
		 
			// 1. Send a "GET" request, so that you can extract the form's data.
			String page = httpSessionLogin.GetPageContent(loginUrl);
			String postParams = httpSessionLogin.getFormParams(page, userName, password);
		 
			// 2. Construct above post's content and then send a POST request for
			// authentication
			httpSessionLogin.sendPost(loginUrl, postParams);
		}catch(Exception e){
			throw e;
		}
	return httpSessionLogin;
}

  private void sendPost(String url, String postParams) throws Exception {
	URL obj = new URL(url);
	conn = (HttpURLConnection) obj.openConnection();
 
	// Acts like a browser
	conn.setUseCaches(false);
	conn.setRequestMethod("POST");
	conn.setRequestProperty("Host", "board.thaivi.org");
	conn.setRequestProperty("User-Agent", USER_AGENT);
	conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	for (String cookie : this.cookies) {
		conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
	}
	conn.setRequestProperty("Connection", "keep-alive");
	conn.setRequestProperty("Referer", "http://board.thaivi.org/ucp.php?mode=login");
	conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	conn.setRequestProperty("Content-Length", Integer.toString(postParams.length()));
 
	conn.setDoOutput(true);
	conn.setDoInput(true);
 
	// Send post request
	DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	wr.writeBytes(postParams);
	wr.flush();
	wr.close();
 
	int responseCode = conn.getResponseCode();
	//log.debug("\n Sending 'POST' request to URL : " + url);
	//log.debug("Post parameters : " + postParams);
	log.debug("sendPost Response Code : " + responseCode);
 
	BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();
 
	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();
   // log.debug(response.toString());
 
  }
 
  public String GetPageContent(String url) throws Exception {
	URL obj = new URL(url);
	conn = (HttpURLConnection) obj.openConnection();
 
	// default is GET
	conn.setRequestMethod("GET");
	conn.setUseCaches(false);
 
	// act like a browser
	conn.setRequestProperty("User-Agent", USER_AGENT);
	conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
	conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
	if (cookies != null) {
		for (String cookie : this.cookies) {
			conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
		}
	}
	int responseCode = conn.getResponseCode();
	//log.debug("\n Sending 'GET' request to URL : " + url);
	log.debug("GetPageContent Response Code : " + responseCode);
 
	BufferedReader in =  new BufferedReader(new InputStreamReader(conn.getInputStream()));
	String inputLine;
	StringBuffer response = new StringBuffer();
 
	while ((inputLine = in.readLine()) != null) {
		response.append(inputLine);
	}
	in.close();
 
	// Get the response cookies
	setCookies(conn.getHeaderFields().get("Set-Cookie"));
	return response.toString();
  }
 
  public InputStream getImages(String url) throws Exception {
	  InputStream in = null;
	  try{
		URL obj = new URL(url);
		conn = (HttpURLConnection) obj.openConnection();
	 
		// default is GET
		conn.setRequestMethod("GET");
		conn.setUseCaches(false);
	 
		// act like a browser
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Accept","text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
		if (cookies != null) {
			for (String cookie : this.cookies) {
				conn.addRequestProperty("Cookie", cookie.split(";", 1)[0]);
			}
		}
		int responseCode = conn.getResponseCode();
		//log.debug("\n Sending 'GET' request to URL : " + url);
		log.debug("getImages Response Code : " + responseCode);
	 
		in = conn.getInputStream();
		
		// Get the response cookies
		setCookies(conn.getHeaderFields().get("Set-Cookie"));
		
	  }catch(Exception e){
		  e.printStackTrace();
	  }
	  return in;
  }
	 
  public String getFormParams(String html, String username, String password)throws UnsupportedEncodingException {
	log.debug("Extracting form's data...");
	Document doc = Jsoup.parse(html);
 
	// Google form id
	//Element loginform = doc.getElementById("gaia_loginform");
	Elements inputElements = doc.getElementsByTag("input");
	List<String> paramList = new ArrayList<String>();
	for (Element inputElement : inputElements) {
		String key = inputElement.attr("name");
		String value = inputElement.attr("value");
 
		if (key.equals("username"))
			value = username;
		else if (key.equals("password"))
			value = password;
		paramList.add(key + "=" + URLEncoder.encode(value, "UTF-8"));
	}
 
	// build parameters list
	StringBuilder result = new StringBuilder();
	for (String param : paramList) {
		if (result.length() == 0) {
			result.append(param);
		} else {
			result.append("&" + param);
		}
	}
	return result.toString();
  }
 
  public List<String> getCookies() {
	return cookies;
  }
 
  public void setCookies(List<String> cookies) {
	this.cookies = cookies;
  }
 
}