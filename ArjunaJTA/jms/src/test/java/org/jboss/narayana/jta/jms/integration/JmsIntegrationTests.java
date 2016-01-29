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

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import org.apache.activemq.artemis.jms.server.embedded.EmbeddedJMS;
import org.jboss.narayana.jta.jms.ConnectionFactoryProxy;
import org.jboss.narayana.jta.jms.TransactionHelperImpl;
import org.jboss.narayana.jta.jms.helpers.JmsHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.XAConnectionFactory;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JmsIntegrationTests {

    private static final JmsHelper JMS_HELPER = new JmsHelper();

    @Mock
    private XAResource xaResourceMock;

    private TransactionManager transactionManager;

    private EmbeddedJMS jmsServer;

    private Connection connection;

    private Queue queue;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        arjPropertyManager.getCoreEnvironmentBean().setNodeIdentifier("1");

        transactionManager = com.arjuna.ats.jta.TransactionManager.transactionManager();
        jmsServer = JMS_HELPER.startServer();
        queue = (Queue) jmsServer.lookup(JmsHelper.QUEUE_NAME);

        ConnectionFactory connectionFactory = new ConnectionFactoryProxy(
                (XAConnectionFactory) jmsServer.lookup(JmsHelper.FACTORY_NAME), new TransactionHelperImpl(transactionManager));
        connection = connectionFactory.createConnection();
        connection.start();
    }

    @After
    public void after() throws Exception {
        if (connection != null) {
            connection.close();
            connection = null;
        }

        if (jmsServer != null) {
            JMS_HELPER.stopServer(jmsServer);
            jmsServer = null;
        }
    }

    @Test
    public void testCommit() throws Exception {
        when(xaResourceMock.prepare(any(Xid.class))).thenReturn(XAResource.XA_OK);

        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(xaResourceMock);

        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        MessageProducer messageProducer = session.createProducer(queue);
        TextMessage originalMessage = session.createTextMessage("Test " + new Date());

        messageProducer.send(originalMessage);
        assertNull(messageConsumer.receiveNoWait());

        transactionManager.commit();

        TextMessage receivedMessage = (TextMessage) messageConsumer.receiveNoWait();
        assertEquals(originalMessage.getText(), receivedMessage.getText());
        verify(xaResourceMock, times(1)).prepare(any(Xid.class));
        verify(xaResourceMock, times(1)).commit(any(Xid.class), anyBoolean());
    }

    @Test
    public void testRollback() throws Exception {
        transactionManager.begin();
        transactionManager.getTransaction().enlistResource(xaResourceMock);

        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        MessageProducer messageProducer = session.createProducer(queue);
        TextMessage originalMessage = session.createTextMessage("Test " + new Date());

        messageProducer.send(originalMessage);
        assertNull(messageConsumer.receiveNoWait());

        transactionManager.rollback();

        assertNull(messageConsumer.receiveNoWait());
        verify(xaResourceMock, times(1)).rollback(any(Xid.class));
    }

}
