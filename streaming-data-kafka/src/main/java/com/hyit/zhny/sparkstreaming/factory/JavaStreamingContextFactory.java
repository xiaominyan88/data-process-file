package com.hyit.zhny.sparkstreaming.factory;

import org.apache.spark.streaming.api.java.JavaStreamingContext;

public interface JavaStreamingContextFactory {

    public JavaStreamingContext createStreamingContext();

    public JavaStreamingContext createStreamContextWithOpenCheckPoint(boolean isOpen, String path);
}
