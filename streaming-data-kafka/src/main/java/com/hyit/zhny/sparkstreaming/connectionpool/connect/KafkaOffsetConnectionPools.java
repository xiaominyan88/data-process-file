package com.hyit.zhny.sparkstreaming.connectionpool.connect;



import com.hyit.zhny.sparkstreaming.connectionpool.connectproperty.DataBaseProperty;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.LinkedList;

public class KafkaOffsetConnectionPools implements Serializable {

    private static LinkedList<Connection> connectionQueue;

    private KafkaOffsetConnectionPools(){

    }


    public synchronized static Connection getConnection(String version) {
        try{

            DataBaseProperty.KafkaOffSetDataBaseProperty kafkaDBProps = new DataBaseProperty().new KafkaOffSetDataBaseProperty(version);

            int initCount = kafkaDBProps.getInitCount();

            int poolMaxSize = kafkaDBProps.getPoolMaxSize();

            String jdbcDriver = kafkaDBProps.getJdbcDriver();

            String username = kafkaDBProps.getUsername();

            String password = kafkaDBProps.getPassword();

            String url = kafkaDBProps.getUrl();

            Class.forName(jdbcDriver);

            if(connectionQueue == null){

                connectionQueue = new LinkedList<>();

                for(int i = 0; i < initCount; i++){

                    Connection conn = DriverManager.getConnection(url,username,password);

                    connectionQueue.push(conn);
                }

            }

        }catch (Exception e){
            e.printStackTrace();
        }

        return connectionQueue.poll();
    }

    public static void returnConnection(Connection conn){
        connectionQueue.push(conn);
    }
}
