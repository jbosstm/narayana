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

import java.util.Arrays;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.RunServer;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

public class TestTPCall extends TestCase {
    private static final Logger log = LogManager.getLogger(TestTPCall.class);
    private RunServer server = new RunServer();
    private Connection connection;

    public void setUp() throws ConnectionException, ConfigurationException {
        log.info("TestTPCall::setUp");
        server.serverinit();

        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        log.info("TestTPCall::tearDown");
        connection.close();
        server.serverdone();
    }

    public void test_tpcall_unknown_service() throws ConnectionException, ConfigurationException {
        log.info("TestTPCall::test_tpcall_unknown_service");

        String message = "test_tpcall_unknown_service";
        int sendlen = message.length() + 1;
        X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray("test_tpcall_unknown_service".getBytes());

        try {
            Response rcvbuf = connection.tpcall("UNKNOWN_SERVICE", sendbuf, 0);
            fail("Expected TPENOENT, got a buffer with rval: " + rcvbuf.getRval());
        } catch (ConnectionException e) {
            if (e.getTperrno() != Connection.TPENOENT) {
                fail("Expected TPENOENT, got: " + e.getTperrno());
            }
        }
    }

    public void test_tpcall_x_octet() throws ConnectionException, ConfigurationException {
        log.info("TestTPCall::test_tpcall_x_octet");
        server.tpadvertisetpcallXOctet();

        String toSend = "test_tpcall_x_octet";
        int sendlen = toSend.length() + 1;
        X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray(toSend.getBytes());

        Response rcvbuf = connection.tpcall(RunServer.getServiceNametpcallXOctet(), sendbuf, 0);
        assertTrue(rcvbuf != null);
        assertTrue(rcvbuf.getBuffer() != null);
        assertTrue(((X_OCTET) rcvbuf.getBuffer()).getByteArray() != null);
        byte[] received = ((X_OCTET) rcvbuf.getBuffer()).getByteArray();
        byte[] expected = new byte[received.length];
        System.arraycopy("tpcall_x_octet".getBytes(), 0, expected, 0, "tpcall_x_octet".getBytes().length);
        assertTrue(Arrays.equals(received, expected));
    }

    public void test_tpcall_x_octet_after_delay() throws ConnectionException, InterruptedException, ConfigurationException {
        log.info("TestTPCall::test_tpcall_x_octet_after_delay");
        server.tpadvertisetpcallXOctet();
        Thread.currentThread().sleep(3000);
        String toSend = "test_tpcall_x_octet";
        int sendlen = toSend.length() + 1;
        X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray(toSend.getBytes());

        Response rcvbuf = connection.tpcall(RunServer.getServiceNametpcallXOctet(), sendbuf, 0);
        assertTrue(rcvbuf != null);
        assertTrue(rcvbuf.getBuffer() != null);
        assertTrue(((X_OCTET) rcvbuf.getBuffer()).getByteArray() != null);
        byte[] received = ((X_OCTET) rcvbuf.getBuffer()).getByteArray();
        byte[] expected = new byte[received.length];
        System.arraycopy("tpcall_x_octet".getBytes(), 0, expected, 0, "tpcall_x_octet".getBytes().length);
        assertTrue(Arrays.equals(received, expected));
    }

    public void test_tpcall_x_common() throws ConnectionException, ConfigurationException {
        log.info("TestTPCall::test_tpcall_x_common");
        server.tpadvertisetpcallXCommon();

        X_COMMON dptr = (X_COMMON) connection.tpalloc("X_COMMON", "deposit");

        dptr.setLong("acct_no", 12345678);
        dptr.setShort("amount", (short) 50);

        Response rcvbuf = connection.tpcall(RunServer.getServiceNametpcallXCommon(), dptr, 0);
        assertTrue(rcvbuf.getRcode() == 22);
        byte[] received = ((X_OCTET) rcvbuf.getBuffer()).getByteArray();
        byte[] expected = new byte[received.length];
        System.arraycopy("tpcall_x_common".getBytes(), 0, expected, 0, "tpcall_x_common".getBytes().length);
        assertTrue(Arrays.equals(received, expected));
    }

    public void test_tpcall_x_c_type() throws ConnectionException, ConfigurationException {
        log.info("TestTPCall::test_tpcall_x_c_type");
        server.tpadvertisetpcallXCType();

        X_C_TYPE aptr = (X_C_TYPE) connection.tpalloc("X_C_TYPE", "acct_info");

        aptr.setLong("acct_no", 12345678);
        aptr.setByteArray("name", "TOM".getBytes());

        float[] foo = new float[2];
        foo[0] = 1.1F;
        foo[1] = 2.2F;
        aptr.setFloatArray("foo", foo);

        double[] balances = new double[2];
        balances[0] = 1.1;
        balances[1] = 2.2;
        aptr.setDoubleArray("balances", balances);

        Response rcvbuf = connection.tpcall(RunServer.getServiceNametpcallXCType(), aptr, Connection.TPNOCHANGE);
        assertTrue(rcvbuf.getRcode() == 23);
        byte[] received = ((X_OCTET) rcvbuf.getBuffer()).getByteArray();
        byte[] expected = new byte[received.length];
        System.arraycopy("tpcall_x_c_type".getBytes(), 0, expected, 0, "tpcall_x_c_type".getBytes().length);
        assertTrue(Arrays.equals(received, expected));
    }
}
