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
 * A session reference may either be obtained from the tpconnect
 * <code>Connection</code> invocation for a client or retrieved from the
 * TPSVCINFO structure for a service (assuming the service was invoked within
 * the scope of a tpconnect).
 * 
 * It is used to send and retrieve data:
 * Connection#tpconnect(String, Buffer, int, int) TPSVCINFO#getSession()
 */
public interface Session {

	/**
	 * Send a buffer to a remote server in a conversation
	 * 
	 * @param toSend
	 *            The outbound data
	 * @param flags
	 *            The flags to use
	 * @throws ConnectionException
	 *             If the message cannot be sent.
	 */
	public int tpsend(Buffer toSend, int flags) throws ConnectionException;

	/**
	 * Received the next response in a conversation
	 * 
	 * @param flags
	 *            The flags to use
	 * @return The next response
	 * @throws ConnectionException
	 *             If the message cannot be received or the flags are incorrect
	 * @throws ConfigurationException
	 */
	public Buffer tprecv(int flags) throws ConnectionException,
			ConfigurationException;

	/**
	 * Close the conversation with the remote service. This will close the
	 * session.
	 */
	public void tpdiscon() throws ConnectionException;
}
