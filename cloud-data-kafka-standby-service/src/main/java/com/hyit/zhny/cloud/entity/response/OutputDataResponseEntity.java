package com.hyit.zhny.cloud.entity.response;

import lombok.Data;

import java.io.Serializable;

@Data
public class OutputDataResponseEntity implements Serializable {

    private String id;
    private String tag;
    private double value;
    private String time;
    private String dataType;
}
