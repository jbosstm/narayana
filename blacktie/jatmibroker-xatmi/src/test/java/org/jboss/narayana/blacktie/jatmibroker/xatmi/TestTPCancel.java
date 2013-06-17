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

public class TestTPCancel extends TestCase {
    private static final Logger log = LogManager.getLogger(TestTPCancel.class);
    private RunServer server = new RunServer();
    private Connection connection;

    public void setUp() throws ConnectionException, ConfigurationException {
        server.serverinit();
        server.tpadvertiseTestTPCancel();

        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        connection.close();
        server.serverdone();
    }

    public void test_tpcancel() throws ConnectionException, ConfigurationException {
        log.info("test_tpcancel");
        byte[] message = "cancel".getBytes();
        int sendlen = message.length + 1;
        X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray(message);

        int cd = connection.tpacall(RunServer.getServiceNameTestTPCancel(), sendbuf, 0);
        assertTrue(cd != -1);
        assertTrue(cd != 0);

        // CANCEL THE REQUEST
        int cancelled = connection.tpcancel(cd);
        assertTrue(cancelled != -1);

        // FAIL TO RETRIEVE THE RESPONSE
        try {
            Response valToTest = connection.tpgetrply(cd, 0);
            assertNull("Could get a reply", valToTest);
            fail("Method completed ok");
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPEBADDESC);
        }
    }

    public void test_tpcancel_noreply() throws ConnectionException, ConfigurationException {
        log.info("test_tpcancel_noreply");
        byte[] message = "cancel".getBytes();
        int sendlen = message.length + 1;
        X_OCTET sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray(message);

        int cd = connection.tpacall(RunServer.getServiceNameTestTPCancel(), sendbuf, Connection.TPNOREPLY);
        assertTrue(cd == 0);

        // CANCEL THE REQUEST
        try {
            int cancelled = connection.tpcancel(cd);
            fail("Could cancel a TPNOREPLY tpacall");
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPEBADDESC);
        }
    }

    // 8.5
    public void test_tpcancel_baddesc() {
        log.info("test_tpcancel_baddesc");
        // CANCEL THE REQUEST
        try {
            int cancelled = connection.tpcancel(2);
            fail("Could cancel a TPNOREPLY tpacall");
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPEBADDESC);
        }
    }
}
