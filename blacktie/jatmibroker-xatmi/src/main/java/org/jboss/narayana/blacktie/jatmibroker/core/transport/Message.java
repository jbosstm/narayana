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
package org.jboss.narayana.blacktie.jatmibroker.core.transport;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.StompManagement;

public class Message {
    private static final Logger log = LogManager.getLogger(Message.class);

    public Object replyTo;
    public byte[] data;
    public int len;
    public int flags;
    public String control;
    public short rval;
    public int rcode;
    public int cd;
    public String serviceName;
    public String type;
    public String subtype;

    private String messageId;
    private OutputStream outputStream;

    public void ack() throws IOException {
        log.debug("Acking message: " + messageId);
        org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.Message ack = new org.jboss.narayana.blacktie.jatmibroker.core.transport.hybrid.stomp.Message();
        ack.setCommand("ACK");
        Map<String, String> headers = new HashMap<String, String>();
        headers.put("message-id", messageId);
        ack.setHeaders(headers);
        StompManagement.send(ack, outputStream);
        log.debug("Acked message: " + messageId);
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }
}
