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
package org.jboss.narayana.blacktie.jatmibroker.tx;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.RunServer;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Buffer;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ResponseException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Session;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.TestTPConversation;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class TestRollbackOnly extends TestCase {
    private static final Logger log = LogManager.getLogger(TestRollbackOnly.class);
    private RunServer server = new RunServer();
    private Connection connection;
    private int sendlen;
    private X_OCTET sendbuf;

    public void setUp() throws ConnectionException, ConfigurationException {
        server.serverinit();

        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();

        sendlen = "TestRbkOnly".length() + 1;
        sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray("TestRbkOnly".getBytes());
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        connection.close();
        server.serverdone();
    }

    public void test_tpcall_TPETIME() throws ConnectionException, ConfigurationException {
        log.info("test_tpcall_TPETIME");
        server.tpadvertiseTestRollbackOnlyTpcallTPETIMEService();
        assertTrue(TX.tx_open() == TX.TX_OK);
        assertTrue(TX.tx_begin() == TX.TX_OK);

        try {
            connection.tpcall(RunServer.getServiceNameTestRollbackOnly(), sendbuf, 0);
            fail("Expected e.getTperrno() == Connection.TPETIME");
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPETIME);
        }

        TXINFO txinfo = new TXINFO();
        int inTx = TX.tx_info(txinfo);
        log.info("inTx=" + inTx);
        assertTrue(txinfo.transaction_state == TX.TX_ROLLBACK_ONLY);
        assertTrue(TX.tx_commit() == TX.TX_ROLLBACK);
    }

    public void x_test_tpcall_TPEOTYPE() throws ConnectionException, ConfigurationException {
        log.info("test_tpcall_TPETIME");
        server.tpadvertiseTestTpcallTPEOTYPEService();

        assertTrue(TX.tx_open() == TX.TX_OK);
        assertTrue(TX.tx_begin() == TX.TX_OK);

        try {
            connection.tpcall(RunServer.getServiceNameTestRollbackOnly(), sendbuf, Connection.TPNOCHANGE);
            fail("Expected e.getTperrno() == TPEOTYPE");
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPEOTYPE);
        }

        TXINFO txinfo = new TXINFO();
        int inTx = TX.tx_info(txinfo);
        log.info("inTx=" + inTx);
        assertTrue(txinfo.transaction_state == TX.TX_ROLLBACK_ONLY);
        assertTrue(TX.tx_commit() == TX.TX_ROLLBACK);
    }

    public void test_tpcall_TPESVCFAIL() throws ConnectionException, ConfigurationException {
        log.info("test_tpcall_TPESVCFAIL");
        server.tpadvertiseTestRollbackOnlyTpcallTPESVCFAILService();

        assertTrue(TX.tx_open() == TX.TX_OK);
        assertTrue(TX.tx_begin() == TX.TX_OK);

        try {
            connection.tpcall(RunServer.getServiceNameTestRollbackOnly(), sendbuf, 0);
            fail("Expected e.getTperrno() == TPESVCFAIL");
        } catch (ResponseException e) {
            assertTrue(TestTPConversation.strcmp(e.getReceived(), "test_tpcall_TPESVCFAIL_service") == 0);
            assertTrue(e.getTperrno() == Connection.TPESVCFAIL);
        } catch (ConnectionException e) {
            fail("Expected e.getTperrno() == TPESVCFAIL: " + e.getTperrno());
        }

        TXINFO txinfo = new TXINFO();
        int inTx = TX.tx_info(txinfo);
        log.info("inTx=" + inTx);
        assertTrue(txinfo.transaction_state == TX.TX_ROLLBACK_ONLY);
        assertTrue(TX.tx_commit() == TX.TX_ROLLBACK);
    }

    public void test_tprecv_TPEV_DISCONIMM() throws ConnectionException {
        log.info("test_tprecv_TPEV_DISCONIMM");
        server.tpadvertiseTestRollbackOnlyTprecvTPEVDISCONIMMService();

        assertTrue(TX.tx_open() == TX.TX_OK);
        assertTrue(TX.tx_begin() == TX.TX_OK);

        Session cd = connection.tpconnect(RunServer.getServiceNameTestRollbackOnly2(), sendbuf, Connection.TPSENDONLY);
        cd.tpdiscon();

        TXINFO txinfo = new TXINFO();
        int inTx = TX.tx_info(txinfo);
        log.info("inTx=" + inTx);
        assertTrue(txinfo.transaction_state == TX.TX_ROLLBACK_ONLY);
        assertTrue(TX.tx_commit() == TX.TX_ROLLBACK);
    }

    public void test_tprecv_TPEV_SVCFAIL() throws ConnectionException, ConfigurationException {
        log.info("test_tprecv_TPEV_SVCFAIL");
        server.tpadvertiseTestRollbackOnlyTprecvTPEVSVCFAILService();

        assertTrue(TX.tx_open() == TX.TX_OK);
        assertTrue(TX.tx_begin() == TX.TX_OK);

        Session cd = connection.tpconnect(RunServer.getServiceNameTestRollbackOnly2(), sendbuf, Connection.TPRECVONLY);

        try {
            cd.tprecv(0);
            fail("Expected e.getEvent() == Connection.TPEV_SVCFAIL");
        } catch (ResponseException e) {
            assertTrue(e.getEvent() == Connection.TPEV_SVCFAIL);
            assertTrue(e.getTperrno() == Connection.TPEEVENT);
            Buffer rcvbuf = e.getReceived();
            assertTrue(TestTPConversation.strcmp(rcvbuf, "test_tprecv_TPEV_SVCFAIL_service") == 0);
        } catch (ConnectionException e) {
            fail("Expected e.getEvent() == Connection.TPEV_SVCFAIL");
        }

        TXINFO txinfo = new TXINFO();
        int inTx = TX.tx_info(txinfo);
        log.info("inTx=" + inTx);
        assertTrue(txinfo.transaction_state == TX.TX_ROLLBACK_ONLY);
        assertTrue(TX.tx_commit() == TX.TX_ROLLBACK);
    }

    public void test_no_tpreturn() throws ConnectionException, ConfigurationException {
        log.info("test_no_tpreturn");
        server.tpadvertiseTestRollbackOnlyNoTpreturnService();

        assertTrue(TX.tx_open() == TX.TX_OK);
        assertTrue(TX.tx_begin() == TX.TX_OK);

        try {
            connection.tpcall(RunServer.getServiceNameTestRollbackOnly(), sendbuf, 0);
            fail("Expected e.getTperrno() == Connection.TPESVCERR");
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPESVCERR);
        }

        TXINFO txinfo = new TXINFO();
        int inTx = TX.tx_info(txinfo);
        log.info("inTx=" + inTx);
        assertTrue(txinfo.transaction_state == TX.TX_ROLLBACK_ONLY);
        assertTrue(TX.tx_commit() == TX.TX_ROLLBACK);
    }
}
