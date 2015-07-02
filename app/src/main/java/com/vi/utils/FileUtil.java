package com.vi.utils;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.content.res.AssetManager;


public class FileUtil {
	private static LogUtils log = new LogUtils("FileUtils");

 public static BufferedReader getBufferReaderFromClassLoader(String filename) throws Exception {
  BufferedReader br = null;
  ClassLoader cl = FileUtil.class.getClassLoader();
  InputStream in = cl.getResourceAsStream(filename);
  InputStreamReader isr = new InputStreamReader(in);
  br = new BufferedReader(isr);
  return br;
}

  public static String readFile(String filename) throws Exception {
    File file = new File(filename);
      FileInputStream fis = null;
      BufferedInputStream bis = null;
      DataInputStream dis = null;
        String str ="";
      try {
          fis = new FileInputStream(file);
  
          // Here BufferedInputStream is added for fast reading.
          bis = new BufferedInputStream(fis);
          dis = new DataInputStream(bis);

          // dis.available() returns 0 if the file does not have more lines.
          while (dis.available() != 0) {
             str +=dis.readLine()+"\n";
          }
  
          // dispose all the resources after using them.
          fis.close();
          bis.close();
          dis.close();
        return str;
      } catch (FileNotFoundException e) {
         throw e;
      } catch (IOException e) {
         throw e;
      }
  }
  
  public static String readFile(InputStream fis) throws Exception {
	      BufferedInputStream bis = null;
	      DataInputStream dis = null;
	        String str ="";
	      try {
	          // Here BufferedInputStream is added for fast reading.
	          bis = new BufferedInputStream(fis);
	          dis = new DataInputStream(bis);

	          // dis.available() returns 0 if the file does not have more lines.
	          while (dis.available() != 0) {
	             str +=dis.readLine()+"\n";
	          }
	  
	          // dispose all the resources after using them.
	          fis.close();
	          bis.close();
	          dis.close();
	        return str;
	      } catch (FileNotFoundException e) {
	         throw e;
	      } catch (IOException e) {
	         throw e;
	      }
	  }
	  


  public static String readFile(String fileName,String encoding)  {
    
     InputStream is = null;
     Writer writer = new StringWriter();
       try{
         is = new FileInputStream(fileName);
           Reader reader = null;
         char[] buffer = new char[1024];
            try {
              reader = new BufferedReader(new InputStreamReader(is,encoding));
                int n;
                while ((n = reader.read(buffer)) != -1) {
                     writer.write(buffer, 0, n);
                 }
            }finally {
                if(is != null){
                  is.close();
                  is =null;
                }
                if(reader != null){
                  reader.close();
                  reader = null;
                }
                writer.flush();
            }
              
       }catch(Exception e){
        // logger.error(e.getMessage(),e);
       }
       return writer.toString();
    }
  
  public static String readFile(InputStream is,String encoding)  {
	    
	     Writer writer = new StringWriter();
	       try{
	           Reader reader = null;
	           char[] buffer = new char[1024];
	            try {
	              reader = new BufferedReader(new InputStreamReader(is,encoding));
	                int n;
	                while ((n = reader.read(buffer)) != -1) {
	                     writer.write(buffer, 0, n);
	                 }
	            }finally {
	                if(is != null){
	                  is.close();
	                  is =null;
	                }
	                if(reader != null){
	                  reader.close();
	                  reader = null;
	                }
	                writer.flush();
	            }
	              
	       }catch(Exception e){
	        // logger.error(e.getMessage(),e);
	       }
	       return writer.toString();
	    }
  
  public static void writeFile(String fileName,String str) {
    FileWriter fstream =null;
    try{
        // Create file 
        fstream = new FileWriter(fileName);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write(str);
        //Close the output stream
        out.close();
        fstream.close();
        
      }catch (Exception e){//Catch exception if any
         //logger.error("Write File Error:"+e.getMessage(),e);
      }
    }
  
  public static void writeFile(String fileName,byte[] bytefile) throws Exception{
    FileOutputStream fos = null;
    try {
        fos = new FileOutputStream( fileName );
        fos.write( bytefile );       
    }
      catch (Exception ex){
        //logger.error( ex.getMessage(), ex);
        throw ex;
      }finally{
          if(fos != null){
            fos.close();              
          }
      }      
    }
  /**
   * get file content from classloader path
   */
  public static String getClassloaderContentStr(String filename) throws Exception {

    return getClassloaderContentStr(filename,false);
  }
  
  
  public static String getClassloaderContentStr(String filename,boolean preserveNewLine) throws Exception {
    String r = "";
    String newLine = System.getProperty("line.separator");
    BufferedReader br = getBufferReaderFromClassLoader(filename);
    try {
      String temp = null;
      while ((temp = br.readLine()) != null) {
        r += temp+" ";
        if (preserveNewLine) {
          r += newLine;
        }
      }
    } finally {
      close(br);
    }
    return r;
  }

  /**
   * get file content full path
   */
  

  public static void close(BufferedReader br) throws IOException {
    if (br != null) {
      br.close();br=null;
    }
  }


  public static File createFile(String fileName, byte[] fileData) throws IOException {
    createParentDir(fileName);
    File destFile = new File(fileName);
   // logger.debug("create file =" + destFile.getAbsolutePath());
    FileOutputStream fo = null;
    try {
      fo = new FileOutputStream(destFile);
      fo.write(fileData);
    } finally {
      if(fo != null)fo.close();
    }

    return destFile;
  }

  public static void createFile(String fileName) throws Exception {
    PrintWriter pOut = null;
    createParentDir(fileName);
    //logger.debug("create file=" + fileName);
    try {
      pOut = new PrintWriter(new BufferedWriter(new FileWriter(fileName, false)));
      // pOut.println(out);
    } finally {
      if (pOut != null) {
        pOut.close();
      }
    }
  }

  public static void createDir(String dirName) {
    // create dir
    File f = new File(dirName);
    File dir = new File(f.getAbsolutePath());
    if (!dir.exists()) {
     // logger.debug("create dir=" + dir);
      dir.mkdirs();
    }
  }

  public static void createParentDir(String fileName) {
    // create dir
    File f = new File(fileName);
    f = f.getParentFile();
    createDir(f.getAbsolutePath());
  }

  public static void deleteFile(String fileName) {
    // create dir
    try{
       File f = new File(fileName);
       f.delete();
    }catch(Exception e){
      //logger.error(e.getMessage(),e);
    }
  }
  
  public static boolean deleteDir(File dir) {
	    if (dir != null && dir.isDirectory()) {
	       String[] children = dir.list();
	       for (int i = 0; i < children.length; i++) {
	    	  log.debug("Folder "+children[i]);
	          boolean success = deleteDir(new File(dir, children[i]));
	          if (!success) {
	             return false;
	          }
	       }
	    }
	    //log.debug("Delete "+dir.getName());
	    // The directory is now empty so delete it
	    if(dir != null){
	    	return dir.delete();
	    }
	    return false;
   }

}

