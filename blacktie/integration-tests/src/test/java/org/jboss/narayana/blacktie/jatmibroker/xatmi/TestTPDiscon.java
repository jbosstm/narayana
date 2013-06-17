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

public class TestTPDiscon extends TestCase {
    private static final Logger log = LogManager.getLogger(TestTPDiscon.class);
    private RunServer server = new RunServer();
    private Connection connection;
    private Session cd;

    public void setUp() throws ConnectionException, ConfigurationException {
        server.serverinit();
        server.tpadvertiseTestTPDiscon();

        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();

        cd = connection.tpconnect(RunServer.getServiceNameTestTPDiscon(), null, Connection.TPSENDONLY);
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        connection.close();
        server.serverdone();
    }

    public void test_tpdiscon() throws ConnectionException {
        log.info("TestOne");
        cd.tpdiscon();
        cd = null;
    }
    //
    // public void test_tpdiscon_baddescr() {
    // log.info("test_tpdiscon_baddescr");
    // cd.tpdiscon(2);
    // CPPUNIT_ASSERT(tperrno == TPEBADDESC);
    // }
    //
    // public void test_tpdiscon_negdescr() {
    // log.info("test_tpdiscon_negdescr");
    // cd.tpdiscon(-1);
    // CPPUNIT_ASSERT(tperrno == TPEBADDESC);
    // }
}
