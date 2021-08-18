package com.hyit.zhny.sparkstreaming.source;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import org.apache.spark.metrics.source.Source;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

public class KafkaJVMCPUSource implements Source {

    @Override
    public String sourceName() {
        return "kafkaCPUJVM";
    }

    @Override
    public MetricRegistry metricRegistry() {
        MetricRegistry metricRegistry = null;
        try{
            Gauge gauge = new Gauge<Long>(){
                MBeanServer mBean = ManagementFactory.getPlatformMBeanServer();
                ObjectName name = new ObjectName("java.lang", "type", "OperatingSystem");
                @Override
                public Long getValue(){
                    try{
                        return (Long)mBean.getAttribute(name,"ProcessCpuTime");
                    }catch(Exception e){
                        e.printStackTrace();
                        return 0L;
                    }
                }
            };

            metricRegistry = new MetricRegistry();

            metricRegistry.register(MetricRegistry.name("kafkaCPUJVM"), gauge);

        }catch(Exception e){
            e.printStackTrace();
        }

        return metricRegistry;

    }


}
