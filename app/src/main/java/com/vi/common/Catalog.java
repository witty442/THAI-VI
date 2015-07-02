package com.vi.common;

import java.util.Date;

public class Catalog {
    private int id;
	private String title;
	private String type;
	private String author;
	private Date createDate = null;
	private int totalItem;
	private String topten;
	private boolean fav;


	public boolean isFav() {
		return fav;
	}
	public void setFav(boolean fav) {
		this.fav = fav;
	}
	public String getTopten() {
		return topten;
	}
	public void setTopten(String topten) {
		this.topten = topten;
	}
	public int getTotalItem() {
		return totalItem;
	}
	public void setTotalItem(int totalItem) {
		this.totalItem = totalItem;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
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
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
}
