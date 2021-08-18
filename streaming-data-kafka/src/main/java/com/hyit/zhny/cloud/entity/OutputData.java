package com.hyit.zhny.cloud.entity;

import java.io.Serializable;
import java.util.Map;

public class OutputData implements Serializable {

    private static final long serialVersionUID = -3311261040132370005L;

    private Integer type =1;

    private Map<String,String> dataMap;

    private String dataStr;

    public Map<String, String> getDataMap() {
        return dataMap;
    }
    public void setDataMap(Map<String, String> dataMap) {
        this.dataMap = dataMap;
    }
    public Integer getType() {
        return type;
    }
    public void setType(Integer type) {
        this.type = type;
    }
    public String getDataStr() {
        return dataStr;
    }
    public void setDataStr(String dataStr) {
        this.dataStr = dataStr;
    }

}
