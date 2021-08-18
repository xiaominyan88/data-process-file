package com.hyit.zhny.sparkstreaming.config;


import com.hyit.zhny.sparkstreaming.factory.JavaStreamingContextFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.streaming.Durations;
import org.apache.spark.streaming.api.java.JavaStreamingContext;

public class StreamContextConfig implements JavaStreamingContextFactory {

    private static JavaStreamingContext jssc;

    private static SparkConf sparkConf;

    private volatile static StreamContextConfig instance;

    private StreamContextConfig(){

    }

    static{
        sparkConf = new SparkConf().setAppName("energyDoubleControl");
        sparkConf.set("spark.streaming.stopGracefullyOnShutdown","true");
        sparkConf.set("spark.streaming.backpressure.enabled","true");
        sparkConf.set("spark.streaming.backpressure.initialRate","1000");
        sparkConf.set("spark.streaming.kafka.maxRatePerPartition","1000");
        jssc = new JavaStreamingContext(sparkConf, Durations.seconds(5));
    }


    @Override
    public JavaStreamingContext createStreamingContext(){

        return jssc;

    }

    @Override
    public JavaStreamingContext createStreamContextWithOpenCheckPoint(boolean isOpen, String path){

        if(isOpen){
            jssc.checkpoint(path);
        }

        return jssc;
    }

    public static SparkConf getSparkConf(){
        return sparkConf;
    }

    public static StreamContextConfig getInstance(){
        if(instance == null){
            synchronized (StreamContextConfig.class){
                if(instance == null){
                    instance = new StreamContextConfig();
                }
            }
        }
        return instance;
    }
}
