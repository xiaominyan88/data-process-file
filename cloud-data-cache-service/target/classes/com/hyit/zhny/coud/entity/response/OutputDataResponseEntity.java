package com.hyit.zhny.coud.entity.response;

import lombok.Data;

@Data
public class OutputDataResponseEntity {
    private String id;
    private String tag;
    private double value;
    private String time;
    private String dataType;
}
