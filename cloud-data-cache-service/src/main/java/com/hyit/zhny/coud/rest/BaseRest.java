package com.hyit.zhny.coud.rest;

import com.hyit.zhny.coud.entity.WebResponse;
import com.hyit.zhny.coud.entity.response.OutputDataResponseEntity;
import com.hyit.zhny.coud.predictions.Predictions;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class BaseRest {

    public BaseRest() { }

    public WebResponse buildResponse(List<OutputDataResponseEntity> data){
        WebResponse webResponse = new WebResponse();
        if(Predictions.checkNotNull(data)){
            webResponse.setMessage("成功收到返回结果");
            webResponse.setStatusCode("200");
            webResponse.setResult(data);
        }else{
            webResponse.setMessage("无结果返回或者返回结果为null");
            webResponse.setStatusCode("500");
            webResponse.setResult(null);
        }
        return webResponse;
    }
}
