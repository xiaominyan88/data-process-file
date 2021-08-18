package com.hyit.zhny.sparkstreaming.consume;


import com.hyit.zhny.cloud.entity.OutputData;
import com.hyit.zhny.sparkstreaming.config.KerberosConfig;
import com.hyit.zhny.sparkstreaming.config.SparkSessionConfig;
import com.hyit.zhny.sparkstreaming.config.StreamContextConfig;
import com.hyit.zhny.sparkstreaming.connectionpool.connect.KafkaOffsetConnectionPools;
import com.hyit.zhny.sparkstreaming.deserialize.OutputDataDeserializer;
import com.hyit.zhny.sparkstreaming.entity.OutputDataResponseEntity;
import com.hyit.zhny.sparkstreaming.utils.PhoenixUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.functions;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka010.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static org.apache.spark.sql.functions.col;

public class KafkaConsumeMain implements Serializable {


    private String kafkaBrokers = "node5:6667,node6:6667,node13:6667";

    private  Map<TopicPartition,Long> offsetMap = new HashMap<>();

    public static void main(String[] args) {

        String version = args[0];

        String topic = args[1];

        String groupId = args[2];

        KafkaConsumeMain consumeMain = new KafkaConsumeMain();


        try{

            KerberosConfig.getkerberosConfiguration(version);

            JavaStreamingContext jsc = consumeMain.initializeStreamContext().createStreamingContext();

            SparkConf sparkConf  = StreamContextConfig.getSparkConf();

            SparkSession sparkSession = SparkSessionConfig.getSparkSession(sparkConf);

            sparkSession.sparkContext().setLogLevel("WARN");

            consumeMain.initOffsetMap(version,topic);

            consumeMain.JavaStreamConsumeFromKafkaToDB(version,jsc,sparkSession,topic,groupId);

            jsc.start();

            jsc.awaitTermination();

        }catch (Exception e){

            e.printStackTrace();

        }


    }

    public void initOffsetMap(String version,String topic) throws SQLException {

        Connection conn = KafkaOffsetConnectionPools.getConnection(version);

        Statement stmt = conn.createStatement();

        ResultSet rs = stmt.executeQuery("select topic,kafka_partition,kafka_offset from kafka_offsets");

        while(rs.next()){

            TopicPartition tp = new TopicPartition(rs.getString("topic"),rs.getInt("kafka_partition"));

            offsetMap.put(tp,rs.getLong("kafka_offset"));

        }

        rs.close();

        stmt.close();

        KafkaOffsetConnectionPools.returnConnection(conn);
    }


    StreamContextConfig initializeStreamContext() throws ClassNotFoundException,IllegalAccessException,InstantiationException{

        return StreamContextConfig.getInstance();

    }

    void insertPhoenixTable(String version, SparkSession sparkSession, JavaRDD<Row> rdd, String desTable){

        List<Row> list = rdd.collect();

        try{

            if(list.size() > 0){

                Connection conn = PhoenixUtils.getConnection(version,"jdbc:phoenix:172.30.92.235,172.30.92.236,172.30.92.237:2181");

                conn.setAutoCommit(false);

                PreparedStatement prst = conn.prepareStatement("UPSERT INTO " + desTable  + " (\"ROW\",MEASUREPOINTID,SAMPLETIME,MEASURETAG,VALUE1,UNIT) VALUES (?,?,?,?,?,?)");

                int m = 0;

                for(int i = 0; i < list.size(); i ++){

                    if(!list.get(i).anyNull()){

                        prst.setString(1,list.get(i).getString(0));

                        prst.setString(2,list.get(i).getString(1));

                        prst.setString(3,list.get(i).getString(2));

                        prst.setString(4,list.get(i).getString(3));

                        prst.setBigDecimal(5,new BigDecimal(list.get(i).getDouble(4)));

                        prst.setString(6,list.get(i).getString(5));

                        prst.execute();

                        m++;

                        if(m%1000 == 0){

                            conn.commit();

                        }

                    }

                }

                conn.commit();

                prst.close();

                PhoenixUtils.returnConnection(conn);

            }

        }catch (Exception e){

            e.printStackTrace();

        }


    }

    void filterDataFrameAndInsertIntoDesTable(String version, SparkSession sparkSession, Dataset<Row> df){

        if(!df.javaRDD().isEmpty()){

            df.createOrReplaceTempView("temp_filter_table");

            Dataset<Row> dayDataSet = sparkSession.sql("select concat(id,'|',tag,'|',9999999999999-unix_timestamp(time)) as rowx,id,time,tag,value from temp_filter_table where dataType = 'day'").toDF().withColumn("unit", functions.callUDF("to_unit_func",col("tag")));

            Dataset<Row> monthDataSet = sparkSession.sql("select concat(id,'|',tag,'|',9999999999999-unix_timestamp(time)) as rowx,id,time,tag,value from temp_filter_table where dataType = 'month'").toDF().withColumn("unit", functions.callUDF("to_unit_func",col("tag")));

            JavaRDD<Row> dayFilterRDD = dayDataSet.javaRDD();

            JavaRDD<Row> monthFilterRDD = monthDataSet.javaRDD();

            if(!dayDataSet.javaRDD().isEmpty())
                dayDataSet.show(5);
            else
                System.out.println("日表数据集为空！！！！");

            if(!monthDataSet.javaRDD().isEmpty())
                monthDataSet.show(5);
            else
                System.out.println("月数据表为空！！！！");

            if(!dayFilterRDD.isEmpty() && monthFilterRDD.isEmpty()){

                insertPhoenixTable(version, sparkSession, dayFilterRDD, "NEW_DW_DAY_TJL_LJ");

            }else if(!monthFilterRDD.isEmpty() && dayFilterRDD.isEmpty()){

                insertPhoenixTable(version, sparkSession, monthFilterRDD, "NEW_DW_MONTH_TJL_LJ");

            }else if(!dayFilterRDD.isEmpty() && !monthFilterRDD.isEmpty()){

                insertPhoenixTable(version, sparkSession, monthFilterRDD, "NEW_DW_MONTH_TJL_LJ");

                insertPhoenixTable(version, sparkSession, dayFilterRDD, "NEW_DW_DAY_TJL_LJ");

            }

        }


    }

