/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms.integration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;

import jakarta.jms.MessageConsumer;
import jakarta.jms.Session;
import jakarta.jms.TextMessage;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
@RunWith(BMUnitRunner.class)
public class JmsRecoveryIntegrationTests extends AbstractIntegrationTests {

    @Mock
    private XAResource xaResourceMock;

    @Mock
    private XAResourceRecoveryHelper xaResourceRecoveryHelperMock;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        try {
            initNarayana();
            initJms();
            initNarayanaRecovery();
            initJmsRecovery();
        } catch (Exception e) {
            throw new IntegrationTestRuntimeException(e.getMessage());
        }
        registerRecoveryHelper(xaResourceRecoveryHelperMock);
    }

    @After
    public void after() {
        closeResources();
    }

    @Test
    @BMRule(name = "Fail before commit", targetClass = "com.arjuna.ats.arjuna.coordinator.BasicAction",
            targetMethod = "phase2Commit", targetLocation = "ENTRY", helper = "org.jboss.narayana.jta.jms.helpers.BytemanHelper",
            action = "incrementCommitsCounter(); failFirstCommit($0.get_uid());")
    public void shouldCrashBeforeCommitAndRecover() throws Exception {
        List<Xid> mockXids = new ArrayList<>();
        when(xaResourceMock.prepare(any(Xid.class))).then(i -> {
            mockXids.add((Xid) i.getArguments()[0]);
            return XAResource.XA_OK;
        });
        when(xaResourceMock.recover(anyInt())).then(i -> mockXids.toArray(new Xid[mockXids.size()]));
        when(xaResourceRecoveryHelperMock.getXAResources()).thenReturn(new XAResource[] { xaResourceMock });

        TransactionManager.transactionManager().begin();
        TransactionManager.transactionManager().getTransaction().enlistResource(xaResourceMock);

        Session session = connection.createSession(true, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer messageConsumer = session.createConsumer(queue);
        TextMessage originalMessage = session.createTextMessage("Test " + new Date());

        session.createProducer(queue).send(originalMessage);

        try {
            TransactionManager.transactionManager().commit();
            fail("Commit failure was expected");
        } catch (Exception e) {
            // Expected
        }

        verify(xaResourceMock, times(1)).prepare(any(Xid.class));
        verify(xaResourceMock, times(0)).commit(any(Xid.class), anyBoolean());
        assertNull(messageConsumer.receiveNoWait());

        RecoveryManager.manager(RecoveryManager.DIRECT_MANAGEMENT).scan();

        verify(xaResourceMock, times(1)).commit(any(Xid.class), anyBoolean());
        TextMessage receivedMessage = (TextMessage) messageConsumer.receiveNoWait();
        assertEquals(originalMessage.getText(), receivedMessage.getText());
    }

}