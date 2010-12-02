/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 *
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.arjuna.tools.osb.api.proxy;

import com.arjuna.ats.arjuna.tools.osb.api.mbeans.ParticipantStoreBeanMBean;
import com.arjuna.ats.arjuna.tools.osb.api.mbeans.RecoveryStoreBeanMBean;
import com.arjuna.ats.arjuna.tools.osb.util.JMXServer;

import javax.management.*;
import javax.management.remote.*;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Miscellaneous methods for obtaining remote proxies to the JBossTS Recovery and Participant stores
 */
public class StoreManagerProxy {
    // class and bean names of MBeans representing the JBossTS recovery and participant stores
    public static final String RECOVERY_BEAN_NAME = "jboss.jta:type=com.arjuna.ats.arjuna.tools.osb.api.mbeans.RecoveryStoreBean,name=store1";
    public static final String PARTICIPANT_BEAN_NAME = "jboss.jta:type=com.arjuna.ats.arjuna.tools.osb.api.mbeans.ParticipantStoreBean,name=store1";

//    public static final String SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:9999/server";

    private static Map<String, StoreManagerProxy> proxies = Collections.synchronizedMap(new HashMap<String, StoreManagerProxy>());
    private static JMXConnectorServer jmxCServer;

    private MBeanServerConnection mbsc;	// MBean server implementing the object store MBeans

    private JMXConnector jmxc;
    private RecoveryStoreProxy rsProxy;	// proxy for the recovery store
    private ParticipantStoreProxy psProxy;	// proxy for the participant store
    private ObjectName recoveryStoreON;	// object name of the recover store MBean
    private ObjectName participantStoreON;	// object name of the participant store MBean
    private NotificationListener recoveryListener = null;
    private NotificationListener participantListener = null;

    /**
     * Construct a holder for Participant and Recovery Store proxies. There is one instance for each connection
     * to a JVM. In practice there will only ever be one instance.
     *
     * @param serviceUrl the url for the MBean server to connect to. Use default to connect to the local MBean Server
     * @param listener optionally register a listener for notifications
     * @throws JMException if there are JMX errors during registration of MBeans and notification listeners
     * @throws IOException if there are errors on the connection to the MBean Server
     */
    private StoreManagerProxy(String serviceUrl, NotificationListener listener) throws JMException, IOException {
        if ("default".equals(serviceUrl)) {
            mbsc = JMXServer.getAgent().getServer();
        } else {
            // create an RMI connector
            JMXServiceURL url = new JMXServiceURL(serviceUrl);
            // connect to the target MBean server
            jmxc = JMXConnectorFactory.connect(url, null);
            mbsc = jmxc.getMBeanServerConnection();
        }

        recoveryStoreON = ObjectName.getInstance(RECOVERY_BEAN_NAME);
        participantStoreON = ObjectName.getInstance(PARTICIPANT_BEAN_NAME);

        rsProxy = new RecoveryStoreProxy(JMX.newMBeanProxy( mbsc, recoveryStoreON, RecoveryStoreBeanMBean.class, true));
        psProxy = new ParticipantStoreProxy(JMX.newMBeanProxy( mbsc, participantStoreON, ParticipantStoreBeanMBean.class, true));

        if (listener != null) {
            mbsc.addNotificationListener(recoveryStoreON, listener, null, null);
            mbsc.addNotificationListener(participantStoreON, listener, null, null);
        }
    }

    /**
     * Mechanism for JMX clients to remove listeners and to close the JMX connection if the client
     * create one
     * @throws JMException if there are errors removing listeners
     * @throws IOException if there are errors removing listeners or closing the JMX connection
     */
    private void close() throws JMException, IOException {
        System.out.println("Remove notification listener...");
        // Remove notification listener on RecoveryStore MBean
        if (this.recoveryListener != null)
            mbsc.removeNotificationListener(recoveryStoreON, recoveryListener);
        if (this.participantListener != null)
            mbsc.removeNotificationListener(participantStoreON, participantListener);

        recoveryListener = participantListener = null;

        // close the connection to the JMX server
        if (jmxc != null) {
            jmxc.close();
            jmxc = null;
        }
    }

    /**
     * Helper method for remote clients to connect to an MBean Server
     *
     * @param serviceUrl the url on which the target MBean Server resides
     * @throws IOException if the serviceUrl is invalid or if the connection cannot be started
     */
    public static void startServerConnector(String serviceUrl) throws IOException {
        jmxCServer = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL(serviceUrl), null, JMXServer.getAgent().getServer());

