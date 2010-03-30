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
 * $Id: Listener.java 2342 2006-03-30 13:06:17Z  $
 */
 
package com.arjuna.ats.internal.arjuna.recovery;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.LinkedList;

import com.arjuna.ats.arjuna.common.*;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.recovery.Service;

public class Listener extends Thread
{
   /**
    * Creates a listener thread on the specified port
    * for the specified service to run.
    */

   public Listener( int port, Service service )
      throws IOException
   {
      super( "Listener:" + port );

      _listener_port = port;
      _listener_service = service;

      _listener_socket = new ServerSocket( _listener_port );

      if (Listener.setTimeout)
	  _listener_socket.setSoTimeout( _listener_socket_timeout_in_msecs );

       connections = new LinkedList<Socket>();
   }
   
   /**
    * Creates a listener thread on the specified server socket
    * for the specified service to run.
    */

   public Listener( ServerSocket serverSocket, Service service )
      throws IOException
   {
      super( "Listener:" + serverSocket.getLocalPort() );

      _listener_port = serverSocket.getLocalPort();
      _listener_service = service;

      _listener_socket = serverSocket;

      if (Listener.setTimeout)
	  _listener_socket.setSoTimeout( _listener_socket_timeout_in_msecs );

      connections = new LinkedList<Socket>();
   }

   /*
    * Close down the socket.
    *
    * @message com.arjuna.ats.internal.arjuna.recovery.Listener_1 [com.arjuna.ats.internal.arjuna.recovery.Listener_1] - failed to close listener socket

    * this is pointless because this instance is a thread so never gets garbage collected until it has stopped running.
    * but that means shutdown will have been called making the call to close in this method redundant.

   public void finalize()
   {
      stopListener();

      try
      {
         _listener_socket.close();
      }
      catch ( IOException ex )
      {
	  tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.Listener_1");
      }
   }      
    */

   /**
    * Loops waiting for connection requests from client,
    * creates a new Connection object for each connection.
    *
    * @message com.arjuna.ats.internal.arjuna.recovery.Listener_2 [com.arjuna.ats.internal.arjuna.recovery.Listener_2] Listener - IOException
    */

   public void run()
   {
      while ( !stopRequested() )
      {
         try
         {
            final Socket conn = _listener_socket.accept();
             // n.b. add may not occur because a shutdown was requested
            if (addConnection(conn)) {
                // ok the connection is in the list -- ensure it clears itself out
            Connection.Callback callback = new Connection.Callback() {
                private Socket _conn = conn;
                public void run() {
                    removeConnection(_conn);
                }
            };

            Connection new_conn = new Connection( conn, _listener_service, callback );

     	    if (tsLogger.arjLogger.isDebugEnabled()) {
                 tsLogger.arjLogger.debug("Connected to " + conn.getInetAddress().getHostAddress() +
                         " on port " + conn.getPort() + " on listener port " +
                         conn.getLocalPort() + " for service " +
                         _listener_service.getClass().getName());
             }

            new_conn.start();
            }
         }
         catch ( final InterruptedIOException ex )
         {
            // timeout on the listener socket expired.
         }
         catch (final SocketException ex)
         {
             // we get this if the socket is closed under a call to shutdown
             if (tsLogger.arjLogger.isDebugEnabled()) {
                 tsLogger.arjLogger.debug("Recovery listener existing " +
                         _listener_service.getClass().getName());
             }
         }
         catch ( final IOException ex )
         {
	     if (tsLogger.arjLoggerI18N.isDebugEnabled())
		 tsLogger.arjLoggerI18N.debug("com.arjuna.ats.internal.arjuna.recovery.Listener_2"+" "+ex);
         }
         catch (final Exception ex)
         {
         }
      }
   }

    public synchronized boolean addConnection(Socket conn)
    {
        if (!_stop_listener) {
            connections.add(conn);
            return true;
        } else {
            // a close down request got in between the connection create and the
            // call to this method. it will have closed all the other connections
            // and will be waiting on this (listener) thread. so close this connection
            // before returning false
            try {
                conn.close();
            } catch (Exception e) {
                // ignore
            }
            return false;
        }
    }

    public synchronized void removeConnection(Socket conn)
    {
        connections.remove(conn);
        notifyAll();
    }

   /**
    * Halts running of the listener thread.
    */

   public synchronized void stopListener()
   {
      _stop_listener = true;

       try
       {
           _listener_socket.close();  // in case we're still in accept
       }
       catch (final Exception ex)
       {
       }
      // there is no need for this as the close will interrupt any i/o that is in progress
      // this.interrupt();

       // ok, closing a connection socket will cause the connection thread to remove it from the list as it
       // exits so we keep on closing them and waiting until the list is empty

       while(connections.size() > 0) {
           Socket conn = connections.get(0);
           try {
               conn.close();
           } catch (Exception e) {
               // ignore
           }
           try {
               wait();
           } catch (InterruptedException e) {
               // ignore
           }
       }
      
       // make sure this listener thread has exited before we return

       try {
           this.join();
       } catch (InterruptedException ie) {
       }
   }

   private synchronized boolean stopRequested()
   {
       return _stop_listener;
   }
    
   // Socket & port which client(RecoveryManager) connects to.
   private ServerSocket _listener_socket;
   private int          _listener_port;

   // Flag to indicate when to shutdown the listener thread.
   private boolean _stop_listener = false;
   
   // Timeout used on for accept.
   private int _listener_socket_timeout_in_msecs = 1500;
   
   // The work item to execute.
   private Service _listener_service;

    private List<Socket> connections;

    private static boolean setTimeout = false;
    
    static
    {
    	setTimeout = recoveryPropertyManager.getRecoveryEnvironmentBean().isTimeoutSocket();
    }
    
}

