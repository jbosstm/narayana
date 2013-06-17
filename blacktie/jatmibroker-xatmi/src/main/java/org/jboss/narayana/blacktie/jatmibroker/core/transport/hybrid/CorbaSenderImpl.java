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
package org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Sender;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;

import AtmiBroker.EndpointQueue;
import AtmiBroker.EndpointQueueHelper;

public class CorbaSenderImpl implements Sender {
    private static final Logger log = LogManager.getLogger(CorbaSenderImpl.class);
    private EndpointQueue queue;
    private String name;
    private int pad = 0;
    private boolean closed;

    CorbaSenderImpl(org.omg.CORBA.Object serviceFactoryObject, String name) {
        this.queue = EndpointQueueHelper.narrow(serviceFactoryObject);
        this.name = name;
        log.debug("Corba sender for: " + name + " created");
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
            len = 1;
        }
        String toReplyTo = (String) replyTo;
        if (toReplyTo == null) {
            log.trace("Reply to set as null");
            toReplyTo = "";
        }
        if (type == null) {
            log.trace("Type set as null");
            type = "";
        }
        if (subtype == null) {
            log.trace("Subtype set as null");
            subtype = "";
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
        log.debug("Preparing to send the message");
        queue.send(toReplyTo, rval, rcode, toSend, toSend.length, correlationId, flags, type, subtype);
        log.debug("Sent the message");
    }

    public void close() throws ConnectionException {
        log.debug("Close called");
        if (closed) {
            throw new ConnectionException(Connection.TPEPROTO, "Sender already closed");
        }
        closed = true;
        log.debug("Sender closed: " + name);
    }

    public Object getSendTo() {
        return name;
    }

    public Object getEndpoint() {
      return queue;
    }
}
