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

public class TestTPConversation extends TestCase {
	private static final Logger log = LogManager
			.getLogger(TestTPConversation.class);
	private RunServer server = new RunServer();

	private Connection connection;
	private int sendlen;
	private X_OCTET sendbuf;
	private Session cd;

	public void setUp() throws ConnectionException, ConfigurationException {
		server.serverinit();

		ConnectionFactory connectionFactory = ConnectionFactory
				.getConnectionFactory();
		connection = connectionFactory.getConnection();
		sendlen = 11;
		sendbuf = (X_OCTET) connection.tpalloc("X_OCTET", null);
	}

	public void tearDown() throws ConnectionException, ConfigurationException {
		log.info("Calling teardown");
		server.serverdone();
		connection.close();
		log.info("Called teardown");
	}

	public void test_conversation() throws ConnectionException,
			ConfigurationException {
		log.info("test_conversation");
		server.tpadvertiseTestTPConversation();

		sendbuf.setByteArray("conversate".getBytes());
		cd = connection.tpconnect(RunServer.getServiceNameTestTPConversation(),
				sendbuf, Connection.TPRECVONLY);
		long revent = 0;
		log.info("Started conversation");
		for (int i = 10; i < 100; i++) {
			try {
				Buffer result = cd.tprecv(0);
				fail("Did not get sendonly event");
			} catch (ResponseException e) {
				assertTrue(e.getTperrno() == Connection.TPEEVENT);
				assertTrue(e.getEvent() == Connection.TPEV_SENDONLY);
				Buffer rcvbuf = e.getReceived();
				String expectedResult = ("hi" + i);
				assertTrue(strncmp(expectedResult, rcvbuf, 4) == 0);

				sendbuf.setByteArray(("yo" + i).getBytes());
				// btlogger((char*) "test_conversation:%s:", sendbuf);
				int result = cd.tpsend(sendbuf, Connection.TPRECVONLY);
				assertTrue(result != -1);
			}
		}
		log.info("Conversed");
		try {
			cd.tprecv(0);
			fail("Expected event");
		} catch (ResponseException e) {
			assertTrue(e.getTperrno() == Connection.TPEEVENT);
			Buffer rcvbuf = e.getReceived();
			String expectedResult = ("hi" + 100);
			log.info("Expected: " + expectedResult + " Received: "
					+ new String(((X_OCTET) rcvbuf).getByteArray()));
			assertTrue(strncmp(expectedResult, rcvbuf, 5) == 0);
		}
	}

	public void test_short_conversation() throws ConnectionException,
			ConfigurationException {
		server.tpadvertiseTestTPConversa2();

		log.info("test_short_conversation");
		cd = connection.tpconnect(RunServer.getServiceNameTestTPConversa2(),
				null, Connection.TPRECVONLY);
		assertTrue(cd != null);

		Buffer rcvbuf = cd.tprecv(0);
		assertTrue(rcvbuf != null);
		assertTrue(strncmp("hi0", rcvbuf, 3) == 0);

		try {
			cd.tprecv(0);
			fail("Expected event");
		} catch (ResponseException e) {
			assertTrue(e.getTperrno() == Connection.TPEEVENT);
			rcvbuf = e.getReceived();
			assertTrue(strncmp("hi1", rcvbuf, 3) == 0);
		}
	}

	public static int strcmp(String string, Buffer buffer) {
		return strncmp(string, buffer, -1);
	}

	public static int strncmp(String string, Buffer buffer, int max) {
		boolean completeCheck = false;
		if (max == -1) {
			completeCheck = true;
			max = string.length();
		}
		byte[] expected = string.getBytes();
		byte[] received = ((X_OCTET) buffer).getByteArray();
		if (received.length < expected.length) {
			return -1;
		}
		if (max > expected.length || max > received.length) {
			return -1;
		}
		for (int i = 0; i < max; i++) {
			if (expected[i] != received[i]) {
				return -1;
			}
		}

		if (completeCheck) {
			for (int i = max; i < received.length; i++) {
				if (received[i] != '\0') {
					return -1;
				}
			}
		}
		return 0;
	}

}
