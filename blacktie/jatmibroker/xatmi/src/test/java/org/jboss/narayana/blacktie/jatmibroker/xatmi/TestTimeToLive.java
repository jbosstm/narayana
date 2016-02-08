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
package org.jboss.narayana.blacktie.jatmibroker.xatmi;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.RunServer;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

public class TestTimeToLive extends TestCase {
    private static final Logger log = LogManager.getLogger(TestTimeToLive.class);
    private RunServer server = new RunServer();
    private Connection connection;

    public void setUp() throws ConnectionException, ConfigurationException {
        server.serverinit();

        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        connection.close();
        server.serverdone();
    }

    public void test_call_ttl() throws ConnectionException, ConfigurationException {
        log.info("test_call_ttl");

        server.tpadvertiseTTL();

        try {
            log.info("send first message");

            String toSend = "test_call_ttl_1";
            int sendlen = toSend.length() + 1;
            X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
            sendbuf.setByteArray(toSend.getBytes());

            Response rcvbuf = connection.tpcall(RunServer.getServiceNameTTL(), sendbuf, 0);
            fail("Expected TPETIME, got a buffer with rval: " + rcvbuf.getRval());
        } catch (ConnectionException e) {
            if (e.getTperrno() != Connection.TPETIME) {
                fail("Expected TPETIME, got: " + e.getTperrno());
            }
        }

        try {
            log.info("send second message");

            String toSend = "test_call_ttl_2";
            int sendlen = toSend.length() + 1;
            X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
            sendbuf.setByteArray(toSend.getBytes());

            Response rcvbuf = connection.tpcall(RunServer.getServiceNameTTL(), sendbuf, 0);
            fail("Expected TPETIME, got a buffer with rval: " + rcvbuf.getRval());
        } catch (ConnectionException e) {
            if (e.getTperrno() != Connection.TPETIME) {
                fail("Expected TPETIME, got: " + e.getTperrno());
            }
        }

        // try {
        // log.info("wait 30 second for first message process");
        // Thread.sleep(30 * 1000);
        // log.info("wait done");
        // } catch (Exception e) {
        // log.warn("sleep exception " + e);
        // }

        log.info("send third message");

        String toSend = "counter";
        int sendlen = toSend.length() + 1;
        X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray(toSend.getBytes());

        Response rcvbuf = connection.tpcall(RunServer.getServiceNameTTL(), sendbuf, 0);

        assertTrue(rcvbuf != null);
        assertTrue(rcvbuf.getBuffer() != null);
        assertTrue(((X_OCTET) rcvbuf.getBuffer()).getByteArray() != null);
        byte[] received = ((X_OCTET) rcvbuf.getBuffer()).getByteArray();

        log.info("received length is " + received.length);
        String counter = new String(received);
        log.info("get message counter of TTL is " + counter);
        assertTrue(received[0] == '1');
    }
}
