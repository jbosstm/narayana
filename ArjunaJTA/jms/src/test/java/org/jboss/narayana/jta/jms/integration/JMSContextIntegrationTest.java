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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.jms.Connection;
import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSConsumer;
import jakarta.jms.JMSContext;
import jakarta.jms.MessageConsumer;
import jakarta.jms.Queue;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
import jakarta.jms.XAConnectionFactory;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 * @author <a href="mailto:tom.jenkinson@redhat.com">Tom Jenkinson</a>
 */
public class JMSContextIntegrationTest {

    @Mock
    private XAResource xaResourceMock;
    private static final JmsHelper JMS_HELPER = new JmsHelper();

    protected EmbeddedJMS jmsServer;

    protected Queue queue;
    private ConnectionFactoryProxy connectionFactory;


    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        try {
            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, null)
                    .setObjectStoreDir("target/tx-object-store");
            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "communicationStore")
                    .setObjectStoreDir("target/tx-object-store");
            BeanPopulator.getNamedInstance(ObjectStoreEnvironmentBean.class, "stateStore")
                    .setObjectStoreDir("target/tx-object-store");

            arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");

            jmsServer = JMS_HELPER.startServer();
            queue = (Queue) jmsServer.lookup(JmsHelper.QUEUE_NAME);

            connectionFactory = new ConnectionFactoryProxy(
                    (XAConnectionFactory) jmsServer.lookup(JmsHelper.FACTORY_NAME),
                    new TransactionHelperImpl(TransactionManager.transactionManager()));
        } catch (Exception e) {
            throw new IntegrationTestRuntimeException(e.getMessage());
        }
    }

    @After
    public void after() {
        if (jmsServer != null) {
            try {
                JMS_HELPER.stopServer(jmsServer);
            } catch (Throwable t) {
                // Ignore
            }
            jmsServer = null;
        }
    }

    @Test
    public void testCommit() throws Exception {
        when(xaResourceMock.prepare(any(Xid.class))).thenReturn(XAResource.XA_OK);

        TransactionManager.transactionManager().begin();
        TransactionManager.transactionManager().getTransaction().enlistResource(xaResourceMock);

        JMSContext context = connectionFactory.createContext();
        JMSConsumer messageConsumer = context.createConsumer(queue);
        TextMessage originalMessage = context.createTextMessage("Test " + new Date());

        context.createProducer().send(queue, originalMessage);
        assertNull(messageConsumer.receiveNoWait());

        TransactionManager.transactionManager().commit();

        TextMessage receivedMessage = (TextMessage) messageConsumer.receiveNoWait();
        assertEquals(originalMessage.getText(), receivedMessage.getText());
        verify(xaResourceMock, times(1)).prepare(any(Xid.class));
        verify(xaResourceMock, times(1)).commit(any(Xid.class), anyBoolean());
    }
}