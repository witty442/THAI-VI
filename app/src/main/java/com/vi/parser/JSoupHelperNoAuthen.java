package com.vi.parser;

import java.io.File;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.vi.appconfig.AppProperty;
import com.vi.common.ContentBean;
import com.vi.common.Display;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.storage.DBAdapter;
import com.vi.storage.DBSchema;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.Utils;

public class JSoupHelperNoAuthen {


    static LogUtils log = new LogUtils("JSoupHelperNoAuthen");
    static int imageRunning = 0;
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    public  Map<String ,AppProperty> getAppConfigFromDropboxDB(){
        Map<String ,AppProperty> appConfigMap = new HashMap<String, AppProperty>();
        try{
            log.debug("AppConfig url:"+Constants.URL_APP_CONFIG);
            URL url = new URL(Constants.URL_APP_CONFIG);
            Document doc = null;
            try{
                doc = Jsoup.parse(url, Constants.JSOUP_TIMEOUT_SHORT);
            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(url, Constants.JSOUP_TIMEOUT_SHORT);
            }

            String node = doc.body().text();
            //log.debug("NodeText["+node+"]");
            String[] allStrs = node.toString().split("\\"+Constants.DELIMETER_NEW_LINE);
            //log.debug("allStrsLength["+allStrs.length+"]");

            for(int i=0;i<allStrs.length;i++){
                if( !Utils.isNull(allStrs[i]).equals("")){
                    String[] lineText = allStrs[i].split("\\"+Constants.DELIMETER_PIPE);
                    // log.debug("LineText["+lineText[0]+"]");
                    if( !Utils.isNull(lineText[0]).equals("")){
                        AppProperty app = new AppProperty();
                        for(int k=0;k<lineText.length;k++){
                            if(k==0){
                                log.debug("Name:"+Utils.isNull(lineText[k]));
                                app.setConfigName(Utils.isNull(lineText[k]));
                            }else if(k==1){
                                log.debug("Action:"+Utils.isNull(lineText[k]));
                                app.setConfigValue(Utils.isNull(lineText[k]));
                            }
                        }//for 2

                        appConfigMap.put(app.getConfigName(), app);
                    }//if
                }//if
            }//for 1
        }catch(Exception e){
            e.printStackTrace();
        }finally{
        }
        return appConfigMap;
    }

   public String getScriptFromDropbox(){
       String scriptSql = "";
       List<Feed> listItem = new ArrayList<Feed>();
       try{
           log.debug("Script Update url:"+Constants.URL_SCRIPT_UPDATE);
           URL url = new URL(Constants.URL_SCRIPT_UPDATE);
           Document doc = null;
           try{
               doc = Jsoup.parse(url, Constants.JSOUP_TIMEOUT_SHORT);
               scriptSql = doc.body().text().toString();
           }catch(Exception e){
               log.debug(e.getMessage());
               log.debug("Retry conn");

               doc = Jsoup.parse(url, Constants.JSOUP_TIMEOUT_SHORT);
               scriptSql = doc.body().text().toString();
           }
       }catch(Exception e){
           log.debug(e.getMessage());
       }
      return scriptSql;
   }

