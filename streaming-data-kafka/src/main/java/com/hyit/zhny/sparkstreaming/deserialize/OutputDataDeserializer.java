package com.hyit.zhny.sparkstreaming.deserialize;


import com.hyit.zhny.cloud.entity.OutputData;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.util.Map;

public class OutputDataDeserializer implements Deserializer<OutputData> {

    @Override
    public void configure(Map<String,?> map, boolean b){

    }

    @Override
    public OutputData deserialize(String topic, byte[] bytes){

        OutputData dataMsg = null;
        // 序列化对象遵循的前缀格式
//        if(bytes[0] == -84 &&
//            bytes[1] == -19 &&
//            bytes[2] == 0 &&
//            bytes[3] == 5){
        if(bytes.length > 4) {
            ByteArrayInputStream byteArrayInputStream = null;
            ObjectInputStream objectInputStream = null;
            try {
                byteArrayInputStream = new ByteArrayInputStream(bytes);
                objectInputStream = new ObjectInputStream(byteArrayInputStream);
                dataMsg = (OutputData) objectInputStream.readObject();
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                close(byteArrayInputStream);
                close(objectInputStream);
            }
        }
//        }
        return dataMsg;
    }

    public void close(InputStream inputStream){
        if(inputStream != null){
            try{
                inputStream.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public void close(){

    }
}
