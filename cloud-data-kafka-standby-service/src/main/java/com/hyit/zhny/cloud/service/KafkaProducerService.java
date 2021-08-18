package com.hyit.zhny.cloud.service;

import com.hyit.zhny.cloud.entity.OutputData;
import com.hyit.zhny.cloud.entity.response.OutputDataResponseEntity;
import org.apache.kafka.clients.producer.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.Future;

@Service
public class KafkaProducerService {

    class DefineCallBack implements Callback{

        public void onCompletion(RecordMetadata recordMetadata,Exception e){
            if(recordMetadata != null){
                System.out.println("写入的分区partition为：" + recordMetadata.partition() + " || 写入的偏移量offset为：" + recordMetadata.offset());
            }else{
                e.printStackTrace();
            }
        }

    }

    private static Properties props = new Properties();

    static{
        props.put("bootstrap.servers",System.getProperty("bootstrap.servers"));
        props.put("security.protocol","SASL_PLAINTEXT");
        props.put("sasl.mechanism","GSSAPI");
        props.put("acks","1");
        props.put("retries","3");
        props.put("max.in.flight.request.per.connection","1");
        props.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer","com.hyit.zhny.cloud.util.OutputDataSerializer");
    }

    public boolean dataTransferToKafkaService(List<OutputDataResponseEntity> collectionList){

        String topic = System.getProperty("topic");

        Producer<String,OutputData> kafkaProducer = new KafkaProducer<String, OutputData>(props);

        boolean insert_status = false;

        for(OutputDataResponseEntity outDataEntity : collectionList){

            Map<String,String> map = new HashMap<>();

            map.put("id",outDataEntity.getId());

            map.put("tag",outDataEntity.getTag());

            map.put("value",String.valueOf(outDataEntity.getValue()));

            map.put("time",outDataEntity.getTime());

            map.put("dataType",outDataEntity.getDataType());

            OutputData od = new OutputData();

            od.setDataMap(map);

            ProducerRecord<String, OutputData> record = new ProducerRecord<>(topic,od);

            try{

                Future<RecordMetadata> recordMetadataFuture = kafkaProducer.send(record, new DefineCallBack());

                if(recordMetadataFuture.get() != null) insert_status = true;

            }catch (Exception e){
                e.printStackTrace();
            }

        }

        return insert_status;

    }

}
