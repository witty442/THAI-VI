package com.vi.appconfig;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AppConfig implements Serializable{
	
/**
	 * 
	 */
private static final long serialVersionUID = 7043999595284172568L;

private Map<String ,AppProperty> appConfigMap = new HashMap<String, AppProperty>();

public Map<String, AppProperty> getAppConfigMap() {
	return appConfigMap;
}

public void setAppConfigMap(Map<String, AppProperty> appConfigMap) {
	this.appConfigMap = appConfigMap;
}



}
