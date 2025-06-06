/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery ;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.utils.Utility;
import com.arjuna.ats.internal.arjuna.recovery.Listener;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem;
import com.arjuna.common.internal.util.ClassloadingUtility;

/**
 *
 * @author Dave Elsworthy (david_elsworthy@hp.com)
 * @version $Id: TransactionStatusManager.java 2342 2006-03-30 13:06:17Z  $
 * @since HPTS 3.0.
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

         tsLogger.logger.debug(service.getClass().getName() + " starting");

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
    * instance from being garbage collected. we need a proper shutdown method which closes the
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
           Service service = ClassloadingUtility.loadAndInstantiateClass(Service.class, serviceName,  null);
           if(service == null) {
               tsLogger.i18NLogger.warn_recovery_TransactionStatusManager_4(serviceName);
               return;
           }

           ServerSocket socketServer = getTsmServerSocket(host, port);

           addService( service, socketServer ) ;

           TransactionStatusManagerItem.createAndSave(socketServer.getInetAddress().getHostAddress(), socketServer.getLocalPort() ) ;

           if (recoveryPropertyManager.getRecoveryEnvironmentBean().getTransactionStatusManagerPort() == 0) {
               tsLogger.i18NLogger.info_recovery_TransactionStatusManager_3(Integer.toString(socketServer.getLocalPort()),
               socketServer.getInetAddress().getHostAddress(), serviceName);
           } else {
               tsLogger.logger.debugf("TransactionStatusManager started on port %s and host %s with service %s", 
                   Integer.toString(socketServer.getLocalPort()), socketServer.getInetAddress().getHostAddress(), serviceName);
           }
       }
       catch ( IOException ex ) {
           // JBTM-3990 still log the information
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
