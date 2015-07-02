
package com.vi.common;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.vi.storage.DBSchema;


public class Feed implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1680923903467395159L;

	private static final String LOG_TAG = "Feed";

	private long id = -1;
	private long idSub = -1;
	private String link;
	private String orgLink;
	private String title;
	private String type;
	private Date createDate = null;
	private Date updateDate = null;
	private boolean mEnabled = true;
	private String author;
	private List<Item> items;
	private int totalItem;
	private int totalReply;
	private int totalRead;
	
	//optional
	private boolean showTopicAnnounce;
	
	
	public long getIdSub() {
		return idSub;
	}

	public void setIdSub(long idSub) {
		this.idSub = idSub;
	}

	public boolean isShowTopicAnnounce() {
		return showTopicAnnounce;
	}

	public void setShowTopicAnnounce(boolean showTopicAnnounce) {
		this.showTopicAnnounce = showTopicAnnounce;
	}

	public String getOrgLink() {
		return orgLink;
	}

	public void setOrgLink(String orgLink) {
		this.orgLink = orgLink;
	}

	public int getTotalReply() {
		return totalReply;
	}

	public void setTotalReply(int totalReply) {
		this.totalReply = totalReply;
	}

	public int getTotalRead() {
		return totalRead;
	}

	public void setTotalRead(int totalRead) {
		this.totalRead = totalRead;
	}

	public int getTotalItem() {
		return totalItem;
	}

	public void setTotalItem(int totalItem) {
		this.totalItem = totalItem;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public void enable() {
		this.mEnabled = true;
	}
	
	public void disable() {
		this.mEnabled = false;
	}
	
	public void setEnabled(int state) {
		this.mEnabled = state != DBSchema.OFF;
	}
	
	public boolean isEnabled() {
		return this.mEnabled;
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

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public List<Item> getItems() {
		return items;
	}

	public void setItems(List<Item> items) {
		this.items = items;
	}
	
	
}
