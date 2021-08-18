package com.hyit.zhny.sparkstreaming.connectionpool.connect;



import com.hyit.zhny.sparkstreaming.connectionpool.connectproperty.DataBaseProperty;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

public class PhoenixConnectionPool extends ConnectionPool {

    private PhoenixConnectionPool(){

    }

    private volatile static PhoenixConnectionPool instance;

    public Connection createConnection(String version) throws SQLException, ClassNotFoundException{

        DataBaseProperty.PhoenixDataBaseProperty phonixDBProps = new DataBaseProperty().new PhoenixDataBaseProperty(version);

        initCount = phonixDBProps.getInitCount();

        poolMaxSize = phonixDBProps.getPoolMaxSize();

        jdbcDriver = phonixDBProps.getJdbcDriver();

        url = phonixDBProps.getUrl();

        Class.forName(jdbcDriver);

        return DriverManager.getConnection(url);

    }


    public static PhoenixConnectionPool getInstance(){

        if(instance == null){

            synchronized (PhoenixConnectionPool.class){

                if(instance == null){

                    instance = new PhoenixConnectionPool();

                }

            }

        }

        return instance;

    }

}
