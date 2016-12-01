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

import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicSubscriber;
import javax.jms.XASession;
import javax.transaction.Synchronization;
import java.io.Serializable;

/**
 * Proxy session to wrap around provided {@link XASession}.
 *
 * @author <a href="mailto:gytis@redhat.com">Gytis Trikleris</a>
 */
public class SessionProxy implements Session {

    private final XASession xaSession;

    private final TransactionHelper transactionHelper;

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
     * and register {@link SessionClosingSynchronization}.
     * 
     * @throws JMSException
     */
    @Override
    public void close() throws JMSException {
        if (transactionHelper.isTransactionAvailable()) {
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
        return xaSession.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() throws JMSException {
        return xaSession.createMapMessage();
    }

    @Override
    public Message createMessage() throws JMSException {
        return xaSession.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() throws JMSException {
        return xaSession.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable serializable) throws JMSException {
        return xaSession.createObjectMessage(serializable);
    }

    @Override
    public StreamMessage createStreamMessage() throws JMSException {
        return xaSession.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() throws JMSException {
        return xaSession.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String s) throws JMSException {
        return xaSession.createTextMessage(s);
    }

    @Override
    public boolean getTransacted() throws JMSException {
        return xaSession.getTransacted();
    }

    @Override
    public int getAcknowledgeMode() throws JMSException {
        return xaSession.getAcknowledgeMode();
    }

    @Override
    public void commit() throws JMSException {
        xaSession.commit();
    }

    @Override
    public void rollback() throws JMSException {
        xaSession.rollback();
    }

    @Override
    public void recover() throws JMSException {
        xaSession.recover();
    }

    @Override
    public MessageListener getMessageListener() throws JMSException {
        return xaSession.getMessageListener();
    }

    @Override
    public void setMessageListener(MessageListener messageListener) throws JMSException {
        xaSession.setMessageListener(messageListener);
    }

    @Override
    public void run() {
        xaSession.run();
    }

    @Override
    public MessageProducer createProducer(Destination destination) throws JMSException {
        return xaSession.createProducer(destination);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination) throws JMSException {
        return xaSession.createConsumer(destination);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String s) throws JMSException {
        return xaSession.createConsumer(destination, s);
    }

    @Override
    public MessageConsumer createConsumer(Destination destination, String s, boolean b) throws JMSException {
        return xaSession.createConsumer(destination, s, b);
    }

    @Override
    public Queue createQueue(String s) throws JMSException {
        return xaSession.createQueue(s);
    }

    @Override
    public Topic createTopic(String s) throws JMSException {
        return xaSession.createTopic(s);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String s) throws JMSException {
        return xaSession.createDurableSubscriber(topic, s);
    }

    @Override
    public TopicSubscriber createDurableSubscriber(Topic topic, String s, String s1, boolean b) throws JMSException {
        return xaSession.createDurableSubscriber(topic, s, s1, b);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) throws JMSException {
        return xaSession.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String s) throws JMSException {
        return xaSession.createBrowser(queue, s);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() throws JMSException {
        return xaSession.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() throws JMSException {
        return xaSession.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String s) throws JMSException {
        xaSession.unsubscribe(s);
    }

}
