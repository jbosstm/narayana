package org.jboss.narayana.jta.jms;

import javax.jms.*;

import static org.jboss.narayana.jta.jms.MessageListenerProxy.isDeclaredTransactional;

public class MessageConsumerProxy implements MessageConsumer {
    private MessageConsumer consumer;
    private XASession xaSession;

    public MessageConsumerProxy(MessageConsumer consumer, XASession xaSession) {
        this.consumer = consumer;
        this.xaSession = xaSession;
    }

    @Override
    public String getMessageSelector() throws JMSException {
        return consumer.getMessageSelector();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return consumer.getMessageListener();
    }

    @Override
    public void setMessageListener(MessageListener listener) throws JMSException {
        if (isDeclaredTransactional(listener)) {
            MessageListenerProxy messageListenerProxy = new MessageListenerProxy(listener, xaSession);
            consumer.setMessageListener(messageListenerProxy);
        } else {
            consumer.setMessageListener(listener);
        }
    }

    @Override
    public Message receive() throws JMSException {
        return consumer.receive();
    }

    @Override
    public Message receive(long timeout) throws JMSException {
        return consumer.receive(timeout);
    }

    @Override
    public Message receiveNoWait() throws JMSException {
        return consumer.receiveNoWait();
    }

    @Override
    public void close() throws JMSException {
        consumer.close();
    }
}