package com.hyit.zhny.cloud.config;

import com.hyit.zhny.cloud.util.CopyFile;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.Properties;

public class KafkaConfig {

    public enum Module{

        KAFKA("KafkaClient"),
        ZOOKEEPER("Client");

        private String name;

        private Module(String name){
            this.name = name;
        }

        private String getName(){
            return name;
        }
    }

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final String JAAS_POSTFIX = ".jaas.conf";

    private static final boolean IS_IBM_JDK = System.getProperty("java.vendor").contains("IBM");

    private static final String IBM_LOGIN_MODULE = "com.ibm.security.auth.module.Krb5LoginModule required";

    private static final String SUN_LOGIN_MODULE = "com.sun.security.auth.module.Krb5LoginModule required";

    public static final String ZOOKEEPER_AUTH_PRINCIPAL = "zookeeper.server.principal";

    public static final String JAVA_SECURITY_KRB5_CONF = "java.security.krb5.conf";

    public static final String JAVA_SECURITY_LOGIN_CONF = "java.securtiy.auth.login.config";

    public static void setConfig() throws IOException{

        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();

        yaml.setResources(new ClassPathResource("application.yml"));

        Properties properties = yaml.getObject();

        String version = properties.getProperty("kerberos.version");

        String bootstrap_servers_ip = properties.getProperty("kafka.bootstrap.servers");

        String topic = properties.getProperty("kafka.topic");

        String jassPath = System.getProperty("user.dir") + "/" + System.getProperty("username") + JAAS_POSTFIX;

        String parentPath = System.getProperty("user.dir").replace("\\","\\\\");

        String path = getClassPath();

        if(version.equalsIgnoreCase("prod")){
            CopyFile.copyFiles("eidp_kafka.keytab", parentPath + "/eidp_kafka.keytab");
            writeFile("eidp_kafka@HDE.H3C.COM",parentPath+"/eidp_kafka.keytab",jassPath);
        }else{
            CopyFile.copyFiles("kafka.keytab",parentPath+"/kafka.keytab");
            writeFile("kafka@HDE.H3C.COM",parentPath+"/kafka.keytab",jassPath);
        }
        System.setProperty("java.security.auth.login.config", jassPath);
        CopyFile.copyFiles("krb5.conf",parentPath+"/krb5.conf");
        System.setProperty("java.security.krb5.conf",parentPath+"/krb5.conf");
        System.setProperty("bootstrap.servers",bootstrap_servers_ip);
        System.setProperty("topic",topic);
    }

    public static String getClassPath(){
        String path = new Object(){
            public String getPath(){
                return this.getClass().getResource("/").getPath();
            }
        }.getPath();
        return path;
    }

    public static void writeFile(String principal, String keytabPath, String jaasPath) throws IOException{

        jaasPath = jaasPath.replace("\\","\\\\");

        deleteJaasFile(jaasPath);

        writeJaasFile(jaasPath,principal,keytabPath);

    }

    private static void writeJaasFile(String jaasPath, String principal, String keytabPath) throws IOException{

        FileWriter writer = new FileWriter(new File(jaasPath));

        try{
            writer.write(getJaasConfContext(principal,keytabPath));
        }catch (IOException e){
            e.printStackTrace();
        }finally {
            writer.close();
        }
    }

    private static void deleteJaasFile(String jaasPath) throws IOException{

        File jaasFile = new File(jaasPath);

        if(jaasFile.exists()){

            if(!jaasFile.delete()) throw new FileAlreadyExistsException(jaasFile.getName());

        }
    }

    private static String getJaasConfContext(String principal, String keytabPath){

        Module[] allModule = Module.values();

        StringBuilder builder = new StringBuilder();

        for(Module module : allModule){

            builder.append(writeKafkaJaas(principal,keytabPath,module));

        }
        return builder.toString();
    }

    public static String writeKafkaJaas(String userPrincipal, String keyTabPath, Module module){

        StringBuilder builder = new StringBuilder();

        if(IS_IBM_JDK){

            builder.append(module.getName()).append(" {").append(LINE_SEPARATOR);

            builder.append(IBM_LOGIN_MODULE).append(LINE_SEPARATOR);

            builder.append("credsType=both").append(LINE_SEPARATOR);

            builder.append("principal=\"" + userPrincipal + "\"").append(LINE_SEPARATOR);

            builder.append("useKeytab=\"" + keyTabPath + "\"").append(LINE_SEPARATOR);

            builder.append("debug=true;").append(LINE_SEPARATOR);

            builder.append("};").append(LINE_SEPARATOR);

        }else{

            if(module.getName().contains("KafkaClient")) {

                builder.append(module.getName()).append(" {").append(LINE_SEPARATOR);

                builder.append(SUN_LOGIN_MODULE).append(LINE_SEPARATOR);

                builder.append("useKeyTab=true").append(LINE_SEPARATOR);

                builder.append("keyTab=\"" + keyTabPath + "\"").append(LINE_SEPARATOR);

                builder.append("principal=\"" + userPrincipal + "\"").append(LINE_SEPARATOR);

                builder.append("useTicketCache=true").append(LINE_SEPARATOR);

                builder.append("storeKey=true").append(LINE_SEPARATOR);

                builder.append("doNotPrompt=true").append(LINE_SEPARATOR);

                builder.append("serviceName=\"kafka\"").append(LINE_SEPARATOR);

                builder.append("client=true").append(LINE_SEPARATOR);

                builder.append("debug=true;").append(LINE_SEPARATOR);

                builder.append("};").append(LINE_SEPARATOR);

            }else if(module.getName().equalsIgnoreCase("Client")){

                builder.append(module.getName()).append(" {").append(LINE_SEPARATOR);

                builder.append(SUN_LOGIN_MODULE).append(LINE_SEPARATOR);

                builder.append("renewTicket=true").append(LINE_SEPARATOR);

                builder.append("useTicketCache=true").append(LINE_SEPARATOR);

                builder.append("serviceName=\"zookeeper\"").append(LINE_SEPARATOR);

                builder.append("debug=true;").append(LINE_SEPARATOR);

                builder.append("};").append(LINE_SEPARATOR);

            }
        }
        return builder.toString();
    }
}
