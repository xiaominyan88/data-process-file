package com.hyit.zhny.sparkstreaming.connectionpool.factory;


import java.sql.Connection;

public interface ConnectionFactory {

    /*
     *@Param String version 判断取什么环境下的数据库链接
     *
     * */
    public Connection getConnection(String version);


    /**
     * 数据库链接使用完毕后返还给链接池
     * @param connection
     */
    public void returnConnectionToPool(Connection connection);
}
