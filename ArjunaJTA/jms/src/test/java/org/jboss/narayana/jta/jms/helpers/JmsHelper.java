/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms.helpers;

import java.util.ArrayList;

import org.apache.activemq.artemis.api.core.TransportConfiguration;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMAcceptorFactory;
import org.apache.activemq.artemis.core.remoting.impl.invm.InVMConnectorFactory;
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
        configuration.getAcceptorConfigurations().add(new TransportConfiguration(InVMAcceptorFactory.class.getName()));
        configuration.getConnectorConfigurations().put("connector",
                new TransportConfiguration(InVMConnectorFactory.class.getName()));

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