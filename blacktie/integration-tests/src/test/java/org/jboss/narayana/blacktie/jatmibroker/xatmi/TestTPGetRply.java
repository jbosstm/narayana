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

public class TestTPGetRply extends TestCase {
    private static final Logger log = LogManager.getLogger(TestTPGetRply.class);
    private RunServer server = new RunServer();
    private Connection connection;
    private X_OCTET sendbuf;

    public void setUp() throws ConnectionException, ConfigurationException {
        server.serverinit();

        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();

        sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray("grply".getBytes());
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        connection.close();
        server.serverdone();
    }

    public void test_tpgetrply() throws ConnectionException, ConfigurationException {
        log.info("test_tpgetrply");
        server.tpadvertiseTestTPGetrply();
        int cd = connection.tpacall(RunServer.getServiceNameTestTPGetrply(), sendbuf, 0);
        assertTrue(cd != -1);

        // RETRIEVE THE RESPONSE
        Buffer rcvbuf = connection.tpgetrply(cd, 0).getBuffer();
        assertTrue(TestTPConversation.strcmp("testtpgetrply_service", rcvbuf) == 0);
    }

    // 8.5
    public void test_tpgetrply_baddesc() throws ConnectionException, ConfigurationException {
        log.info("test_tpgetrply_baddesc");
        server.tpadvertiseTestTPGetrply();
        int cd = 2;
        try {
            connection.tpgetrply(cd, 0);
            fail("Did not get exception");
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPEBADDESC);
        }
    }

    // can't have null cd
    // public void test_tpgetrply_nullcd() {
    // log.info("test_tpgetrply_nullcd");
    // int valToTest = connection.tpgetrply(0);
    // assertTrue(valToTest == -1);
    // assertTrue(tperrno != 0);
    // assertTrue(tperrno != TPEBADDESC);
    // assertTrue(tperrno != TPEOTYPE);
    // assertTrue(tperrno != TPETIME);
    // assertTrue(tperrno != TPESVCFAIL);
    // assertTrue(tperrno != TPESVCERR);
    // assertTrue(tperrno != TPEBLOCK);
    // assertTrue(tperrno == TPEINVAL);
    // }

    // CAN@T HAVE NULL BUFFERS
    // public void test_tpgetrply_nullrcvbuf() {
    // log.info("test_tpgetrply_nullrcvbuf");
    // int cd = 2;
    // int valToTest = connection.tpgetrply(&cd, NULL, &rcvlen, 0);
    // assertTrue(valToTest == -1);
    // assertTrue(tperrno != 0);
    // assertTrue(tperrno != TPEINVAL);
    // assertTrue(tperrno != TPEOTYPE);
    // assertTrue(tperrno != TPETIME);
    // assertTrue(tperrno != TPESVCFAIL);
    // assertTrue(tperrno != TPESVCERR);
    // assertTrue(tperrno != TPEBLOCK);
    // assertTrue(tperrno == TPEBADDESC);
    // }

    // CANT HAVE NULL RCVLEN
    // public void test_tpgetrply_nullrcvlen() {
    // log.info("test_tpgetrply_nullrcvlen");
    // int cd = 2;
    // int valToTest = connection.tpgetrply(&cd, (char **) &rcvbuf, NULL, 0);
    // assertTrue(valToTest == -1);
    // assertTrue(tperrno != 0);
    // assertTrue(tperrno != TPEBADDESC);
    // assertTrue(tperrno != TPEOTYPE);
    // assertTrue(tperrno != TPETIME);
    // assertTrue(tperrno != TPESVCFAIL);
    // assertTrue(tperrno != TPESVCERR);
    // assertTrue(tperrno != TPEBLOCK);
    // assertTrue(tperrno == TPEINVAL);
    // }

    // public void test_tpgetrply_with_TPNOBLOCK() throws ConnectionException, ConfigurationException {
    // log.info("test_tpgetrply_with_TPNOBLOCK");
    // server.tpadvertiseTestTPGetRplyTPNOBLOCK();
    //
    // int cd = connection.tpacall(server.getServiceNameTPGetRplyTPNOBLOCK(),
    // sendbuf, 0);
    // assertTrue(cd != -1);
    //
    // // RETRIEVE THE RESPONSE
    // try {
    // Response valToTest = connection.tpgetrply(cd, Connection.TPNOBLOCK);
    // fail("Expected exception");
    // } catch (ConnectionException e) {
    // assertTrue(e.getTperrno() == Connection.TPEBLOCK);
    // // DRAIN THE RESPONSE
    // Response toTest = connection.tpgetrply(cd, 0);
    // assertTrue(TestTPConversation.strcmp(toTest.getBuffer(),
    // "test_tpgetrply_TPNOBLOCK") == 0);
    // }
    // }
    //
    // public void test_tpgetrply_without_TPNOBLOCK() throws ConnectionException, ConfigurationException {
    // log.info("test_tpgetrply_without_TPNOBLOCK");
    // server.tpadvertiseTestTPGetRplyTPNOBLOCK();
    //
    // int cd = connection.tpacall(server.getServiceNameTPGetRplyTPNOBLOCK(),
    // sendbuf, 0);
    // assertTrue(cd != -1);
    //
    // // RETRIEVE THE RESPONSE
    // Response toTest = connection.tpgetrply(cd, 0);
    // assertTrue(TestTPConversation.strcmp(toTest.getBuffer(),
    // "test_tpgetrply_TPNOBLOCK") == 0);
    // }

    public void test_tpgetrply_with_TPGETANY() throws ConnectionException, ConfigurationException {
        log.info("test_tpgetrply_with_TPGETANY");
        server.tpadvertiseTestTPGetrplyOne();
        server.tpadvertiseTestTPGetrplyTwo();

        int cd1 = connection.tpacall(RunServer.getServiceNameTestTPGetrplyOne(), sendbuf, 0);
        assertTrue(cd1 != -1);

        int cd2 = connection.tpacall(RunServer.getServiceNameTestTPGetrplyTwo(), sendbuf, 0);
        assertTrue(cd2 != -1);
        assertTrue(cd1 != cd2);

        // RETRIEVE THE RESPONSE
        int cdToGet = cd1;
        Response response = connection.tpgetrply(cdToGet, Connection.TPGETANY);
        assertTrue(response.getCd() == cd2);
        assertTrue(TestTPConversation.strcmp("test_tpgetrply_TPGETANY_two", response.getBuffer()) == 0);
    }

    public void test_tpgetrply_without_TPGETANY() throws ConnectionException, ConfigurationException {
        log.info("test_tpgetrply_without_TPGETANY");
        server.tpadvertiseTestTPGetrplyOne();
        server.tpadvertiseTestTPGetrplyTwo();

        int cd1 = connection.tpacall(RunServer.getServiceNameTestTPGetrplyOne(), sendbuf, 0);
        assertTrue(cd1 != -1);

        int cd2 = connection.tpacall(RunServer.getServiceNameTestTPGetrplyTwo(), sendbuf, 0);
        assertTrue(cd2 != -1);
        assertTrue(cd1 != cd2);

        // RETRIEVE THE RESPONSE
        int cdToGet = cd1;
        Response response = connection.tpgetrply(cdToGet, 0);
        assertTrue(response.getCd() == cd1);
        assertTrue(TestTPConversation.strcmp("test_tpgetrply_TPGETANY_one", response.getBuffer()) == 0);
    }

}
