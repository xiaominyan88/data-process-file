package com.hyit.zhny.coud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@SpringBootApplication
@EnableEurekaClient
public class CloudDataCacheServiceAppilcation {
    public static void main(String[] args) {
        SpringApplication.run(CloudDataCacheServiceAppilcation.class,args);
    }
}
