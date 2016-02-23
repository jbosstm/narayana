/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Receiver;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

public class StompReceiverImpl implements Receiver {
    private static final Logger log = LogManager.getLogger(StompReceiverImpl.class);
    private int timeout = 0;
    private boolean closed;
    private String destinationName;
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.Message pendingMessage;
    private boolean ignoreSingleReceipt;

    public StompReceiverImpl(String serviceName, boolean conversational, String type, Properties properties)
            throws ConnectionException, IOException {
        String qtype = "/queue/";
        if (type != null) {
            qtype = "/" + type + "/";
        }

        if (conversational) {
            this.destinationName = qtype + "BTC_" + serviceName;
        } else {
            this.destinationName = qtype + "BTR_" + serviceName;
        }

        String host = (String) properties.get("StompConnectHost");
        int port = Integer.parseInt((String) properties.get("StompConnectPort"));
        String username = (String) properties.get("StompConnectUsr");
        String password = (String) properties.get("StompConnectPwd");

        this.socket = StompManagement.connect(host, port, username, password);
        this.outputStream = socket.getOutputStream();
        this.inputStream = socket.getInputStream();

        org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.Message message = new org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.Message();
        message.setCommand("SUBSCRIBE");

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("destination", destinationName);
        headers.put("receipt", destinationName);
        headers.put("ack", "client");
        message.setHeaders(headers);

        StompManagement.send(message, outputStream);
        org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.Message receive = StompManagement.receive(socket,
                inputStream);
        if (receive.getCommand().equals("ERROR")) {
            log.error(new String(receive.getBody()));
            throw new ConnectionException(Connection.TPENOENT, new String(receive.getBody()));
        } else if (receive.getCommand().equals("MESSAGE")) {
            // TODO remove when moving to HQStomp
            log.trace("Received a message rather than a receipt");
            this.pendingMessage = receive;
            ignoreSingleReceipt = true;
        }
        log.debug("Created a consumer on: " + destinationName + " with timeout: " + timeout);
    }

    public Object getReplyTo() throws ConnectionException {
        return null;
    }

    public Message receive(long flagsIn) throws ConnectionException {
        log.debug("Receiving from: " + destinationName);
        org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.Message receive = pendingMessage;
        pendingMessage = null;
        try {
            if (receive == null) {
                receive = StompManagement.receive(socket, inputStream);
                // TODO remove when moving to HQStomp
                if (receive != null && receive.getCommand().equals("RECEIPT") && ignoreSingleReceipt) {
                    ignoreSingleReceipt = false;
                    receive = StompManagement.receive(socket, inputStream);
                }
                log.debug("Received from: " + destinationName);
            }
            if (receive == null) {
                log.debug("No message to return: " + destinationName);
                return null;
            }
            if (!receive.getCommand().equals("MESSAGE")) {
                throw new ConnectionException(Connection.TPESYSTEM, "Internal error, received unexpected receipt");
            }
            Message convertFromBytesMessage = convertFromBytesMessage(receive);
            convertFromBytesMessage.setOutputStream(outputStream);
            convertFromBytesMessage.setMessageId(receive.getHeaders().get("message-id"));
            log.debug("Returning message from: " + destinationName);
            return convertFromBytesMessage;
        } catch (ConnectionException e) {
            throw e;
        } catch (Exception t) {
            log.debug("Couldn't receive the message: " + t.getMessage(), t);
            throw new ConnectionException(Connection.TPESYSTEM, "Couldn't receive the message", t);
        }
    }

    public void close() throws ConnectionException {
        log.debug("close");
        if (closed) {
            throw new ConnectionException(Connection.TPEPROTO, "Sender already closed");
        }
        try {
            log.debug("closing socket: " + socket);
            StompManagement.close(socket, outputStream, inputStream);
            inputStream.close();
            log.debug("closed input stream: " + inputStream);
            socket.close();
            log.debug("closed socket: " + socket);
            closed = true;
        } catch (Throwable t) {
            log.debug("consumer could not be closed");
            throw new ConnectionException(Connection.TPESYSTEM, "Could not delete the queue", t);
        }
    }

    public org.jboss.narayana.blacktie.jatmibroker.core.transport.Message convertFromBytesMessage(
            org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.Message receive) {
        log.trace("convertFromBytesMessage");
        String controlIOR = receive.getHeaders().get("messagecontrol");
        log.trace("got messagecontrol: " + controlIOR);
        String replyTo = receive.getHeaders().get("messagereplyto");
        log.trace("got messagereplyto: " + replyTo);
        int len = Integer.parseInt(receive.getHeaders().get("content-length"));
        log.trace("gotcontent-length: " + len);
        String serviceName = receive.getHeaders().get("servicename");
        log.trace("got servicename: " + serviceName);
        int flags = new Integer(receive.getHeaders().get("messageflags"));
        log.trace("got messageflags: " + flags);
        int cd = new Integer(receive.getHeaders().get("messagecorrelationId"));
        log.trace("got messagecorrelationId: " + cd);

        String type = receive.getHeaders().get("messagetype");
        log.trace("got messagetype: " + type);
        String subtype = receive.getHeaders().get("messagesubtype");
        log.trace("got messagesubtype: " + subtype);
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
            toProcess.data = receive.getBody();
        }
        toProcess.control = controlIOR;
        return toProcess;
    }

    public int determineTimeout(long flags) throws ConnectionException {
        throw new ConnectionException(Connection.TPEPROTO, "Tried to retrieve the cd on mom receiver");
    }

    public int getCd() throws ConnectionException {
        throw new ConnectionException(Connection.TPEPROTO, "Tried to retrieve the cd on mom receiver");
    }

    public Object getEndpoint() throws ConnectionException {
        return socket;
    }
}
