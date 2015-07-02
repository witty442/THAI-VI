package com.vi.common;

public class Control {
	
  private int textSize;
  private int textColor;
  private int bgColor;
  private String currentStyle;
  
  private int currentPage;
 
  
public String getCurrentStyle() {
	return currentStyle;
}
public void setCurrentStyle(String currentStyle) {
	this.currentStyle = currentStyle;
}
public int getTextColor() {
	return textColor;
}
public void setTextColor(int textColor) {
	this.textColor = textColor;
}
public int getTextSize() {
	return textSize;
}
public void setTextSize(int textSize) {
	this.textSize = textSize;
}
public int getBgColor() {
	return bgColor;
}
public void setBgColor(int bgColor) {
	this.bgColor = bgColor;
}
public int getCurrentPage() {
	return currentPage;
}
public void setCurrentPage(int currentPage) {
	this.currentPage = currentPage;
}

  
}
