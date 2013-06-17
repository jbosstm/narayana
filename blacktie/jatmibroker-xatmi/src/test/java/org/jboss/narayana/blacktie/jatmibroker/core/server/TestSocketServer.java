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

package org.jboss.narayana.blacktie.jatmibroker.core.server;

import static org.junit.Assert.*;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.core.transport.Message;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author zhfeng
 *
 */
public class TestSocketServer {
    private static final Logger log = LogManager.getLogger(TestSocketServer.class);
    private SocketServer server;
    private int port = 12341;
    
    @Before
    public void setUp() throws Exception {
        Properties prop = new Properties();
        prop.setProperty("blacktie.java.socketserver.port", new StringBuffer().append(port).toString());
        server = SocketServer.getInstance(prop);
        log.info("socket server start");
    }
    
    @After
    public void tearDown() throws Exception {
        log.info("socket server stop");
        SocketServer.discardInstance();
    }

    @Test
    public void test() throws Exception {
        server.register(1, null, null);
        Socket socket = new Socket("localhost", port);
        DataOutputStream outs =  new DataOutputStream(socket.getOutputStream());
        int sid = 1;
        int correlationId = 0;
        int rcode = 0;
        int flags = 0;
        int rval = 0;
        int len = 4;
        
        byte[] toSend = new byte[len];
        toSend[0] = 'a';
        String toReplyTo = "(null)";
        String type = "X_OCTET";
        String subtype = "(null)";
        
        send(outs, sid, correlationId, rcode, toSend, flags, rval, toReplyTo, type, subtype);
        send(outs, sid, correlationId, 1, toSend, 1, 1, "test", type, subtype);
        Message msg = server.receiveMessage(sid, 2000);
        assertTrue(msg != null);
        assertTrue(msg.cd == 0);
        assertTrue(msg.type.equals(type));
        assertTrue(msg.len == len);
        assertTrue(msg.rcode == 0);
        assertTrue(msg.flags == 0);
        assertTrue(msg.subtype == null);
        assertTrue(msg.data[0] == toSend[0]);
        assertTrue(msg.rval == 0);
        assertTrue(msg.replyTo == null);
        
        msg = server.receiveMessage(sid, 2000);
        assertTrue(msg != null);
        assertTrue(msg.rcode == 1);
        
        outs.close();
        socket.close();
        server.unregister(1);
    }

    private void send(DataOutputStream outs, int sid, int correlationId, int rcode, byte[] toSend, int flags, int rval, String toReplyTo, String type, String subtype) throws IOException {
        StringBuffer buffer = new StringBuffer();
        buffer.append(sid).append("\n").append(correlationId).append("\n").append(rcode).append("\n").
        append(toSend.length).append("\n").append(flags).append("\n").
        append(rval).append("\n").append(toReplyTo).append("\n").
        append(type).append("\n").append(subtype).append("\n");
        
        int sendlen = buffer.length() + toSend.length;
        outs.writeInt(sendlen);
        
        outs.write(buffer.toString().getBytes(), 0, buffer.length());
        outs.write(toSend, 0, toSend.length);
    }
}
