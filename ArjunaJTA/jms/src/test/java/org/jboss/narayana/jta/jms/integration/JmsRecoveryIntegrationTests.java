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

import com.arjuna.ats.arjuna.recovery.RecoveryManager;
import com.arjuna.ats.jta.TransactionManager;
import com.arjuna.ats.jta.recovery.XAResourceRecoveryHelper;
import org.jboss.byteman.contrib.bmunit.BMRule;
import org.jboss.byteman.contrib.bmunit.BMUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
