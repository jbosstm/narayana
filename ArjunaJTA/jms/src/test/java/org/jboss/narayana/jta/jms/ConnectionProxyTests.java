/*
 * SPDX short identifier: Apache-2.0
 */

package org.jboss.narayana.jta.jms;

import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.Session;
import jakarta.jms.XAConnection;
import jakarta.jms.XASession;
import jakarta.transaction.Synchronization;
import javax.transaction.xa.XAResource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class ConnectionProxyTests {

    @Mock
    private XAConnection xaConnectionMock;

    @Mock
    private Session sessionMock;

    @Mock
    private XASession xaSessionMock;

    @Mock
    private TransactionHelper transactionHelperMock;

    @Mock
    private XAResource xaResourceMock;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldCreateSessionWithoutTransaction() throws Exception {
        when(transactionHelperMock.isTransactionAvailable()).thenReturn(false);
        when(xaConnectionMock.createSession(true, 0)).thenReturn(sessionMock);

        Connection connection = new ConnectionProxy(xaConnectionMock, transactionHelperMock);
        Session session = connection.createSession(true, 0);

        assertThat(session, sameInstance(sessionMock));
        verify(xaConnectionMock, times(1)).createSession(true, 0);
        verify(transactionHelperMock, times(1)).isTransactionAvailable();
    }

    @Test
    public void shouldCreateSessionWithTransaction() throws Exception {
        when(transactionHelperMock.isTransactionAvailable()).thenReturn(true);
        when(xaConnectionMock.createXASession()).thenReturn(xaSessionMock);
        when(xaSessionMock.getXAResource()).thenReturn(xaResourceMock);

        Connection connection = new ConnectionProxy(xaConnectionMock, transactionHelperMock);
        Session session = connection.createSession(true, 0);

        assertThat(session, instanceOf(SessionProxy.class));
        verify(transactionHelperMock, times(1)).isTransactionAvailable();
        verify(transactionHelperMock, times(1)).registerXAResource(xaResourceMock);
        verify(xaConnectionMock, times(1)).createXASession();
        verify(xaSessionMock, times(1)).getXAResource();
    }

    @Test
    public void shouldFailToRegisterSessionResource() throws Exception {
        when(transactionHelperMock.isTransactionAvailable()).thenReturn(true);
        doThrow(new JMSException(null)).when(transactionHelperMock).registerXAResource(any(XAResource.class));
        when(xaConnectionMock.createXASession()).thenReturn(xaSessionMock);
        when(xaSessionMock.getXAResource()).thenReturn(xaResourceMock);

        Connection connection = new ConnectionProxy(xaConnectionMock, transactionHelperMock);

        try {
            connection.createSession(true, 0);
            fail("JMSException was expected");
        } catch (JMSException e) {
            // Expected
        }

        verify(transactionHelperMock, times(1)).isTransactionAvailable();
        verify(transactionHelperMock, times(1)).registerXAResource(xaResourceMock);
        verify(xaConnectionMock, times(1)).createXASession();
        verify(xaSessionMock, times(1)).getXAResource();
        verify(xaSessionMock, times(1)).close();
    }

    @Test
    public void shouldCloseConnectionWithoutTransaction() throws Exception {
        when(transactionHelperMock.isTransactionAvailable()).thenReturn(false);

        Connection connection = new ConnectionProxy(xaConnectionMock, transactionHelperMock);
        connection.close();

        verify(xaConnectionMock, times(1)).close();
        verify(transactionHelperMock, times(1)).isTransactionAvailable();
    }

    @Test
    public void shouldCloseConnectionWithTransaction() throws Exception {
        when(transactionHelperMock.isTransactionAvailable()).thenReturn(true);

        List<Synchronization> synchronizations = new ArrayList<>(1);
        doAnswer(i -> synchronizations.add(i.getArgument(0))).when(transactionHelperMock)
                .registerSynchronization(any(Synchronization.class));

        Connection connection = new ConnectionProxy(xaConnectionMock, transactionHelperMock);
        connection.close();

        // Will check if the correct connection was registered for closing
        synchronizations.get(0).afterCompletion(0);

        verify(transactionHelperMock, times(1)).isTransactionAvailable();
        verify(transactionHelperMock, times(1)).registerSynchronization(any(Synchronization.class));
        verify(xaConnectionMock, times(1)).close();
    }

}