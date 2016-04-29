package org.jboss.narayana.jta.jms;

import com.arjuna.ats.jta.logging.jtaLogger;

import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.XASession;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.Transactional;
import java.lang.reflect.Method;

public class MessageListenerProxy implements MessageListener {

    private MessageListener messageListener;
    private TransactionManager transactionManager;
    private XASession xaSession;

    static boolean isDeclaredTransactional(MessageListener messageListener) {
        try {
            if (messageListener == null) {
                return false;
            }

            Class<? extends MessageListener> listenerClass = messageListener.getClass();
            if (listenerClass.isAnnotationPresent(Transactional.class)) {
                return true;
            } else {
                Method onMessageMethod = listenerClass.getDeclaredMethod("onMessage", Message.class);
                return onMessageMethod.isAnnotationPresent(Transactional.class);
            }
        } catch (NoSuchMethodException e) {
            // Should not occur, as messageListener is an instance of javax.jms.MessageListener
        }
        return false;
    }

    public MessageListenerProxy(MessageListener messageListener, XASession session) {
        this(messageListener, session, com.arjuna.ats.jta.TransactionManager.transactionManager());
    }

    /**
     * Mainly for tests
     */
    MessageListenerProxy(MessageListener messageListener, XASession session, TransactionManager transactionManager ) {
        this.messageListener = messageListener;
        this.transactionManager = transactionManager;
        this.xaSession = session;
    }

    @Override
    public void onMessage(Message jmsMessage) {
        try {
            transactionManager.begin();
            Transaction transaction = transactionManager.getTransaction();
            transaction.enlistResource(xaSession.getXAResource());

            messageListener.onMessage(jmsMessage);

            transactionManager.commit();
        } catch (Exception e) {
            jtaLogger.logger.error(e);
            try {
                transactionManager.rollback();
            } catch (SystemException se) {
                jtaLogger.logger.warn("Problem on transaction rollback", e);
            }
        }

    }


}