/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */

package org.jboss.narayana.blacktie.administration.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.administration.BlacktieAdministration;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.XMLParser;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

/**
 * This is the core proxy to forward requests to the individual servers.
 */
public class AdministrationProxy implements BlacktieAdministration {
    private static final Logger log = LogManager.getLogger(AdministrationProxy.class);
    private Properties prop = new Properties();
    private MBeanServerConnection beanServerConnection;
    private Connection connection;

    public static Boolean isDomainPause = false;

    public AdministrationProxy() throws ConfigurationException {
        log.debug("Administration Proxy");
                
        beanServerConnection = java.lang.management.ManagementFactory.getPlatformMBeanServer();
        log.debug("Created Administration Proxy");
    }
    
    public void onConstruct() throws ConfigurationException {
        log.info("onConstruct load btconfig.xml");
        XMLParser.loadProperties("btconfig.xsd", "btconfig.xml", prop);
        ConnectionFactory cf = ConnectionFactory.getConnectionFactory();
        connection = cf.getConnection();
    }

    private Response callAdminService(String serverName, int id, String command) throws ConnectionException,
            ConfigurationException {
        log.trace("callAdminService");
        command = command + "\0";
        X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray(command.getBytes());

        String service = "." + serverName + id;

        Response rcvbuf = connection.tpcall(service, sendbuf, 0);
        return rcvbuf;
    }

    private Boolean callAdminCommand(String serverName, int id, String command) {
        log.trace("callAdminCommand");
        try {
            Response buf = callAdminService(serverName, id, command);
            if (buf != null) {
                byte[] received = ((X_OCTET) buf.getBuffer()).getByteArray();
                return (received[0] == '1');
            }
        } catch (ConnectionException e) {
            log.error("call server " + serverName + " id " + id + " command " + command + " failed with " + e.getTperrno());
        } catch (ConfigurationException e) {
            log.error("call server " + serverName + " id " + id + " command " + command
                    + " failed with configuration exception");
        }
        return false;
    }

    private Boolean advertise(String serverName, int id, String serviceName) {
        log.trace("advertise");
        String command = "advertise," + serviceName + ",";
        return callAdminCommand(serverName, id, command);
    }

    private Boolean unadvertise(String serverName, int id, String serviceName) {
        log.trace("unadvertise");
        String command = "unadvertise," + serviceName + ",";
        return callAdminCommand(serverName, id, command);
    }

    public String getDomainName() {
        log.trace("getDomainName");
        return prop.getProperty("blacktie.domain.name");
    }

    public String getSoftwareVersion() {
        log.trace("getSoftwareVersion");
        return prop.getProperty("blacktie.domain.version");
    }

    public Boolean getDomainStatus() {
        return isDomainPause;
    }

    public Boolean pauseDomain() {
        log.trace("pauseDomain");
        Boolean result = true;
        List<String> servers = listRunningServers();

        for (int i = 0; i < servers.size(); i++) {
            result = pauseServer(servers.get(i)) && result;
        }

        if (result == true && isDomainPause == false) {
            isDomainPause = true;
            log.info("Domain pause");
        }

        return result;
    }

    public Boolean pauseServer(String serverName) {
        log.trace("pauseServer");
        Boolean result = true;
        List<Integer> ids = listRunningInstanceIds(serverName);

        for (int i = 0; i < ids.size(); i++) {
            result = pauseServerById(serverName, ids.get(i)) && result;
        }
        return result;
    }

    public Boolean pauseServerById(String serverName, int id) {
        log.trace("pauseServerById");
        return callAdminCommand(serverName, id, "pause");
    }

    public Boolean resumeDomain() {
        log.trace("resumeDomain");
        Boolean result = true;
        List<String> servers = listRunningServers();

        for (int i = 0; i < servers.size(); i++) {
            result = resumeServer(servers.get(i)) && result;
        }

        if (result == true && isDomainPause == true) {
            isDomainPause = false;
            log.info("Domain resume");
        }

        return result;
    }

