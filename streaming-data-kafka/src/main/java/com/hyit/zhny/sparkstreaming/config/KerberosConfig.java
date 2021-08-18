package com.hyit.zhny.sparkstreaming.config;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.security.UserGroupInformation;

public class KerberosConfig {

    private static final String zookeeperUrl = "node3,node4,node5,node10,node11";

    private static final String zookeeperUrlTest = "node3,node4,node5,node6,node7";

    public static void getkerberosConfiguration(String version){

        Configuration conf = HBaseConfiguration.create();
        if("prod".equals(version)){
            conf.set("hbase.zookeeper.quorum",zookeeperUrl);
        }else{
            conf.set("hbase.zookeeper.quorum",zookeeperUrlTest);
        }
        conf.set("hbase.zookeeper.property.clientPort","2181");
        conf.set("hadoop.security.authentication", "Kerberos");
        conf.addResource( "/etc/hadoop/2.3.4.0-3485/0/hdfs-site.xml");
        System.setProperty("java.security.krb5.conf", "/etc/krb5.conf");
        UserGroupInformation.setConfiguration(conf);
        try{
            if ("prod".equals(version)) {
                UserGroupInformation.loginUserFromKeytab("eidp_shell@HDE.H3C.COM",  "/etc/security/keytabs/eidp_shell.keytab");
            } else {
                UserGroupInformation.loginUserFromKeytab("hbase@HDE.H3C.COM", "/etc/security/keytabs/hbase.keytab");
            }
        }catch(Exception e){
            e.printStackTrace();
        }

    }
}
