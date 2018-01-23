/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
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
package com.arjuna.wsc.tests.local;

import com.arjuna.wsc11.AsynchronousRegistrationMapper;
import com.arjuna.wsc11.FaultOrResponse;
import org.junit.Assert;
import org.junit.Test;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType;
import org.xmlsoap.schemas.soap.envelope.Fault;

/**
 *
 * @author rmartinc
 */
public class AsynchronousRegistrationMapperTest {

    private static final long WAIT_TIME = 5000L;

    private static final AsynchronousRegistrationMapper mapper = new AsynchronousRegistrationMapper();

    private static class ClientThread implements Runnable {

        private final String message;
        private FaultOrResponse result;

        public ClientThread(String message) {
            this.message = message;
        }

        @Override
        public void run() {
            result = mapper.waitForResponse(message, WAIT_TIME);
        }

        public FaultOrResponse getResult() {
            return result;
        }
    }

    private static class ServerThread implements Runnable {

        private final long millis;
        private final String message;
        private final Object result;

        public ServerThread(String message, long millis, Object result) {
            this.message = message;
            this.millis = millis;
            this.result = result;
        }

        @Override
        public void run() {
            if (millis > 0) {
                try {
                    Thread.sleep(millis);
                } catch (InterruptedException e) {
                    // nothing
                }
            }
            if (result instanceof RegisterResponseType) {
                mapper.assignResponse(message, (RegisterResponseType) result);
            } else {
                mapper.assignFault(message, (Fault) result);
            }
        }
    }

    private void testMapper(String message, long millis, Object result) {
        mapper.addClientMessage(message);
        
        ClientThread client = new ClientThread(message);
        Thread clientThread = new Thread(client);
        Thread serverThread = new Thread(new ServerThread(message, millis, result));
        
        clientThread.start();
        if (millis >= 0) {
            serverThread.start();
        }
        try {
            clientThread.join(WAIT_TIME * 2);
        } catch (InterruptedException e) {
        }
        
        Assert.assertFalse(clientThread.isAlive());
        FaultOrResponse r = client.getResult();
        Assert.assertNotNull(r);
        Assert.assertEquals(0, mapper.size());
        if (millis >= 0 && millis < WAIT_TIME) {
            if (result instanceof RegisterResponseType) {
                Assert.assertTrue(r.isResponse());
                Assert.assertEquals(result, r.getResponse());
            } else {
                Assert.assertTrue(r.isFault());
                Assert.assertEquals(result, r.getFault());
            }
        } else {
            Assert.assertTrue(r.isEmpty());
        }
    }

    @Test
    public void testMapperResponseResponseImmediate() {
        this.testMapper("testMapperResponseResponseImmediate", 0L, new RegisterResponseType());
    }

    @Test
    public void testMapperResponseResponseWait() {
        this.testMapper("testMapperResponseResponseWait", WAIT_TIME / 2, new RegisterResponseType());
    }

    @Test
    public void testMapperResponseResponseNotReceived() {
        this.testMapper("testMapperResponseResponseWait", -1L, new RegisterResponseType());
    }

    @Test
    public void testMapperResponseFaultImmediate() {
        this.testMapper("testMapperResponseFaultImmediate", 0L, new Fault());
    }

    @Test
    public void testMapperResponseFaultWait() {
        this.testMapper("testMapperResponseFaultWait", WAIT_TIME / 2, new Fault());
    }

    @Test
    public void testMapperResponseFaultNotReceived() {
        this.testMapper("testMapperResponseFaultNotReceived", -1L, new Fault());
    }
}
