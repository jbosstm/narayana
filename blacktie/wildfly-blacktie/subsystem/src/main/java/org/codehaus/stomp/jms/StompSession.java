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
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.stomp.ProtocolException;
import org.codehaus.stomp.Stomp;
import org.codehaus.stomp.StompFrame;

/**
 * Represents a logical session (a parallel unit of work) within a Stomp connection
  *
 * @version $Revision: 61 $
 */
public class StompSession {
    private final ProtocolConverter protocolConverter;
    private final Session session;
    private MessageProducer producer;
    private static Map<String, Destination> temporaryDestinations = new HashMap<String, Destination>();
    private final Map<String, StompSubscription> subscriptions = new ConcurrentHashMap<String, StompSubscription>();
    private List<String> created = new ArrayList<String>();
    private Connection connection;
    private InitialContext initialContext;
    private static final Log log = LogFactory.getLog(StompSession.class);

    public StompSession(InitialContext initialContext, ProtocolConverter protocolConverter, Session session,
            Connection connection) throws JMSException {
        this.initialContext = initialContext;
        this.protocolConverter = protocolConverter;
        this.session = session;
        this.connection = connection;
        this.producer = session.createProducer(null);
    }

    public ProtocolConverter getProtocolConverter() {
        return protocolConverter;
    }

    public void close() throws JMSException {
        Iterator<StompSubscription> iterator = subscriptions.values().iterator();
        try {
            while (iterator.hasNext()) {
                iterator.next().close();
            }
        } finally {
            subscriptions.clear();
            connection.close();
        }
    }

    public void sendToJms(StompFrame command) throws JMSException, ProtocolException, NamingException,
            UnsupportedEncodingException {
        Map headers = command.getHeaders();
        String destinationName = (String) headers.remove(Stomp.Headers.Send.DESTINATION);
        Message message = convertFrame(command);
        Destination destination = convertDestination(destinationName, false);

        int deliveryMode = getDeliveryMode(headers);
        int priority = getPriority(headers);
        long timeToLive = getTimeToLive(headers);

        producer.send(destination, message, deliveryMode, priority, timeToLive);
        log.debug("Sent message: " + message.getJMSMessageID());
    }

    public void sendToStomp(Message message, String subscriptionID) throws JMSException, IOException {
        log.debug("Sending to stomp");
        StompFrame frame = convertMessage(message);
        frame.getHeaders().put(Stomp.Headers.Message.SUBSCRIPTION, subscriptionID);
        protocolConverter.sendToStomp(frame);
    }

    public Destination convertDestination(String name, boolean forceNew) throws ProtocolException, JMSException,
            NamingException {
        if (name == null) {
            throw new ProtocolException("No destination is specified!");
        } else if (name.startsWith("/queue/") || name.startsWith("/topic/")) {
            return (Destination) initialContext.lookup("java:" + name);
            // } else if (name.startsWith("/temp-queue/")) {
            // String tempName = name.substring("/temp-queue/".length(), name.length());
            // Destination answer = temporaryDestinations.get(tempName);
            //
            // if (forceNew || answer == null) {
            // return temporaryDestination(tempName, session.createTemporaryQueue());
            // } else {
            // return answer;
            // }
            // } else if (name.startsWith("/temp-topic/")) {
            // String tempName = name.substring("/temp-topic/".length(), name.length());
            // Destination answer = temporaryDestinations.get(tempName);
            // if (forceNew || answer == null) {
            // return temporaryDestination(tempName, session.createTemporaryTopic());
            // } else {
            // return answer;
            // }
        } else {
            throw new ProtocolException("Illegal destination name: [" + name + "] -- StompConnect destinations "
                    + "must begine with one of: /queue/ /topic/ /temp-queue/ /temp-topic/");
        }
    }

