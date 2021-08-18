package com.hyit.zhny.coud.controller;

import com.hyit.zhny.coud.entity.WebResponse;
import com.hyit.zhny.coud.entity.request.InputParamRequestEntity;
import com.hyit.zhny.coud.entity.response.OutputDataResponseEntity;
import com.hyit.zhny.coud.rest.BaseRest;
import com.hyit.zhny.coud.service.EnergyDataProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/dataCanal")
public class EnergyDataProcessController {

    private static final String DATA_URL = "http://CLOUD-DATA-KAFKA-SERVICE";

    @Autowired
    EnergyDataProcessService edps;

    @Autowired
    RestTemplate restTemplate;

    @Autowired
    BaseRest baseRest;

    @PostMapping("/processDataDependingOnTag")
    @ResponseBody
    public WebResponse processDataCollectionController(@RequestBody InputParamRequestEntity in){
        List<OutputDataResponseEntity> result = edps.energyDataProcessAndReturnService(in);
        boolean flag = restTemplate.postForObject(DATA_URL + "/kafkaProducer/dataToKafka",baseRest.buildResponse(result),Boolean.class);
        if(flag){
            WebResponse response = new WebResponse();
            response.setStatusCode("200");
            response.setMessage("写入Kafka操作执行成功！");
            return response;
        }else{
            WebResponse response = new WebResponse();
            response.setStatusCode("500");
            response.setMessage("写入Kafka操作执行失败！");
            return response;
        }
    }

    @PostMapping("/formDataProcessingOnTag")
    @ResponseBody
    public WebResponse formDataProcessController(@RequestParam("file") MultipartFile multipartFile,@RequestParam("tagType") String tag,@RequestParam("timeType") String time){
        List<OutputDataResponseEntity> result = edps.fileDataParsingAndProcessingService(multipartFile,tag,time);
        if(result.size() == 0){
            WebResponse response = new WebResponse();
            response.setStatusCode("500");
            response.setMessage("解析文件失败，请检查文件格式或者文件行头是否符合要求");
            return response;
        }else{
            boolean flag = restTemplate.postForObject(DATA_URL + "/kafkaProducer/dataToKafka",baseRest.buildResponse(result),Boolean.class);
            if(flag){
                WebResponse response = new WebResponse();
                response.setStatusCode("200");
                response.setMessage("写入Kafka操作执行成功！");
                return response;
            }else{
                WebResponse response = new WebResponse();
                response.setStatusCode("500");
                response.setMessage("写入Kafka操作执行失败！");
                return response;
            }
        }
    }

    @RequestMapping("/page")
    public String dataAccessPageController(){
        return "testpage";
    }

    @RequestMapping("/uploadFilePage")
    public String uploadFilePageController(){
        return "uploadFilePage";
    }

    @PostMapping("/test")
    @ResponseBody
    public String testController(){
        return "hello!";
    }



}
