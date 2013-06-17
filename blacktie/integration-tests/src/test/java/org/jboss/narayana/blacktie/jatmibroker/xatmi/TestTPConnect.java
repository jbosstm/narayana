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

public class TestTPConnect extends TestCase {
    private static final Logger log = LogManager.getLogger(TestTPConnect.class);
    private RunServer server = new RunServer();
    private Connection connection;
    private int sendlen;
    private X_OCTET sendbuf;
    private Session cd;
    private Session cd2;

    public void setUp() throws ConnectionException, ConfigurationException {
        log.info("TestTPConnect::setUp");
        server.serverinit();
        server.tpadvertiseTestTPConnect();

        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();

        byte[] message = "connect".getBytes();
        sendlen = message.length + 1;
        sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray(message);
        cd = null;
        cd2 = null;
        log.info("TestTPConnect::setUp done");
    }

    public void tearDown() throws ConnectionException {
        log.info("TestTPConnect::tearDown");
        // Do local work
        if (cd != null) {
            cd.tpdiscon();
            cd = null;
        }
        if (cd2 != null) {
            cd2.tpdiscon();
            cd2 = null;
        }

        connection.close();
        server.serverdone();
        log.info("TestTPConnect::tearDown done");
    }

    public void test_tpconnect() throws ConnectionException {
        log.info("test_tpconnect: " + RunServer.getServiceNameTestTPConnect());
        cd = connection.tpconnect(RunServer.getServiceNameTestTPConnect(), sendbuf, Connection.TPRECVONLY);
        assertTrue(cd != null);
    }

    public void test_tpconnect_double_connect() throws ConnectionException {
        log.info("test_tpconnect_double_connect: " + RunServer.getServiceNameTestTPConnect());
        cd = connection.tpconnect(RunServer.getServiceNameTestTPConnect(), sendbuf, Connection.TPRECVONLY);
        cd2 = connection.tpconnect(RunServer.getServiceNameTestTPConnect(), sendbuf, Connection.TPRECVONLY);
        assertTrue(cd != null);
        assertTrue(cd2 != null);
        assertTrue(cd != cd2);
        assertTrue(!cd.equals(cd2));
    }

    public void test_tpconnect_nodata() throws ConnectionException {
        log.info("test_tpconnect_nodata: " + RunServer.getServiceNameTestTPConnect());
        cd = connection.tpconnect(RunServer.getServiceNameTestTPConnect(), null, Connection.TPRECVONLY);
        assertTrue(cd != null);
    }
}
