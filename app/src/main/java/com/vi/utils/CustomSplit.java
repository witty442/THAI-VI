package com.vi.utils;

public class CustomSplit {

	private static LogUtils log = new LogUtils("CustomSplite");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
         split("");
	}
	
	public static String[] split(String s){
		String[] result = new String[2];
		try{
			//s ="<p>TopicName <a href='http://portal.settrade.com/blog/nivate/2012/02/20/1099'>Continue reading <span class='meta-nav'>&rarr;</span></a></p>";
			result[0] = s.substring(s.indexOf("<p>")+3 , s.indexOf("<a"));
			result[1] = s.substring(s.indexOf("href")+6,s.indexOf("Continue")-2);
			
			//log.debug(result[0] );
			//log.debug(result[1] );
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return result;
	}

}