    public Boolean resumeServer(String serverName) {
        log.trace("resumeServer");
        Boolean result = true;
        List<Integer> ids = listRunningInstanceIds(serverName);

        for (int i = 0; i < ids.size(); i++) {
            result = resumeServerById(serverName, ids.get(i)) && result;
        }
        return result;
    }

    public Boolean resumeServerById(String serverName, int id) {
        log.trace("resumeServerById");
        return callAdminCommand(serverName, id, "resume");
    }

    @SuppressWarnings("unchecked")
    public List<String> listRunningServers() {
        log.trace("listRunningServers");
        List<String> runningServerList = new ArrayList<String>();

        try {
            ObjectName objName = new ObjectName("jboss.as:subsystem=messaging,hornetq-server=default,jms-queue=BTR_*");
            ObjectInstance[] dests = beanServerConnection.queryMBeans(objName, null).toArray(new ObjectInstance[] {});
            for (int i = 0; i < dests.length; i++) {
                String serviceComponentOfObjectName = dests[i].getObjectName().getCanonicalName();
                serviceComponentOfObjectName = serviceComponentOfObjectName.substring(
                        serviceComponentOfObjectName.indexOf('_') + 1,
                        serviceComponentOfObjectName.indexOf(",", serviceComponentOfObjectName.indexOf('_')));
                log.debug("Service name component of ObjectName is: " + serviceComponentOfObjectName);
                if (serviceComponentOfObjectName.startsWith(".")) {
                    serviceComponentOfObjectName = serviceComponentOfObjectName.substring(1);
                    serviceComponentOfObjectName = serviceComponentOfObjectName.replaceAll("[0-9]", "");
                    log.trace("contains?: " + serviceComponentOfObjectName);
                    if (!runningServerList.contains(serviceComponentOfObjectName)) {
                        log.trace("contains!: " + serviceComponentOfObjectName);
                        runningServerList.add(serviceComponentOfObjectName);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Caught an exception: " + e.getMessage(), e);
        }
        return runningServerList;
    }

    @SuppressWarnings("unchecked")
    public List<Integer> listRunningInstanceIds(String serverName) {
        log.debug("listRunningInstanceIds: " + serverName);
        ArrayList<Integer> ids = new ArrayList<Integer>();

        try {
            ObjectName objName = new ObjectName("jboss.as:subsystem=messaging,hornetq-server=default,jms-queue=BTR_*");
            ObjectInstance[] dests = beanServerConnection.queryMBeans(objName, null).toArray(new ObjectInstance[] {});
            for (int i = 0; i < dests.length; i++) {
                String serviceComponentOfObjectName = dests[i].getObjectName().getCanonicalName();
                serviceComponentOfObjectName = serviceComponentOfObjectName.substring(
                        serviceComponentOfObjectName.indexOf('_') + 1,
                        serviceComponentOfObjectName.indexOf(",", serviceComponentOfObjectName.indexOf('_')));
                log.debug("Service name component of ObjectName is: " + serviceComponentOfObjectName);

                if (serviceComponentOfObjectName.startsWith(".")) {
                    serviceComponentOfObjectName = serviceComponentOfObjectName.substring(1);
                    String sname = serviceComponentOfObjectName.replaceAll("[0-9]", "");
                    if (sname.equals(serverName)) {
                        String id = serviceComponentOfObjectName.replaceAll("[A-Za-z]", "");
                        log.debug(id);
                        ids.add(new Integer(id));
                    }
                }
            }
        } catch (Exception e) {
            log.error("Caught an exception: " + e.getMessage(), e);
        }

        return ids;
    }

    public String listServersStatus() {
        log.trace("listServersStatus");

        String status = "<servers>\n";

        List<String> servers = listRunningServers();

        for (String server : servers) {
            status += "\t<server>\n";
            status += "\t\t<name>" + server + "</name>\n";
            List<Integer> ids = listRunningInstanceIds(server);
            if (ids.size() > 0) {
                status += "\t\t<instances>\n";
                for (int i = 0; i < ids.size(); i++) {
                    status += "\t\t\t<instance>\n";
                    status += "\t\t\t\t<id>" + ids.get(i) + "</id>\n";
                    status += "\t\t\t\t<status>1</status>\n";
                    status += "\t\t\t</instance>\n";
                }
                status += "\t\t</instances>\n";
            }
            status += "\t</server>\n";
        }

        status += "</servers>";
        return status;
    }

    public String listServiceStatus(String serverName, String serviceName) {
        log.trace("listServiceStatus");
        String servers;
        List<Integer> ids = listRunningInstanceIds(serverName);

        servers = "<servers>";
        for (int i = 0; i < ids.size(); i++) {
            String result = listServiceStatusById(serverName, ids.get(i), serviceName);
            if (result != null) {
                servers += "<instance><id>" + ids.get(i) + "</id>";
                servers += result;
                servers += "</instance>";
            }
        }
        servers += "</servers>";
        return servers;
    }

    public Boolean advertise(String serverName, String serviceName) {
        log.trace("advertise");
        List<Integer> ids = listRunningInstanceIds(serverName);
        Boolean result = true;

        if (ids.size() == 0) {
            log.warn("Server was not running: " + serverName);
            return false;
        }

        for (int i = 0; i < ids.size(); i++) {
            boolean advertised = advertise(serverName, ids.get(i), serviceName);
            result = advertised && result;
            if (!advertised) {
                log.warn("Failed to advertise service: " + serviceName + " at: " + serverName + ids.get(i));
            }
        }

        return result;
    }

    public Boolean unadvertise(String serverName, String serviceName) {
        log.trace("unadvertise");
        List<Integer> ids = listRunningInstanceIds(serverName);
        Boolean result = true;

        if (ids.size() == 0) {
            log.warn("Server was not running: " + serverName);
            return false;
        }

        for (int i = 0; i < ids.size(); i++) {
            boolean unadvertised = unadvertise(serverName, ids.get(i), serviceName);
            result = unadvertised && result;
            if (!unadvertised) {
                log.warn("Failed to unadvertise service: " + serviceName + " at: " + serverName + ids.get(i));
            }
        }

        return result;
    }

    public Boolean shutdown(String serverName, int id) {
        log.trace("shutdown");
        List<String> servers = listRunningServers(); 
        if (servers.contains(serverName)) {
            String command = "serverdone";
            boolean shutdown = false;
            try {
                if (id == 0) {
                    List<Integer> ids = listRunningInstanceIds(serverName);
                    ConnectionException toRethrow = null;
                    for (int i = 0; i < ids.size(); i++) {
                        try {
                            callAdminService(serverName, ids.get(i), command);
                        } catch (ConnectionException e) {
                            log.error("call server " + serverName + " id " + id + " failed with " + e.getTperrno(), e);
                            if (e.getTperrno() == org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection.TPETIME) {
                                callAdminService(serverName, ids.get(i), command);
                            } else {
                                toRethrow = e;
                            }
                        } catch (ConfigurationException e) {
                            log.error("call server " + serverName + " id " + id + " failed with configuration exception", e);
                            toRethrow = new ConnectionException(Connection.TPEOS, "Configuration issue: " + e.getMessage(), e);
                        }
                    }
                    if (toRethrow != null) {
                        throw toRethrow;
                    }
                } else {
                    callAdminService(serverName, id, command);
                }
                int timeout = 40;
                while (true) {
                    List<Integer> ids = listRunningInstanceIds(serverName);
                    if (id == 0 && ids.size() > 0 || ids.contains(id)) {
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        timeout--;
                    } else {
                        shutdown = true;
                        break;
                    }
                    if (timeout == 0) {
                        log.warn("Server did not shutdown in time: " + serverName + ": " + id);
                        break;
                    }
                }
                return shutdown;
            } catch (ConnectionException e) {
                log.error("call server " + serverName + " id " + id + " failed with " + e.getTperrno(), e);
                return false;
            } catch (RuntimeException e) {
                log.error("Could not shutdown server: " + e.getMessage(), e);
                throw e;
            } catch (ConfigurationException e) {
                log.error("call server " + serverName + " id " + id + " failed with configuration exception", e);
                return false;
            }
        } else {
            log.error("Server not configured: " + serverName);
            return false;
        }
    }

    public String getResponseTimeById(String serverName, int id, String serviceName) {
        log.trace("getResponseTimeById");
        String command = "responsetime," + serviceName + ",";
        log.trace("response command is " + command);

        try {
            Response buf = callAdminService(serverName, id, command);
            if (buf != null) {
                byte[] received = ((X_OCTET) buf.getBuffer()).getByteArray();
                String result = new String(received, 1, received.length - 1);
                log.trace("response result is " + result);
                return result;
            }
        } catch (ConnectionException e) {
            log.error("call server " + serverName + " id " + id + " failed with " + e.getTperrno(), e);
        } catch (RuntimeException e) {
            log.error("Could not get response time from server: " + e.getMessage(), e);
            throw e;
        } catch (ConfigurationException e) {
            log.error("call server " + serverName + " id " + id + " failed with configuration exception", e);
        }
        return null;
    }

    public String getResponseTime(String serverName, String serviceName) {
        log.trace("getResponseTime");

        List<Integer> ids = listRunningInstanceIds(serverName);
        String responseTime;
        long min = 0;
        long avg = 0;
        long max = 0;
        long total = 0;

        for (int i = 0; i < ids.size(); i++) {
            responseTime = getResponseTimeById(serverName, ids.get(i), serviceName);
            String[] times = responseTime.split(",");

            if (times.length == 3) {
                long t = Long.valueOf(times[0]);
                if (min == 0 || t < min) {
                    min = t;
                }

                t = Long.valueOf(times[2]);
                if (t > max) {
                    max = t;
                }

                long counter = getServiceCounterById(serverName, ids.get(i), serviceName);
                t = Long.valueOf(times[1]);
                if (total != 0 || counter != 0) {
                    avg = (avg * total + t * counter) / (total + counter);
                }
            }
        }

        return String.format("%d,%d,%d", min, avg, max);
    }

    public long getServiceCounterById(String serverName, int id, String serviceName) {
        log.trace("getServiceCounterById");
        long counter = 0;
        String command = "counter," + serviceName + ",";

        try {
            Response buf = callAdminService(serverName, id, command);
            if (buf != null) {
                byte[] received = ((X_OCTET) buf.getBuffer()).getByteArray();
                counter = Long.parseLong(new String(received, 1, received.length - 1));
            }
        } catch (ConnectionException e) {
            log.error("call server " + serverName + " id " + id + " failed with " + e.getTperrno());
        } catch (ConfigurationException e) {
            log.error("call server " + serverName + " id " + id + " failed with configuration exception", e);
        }

        return counter;
    }

    public long getServiceCounter(String serverName, String serviceName) {
        log.trace("getServiceCounter");
        long counter = 0;
        List<Integer> ids = listRunningInstanceIds(serverName);

        for (int i = 0; i < ids.size(); i++) {
            counter += getServiceCounterById(serverName, ids.get(i), serviceName);
        }

        return counter;
    }

    public long getErrorCounterById(String serverName, int id, String serviceName) {
        log.trace("getErrorCounterById");
        long counter = 0;
        String command = "error_counter," + serviceName + ",";

        try {
            Response buf = callAdminService(serverName, id, command);
            if (buf != null) {
                byte[] received = ((X_OCTET) buf.getBuffer()).getByteArray();
                counter = Long.parseLong(new String(received, 1, received.length - 1));
            }
        } catch (ConnectionException e) {
            log.error("call server " + serverName + " id " + id + " failed with " + e.getTperrno());
        } catch (ConfigurationException e) {
            log.error("call server " + serverName + " id " + id + " failed with configuration exception", e);
        }

        return counter;
    }

    public long getErrorCounter(String serverName, String serviceName) {
        log.trace("getErrorCounter");
        long counter = 0;
        List<Integer> ids = listRunningInstanceIds(serverName);

        for (int i = 0; i < ids.size(); i++) {
            counter += getErrorCounterById(serverName, ids.get(i), serviceName);
        }

        return counter;
    }

    public Boolean reloadDomain() {
        log.trace("reloadDomain");
        Boolean result = true;
        List<String> servers = listRunningServers();

        for (int i = 0; i < servers.size(); i++) {
            result = reloadServer(servers.get(i)) && result;
        }
        return result;
    }

    public Boolean reloadServer(String serverName) {
        log.trace("reloadServer");
        Boolean result = true;
        List<Integer> ids = listRunningInstanceIds(serverName);

        for (int i = 0; i < ids.size(); i++) {
            result = reloadServerById(serverName, ids.get(i)) && result;
        }
        return result;
    }

    public Boolean reloadServerById(String serverName, int id) {
        log.trace("reloadServerById");
        return false;

    }

    public String listServiceStatusById(String serverName, int id, String serviceName) {
        log.trace("listServiceStatusById");
        String command = "status";
        Response buf = null;
        String status = null;

        try {
            if (serviceName != null) {
                command = command + "," + serviceName + ",";
            }

            buf = callAdminService(serverName, id, command);
            if (buf != null) {
                byte[] received = ((X_OCTET) buf.getBuffer()).getByteArray();
                if (received[0] == '1') {
                    status = new String(received, 1, received.length - 1);
                    log.debug("status is " + status);
                    return status;
                }
            }
        } catch (ConnectionException e) {
            log.error("call server " + serverName + " id " + id + " failed with " + e.getTperrno());
        } catch (Exception e) {
            log.error("response " + status + " error with " + e);
        }
        return null;
    }

    public void close() throws ConnectionException, IOException {
        log.debug("Closed Administration Proxy");
        connection.close();
        // c.close();
    }

    public int getQueueDepth(String serverName, String serviceName) {
        Integer depth;
        try {
            log.trace(serviceName);
            boolean conversational = false;
            String type = "queue";
            if (!serviceName.startsWith(".")) {
                conversational = (Boolean) prop.get("blacktie." + serviceName + ".conversational");
                type = (String) prop.getProperty("blacktie." + serviceName + ".type");
            }
            String prefix = null;
            if (conversational) {
                prefix = "BTC_";
            } else {
                prefix = "BTR_";
            }
            ObjectName objName = new ObjectName("jboss.as:subsystem=messaging,hornetq-server=default,jms-" + type + "="
                    + prefix + serviceName);
            depth = (Integer) beanServerConnection.getAttribute(objName, "messageCount");
        } catch (Exception e) {
            log.error("getQueueDepth failed with " + e);
            return -1;
        }
        return depth.intValue();
    }

    public String getServerName(String serviceName) {
        return prop.getProperty("blacktie." + serviceName + ".server");
    }

    public String getServerVersionById(String serverName, int id) {
        log.trace("getServerVersionById");
        String command = "version";
        Response buf = null;
        String version = null;

        try {
            buf = callAdminService(serverName, id, command);
            if (buf != null) {
                byte[] received = ((X_OCTET) buf.getBuffer()).getByteArray();
                if (received[0] == '1') {
                    version = new String(received, 1, received.length - 1);
                    log.debug("version is " + version);
                }
            }
        } catch (ConnectionException e) {
            log.error("call server " + serverName + " id " + id + " failed with " + e.getTperrno(), e);
        } catch (ConfigurationException e) {
            log.error("call server " + serverName + " id " + id + " failed with configuration exception", e);
        }
        return version;
    }
}
