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
package org.jboss.narayana.blacktie.jatmibroker.nbf;

import junit.framework.TestCase;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jboss.narayana.blacktie.jatmibroker.RunServer;
import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.BT_NBF;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;

public class TestNBF extends TestCase {
    private static final Logger log = LogManager.getLogger(TestNBF.class);
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

    public void test_nbf() throws ConnectionException, ConfigurationException {
        log.info("test_nbf");
        server.tpadvertiseTestNBF();

        try {
            BT_NBF buffer = (BT_NBF) connection.tpalloc("BT_NBF", "employee");
            assertTrue(buffer.btaddattribute("name", "zhfeng"));
            assertTrue(buffer.btaddattribute("id", new Long(1001)));

            Response resp = connection.tpcall(RunServer.getServiceNameNBF(), buffer, 0);
            assertTrue(resp != null);

            BT_NBF rcvbuf = (BT_NBF) resp.getBuffer();
            assertTrue(rcvbuf != null);
            log.info(rcvbuf);
            Long id = (Long) rcvbuf.btgetattribute("id", 0);
            assertTrue(id.longValue() == 1234);
            String name = (String) rcvbuf.btgetattribute("name", 0);
            assertTrue(name == null);
        } catch (ConnectionException e) {
            log.warn("call service faild with " + e);
            throw e;
        }
    }
}
