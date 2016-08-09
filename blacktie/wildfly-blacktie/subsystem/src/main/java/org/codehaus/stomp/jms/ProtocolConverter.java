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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;
import javax.transaction.xa.Xid;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.stomp.ProtocolException;
import org.codehaus.stomp.Stomp;
import org.codehaus.stomp.StompFrame;
import org.codehaus.stomp.StompFrameError;
import org.codehaus.stomp.tcp.TcpTransport;
import org.jboss.narayana.rest.bridge.inbound.InboundBridge;
import org.jboss.narayana.rest.bridge.inbound.InboundBridgeManager;

/**
 * A protocol switch between JMS and Stomp
 *
 * @author <a href="http://people.apache.org/~jstrachan/">James Strachan</a>
 * @author <a href="http://hiramchirino.com">chirino</a>
 */
public class ProtocolConverter {
    private static final transient Log log = LogFactory.getLog(ProtocolConverter.class);
    private final TcpTransport tcpTransport;
    private ConnectionFactory noneXAConnectionFactory;
    private XAConnectionFactory xaConnectionFactory;
    private StompSession noneXaSession;
    private Map<Xid, StompSession> xaSessions = new ConcurrentHashMap<Xid, StompSession>();

    private TransactionManager tm;
    private String login;
    private String passcode;
    private String clientId;
    private InitialContext initialContext;
    private boolean closed;

    public ProtocolConverter(InitialContext initialContext, ConnectionFactory connectionFactory,
            XAConnectionFactory xaConnectionFactory, TcpTransport outputHandler) throws NamingException {
        this.noneXAConnectionFactory = connectionFactory;
        this.xaConnectionFactory = xaConnectionFactory;
        this.tcpTransport = outputHandler;
        this.initialContext = initialContext;
        tm = (TransactionManager) initialContext.lookup("java:/TransactionManager");
        outputHandler.setProtocolConverter(this);
    }

    public void close() {
        if (!closed) {
            try {
                // First close the XA sessions
                Iterator<StompSession> iterator = xaSessions.values().iterator();
                while (iterator.hasNext()) {
                    StompSession xaSession = iterator.next();
                    try {
                        xaSession.close();
                    } catch (JMSException e) {
                        log.error("Could not close XASession: " + e);
                    }
                }
                xaSessions.clear();
                xaSessions = null;
            } finally {
                if (noneXaSession != null) {
                    try {
                        noneXaSession.close();
                    } catch (JMSException e) {
                        log.error("Could not close none XASession: " + e);
                    }
                    noneXaSession = null;
                }
            }
            closed = true;
        }
    }

    /**
     * Process a Stomp Frame
     *
     * @throws IOException
     */
    public void onStompFrame(StompFrame command) throws IOException {
        try {
            if (log.isDebugEnabled()) {
                log.debug(">>>> " + command.getAction() + " headers: " + command.getHeaders());
            }

            if (command.getClass() == StompFrameError.class) {
                throw ((StompFrameError) command).getException();
            }

            if (closed) {
                log.error("Connection is closed: " + this);
                throw new ProtocolException("Connection is closed: " + this);
            }

            String action = command.getAction();
            if (action.startsWith(Stomp.Commands.SEND)) {
                onStompSend(command);
            } else if (action.startsWith(Stomp.Commands.RECEIVE)) {
                onStompReceive(command);
            } else if (action.startsWith(Stomp.Commands.ACK)) {
                onStompAck(command);
            } else if (action.startsWith(Stomp.Commands.SUBSCRIBE)) {
                onStompSubscribe(command);
            } else if (action.startsWith(Stomp.Commands.UNSUBSCRIBE)) {
                onStompUnsubscribe(command);
            } else if (action.startsWith(Stomp.Commands.CONNECT)) {
                onStompConnect(command);
            } else if (action.startsWith(Stomp.Commands.DISCONNECT)) {
                onStompDisconnect(command);
            } else {
                throw new ProtocolException("Unknown STOMP action: " + action);
            }

            if (log.isDebugEnabled()) {
                log.debug(">>>> " + command.getAction() + " headers: " + command.getHeaders() + " >> done");
            }
        } catch (Exception e) {
            log.debug("Caught an exception: ", e);
            // Let the stomp client know about any protocol errors.
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter stream = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));
            e.printStackTrace(stream);
            stream.close();

            Map<String, Object> headers = new HashMap<String, Object>();
            headers.put(Stomp.Headers.Error.MESSAGE, e.getMessage());

            final String receiptId = (String) command.getHeaders().get(Stomp.Headers.RECEIPT_REQUESTED);
            if (receiptId != null) {
                headers.put(Stomp.Headers.Response.RECEIPT_ID, receiptId);
            }

            StompFrame errorMessage = new StompFrame(Stomp.Responses.ERROR, headers, baos.toByteArray());
            sendToStomp(errorMessage);

