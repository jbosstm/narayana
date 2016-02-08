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

import org.jboss.narayana.blacktie.jatmibroker.core.conf.ConfigurationException;

/**
 * This is the connection to remote BlackTie services. It must be created using
 * the ConnectionFactory.
 * 
 * @see ConnectionFactory#getConnection()
 */
public interface Connection {

	// AVAILABLE FLAGS
	public static final int TPNOBLOCK = 0x00000001;
	public static final int TPSIGRSTRT = 0x00000002;
	public static final int TPNOREPLY = 0x00000004;
	public static final int TPNOTRAN = 0x00000008;
	public static final int TPTRAN = 0x00000010;
	public static final int TPNOTIME = 0x00000020;
	public static final int TPGETANY = 0x00000080;
	public static final int TPNOCHANGE = 0x00000100;
	public static final int TPCONV = 0x00000400;
	public static final int TPSENDONLY = 0x00000800;
	public static final int TPRECVONLY = 0x00001000;

	// ERROR CONDITIONS
	public static final int TPEBADDESC = 2;
	public static final int TPEBLOCK = 3;
	public static final int TPEINVAL = 4;
	public static final int TPELIMIT = 5;
	public static final int TPENOENT = 6;
	public static final int TPEOS = 7;
	public static final int TPEPROTO = 9;
	public static final int TPESVCERR = 10;
	public static final int TPESVCFAIL = 11;
	public static final int TPESYSTEM = 12;
	public static final int TPETIME = 13;
	public static final int TPETRAN = 14;
	public static final int TPGOTSIG = 15;
	public static final int TPEITYPE = 17;
	public static final int TPEOTYPE = 18;
	public static final int TPEEVENT = 22;
	public static final int TPEMATCH = 23;

	// SERVICE CONDITIONS
	public static final short TPFAIL = 0x00000001;
	public static final short TPSUCCESS = 0x00000002;

	// Events
	public static final long TPEV_DISCONIMM = 0x0001;
	public static final long TPEV_SVCERR = 0x0002;
	public static final long TPEV_SVCFAIL = 0x0004;
	public static final long TPEV_SVCSUCC = 0x0008;
	public static final long TPEV_SENDONLY = 0x0020;

	public static final int XATMI_SERVICE_NAME_LENGTH = 128;

	/**
	 * Allocate a new buffer
	 * 
	 * @param type
	 *            The type of the buffer
	 * @param subtype
	 *            The subtype of the buffer
	 * @return The new buffer
	 * @throws ConnectionException
	 *             If the buffer was unknown or invalid.
	 * @throws ConfigurationException
	 */
	public Buffer tpalloc(String type, String subtype)
			throws ConnectionException, ConfigurationException;

	/**
	 * Synchronous call.
	 * 
	 * @param svc
	 *            The name of the service to call
	 * @param buffer
	 *            The inbound data
	 * @param flags
	 *            The flags to use
	 * @return The returned buffer
	 * @throws ConnectionException
	 *             If the service cannot be contacted.
	 * @throws ConfigurationException
	 */
	public Response tpcall(String svc, Buffer buffer, int flags)
			throws ConnectionException, ConfigurationException;

	/**
	 * Asynchronous call
	 * 
	 * @param svc
	 *            The name of the service to call
	 * @param toSend
	 *            The inbound data
	 * @param flags
	 *            The flags to use
	 * @return The connection descriptor
	 * @throws ConnectionException
	 *             If the service cannot be contacted.
	 */
	public int tpacall(String svc, Buffer toSend, int flags)
			throws ConnectionException;

	/**
	 * Cancel the outstanding asynchronous call.
	 * 
	 * @param cd
	 *            The connection descriptor
	 * @throws ConnectionException
	 *             If the client cannot be cleaned up.
	 */
	public int tpcancel(int cd) throws ConnectionException;

	/**
	 * Get the reply for an asynchronous call.
	 * 
	 * @param cd
	 *            The connection descriptor to use
	 * @param flags
	 *            The flags to use
	 * @return The response from the server
	 * @throws ConnectionException
	 *             If the service cannot be contacted.
	 * @throws ConfigurationException
	 */
	public Response tpgetrply(int cd, int flags) throws ConnectionException,
			ConfigurationException;

	/**
	 * Handle the initiation of a conversation with the server.
	 * 
	 * @param svc
	 *            The name of the service
	 * @param toSend
	 *            The outbound buffer
	 * @param flags
	 *            The flags to use
	 * @return The connection descriptor
	 * @throws ConnectionException
	 *             If the service cannot be contacted.
	 */
	public Session tpconnect(String svc, Buffer toSend, int flags)
			throws ConnectionException;

	/**
	 * Close any resources associated with this connection
	 * 
	 * @throws ConnectionException
	 *             If an open session cannot be cancelled or disconnected.
	 */
	public void close() throws ConnectionException;
}
