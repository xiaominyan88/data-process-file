package com.hyit.zhny.sparkstreaming.connectionpool.connect;



import com.hyit.zhny.sparkstreaming.connectionpool.connectproperty.DataBaseProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

public class KafkaOffsetConnectionPool extends ConnectionPool {

    private KafkaOffsetConnectionPool(){

    }

    private volatile static KafkaOffsetConnectionPool instance;

    public Connection createConnection(String version) throws SQLException,ClassNotFoundException{

        DataBaseProperty.KafkaOffSetDataBaseProperty kafkaDBProps = new DataBaseProperty().new KafkaOffSetDataBaseProperty(version);

        initCount = kafkaDBProps.getInitCount();

        poolMaxSize = kafkaDBProps.getPoolMaxSize();

        jdbcDriver = kafkaDBProps.getJdbcDriver();

        username = kafkaDBProps.getUsername();

        password = kafkaDBProps.getPassword();

        url = kafkaDBProps.getUrl();

        Class.forName(jdbcDriver);

        return DriverManager.getConnection(url,username,password);

    }

    public synchronized Connection getConnection(String version){

        try{

            if(pooledConnectionList == null) {

                pooledConnectionList = new LinkedList<>();

                for (int i = 0; i < initCount; i++) {

                    System.out.println("第" + i + "个链接已经创建");

                    Connection conn = createConnection(version);

                    System.out.println("链接为："+conn);

                    System.out.println("链接是否可用：" + conn.isValid(3000));

                    pooledConnectionList.push(conn);


                }

            }

        }catch(Exception e){

            e.printStackTrace();

        }

        return pooledConnectionList.poll();

    }

    public static KafkaOffsetConnectionPool getInstance(){

        if(instance == null){

            synchronized (KafkaOffsetConnectionPool.class){

                if(instance == null){

                    instance = new KafkaOffsetConnectionPool();

                }

            }

        }

        return instance;

    }



}
