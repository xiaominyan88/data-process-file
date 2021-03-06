package com.hyit.zhny.sparkstreaming.utils;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.security.UserGroupInformation;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

public class PhoenixUtils implements Serializable {


    private static LinkedList<Connection> connectionQueue;


    public synchronized static Connection getConnection(String version,String zkUrl) throws SQLException {
        try {
            if (connectionQueue == null){
                connectionQueue = new LinkedList<Connection>();
                for (int i = 0;i < 3;i++){
                    Configuration conf = new Configuration();
                    conf.set("hadoop.security.authentication", "Kerberos");
                    //conf.addResource(path + "/pro/core-site.xml");
                    // conf.addResource(path+"/replaceChangePro/hbase-site.xml");
                    conf.addResource(new Path("habse-site.xml"));
                    conf.addResource(new Path("hdfs-site.xml"));
                    conf.addResource( "/etc/hadoop/2.3.4.0-3485/0/hdfs-site.xml");
                    System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
                    Connection conn = null;
                    UserGroupInformation.setConfiguration(conf);
                    if ("prod".equals(version)) {
                        UserGroupInformation.loginUserFromKeytab("eidp_shell@HDE.H3C.COM", "/etc/security/keytabs/eidp_shell.keytab");
                    } else {
                        UserGroupInformation.loginUserFromKeytab("hbase@HDE.H3C.COM", "/etc/security/keytabs/hbase.keytab");
                    }
                    System.out.println("?????????????????????");
                    conn = DriverManager.getConnection(zkUrl);
                    System.out.println("?????????????????????,????????????????????????????????????" + connectionQueue.size());

                    connectionQueue.push(conn);
                }
            }
        }catch (Exception e1){
            e1.printStackTrace();
        }
        return connectionQueue.poll();
    }

    public static void returnConnection(Connection conn){
        connectionQueue.push(conn);
    }


    /**
     * ???????????????????????????phoenix??????
     * @param version ??????????????????????????????
     * @param zkUrl zookeeper????????????
     * @param sparkSession sparkSession????????????
     * @param dataSet ??????hive??????????????????????????????Dataset
     * @param stationAndTableIndex ??????????????????tableIndex???stationId??????????????????RDD,key???index,value????????????Iterable??????
     * @param desTable ??????phoenix???????????????
     */
    public static void insertPhoenixTable(SparkSession sparkSession, Dataset<Row> dataSet, String version, JavaPairRDD<String,Iterable<String>> stationAndTableIndex, String zkUrl, String desTable){

        dataSet.createOrReplaceTempView("temp_data_table");

        try{
            if("NEW_DW_15MIN_TJL".equals(desTable)){
                List<String> list = stationAndTableIndex.keys().collect();
                for(int i = 0; i < list.size(); i++){
                    String table = desTable + "_" + list.get(i);
                    JavaRDD<Row> rows = sparkSession.sql("select tRow,measurepointid,sampletime,measuretag,value1,unit from temp_data_table where index = '" + list.get(i) + "'").javaRDD();
                    List<Row> list1 = rows.collect();
                    if(list1.size() > 0){
                        Connection conn = PhoenixUtils.getConnection(version,zkUrl);
                        conn.setAutoCommit(false);
                        PreparedStatement prst = conn.prepareStatement("UPSERT INTO " + table  + " (\"ROW\",MEASUREPOINTID,SAMPLETIME,MEASURETAG,VALUE1,UNIT) VALUES (?,?,?,?,?,?)");
                        int m = 0;
                        for(int j = 0; j < list1.size(); j++){
                            if(list1.get(j).getString(0) != null && list1.get(j).getString(1) != null && list1.get(j).getString(2) != null && list1.get(j).getString(3) != null
                             && list1.get(j).getDecimal(4) != null && list1.get(j).getString(5) != null){
                                prst.setString(1,list1.get(j).getString(0));
                                prst.setString(2,list1.get(j).getString(1));
                                prst.setString(3,list1.get(j).getString(2));
                                prst.setString(4,list1.get(j).getString(3));
                                prst.setBigDecimal(5,list1.get(j).getDecimal(4));
                                prst.setString(6,list1.get(j).getString(5));
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
                    }else{
                        continue;
                    }
                }
            }else{
                JavaRDD<Row> rows = sparkSession.sql("select tRow,measurepointid,sampletime,measuretag,value1,unit from temp_data_table").javaRDD();
                Connection conn = PhoenixUtils.getConnection(version,zkUrl);
                conn.setAutoCommit(false);
                PreparedStatement prst = conn.prepareStatement("UPSERT INTO " + desTable  + " (\"ROW\",MEASUREPOINTID,SAMPLETIME,MEASURETAG,VALUE1,UNIT) VALUES (?,?,?,?,?,?)");
                List<Row> list1 = rows.collect();
                int m = 0;
                for(int j = 0; j < list1.size(); j++){
                    prst.setString(1,list1.get(j).getString(0));
                    prst.setString(2,list1.get(j).getString(1));
                    prst.setString(3,list1.get(j).getString(2));
                    prst.setString(4,list1.get(j).getString(3));
                    prst.setBigDecimal(5,list1.get(j).getDecimal(4));
                    prst.setString(6,list1.get(j).getString(5));
                    prst.execute();
                    m++;
                    if(m%1000 == 0){
                        conn.commit();
                    }
                }
                conn.commit();
                prst.close();
                PhoenixUtils.returnConnection(conn);
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }


    /**
     * ???????????????????????????phoenix????????????
     * @param version ??????????????????????????????
     * @param zkUrl zookeeper????????????
     * @param sparkSession sparkSession????????????
     * @param dataSet ??????hive??????????????????????????????Dataset
     * @param desTable ??????phoenix???????????????
     */
    public static void insertPhoenixLJTable(SparkSession sparkSession, Dataset<Row> dataSet, String version, String zkUrl, String desTable){

        dataSet.createOrReplaceTempView("temp_data_table");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        try{
            JavaRDD<Row> rows = sparkSession.sql("select row,measurepointid,sampletime,measuretag,value1,unit from temp_data_table").javaRDD();
            Connection conn = PhoenixUtils.getConnection(version,zkUrl);
            conn.setAutoCommit(false);
            PreparedStatement prst = conn.prepareStatement("UPSERT INTO " + desTable  + " (\"ROW\",MEASUREPOINTID,SAMPLETIME,MEASURETAG,VALUE1,UNIT) VALUES (?,?,?,?,?,?)");
            List<Row> list1 = rows.collect();
            int m = 0;
            for(int j = 0; j < list1.size(); j++){
                if(list1.get(j).getString(0) != null && list1.get(j).getString(1) != null && sdf.format(list1.get(j).getTimestamp(2)) != null && list1.get(j).getString(3) != null
                        && list1.get(j).getDecimal(4) != null && list1.get(j).getString(5) != null){
                    prst.setString(1,list1.get(j).getString(0));
                    prst.setString(2,list1.get(j).getString(1));
                    prst.setString(3,sdf.format(new Date(list1.get(j).getTimestamp(2).getTime())));
                    prst.setString(4,list1.get(j).getString(3));
                    prst.setBigDecimal(5,list1.get(j).getDecimal(4));
                    prst.setString(6,list1.get(j).getString(5));
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
        }catch(Exception e){
            e.printStackTrace();
        }

    }


}
