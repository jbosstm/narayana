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
package org.jboss.narayana.blacktie.jatmibroker.core.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * XMLEnvHandler extends DefaultHandler to Environment Info
 */
public class XMLEnvHandler extends DefaultHandler {
    private static final Logger log = LogManager.getLogger(XMLEnvHandler.class);
    private final static String envVarPatStr = "(.*)\\$\\{(.*)\\}(.*)";
    private static java.util.regex.Pattern pattern;

    private final String DOMAIN = "DOMAIN";
    private final String BUFFER = "BUFFER";
    private final String ATTRIBUTE = "ATTRIBUTE";
    private final String SERVER_NAME = "SERVER";
    private final String SERVICE_NAME = "SERVICE";
    private final String ORB = "ORB";
    private final String MQ = "MQ";
    private final String SOCKETSERVER = "SOCKETSERVER";
    private final String MACHINE = "MACHINE";
    private final String MACHINE_REF = "MACHINE-REF";

    private Properties prop;

    private String value;
    private String serverName;
    private String serviceName;

    private List<String> servers = new ArrayList<String>();

    private Map<String, BufferStructure> buffers = new HashMap<String, BufferStructure>();

    private Map<String, Machine> machines = new HashMap<String, Machine>();

    private List<Server> serverLaunchers = new ArrayList<Server>();

    private String currentBufferName;

    static int CHAR_SIZE = 1;
    static int LONG_SIZE = 8;
    static int INT_SIZE = 4;
    static int SHORT_SIZE = 2;
    static int FLOAT_SIZE = 4;
    static int DOUBLE_SIZE = 8;

    XMLEnvHandler(Properties prop) {
        this.prop = prop;
        prop.put("blacktie.domain.servers", servers);
        prop.put("blacktie.domain.buffers", buffers);
        prop.put("blacktie.domain.serverLaunchers", serverLaunchers);
        prop.setProperty("blacktie.domain.version", "6.0.0.Alpha1-SNAPSHOT");

        if (pattern == null) {
            pattern = java.util.regex.Pattern.compile(envVarPatStr);
        }
    }

    public XMLEnvHandler() {
        this(new Properties());
    }

    public void characters(char[] ch, int start, int length) throws SAXException {
        String strValue = new String(ch, start, length);
        value += strValue;
    }

    /**
     * Search inputStr for sequences of the form ${VAR} and replace them with the result of System.getenv("VAR"); If the
     * enviromment variable VAR is not set then the literal text "VAR" is used instead.
     * 
     * @param inputStr the pattern to match against
     * @return the same string with sequences of the form ${VAR} replaced by the result of System.getenv("VAR") or "VAR" if that
     *         returns null
     */
    public String getenv(CharSequence inputStr) {
        String[] matches = new String[3];
        matches[0] = "";
        matches[2] = "";
        java.util.regex.Matcher matcher = pattern.matcher(inputStr);
        //matcher.reset(inputStr);

        if (matcher.find()) {
            StringBuilder sb = new StringBuilder();
            boolean expanded = false;

            for (int i = 0; i < matcher.groupCount(); i++) {
                if (i < matches.length) {
                    String val = matcher.group(i + 1);
                    if (val != null && val.length() > 0) {
                        String env = System.getenv(val);
                        if (env == null) {
                            env = System.getProperty(val);
                        }

                        if (env == null) {
                            if (val.equals("jboss.bind.address.management")) {
                                env = "localhost";
                            }
                            matches[i] = val;
                        } else {
                            matches[i] = env;
                            expanded = true;
                        }
                    }
                }
            }

            if (!expanded)
                log.error("There is an unset environment variable within the configuration element/attribute: " + inputStr);

            for (int i = 0; i < matches.length; i++)
                sb.append(matches[i]);
            return sb.toString();
        }

        return inputStr.toString();
    }

    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        String avalue;
        value = "";

