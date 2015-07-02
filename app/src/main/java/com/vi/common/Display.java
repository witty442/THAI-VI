package com.vi.common;

public class Display {
	
  private long id = -1;
  private String title;
  private String content;
  private String author;
  private String postDate;
  private String type;
  private boolean topicTitle;
  private String showImage; 

  
public String getShowImage() {
	return showImage;
}
public void setShowImage(String showImage) {
	this.showImage = showImage;
}
public boolean isTopicTitle() {
	return topicTitle;
}
public void setTopicTitle(boolean topicTitle) {
	this.topicTitle = topicTitle;
}
public String getType() {
	return type;
}
public void setType(String type) {
	this.type = type;
}
public String getPostDate() {
	return postDate;
}
public void setPostDate(String postDate) {
	this.postDate = postDate;
}
public long getId() {
	return id;
}
public void setId(long id) {
	this.id = id;
}
public String getTitle() {
	return title;
}
public void setTitle(String title) {
	this.title = title;
}
public String getContent() {
	return content;
}
public void setContent(String content) {
	this.content = content;
}
public String getAuthor() {
	return author;
}
public void setAuthor(String author) {
	this.author = author;
}
  
  
}
