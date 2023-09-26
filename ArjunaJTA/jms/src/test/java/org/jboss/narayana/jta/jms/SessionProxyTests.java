/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import jakarta.jms.Session;
import jakarta.jms.XASession;
import jakarta.transaction.Synchronization;
import javax.transaction.xa.XAResource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SessionProxyTests {

    @Mock
    private XASession xaSessionMock;

    @Mock
    private XAResource xaResourceMock;

    @Mock
    private TransactionHelper transactionHelperMock;

    private Session session;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        session = new SessionProxy(xaSessionMock, transactionHelperMock);
    }

    @Test
    public void shouldCloseSession() throws Exception {
        when(transactionHelperMock.isTransactionAvailable()).thenReturn(true);
        when(xaSessionMock.getXAResource()).thenReturn(xaResourceMock);

        List<Synchronization> synchronizations = new ArrayList<>(1);
        doAnswer(i -> synchronizations.add(i.getArgument(0))).when(transactionHelperMock)
                .registerSynchronization(any(Synchronization.class));

        session.close();

        // Will check if the correct session was registered for closing
        synchronizations.get(0).afterCompletion(0);

        verify(transactionHelperMock, times(1)).isTransactionAvailable();
        verify(transactionHelperMock, times(1)).deregisterXAResource(xaResourceMock);
        verify(transactionHelperMock, times(1)).registerSynchronization(any(SessionClosingSynchronization.class));
        verify(xaSessionMock, times(1)).close();
    }

}