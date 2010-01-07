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
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import com.arjuna.ats.arjuna.utils.Utility;

import java.net.*;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import java.io.IOException;

/**
 * Obtains a unique value to represent the process id via sockets and
 * ports.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: SocketProcessId.java 2342 2006-03-30 13:06:17Z  $
 * @since HPTS 3.0.
 */

public class SocketProcessId implements com.arjuna.ats.arjuna.utils.Process
{
    /**
     * @message com.arjuna.ats.internal.arjuna.utils.SocketProcessId_2 [com.arjuna.ats.internal.arjuna.utils.SocketProcessId_2] - SocketProcessId.getpid could not get unique port.
     */
    public SocketProcessId()
    {
        int port = arjPropertyManager.getCoreEnvironmentBean().getSocketProcessIdPort();
        int maxPorts = arjPropertyManager.getCoreEnvironmentBean().getSocketProcessIdMaxPorts();

        int maxPort;

        if (maxPorts <= 1)
        {
            maxPort = port;
        }
        else if (Utility.MAX_PORT - maxPorts < port)
        {
            maxPort = Utility.MAX_PORT;
        }
        else
        {
            maxPort = port + maxPorts;
        }

        do {
            _theSocket = createSocket(port);
        } while (_theSocket == null && ++port < maxPort);

        _thePort = ((_theSocket == null) ? -1 : _theSocket.getLocalPort());

        if (_thePort == -1) {
            throw new FatalError(tsLogger.arjLoggerI18N.getString("com.arjuna.ats.internal.arjuna.utils.SocketProcessId_2"));
        }
    }

    /**
     * @return the process id. This had better be unique between processes
     * on the same machine. If not we're in trouble!
     */
    public int getpid ()
    {
    	return _thePort;
    }

    private static ServerSocket createSocket(int port)
    {
        try
        {
            return new ServerSocket(port, 0, InetAddress.getByName(null));
        }
        catch (IOException e)
        {
            return null;
        }
    }

    private final int _thePort;
    private ServerSocket _theSocket;
}
