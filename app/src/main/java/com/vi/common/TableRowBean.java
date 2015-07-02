package com.vi.common;

import java.io.Serializable;

import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

public class TableRowBean{
	
	private int length;
	private String type;
	private String tableRowData;
	private TableRow tableRow = null;
	private TextView lastTextView = null;
	private ImageView lastImageView = null;
	
	
	public TextView getLastTextView() {
		return lastTextView;
	}
	public void setLastTextView(TextView lastTextView) {
		this.lastTextView = lastTextView;
	}
	public ImageView getLastImageView() {
		return lastImageView;
	}
	public void setLastImageView(ImageView lastImageView) {
		this.lastImageView = lastImageView;
	}
	public String getTableRowData() {
		return tableRowData;
	}
	public void setTableRowData(String tableRowData) {
		this.tableRowData = tableRowData;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public TableRow getTableRow() {
		return tableRow;
	}
	public void setTableRow(TableRow tableRow) {
		this.tableRow = tableRow;
	}
	
	

}
