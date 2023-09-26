/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms.integration;

import com.arjuna.ats.jta.TransactionManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.Ignore;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;
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

}