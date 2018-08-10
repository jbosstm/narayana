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
 * Copyright (C) 2003,
 *
 * Arjuna Technologies Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: RecoveryMonitor.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import com.arjuna.ats.arjuna.recovery.RecoveryDriver;

public class RecoveryMonitor
{
	private static String response = "";
	private static String systemOutput = "";

	public static void main (String[] args)
    {
	String host = null;
	int port = 0;
	boolean asyncScan = false;
	int timeout = 20000;
        boolean test = false;
	boolean verbose = false;

	for (int i = 0; i < args.length; i++)
	{
	    if (args[i].compareTo("-help") == 0)
	    {
		usage();
		System.exit(0);
	    }
	    else if (args[i].compareTo("-verbose") == 0)
		{
			verbose = true;
		}
	    else
	    {
		if (args[i].compareTo("-host") == 0)
		{
		    host = args[i+1];
		    i++;
		}
		else
		{
		    if (args[i].compareTo("-port") == 0)
		    {
			try
			{
			    port = Integer.parseInt(args[i+1]);
			}
			catch (Exception ex)
			{
			    System.err.println("Invalid port: "+args[i+1]);
			}

			i++;
		    }
		    else
		    {
			if (args[i].compareTo("-timeout") == 0)
			{
			    try
			    {
				timeout = Integer.parseInt(args[i+1]);
			    }
			    catch (Exception ex)
			    {
				System.err.println("Invalid timeout: "+args[i+1]);
			    }

			    i++;
			}
			else
			{
			    if (args[i].compareTo("-async") == 0)
			    {
				asyncScan = true;
			    }
			    else
			    {
				if (args[i].compareTo("-test") == 0)
				{
				    test = true;
				}
				else
				{
				    System.out.println("Unknown option "+args[i]);
				    usage();

				    System.exit(0);
				}
			    }
			}
		    }
		}
	    }
	}

	try
	{
	    if (host == null)
		host = InetAddress.getLocalHost().getHostName();

	    Socket connectorSocket = new Socket(host, port);

	    connectorSocket.setSoTimeout(timeout);

	    // streams to and from the RecoveryManager

	    BufferedReader fromServer = new BufferedReader(new InputStreamReader(connectorSocket.getInputStream(), StandardCharsets.UTF_8)) ;
                              
	    PrintWriter toServer = new PrintWriter(new OutputStreamWriter(connectorSocket.getOutputStream(), StandardCharsets.UTF_8));

	    if (verbose) {
			toServer.println(asyncScan ? RecoveryDriver.VERBOSE_ASYNC_SCAN : RecoveryDriver.VERBOSE_SCAN);
		} else {
			toServer.println(asyncScan ? RecoveryDriver.ASYNC_SCAN : RecoveryDriver.SCAN);
		}

        toServer.flush() ;

        response = fromServer.readLine();

        if (response.equals("DONE"))
            systemOutput = asyncScan ? "RecoveryManager scan begun." : "DONE";
        else
            systemOutput = verbose ? "ERROR" : "RecoveryManager did not understand request: " + response;

        System.out.println(systemOutput);

	    connectorSocket.close() ;
	}
	catch (java.net.ConnectException e)
	{
	    System.err.println("Connection refused - check the host/port information is correct.");
	}
	catch (Exception e)
	{
	    System.err.println("Caught unexpected exception: "+e);
	}

	if ( test )
	    System.out.println("Ready");
    }

    private static void usage ()
    {
	System.out.println("Usage: RecoveryMonitor -port <port number> [-host <host name>] [-verbose] [-async] [-timeout <wait time>] [-help]");
    }

	public static String getResponse() {
		return response;
	}

	public static String getSystemOutput() {
		return systemOutput;
	}
}
