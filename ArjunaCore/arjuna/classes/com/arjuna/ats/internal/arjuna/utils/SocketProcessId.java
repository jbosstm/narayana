/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and others contributors as indicated 
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
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: SocketProcessId.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.utils;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import com.arjuna.ats.arjuna.utils.Process;
import com.arjuna.ats.arjuna.utils.Utility;

import java.io.*;
import java.net.*;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import java.net.UnknownHostException;
import java.lang.NumberFormatException;
import java.lang.StringIndexOutOfBoundsException;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * Obtains a unique value to represent the process id via sockets and
 * ports.
 *
 * This implementation is tied closely with the socket/port version of
 * crash recovery, since that requires a thread to listen on a port for
 * incoming requests for a specific transaction's status.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: SocketProcessId.java 2342 2006-03-30 13:06:17Z  $
 * @since HPTS 3.0.
 */

public class SocketProcessId implements com.arjuna.ats.arjuna.utils.Process
{

    /**
     * @return the process id. This had better be unique between processes
     * on the same machine. If not we're in trouble!
     *
     * @message com.arjuna.ats.internal.arjuna.utils.SocketProcessId_1 [com.arjuna.ats.internal.arjuna.utils.SocketProcessId_1]- Invalid port specified 
     * @message com.arjuna.ats.internal.arjuna.utils.SocketProcessId_2 [com.arjuna.ats.internal.arjuna.utils.SocketProcessId_2] - SocketProcessId.getpid could not get unique port.
     */
    
    public int getpid ()
    {
	synchronized (SocketProcessId._lock)
	{
	    if (_thePort == 0)
	    {
		if (_theSocket == null)
		{
		    int port = _defaultPort;
      
		    String portStr = arjPropertyManager.propertyManager.getProperty("com.arjuna.ats.arjuna.recovery.transactionStatusManagerPort");

		    if ( portStr != null )
		    {
			try
			{
			    port = Integer.parseInt(portStr);
			}
			catch (Exception ex)
			{
			    if (tsLogger.arjLoggerI18N.isWarnEnabled())
				tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.utils.SocketProcessId_1",ex);
			    
			    port = -1;
			}
		    }
			
		    if (port != -1)
		    {
			try
			{
			    _theSocket = new ServerSocket(port);
			    
			    _thePort = _theSocket.getLocalPort();
			}
			catch (Exception ex)
			{
			    _thePort = -1;
			}
		    }
		    else
			_thePort = -1;
		}
	    }
	}

	if (_thePort == -1)
	    throw new FatalError(tsLogger.log_mesg.getString("com.arjuna.ats.internal.arjuna.utils.SocketProcessId_2"));
	
	return _thePort;
    }

    public static final ServerSocket getSocket ()
    {
	synchronized (SocketProcessId._lock)
	{
	    return _theSocket;
	}
    }
    
    private static int          _thePort = 0;
    private static ServerSocket _theSocket = null;
    private static Object       _lock = new Object();

    /**
     * Default port is any free port.
     */

    private static final int _defaultPort = 0 ;
 
}
