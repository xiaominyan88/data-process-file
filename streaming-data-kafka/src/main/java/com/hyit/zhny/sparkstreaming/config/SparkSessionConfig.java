package com.hyit.zhny.sparkstreaming.config;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataTypes;

public class SparkSessionConfig {

    private static SparkSession sparkSession;

    public static SparkSession getSparkSession(SparkConf sparkConf){

        sparkSession = SparkSession.builder()
                .config(sparkConf)
                .config("spark.default.parallelism",9)
                .config("spark.sql.shuffle.partitions",9)
                .enableHiveSupport()
                .getOrCreate();

        UDF1 to_unit = new UDF1<String,String>(){
            @Override
            public String call(String tag) throws Exception {
                switch (tag){
                    case "GEN.CurkWhRec":
                        return "kW·h";
                    case "GEN.kWh":
                        return "kW·h";
                    case "GEN.CurkWhRecFee1":
                        return "kW·h";
                    case "GEN.CurkWhRecFee2":
                        return "kW·h";
                    case "GEN.CurkWhRecFee4":
                        return "kW·h";
                    case "GEN.P":
                        return "kW";
                    default:
                        return null;
                }
            }
        };
        sparkSession.udf().register("to_unit_func",to_unit, DataTypes.StringType);

        return sparkSession;
    }
}