            // TODO need to do anything else? Should we close the connection?
        }
    }

    // Implemenation methods
    // -------------------------------------------------------------------------
    protected void onStompConnect(StompFrame command) throws IOException, JMSException {
        if (noneXaSession != null) {
            throw new ProtocolException("Already connected.");
        }

        Map<String, Object> headers = command.getHeaders();
        login = (String) headers.get(Stomp.Headers.Connect.LOGIN);
        passcode = (String) headers.get(Stomp.Headers.Connect.PASSCODE);
        clientId = (String) headers.get(Stomp.Headers.Connect.CLIENT_ID);

        Connection noneXaConnection;
        if (login != null) {
            noneXaConnection = noneXAConnectionFactory.createConnection(login, passcode);
        } else {
            noneXaConnection = noneXAConnectionFactory.createConnection();
        }
        if (clientId != null) {
            noneXaConnection.setClientID(clientId);
        }

        noneXaConnection.start();

        Session session = noneXaConnection.createSession(false, Session.CLIENT_ACKNOWLEDGE);
        if (log.isDebugEnabled()) {
            log.debug("Created session with ack mode: " + session.getAcknowledgeMode());
        }
        this.noneXaSession = new StompSession(initialContext, this, session, noneXaConnection);

        Map<String, Object> responseHeaders = new HashMap<String, Object>();

        responseHeaders.put(Stomp.Headers.Connected.SESSION, clientId);
        String requestId = (String) headers.get(Stomp.Headers.Connect.REQUEST_ID);
        if (requestId == null) {
            // TODO legacy
            requestId = (String) headers.get(Stomp.Headers.RECEIPT_REQUESTED);
        }
        if (requestId != null) {
            // TODO legacy
            responseHeaders.put(Stomp.Headers.Connected.RESPONSE_ID, requestId);
            responseHeaders.put(Stomp.Headers.Response.RECEIPT_ID, requestId);
        }

        StompFrame sc = new StompFrame();
        sc.setAction(Stomp.Responses.CONNECTED);
        sc.setHeaders(responseHeaders);
        sendToStomp(sc);
    }

    protected void onStompDisconnect(StompFrame command) throws JMSException, InterruptedException, IOException,
    URISyntaxException {
        checkConnected();
        close();
        sendResponse(command);
    }

    protected void onStompSend(StompFrame command) throws IllegalStateException, SystemException, JMSException,
    NamingException, IOException {
        checkConnected();

        Map<String, Object> headers = command.getHeaders();
        String xid = (String) headers.get("messagexid");

        if (xid != null) {
            if (xid.startsWith("IOR")) {
                log.error("OTS Transaction Not Supported");
            } else if (xid.startsWith("http")) {
                log.trace("RTS Transaction was propagated: " + xid);

                String enlistmentUrl = xid;
                final InboundBridge inboundBridge = InboundBridgeManager.getInstance().createInboundBridge(enlistmentUrl);
                log.trace("Start inboundBridge");
                inboundBridge.start();
                StompSession session = getXASession(inboundBridge.getXid());
                session.sendToJms(command);
                inboundBridge.stop();
                log.trace("Stop inboundBridge");
            } else {
                log.error(xid + " is not OTS or RTS transaction");
            }
        } else {
            String destinationName = (String) headers.get(Stomp.Headers.Send.DESTINATION);
            log.trace("WAS NULL XID: " + destinationName);
            noneXaSession.sendToJms(command);
            log.trace("Sent to JMS: " + destinationName);
        }
        sendResponse(command);
        log.trace("Sent Response");
    }

    protected void onStompReceive(StompFrame command) throws IllegalStateException, SystemException, JMSException,
    NamingException, IOException {
        checkConnected();

        Map<String, Object> headers = command.getHeaders();
        String destinationName = (String) headers.remove(Stomp.Headers.Send.DESTINATION);
        String xid = (String) headers.get("messagexid");
        Message msg = null;
        StompSession session = null;
        if (xid != null) {
            if (xid.startsWith("IOR")){
                log.error("OTS Transaction Not Supported");
            } else if (xid.startsWith("http")){
                log.trace("RTS Transaction was propagated: " + xid);

                String enlistmentUrl = xid;
                final InboundBridge inboundBridge = InboundBridgeManager.getInstance().createInboundBridge(enlistmentUrl);
                log.trace("Start inboundBridge");
                inboundBridge.start();
                session = getXASession(inboundBridge.getXid());
                msg = session.receiveFromJms(destinationName, headers);
                inboundBridge.stop();
                log.trace("Stop inboundBridge");

            } else {
                log.error(xid + " is not OTS or RTS transaction");
            }
        } else {
            log.trace("WAS NULL XID");
            session = noneXaSession;
            msg = session.receiveFromJms(destinationName, headers);

            log.trace("Received from JMS");
        }

        StompFrame sf;
        if (msg == null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintWriter stream = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8));
            stream.print("No messages available");
            stream.close();

            Map<String, Object> eheaders = new HashMap<String, Object>();
            eheaders.put(Stomp.Headers.Error.MESSAGE, "timeout");

            sf = new StompFrame(Stomp.Responses.ERROR, eheaders, baos.toByteArray());
        } else {
            // Don't use sendResponse since it uses Stomp.Responses.RECEIPT as the action
            // which only allows zero length message bodies, Stomp.Responses.MESSAGE is correct:
            sf = session.convertMessage(msg);
        }

        if (headers.containsKey(Stomp.Headers.RECEIPT_REQUESTED))
            sf.getHeaders().put(Stomp.Headers.Response.RECEIPT_ID, headers.get(Stomp.Headers.RECEIPT_REQUESTED));

        sendToStomp(sf);
    }

    protected void onStompSubscribe(StompFrame command) throws JMSException, NamingException, IOException {
        checkConnected();

        Map<String, Object> headers = command.getHeaders();

        String subscriptionId = (String) headers.get(Stomp.Headers.Subscribe.ID);
        if (subscriptionId == null) {
            subscriptionId = createSubscriptionId(headers);
        }

        // We know this is going to be none-XA as the XA receive is handled in onStompReceive
        noneXaSession.subscribe(subscriptionId, command);
        sendResponse(command);
    }

    protected void onStompUnsubscribe(StompFrame command) throws JMSException, IOException {
        checkConnected();
        Map<String, Object> headers = command.getHeaders();

        String destinationName = (String) headers.get(Stomp.Headers.Unsubscribe.DESTINATION);
        String subscriptionId = (String) headers.get(Stomp.Headers.Unsubscribe.ID);

        if (subscriptionId == null) {
            if (destinationName == null) {
                throw new ProtocolException("Must specify the subscriptionId or the destination you are unsubscribing from");
            }
            subscriptionId = createSubscriptionId(headers);
        }

        noneXaSession.unsubscribe(subscriptionId);
        sendResponse(command);
    }

    protected void onStompAck(StompFrame command) throws JMSException, IOException {
        checkConnected();

        // We know this is none XA
        StompSession session = noneXaSession;
        if (session == null) {
            throw new ProtocolException("None XA session was not stopped");
        }

        log.debug("Locking session to restart it");
        // Dont allow the session to deliver any more messages until after we have acked the clients ack
        synchronized (session) {
            // Allow another message to be consumed
            session.resume();
            sendResponse(command);
        }
        log.debug("Session started");
    }

    protected void checkConnected() throws ProtocolException {
        if (noneXaSession == null) {
            throw new ProtocolException("Not connected.");
        }
    }

    /**
     * Auto-create a subscription ID using the destination
     */
    protected String createSubscriptionId(Map<String, Object> headers) {
        return "/subscription-to/" + headers.get(Stomp.Headers.Subscribe.DESTINATION);
    }

    protected StompSession getXASession(Xid xid) throws JMSException {
        StompSession xaSession = xaSessions.get(xid);
        if (xaSession == null) {

            XAConnection xaConnection;
            if (login != null) {
                xaConnection = xaConnectionFactory.createXAConnection(login, passcode);
            } else {
                xaConnection = xaConnectionFactory.createXAConnection();
            }
            if (clientId != null) {
                xaConnection.setClientID(clientId);
            }
            xaConnection.start();
            Session session = xaConnection.createXASession();
            if (log.isDebugEnabled()) {
                log.debug("Created XA session");
            }
            xaSession = new StompSession(initialContext, this, session, xaConnection);
            log.trace("Created XA Session");
            xaSessions.put(xid, xaSession);
        } else {
            log.trace("Returned existing XA session");
        }
        return xaSession;
    }

    protected void sendResponse(StompFrame command) throws IOException {
        final String receiptId = (String) command.getHeaders().get(Stomp.Headers.RECEIPT_REQUESTED);
        // A response may not be needed.
        if (receiptId != null) {
            StompFrame sc = new StompFrame();
            sc.setAction(Stomp.Responses.RECEIPT);
            sc.setHeaders(new HashMap<String, Object>(1));
            sc.getHeaders().put(Stomp.Headers.Response.RECEIPT_ID, receiptId);
            sendToStomp(sc);
        } else {
            log.trace("No receipt required");
        }
    }

    protected void sendToStomp(StompFrame frame) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("<<<< " + frame.getAction() + " headers: " + frame.getHeaders());
        }
        log.debug("Locking output handler to ensure that we don't mux signals");
        synchronized (tcpTransport) {
            tcpTransport.onStompFrame(frame);
        }
        if (log.isDebugEnabled()) {
            log.debug("<<<< " + frame.getAction() + " headers: " + frame.getHeaders() + " << done");
        }
    }

}