    /**
     *
     * @param curFeedType
     * @return
     */
    public  List<Feed> getFeedCatalogsFromDropboxDB(String curFeedType){
        List<Feed> listItem = new ArrayList<Feed>();
        String node = null;
        String[] allStrs = null;
        try{
            log.debug("Catalogs url:"+Constants.URL_CATALOGS);
            URL url = new URL(Constants.URL_CATALOGS);
            Document doc = null;
            try{
                doc = Jsoup.parse(url,Constants.JSOUP_TIMEOUT_SHORT);

                node = doc.body().text();
                //log.debug("NodeText["+node+"]");
                allStrs = node.toString().split("\\" + Constants.DELIMETER_NEW_LINE);
                //log.debug("allStrsLength["+allStrs.length+"]");

            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(url,Constants.JSOUP_TIMEOUT_SHORT);

                node = doc.body().text();
                //log.debug("NodeText["+node+"]");
                allStrs = node.toString().split("\\" + Constants.DELIMETER_NEW_LINE);
                //log.debug("allStrsLength["+allStrs.length+"]");
            }

            for(int i=0;i<allStrs.length;i++){
                if( !Utils.isNull(allStrs[i]).equals("")){
                    String[] lineText = allStrs[i].split("\\"+Constants.DELIMETER_PIPE);
                    // log.debug("LineText["+lineText[0]+"]");
                    if( !Utils.isNull(lineText[0]).equals("")){
                        Feed t = new Feed();
                        for(int k=0;k<lineText.length;k++){
                            if(k==0){
                                //log.debug("url:"+Utils.isNull(lineText[k]));
                                t.setLink(Utils.isNull(lineText[k]));
                            }else if(k==1){
                                //log.debug("Title:"+Utils.isNull(lineText[k]));
                                t.setTitle(StringEscapeUtils.unescapeHtml(Utils.isNull(lineText[k])));
                            }else if(k==2){
                                //log.debug("creatDate:"+Utils.isNull(lineText[k]));
                                t.setCreateDate(Utils.convertToDate(Utils.isNull(lineText[k])));
                            }else if(k==3){
                                //log.debug("author:"+Utils.isNull(lineText[k]));
                                t.setAuthor(Utils.isNull(lineText[k]));
                            }else if(k==4){
                                t.setType(Utils.isNull(lineText[k]));
                                //log.debug("type:"+Utils.isNull(lineText[k]));
                            }else if(k==5){
                                t.setId(Integer.parseInt(Utils.isNull(lineText[k])));
                                //log.debug("type:"+Utils.isNull(lineText[k]));
                            }
                            if(lineText.length >= k){
                                if(k==6){
                                    t.setTotalReply(Integer.parseInt(Utils.isNull(lineText[k])));
                                }else if(k==7){
                                    t.setTotalRead(Integer.parseInt(Utils.isNull(lineText[k])));
                                }
                            }
                            t.setEnabled(DBSchema.ON);
                        }//for 2

                        if(curFeedType == null || t.getType().startsWith((curFeedType)) ){
                            listItem.add(t);
                        }//if
                    }//if
                }//if
            }//for 1
        }catch(Exception e){
            e.printStackTrace();
        }finally{
        }
        return listItem;
    }

    /**
     *
     * @param mDbFeedAdapter
     * @param feed
     * @return
     */
    public  List<Item> getTopicItemsFromDropboxDB(DBAdapter mDbFeedAdapter,Feed feed){
        List<Item> listItem = new ArrayList<Item>();
        String node = null;
        String[] allStrs = null;
        try{
            log.debug("Link:"+feed.getLink());
            URL url = new URL(Utils.isNull(feed.getLink()));
            Document doc = null;
            try{
                doc = Jsoup.parse(url,Constants.JSOUP_TIMEOUT_SHORT);

                node = doc.body().text();
                // log.debug("NodeText["+node+"]");
                allStrs = node.toString().split("\\"+Constants.DELIMETER_NEW_LINE);
                //log.debug("allStrsLength["+allStrs.length+"]");

            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(url,Constants.JSOUP_TIMEOUT_SHORT);

                node = doc.body().text();
                // log.debug("NodeText["+node+"]");
                allStrs = node.toString().split("\\"+Constants.DELIMETER_NEW_LINE);
                //log.debug("allStrsLength["+allStrs.length+"]");
            }

            for(int i=0;i<allStrs.length;i++){
                String[] lineText = allStrs[i].split("\\"+Constants.DELIMETER_PIPE);
                //log.debug("LineText:"+allStrs[i]);
                if( !Utils.isNull(lineText[0]).equals("")){
                    Item t = new Item();
                    for(int k=0;k<lineText.length;k++){
                        if(k==0){
                            //log.debug("url:"+Utils.isNull(lineText[k]));
                            t.setOrgLink(Utils.isNull(lineText[k]));
                        }else if(k==1){
                            t.setLink(Utils.isNull(lineText[k]));
                        }else if(k==2){
                            //log.debug("Title:"+Utils.isNull(lineText[k]));
                            t.setTitle(StringEscapeUtils.unescapeHtml(Utils.isNull(lineText[k])));
                        }else if(k==3){
                            //log.debug("creatDate:"+Utils.isNull(lineText[k]));
                            t.setCreateDate(Utils.convertToDate(Utils.isNull(lineText[k])));
                        }else if(k==4){
                            // log.debug("author:"+Utils.isNull(lineText[k]));
                            //t.setAuthor(Utils.isNull(lineText[k]));
                        }else if(k==5){
                            t.setType(Utils.isNull(lineText[k]));
                            //log.debug("type:"+Utils.isNull(lineText[k]));
                        }
                        if(lineText.length >= k){
                            if(k==6){
                                t.setTotalReply(Integer.parseInt(Utils.isNull(lineText[k])));
                                //log.debug("TotalReply:"+Utils.isNull(lineText[k]));
                            }
                            if(k==7){
                                t.setTotalRead(Integer.parseInt(Utils.isNull(lineText[k])));
                                //log.debug("TotalRead:"+Utils.isNull(lineText[k]));
                            }
                        }
                    }//for 2

                    // log.debug("CreateDate:"+t.getCreateDate());

                    //Check Exist
                    boolean existItem = mDbFeedAdapter.isItemExistDB(t.getTitle());
                    // log.debug("title["+t.getTitle()+"]existItem:"+existItem);
                    //if(!existItem){
                    listItem.add(t);
                    //}
                }//if
            }//for 1


        }catch(Exception e){
            e.printStackTrace();
        }finally{
        }
        return listItem;
    }

