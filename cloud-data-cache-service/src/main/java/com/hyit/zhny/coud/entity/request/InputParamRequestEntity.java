package com.hyit.zhny.coud.entity.request;

import lombok.Data;

import java.util.List;

@Data
public class InputParamRequestEntity {
    //用于判断输入的数据是蒸汽还是电量
    private String tagFlag;
    //时间类型，用于判断是写入的数据是天数据还是月数据
    private String timeType;
    //数据的实体类型集合
    private List<InputDataRequestEntity> list;
}
