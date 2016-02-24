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
package org.jboss.narayana.blacktie.administration;

import junit.framework.TestCase;

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Connection;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionException;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.ConnectionFactory;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.Response;
import org.jboss.narayana.blacktie.jatmibroker.xatmi.X_OCTET;

public class BlacktieStompAdministrationServiceTest extends TestCase {
    private Connection connection;

    public void setUp() throws ConnectionException, ConfigurationException {
        ConnectionFactory connectionFactory = ConnectionFactory.getConnectionFactory();
        connection = connectionFactory.getConnection();
    }

    public void tearDown() throws ConnectionException, ConfigurationException {
        connection.close();
    }

    /**
     * Cannot test this without using a C server as unadvertise checks for a running server
     * 
     * @throws ConnectionException
     */
    public void xtest() throws ConnectionException, ConfigurationException {
        processStompCommand("tpadvertise,foo,FOOTest,0,queue,5.2.14.Final,", 1);
        try {
            connection.tpacall("FOOTest", null, Connection.TPNOREPLY);
        } catch (ConnectionException e) {
            fail("Was not able to send the request : " + e.getMessage());
        }

        processDomainCommand("unadvertise,foo,FOOTest,", 1);
        try {
            connection.tpcall("FOOTest", null, Connection.TPNOREPLY);
            fail("Was able to send the request");
        } catch (ConnectionException e) {
            // EXPECTED
        }

        processStompCommand("tpadvertise,foo,FOOTest,0,queue,5.2.14.Final,", 1);

        try {
            connection.tpacall("FOOTest", null, Connection.TPNOREPLY);
        } catch (ConnectionException e) {
            fail("Was not able to send the request : " + e.getMessage());
        }
        processDomainCommand("unadvertise,foo,FOOTest,", 1);
    }

    public void testWrongVersionService() throws ConnectionException, ConfigurationException {
        processStompCommand("tpadvertise,foo,FOOTest,0,queue,WrongVersion,", 4);
    }

    private void processStompCommand(String command, int expectation) throws ConnectionException, ConfigurationException {
        byte[] toSend = command.getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null);
        buffer.setByteArray(toSend);

        Response response = connection.tpcall("BTStompAdmin", buffer, 0);

        byte[] responseData = ((X_OCTET) response.getBuffer()).getByteArray();
        assertEquals(expectation, responseData[0]);
    }

    private void processDomainCommand(String command, int expectation) throws ConnectionException, ConfigurationException {
        byte[] toSend = command.getBytes();
        X_OCTET buffer = (X_OCTET) connection.tpalloc("X_OCTET", null);
        buffer.setByteArray(toSend);

        Response response = connection.tpcall("BTDomainAdmin", buffer, 0);

        byte[] responseData = ((X_OCTET) response.getBuffer()).getByteArray();
        assertEquals(expectation, responseData[0]);
    }
}