        // accept JMX connections
        jmxCServer.start();
    }

    public static void stopServerConnector() throws IOException {
        jmxCServer.stop();
    }

    /**
     * MBean registration helper method
     * @param name MBean object name
     * @param bean MBean implementation
     * @param register whether to register or unregister the MBean
     * @return true if the bean was successfully registered or unregistered
     */
    public static boolean registerBean(ObjectName name, Object bean, boolean register) {
        try {
            MBeanServer mbs = JMXServer.getAgent().getServer();
            boolean isRegistered = mbs.isRegistered(name);

            System.out.println((register ? "" : "un") + "registering bean " + name);

            if (register && isRegistered) {
                System.out.println(name + " is already registered");
                return true;
            } else if (!register && !isRegistered) {
                System.out.println(name + " is not registered");
                return true;
            } else if (register) {
                mbs.registerMBean(bean, name);
            } else {
                mbs.unregisterMBean(name);
            }

            return true;

        } catch (JMException e) {
            System.out.println("MBean registration error: " + e.getMessage());

            return false;
        }
    }

    // Obtain a JMX proxy to the ObjectStoreAPI
    private static synchronized StoreManagerProxy getProxy(String serviceUrl, NotificationListener listener) throws IOException, JMException {
        if (serviceUrl == null)
            serviceUrl = "default";

        if (!proxies.containsKey(serviceUrl))
            proxies.put(serviceUrl, new StoreManagerProxy(serviceUrl, listener));

        return proxies.get(serviceUrl);
    }

    /**
     * release proxies to the object stores
     * @throws JMException if there are errors removing listeners
     * @throws IOException if there are errors removing listeners or closing the JMX connection
     */
    public static void releaseProxy() throws JMException, IOException {
        releaseProxy("default");
    }

    /**
     * release proxies to the object stores
     *
     * @param serviceUrl the service url of the MBean Server where the proxies are located
     * @throws JMException if there are errors removing listeners
     * @throws IOException if there are errors removing listeners or closing the JMX connection
     */
    public static void releaseProxy(String serviceUrl) throws JMException, IOException {
        StoreManagerProxy psm = proxies.remove(serviceUrl);

        if (psm != null)
            psm.close();
    }

    /**
     * Get a recovery store proxy from the local MBeanServer
     * @return a proxy for the target RecoveryStore
     * @throws JMException if there are JMX errors during registration of MBeans
     * @throws IOException if there are errors on the connection to the MBean Server
     */
    public static synchronized RecoveryStoreProxy getRecoveryStore() throws IOException, JMException {
        return getRecoveryStore(null);
    }

    /**
     * Get a recovery store proxy from the local MBeanServer
     * @param listener listener an optional notification listener (use null if one is not required)
     * @return a proxy for the target RecoveryStore
     * @throws JMException if there are JMX errors during registration of MBeans and notification listeners
     * @throws IOException if there are errors on the connection to the MBean Server
     */
    public static synchronized RecoveryStoreProxy getRecoveryStore(NotificationListener listener) throws IOException, JMException {
        return getProxy("default", listener).rsProxy;
    }

    /**
     * Get a RecoveryStore proxy.
     *
     * @param serviceUrl the location of the MBean Server
     * @param listener an optional notification listener (use null if one is not required)
     * @return a proxy for the target RecoveryStore
     * @throws JMException if there are JMX errors during registration of MBeans and notification listeners
     * @throws IOException if there are errors on the connection to the MBean Server
     */
    public static synchronized RecoveryStoreProxy getRecoveryStore(String serviceUrl, NotificationListener listener) throws IOException, JMException {
        return getProxy(serviceUrl, listener).rsProxy;
    }

    /**
     * Get a participant store proxy from the local MBeanServer
     * @return a proxy for the target ParticipantStore
     * @throws JMException if there are JMX errors during registration of MBeans
     * @throws IOException if there are errors on the connection to the MBean Server
     */
    public static synchronized ParticipantStoreProxy getParticipantStore() throws IOException, JMException {
        return getParticipantStore(null);
    }

    /**
     * Get a participant store proxy from the local MBeanServer
     * @param listener listener an optional notification listener (use null if one is not required)
     * @return a proxy for the target ParticipantStore
     * @throws JMException if there are JMX errors during registration of MBeans and notification listeners
     * @throws IOException if there are errors on the connection to the MBean Server
     */
    public static synchronized ParticipantStoreProxy getParticipantStore(NotificationListener listener) throws IOException, JMException {
        return getProxy("default", listener).psProxy;
    }

    /**
     * Get a participant store proxy.
     *
     * @param serviceUrl the location of the MBean Server
     * @param listener an optional notification listener (use null if one is not required)
     * @return a proxy for the target ParticipantStore
     * @throws JMException if there are JMX errors during registration of MBeans and notification listeners
     * @throws IOException if there are errors on the connection to the MBean Server
     */
    public static synchronized ParticipantStoreProxy getParticipantStore(String serviceUrl, NotificationListener listener) throws IOException, JMException {
        return getProxy(serviceUrl, listener).psProxy;
    }
}
