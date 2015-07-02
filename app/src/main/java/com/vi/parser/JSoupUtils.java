package com.vi.parser;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.vi.utils.LogUtils;
import com.vi.utils.Utils;

public class JSoupUtils {

	static LogUtils log = new LogUtils("JSoupUtils");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        String html = "";
       
	}
	
	public static List<String[]> getLinkTagEmbedList(String html){
		List<String[]> returnList = new ArrayList<String[]>();
		String[] returnLinkTag = new String[2];
		try{
			Document doc = Jsoup.parse(html);

			Iterator<Element> its = doc.select("embed").iterator();
			while(its.hasNext()){
				Element link = its.next();
			    returnLinkTag = new String[2];
			    returnLinkTag[0]= link.attr("src"); // "http://jsoup.org/"
			    returnLinkTag[1]= Utils.isNull(link.text()).equals("")?returnLinkTag[0]:link.text(); // Text
			    
			    log.debug("Link:"+returnLinkTag[0]);
			    log.debug("LinkText:"+returnLinkTag[1]);
			    
			    returnList.add(returnLinkTag);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return returnList;
	}
	/**
     * 
     * @param html
     * @return List<String[]>
     * String[2]
     * [0] = href
     * [1] = text
     */
	public static List<String[]> getLinkTagList(String html){
		List<String[]> returnList = new ArrayList<String[]>();
		String[] returnLinkTag = new String[2];
		try{
			Document doc = Jsoup.parse(html);

			Iterator<Element> its = doc.select("a").iterator();
			while(its.hasNext()){
				Element link = its.next();
			    log.debug("Link:"+link.attr("href"));
			    log.debug("LinkText:"+link.text());
			    
			    returnLinkTag = new String[2];
			    returnLinkTag[0]= link.attr("href"); // "http://jsoup.org/"
			    returnLinkTag[1]= Utils.isNull(link.text()).equals("")?returnLinkTag[0]:link.text(); // Text
			    
			    returnList.add(returnLinkTag);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return returnList;
	}
    public static String[] getLinkTags(String html){
		
		String[] returnLinkTag = new String[2];
		try{
			Document doc = Jsoup.parse(html);

			Iterator<Element> its = doc.select("a").iterator();
			while(its.hasNext()){
				Element link = its.next();
			   // log.debug("Link:"+link.attr("href"));
			    
			    returnLinkTag = new String[2];
			    returnLinkTag[0]= link.attr("href"); // "http://jsoup.org/"
			    returnLinkTag[1]= Utils.isNull(link.text()).equals("")?returnLinkTag[0]:Utils.isNull(link.text()).trim(); // Text
			   
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return returnLinkTag;
	}
	public static String getTextOnlyFromLinkTag(String html){
		String s = "";
		try{
		   s = Jsoup.parse(html).text();
		}catch(Exception e){
			e.printStackTrace();
		}
		return s;
	}
}
