/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.recovery;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
      catch ( IOException ioe ) {
          tsLogger.i18NLogger.warn_recovery_Connection_1(ioe);
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
   private final Socket  _server_socket;
   
   // What Service is provided to the client(RecoveryManager).
   private final Service _service;

   private final Callback _callback;

    // abstract class instantiated by clients to allow notification that a connection has been closed
    
   public static abstract class Callback
   {
       abstract void run();
   }
}