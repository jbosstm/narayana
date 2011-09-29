/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2004,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: RecoveryDriver.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.recovery;

import java.io.*;
import java.net.*;

import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

public class RecoveryDriver
{
    public static final String SCAN = "SCAN";
    public static final String ASYNC_SCAN = "ASYNC_SCAN";
    public static final String PING = "PING";
    public static final String PONG = "PONG";

    // allow time for one complete scan (which is dominated by the backoff wait) plus a fudge factor for actual work + comms delay
    public static final int DEFAULT_SYNC_TIMEOUT = 1000 + (recoveryPropertyManager.getRecoveryEnvironmentBean().getRecoveryBackoffPeriod() * 1000); // in milliseconds
    public static final int DEFAULT_SYNC_RETRY = 5;

    public static final int DEFAULT_SO_TIMEOUT = 20000;

    public RecoveryDriver (int port)
    {
	this(port, null, DEFAULT_SO_TIMEOUT);
    }

    public RecoveryDriver (int port, String hostName)
    {
	this(port, hostName, DEFAULT_SO_TIMEOUT);
    }

    public RecoveryDriver (int port, String hostName, int timeout)
    {
	_port = port;
	_hostName = hostName;
	_timeout = timeout;
    }

    public final boolean synchronousScan () throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException
    {
	return synchronousScan(DEFAULT_SYNC_TIMEOUT, DEFAULT_SYNC_RETRY);
    }

    public final boolean synchronousScan (int timeout, int retry) throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException
    {
        return scan(SCAN, timeout, retry);
    }

    public final boolean asynchronousScan () throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException
    {
        /*
         * For async the timeout is the socket timeout and number of attempts on call is 1.
         */

	return scan(ASYNC_SCAN, _timeout, 1);
    }

    /*
     * Ignore timeout/retry for async.
     */

    private final boolean scan (String scanType, int timeout, int retry) throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException
    {
	if (_hostName == null)
	    _hostName = InetAddress.getLocalHost().getHostName();

        boolean success = false;
        Socket connectorSocket = null;

	for (int i = 0; i < retry && !success; i++)
	{
	    connectorSocket = new Socket(_hostName, _port);

            connectorSocket.setSoTimeout(timeout);

	    try
	    {
	        // streams to and from the RecoveryManager

	        BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream())) ;

	        PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream()));

	        toServer.println(scanType);

	        toServer.flush() ;

	        String response = fromServer.readLine();

	        if (response.equals("DONE"))
	            success = true;
	    }
	    catch (final SocketTimeoutException ex)
	    {
	    }
	    finally
	    {
	        if (connectorSocket != null)
	            connectorSocket.close() ;
	    }
	}

	return success;
    }

    private String _hostName = null;
    private int    _port = 0;
    private int    _timeout = 20000;

}
