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

public class TestTPRecv extends TestCase {
    private static final Logger log = LogManager.getLogger(TestTPRecv.class);
    private RunServer server = new RunServer();
    private Connection connection;
    private X_OCTET sendbuf;
    private Session cd;

    public void setUp() throws ConnectionException, ConfigurationException {
        server.serverinit();
        server.tpadvertiseTestTPRecv();

        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();


        sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
        sendbuf.setByteArray("recv".getBytes());
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        connection.close();
        server.serverdone();
    }

    public void test_tprecv_sendonly() throws ConnectionException, ConfigurationException {
        log.info("test_tprecv_sendonly");
        cd = connection.tpconnect(RunServer.getServiceNameTestTPRecv(), sendbuf, Connection.TPSENDONLY);
        try {
            cd.tprecv(0);
            fail("expected proto error");
        } catch (ResponseException e) {
            assertTrue(e.getEvent() == Connection.TPEV_SVCERR);
        } catch (ConnectionException e) {
            assertTrue(e.getTperrno() == Connection.TPEPROTO);
        }
    }
}
