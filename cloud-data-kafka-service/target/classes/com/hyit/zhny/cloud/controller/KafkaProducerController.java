package com.hyit.zhny.cloud.controller;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.hyit.zhny.cloud.entity.WebResponse;
import com.hyit.zhny.cloud.entity.response.OutputDataResponseEntity;
import com.hyit.zhny.cloud.service.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/kafkaProducer")
public class KafkaProducerController {

    @Autowired
    KafkaProducerService kps;

    @PostMapping("/dataToKafka")
    @ResponseBody
    public boolean dataTransferToKafkaController(@RequestBody WebResponse webResponse){

        boolean insert_status = false;

        if("200".equalsIgnoreCase(webResponse.getStatusCode())){
            List<OutputDataResponseEntity> list = webResponse.getResult();

            insert_status = kps.dataTransferToKafkaService(list);
        }

        return insert_status;
    }

    @PostMapping("/test")
    @ResponseBody
    public String testController(){
        return "hello!";
    }
}
