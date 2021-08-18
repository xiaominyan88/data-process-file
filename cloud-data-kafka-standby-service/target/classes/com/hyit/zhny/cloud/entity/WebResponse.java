package com.hyit.zhny.cloud.entity;

import com.hyit.zhny.cloud.entity.response.OutputDataResponseEntity;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class WebResponse implements Serializable {

    private static final long serialVersionUID = 1373984982574414691L;

    private String statusCode;

    private String message;

    private List<OutputDataResponseEntity> result;

    private String version;

    public WebResponse(){ }
}
