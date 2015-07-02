package com.vi.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.vi.common.Item;


public class Utils {

	private static LogUtils log = new LogUtils("Util");
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
        String[] r = "http://portal.settrade.com/blog/nivate/2012/02/20/1099".split("/");
        for(int i=0;i<r.length;i++){
        	if(r[i].matches("\\d+")){
               log.debug(r[i]);
        	}
        }
	}
	public static String validFileName(String s){
		if(isNull(s).equals(""))return "_untitled_"+new Date().getTime();
		return renameWrongFileName(s);
	}
	public static String getValidFolderName(String s){
		if(isNull(s).equals("ดร.นิเวศน์ เหมวชิรวรากร") || isNull(s).startsWith("ดร.นิเวศน์ เหมวชิรวรากร")){
			s = "DR_NIVET";
		}else{
		    s = s.replaceAll(" ", "_");
		}
		return s;
	}
	
	public static Date convertLinkTextToDate(String linkText){
		int c = 0;
		String yyyy = "";
		String mm = "";
		String dd ="01";
		try{
			String[] r =linkText.split("/");
			  for(int i=0;i<r.length;i++){
		        	if(r[i].matches("\\d+")){
		               //log.debug(r[i]);
		        		if(c==0) yyyy = r[i];
		        		if(c==1)  mm = r[i];
		        		c++;
		        	}
		        	
		        }
			  
			  if(!"".equals(yyyy) && !"".equals(mm)){
			      SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.US); 
			      Date convertedDate = dateFormat.parse(yyyy+"/"+mm+"/"+dd); 
			
			     return convertedDate;
			  }
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	public static Date convertToDate(String d) throws Exception{
		if(d == null ||(d != null && d.trim().equals(""))) return new Date();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.US); 
	    Date convertedDate = dateFormat.parse(d);
	
	    return convertedDate;
	}
	
	public static Date convertToDate(String d,String formatDate) throws Exception{
		if(d == null ||(d != null && d.trim().equals(""))) return null;
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatDate,Locale.US); 
	    Date convertedDate = dateFormat.parse(d.trim());
	
	    return convertedDate;
	}
	public static Date convertToDateTh(String d,String formatDate) throws Exception{
		if(d == null ||(d != null && d.trim().equals(""))) return new Date();
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(formatDate,new Locale("TH","th")); 
	    Date convertedDate = dateFormat.parse(d.trim());
	
	    return convertedDate;
	}
	
	public static String convertToString(Date d) {
		if(d == null) return "";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd",Locale.US); 
	    String convertedDate = dateFormat.format(d);
	
	     return convertedDate;
	}
	
	public static Map<String, String> convertListToMap(List list ,Object obj){
		if(list== null ||(list != null && list.size() ==0))
			return  new HashMap<String,String>();

		if(obj instanceof Item){
			Map<String,String> map = new HashMap<String,String>();
			for (int i=0;i<list.size();i++) {
				Item b =(Item)list.get(i);
				map.put(b.getDescription(),b.getDescription());
			}
			return map;
		}
		return null;
	}
	
	public static String trim(String o){
		if(o == null) return "";
		return o.trim();
	}
	public static long isNullLong(String o,long d){
		if(o == null) return d;
		return (new Long(o)).longValue();
	}
	
	public static long isNullLong(String o){
		if(o == null) return 0;
		return (new Long(o)).longValue();
	}
	public static int isNullInt(String o,int d){
		if(o == null) return d;
		return Integer.parseInt(o);
	}
	
	public static int isNullInt(String o){
		try{
		  if(o == null || (o != null && "".equals(o))) return 0;
		  return Integer.parseInt(o);
		}catch(Exception e){
			log.debug("Error["+o+"]\n"+e.getMessage());
		}
		return 0;
	}
	
	public static String isNull(String o){
		if(o == null) return "";
		return o.trim();
	}
	public static String isNull(Object o){
		if(o == null) return "";
		if(o instanceof String)
		  return ((String)o).trim();
		
		return o.toString();
	}
	
	public static String isNull(String o,String d){
		if(o == null) return d;
		return o.trim();
	}
	public static String toString(int o){
		return String.valueOf(o);
	}
	
	public static String toString(float o){
		return String.valueOf(o);
	}
	
	public static float isNullFloat(String o){
		if(o == null) return 0;
		return Float.parseFloat(o);
	}
	
	public static String renameWrongFileName(String fileName){
		if(isNull(fileName).equals("")) return "_"+new Date().getTime()+"_unknow_file_name";
		String newFileName = fileName.replaceAll("[\\/:''*?<>|]+","");
		log.debug("resultString2:"+newFileName);
		
		return newFileName;
	}
	
	public static String getFileNameFromLink(String link){
		if(isNull(link).equals("")) return "_"+new Date().getTime()+"_unknow_file_name";
		link = link.substring(link.indexOf("www"),link.length());
		String newFileName = link.replaceAll("[\\/:*?<>|+-.]","_");
		return newFileName;
	}
}
