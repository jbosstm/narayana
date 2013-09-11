/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.stomp.jms;

import java.io.IOException;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.stomp.ProtocolException;
import org.codehaus.stomp.Stomp;
import org.codehaus.stomp.StompFrame;

/**
 * Represents an individual Stomp subscription
  *
 * @version $Revision: 50 $
 */
public class StompSubscription implements MessageListener {
    public static final String AUTO_ACK = Stomp.Headers.Subscribe.AckModeValues.AUTO;
    public static final String CLIENT_ACK = Stomp.Headers.Subscribe.AckModeValues.CLIENT;
    private static final transient Log log = LogFactory.getLog(StompSubscription.class);
    private final StompSession session;
    private final String subscriptionId;
    private MessageConsumer consumer;
    private Map<String, Object> headers;

    public StompSubscription(StompSession session, String subscriptionId, StompFrame frame) throws JMSException,
            ProtocolException, NamingException {
        this.subscriptionId = subscriptionId;
        this.session = session;
        this.headers = frame.getHeaders();
        this.consumer = session.createConsumer(headers);
        this.consumer.setMessageListener(this);
    }

    public void close() throws JMSException {
        consumer.close();
    }

    public void onMessage(Message message) {
        String destinationName = (String) headers.get(Stomp.Headers.Subscribe.DESTINATION);
        try {
            log.debug("received: " + destinationName + " for: " + message.getObjectProperty("messagereplyto"));
        } catch (JMSException e) {
            log.warn("received: " + destinationName + " with trouble getting the messagereplyto");
        }
        if (message != null) {
            log.debug("Locking session to send a message");
            // Lock the session so that the connection cannot be started before the acknowledge is done
            synchronized (session) {
                // Stop the session before sending the message
                try {
                    session.stop();
                } catch (JMSException e) {
                    log.fatal("Could not stop the connection: " + e, e);
                }
                // Send the message to the server
                log.debug("Sending message: " + session);
                try {
                    session.sendToStomp(message, subscriptionId);
                    // Acknowledge the message for this connection as we know the server has received it now
                    try {
                        log.debug("Acking message: " + session);
                        message.acknowledge();
                        log.debug("Acked message: " + session);
                    } catch (JMSException e) {
                        log.error("Could not acknowledge the message: " + e, e);
                    }
                } catch (IOException e) {
                    log.warn("Could not send to stomp: " + e, e);
                    try {
                        session.recover();
                    } catch (JMSException e1) {
                        log.fatal("Could not recover the session, possible lost message: " + e, e);
                    }
                } catch (JMSException e) {
                    log.warn("Could not convert message to send to stomp: " + e, e);
                    try {
                        session.recover();
                    } catch (JMSException e1) {
                        log.fatal("Could not recover the session, possible lost message: " + e, e);
                    }
                }
            }
        }
    }
}
