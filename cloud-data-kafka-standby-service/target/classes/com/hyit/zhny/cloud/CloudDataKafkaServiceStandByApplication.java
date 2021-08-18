package com.hyit.zhny.cloud;

import com.hyit.zhny.cloud.config.KafkaConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class CloudDataKafkaServiceStandByApplication {

    public static void main(String[] args) {
        try{
            KafkaConfig.setConfig();
        }catch(Exception e){
            e.printStackTrace();
        }
        SpringApplication.run(CloudDataKafkaServiceStandByApplication.class,args);
    }
}
