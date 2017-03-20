package org.jboss.narayana.jta.jms;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.XASession;
import javax.transaction.Transactional;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class MessageConsumerProxyTest {

    private MessageConsumerProxy messageConsumerProxy;

    @Mock
    private MessageConsumer messageConsumerMock;

    @Mock
    private XASession xaSessionMock;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        messageConsumerProxy = new MessageConsumerProxy(messageConsumerMock, xaSessionMock);
    }

    @Test
    public void should_set_MessageListenerProxy_when_Transactional() throws Exception {

        MessageListener listener = new MessageListener() {
            @Transactional
            public void onMessage(Message message) {}
        };
        messageConsumerProxy.setMessageListener(listener);

        verify(messageConsumerMock, never()).setMessageListener(listener);
        verify(messageConsumerMock, times(1)).setMessageListener(any(MessageListenerProxy.class));

    }

    @Test
    public void should_set_genuine_MessageListener_when_not_Transactional() throws Exception {

        MessageListener listener = new MessageListener() {
            public void onMessage(Message message) {}
        };
        messageConsumerProxy.setMessageListener(listener);

        verify(messageConsumerMock, times(1)).setMessageListener(listener);

    }

}