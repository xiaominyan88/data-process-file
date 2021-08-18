package com.hyit.zhny.sparkstreaming.sink;

import com.codahale.metrics.MetricRegistry;
import org.apache.spark.SecurityManager;
import org.apache.spark.metrics.sink.Sink;

import java.util.Properties;

public class KafkaSink implements Sink {


    private Properties properties;

    private SecurityManager securityManager;

    private MetricRegistry metricRegistry;


    @Override
    public void start(){

    }

    @Override
    public void stop(){

    }

    @Override
    public void report(){

    }




}