    public void JavaStreamConsumeFromKafkaToDB(String version, JavaStreamingContext jsc, SparkSession sparkSession, String topic, String groupId){

        try{

            sparkSession.sql("set hive.exec.dynamic.partition.mode=nonstrict");

            sparkSession.sql("set hive.exec.dynamic.partition=true");

            sparkSession.sql("set spark.sql.auto.repartition=true");

            Map<String,Object> kafkaParams = new HashMap<>();


            kafkaParams.put("bootstrap.servers",kafkaBrokers);
            kafkaParams.put("key.deserializer", StringDeserializer.class);
            kafkaParams.put("value.deserializer", OutputDataDeserializer.class);
            kafkaParams.put("group.id",groupId);
            kafkaParams.put("security.protocol","SASL_PLAINTEXT");
            kafkaParams.put("sasl.mechanism","GSSAPI");
            kafkaParams.put("sasl.kerberos.service.name","kafka");
            kafkaParams.put("enable.auto.commit",false);
            kafkaParams.put("auto.offset.reset","earliest");

            JavaInputDStream<ConsumerRecord<String, OutputData>> stream = KafkaUtils.createDirectStream(jsc, LocationStrategies.PreferConsistent(),
                    ConsumerStrategies.Subscribe(Collections.singletonList(topic),kafkaParams,offsetMap));


            final AtomicReference<OffsetRange[]> offsetRanges = new AtomicReference<>();

            JavaDStream<OutputDataResponseEntity> objectStream1 =

                    stream.map(new Function<ConsumerRecord<String, OutputData>, OutputDataResponseEntity>() {
                        @Override
                        public OutputDataResponseEntity call(ConsumerRecord<String, OutputData> v1) throws Exception {

                            OutputData opd = v1.value();

                            if(opd != null){

                                Map<String,String> dataMap = opd.getDataMap();

                                String id = dataMap.get("id") == null? null:dataMap.get("id");

                                String tag = dataMap.get("tag") == null? null:dataMap.get("tag");

                                double value = dataMap.get("value") == null? 0.0000:Double.parseDouble(dataMap.get("value"));

                                String time = dataMap.get("time") == null? null:dataMap.get("time");

                                String dataType = dataMap.get("dataType") == null? null:dataMap.get("dataType");

                                return new OutputDataResponseEntity(id,tag,value,time,dataType);

                            }else{
                                throw new ExceptionInInitializerError("消费异常，请检查消费记录");
                            }

                        }
                    });


            stream.foreachRDD(new VoidFunction<JavaRDD<ConsumerRecord<String, OutputData>>>() {
                @Override
                public void call(JavaRDD<ConsumerRecord<String, OutputData>> consumerRecordJavaRDD) throws Exception {

                    OffsetRange[] offsets = ((HasOffsetRanges) consumerRecordJavaRDD.rdd()).offsetRanges();

                    offsetRanges.set(offsets);

                    Connection conn = KafkaOffsetConnectionPools.getConnection(version);

                    Statement stmt = conn.createStatement();

                    for(OffsetRange offsetRange : offsetRanges.get()){

                        stmt.executeUpdate("replace into kafka_offsets (topic,kafka_partition,kafka_offset,create_time) " +
                                                "values " +
                                                "('" + offsetRange.topic() +
                                                "'," + offsetRange.partition() +
                                                "," + offsetRange.untilOffset() +
                                                "," + System.currentTimeMillis() + ")");

                    }

                    stmt.close();

                    KafkaOffsetConnectionPools.returnConnection(conn);
                }
            });

            objectStream1.foreachRDD(new VoidFunction<JavaRDD<OutputDataResponseEntity>>() {
                @Override
                public void call(JavaRDD<OutputDataResponseEntity> outputDataResponseEntityJavaRDD) throws Exception {

                    if(!outputDataResponseEntityJavaRDD.isEmpty()){


                        List<OutputDataResponseEntity> list = outputDataResponseEntityJavaRDD.collect();

                        Dataset<Row> rowDF = sparkSession.createDataFrame(outputDataResponseEntityJavaRDD,OutputDataResponseEntity.class);

                        filterDataFrameAndInsertIntoDesTable(version,sparkSession,rowDF);

                    }
                }
            });



        }catch (Exception e){
            e.printStackTrace();
        }


    }


}
