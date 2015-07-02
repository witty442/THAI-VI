package com.vi.adapter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.widget.ImageView;

import com.vi.R;
import com.vi.common.ContentBean;
import com.vi.common.Item;
import com.vi.parser.JSoupHelperAuthen;
import com.vi.parser.JSoupUtils;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.Utils;

public class FeedItemAdapterHelper {

    private static LogUtils log = new LogUtils("FeedItemAdapterHelper");
    static int imageRunning = 0;

    //input = "คอมเม้นท์จากเฟสบุคครับ <br/><img src='http://images.temppic.com/29-12-2014/images_vertis/1419852974_0.60853800.jpg' alt='Image'/>";

    public static List<ContentBean> splitContentToList(JSoupHelperAuthen authen, String s){
        Document doc = null;
        int i=0;
        int childNodeSize = 0;
        List<ContentBean> contentList = new ArrayList<ContentBean>();
        try{
            // doc = Jsoup.parseBodyFragment(s);
            doc = Jsoup.parse(s);
            //log.debug("**********ALL***************");
            //log.debug(""+s);
            //log.debug("**********/ALL***************");
            Element body = doc.body();
            childNodeSize = body.childNodeSize();

            //log.debug("childNodeSize:"+body.childNodeSize());

            for(i=0;i<childNodeSize;i++){
                ContentBean b = new ContentBean();

                Node node =body.childNode(i);
                String nodeName = node.nodeName();
                log.debug(nodeName);
                log.debug("*****Content NodeName["+nodeName+"]:\n["+node.toString()+"]\n****************");

                if(nodeName.equalsIgnoreCase("#text") ){
                    //log.debug("=text");

                    b.setType(Constants.ITEM_TYPE_TEXT);
                    b.setContent(node.toString());

                    contentList.add(b);
                }else if(nodeName.equalsIgnoreCase("img")){
                    //log.debug("=image");

                    b.setType(Constants.ITEM_TYPE_IMAGE);
                    b.setImageUrl(getImageProperty(node.toString()));
                    b.setImageLocal(false);

                    if(b.getImageUrl().indexOf("icon_") != -1
                            || b.getImageUrl().indexOf("cheers") != -1 || b.getImageUrl().indexOf("cool") != -1
                            || b.getImageUrl().indexOf("doi") != -1 || b.getImageUrl().indexOf("drink") != -1
                            || b.getImageUrl().indexOf("liverpool") != -1 || b.getImageUrl().indexOf("pantipman") != -1
                            || b.getImageUrl().indexOf("pig") != -1 || b.getImageUrl().indexOf("welcome") != -1
                            ){
                        String pathEmotion = "images_"+b.getImageUrl().substring(b.getImageUrl().lastIndexOf("/")+1,b.getImageUrl().length());
                        b.setImageUrl(pathEmotion);

                        b.setThaiviIcon(true);
                        b.setImageLocal(true);
                    }else{
                        String imageUrl =b.getImageUrl();
                        if( !Utils.isNull(b.getImageUrl()).equals("") && b.getImageUrl().indexOf("http") == -1){
                            imageUrl = "http://board.thaivi.org/"+imageUrl.substring(2,imageUrl.length());
                        }
                        b.setImageUrl(imageUrl);
                    }
                    log.debug("Result:ImageUrl:"+b.getImageUrl());
                    contentList.add(b);

                }else if(nodeName.equalsIgnoreCase("a")){
                    // log.debug("=a");
                    String[] link = getLinkProperty(node.toString());

                    b.setType(Constants.ITEM_TYPE_TEXT_LINK);
                    b.setContent(link[0]);
                    b.setLinkUrl(link[1]);
                    contentList.add(b);
                }else if(nodeName.equalsIgnoreCase("span")){
                    contentList.addAll(splitCaseSpan(authen,removeAllSpan(node.toString())));

                }else if(nodeName.equalsIgnoreCase("div")){
                    contentList.addAll(splitCaseDiv(authen,removeAllDiv(node.toString())));

                } else if(nodeName.equalsIgnoreCase("object")){
                    String d = node.toString();
                    log.debug("input:"+d);
                    d = getEmbedProperty(d);
                    b.setType(Constants.ITEM_TYPE_TEXT_LINK);
                    b.setContent(d);
                    b.setLinkUrl(d);

                    log.debug("output:"+d);
                    contentList.add(b);
                }else if(nodeName.equalsIgnoreCase("strong") ){
                    //System.out.println("=text");
                    b.setType(Constants.ITEM_TYPE_TEXT);
                    b.setContent(getTextOnly(node.toString()));

                    log.debug("output:"+b.getContent());
                    contentList.add(b);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return contentList;
    }

    public static String removeAllSpan(String s){
        String temp = s;
        int end = 0;
        Document doc = null;
        log.debug("removeAllSpan");
        try{
            if(temp.indexOf("<span") != -1 && temp.indexOf("<div") != -1){
                doc =Jsoup.parse(s);
                Element c = doc.select("div.codecontent").first();
                temp = c.text();
                log.debug("temp:"+temp);
            }else{
                while(temp.indexOf("<span") != -1){
                    if(temp.indexOf("</span") == -1){
                        end = temp.length();
                    }else{
                        end = temp.indexOf("</span>");
                    }
                    temp = temp.substring(temp.indexOf(">")+1,end);
                    log.debug("temp:"+temp);
                }
            }
            temp = "<span>"+temp+"</span>";
            log.debug("output:"+temp);
        }catch(Exception e){
            e.printStackTrace();
        }
        return temp;
    }

    public static String removeAllDiv(String s){
        String temp = s;
        int end = 0;
        log.debug("removeAllDiv");
        try{
            while(temp.indexOf("<div") != -1){
                if(temp.indexOf("</div") == -1){
                    end = temp.length();
                }else{
                    end = temp.indexOf("</div");
                }
                temp = temp.substring(temp.indexOf(">")+1,end);
                log.debug("temp:"+temp);
            }

            temp = "<div>"+temp+"</div>";
        }catch(Exception e){
            e.printStackTrace();
        }
        return temp;
    }

    public static List<ContentBean> splitCaseSpan(JSoupHelperAuthen authen, String s){
        Document doc = null;
        int i=0;
        int childNodeSize = 0;
        List<ContentBean> contentList = new ArrayList<ContentBean>();
        try{
            // doc = Jsoup.parseBodyFragment(s);
            doc = Jsoup.parse(s);
            // log.debug("**********splitCaseSpan***************");
            // log.debug(""+s);
            // log.debug("**********/splitCaseSpan***************");

            Element body= doc.select("span").first();
            childNodeSize = body.childNodeSize();

            //log.debug("childNodeSize:"+body.childNodeSize());

            for(i=0;i<childNodeSize;i++){
                Node node =body.childNode(i);
                // String nodeName = node.nodeName();
                //log.debug(nodeName);
                // log.debug("*****Content NodeName["+nodeName+"]:\n["+node.toString()+"]\n****************");

                ContentBean b = convertToContentBean(node);
                if(b != null)
                    contentList.add(b);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return contentList;
    }

    public static List<ContentBean> splitCaseDiv(JSoupHelperAuthen authen, String s){
        Document doc = null;
        int i=0;
        int childNodeSize = 0;
        List<ContentBean> contentList = new ArrayList<ContentBean>();
        try{
            // doc = Jsoup.parseBodyFragment(s);
            doc = Jsoup.parse(s);
            //log.debug("**********DivALL***************");
            //log.debug(""+s);
            // log.debug("**********/DivALL***************");

            Element body= doc.select("div").first();
            childNodeSize = body.childNodeSize();

            //log.debug("childNodeSize:"+body.childNodeSize());

            for(i=0;i<childNodeSize;i++){
                Node node =body.childNode(i);
                String nodeName = node.nodeName();
                //log.debug(nodeName);
                //log.debug("*****Content NodeName["+nodeName+"]:\n["+node.toString()+"]\n****************");
                ContentBean b = convertToContentBean(node);
                if(b != null)
                    contentList.add(b);

            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return contentList;
    }

    public static ContentBean convertToContentBean(Node node ){
        String nodeName = node.nodeName();
        ContentBean b = new ContentBean();
        boolean found = false;
        try{
            if(nodeName.equalsIgnoreCase("#text") ){
                //System.out.println("=text");

                b.setType(Constants.ITEM_TYPE_TEXT);
                b.setContent(node.toString());
                found = true;
            }else if(nodeName.equalsIgnoreCase("img")){
                //System.out.println("=image");

                b.setType(Constants.ITEM_TYPE_IMAGE);
                b.setImageUrl(getImageProperty(node.toString()));
                b.setImageLocal(false);

                if(b.getImageUrl().indexOf("icon_") != -1
                        || b.getImageUrl().indexOf("cheers") != -1 || b.getImageUrl().indexOf("cool") != -1
                        || b.getImageUrl().indexOf("doi") != -1 || b.getImageUrl().indexOf("drink") != -1
                        || b.getImageUrl().indexOf("liverpool") != -1 || b.getImageUrl().indexOf("pantipman") != -1
                        || b.getImageUrl().indexOf("pig") != -1 || b.getImageUrl().indexOf("welcome") != -1
                        ){
                    String pathEmotion = "images_"+b.getImageUrl().substring(b.getImageUrl().lastIndexOf("/")+1,b.getImageUrl().length());
                    b.setImageUrl(pathEmotion);

                    b.setThaiviIcon(true);
                    b.setImageLocal(true);
                }else{
                    String imageUrl =b.getImageUrl();
                    if( !Utils.isNull(b.getImageUrl()).equals("") && b.getImageUrl().indexOf("http") == -1){
                        imageUrl = "http://board.thaivi.org/"+imageUrl.substring(2,imageUrl.length());
                    }
                    b.setImageUrl(imageUrl);
                }
                found = true;

            }else if(nodeName.equalsIgnoreCase("a")){
                // System.out.println("=a");
                String[] link =getLinkProperty(node.toString());

                b.setType(Constants.ITEM_TYPE_TEXT_LINK);
                b.setContent(link[0]);
                b.setLinkUrl(link[1]);
                found = true;

            } else if(nodeName.equalsIgnoreCase("object")){
                String d = node.toString();
                d = getEmbedProperty(d);

                b.setType(Constants.ITEM_TYPE_TEXT_LINK);
                b.setContent(d);
                b.setLinkUrl(d);
                found = true;
                System.out.println("output:"+d);

            }else if(nodeName.equalsIgnoreCase("strong") ){

                b.setType(Constants.ITEM_TYPE_TEXT);
                b.setContent(getTextOnly(node.toString()));
                found = true;
            }

            //log.debug("NodeName["+nodeName+"]Found:"+found);
            if(found){
                //log.debug("output>>:"+b.getContent());
            }else{
                b = null;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return b;
    }

    private static String getImageProperty(String s){
        String src = "";
        Document doc = null;
        try{
            //log.debug("Content:"+s);
            doc = Jsoup.parse(s);
            //Elements pngs = doc.select("img[src$=.jpg]");

            Elements links = doc.select("img[src]");
            //log.debug("src:"+links.attr("src"));
            src = links.attr("src");

        }catch(Exception e){
            e.printStackTrace();
        }
        return src;
    }

    private static String getTextOnly(String s){
        String src = "";
        Document doc = null;
        try{
            System.out.println("Content:"+s);
            doc = Jsoup.parse(s);

            src = doc.text();
        }catch(Exception e){
            e.printStackTrace();
        }
        return src;
    }
    private static String[] getLinkProperty(String s){
        String link[] = new String[2];
        Document doc = null;
        try{
            doc = Jsoup.parse(s);

            Elements links = doc.select("a[href]");
            //log.debug("src:"+links.attr("href"));
            //log.debug("text:"+links.text());
            //log.debug("*********************************");

            link[0] = links.attr("href");
            link[1] = links.text();

        }catch(Exception e){
            e.printStackTrace();
        }
        return link;
    }

    private static String getEmbedProperty(String s){
        String src = "";
        Document doc = null;
        try{
            log.debug("Content:"+s);
            doc = Jsoup.parse(s);
            //Elements pngs = doc.select("img[src$=.jpg]");

            Elements links = doc.select("embed[src]");
            log.debug("src:"+links.attr("src"));
            src = links.attr("src");

        }catch(Exception e){
            e.printStackTrace();
        }
        return src;
    }


    private static String splitImageContentAndSaveImageToLocal(JSoupHelperAuthen authen,String s,Item currentItem ){
        StringBuffer data = new StringBuffer("");
        int i=0;
        //String thaiVITopicId = currentItem.getThaiviTopicId();
        try{
            String temp =s;
            if(s.indexOf("img") != -1){
                String content ="";
                while(temp.indexOf("img") != -1){
                    //log.debug("img index:"+temp.indexOf("img"));

                    if(temp.indexOf("img") ==1){
                        content = temp.substring(0,temp.indexOf("/>")+2);
                        temp = temp.substring(temp.indexOf("/>")+2,temp.length());
                    }else{
                        content = temp.substring(0,temp.indexOf("img")-1);
                        temp = temp.substring(temp.indexOf("img")-1,temp.length());
                    }

                    //log.debug("content:"+content);
                    //log.debug("temp:"+temp);

                    ContentBean b = new ContentBean();
                    //log.debug("content:"+content);
                    b.setContent(content);
                    b.setType(Constants.ITEM_TYPE_TEXT);

                    if(content.indexOf("img") != -1){
                        b.setType(Constants.ITEM_TYPE_IMAGE);
                        String imageUrl = "";
                        int lastSubIndex = content.length();

                        if(content.indexOf("jpg") != -1){
                            lastSubIndex = content.indexOf("jpg")+3;
                            b.setFileType("jpg");
                        }else if(content.indexOf("JPG") != -1){
                            lastSubIndex = content.indexOf("JPG")+3;
                            b.setFileType("jpg");
                        }else  if(content.indexOf("jpeg") != -1){
                            lastSubIndex = content.indexOf("jpeg")+4;
                            b.setFileType("jpeg");
                        }else  if(content.indexOf("JPEG") != -1){
                            lastSubIndex = content.indexOf("JPEG")+4;
                            b.setFileType("JPEG");
                        }else if(content.indexOf("gif") != -1){
                            lastSubIndex = content.indexOf("gif")+3;
                            b.setFileType("gif");
                        }else if(content.indexOf("GIF") != -1){
                            lastSubIndex = content.indexOf("GIF")+3;
                            b.setFileType("GIF");
                        }else if(content.indexOf("png") != -1){
                            lastSubIndex = content.indexOf("png")+3;
                            b.setFileType("png");
                        }else if(content.indexOf("PNG") != -1){
                            lastSubIndex = content.indexOf("PNG")+3;
                            b.setFileType("PNG");
                        }

                        //case url1: ./images/smilies/icon_redface.gif
                        //     url2: http://board.thaivi.org/images/smilies/icon_redface.gif

                        //log.debug("content:"+content);
                        if(content.indexOf("src") != -1){
                            imageUrl = content.substring(content.indexOf("src")+5,lastSubIndex);

                            if(content.indexOf("file.php") != -1){
                                //<img src="./download/file.php?id=26552" alt="image.jpg" />
                                lastSubIndex = content.indexOf("alt")-2;
                                imageUrl = content.substring(content.indexOf("src")+5,lastSubIndex);
                                b.setFileType("jpg");
                            }
                        }

                        if( !Utils.isNull(imageUrl).equals("") && imageUrl.indexOf("http") == -1){
                            imageUrl = "http://board.thaivi.org/"+imageUrl.substring(2,imageUrl.length());
                        }
                        b.setImageUrl(imageUrl);

                        imageRunning++;

                        Date st = new Date();
                        //log.debug("imageUrl:"+imageUrl);

                        String pathFile = "";
                        if( !Utils.isNull(imageUrl).equals("")){
                            if(imageUrl.indexOf("icon_") != -1
                                    || imageUrl.indexOf("cheers") != -1 || imageUrl.indexOf("cool") != -1
                                    || imageUrl.indexOf("doi") != -1 || imageUrl.indexOf("drink") != -1
                                    || imageUrl.indexOf("liverpool") != -1 || imageUrl.indexOf("pantipman") != -1
                                    || imageUrl.indexOf("pig") != -1 || imageUrl.indexOf("welcome") != -1
                                    ){
                                String pathEmotion = imageUrl.substring(imageUrl.lastIndexOf("/")+1,imageUrl.length());
                                pathFile = "images_"+pathEmotion;
                            }else{
                                pathFile = saveImageToLocalByTopicItem(authen,b,currentItem);
                            }
                        }
                        //log.debug("** Time splitImageContentAndSaveImageToLocal.saveImageToLocalByTopicItem:"+((new Date().getTime())-st.getTime()));
                        data.append(pathFile+Constants.content_image_delimeter);
                    }else{
                        String cleanContenHtmlCode = b.getContent();// Jsoup.parse(b.getContent()).text();
                        data.append(cleanContenHtmlCode+Constants.content_image_delimeter);
                    }
                    i++;
                }//while

                //Last
                String cleanContenHtmlCode =  temp;//Jsoup.parse(temp).text();
                //log.debug("Content Text["+i+"]:"+cleanContenHtmlCode);
                data.append(cleanContenHtmlCode+Constants.content_image_delimeter);

            }else{
                //Context text only
                String cleanContenHtmlCode = s;// Jsoup.parse(s).text();
                data.append(cleanContenHtmlCode+Constants.content_image_delimeter);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return data.toString();
    }

    public static String saveImageToLocalByTopicItem(JSoupHelperAuthen j,ContentBean b,Item currentItem){
        String pathFile = "";
        FileOutputStream outStream = null;
        String thaiVITopicId = currentItem.getThaiviTopicId();
        try{
            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
            File myNewFolder = new File(extStorageDirectory +"/"+ Constants.APP_FOLDER+"/images");
            //log.debug("extStorageDirectory["+extStorageDirectory+"] appFolder Exist["+myNewFolder.exists()+"]");
            if( !myNewFolder.exists()){
                boolean makeDir = myNewFolder.mkdir();
                log.debug("mkDir:"+makeDir);
            }
            pathFile = extStorageDirectory+"/"+Constants.APP_FOLDER+"/images/"+thaiVITopicId+"_"+imageRunning+"."+b.getFileType();

            // log.debug("Result Image Url:"+b.getImageUrl());
            log.debug("Result Image Path:"+pathFile);

            //Need Authen
            if(b.getImageUrl().indexOf("thaivi") != -1){
                try{
                    InputStream in = JSoupHelperAuthen.authen.getImages(b.getImageUrl());
                    if(in != null){
                        Bitmap image = BitmapFactory.decodeStream(in);
                        outStream = new FileOutputStream(pathFile);
                        image.compress(Bitmap.CompressFormat.PNG, 45, outStream);

                        outStream.flush();
                        outStream.close();
                    }
                }catch(Exception e){}
            }else{
                // No Authen
                try{
                    URL url = new URL(b.getImageUrl());
                    Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

                    if(image != null){
                        outStream = new FileOutputStream(pathFile);
                        image.compress(Bitmap.CompressFormat.JPEG, 45, outStream);

                        outStream.flush();
                        outStream.close();
                    }
                }catch(Exception e){}
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return pathFile;
    }


    /**
     *
     * @param s  -> 'xxxx^๑yyyyyy^๑/mnt/sdcard/THAIVI/images/xx.gif'
     * @return
     */

    private static List<ContentBean> splitContentToListBK(String s){
        List<ContentBean> contentList = new ArrayList<ContentBean>();

        try{
            log.debug("******splitContentToList.Content ALL************************" +
                    " \n"+s);
            log.debug("*****************************************");

            if(s.indexOf("images") != -1){
                String content ="";
                String[] contentArr = s.split("\\"+Constants.content_image_delimeter);
                for(int i=0;i<contentArr.length;i++){
                    content  = contentArr[i];
                    //log.debug("******contentArr["+i+"]:********************" +
                    //"\n "+content +"\n *****************************************");

                    ContentBean b = new ContentBean();
                    if(content.indexOf("images") != -1){
                        b.setType(Constants.ITEM_TYPE_IMAGE);
                        b.setImageUrl(content.substring(0,content.length()));

                        contentList.add(b);
                    }else{
                        String textContent = content.substring(0,content.length()).trim();
                        //log.debug("1.textContent:\n"+textContent +"");

                        if( Utils.isNull(textContent).indexOf("href") != -1){
                            Document doc = Jsoup.parse("<body>"+textContent+"</body>");
                            Element div = doc.select("body").first();
                            int r= 0;
                            for (Node node : div.childNodes()) {
                                if( !Utils.isNull(node.toString()).equals("")
                                        && !Utils.isNull(node.toString()).startsWith("<!--")
                                        && !Utils.isNull(node.toString()).endsWith("/>")){

                                    // log.debug("Text["+node.toString()+"]FoundLink["+Utils.isNull(node.toString()).indexOf("href") +"]");

                                    if( Utils.isNull(node.toString()).indexOf("href") != -1){
                                        List<String[]> linkList = JSoupUtils.getLinkTagList(node.toString());
                                        if(linkList != null && linkList.size()>0){
                                            for(int y=0;y<linkList.size();y++){
                                                String[] linkTags = linkList.get(y);
                                                //log.debug("R["+r+"]LinkText["+linkTags[1]+"]LinkUrl["+linkTags[0]+"]");
                                                String textLink =linkTags[1]+"|"+linkTags[0];

                                                b = new ContentBean();
                                                b.setContent(textLink);
                                                b.setType(Constants.ITEM_TYPE_TEXT_LINK);
                                                contentList.add(b);
                                                r++;
                                            }
                                        }
                                    }else{
                                        //log.debug("R["+r+"]Text["+JSoupUtils.getTextOnlyFromLinkTag(node.toString())+"]");
                                        b = new ContentBean();
                                        b.setContent(JSoupUtils.getTextOnlyFromLinkTag(node.toString()).trim());
                                        b.setType(Constants.ITEM_TYPE_TEXT);
                                        contentList.add(b);

                                        r++;
                                    }//if
                                }//if
                            }//for
                        }else if( Utils.isNull(textContent).indexOf("embed") != -1){
                            Document doc = Jsoup.parse("<body>"+textContent+"</body>");
                            Element div = doc.select("body").first();
                            int r= 0;
                            for (Node node : div.childNodes()) {

                                if( !Utils.isNull(node.toString()).equals("")
                                        && !Utils.isNull(node.toString()).startsWith("<!--")
                                        && !Utils.isNull(node.toString()).endsWith("/>")){

                                    //log.debug("Text["+node.toString()+"]FoundLink["+Utils.isNull(node.toString()).indexOf("embed") +"]");

                                    if( Utils.isNull(node.toString()).indexOf("embed") != -1){
                                        List<String[]> linkList = JSoupUtils.getLinkTagEmbedList(node.toString());
                                        if(linkList != null && linkList.size()>0){
                                            for(int y=0;y<linkList.size();y++){
                                                String[] linkTags = linkList.get(y);
                                                //log.debug("R["+r+"]LinkText["+linkTags[1]+"]LinkUrl["+linkTags[0]+"]");
                                                String textLink =linkTags[1]+"|"+linkTags[0];

                                                b = new ContentBean();
                                                b.setContent(textLink);
                                                b.setType(Constants.ITEM_TYPE_TEXT_LINK);
                                                contentList.add(b);
                                                r++;
                                            }
                                        }
                                    }else{
                                        //log.debug("R["+r+"]Text["+JSoupUtils.getTextOnlyFromLinkTag(node.toString())+"]");
                                        b = new ContentBean();
                                        b.setContent(JSoupUtils.getTextOnlyFromLinkTag(node.toString()).trim());
                                        b.setType(Constants.ITEM_TYPE_TEXT);
                                        contentList.add(b);

                                        r++;
                                    }//if
                                }//if
                            }//for
                        }else{
                            // log.debug("No Linktag");
                            b = new ContentBean();
                            b.setContent(textContent);
                            b.setType(Constants.ITEM_TYPE_TEXT);
                            contentList.add(b);
                        }

                    }//if

                }//while
            }else{
                ContentBean b = new ContentBean();
                String textContent = (s.substring(0,s.length()-2)).trim();
                //log.debug("2.textContent:\n"+textContent);

                if( Utils.isNull(textContent).indexOf("href") != -1){
                    Document doc = Jsoup.parse("<body>"+textContent+"</body>");
                    Element div = doc.select("body").first();
                    int r= 0;
                    for (Node node : div.childNodes()) {

                        if( !Utils.isNull(node.toString()).equals("")
                                && !Utils.isNull(node.toString()).startsWith("<!--")
                                && !Utils.isNull(node.toString()).endsWith("/>")){

                            //log.debug("Text["+node.toString()+"]FoundLink["+Utils.isNull(node.toString()).indexOf("href") +"]");

                            if( Utils.isNull(node.toString()).indexOf("href") != -1){
                                List<String[]> linkList = JSoupUtils.getLinkTagList(node.toString());
                                if(linkList != null && linkList.size()>0){
                                    for(int y=0;y<linkList.size();y++){
                                        String[] linkTags = linkList.get(y);
                                        //log.debug("R["+r+"]LinkText["+linkTags[1]+"]LinkUrl["+linkTags[0]+"]");
                                        String textLink =linkTags[1]+"|"+linkTags[0];

                                        b = new ContentBean();
                                        b.setContent(textLink);
                                        b.setType(Constants.ITEM_TYPE_TEXT_LINK);
                                        contentList.add(b);
                                        r++;
                                    }
                                }
                            }else{
                                //log.debug("R["+r+"]Text["+JSoupUtils.getTextOnlyFromLinkTag(node.toString())+"]");
                                b = new ContentBean();
                                b.setContent(JSoupUtils.getTextOnlyFromLinkTag(node.toString()).trim());
                                b.setType(Constants.ITEM_TYPE_TEXT);
                                contentList.add(b);

                                r++;
                            }//if
                        }//if
                    }//for
                }else if( Utils.isNull(textContent).indexOf("embed") != -1){
                    Document doc = Jsoup.parse("<body>"+textContent+"</body>");
                    Element div = doc.select("body").first();
                    int r= 0;
                    for (Node node : div.childNodes()) {

                        if( !Utils.isNull(node.toString()).equals("")
                                && !Utils.isNull(node.toString()).startsWith("<!--")
                                && !Utils.isNull(node.toString()).endsWith("/>")){

                            //log.debug("Text["+node.toString()+"]FoundLink["+Utils.isNull(node.toString()).indexOf("embed") +"]");

                            if( Utils.isNull(node.toString()).indexOf("embed") != -1){
                                List<String[]> linkList = JSoupUtils.getLinkTagEmbedList(node.toString());
                                if(linkList != null && linkList.size()>0){
                                    for(int y=0;y<linkList.size();y++){
                                        String[] linkTags = linkList.get(y);
                                        //log.debug("R["+r+"]LinkText["+linkTags[1]+"]LinkUrl["+linkTags[0]+"]");
                                        String textLink =linkTags[1]+"|"+linkTags[0];

                                        b = new ContentBean();
                                        b.setContent(textLink);
                                        b.setType(Constants.ITEM_TYPE_TEXT_LINK);
                                        contentList.add(b);
                                        r++;
                                    }
                                }
                            }else{
                                //log.debug("R["+r+"]Text["+JSoupUtils.getTextOnlyFromLinkTag(node.toString())+"]");
                                b = new ContentBean();
                                b.setContent(JSoupUtils.getTextOnlyFromLinkTag(node.toString()).trim());
                                b.setType(Constants.ITEM_TYPE_TEXT);
                                contentList.add(b);

                                r++;
                            }//if
                        }//if
                    }//for
                }else{
                    // log.debug("No Linktag ");
                    b = new ContentBean();
                    b.setContent(textContent);
                    b.setType(Constants.ITEM_TYPE_TEXT);
                    contentList.add(b);
                }
            }//if
        }catch(Exception e){
            e.printStackTrace();
        }
        return contentList;
    }

    public static Bitmap displayIcon(Context context, Bitmap bm,String iconName){
        if(iconName.indexOf("cheers") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.cheers);
        }else if(iconName.indexOf("cool") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.cool);
        }else if(iconName.indexOf("doi") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.doi);
        }else if(iconName.indexOf("drink") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.drink);
        }else if(iconName.indexOf("icon_arrow") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_arrow);
        }else if(iconName.indexOf("icon_bigcry") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_bigcry);
        }else if(iconName.indexOf("icon_biggrin") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_biggrin);
        }else if(iconName.indexOf("icon_bigsmile") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_bigsmile);
        }else if(iconName.indexOf("icon_bow") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_bow);
        }else if(iconName.indexOf("icon_confused") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_confused);
        }else if(iconName.indexOf("icon_cool") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_cool);
        }else if(iconName.indexOf("icon_cry") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_cry);
        }else if(iconName.indexOf("icon_donottdothat") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_donottdothat);
        }else if(iconName.indexOf("icon_eek") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_eek);
        }else if(iconName.indexOf("icon_evil") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_evil);
        }else if(iconName.indexOf("icon_evilplan") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_evilplan);
        }else if(iconName.indexOf("icon_exclaim") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_exclaim);
        }else if(iconName.indexOf("icon_idea") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_idea);
        }else if(iconName.indexOf("icon_juju") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_juju);
        }else if(iconName.indexOf("icon_keepquiet") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_keepquiet);
        }else if(iconName.indexOf("icon_look") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_look);
        }else if(iconName.indexOf("icon_mad") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_mad);
        }else if(iconName.indexOf("icon_mrgreen") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_mrgreen);
        }else if(iconName.indexOf("icon_neutral") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_neutral);
        }else if(iconName.indexOf("icon_nevermind") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_nevermind);
        }else if(iconName.indexOf("icon_ohno") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_ohno);
        }else if(iconName.indexOf("icon_pray") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_pray);
        }else if(iconName.indexOf("icon_question") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_question);
        }else if(iconName.indexOf("icon_razz") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_razz);
        }else if(iconName.indexOf("icon_redface") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_redface);
        }else if(iconName.indexOf("icon_rofl") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_rofl);
        }else if(iconName.indexOf("icon_rolleyes") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_rolleyes);
        }else if(iconName.indexOf("icon_sad") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_sad);
        }else if(iconName.indexOf("icon_smile") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_smile);
        }else if(iconName.indexOf("icon_surprised") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_surprised);
        }else if(iconName.indexOf("icon_twisted") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_twisted);
        }else if(iconName.indexOf("icon_vomit") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_vomit);
        }else if(iconName.indexOf("icon_wal") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_wall);
        }else if(iconName.indexOf("icon_wink") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.icon_wink);
        }else if(iconName.indexOf("liverpool") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.liverpool);
        }else if(iconName.indexOf("pantipman") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.pantipman);
        }else if(iconName.indexOf("pig") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.pig);
        }else if(iconName.indexOf("welcome") != -1){
            bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.welcome);
        }
        return bm;
    }


}
