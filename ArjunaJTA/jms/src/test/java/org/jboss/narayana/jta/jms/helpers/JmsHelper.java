/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.narayana.jta.jms.helpers;

import java.util.ArrayList;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.netty.NettyConnectorFactory;
import org.apache.activemq.artemis.jms.server.config.ConnectionFactoryConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSConfiguration;
import org.apache.activemq.artemis.jms.server.config.JMSQueueConfiguration;
import org.apache.activemq.artemis.jms.server.config.impl.ConnectionFactoryConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSConfigurationImpl;
import org.apache.activemq.artemis.jms.server.config.impl.JMSQueueConfigurationImpl;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.jboss.logging.Logger;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JmsHelper {

    public static final String FACTORY_NAME = "connection-factory";

    public static final String QUEUE_NAME = "test-queue";

    private static final Logger LOGGER = Logger.getLogger(JmsHelper.class.getName());

    public EmbeddedJMS startServer() throws Exception {
        EmbeddedJMS jmsServer = new EmbeddedJMS();
        jmsServer.setConfiguration(getCoreConfiguration());
        jmsServer.setJmsConfiguration(getJmsConfiguration());
        jmsServer.start();

        LOGGER.info("Started embedded JMS server");

        return jmsServer;
    }

    public void stopServer(EmbeddedJMS jmsServer) throws Exception {
        jmsServer.stop();

        LOGGER.info("Stopped embedded JMS server");
    }

    private Configuration getCoreConfiguration() {
        Configuration configuration = new ConfigurationImpl();
        configuration.setPersistenceEnabled(false);
        configuration.setJournalDirectory("target/data/journal");
        configuration.setSecurityEnabled(false);
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(NettyAcceptorFactory.class.getName()));
        configuration.getConnectorConfigurations().put("connector",
                new TransportConfiguration(NettyConnectorFactory.class.getName()));

        return configuration;
    }

    private JMSConfiguration getJmsConfiguration() {
        JMSConfiguration jmsConfiguration = new JMSConfigurationImpl();
        jmsConfiguration.getConnectionFactoryConfigurations().add(getConnectionFactoryConfiguration());
        jmsConfiguration.getQueueConfigurations().add(getQueueConfiguration());

        return jmsConfiguration;
    }

    private ConnectionFactoryConfiguration getConnectionFactoryConfiguration() {
        ArrayList<String> connectorNames = new ArrayList<>();
        connectorNames.add("connector");

        return new ConnectionFactoryConfigurationImpl().setName(FACTORY_NAME).setConnectorNames(connectorNames)
                .setBindings(FACTORY_NAME);
    }

    private JMSQueueConfiguration getQueueConfiguration() {
        return new JMSQueueConfigurationImpl().setName(QUEUE_NAME).setDurable(false).setBindings(QUEUE_NAME);
    }

}