    protected String convertDestination(Destination d) throws JMSException {
        if (d == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        if (d instanceof Topic) {
            Topic topic = (Topic) d;
            // if (d instanceof TemporaryTopic) {
            // buffer.append("/temp-topic/");
            // temporaryDestination(topic.getTopicName(), d);
            // } else {
            buffer.append("/topic/");
            // }
            buffer.append(topic.getTopicName());
        } else {
            Queue queue = (Queue) d;
            // if (d instanceof TemporaryQueue) {
            // buffer.append("/temp-queue/");
            // temporaryDestination(queue.getQueueName(), d);
            // } else {
            buffer.append("/queue/");
            // }
            buffer.append(queue.getQueueName());
        }
        return buffer.toString();
    }

    protected int getDeliveryMode(Map headers) throws JMSException {
        Object o = headers.remove(Stomp.Headers.Send.PERSISTENT);
        if (o != null) {
            return "true".equals(o) ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
        } else {
            return producer.getDeliveryMode();
        }
    }

    protected int getPriority(Map headers) throws JMSException {
        Object o = headers.remove(Stomp.Headers.Send.PRIORITY);
        if (o != null) {
            return Integer.parseInt((String) o);
        } else {
            return producer.getPriority();
        }
    }

    protected long getTimeToLive(Map headers) throws JMSException {
        Object o = headers.remove(Stomp.Headers.Send.EXPIRATION_TIME);
        if (o != null) {
            return Long.parseLong((String) o);
        } else {
            return producer.getTimeToLive();
        }
    }

    protected void copyStandardHeadersFromMessageToFrame(Message message, StompFrame command) throws JMSException {
        final Map headers = command.getHeaders();
        headers.put(Stomp.Headers.Message.DESTINATION, convertDestination(message.getJMSDestination()));
        headers.put(Stomp.Headers.Message.MESSAGE_ID, message.getJMSMessageID());

        if (message.getJMSCorrelationID() != null) {
            headers.put(Stomp.Headers.Message.CORRELATION_ID, message.getJMSCorrelationID());
        }
        headers.put(Stomp.Headers.Message.EXPIRATION_TIME, "" + message.getJMSExpiration());

        if (message.getJMSRedelivered()) {
            headers.put(Stomp.Headers.Message.REDELIVERED, "true");
        }
        headers.put(Stomp.Headers.Message.PRORITY, "" + message.getJMSPriority());

        if (message.getJMSReplyTo() != null) {
            headers.put(Stomp.Headers.Message.REPLY_TO, convertDestination(message.getJMSReplyTo()));
        }
        headers.put(Stomp.Headers.Message.TIMESTAMP, "" + message.getJMSTimestamp());

        if (message.getJMSType() != null) {
            headers.put(Stomp.Headers.Message.TYPE, message.getJMSType());
        }

        // now lets add all the message headers
        Enumeration names = message.getPropertyNames();
        while (names.hasMoreElements()) {
            String name = (String) names.nextElement();
            Object value = message.getObjectProperty(name);
            headers.put(name, value);
        }
    }

    protected void copyStandardHeadersFromFrameToMessage(StompFrame command, Message msg) throws JMSException,
            ProtocolException, NamingException {
        final Map headers = new HashMap(command.getHeaders());

        // the standard JMS headers
        msg.setJMSCorrelationID((String) headers.remove(Stomp.Headers.Send.CORRELATION_ID));

        Object o = headers.remove(Stomp.Headers.Send.TYPE);
        if (o != null) {
            msg.setJMSType((String) o);
        }

        o = headers.remove(Stomp.Headers.Send.REPLY_TO);
        if (o != null) {
            msg.setJMSReplyTo(convertDestination((String) o, false));
        }

        // now the general headers
        for (Iterator iter = headers.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            String name = (String) entry.getKey();
            Object value = entry.getValue();
            msg.setObjectProperty(name, value);
        }
    }

    protected Message convertFrame(StompFrame command) throws JMSException, UnsupportedEncodingException, ProtocolException,
            NamingException {
        final Map headers = command.getHeaders();
        final Message msg;
        if (headers.containsKey(Stomp.Headers.CONTENT_LENGTH)) {
            headers.remove(Stomp.Headers.CONTENT_LENGTH);
            BytesMessage bm = session.createBytesMessage();
            bm.writeBytes(command.getContent());
            msg = bm;
        } else {
            String text = new String(command.getContent(), "UTF-8");
            msg = session.createTextMessage(text);
        }
        copyStandardHeadersFromFrameToMessage(command, msg);
        return msg;
    }

    protected StompFrame convertMessage(Message message) throws JMSException, UnsupportedEncodingException {
        StompFrame command = new StompFrame();
        command.setAction(Stomp.Responses.MESSAGE);
        Map headers = new HashMap(25);
        command.setHeaders(headers);

        copyStandardHeadersFromMessageToFrame(message, command);

        if (message instanceof TextMessage) {
            TextMessage msg = (TextMessage) message;
            command.setContent(msg.getText().getBytes("UTF-8"));
        } else if (message instanceof BytesMessage) {

            BytesMessage msg = (BytesMessage) message;
            byte[] data = new byte[(int) msg.getBodyLength()];
            msg.readBytes(data);

            headers.put(Stomp.Headers.CONTENT_LENGTH, "" + data.length);
            command.setContent(data);
        }
        return command;
    }

    public Message receiveFromJms(String destinationName, Map headers) throws JMSException, ProtocolException, NamingException {
        long ttl = getTimeToLive(headers);
        log.trace("Consuming message - ttl=" + ttl);
        Destination destination = convertDestination(destinationName, true);
        MessageConsumer consumer = session.createConsumer(destination);
        Message message;
        if (ttl > 0) {
            message = consumer.receive(ttl);
        } else {
            message = consumer.receive();
        }
        if (message != null) {
            // As this is a dequeue, automatically acknowledge the message
            message.acknowledge();
        }
        consumer.close();
        log.trace("Received message: " + message);
        return message;
    }

    public MessageConsumer createConsumer(Map headers) throws ProtocolException, JMSException, NamingException {
        String selector = (String) headers.remove(Stomp.Headers.Subscribe.SELECTOR);
        String destinationName = (String) headers.get(Stomp.Headers.Subscribe.DESTINATION);
        Destination destination = convertDestination(destinationName, true);

        MessageConsumer consumer;
        if (destination instanceof Topic) {
            boolean noLocal = false;
            String value = (String) headers.get(Stomp.Headers.Subscribe.NO_LOCAL);
            if (value != null && "true".equalsIgnoreCase(value)) {
                noLocal = true;
            }

            String subscriberName = (String) headers.get(Stomp.Headers.Subscribe.DURABLE_SUBSCRIPTION_NAME);
            if (subscriberName != null) {
                consumer = session.createDurableSubscriber((Topic) destination, subscriberName, selector, noLocal);
            } else {
                consumer = session.createConsumer(destination, selector, noLocal);
            }
        } else {
            consumer = session.createConsumer(destination, selector);
        }
        return consumer;
    }

    public StompSubscription subscribe(String subscriptionId, StompFrame command) throws ProtocolException, JMSException,
            NamingException {
        if (subscriptions.size() > 0) {
            throw new ProtocolException("This connection already has a subscription");
        }

        StompSubscription subscription = (StompSubscription) subscriptions.get(subscriptionId);
        if (subscription != null) {
            throw new ProtocolException("There already is a subscription for: " + subscriptionId
                    + ". Either use unique subscription IDs or do not create multiple subscriptions for the same destination");
        } else {
            subscription = new StompSubscription(this, subscriptionId, command);
            subscriptions.put(subscriptionId, subscription);
        }
        return subscription;
    }

    public void unsubscribe(String subscriptionId) throws ProtocolException, JMSException {
        StompSubscription subscription = (StompSubscription) subscriptions.remove(subscriptionId);
        if (subscription == null) {
            throw new ProtocolException("Cannot unsubscribe as mo subscription exists for id: " + subscriptionId);
        }
        subscription.close();
    }

    public void start() throws JMSException {
        log.debug("Starting session: " + session);
        connection.start();
    }

    public void stop() throws JMSException {
        log.debug("Stopping session: " + session);
        connection.stop();
    }

    public void recover() throws JMSException {
        log.debug("Recovering session: " + session);
        session.recover();
    }
}
