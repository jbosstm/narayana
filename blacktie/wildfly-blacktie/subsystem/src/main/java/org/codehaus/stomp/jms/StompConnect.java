/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.stomp.jms;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.XAConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.net.ServerSocketFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.stomp.tcp.TcpTransport;
import org.codehaus.stomp.tcp.TcpTransportServer;

/**
 * This class represents a service which accepts STOMP socket connections and binds them to JMS operations
  *
 * @version $Revision: 53 $
 */
public class StompConnect {
    private static final transient Log log = LogFactory.getLog(StompConnect.class);

    private ConnectionFactory connectionFactory;
    private XAConnectionFactory xaConnectionFactory;
    private String uri = "tcp://localhost:61613";
    private URI location;
    private ServerSocketFactory serverSocketFactory;
    private TcpTransportServer tcpServer;
    private InitialContext initialContext;
    private String connectionFactoryName = "java:/ConnectionFactory";
    private String xaConnectionFactoryName = "java:/JmsXA";

    public StompConnect() throws NamingException {
        initialContext = new InitialContext();
    }

    public void assignProtocolConverter(TcpTransport transport) throws NamingException {
        ConnectionFactory factory = getConnectionFactory();
        if (factory == null) {
            throw new IllegalArgumentException("No ConnectionFactory is configured!");
        }
        XAConnectionFactory xaFactory = getXAConnectionFactory();
        if (xaFactory == null) {
            throw new IllegalArgumentException("No XAConnectionFactory is configured!");
        }
        new ProtocolConverter(initialContext, factory, xaFactory, transport);
    }

    // Properties
    // -------------------------------------------------------------------------
    public ConnectionFactory getConnectionFactory() throws NamingException {
        if (connectionFactory == null) {
            connectionFactory = createConnectionFactory();
        }
        return connectionFactory;
    }

    // Properties
    // -------------------------------------------------------------------------
    public XAConnectionFactory getXAConnectionFactory() throws NamingException {
        if (xaConnectionFactory == null) {
            xaConnectionFactory = createXAConnectionFactory();
        }
        return xaConnectionFactory;
    }

    /**
     * Sets the JMS connection factory to use to communicate with
     */
    public void setConnectionFactory(XAConnectionFactory connectionFactory) {
        this.xaConnectionFactory = connectionFactory;
    }

    public String getUri() {
        return uri;
    }

    /**
     * Sets the URI string for the hostname/IP address and port to listen on for STOMP frames
     */
    public void setUri(String uri) {
        this.uri = uri;
    }

    public URI getLocation() throws URISyntaxException {
        if (location == null) {
            location = new URI(uri);
        }
        return location;
    }

    /**
     * Sets the URI for the hostname/IP address and port to listen on for STOMP frames
     */
    public void setLocation(URI location) {
        this.location = location;
    }

    public ServerSocketFactory getServerSocketFactory() {
        if (serverSocketFactory == null) {
            serverSocketFactory = ServerSocketFactory.getDefault();
        }
        return serverSocketFactory;
    }

    /**
     * Sets the {@link ServerSocketFactory} to use to listen for STOMP frames
     */
    public void setServerSocketFactory(ServerSocketFactory serverSocketFactory) {
        this.serverSocketFactory = serverSocketFactory;
    }

    public TcpTransportServer getTcpServer() throws IOException, URISyntaxException {
        if (tcpServer == null) {
            tcpServer = new TcpTransportServer(this, getLocation(), getServerSocketFactory());
        }
        return tcpServer;
    }

    public void setTcpServer(TcpTransportServer tcpServer) {
        this.tcpServer = tcpServer;
    }

    /**
     * Allows an initial context to be configured which is used if no explicit {@link ConnectionFactory} is configured via the
     * {@link #setConnectionFactory(ConnectionFactory)} method
     */
    public void setInitialContext(InitialContext initialContext) {
        this.initialContext = initialContext;
    }

    public String getXAConnectionFactoryName() {
        return xaConnectionFactoryName;
    }

    /**
     * Allows the JNDI name to be configured which is used to perform a JNDI lookup if no explicit {@link ConnectionFactory} is
     * configured via the {@link #setConnectionFactory(ConnectionFactory)} method
     */
    public void setXAConnectionFactoryName(String jndiName) {
        this.xaConnectionFactoryName = jndiName;
    }

    public String getConnectionFactoryName() {
        return connectionFactoryName;
    }

    /**
     * Allows the JNDI name to be configured which is used to perform a JNDI lookup if no explicit {@link ConnectionFactory} is
     * configured via the {@link #setConnectionFactory(ConnectionFactory)} method
     */
    public void setConnectionFactoryName(String jndiName) {
        this.connectionFactoryName = jndiName;
    }

    public void start() throws IOException, URISyntaxException, IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        getTcpServer().start();
    }

    public void stop() throws InterruptedException, IOException, JMSException, URISyntaxException {
        getTcpServer().stop();
    }

    /**
     * Factory method to lazily create a {@link ConnectionFactory} if one is not explicitly configured. By default lets try
     * looking in JNDI
     */
    protected XAConnectionFactory createXAConnectionFactory() throws NamingException {
        String name = getXAConnectionFactoryName();
        log.info("Looking up name: " + name + " in JNDI InitialContext for JMS ConnectionFactory");

        Object value = initialContext.lookup(name);
        if (value == null) {
            throw new IllegalArgumentException("No ConnectionFactory object is available in JNDI at name: " + name);
        }
        if (value instanceof XAConnectionFactory) {
            return (XAConnectionFactory) value;
        } else {
            throw new IllegalArgumentException("The object in JNDI at name: " + name
                    + " cannot be cast to XAConnectionFactory. "
                    + "Either a JNDI configuration issue or you have multiple JMS API jars on your classpath. "
                    + "Actual Object was: " + value);
        }
    }

    /**
     * Factory method to lazily create a {@link ConnectionFactory} if one is not explicitly configured. By default lets try
     * looking in JNDI
     */
    protected ConnectionFactory createConnectionFactory() throws NamingException {
        String name = getConnectionFactoryName();
        log.info("Looking up name: " + name + " in JNDI InitialContext for JMS ConnectionFactory");

        Object value = initialContext.lookup(name);
        if (value == null) {
            throw new IllegalArgumentException("No ConnectionFactory object is available in JNDI at name: " + name);
        }
        if (value instanceof ConnectionFactory) {
            return (ConnectionFactory) value;
        } else {
            throw new IllegalArgumentException("The object in JNDI at name: " + name + " cannot be cast to ConnectionFactory. "
                    + "Either a JNDI configuration issue or you have multiple JMS API jars on your classpath. "
                    + "Actual Object was: " + value);
        }
    }
}
