/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Sender;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

/**
 * @author zhfeng
 *
 */
public class SocketSenderImpl implements Sender {
    private static final Logger log = LogManager.getLogger(SocketSenderImpl.class);
    private String addr;
    private int sid;
    private boolean closed;
    private int pad = 0;
    private Socket socket;
    private DataOutputStream outs;

    SocketSenderImpl(String addr) throws ConnectionException {
        log.debug("creating socket sender ");
        this.addr = addr;
        String[] s = addr.split(":");

        try{
            socket = new Socket(s[0], Integer.parseInt(s[1]));
            sid = Integer.parseInt(s[2]);
            outs =  new DataOutputStream(socket.getOutputStream());
            this.closed = false;
        } catch(Exception e) {
            throw new ConnectionException(Connection.TPEPROTO, "connect to " + addr + " failed with " + e);
        }
        log.debug("create socket sender for " + addr);
    }

    public SocketSenderImpl(Socket endpoint, String replyTo) throws ConnectionException {
        log.debug("create socket sender with receiver endpoint " + endpoint);
        this.socket = endpoint;
        try {
            this.outs = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new ConnectionException(Connection.TPEPROTO, "get stream from endpoint failed with " + e);
        }
        this.addr = replyTo;
        this.closed = false;
        log.debug("create socket sender for " + replyTo);
    }

    public Object getSendTo() {
        return addr;
    }

    public void send(Object replyTo, short rval, int rcode, byte[] data, int len, int correlationId, int flags, int ttl,
            String type, String subtype) throws ConnectionException {
        log.debug("Sending the message");
        if (closed) {
            log.error("Sender closed");
            throw new ConnectionException(Connection.TPEPROTO, "Sender closed");
        }
        if (data == null) {
            data = new byte[1];
            data[0] = 0;
            len = 1;
        }
        String toReplyTo = (String) replyTo;
        if (toReplyTo == null || toReplyTo.isEmpty()) {
            log.debug("Reply to set as null");
            toReplyTo = "(null)";
        }
        if (type == null || type.isEmpty()) {
            log.debug("Type set as null");
            type = "(null)";
        }
        if (subtype == null || subtype.isEmpty()) {
            log.debug("Subtype set as null");
            subtype = "(null)";
        }
        if (len < 1) {
            log.error("Length of buffer must be greater than 0");
            throw new ConnectionException(Connection.TPEINVAL, "Length of buffer must be greater than 0");
        }
        byte[] toSend = new byte[len + pad];
        if (data != null) {
            int min = Math.min(toSend.length, data.length);
            System.arraycopy(data, 0, toSend, 0, min);
        }
        try {
            StringBuffer buffer = new StringBuffer();
            buffer.append(sid).append("\n").append(correlationId).append("\n").append(rcode).append("\n").
            append(toSend.length).append("\n").append(flags).append("\n").
            append(rval).append("\n").append(toReplyTo).append("\n").
            append(type).append("\n").append(subtype).append("\n");

            int sendlen = buffer.length() + toSend.length;
            log.debug("sendlen is " + sendlen);
            //log.debug(buffer);
            //log.info("toSend[0] is " + toSend[0]);

            try {
                outs.writeInt(sendlen);         
                outs.write(buffer.toString().getBytes(), 0, buffer.length());
                outs.write(toSend, 0, toSend.length);
            } catch (SocketException e)  {
                // The socket might be closed by service side
                log.debug("socket send with " + e);
            }

        } catch (IOException e) {
            throw new ConnectionException(Connection.TPEPROTO, "send failed with " + e);
        }

    }

    public void close() throws ConnectionException {
        log.debug("Close called");
        if (closed) {
            throw new ConnectionException(Connection.TPEPROTO, "Sender already closed");
        }
        closed = true;
        try {
            outs.close();
            socket.shutdownOutput();
        } catch (SocketException e) {
        } catch (IOException e) {
            throw new ConnectionException(Connection.TPEPROTO, "close socket failed with " + e);
        }
        log.debug("Sender closed: " + addr);
    }

    public Object getEndpoint() {
        return socket;
    }
}
