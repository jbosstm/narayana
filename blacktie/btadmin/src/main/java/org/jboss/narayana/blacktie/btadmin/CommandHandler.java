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
package org.jboss.narayana.blacktie.btadmin;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.management.MalformedObjectNameException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

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
import org.jboss.narayana.blacktie.jatmibroker.xatmi.impl.BufferImpl;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Handle the command
 */
public class CommandHandler implements java.lang.reflect.InvocationHandler {
    private static Logger log = LogManager.getLogger(CommandHandler.class);
    private Properties prop = new Properties();
    private Connection connection;
    private BlacktieAdministration administrationProxy;

    public CommandHandler() throws ConfigurationException, MalformedObjectNameException, NullPointerException {
        // Obtain the JMXURL from the btconfig.xml
        XMLParser.loadProperties("btconfig.xsd", "btconfig.xml", prop);

        administrationProxy = (BlacktieAdministration) Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[] { BlacktieAdministration.class }, this);
    }

    public int handleCommand(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        int exitStatus = -1;
        if (args.length < 1 || args[0] == null || args[0].trim().length() == 0) {
            log.error("No command was provided");
        } else {
            Command command = loadCommand(args[0]);

            // Create an new array for the commands arguments
            String[] commandArgs = new String[args.length - 1];
            if (commandArgs.length > 0) {
                log.trace("Copying arguments for the command");
                System.arraycopy(args, 1, commandArgs, 0, commandArgs.length);
            }

            String quickstartUsage = command.getQuickstartUsage();
            char[] charArray = quickstartUsage.toCharArray();
            int expectedArgsLength = 0;
            int optionalArgs = 0;
            // Note this does assume that each word is a parameter
            for (int i = 0; i < quickstartUsage.length(); i++) {
                if (charArray[i] == ' ') {
                    expectedArgsLength++;
                } else if (charArray[i] == '[') {
                    optionalArgs++;
                }
            }
            // Add the last parameter
            if (charArray.length > 0) {
                expectedArgsLength++;
            }
            // Check if the number of parameters is in an expected range
            if (commandArgs.length > expectedArgsLength || commandArgs.length < expectedArgsLength - optionalArgs) {
                if (optionalArgs == 0) {
                    log.trace("Arguments incompatible, expected " + expectedArgsLength + ", received: " + commandArgs.length);
                } else {
                    log.trace("Arguments incompatible, expected at least " + optionalArgs + " and no more than "
                            + expectedArgsLength + ", received: " + commandArgs.length);
                }
                log.error(("Expected Usage: " + args[0] + " " + quickstartUsage).trim());
            } else {
                try {
                    // Try to initialize the arguments
                    command.initializeArgs(commandArgs);
                    log.trace("Arguments initialized");
                    try {
                        // Try to invoke the command
                        command.invoke(administrationProxy, prop);
                        exitStatus = 0;
                        log.trace("Command invoked");
                    } catch (CommandFailedException e) {
                        exitStatus = e.getExitCode();
                    } catch (Exception e) {
                        log.error("Could not invoke the command: " + e.getMessage(), e);
                    }
                } catch (IncompatibleArgsException e) {
                    String usage = "Expected Usage: " + args[0] + " " + quickstartUsage;
                    log.error("Arguments invalid: " + e.getMessage());
                    log.error(usage.trim());
                    log.trace("Arguments invalid: " + e.getMessage(), e);
                }
            }
        }
        return exitStatus;
    }

    public static Command loadCommand(String commandName) throws InstantiationException, IllegalAccessException,
            ClassNotFoundException {
        String firstLetter = commandName.substring(0, 1);
        String remainder = commandName.substring(1);
        String capitalized = firstLetter.toUpperCase() + remainder;
        String className = "org.jboss.narayana.blacktie.btadmin.commands." + capitalized;
        log.debug("Will execute the " + className + " command");
        Command command = (Command) Class.forName(className).newInstance();
        log.debug("Command was known");
        return command;
    }

    /**
     * Utility function to output the list
     * 
     * @param operationName
     * @param list
     */
    public static String convertList(String operationName, List list) {
        StringBuffer buffer = new StringBuffer();
        buffer.append("Output from: " + operationName);
        int i = 0;
        Iterator iterator = list.iterator();
        while (iterator.hasNext()) {
            buffer.append("\nElement: " + i + " Value: " + iterator.next());
            i++;
        }
        return buffer.toString();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws ConfigurationException, ConnectionException,
            CommandFailedException, ParserConfigurationException, SAXException, IOException {
        if (connection == null) {
            ConnectionFactory cf = ConnectionFactory.getConnectionFactory();
            connection = cf.getConnection();
        }

        StringBuffer command = new StringBuffer(method.getName());
        command.append(',');
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    args[i] = "";
                }
                command.append(args[i].toString());
                command.append(',');
            }
        }
        X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray(command.toString().getBytes());

        Response received = connection.tpcall("BTDomainAdmin", sendbuf, 0);
        X_OCTET rcvbuf = (X_OCTET) received.getBuffer();

        Class<?> returnType = method.getReturnType();
        byte[] byteArray = rcvbuf.getByteArray();
        ;
        if (returnType == Boolean.class) {
            if (byteArray[0] == 1) {
                return true;
            } else {
                return false;
            }
        } else if (returnType == Long.class) {
            return convertLong(byteArray);
        } else if (returnType == String.class) {
            return new String(byteArray);
        } else if (returnType == java.util.List.class) {
            String listTerminator = BlacktieAdministration.LIST_TERMINATOR;
            String string = new String(byteArray);
            StringTokenizer outParameters = new StringTokenizer(string, "," + "", false);
            if (!method.getName().equals("listRunningInstanceIds")) {
                List<String> toReturn = new ArrayList<String>();
                while (outParameters.hasMoreTokens()) {
                    String nextToken = outParameters.nextToken();
                    if (!nextToken.equals(listTerminator)) {
                        toReturn.add(nextToken);
                    }
                }
                return toReturn;
            } else {
                List<Integer> toReturn = new ArrayList<Integer>();
                while (outParameters.hasMoreTokens()) {
                    String nextToken = outParameters.nextToken();
                    if (!nextToken.equals(listTerminator)) {
                        toReturn.add(Integer.parseInt(nextToken));
                    }
                }
                return toReturn;
            }
        } else if (returnType == org.w3c.dom.Element.class) {
            StringReader sreader = new StringReader(new String(byteArray));
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            Document doc = parser.parse(new InputSource(sreader));
            return doc.getDocumentElement();
        } else {
            log.error("Could not handle response type: " + returnType);
            throw new CommandFailedException(-1);
        }
        // java.util.List<Integer>
    }

    private long convertLong(byte[] response) throws IOException {
        ByteArrayInputStream baos = new ByteArrayInputStream(response);
        DataInputStream dos = new DataInputStream(baos);
        ByteBuffer bbuf = ByteBuffer.allocate(BufferImpl.LONG_SIZE);
        bbuf.order(ByteOrder.BIG_ENDIAN);
        bbuf.put(response);
        bbuf.order(ByteOrder.LITTLE_ENDIAN);
        return bbuf.getLong(0);
    }
}
