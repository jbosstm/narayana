package org.jboss.narayana.blacktie.jatmibroker.xatmi.mdb;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.jms.BytesMessage;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.JtsTransactionImple;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.BlackTieService;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

/**
 * All BlackTie MDB services should extend this class so that they can be advertised
 */
public abstract class MDBBlacktieService extends BlackTieService implements MessageListener {

    /**
     * A logger to log the output to.
     */
    private static final Logger log = LogManager.getLogger(MDBBlacktieService.class);

    /**
     * This will allow the connection factory to be failed
     * 
     * @param name
     * 
     * @throws ConfigurationException
     */
    protected MDBBlacktieService(String name) throws ConfigurationException {
        super(name);
    }

    /**
     * The onMessage method formats the JMS received bytes message into a format understood by the XATMI API.
     * 
     * @param message The message received wrapping an XATMI invocation
     */
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public void onMessage(Message message) {
        try {
            String serviceName = null;
            Destination jmsDestination = message.getJMSDestination();
            if (jmsDestination instanceof Queue) {
                serviceName = ((Queue) jmsDestination).getQueueName();
            } else {
                serviceName = ((Topic) jmsDestination).getTopicName();
            }
            serviceName = serviceName.substring(serviceName.indexOf('_') + 1);
            log.trace(serviceName);
            if (JtsTransactionImple.hasTransaction()) {
                throw new ConnectionException(Connection.TPEPROTO,
                        "Blacktie services must not be called with a transactional context: " + getName() + ":" + serviceName);
            }
            BytesMessage bytesMessage = ((BytesMessage) message);
            org.jboss.narayana.blacktie.jatmibroker.core.transport.Message toProcess = convertFromBytesMessage(bytesMessage);
            log.debug("SERVER onMessage: transaction control ior: " + toProcess.control);
            processMessage(serviceName, toProcess);
            log.debug("Processed message");
        } catch (Throwable t) {
            log.error("Could not service the request", t);
        }
    }

    private static org.jboss.narayana.blacktie.jatmibroker.core.transport.Message convertFromBytesMessage(BytesMessage message)
            throws JMSException {
        String controlIOR = message.getStringProperty("messagecontrol");
        String replyTo = message.getStringProperty("messagereplyto");
        int len = (int) message.getBodyLength();
        String serviceName = message.getStringProperty("servicename");
        int flags = new Integer(message.getStringProperty("messageflags"));
        int cd = new Integer(message.getStringProperty("messagecorrelationId"));

        String type = message.getStringProperty("messagetype");
        String subtype = message.getStringProperty("messagesubtype");
        log.debug("type: " + type + " subtype: " + subtype);

        org.jboss.narayana.blacktie.jatmibroker.core.transport.Message toProcess = new org.jboss.narayana.blacktie.jatmibroker.core.transport.Message();
        toProcess.type = type;
        toProcess.subtype = subtype;
        toProcess.replyTo = replyTo;
        toProcess.serviceName = serviceName;
        toProcess.flags = flags;
        toProcess.cd = cd;
        toProcess.len = len;
        if (toProcess.type == "") {
            toProcess.data = null;
        } else {
            toProcess.data = new byte[toProcess.len];
            message.readBytes(toProcess.data);
        }
        toProcess.control = controlIOR;
        return toProcess;
    }
}
