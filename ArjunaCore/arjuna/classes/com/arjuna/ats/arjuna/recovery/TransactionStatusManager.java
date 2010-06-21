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
 * $Id: TransactionStatusManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.arjuna.recovery ;

import java.io.* ;
import java.net.* ;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.utils.Utility ;
import com.arjuna.ats.internal.arjuna.recovery.Listener ;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem ;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;

/**
 *
 * @author Dave Elsworthy (david_elsworthy@hp.com)
 * @version $Id: TransactionStatusManager.java 2342 2006-03-30 13:06:17Z  $
 * @since HPTS 3.0.
 *
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_1 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_1] - Starting service {0} on port {1}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_2 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_2] - Listener failed
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_3 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_3] - TransactionStatusManager started on port {0} and host {1} with service {2}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_4 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_4] - Class not found: {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_5 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_5] - Failed to instantiate service class: {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_6 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_6] - Illegal access to service class: {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_7 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_7] - Failed to create server socket on port: {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_8 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_8] - Invalid port specified {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_9 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_9] - Could not get unique port.
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_10 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_10] - Unknown host {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_11 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_11] - Invalid port specified
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_12 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_12] - Unknown host specified
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_13 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_13] - Invalid host or port
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_14 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_14] - Failed to create server socket on address {0} and port: {1}
 */

public class TransactionStatusManager
{
   public TransactionStatusManager()
   {
      start( _defaultTsmService, null, -1 ) ;
   }

   public TransactionStatusManager( int port )
   {
      start( _defaultTsmService, null, port ) ;
   }

   public TransactionStatusManager( String serviceName )
   {
      start( serviceName, null, -1 ) ;
   }

   public TransactionStatusManager( String serviceName, int port  )
   {
      start( serviceName, null, port ) ;
   }

   /**
    * The work item to be executed.
    *
    * this must be private as it should only be called once. otherwise we leak listener threads
    */
   private void addService( Service service, ServerSocket serverSocket )
   {
      try
      {
         _listener = new Listener( serverSocket, service );
         _listener.setDaemon(true);

	    tsLogger.i18NLogger.info_recovery_TransactionStatusManager_1(service.getClass().getName(),
							  Integer.toString(serverSocket.getLocalPort()));

         _listener.start() ;
      }
      catch ( IOException ex ) {
          tsLogger.i18NLogger.warn_recovery_TransactionStatusManager_2();
      }
   }

   /*
    * Removes the TransactionStatusManager from the object store
    * and closes down the listener thread.

    * this will never work as a finalizer because the listener thread is always running and keeping this
    * instance from being garbage collected. we need a proper shutdonw method which closes the
    * listener socket causing the thread to shut down
    *
   // TODO consider adding a shutdown operation (signature change)
   public void finalize()
   {
       if ( ! _finalizeCalled )
       {
	  _finalizeCalled = true ;

	   _listener.stopListener() ;
	   TransactionStatusManagerItem.removeThis( Utility.getProcessUid() ) ;
      }
   }
    */

   /**
    * Create service and Transaction status manager item.
    */
   private void start( String serviceName, String host, int port )
   {
      try
      {
         Class serviceClass = Thread.currentThread().getContextClassLoader().loadClass( serviceName ) ;

         Service service = (Service) serviceClass.newInstance() ;

         ServerSocket socketServer = getTsmServerSocket(host, port);

         addService( service, socketServer ) ;

         TransactionStatusManagerItem.createAndSave(socketServer.getInetAddress().getHostAddress(), socketServer.getLocalPort() ) ;

         tsLogger.i18NLogger.info_recovery_TransactionStatusManager_3(Integer.toString(socketServer.getLocalPort()),
                 socketServer.getInetAddress().getHostAddress(), serviceName);
      }
      catch ( ClassNotFoundException ex ) {
          tsLogger.i18NLogger.warn_recovery_TransactionStatusManager_4(serviceName);
      }
      catch ( InstantiationException ex ) {
          tsLogger.i18NLogger.warn_recovery_TransactionStatusManager_5(serviceName);
      }
      catch ( IllegalAccessException ex ) {
          tsLogger.i18NLogger.warn_recovery_TransactionStatusManager_6(serviceName);
      }
      catch ( IOException ex ) {
          tsLogger.i18NLogger.warn_recovery_TransactionStatusManager_14(getListenerHostName(), Integer.toString(getListenerPort(-1)));

          throw new FatalError(tsLogger.i18NLogger.get_recovery_TransactionStatusManager_9(), ex);
      }
   }

    public void shutdown()
    {
        if (_listener != null) {
            _listener.stopListener() ;
            TransactionStatusManagerItem.removeThis( Utility.getProcessUid() ) ;
            _listener = null;
        }
    }
    /**
     * Lookup the listener port for the transaction manager
     * @param defValue the value to use if no valid port number can be found
     * @return the listener port
     */
    private int getListenerPort(Integer defValue)
    {
        // has the port already been bound
        if (_port > 0)
            return _port;

        return recoveryPropertyManager.getRecoveryEnvironmentBean().getTransactionStatusManagerPort();
    }

    private String getListenerHostName()
    {
        return recoveryPropertyManager.getRecoveryEnvironmentBean().getTransactionStatusManagerAddress();
    }

    /**
     * Create a new listener socket. If the input paramters are invalid use the config properties
     * to choose the desired address and port to bind the listener to. A port value of -1 is considered
     * invalid.
     *
     * @param hostNameOverride override the config property for the hostname
     * @param portOverride override the config property for the port
     * @return a socket bound to the appropriate host and port
     * @throws IOException if the host name is unknown
     */
    private ServerSocket getTsmServerSocket (String hostNameOverride, int portOverride) throws IOException
    {
        if (_socket != null)
        {
            // the socket has already been created
            return _socket;
        }

        if (_port == -1)
        {
            // a previous attempt to create the socket failed
            throw new com.arjuna.ats.arjuna.exceptions.FatalError(tsLogger.i18NLogger.get_recovery_TransactionStatusManager_13());
        }

        try
        {
            String host = hostNameOverride == null ? getListenerHostName() : hostNameOverride;
            InetAddress bindAddress = Utility.hostNameToInetAddress(host);

            _port = portOverride == -1 ? getListenerPort(null) : portOverride;
            _socket = new ServerSocket(_port, Utility.BACKLOG, bindAddress);

            _port = _socket.getLocalPort();
        }
        catch (UnknownHostException ex)
        {
            _port = -1;

            throw ex;
        }

        return _socket;
    }

    /**
     * Listener thread.
     */
    private Listener _listener ;

    /**
     * Default service run on listener thread.
     */
    private static final String _defaultTsmService = "com.arjuna.ats.arjuna.recovery.ActionStatusService" ;

    /**
     * Flag used to ensure finalize gets called just once.
     */
    private boolean _finalizeCalled = false ;

    /**
     * The listener socket
     */
    private ServerSocket _socket;

    /**
     * Bound port for listener socket
     * A value of -1 means that the attempt to create the socket failed
     */
    private int _port = 0;

    /**
     * Default bind port is any port
     */
    private int DEFAULT_TMS_PORT = 0;
}







