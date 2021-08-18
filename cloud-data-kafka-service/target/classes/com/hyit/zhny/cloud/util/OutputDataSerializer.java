package com.hyit.zhny.cloud.util;

import com.hyit.zhny.cloud.entity.OutputData;
import com.hyit.zhny.cloud.entity.response.OutputDataResponseEntity;
import org.apache.kafka.common.serialization.Serializer;


import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Map;

public class OutputDataSerializer implements Serializer<OutputData> {

    @Override
    public void configure(Map<String,?> map, boolean b){

    }


    @Override
    public byte[] serialize(String topic, OutputData dataMsg){

        if (dataMsg != null) {
            ObjectOutputStream oos = null;
            ByteArrayOutputStream baos = null;
            try {
                // 序列化
                baos = new ByteArrayOutputStream();
                oos = new ObjectOutputStream(baos);
                oos.writeObject(dataMsg);
                byte[] bytes = baos.toByteArray();
                return bytes;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                close(baos);
                close(oos);
            }
        }
        return null;
    }

    public void close(OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.close();
            } catch(Exception ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void close(){

    }
}
