/*
 * Copyright (C) 2010-2011 Mathieu Favez - http://mfavez.com
 *
 *
 * This file is part of FeedGoal.
 * 
 * FeedGoal is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * FeedGoal is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with FeedGoal.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.vi.common;

import java.io.Serializable;
import java.net.URL;
import java.util.Date;


public class Item implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4783322463201965904L;


	private static final String LOG_TAG = "Item";
	
	
	private long id = -1;
	private String link;
	private String orgLink;
	private String title;
	private String description;
	private String content;
	private StringBuffer contentBuffer;
	private URL image = null;
	private Date createDate;
	private Date updateDate;
	private String pathFile;
	private boolean read = false;
	private boolean fav = false;
	private String type;
	private long countOpen = 0;
	private int totalReply;
	private int totalRead;
	private int curPage = 0;
	private int topicCurPage = 0;
	private String lastPosition;
	
	//optional
	private String author;
	private long feedId = -1;
	private String feedType;//Article,BOARD
    private String thaiviTopicId;
	
	
	public String getLastPosition() {
		return lastPosition;
	}

	public void setLastPosition(String lastPosition) {
		this.lastPosition = lastPosition;
	}

	public String getThaiviTopicId() {
		return thaiviTopicId;
	}

	public void setThaiviTopicId(String thaiviTopicId) {
		this.thaiviTopicId = thaiviTopicId;
	}
	
	public int getTopicCurPage() {
		return topicCurPage;
	}

	public void setTopicCurPage(int topicCurPage) {
		this.topicCurPage = topicCurPage;
	}

	public StringBuffer getContentBuffer() {
		return contentBuffer;
	}

	public void setContentBuffer(StringBuffer contentBuffer) {
		this.contentBuffer = contentBuffer;
	}

	public String getFeedType() {
		return feedType;
	}

	public void setFeedType(String feedType) {
		this.feedType = feedType;
	}
    
	public int getTotalReply() {
		return totalReply;
	}

	public int getTotalRead() {
		return totalRead;
	}

	public void setTotalRead(int totalRead) {
		this.totalRead = totalRead;
	}

	public void setTotalReply(int totalReply) {
		this.totalReply = totalReply;
	}

	public long getFeedId() {
		return feedId;
	}

	public void setFeedId(long feedId) {
		this.feedId = feedId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public int getCurPage() {
		return curPage;
	}

	public void setCurPage(int curPage) {
		this.curPage = curPage;
	}

	public Item() {
		createDate = new Date();
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getOrgLink() {
		return orgLink;
	}

	public void setOrgLink(String orgLink) {
		this.orgLink = orgLink;
	}

	public long getCountOpen() {
		return countOpen;
	}

	public void setCountOpen(long countOpen) {
		this.countOpen = countOpen;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public URL getImage() {
		return image;
	}

	public void setImage(URL image) {
		this.image = image;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Date getUpdateDate() {
		return updateDate;
	}

	public void setUpdateDate(Date updateDate) {
		this.updateDate = updateDate;
	}

	public String getPathFile() {
		return pathFile;
	}

	public void setPathFile(String pathFile) {
		this.pathFile = pathFile;
	}

	

	public boolean isRead() {
		return read;
	}

	public void setRead(boolean read) {
		this.read = read;
	}

	public boolean isFav() {
		return fav;
	}

	public void setFav(boolean fav) {
		this.fav = fav;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	
}
