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
package org.jboss.narayana.jta.jms;

import com.arjuna.ats.jta.logging.jtaLogger;

import jakarta.jms.BytesMessage;
import jakarta.jms.Destination;
import jakarta.jms.JMSException;
import jakarta.jms.MapMessage;
import jakarta.jms.Message;
import jakarta.jms.MessageConsumer;
import jakarta.jms.MessageListener;
import jakarta.jms.MessageProducer;
import jakarta.jms.ObjectMessage;
import jakarta.jms.Queue;
import jakarta.jms.QueueBrowser;
import jakarta.jms.Session;
import jakarta.jms.StreamMessage;
import jakarta.jms.TemporaryQueue;
import jakarta.jms.TemporaryTopic;
import jakarta.jms.TextMessage;
import jakarta.jms.Topic;
import jakarta.jms.TopicSubscriber;
import jakarta.jms.XASession;
import jakarta.transaction.Synchronization;
import java.io.Serializable;

/**
 * Proxy session to wrap around provided {@link XASession}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SessionProxy implements Session {

    private final XASession xaSession;

    private final TransactionHelper transactionHelper;
    private boolean sessionCloseScheduled;

    /**
     * @param xaSession XA session that needs to be proxied.
     * @param transactionHelper utility to make transaction resources registration easier.
     */
    public SessionProxy(XASession xaSession, TransactionHelper transactionHelper) {
        this.xaSession = xaSession;
        this.transactionHelper = transactionHelper;
    }

    /**
     * Simply close proxied session if there is no active transaction. Or if transaction exists, delist session's XA resource
     * and register a {@link SessionClosingSynchronization} to close the proxied session.
     * 
     * @throws JMSException
     */
    @Override
    public void close() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        if (transactionHelper.isTransactionAvailable()) {
            sessionCloseScheduled = true;
            
            transactionHelper.deregisterXAResource(xaSession.getXAResource());

            if (jtaLogger.logger.isTraceEnabled()) {
                jtaLogger.logger.trace("Delisted " + xaSession + " XA resource from the transaction");
            }

            Synchronization synchronization = new SessionClosingSynchronization(xaSession);
            transactionHelper.registerSynchronization(synchronization);

            if (jtaLogger.logger.isTraceEnabled()) {
                jtaLogger.logger.trace("Registered synchronization to close the session: " + synchronization);
            }

        } else {
            xaSession.close();
        }
    }

    @Override
    public BytesMessage createBytesMessage() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createMapMessage();
    }

    @Override
    public Message createMessage() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable serializable) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createObjectMessage(serializable);
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String s) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createTextMessage(s);
    }

    @Override
    public boolean getTransacted() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.getTransacted();
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.getAcknowledgeMode();
    }

    @Override
    public void commit() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        xaSession.commit();
    }

    @Override
    public void rollback() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        xaSession.rollback();
    }

    @Override
    public void recover() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        xaSession.recover();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.getMessageListener();
    }

    @Override
    public void setMessageListener(MessageListener messageListener) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        xaSession.setMessageListener(messageListener);
    }

    @Override
    public void run() {
        if (sessionCloseScheduled) {
            throw new RuntimeException("Session is already scheduled to be closed");
        }
        xaSession.run();
    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createProducer(destination);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createConsumer(destination);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String s) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createConsumer(destination, s);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String s, boolean b) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createConsumer(destination, s, b);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createSharedConsumer(topic, sharedSubscriptionName);
    }

    @Override
    public MessageConsumer createSharedConsumer(Topic topic, String sharedSubscriptionName, String messageSelector) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createSharedConsumer(topic, sharedSubscriptionName, messageSelector);
    }

    @Override
    public Queue createQueue(String s) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createQueue(s);
    }

    @Override
    public Topic createTopic(String s) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createTopic(s);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String s) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createDurableSubscriber(topic, s);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String s, String s1, boolean b) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createDurableSubscriber(topic, s, s1, b);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createDurableConsumer(topic, name);
    }

    @Override
    public MessageConsumer createDurableConsumer(Topic topic, String name, String messageSelector, boolean noLocal) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createDurableConsumer(topic, name, messageSelector, noLocal);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return createSharedDurableConsumer(topic, name);
    }

    @Override
    public MessageConsumer createSharedDurableConsumer(Topic topic, String name, String messageSelector) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return createSharedDurableConsumer(topic, name, messageSelector);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String s) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createBrowser(queue, s);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        return xaSession.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String s) throws JMSException {
        if (sessionCloseScheduled) {
            throw new JMSException("Session is already scheduled to be closed");
        }
        xaSession.unsubscribe(s);
    }

}
