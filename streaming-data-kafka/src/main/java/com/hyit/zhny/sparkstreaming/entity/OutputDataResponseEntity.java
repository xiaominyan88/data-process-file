package com.hyit.zhny.sparkstreaming.entity;

import java.io.Serializable;

public class OutputDataResponseEntity implements Serializable {

    private String id;
    private String tag;
    private double value;
    private String time;
    private String dataType;

    public OutputDataResponseEntity(String id,String tag,double value,String time,String dataType){
        this.id = id;

        this.tag = tag;

        this.value = value;

        this.time = time;

        this.dataType = dataType;
    }

    public  String getId(){ return id; }

    public void setId(String id){ this.id = id; }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

}
