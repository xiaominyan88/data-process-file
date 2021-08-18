package com.hyit.zhny.sparkstreaming.connectionpool.connect;



import com.hyit.zhny.sparkstreaming.connectionpool.factory.ConnectionFactory;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.LinkedList;

abstract class ConnectionPool implements ConnectionFactory, Serializable {

    protected static LinkedList<Connection> pooledConnectionList;

    protected int initCount;

    protected int poolMaxSize;

    protected String jdbcDriver;

    protected String username;

    protected String password;

    protected String url;


    public abstract Connection createConnection(String version) throws SQLException, ClassNotFoundException;



    @Override
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



    @Override
    public void returnConnectionToPool(Connection connection) {

        pooledConnectionList.push(connection);

    }


}
