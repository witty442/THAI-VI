package com.vi.utils;

import java.util.ArrayList;
import java.util.List;

public class ArticleUtils {
	private static LogUtils log = new LogUtils("ArticleUtils");
	
	public static StringBuffer customArticle(String ar){
		StringBuffer newAr = new StringBuffer("");
		try{

			log.debug("ar Length:"+ar.length());
			String arArr[] = ar.split(" ");
			log.debug("arArr Length:"+arArr.length);
			List<String> posNewLine = new ArrayList<String>();
			int indexNewLine = 0;
			int countBlank = 0;
			for(int i=0;i<arArr.length;i++){
				log.debug("i["+i+"]:"+arArr[i]);
				if("".equalsIgnoreCase(Utils.isNull(arArr[i]))){
					indexNewLine = 0;
					countBlank++;
				}else{
					if(countBlank >=3){
						indexNewLine = ar.indexOf(arArr[i]);
						posNewLine.add(indexNewLine+"");
						log.debug("indexNewLine:"+indexNewLine);
					}
					//reset count 
					countBlank =0;
				}
			}
			
			log.debug("posNewLine Size:"+posNewLine.size());
			int startPos = 0;
			int endPos = 0;
			if(posNewLine !=null && posNewLine.size() >0){
				for(int i=0;i<posNewLine.size();i++){
					if(i==0){
						startPos = 0;
						endPos =Utils.isNullInt(posNewLine.get(i),0);
						
					}else{
						startPos = Utils.isNullInt(posNewLine.get(i-1),0);
						endPos = Utils.isNullInt(posNewLine.get(i),0);
					}
					log.debug("startPos:"+startPos+":ensPos:"+endPos);
					newAr.append("<br/>....... n"+ar.substring(startPos ,endPos));
				}
			}
			
			startPos = endPos;
			endPos = ar.length();
			log.debug("startPos:"+startPos+":ensPos:"+endPos);
			newAr.append("<br/>....... n"+ar.substring(startPos ,endPos));
			
			log.debug("********newAr\n["+newAr.toString()+"]\n************");
			
            //customArticle(ar);
		}catch(Exception e){
			e.printStackTrace();
		}
		return newAr;
	}
}
