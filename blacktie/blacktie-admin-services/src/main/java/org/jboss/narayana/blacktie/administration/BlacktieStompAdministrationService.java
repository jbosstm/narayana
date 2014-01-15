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
package org.jboss.narayana.blacktie.administration;

import static org.jboss.narayana.blacktie.administration.Authentication.getCallbackHandler;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.management.MBeanServerConnection;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;
import org.jboss.narayana.blacktie.administration.core.AdministrationProxy;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.XMLParser;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TPSVCINFO;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.mdb.MDBBlacktieService;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

@MessageDriven(activationConfig = {
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/BTR_BTStompAdmin") })
public class BlacktieStompAdministrationService extends MDBBlacktieService implements javax.jms.MessageListener {
    private static final Logger log = LogManager.getLogger(BlacktieStompAdministrationService.class);

    private static MBeanServerConnection beanServerConnection;
    private static Properties prop = new Properties();

    private static Hashtable<String, Long> QUEUE_CREATION_TIMES = new Hashtable<String, Long>();
    private static Hashtable<String, ServerInfo> SERVICE_OWNERS = new Hashtable<String, ServerInfo>();

    private static ModelControllerClient client;

    private class ServerInfo {
	public final String name;
        public final boolean conversational;
        public final String type;
        public ServerInfo(String name, boolean conversational, String type) {
           this.name = name;
           this.conversational = conversational;
           this.type = type; 
        } 
    }

    public BlacktieStompAdministrationService() throws ConfigurationException {
        super("BlacktieStompAdministrationService");
    }

    static void applyUpdate(ModelNode update, final ModelControllerClient client) throws IOException {
        ModelNode result = client.execute(new OperationBuilder(update).build());
        if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
            if (result.hasDefined("result")) {
                System.out.println(result.get("result"));
            }
        } else if (result.hasDefined("failure-description")) {
            throw new RuntimeException(result.get("failure-description").toString());
        } else {
            throw new RuntimeException("Operation not successful; outcome = " + result.get("outcome"));
        }
    }

    private static boolean isDeployQueue(String serviceName) throws Exception {

        boolean conversational = false;
        String type = "queue";
        if (!serviceName.startsWith(".")) {
            ServerInfo info = SERVICE_OWNERS.get(serviceName);
	    if(info != null)
            {
               conversational = info.conversational;
               type = info.type;
            }
            else
            {
               return false;
            }
        }
        String prefix = null;
        if (conversational) {
            prefix = "BTC_";
        } else {
            prefix = "BTR_";
        }
        ObjectName objName = new ObjectName("jboss.as:subsystem=messaging,hornetq-server=default,jms-" + type + "=" + prefix
                + "*");
        ObjectInstance[] dests = getMBeanServerConnection().queryMBeans(objName, null).toArray(new ObjectInstance[] {});
        for (int i = 0; i < dests.length; i++) {
            String serviceComponentOfObjectName = dests[i].getObjectName().getCanonicalName();
            serviceComponentOfObjectName = serviceComponentOfObjectName.substring(
                    serviceComponentOfObjectName.indexOf('_') + 1,
                    serviceComponentOfObjectName.indexOf(",", serviceComponentOfObjectName.indexOf('_')));
            log.debug("Service name component of ObjectName is: " + serviceComponentOfObjectName);
            if (serviceComponentOfObjectName.equals(serviceName)) {
                log.debug("find serviceName " + serviceName + " in Queues");
                return true;
            }
        }

        log.trace("did not find serviceName " + serviceName);
        return false;
    }

    private static MBeanServerConnection getMBeanServerConnection() throws ConfigurationException, UnknownHostException {
        initStatic();
        return beanServerConnection;
    }

    private static Object getProperty(String string) throws ConfigurationException, UnknownHostException {
        initStatic();
        return prop.get(string);
    }

    private static ModelControllerClient getClient() throws ConfigurationException, UnknownHostException {
        initStatic();
        return client;
    }

    private static void initStatic() throws ConfigurationException, UnknownHostException {
        synchronized (prop) {
            if (prop.isEmpty()) {
                XMLParser.loadProperties("btconfig.xsd", "btconfig.xml", prop);
                beanServerConnection = java.lang.management.ManagementFactory.getPlatformMBeanServer();
                String managementAddress = System.getProperty("jboss.bind.address.management", "localhost");
                if (managementAddress.equals("0.0.0.0")) {
                    managementAddress = "localhost";
                }
                client = ModelControllerClient.Factory.create("remote", InetAddress.getByName(managementAddress), 9999,
                        getCallbackHandler());
            }
        }
    }

    int consumerCount(String serviceName) throws Exception {
        log.trace("consCount" + serviceName);
        boolean conversational = false;
        String type = "queue";

        if (!serviceName.startsWith(".")) {
            ServerInfo info = SERVICE_OWNERS.get(serviceName);
            conversational = info.conversational;
            type = info.type;
        }
        String prefix = null;
        if (conversational) {
            prefix = "BTC_";
        } else {
            prefix = "BTR_";
        }

        Integer count = null;
        ObjectName objName = new ObjectName("jboss.as:subsystem=messaging,hornetq-server=default,jms-" + type + "=" + prefix
                + serviceName);
        if (type.toLowerCase().equals("queue")) {
            count = (Integer) getMBeanServerConnection().getAttribute(objName, "consumerCount");
        } else {
            count = (Integer) getMBeanServerConnection().getAttribute(objName, "subscriptionCount");
        }
        log.debug("consCount" + serviceName + " " + count.intValue());
        return count.intValue();
    }

    Element stringToElement(String s) throws Exception {
        StringReader sreader = new StringReader(s);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder parser = factory.newDocumentBuilder();
        Document doc = parser.parse(new InputSource(sreader));
        return doc.getDocumentElement();
    }

    String printNode(Node node) {
        try {
            // Set up the output transformer
            TransformerFactory transfac = TransformerFactory.newInstance();
            Transformer trans = transfac.newTransformer();
            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            trans.setOutputProperty(OutputKeys.INDENT, "yes");

            // Print the DOM node

            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);
            DOMSource source = new DOMSource(node);
            trans.transform(source, result);
            String xmlString = sw.toString();

            return xmlString;
        } catch (TransformerException e) {
            log.error(e);
        }
        return null;
    }

    public static boolean isOlderThanReapCheck(String serviceName, long queueReapCheck) {
        // TODO THIS WILL NOT CLUSTER AS IT ASSUMES THE QUEUE WAS CREATED BY
        // THIS SERVER
        log.trace("Locking for isOlderThanReapCheck: " + serviceName);
        synchronized (QUEUE_CREATION_TIMES) {
            log.trace("Locked for isOlderThanReapCheck: " + serviceName);
            boolean toReturn = true;
            Long creationTime = QUEUE_CREATION_TIMES.get(serviceName);
            if (creationTime != null) {
                toReturn = creationTime < queueReapCheck;
                if (!toReturn) {
                    log.debug("New queue will be ignored: " + serviceName);
                }
            }
            return toReturn;
        }
    }

    public int deployQueue(String serviceName, String serverName, boolean conversational, String type, String version) throws ConfigurationException, UnknownHostException {
        log.trace("deployQueue: " + serviceName + " version: " + version);

        if (version == null || !version.equals(getProperty("blacktie.domain.version"))) {
            log.warn("Blacktie Domain version " + getProperty("blacktie.domain.version") + " not match server " + version);
            return 4;
        }

        int result = 0;
        // Long currentTime = QUEUE_CREATION_TIMES.get(serviceName);

        try {
            boolean queue = false;
            log.debug("Locking for deployQueue: " + serviceName);
            synchronized (QUEUE_CREATION_TIMES) {
                log.debug("Locked for deployQueue: " + serviceName);
                queue = isDeployQueue(serviceName);
                log.debug("Queue " + serviceName + " was deployed?: " + queue);
                if (queue == false) {
                    log.debug("Creating " + serviceName);
                    log.trace("Lock acquired");
                    String prefix = null;
                    if (conversational) {
                        prefix = "BTC_";
                    } else {
                        prefix = "BTR_";
                    }
                    QUEUE_CREATION_TIMES.put(serviceName, System.currentTimeMillis());
		    SERVICE_OWNERS.put(serviceName, new ServerInfo(serverName, conversational, type));

                    log.trace(serviceName);

                    log.debug("Invoking hornetq to deploy queue");
                    ModelNode op = new ModelNode();
                    op.get("operation").set("add");
                    op.get("address").add("subsystem", "messaging");
                    op.get("address").add("hornetq-server", "default");
                    op.get("address").add("jms-" + type, prefix + serviceName);
                    op.get("entries").add("/" + type + "/" + prefix + serviceName);
                    // op.get("jms-" + type + "-address").set("jms." + type + "." + prefix + serviceName);
                    applyUpdate(op, getClient());
                    log.debug("Invoked hornetq to deploy queue");
                }
                log.debug("Created: " + serviceName);
                // QUEUE_CREATION_TIMES.put(serviceName, currentTime);
                if (!queue || !serviceName.contains(".")) {
                    result = 1;
                    if (AdministrationProxy.isDomainPause && serviceName.contains(".")) {
                        log.debug("Domain is pause");
                        result = 3;
                    }
                } else if (serviceName.contains(".") && queue && consumerCount(serviceName) > 0) {
                    log.warn("can not advertise ADMIN with same id: " + serviceName);
                    result = 2;
                } else if (AdministrationProxy.isDomainPause) {
                    log.debug("Domain is pause");
                    result = 3;
                } else {
                    result = 1;
                }
            }
        } catch (Throwable t) {
            log.error("Could not deploy queue of " + serviceName, t);
        }

        return result;
    }

    static int undeployQueue(String serviceName) {
        int result = 0;

        try {
            if (isDeployQueue(serviceName)) {
                log.trace(serviceName);
                boolean conversational = false;
                String type = "queue";
                if (!serviceName.startsWith(".")) {
                    ServerInfo info = SERVICE_OWNERS.get(serviceName);
                    conversational = info.conversational;
                    type = info.type;
                }
                String prefix = null;
                if (conversational) {
                    prefix = "BTC_";
                } else {
                    prefix = "BTR_";
                }

                ModelNode op = new ModelNode();
                op.get("operation").set("remove");
                op.get("address").add("subsystem", "messaging");
                op.get("address").add("hornetq-server", "default");
                op.get("address").add("jms-" + type, prefix + serviceName);
                applyUpdate(op, getClient());
            }
            result = 1;
        } catch (Throwable t) {
            log.error("Could not undeploy queue of " + serviceName, t);
        }

        return result;
    }

    public int decrementConsumer(String serviceName) {
        log.trace("decrement");
        int consumerCounts;
        int result = 0;

        try {
            consumerCounts = consumerCount(serviceName);
            if (consumerCounts < 1) {
                result = undeployQueue(serviceName);
                log.debug(serviceName + " undeployed");
            } else {
                // THERE ARE OTHER SERVERS STILL ALIVE
                result = 1;
                log.debug(serviceName + " still has " + consumerCounts + " consumers");
            }
        } catch (Throwable t) {
            log.debug("Could not get consumer counts of " + serviceName, t);
        }
        return result;
    }

    public Response tpservice(TPSVCINFO svcinfo) {
        log.debug("Message received");
        X_OCTET recv = (X_OCTET) svcinfo.getBuffer();
        String string = new String(recv.getByteArray());
        StringTokenizer st = new StringTokenizer(string, ",", false);
        String operation = st.nextToken();
        String serverName = st.nextToken();
        String serviceName = st.nextToken();

        byte[] success = new byte[1];
        String server = null;

        try {
            if (serviceName.indexOf(".") > -1) {
                server = serviceName.substring(1);
                server = server.replaceAll("[0-9]", "");
            } else {

                ServerInfo info = SERVICE_OWNERS.get(serviceName);

                if(info == null)
                {
                   server = serverName;
                }
                else
                {
                   server = info.name;
                }
            }

            if (server != null && server.equals(serverName)) {
                log.trace("Service " + serviceName + " exists for server: " + server);
                if (operation.equals("tpadvertise")) {
                    log.trace("Advertising: " + serviceName);
		    boolean conversational = st.nextToken().equals("1");
		    String type = st.nextToken();
                    String version = st.nextToken();
                    success[0] = (byte) deployQueue(serviceName, serverName, conversational, type, version);
                    log.trace("Advertised: " + serviceName);
                } else if (operation.equals("decrementconsumer")) {
                    log.trace("Decrement consumer: " + serviceName);
                    success[0] = (byte) decrementConsumer(serviceName);
                    log.trace("Decremented consumer: " + serviceName);
                } else {
                    log.error("Unknown operation " + operation);
                    success[0] = 0;
                }
            } else {
                log.error("Service " + serviceName + " already exists for a different server (" + server + ")");
                success[0] = 0;
            }

            X_OCTET buffer = (X_OCTET) svcinfo.getConnection().tpalloc("X_OCTET", null);
            buffer.setByteArray(success);
            log.debug("Responding");
            return new Response(Connection.TPSUCCESS, 0, buffer, 0);
        } catch (ConnectionException e) {
            return new Response(Connection.TPFAIL, 0, null, 0);
        } catch (ConfigurationException e) {
            return new Response(Connection.TPFAIL, 0, null, 0);
        } catch (UnknownHostException e) {
            return new Response(Connection.TPFAIL, 0, null, 0);
        }
    }
}
