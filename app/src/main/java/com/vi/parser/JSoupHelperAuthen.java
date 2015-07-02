package com.vi.parser;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.vi.authen.ThaiVIAuthen;
import com.vi.common.Display;
import com.vi.common.Feed;
import com.vi.common.Item;
import com.vi.utils.Constants;
import com.vi.utils.LogUtils;
import com.vi.utils.Utils;

public class JSoupHelperAuthen implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -5635840296640725429L;
    public static JSoupHelperAuthen jsoupHelperAuthen = null;
    public static ThaiVIAuthen authen = null;
    static LogUtils log = new LogUtils("JSoupHelperAuthen");


    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }

    public static JSoupHelperAuthen getInstance(String userName,String password){
        if(jsoupHelperAuthen ==null){
            jsoupHelperAuthen = new JSoupHelperAuthen();
            authen = new ThaiVIAuthen().initSessionLogin( userName, password);
        }
        return jsoupHelperAuthen;
    }

    public static JSoupHelperAuthen newInstance(String userName,String password){
        jsoupHelperAuthen = new JSoupHelperAuthen();
        authen = new ThaiVIAuthen().initSessionLogin( userName, password);
        return jsoupHelperAuthen;
    }

    public  boolean verifyAuthen(){
        boolean pass = false;
        try{
            String pageGet = "http://board.thaivi.org/viewtopic.php?f=49&t=53486";
            String resultHtml = authen.GetPageContent(pageGet);

            //<span class="gensmall">The board requires you to be registered and logged in to view this forum.</span>
            if(resultHtml.indexOf("The board requires you to be registered and logged in to view this forum.") != -1){
                //log.debug("UserName or Password wrong");
                pass = false;
            }else{
                pass = true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return pass;
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

    public  List<Display> getContentFromWeb(String userName,String password,Item item,int currentPage) throws Exception{
        log.debug("getContentFromWeb");
        //Get Login and get sessionid
        return getContentThaiVIBoardByPage(userName,password,item,currentPage);
    }

    public  int calcTotalPage1(int totalReply,int rowPerPage){
        int totalPage = 1;
        float totalReplyF = totalReply;
        float rowPerPageF = rowPerPage;
        float pageC = totalReplyF/rowPerPageF;
        BigDecimal r = new BigDecimal(pageC);
        r = r.setScale(0,BigDecimal.ROUND_UP);
        totalPage = r.intValue();
        return totalPage;
    }

    private List<Display> getContentThaiVIBoardByPage(String userName,String password,Item item,int currentPage){
        //http://board.thaivi.org/viewtopic.php?f=35&t=36591
        //http://board.thaivi.org/viewtopic.php?f=35&t=36591&start=30
        List<Display> contentAllList = new ArrayList<Display>();
        try{
            int rowPerPage = Constants.THAIVI_ROW_PER_PAGE;
            log.debug("getContentThaiVIBoardByPage currentPage:"+currentPage);

            /** substr link for get id= topic for save image **/
            item.setThaiviTopicId(getThaiviTopicId(item.getLink()));

            if(currentPage ==1){
                item.setLink(item.getOrgLink());
                List<Display> contentList = getContentThaiVIbyPage(userName,password,item,null,currentPage);
                contentAllList.addAll(contentList);
            }else {
                String url = item.getOrgLink()+"&start="+(currentPage-1)*rowPerPage;
                item.setLink(url);
                List<Display> contentList = getContentThaiVIbyPage(userName,password,item,null,currentPage);
                contentAllList.addAll(contentList);
            }
            log.debug("page["+currentPage+"]:"+contentAllList.size());

        }catch(Exception e){
            e.printStackTrace();
        }
        return contentAllList;
    }

    /**
     * getFeedsTopicList (List Topic Stock 100 )
     * @param feed
     * @param topicCurPage
     * @return
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

            String result = authen.GetPageContent(feed.getLink());
            doc = null;//Jsoup.parse(result);
            try{
                doc = Jsoup.parse(result);
            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(result);
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
                        ///log.debug("tdHtml:"+tdHtml);

                        Document docTdHtml = Jsoup.parse(tdHtml);

                        //** Get Link and Title **/
                        Elements links = docTdHtml.select("a[href]");
                        Element linkElement = (Element)links.get(1);//default

                        //Check Element is Topic Title
                        Element linkElementChk = (Element)links.get(0);
                        String topictitle = Utils.isNull(linkElementChk.getElementsByAttribute("class").attr("class"));
                        if(topictitle.equals("topictitle")){
                            linkElement = linkElementChk;
                        }

                        link = linkElement.attr("href");
                        link = "http://board.thaivi.org"+link.substring(1,link.length());
                        title = linkElement.text();
                        if(title.startsWith("MK")){
                            title = "MK";
                        }

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
                                    //log.debug("createDateStr:"+createDateStr);
                                    //log.debug("createDate:"+createDate);
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

                        //log.debug("title:"+title);

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
                                //c.add(Calendar.MILLISECOND,-200);
                                f.setUpdateDate(c.getTime());
                            }
                            listTopic.add(f);
                        }else{
                            //Display Topic Announce
                            if(feed.isShowTopicAnnounce()){
                                c.add(Calendar.MILLISECOND,400);
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

    private List<Display> getContentThaiVIbyPage(String userName,String password,Item currentItem,StringBuffer contentStr,int page ) throws Exception{
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
        //log.debug("url:"+currentItem.getLink());
        try{
            /** Calculate no **/
            if(page==1){
                no = 1;
            }else{
                no = ((page-1)*Constants.THAIVI_ROW_PER_PAGE)+1;
            }
            //log.debug("Page["+page+"]no["+no+"]");
            String result = null;
            doc = null;//Jsoup.parse(result);
            try{
                result = authen.GetPageContent(currentItem.getLink());
                doc = Jsoup.parse(result);

                tableE = doc.select("div[id=pagecontent]").first();
                tablesIT = tableE.select("table[class=tablebg postrow]").iterator();

            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry LoginThaiVI and Get Content");

                // Relogin
                JSoupHelperAuthen.newInstance(userName, password);

                result = authen.GetPageContent(currentItem.getLink());
                doc = Jsoup.parse(result);

                tableE = doc.select("div[id=pagecontent]").first();
                tablesIT = tableE.select("table[class=tablebg postrow]").iterator();
            }
            //Get New TotalReply
            currentItem.setTotalReply(Utils.isNullInt(getTopicTotalReply(result)));

            while(tablesIT.hasNext()){
                title = "";
                author ="";
                authorRole = "";
                postDate = "";
                content ="";

                //log.debug("["+i+"]:"+tables.next().text());
                tableReply = "<table><tbody><tr><td>"+tablesIT.next().outerHtml()+"</table>";
                //log.debug("tableReply:\n"+tableReply);
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
                    //log.debug("Content2:****** \n"+content2);
                    if( !Utils.isNull(content2).equals("")){

                        docContent2 = Jsoup.parse(content2);
                        docContentE12 = docContent2.select("div[class=postbody]").first();
                        docContentE22 = docContent2.select("td[class=postdetails]").first();
                        //log.debug("Content1E2:"+docContentE22.ownText());

                        content = StringEscapeUtils.unescapeHtml(docContentE12.html());
                        authorRole = docContentE22 != null?docContentE22.ownText():"ทั่วไป";
                    }

                    //log.debug("author: " + author);
                    //log.debug("authorRole: " + authorRole);
                    //log.debug("postDate: " + postDate);
                    //log.debug("content: " + content);

                }//while 2

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

                    //disp.setContent(splitImageContentAndSaveImageToLocal(content,currentItem));
                    disp.setAuthor(author);
                    disp.setPostDate(postDate);
                    //add to List
                    //log.debug("*********Add ContetList no["+no+"]content:\n"+content);
                    contentList.add(disp);

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
            //URL urlObj = new URL(link);
            Document doc = null;// Jsoup.parse(authen.GetPageContent(link));
            try{
                doc = Jsoup.parse(authen.GetPageContent(link));
            }catch(Exception e){
                e.printStackTrace();
                log.debug("Retry conn");
                doc = Jsoup.parse(authen.GetPageContent(link));
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

    public  static String getTopicTotalReply(String html){
        String r = "";
        try{
            //URL urlObj = new URL(link);
            Document doc = null;//Jsoup.parse(html);
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
