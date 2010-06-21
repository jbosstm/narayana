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
 * Copyright (C) 1999-2001 by HP Bluestone Software, Inc. All rights Reserved.
 *
 * HP Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: Connection.java 2342 2006-03-30 13:06:17Z  $
 */
 
package com.arjuna.ats.internal.arjuna.recovery;

import java.io.*;
import java.net.*;

import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.recovery.Service;

public class Connection extends Thread
{
   /**
    * Takes socket and service to execute.
    */

   public Connection( Socket server_socket, Service service)
   {
       this(server_socket, service, null);
   }

    /**
     * Takes socket and service to execute and a callback to run when processing of the connection has completed
     */

    public Connection( Socket server_socket, Service service, Callback callback )
   {
      super( "Server.Connection:" + server_socket.getInetAddress().getHostAddress() + ":" + server_socket.getPort() );
              
      _server_socket = server_socket;

      try
      {
	  _server_socket.setSoTimeout(0);
      }
      catch (java.net.SocketException ex) {
          tsLogger.i18NLogger.warn_recovery_Connection_2();
      }

      _service = service;

       _callback = callback;
   }
   
   /**
    * Obtains input and output streams and executes work
    * required by the service.
    */

   public void run()
   {
      try
      {
         InputStream  is = _server_socket.getInputStream();
         OutputStream os = _server_socket.getOutputStream();

         _service.doWork ( is, os );
      }
      catch ( IOException ex ) {
          tsLogger.i18NLogger.warn_recovery_Connection_1();
      }
      finally
      {

      // run the callback to notify completion of processing for this connection

      if (_callback != null) {
          _callback.run();
      }
      }
   }

   // What client (RecoveryManager) talks to.
   private Socket  _server_socket;
   
   // What Service is provided to the client(RecoveryManager).
   private Service _service;

   private Callback _callback;

    // abstract class instantiated by clients to allow notification that a connection has been closed
    
   public static abstract class Callback
   {
       abstract void run();
   }
}