    /**
     *
     * @param t
     * @return
     * @throws Exception
     */
    public StringBuffer getContentFromDropBox(Item t) throws Exception{

        StringBuffer lineStrBuff = new StringBuffer("");
        try{
            Document doc = null;// Jsoup.parse(new URL(t.getLink()),Constants.JSOUP_TIMEOUT);
            try{
                doc = Jsoup.parse(new URL(t.getLink()),Constants.JSOUP_TIMEOUT);
            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(new URL(t.getLink()),Constants.JSOUP_TIMEOUT);
            }

            //log.debug("xxxdoc:"+doc.html());
            // lineStrBuff.append(doc.body().text());
            lineStrBuff.append(doc.html());

        }catch(Exception e){
            throw e;
        }finally{
        }
        return lineStrBuff;
    }

    public  List<Display> getContentFromWeb(Item t,int currentPage) throws Exception{
        log.debug("getContentFromOrg");
        return getContentThaiVIBoardByPage(t,currentPage);
    }

    public  int calcTotalPage(int totalReply,int rowPerPage){
        int totalPage = 1;
        float totalReplyF = totalReply;
        float rowPerPageF = rowPerPage;
        float pageC = totalReplyF/rowPerPageF;
        BigDecimal r = new BigDecimal(pageC);
        r = r.setScale(0,BigDecimal.ROUND_UP);
        totalPage = r.intValue();
        return totalPage;
    }

