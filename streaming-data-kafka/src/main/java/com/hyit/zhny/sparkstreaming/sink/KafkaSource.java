package com.hyit.zhny.sparkstreaming.sink;

import com.codahale.metrics.MetricRegistry;
import org.apache.spark.metrics.source.Source;

public class KafkaSource implements Source {


    @Override
    public String sourceName() {
        return "";
    }

    @Override
    public MetricRegistry metricRegistry() {
        return null;
    }
}
