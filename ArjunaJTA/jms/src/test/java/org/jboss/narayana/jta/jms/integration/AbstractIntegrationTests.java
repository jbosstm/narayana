/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.Queue;
import jakarta.jms.XAConnectionFactory;
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