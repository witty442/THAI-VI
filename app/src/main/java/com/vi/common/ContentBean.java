package com.vi.common;

import java.io.Serializable;

public class ContentBean implements Serializable{
  /**
	 * 
	 */
  private static final long serialVersionUID = -6610776958937449974L;
  private String content;
  private String type;
  private String imageUrl;
  private String linkUrl;
  private boolean imageLocal;
  private boolean thaiviIcon;
  private String fileType;
  
    
	public String getLinkUrl() {
	return linkUrl;
}
public void setLinkUrl(String linkUrl) {
	this.linkUrl = linkUrl;
}
	public boolean isThaiviIcon() {
	return thaiviIcon;
	}
	public void setThaiviIcon(boolean thaiviIcon) {
		this.thaiviIcon = thaiviIcon;
	}
	public boolean isImageLocal() {
	    return imageLocal;
	}
	public void setImageLocal(boolean imageLocal) {
		this.imageLocal = imageLocal;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getFileType() {
		return fileType;
	}
	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
  
	
  
}