    private List<Display> getContentThaiVIBoardAllPage(Item t){
        //http://board.thaivi.org/viewtopic.php?f=35&t=36591
        //http://board.thaivi.org/viewtopic.php?f=35&t=36591&start=30
        List<Display> contentAllList = new ArrayList<Display>();
        StringBuffer contentStr = new StringBuffer("");
        imageRunning = 0;
        try{
            int rowPerPage = Constants.THAIVI_ROW_PER_PAGE;
            int page = 0;
            int totalPage = calcTotalPage(t.getTotalReply(), rowPerPage);
            log.debug("totalPage:"+totalPage);

            /** substr link for get id= topic for save image **/
            t.setThaiviTopicId(getThaiviTopicId(t.getLink()));

            if(totalPage > 0){
                for(page = 0;page<totalPage;page++){
                    //log.debug("page:"+page);
                    if(page ==0){
                        t.setLink(t.getOrgLink());
                        List<Display> contentList = getContentThaiVIbyPage(t,contentStr,page);
                        contentAllList.addAll(contentList);
                    }else {
                        String url = t.getOrgLink()+"&start="+page*rowPerPage;
                        t.setLink(url);
                        List<Display> contentList = getContentThaiVIbyPage(t,contentStr,page);
                        contentAllList.addAll(contentList);
                    }
                    log.debug("page["+page+"]:"+contentAllList.size());
                }
            }else{
                List<Display> contentList = getContentThaiVIbyPage(t ,contentStr,page);
                contentAllList.addAll(contentList);
                log.debug("page["+page+"]:"+contentAllList.size());
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return contentAllList;
    }

    /**
     *
     * @param t
     * @return
     * /**
    ความคิดเห็นที่ :4^@
    ขอขอบคุณผู้เขียน  ผู้รวบรวม และผู้จัดทำบทความเหล่านี้ด้วยค่ะ ถือเป็นการเพิ่มคุณค่าให้กับเวบนี้มากมากเลย....ขอให้กำลังใจและขอเป็นแรงสนับสนุนด้วยนะคะ^@
     @คุณ mey|
     */

    public StringBuffer getContentThaiVIBoardAllPageToSave(Feed currentFeed,Item t){
        StringBuffer contentStr = new StringBuffer("");
        imageRunning = 0;
        try{
            int rowPerPage = Constants.THAIVI_ROW_PER_PAGE;
            int page = 0;
            int totalPage = calcTotalPage(t.getTotalReply(), rowPerPage);
            log.debug("totalPage:"+totalPage);

            /** substr link for get id= topic for save image **/
            t.setThaiviTopicId(getThaiviTopicId(t.getLink()));

            if(totalPage > 0){
                for(page = 0;page<totalPage;page++){
                    //log.debug("page:"+page);
                    if(page ==0){
                        getContentThaiVIbyPage(t,contentStr,page);
                    }else {
                        String url = t.getLink()+"&start="+page*rowPerPage;
                        t.setLink(url);
                        getContentThaiVIbyPage(t,contentStr,page);
                    }
                }
            }else{
                getContentThaiVIbyPage(t,contentStr,page);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return contentStr;
    }

    private List<Display> getContentThaiVIBoardByPage(Item t,int currentPage){
        //http://board.thaivi.org/viewtopic.php?f=35&t=36591
        //http://board.thaivi.org/viewtopic.php?f=35&t=36591&start=30
        List<Display> contentAllList = new ArrayList<Display>();
        imageRunning = 0;
        try{
            int rowPerPage = Constants.THAIVI_ROW_PER_PAGE;
            log.debug("getContentThaiVIBoardByPage currentPage:"+currentPage);

            /** substr link for get id= topic for save image **/
            t.setThaiviTopicId(getThaiviTopicId(t.getLink()));

            if(currentPage ==1){
                t.setLink(t.getOrgLink());
                List<Display> contentList = getContentThaiVIbyPage(t,null,currentPage);
                contentAllList.addAll(contentList);
            }else {
                String url = t.getOrgLink()+"&start="+(currentPage-1)*rowPerPage;
                t.setLink(url);
                List<Display> contentList = getContentThaiVIbyPage(t,null,currentPage);
                contentAllList.addAll(contentList);
            }
            log.debug("page["+currentPage+"]:"+contentAllList.size());

        }catch(Exception e){
            e.printStackTrace();
        }
        return contentAllList;
    }

    private List<Display> getContentThaiVIbyPage(Item currentItem,StringBuffer contentStr,int page ) throws Exception{
        Document doc = null;
        Element tableE = null;
        Iterator<Element> tablesIT = null;
        int no = 1;
        int i = 0;
        List<Display> contentList = new ArrayList<Display>();
        String title = "";
        String author ="";
        String authorRole = "";
        String postDate = "";
        String content ="";
        String tableReply = "";
        Document docReply = null;
        Iterator<Element> tableReplyIT = null;
        String content1 = "";
        String content2 ="";
        Document docContent2 = null;
        Element docContentE12 = null;
        Element docContentE22 = null;
        log.debug("url:"+currentItem.getLink());
        try{
            /** Calculate no **/
            if(page==1){
                no = 1;
            }else{
                no = ((page-1)*Constants.THAIVI_ROW_PER_PAGE)+1;
            }
            log.debug("Page["+page+"]no["+no+"]");
            URL urlObj = new URL(currentItem.getLink());

            doc = null;//Jsoup.parse(urlObj,Constants.JSOUP_TIMEOUT_LONG);
            try{
                doc = Jsoup.parse(urlObj,Constants.JSOUP_TIMEOUT_LONG);
                tableE = doc.select("div[id=pagecontent]").first();
                tablesIT = tableE.select("table[class=tablebg postrow]").iterator();
            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(urlObj,Constants.JSOUP_TIMEOUT_LONG);
                tableE = doc.select("div[id=pagecontent]").first();
                tablesIT = tableE.select("table[class=tablebg postrow]").iterator();
            }
            //Get New TotalReply
            currentItem.setTotalReply(Utils.isNullInt(getTopicTotalReply(doc.html())));
            int test=0;
            while(tablesIT.hasNext()){
                title = "";
                author ="";
                authorRole = "";
                postDate = "";
                content ="";

                //log.debug("["+i+"]:"+tables.next().text());
                tableReply = "<table><tbody><tr><td>"+tablesIT.next().outerHtml()+"</table>";
                // log.debug(".DEBUG.tableReply:\n"+tableReply);
                docReply = Jsoup.parse(tableReply);
                tableReplyIT = null;
                //if(i%2 == 0){
                if(tableReply.indexOf("row1") != -1){
                    tableReplyIT = docReply.select("tr[class=row1]").iterator();
                }else{
                    tableReplyIT = docReply.select("tr[class=row2]").iterator();
                }
                //log.debug("tableRepltIT hasnext:"+tableReplyIT.hasNext());

                while(tableReplyIT.hasNext()){
                    //log.debug("["+i+"]:"+tableReplyIT.next().text());
                    content1 = tableReplyIT.hasNext()?tableReplyIT.next().text():"";
                    //log.debug("Content1:"+content1);
                    if( !Utils.isNull(content1).equals("")
                            && content1.indexOf("Post") != -1
                            && content1.indexOf("Posted") != -1){
                        author = content1.substring(0,content1.indexOf("Post")-1);
                        postDate = content1.substring(content1.indexOf("Posted:")+8,content1.length());
                    }

                    content2 = tableReplyIT.hasNext()?tableReplyIT.next().outerHtml():"";
                    // log.debug(".DEBUG.Content2:****** \n"+content2);

                    if( !Utils.isNull(content2).equals("")){

                        docContent2 = Jsoup.parse(content2);
                        docContentE12 = docContent2.select("div[class=postbody]").first();
                        docContentE22 = docContent2.select("td[class=postdetails]").first();
                        // log.debug(".DEBUG.Content1E2:"+docContentE22.ownText());

                        //content = StringEscapeUtils.unescapeHtml(docContentE12.html());
                        content = docContentE12.html();
                        authorRole = docContentE22 != null?docContentE22.ownText():"ทั่วไป";
                    }

                    //log.debug("author: " + author);
                    //log.debug("authorRole: " + authorRole);
                    //log.debug("postDate: " + postDate);

                    //if(test < 2){

                    //log.debug(".DEBUG.content: " + content);
                    //}


                }//while 2
                test++;

                if(no==1){
                    title = "รายละเอียดกระทู้";
                    author = "@เจ้าของกระทู้ คุณ "+author+"("+authorRole+")";
                    postDate = "@วันที่ "+postDate;
                }else{
                    title = "ความคิดเห็นที่ : "+(no-1);
                    author = "@คุณ "+author+"("+authorRole+")";
                    postDate = "@วันที่ "+postDate;
                }

                if( !"".equalsIgnoreCase(author)){
                    Display disp = new Display();
                    disp.setTitle(title);
                    disp.setContent(content);
                    //disp.setContent(splitImageContentAndSaveImageToLocal(content,currentItem.getThaiviTopicId()));
                    disp.setAuthor(author);
                    disp.setPostDate(postDate);
                    //add to List
                    //log.debug("*********Add ContetList no["+no+"]content:\n"+content);
                    contentList.add(disp);

                    //log.debug(".DEBUG.content XX: " + disp.getContent());

                    //Add To File For Save
                    /**
                     *  ความคิดเห็นที่ :4^@
                     ขอขอบคุณผู้เขียน  ผู้รวบรวม และผู้จัดทำบทความเหล่านี้ด้วยค่ะ ถือเป็นการเพิ่มคุณค่าให้กับเวบนี้มากมากเลย....ขอให้กำลังใจและขอเป็นแรงสนับสนุนด้วยนะคะ^@
                     @คุณ mey|
                     */
                    if(contentStr != null){
                        contentStr.append("\n"+title+Constants.reply_delimeter);
                        contentStr.append("\n"+content+Constants.reply_delimeter);
                        contentStr.append("\n"+author+":"+postDate+Constants.reply_delimeter);
                        //contentStr.append("\n"+postDate+delimeter);
                    }
                }
                no++;
                i++;
            }//while 1

            //log.debug("contentBody:"+contentBody.toString());
            return contentList;
        }catch(Exception e){
            throw e;
        }finally{
            contentList = null;
            if(doc !=null){
                doc = null;
            }
            if(tableE !=null){
                tableE = null;
            }
            if(tablesIT !=null){
                tablesIT = null;
            }
        }
    }

    private static String splitImageContentAndSaveImageToLocal(String s,String thaiVITopicId){
        StringBuffer data = new StringBuffer("");
        int i=0;
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

                        //case url: ./images/smilies/icon_redface.gif
                        //http://board.thaivi.org/images/smilies/icon_redface.gif
                        //log.debug("content:"+content);
                        if(content.indexOf("src") != -1){
                            try{
                                imageUrl = content.substring(content.indexOf("src")+5,lastSubIndex);
                            }catch(Exception e){
                                log.debug("Error:lastSubIndex["+lastSubIndex+"]");
                                e.printStackTrace();
                            }
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
                        //log.debug("imageUrl:"+imageUrl);
                        imageRunning++;
                        //String pathFile = saveImageToLocalByTopicItem(b,thaiVITopicId);

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
                                pathFile = saveImageToLocalByTopicItem(b,thaiVITopicId);
                            }
                        }
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
                String cleanContenHtmlCode = s;// Jsoup.parse(s).text();
                data.append(cleanContenHtmlCode+Constants.content_image_delimeter);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return data.toString();
    }

    public static String saveImageToLocalByTopicItem(ContentBean b,String thaiVITopicId){
        String pathFile = "";
        FileOutputStream outStream = null;
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
            // log.debug("Result Image Path:"+pathFile);

            URL url = new URL(b.getImageUrl());
            Bitmap image = BitmapFactory.decodeStream(url.openConnection().getInputStream());

            if(image != null){
                outStream = new FileOutputStream(pathFile);
                image.compress(Bitmap.CompressFormat.JPEG, 45, outStream);

                outStream.flush();
                outStream.close();
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return pathFile;
    }

    /*
     * getFeedsTopicList
     */
    public  List<Item> getFeedsTopicList(Feed feed,int topicCurPage) {
        Document doc = null;
        Element table = null;
        Iterator<Element> tr = null;
        List<Item> listAll = new ArrayList<Item>();
        List<Item> listTopic = new ArrayList<Item>();
        List<Item> listAnTopic = new ArrayList<Item>();

        Calendar c = Calendar.getInstance();
        try{
            boolean annoucement = true;
            String image = "";
            String link= "";
            String title = "";
            String replyCount ="";
            String readCount ="";
            Date createDate = new Date();
            String createDateStr = "";
            String lastUserReply = "";
            String author = "";

            URL url = new URL(feed.getLink());
            try{
                doc = Jsoup.parse(url, Constants.JSOUP_TIMEOUT_LONG);
            }catch(Exception e){
                //Retry 1 conn
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(url, Constants.JSOUP_TIMEOUT_LONG);
            }
            table = doc.select("table[class=tablebg]").first();
            int i = 0;
            tr = table.select("tr").iterator();
            while(tr.hasNext()){
                try{
                    String tdText = "<table>"+tr.next().outerHtml()+"</table>";
                    //log.debug("tdText:"+tdText);
                    Document docTd = Jsoup.parse(tdText);
                    String tdHtml = Utils.isNull(docTd.select("td").html());
                    //log.debug("tdHtml:"+tdHtml);

                    if( !tdHtml.equals("") && tdHtml.indexOf("Topics") != -1){
                        annoucement = false;
                    }else if( !tdHtml.equals("") && tdHtml.startsWith("<img") ){
                        //log.debug("tdHtml:"+tdHtml);

                        Document docTdHtml = Jsoup.parse(tdHtml);

                        //** Get Link and Title **/
                        Elements links = docTdHtml.select("a[href]");
                        Element linkElement = (Element)links.get(0);
                        link = linkElement.attr("href");
                        link = "http://board.thaivi.org"+link.substring(1,link.length());
                        title = linkElement.text();

                        Elements topicElements = docTdHtml.select("p.topicdetails");
                        int r = 0;
                        for (Element e : topicElements) {
                            //log.debug("text["+e.text()+"]");
                            if(r==0){
                                replyCount = Utils.isNull(e.text());
                            }
                            if(r==1){
                                readCount = Utils.isNull(e.text());
                            }
                            if(r==2){
                                try{
                                    createDateStr = Utils.isNull(e.text());
                                    createDate = Utils.convertToDate(createDateStr, "EEE MMM dd,yyyy hh:mm aaa");
                                }catch(Exception ee){
                                    createDate = new Date();
                                }
                            }
                            if(r==3){
                                lastUserReply = Utils.isNull(e.text());
                            }
                            r++;
                        }

                        Elements authorElements = docTdHtml.select("p.topicauthor");
                        Element authorE = (Element)authorElements.get(0);
                        author = authorE.text();
                        //log.debug("author:"+author);

                        Item f = new Item();
                        f.setLink(link);
                        f.setOrgLink(link);
                        f.setTitle(title);
                        f.setAuthor(author);
                        f.setTotalReply(Utils.isNullInt(replyCount));
                        f.setTotalRead(Utils.isNullInt(readCount));
                        f.setCreateDate(createDate);
                        f.setTopicCurPage(topicCurPage);
                        f.setFeedType(feed.getType());

                        //AnnountTopic
                        if( annoucement == false){
                            //Sort  Sticky Topic in First
                            if(topicCurPage==1){
                                c.add(Calendar.SECOND,-200);
                                f.setUpdateDate(c.getTime());
                            }
                            listTopic.add(f);
                        }else{
                            //Display Topic Announce
                            if(feed.isShowTopicAnnounce()){
                                f.setUpdateDate(c.getTime());
                                listAnTopic.add(f);
                            }
                        }
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }//while

            listAll.addAll(listAnTopic);
            listAll.addAll(listTopic);

        }catch(Exception e){
            e.printStackTrace();
        }finally{
            if(doc !=null){
                doc = null;
            }
            if(table !=null){
                table = null;
            }
            if(tr !=null){
                tr = null;
            }
        }
        return listAll;
    }

    private String getThaiviTopicId(String link){
        String id = "";
        /** substr link for get id= topic for save image **/
        String temp = link.substring(link.indexOf("t=")+2,link.length());
        if(temp.indexOf("&") != -1){
            id = temp.substring(0,temp.indexOf("&"));
        }else{
            id = temp;
        }
        log.debug("thaiviTopicId["+id+"]");

        return id;
    }

    public  static String getTopicReplyCount(String link){
        String r = "";
        try{
            URL urlObj = new URL(link);
            Document doc = Jsoup.parse(urlObj,Constants.JSOUP_TIMEOUT_SHORT);
            Iterator<Element> tables = doc.select("td[class=gensmall]").iterator();
            if(tables.hasNext()){
                String text = Utils.isNull(tables.next().text());
                //log.debug("Text:"+text+"");
                if(text.indexOf("posts") != -1){
                    r = Utils.isNull(text.substring(2,text.indexOf("posts")));
                    log.debug("r:"+r+"");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
        }
        return r;
    }

    public  static String getTopicTotalReply(String html){
        String r = "";
        try{
            Document doc =null;//Jsoup.parse(html);
            try{
                doc = Jsoup.parse(html);
            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(html);
            }
            Iterator<Element> tables = doc.select("td[class=gensmall]").iterator();
            if(tables.hasNext()){
                String text = Utils.isNull(tables.next().text());
                //log.debug("Text:"+text+"");
                if(text.indexOf("posts") != -1){
                    r = Utils.isNull(text.substring(2,text.indexOf("posts")));
                    log.debug("r:"+r+"");
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
        }
        return r;
    }
}
