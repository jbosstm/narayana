package org.jboss.as.integration.jbossts.jopr;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXServiceURL;
import java.net.MalformedURLException;

public class TxnConstants {
    static String JMXURL = "service:jmx:rmi:///jndi/rmi://localhost:1090/jmxrmi";
    static MalformedObjectNameException error = null;

    public static final ObjectName OS_MBEAN = initON("jboss.jta:type=ObjectStore");
    public static final ObjectName STATBEAN = initON("jboss.jta:name=TransactionStatistics");
    public static final ObjectName REBEAN = initON("jboss.jta:name=RecoveryEnvironmentBean");
    public static final ObjectName CEBEAN = initON("jboss.jta:name=CoordinatorEnvironmentBean");
    public static final ObjectName JTAEBEAN = initON("jboss.jta:name=JTAEnvironmentBean");
    public static final ObjectName OSENVBEAN = initON("jboss.jta:name=ObjectStoreEnvironmentBean");
    public static final ObjectName COREEBEAN = initON("jboss.jta:name=CoreEnvironmentBean");

//	public static final ObjectName JTSEBEAN = initON("jboss.jta:name=JTSEnvironmentBean");
//	public static final ObjectName JDBCBEAN = initON("jboss.jta:name=JDBCEnvironmentBean");

    static ObjectName initON(String name) {
        try {
            return new ObjectName(name);
        } catch (MalformedObjectNameException e) {
            error = e;
            return null;
        }
    }

    static void initConstants() throws MalformedObjectNameException {
        if (error != null)
            throw error;
    }
    
    static void setJMXUrl(String url) throws MalformedURLException, MalformedObjectNameException {
        new JMXServiceURL(url);
        JMXURL = url;

        initConstants();
    }

    static String getJMXUrl() {
        return JMXURL;
    }
}