        if (SERVER_NAME.equals(localName)) {
            serverName = atts.getValue(0);
            if (servers.contains(serverName)) {
                throw new SAXException("Duplicate server detected: " + serverName);
            }
            servers.add(serverName);
            serverLaunchers.add(new Server(serverName));
        } else if (MACHINE_REF.equals(localName)) {
            Machine machine = null;
            avalue = getenv(atts.getValue(0));
            // Get the machine out of the list
            machine = machines.get(avalue);
            if (machine == null) {
                throw new SAXException("Machine did not exist: " + avalue);
            }
            // This will be the last server added
            Server server = serverLaunchers.get(serverLaunchers.size() - 1);
            server.addMachine(machine);

            // Make sure to record the administration services as not
            // conversational
            String ad_key = "blacktie.." + server.getName() + machine.getId() + ".conversational";
            prop.put(ad_key, false);
        } else if (BUFFER.equals(localName)) {
            currentBufferName = atts.getValue(0);
            BufferStructure buffer = buffers.get(currentBufferName);
            if (buffer != null) {
                throw new SAXException("Duplicate buffer detected: " + currentBufferName);
            }
            buffer = new BufferStructure();
            buffer.name = currentBufferName;
            buffer.wireSize = 0;
            buffer.memSize = 0;
            buffer.lastPad = 0;
            buffers.put(currentBufferName, buffer);
        } else if (ATTRIBUTE.equals(localName)) {
            BufferStructure buffer = buffers.get(currentBufferName);
            AttributeStructure attribute = new AttributeStructure();
            attribute.id = null;
            attribute.type = null;
            attribute.count = 0;
            attribute.length = 0;
            attribute.wirePosition = 0;
            attribute.memPosition = 0;
            String type = null;
            for (int i = 0; i < atts.getLength(); i++) {
                if (atts.getLocalName(i).equals("id")) {
                    attribute.id = atts.getValue(i);
                } else if (atts.getLocalName(i).equals("type")) {
                    type = atts.getValue(i);
                } else if (atts.getLocalName(i).equals("arrayCount")) {
                    attribute.count = Integer.parseInt(atts.getValue(i));
                } else if (atts.getLocalName(i).equals("arrayLength")) {
                    attribute.length = Integer.parseInt(atts.getValue(i));
                }
            }

            int typeSize = -1;
            boolean contains = buffer.attributeNames.contains(attribute.id);
            boolean fail = false;
            if (!contains) {
                // short, int, long, float, double, char
                if (type.equals("short")) {
                    typeSize = SHORT_SIZE;
                    attribute.instanceSize = SHORT_SIZE;
                    attribute.type = short.class;
                } else if (type.equals("int")) {
                    typeSize = INT_SIZE;
                    attribute.instanceSize = INT_SIZE;
                    attribute.type = int.class;
                } else if (type.equals("long")) {
                    typeSize = LONG_SIZE;
                    attribute.instanceSize = LONG_SIZE;
                    attribute.type = long.class;
                } else if (type.equals("float")) {
                    typeSize = FLOAT_SIZE;
                    attribute.instanceSize = FLOAT_SIZE;
                    attribute.type = float.class;
                } else if (type.equals("double")) {
                    typeSize = DOUBLE_SIZE;
                    attribute.instanceSize = DOUBLE_SIZE;
                    attribute.type = double.class;
                } else if (type.equals("char")) {
                    typeSize = CHAR_SIZE;
                    attribute.instanceSize = CHAR_SIZE;
                    attribute.type = byte.class;
                } else if (type.equals("char[]")) {
                    if (attribute.length == 0) {
                        attribute.length = 1;
                    }
                    typeSize = CHAR_SIZE;
                    attribute.instanceSize = CHAR_SIZE * attribute.length;
                    attribute.type = byte[].class;
                } else if (type.equals("short[]")) {
                    if (attribute.length == 0) {
                        attribute.length = 1;
                    }
                    typeSize = SHORT_SIZE;
                    attribute.instanceSize = SHORT_SIZE * attribute.length;
                    attribute.type = short[].class;
                } else if (type.equals("int[]")) {
                    if (attribute.length == 0) {
                        attribute.length = 1;
                    }
                    typeSize = INT_SIZE;
                    attribute.instanceSize = INT_SIZE * attribute.length;
                    attribute.type = int[].class;
                } else if (type.equals("long[]")) {
                    if (attribute.length == 0) {
                        attribute.length = 1;
                    }
                    typeSize = LONG_SIZE;
                    attribute.instanceSize = LONG_SIZE * attribute.length;
                    attribute.type = long[].class;
                } else if (type.equals("float[]")) {
                    if (attribute.length == 0) {
                        attribute.length = 1;
                    }
                    typeSize = FLOAT_SIZE;
                    attribute.instanceSize = FLOAT_SIZE * attribute.length;
                    attribute.type = float[].class;
                } else if (type.equals("double[]")) {
                    if (attribute.length == 0) {
                        attribute.length = 1;
                    }
                    typeSize = DOUBLE_SIZE;
                    attribute.instanceSize = DOUBLE_SIZE * attribute.length;
                    attribute.type = double[].class;
                } else if (type.equals("char[][]")) {
                    if (attribute.length == 0) {
                        attribute.length = 1;
                    }
                    if (attribute.count == 0) {
                        attribute.count = 1;
                    }
                    typeSize = CHAR_SIZE;
                    attribute.instanceSize = CHAR_SIZE * attribute.length * attribute.count;
                    attribute.type = byte[][].class;
                } else {
                    log.error("Unknown attribute type: " + attribute.type);
                    fail = true;
                }

                if (!fail) {
                    buffer.attributes.add(attribute);

                    // Extend the buffer by the required extra buffer size
                    if (buffer.lastPad < typeSize) {
                        buffer.lastPad = typeSize;
                    }

                    buffer.memSize = buffer.memSize + (buffer.memSize % typeSize);
                    attribute.memPosition = buffer.memSize;
                    attribute.wirePosition = buffer.wireSize;
                    buffer.wireSize = buffer.wireSize + attribute.instanceSize;
                    buffer.memSize = buffer.memSize + attribute.instanceSize;
                }
            } else {
                log.error("Duplicate attribute detected: " + attribute.id);
            }
        } else if (ORB.equals(localName)) {
            for (int j = 0; j < atts.getLength(); j++) {
                if (atts.getLocalName(j).equals("OPT")) {
                    String[] argv = atts.getValue(j).split(" ");
                    int orbargs = 0;

                    boolean lookForInterface = false;
                    for (int i = 0; i < argv.length; i++) {
                        String arg = "blacktie.orb.arg." + (i + 1);
                        String toSet = getenv(argv[i]);

                        if (toSet.equals("-ORBListenEndpoints")) {
                            lookForInterface = true;
                        } else if (lookForInterface) {
                            // " iiop://${JBOSSAS_IP_ADDR}:0"
                            int startOfHostname = toSet.indexOf("//") + 2;
                            int portIndex = toSet.indexOf(":", startOfHostname);
                            String interfaceAddress = toSet.substring(startOfHostname, portIndex);
                            String interfacePort = toSet.substring(portIndex + 1);
                            prop.setProperty("blacktie.orb.interface", interfaceAddress);
                            prop.setProperty("blacktie.orb.interface.port", interfacePort);
                        } else {
                            prop.setProperty(arg, toSet);
                            log.debug(arg + " is " + toSet);
                            orbargs++;
                        }
                    }

                    log.debug("blacktie.orb.args is " + orbargs);
                    prop.setProperty("blacktie.orb.args", Integer.toString(orbargs));

                } else if (atts.getLocalName(j).equals("TRANS_FACTORY_ID")) {
                    prop.setProperty("blacktie.trans.factoryid", atts.getValue(j));
                }
            }
        } else if (MQ.equals(localName)) {
            for (int i = 0; i < atts.getLength(); i++) {
                avalue = getenv(atts.getValue(i));

                if (atts.getLocalName(i).equals("USER")) {
                    prop.setProperty("StompConnectUsr", avalue);
                } else if (atts.getLocalName(i).equals("PASSWORD")) {
                    prop.setProperty("StompConnectPwd", avalue);
                } else if (atts.getLocalName(i).equals("DESTINATION_TIMEOUT")) {
                    prop.setProperty("DestinationTimeout", avalue);
                } else if (atts.getLocalName(i).equals("RECEIVE_TIMEOUT")) {
                    prop.setProperty("ReceiveTimeout", avalue);
                } else if (atts.getLocalName(i).equals("TIME_TO_LIVE")) {
                    prop.setProperty("TimeToLive", avalue);
                } else if (atts.getLocalName(i).equals("NAMING_URL")) {
                    prop.setProperty("java.naming.provider.url", avalue);
                } else if (atts.getLocalName(i).equals("HOST")) {
                    prop.setProperty("StompConnectHost", avalue);
                } else if (atts.getLocalName(i).equals("PORT")) {
                    prop.setProperty("StompConnectPort", avalue);
                }
            }
        } else if(SOCKETSERVER.equals(localName)) {
            for (int i = 0; i < atts.getLength(); i++) {
                avalue = getenv(atts.getValue(i));
                if (atts.getLocalName(i).equals("PORT")) {
                    prop.setProperty("blacktie.java.socketserver.port", avalue);
                } else if (atts.getLocalName(i).equals("HOST")) {
                    prop.setProperty("blacktie.java.socketserver.host", avalue);
                }
            }
        } else if (MACHINE.equals(localName)) {
            Machine machine = new Machine();
            for (int i = 0; i < atts.getLength(); i++) {
                avalue = getenv(atts.getValue(i));

                if (atts.getLocalName(i).equals("id")) {
                    machine.setId(avalue);
                } else if (atts.getLocalName(i).equals("hostname")) {
                    machine.setHostname(avalue);
                } else if (atts.getLocalName(i).equals("ipAddress")) {
                    machine.setIpAddress(avalue);
                } else if (atts.getLocalName(i).equals("pathToExecutable")) {
                    machine.setPathToExecutable(avalue);
                } else if (atts.getLocalName(i).equals("workingDirectory")) {
                    machine.setWorkingDirectory(avalue);
                } else if (atts.getLocalName(i).equals("serverId")) {
                    machine.setServerId(Integer.parseInt(avalue));
                } else if (atts.getLocalName(i).equals("argLine")) {
                    machine.setArgLine(avalue);
                }
            }
            machines.put(machine.getId(), machine);
        } else if (SERVICE_NAME.equals(localName)) {
            for (int i = 0; i < atts.getLength(); i++) {
                String attsLocalName = atts.getLocalName(i);
                if (attsLocalName.equals("name")) {
                    serviceName = atts.getValue(i);
                    String serviceServer = (String) prop.get("blacktie." + serviceName + ".server");
		    String type = (String) prop.get("blacktie." + serviceName + ".type");
                    if (serviceServer != null && !type.equals("topic")) {
                        log.warn("service " + serviceName + " has already been defined in " + serviceName);
                        throw new SAXException("Can not define the same service: " + serviceName);
                    }

                    if (serviceName.equals("BTStompAdmin") || serviceName.equals("BTDomainAdmin")) {
                        throw new SAXException("Can not define service: " + serviceName);
                    }
                    prop.put("blacktie." + serviceName + ".server", serverName);
                } else if (attsLocalName.equals("function_name")) {
                    String func_key = "blacktie." + serviceName + ".function_name";
                    String function_name = atts.getValue(i);
                    prop.put(func_key, function_name);
                } else if (attsLocalName.equals("java_class_name")) {
                    String java_key = "blacktie." + serviceName + ".java_class_name";
                    String java_class_name = atts.getValue(i);
                    prop.put(java_key, java_class_name);
                } else if (attsLocalName.equals("library_name")) {
                    String lib_key = "blacktie." + serviceName + ".library_name";
                    String library_name = atts.getValue(i);
                    prop.put(lib_key, library_name);
                } else if (attsLocalName.equals("advertised")) {
                    String advertised = atts.getValue(i);
                    String ad_key = "blacktie." + serviceName + ".advertised";
                    prop.put(ad_key, advertised);
                    if (advertised.equals("true")) {
                        String skey = "blacktie." + serverName + ".services";
                        String object = (String) prop.get(skey);
                        if (object == null) {
                            object = serviceName;
                        } else {
                            object = new String(object + "," + serviceName);
                        }
                        prop.put(skey, object);
                    }
                } else if (attsLocalName.equals("conversational")) {
                    String conversational = atts.getValue(i);
                    String ad_key = "blacktie." + serviceName + ".conversational";
                    prop.put(ad_key, new Boolean(conversational));
                } else if (attsLocalName.equals("size")) {
                    String sizeKey = "blacktie." + serviceName + ".size";
                    String sizeVal = atts.getValue(i);
                    prop.setProperty(sizeKey, sizeVal);
                } else if (attsLocalName.equals("externally-managed-destination")) {
                    String external = atts.getValue(i);
                    String ad_key = "blacktie." + serviceName + ".externally-managed-destination";
                    prop.put(ad_key, new Boolean(external));
                } else if (attsLocalName.equals("type")) {
                    String type = atts.getValue(i);
                    String type_key = "blacktie." + serviceName + ".type";
                    prop.setProperty(type_key, type);
                } else if (attsLocalName.equals("coding_type")) {
                    String coding_type = atts.getValue(i);
                    String coding_key = "blacktie." + serviceName + ".coding_type";
                    prop.setProperty(coding_key, coding_type);
                }
            }

            // If type was not defined above
            String type_key = "blacktie." + serviceName + ".type";
            if (prop.get(type_key) == null) {
                prop.put(type_key, "queue");
            }

            // If conversational was not defined above
            String ad_key = "blacktie." + serviceName + ".conversational";
            if (prop.get(ad_key) == null) {
                prop.put(ad_key, false);
            }

            String ex_key = "blacktie." + serviceName + ".externally-managed-destination";
            if (prop.get(ex_key) == null) {
                prop.put(ex_key, false);
            }
            log.trace("Externally managed: " + prop.get("blacktie." + serviceName + ".externally-managed-destination"));
            log.trace("Added the service: " + serviceName);
            log.trace("Adding for: " + serviceName + " "
                    + prop.get("blacktie." + serviceName + ".externally-managed-destination"));
        }
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        if (DOMAIN.equals(localName)) {
            prop.setProperty("blacktie.domain.name", getenv(value));
        } else if (SERVICE_NAME.equals(localName)) {
            serviceName = null;
        }
    }
}
