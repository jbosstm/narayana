package org.jboss.narayana.jta.jms;

import com.arjuna.ats.jta.logging.jtaLogger;

import javax.jms.BytesMessage;
import javax.jms.ConnectionMetaData;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.JMSRuntimeException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.StreamMessage;
import javax.jms.TemporaryQueue;
import javax.jms.TemporaryTopic;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.XAJMSContext;
import javax.transaction.Synchronization;
import java.io.Serializable;

public class JMSContextProxy implements JMSContext {
    private final XAJMSContext xaContext;
    private final TransactionHelper transactionHelper;
    private boolean contextCloseScheduled;

    public static JMSContext wrapContext(XAJMSContext xaContext, TransactionHelper transactionHelper) {
        return new JMSContextProxy(xaContext, transactionHelper);
    }

    private JMSContextProxy (XAJMSContext xaContext, TransactionHelper transactionHelper) {
        this.xaContext = xaContext;
        this.transactionHelper = transactionHelper;
        try {
            if (transactionHelper.isTransactionAvailable()) {
                transactionHelper.registerXAResource(xaContext.getXAResource());
            }
        } catch (JMSException e) {
            JMSRuntimeException jmsre = new JMSRuntimeException("Could not register the resource");
            jmsre.addSuppressed(e);
            throw jmsre;
        }
    }

    @Override
    public JMSContext createContext(int i) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createContext(i);
    }

    @Override
    public JMSProducer createProducer() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createProducer();
    }

    @Override
    public String getClientID() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.getClientID();
    }

    @Override
    public void setClientID(String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.setClientID(s);
    }

    @Override
    public ConnectionMetaData getMetaData() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.getMetaData();
    }

    @Override
    public ExceptionListener getExceptionListener() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.getExceptionListener();
    }

    @Override
    public void setExceptionListener(ExceptionListener exceptionListener) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.setExceptionListener(exceptionListener);
    }

    @Override
    public void start() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.start();
    }

    @Override
    public void stop() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.stop();
    }

    @Override
    public void setAutoStart(boolean b) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.setAutoStart(b);
    }

    @Override
    public boolean getAutoStart() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.getAutoStart();
    }

    @Override
    public void close() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        try {
            if (transactionHelper.isTransactionAvailable()) {
                transactionHelper.deregisterXAResource(xaContext.getXAResource());

                if (jtaLogger.logger.isTraceEnabled()) {
                    jtaLogger.logger.trace("Delisted " + xaContext + " XA resource from the transaction");
                }

                Synchronization synchronization = new SessionClosingSynchronization(xaContext);
                transactionHelper.registerSynchronization(synchronization);

                if (jtaLogger.logger.isTraceEnabled()) {
                    jtaLogger.logger.trace("Registered synchronization to close the session: " + synchronization);
                }
                contextCloseScheduled = true;
            } else {
                xaContext.close();
            }
        } catch (JMSException e) {
            JMSRuntimeException jmsre = new JMSRuntimeException("Could not close the context");
            jmsre.addSuppressed(e);
            throw jmsre;
        }
    }

    @Override
    public BytesMessage createBytesMessage() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createBytesMessage();
    }

    @Override
    public MapMessage createMapMessage() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createMapMessage();
    }

    @Override
    public Message createMessage() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createMessage();
    }

    @Override
    public ObjectMessage createObjectMessage() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createObjectMessage();
    }

    @Override
    public ObjectMessage createObjectMessage(Serializable serializable) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createObjectMessage();
    }

    @Override
    public StreamMessage createStreamMessage() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createStreamMessage();
    }

    @Override
    public TextMessage createTextMessage() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createTextMessage();
    }

    @Override
    public TextMessage createTextMessage(String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createTextMessage(s);
    }

    @Override
    public boolean getTransacted() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.getTransacted();
    }

    @Override
    public int getSessionMode() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.getSessionMode();
    }

    @Override
    public void commit() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.commit();
    }

    @Override
    public void rollback() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.rollback();
    }

    @Override
    public void recover() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.recover();
    }

    @Override
    public JMSConsumer createConsumer(Destination destination) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createConsumer(destination);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createConsumer(destination, s);
    }

    @Override
    public JMSConsumer createConsumer(Destination destination, String s, boolean b) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createConsumer(destination, s, b);
    }

    @Override
    public Queue createQueue(String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createQueue(s);
    }

    @Override
    public Topic createTopic(String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createTopic(s);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createDurableConsumer(topic, s);
    }

    @Override
    public JMSConsumer createDurableConsumer(Topic topic, String s, String s1, boolean b) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createDurableConsumer(topic, s, s1, b);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createSharedDurableConsumer(topic, s);
    }

    @Override
    public JMSConsumer createSharedDurableConsumer(Topic topic, String s, String s1) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createSharedDurableConsumer(topic, s, s1);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createSharedConsumer(topic, s);
    }

    @Override
    public JMSConsumer createSharedConsumer(Topic topic, String s, String s1) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createSharedConsumer(topic, s, s1);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createBrowser(queue);
    }

    @Override
    public QueueBrowser createBrowser(Queue queue, String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createBrowser(queue, s);
    }

    @Override
    public TemporaryQueue createTemporaryQueue() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createTemporaryQueue();
    }

    @Override
    public TemporaryTopic createTemporaryTopic() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        return xaContext.createTemporaryTopic();
    }

    @Override
    public void unsubscribe(String s) {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.unsubscribe(s);
    }

    @Override
    public void acknowledge() {
        if (contextCloseScheduled) {
            throw new JMSRuntimeException("This JMSContextProxy is already scheduled to be closed");
        }
        xaContext.acknowledge();
    }
}
