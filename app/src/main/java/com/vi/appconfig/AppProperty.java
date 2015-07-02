package com.vi.appconfig;

import java.io.Serializable;

public class AppProperty implements Serializable{

private static final long serialVersionUID = 1935352452187426811L;
	
private String configName;
private String configValue;

public String getConfigName() {
	return configName;
}
public void setConfigName(String configName) {
	this.configName = configName;
}
public String getConfigValue() {
	return configValue;
}
public void setConfigValue(String configValue) {
	this.configValue = configValue;
}
  
  
}
