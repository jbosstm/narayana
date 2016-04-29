package org.jboss.narayana.jta.jms;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.XASession;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import javax.transaction.xa.XAResource;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class MessageListenerProxyTest {

    private MessageListenerProxy messageListenerProxy;

    @Mock
    private XASession xaSessionMock;

    @Mock
    private TransactionManager transactionManagerMock;

    @Before
    public void before() throws Exception {
        MockitoAnnotations.initMocks(this);

        when(transactionManagerMock.getTransaction()).thenReturn(mock(Transaction.class));
    }

    @Test
    public void should_onMessage_have_transaction() throws Exception {
        messageListenerProxy = new MessageListenerProxy(message -> {}, xaSessionMock, transactionManagerMock);
        messageListenerProxy.onMessage(Mockito.mock(Message.class));

        verify(transactionManagerMock, times(1)).begin();
        verify(transactionManagerMock, times(1)).commit();
    }

    @Test
    public void should_failing_onMessage_been_rollbacked() throws Exception {
        messageListenerProxy = new MessageListenerProxy(message -> {throw new RuntimeException("FAIL");},
                                                        xaSessionMock, transactionManagerMock);
        messageListenerProxy.onMessage(Mockito.mock(Message.class));

        verify(transactionManagerMock, times(1)).begin();
        verify(transactionManagerMock, times(1)).rollback();
    }
}