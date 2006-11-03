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

public class RecoveryDriver
{
    
    public RecoveryDriver (int port)
    {
	this(port, null, 20000);
    }
    
    public RecoveryDriver (int port, String hostName)
    {
	this(port, hostName, 20000);
    }
    
    public RecoveryDriver (int port, String hostName, int timeout)
    {
	_port = port;
	_hostName = hostName;
	_timeout = timeout;
    }
    
    public final boolean synchronousScan () throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException
    {
	return scan("SCAN");
    }
    
    public final boolean asynchronousScan () throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException
    {
	return scan("ASYNC_SCAN");
    }

    private final boolean scan (String scanType) throws java.net.UnknownHostException, java.net.SocketException, java.io.IOException
    {
	if (_hostName == null)
	    _hostName = InetAddress.getLocalHost().getHostName();

	Socket connectorSocket = new Socket(_hostName, _port);

	connectorSocket.setSoTimeout(_timeout);

	// streams to and from the RecoveryManager
	
	BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream())) ;
	
	PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream()));

	toServer.println(scanType);

	toServer.flush() ;
	
	String response = fromServer.readLine();
	boolean success = false;
	
	if (response.equals("DONE"))
	    success = true;
                  
	connectorSocket.close() ;

	return success;
    }

    private String _hostName = null;
    private int    _port = 0;
    private int    _timeout = 20000;

}
