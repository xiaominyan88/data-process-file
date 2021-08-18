package com.hyit.zhny.sparkstreaming.connectionpool.connectproperty;

import javax.xml.crypto.Data;

public class DataBaseProperty {

    public static final String test_kafkaoffset_driver = "com.mysql.jdbc.Driver";

    public static final String test_kafkaoffset_username = "admin";

    public static final String test_kafkaoffset_password = "]#,!aZ[nz}2w5qQm";

    public static final String test_kafkaoffset_url = "jdbc:mysql://172.30.84.136:13306/bigdata_data_storage?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT";

    public static final int test_kafkaoffset_initCount = 10;

    public static final int test_kafkaoffset_poolMaxSize = 15;

    public static final String prod_kafkaoffset_driver = "com.mysql.jdbc.Driver";

    public static final String prod_kafkaoffset_username = "data_overview_prod";

    public static final String prod_kafkaoffset_password = "_ObL8.1hRUW{fN$g";

    public static final String prod_kafkaoffset_url = "jdbc:mysql://172.30.92.25:13306/bigdata_data_storage?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=GMT";

    public static final int prod_kafkaoffset_initCount = 10;

    public static final int prod_kafkaoffset_poolMaxSize = 15;

    public static final String test_phoenix_driver = "org.apache.phoenix.jdbc.PhoenixDriver";

    public static final String test_phoenix_url = "jdbc:phoenix:172.30.83.214,172.30.83.215,172.30.83.216:2181";

    public static final int test_phoenix_initCount = 3;

    public static final int test_phoenix_poolMaxSize = 5;

    public static final String prod_phoenix_driver = "org.apache.phoenix.jdbc.PhoenixDriver";

    public static final String prod_phoenix_url = "jdbc:phoenix:172.30.92.235,172.30.92.236,172.30.92.237:2181";

    public static final int prod_phoenix_initCount = 3;

    public static final int prod_phoenix_poolMaxSize = 5;




    public class KafkaOffSetDataBaseProperty{

        private String jdbcDriver;

        private String username;

        private String password;

        private String url;

        private int initCount;

        private int poolMaxSize;

        public KafkaOffSetDataBaseProperty(String driver,String url,String username,String password,int initCount,int poolMaxSize){
            this.jdbcDriver = driver;
            this.url = url;
            this.username = username;
            this.password = password;
            this.initCount = initCount;
            this.poolMaxSize = poolMaxSize;
        }

        public KafkaOffSetDataBaseProperty(String version){

                if("prod".equalsIgnoreCase(version)){
                    this.jdbcDriver = DataBaseProperty.prod_kafkaoffset_driver;
                    this.url = DataBaseProperty.prod_kafkaoffset_url;
                    this.username = DataBaseProperty.prod_kafkaoffset_username;
                    this.password = DataBaseProperty.prod_kafkaoffset_password;
                    this.initCount = DataBaseProperty.prod_kafkaoffset_initCount;
                    this.poolMaxSize = DataBaseProperty.prod_kafkaoffset_poolMaxSize;
                }else{
                    this.jdbcDriver = DataBaseProperty.test_kafkaoffset_driver;
                    this.url = DataBaseProperty.test_kafkaoffset_url;
                    this.username = DataBaseProperty.test_kafkaoffset_username;
                    this.password = DataBaseProperty.test_kafkaoffset_password;
                    this.initCount = DataBaseProperty.test_kafkaoffset_initCount;
                    this.poolMaxSize = DataBaseProperty.test_kafkaoffset_poolMaxSize;

                }

        }


        public String getJdbcDriver(){
            return jdbcDriver;
        }

        public String getUsername(){
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getUrl() {
            return url;
        }

        public int getInitCount() {
            return initCount;
        }

        public int getPoolMaxSize() {
            return poolMaxSize;
        }




    }

    public class PhoenixDataBaseProperty{

        private String jdbcDriver;

        private String username;

        private String password;

        private String url;

        private int initCount;

        private int poolMaxSize;

        public PhoenixDataBaseProperty(String driver,String url,int initCount,int poolMaxSize){
            this.jdbcDriver = driver;

            this.url = url;

            this.username = null;

            this.password = null;

            this.initCount = initCount;

            this.poolMaxSize = poolMaxSize;
        }

        public PhoenixDataBaseProperty(String version){

            if("prod".equalsIgnoreCase(version)){

                this.jdbcDriver = DataBaseProperty.prod_phoenix_driver;

                this.url = DataBaseProperty.prod_phoenix_url;

                this.username = null;

                this.password = null;

                this.initCount = DataBaseProperty.prod_phoenix_initCount;

                this.poolMaxSize = DataBaseProperty.prod_phoenix_poolMaxSize;

            }else {

                this.jdbcDriver = DataBaseProperty.test_phoenix_driver;

                this.url = DataBaseProperty.test_phoenix_url;

                this.username = null;

                this.password = null;

                this.initCount = DataBaseProperty.test_phoenix_initCount;

                this.poolMaxSize = DataBaseProperty.test_phoenix_poolMaxSize;

            }

        }

        public String getJdbcDriver(){
            return jdbcDriver;
        }

        public String getUsername(){
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getUrl() {
            return url;
        }

        public int getInitCount() {
            return initCount;
        }

        public int getPoolMaxSize() {
            return poolMaxSize;
        }

    }
}
