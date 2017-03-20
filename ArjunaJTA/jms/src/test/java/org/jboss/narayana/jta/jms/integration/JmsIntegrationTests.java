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

import com.arjuna.ats.jta.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.*;
import javax.transaction.*;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.CompletableFuture;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class JmsIntegrationTests extends AbstractIntegrationTests {

    @Mock
    private XAResource xaResourceMock;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        try {
            initNarayana();
            initJms();
        } catch (Exception e) {
            throw new IntegrationTestRuntimeException(e.getMessage());
        }
    }

    @After
    public void after() {
        closeResources();
    }

    @Test
    public void testCommit() throws Exception {
        when(xaResourceMock.prepare(any(Xid.class))).thenReturn(XAResource.XA_OK);

        TransactionManager.transactionManager().begin();
        TransactionManager.transactionManager().getTransaction().enlistResource(xaResourceMock);

        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        TextMessage originalMessage = session.createTextMessage("Test " + new Date());

        session.createProducer(queue).send(originalMessage);
        assertNull(messageConsumer.receiveNoWait());

        TransactionManager.transactionManager().commit();

        TextMessage receivedMessage = (TextMessage) messageConsumer.receiveNoWait();
        assertEquals(originalMessage.getText(), receivedMessage.getText());
        verify(xaResourceMock, times(1)).prepare(any(Xid.class));
        verify(xaResourceMock, times(1)).commit(any(Xid.class), anyBoolean());
    }

    @Test
    public void testRollback() throws Exception {
        TransactionManager.transactionManager().begin();
        TransactionManager.transactionManager().getTransaction().enlistResource(xaResourceMock);

        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        TextMessage originalMessage = session.createTextMessage("Test " + new Date());

        session.createProducer(queue).send(originalMessage);
        assertNull(messageConsumer.receiveNoWait());

        TransactionManager.transactionManager().rollback();

        assertNull(messageConsumer.receiveNoWait());
        verify(xaResourceMock, times(1)).rollback(any(Xid.class));
    }

    @Test
    public void testAsyncCommit() throws Exception {
        when(xaResourceMock.prepare(any(Xid.class))).thenReturn(XAResource.XA_OK);

        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        messageConsumer.setMessageListener(new MessageListener() {
            @Override @Transactional
            public void onMessage(Message message) {
                try {
                    TransactionManager.transactionManager().getTransaction().enlistResource(xaResourceMock);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        sendMessage();

        // Adding timeout, wait until transaction has ended
        verify(xaResourceMock, timeout(1_000L).times(1)).prepare(any(Xid.class));
        verify(xaResourceMock, timeout(100L).times(1)).commit(any(Xid.class), anyBoolean());

        // Assert that the message is no more in the queue
        messageConsumer.setMessageListener(null);
        Message receivedMessage = messageConsumer.receiveNoWait();
        assertNull(receivedMessage);
    }

    @Test
    public void testAsyncRollback() throws Exception {
        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        messageConsumer.setMessageListener(new MessageListener() {
            @Override @Transactional
            public void onMessage(Message message) {
                try {
                    TransactionManager.transactionManager().getTransaction().enlistResource(xaResourceMock);
                    // Disable listener to avoid onMessage on redelivery
                    messageConsumer.setMessageListener(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                throw new RuntimeException("Fail - should imply a rollback");
            }
        });

        sendMessage();

        verify(xaResourceMock, timeout(1_000L).times(1)).rollback(any(Xid.class));

        // Assert that the message has been put back in the queue
        Message receivedMessage = messageConsumer.receiveNoWait();
        assertNotNull(receivedMessage);
    }

    private void sendMessage() throws NotSupportedException, SystemException, JMSException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
        TransactionManager.transactionManager().begin();
        Session sendSession = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        TextMessage originalMessage = sendSession.createTextMessage("Test " + new Date());
        sendSession.createProducer(queue).send(originalMessage);
        TransactionManager.transactionManager().commit();
    }

}
