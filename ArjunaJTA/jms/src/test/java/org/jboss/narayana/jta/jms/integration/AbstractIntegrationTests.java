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
package org.jboss.narayana.jta.jms.integration;

import com.arjuna.ats.arjuna.common.ObjectStoreEnvironmentBean;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.jboss.narayana.jta.jms.ConnectionFactoryProxy;
import org.jboss.narayana.jta.jms.JmsXAResourceRecoveryHelper;
import org.jboss.narayana.jta.jms.TransactionHelperImpl;
import org.jboss.narayana.jta.jms.helpers.JmsHelper;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.XAConnectionFactory;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public abstract class AbstractIntegrationTests {

    private static final JmsHelper JMS_HELPER = new JmsHelper();

    private RecoveryManager recoveryManager;

    protected EmbeddedJMS jmsServer;

    protected Connection connection;

    protected Queue queue;

    protected void closeResources() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Throwable t) {
                // Ignore
            }
            connection = null;
        }

        if (jmsServer != null) {
            try {
                JMS_HELPER.stopServer(jmsServer);
            } catch (Throwable t) {
                // Ignore
            }
            jmsServer = null;
        }

        if (recoveryManager != null) {
            recoveryManager.terminate();
            recoveryManager = null;
        }
    }

    protected void initNarayana() throws Exception {
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, null)
                .setObjectStoreDir("target/tx-object-store");
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore")
                .setObjectStoreDir("target/tx-object-store");
        BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore")
                .setObjectStoreDir("target/tx-object-store");

        arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");
    }

    protected void initNarayanaRecovery() {
        if (recoveryManager != null) {
            throw new IntegrationTestRuntimeException(
                    "Recovery manager is already running");
        }

        final List<String> recoveryExtensions = new ArrayList<>();
        recoveryExtensions.add(AtomicActionRecoveryModule.class.getName());
        recoveryExtensions.add(XARecoveryModule.class.getName());

        recoveryPropertyManager.getRecoveryEnvironmentBean()
                .setRecoveryModuleClassNames(recoveryExtensions);
        recoveryPropertyManager.getRecoveryEnvironmentBean()
                .setRecoveryBackoffPeriod(1);

        recoveryManager =
                RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT);
    }

    protected void initJms() throws Exception {
        jmsServer = JMS_HELPER.startServer();
        queue = (Queue) jmsServer.lookup(JmsHelper.QUEUE_NAME);

        ConnectionFactory connectionFactory = new ConnectionFactoryProxy(
                (XAConnectionFactory) jmsServer.lookup(JmsHelper.FACTORY_NAME),
                new TransactionHelperImpl(TransactionManager.transactionManager()));

        connection = connectionFactory.createConnection();
        connection.start();
    }

    protected void initJmsRecovery() throws Exception {
        registerRecoveryHelper(new JmsXAResourceRecoveryHelper(
                (XAConnectionFactory) jmsServer.lookup(JmsHelper.FACTORY_NAME)));
    }

    protected void registerRecoveryHelper(XAResourceRecoveryHelper helper) {
        recoveryManager.getModules().stream()
                .filter(m -> m instanceof XARecoveryModule).forEach(
                    m -> ((XARecoveryModule) m).addXAResourceRecoveryHelper(helper));
    }

}
