package com.vi.proxy;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

public class ProxyHelper {

	public static HttpURLConnection openConnection(URL url){
	 try{
		 //Case have Proxy Server
		//Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("10.100.1.44", 8080));
		//HttpURLConnection uc = (HttpURLConnection)url.openConnection(proxy);
		
		//Proxy Server
		 HttpURLConnection uc = (HttpURLConnection)url.openConnection();
	    return uc;
	 }catch(Exception e){
		e.printStackTrace();
	 }
	 return null;
	}
}
