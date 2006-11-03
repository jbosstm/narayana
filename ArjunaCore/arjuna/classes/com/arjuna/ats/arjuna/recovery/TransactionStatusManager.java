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
import java.util.* ;

import com.arjuna.common.util.propertyservice.PropertyManager;
import com.arjuna.ats.arjuna.utils.Utility ;
import com.arjuna.ats.internal.arjuna.recovery.Listener ;
import com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem ;
import com.arjuna.ats.internal.arjuna.utils.SocketProcessId;
import com.arjuna.ats.arjuna.common.arjPropertyManager;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

/**
 * This implementation is tied closely with the socket/port version of
 * getpid. If a pid is obtained via a port, then this class will obtain
 * its socket/port from that implementation rather than create a new
 * port: since the socket/port version of getpid is guaranteed to be
 * executed first.
 *
 * @author Dave Elsworthy (david_elsworthy@hp.com)
 * @version $Id: TransactionStatusManager.java 2342 2006-03-30 13:06:17Z  $
 * @since HPTS 3.0.
 *
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_1 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_1] - Starting service {0} on port {1}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_2 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_2] - Listener failed
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_3 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_3] - TransactionStatusManager started on port {0} with service {1}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_4 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_4] - Class not found: {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_5 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_5] - Failed to instantiate service class: {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_6 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_6] - Illegal access to service class: {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_7 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_7] - Failed to create server socket on port: {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_8 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_8] - Invalid port specified {0}
 * @message com.arjuna.ats.arjuna.recovery.TransactionStatusManager_9 [com.arjuna.ats.arjuna.recovery.TransactionStatusManager_9] - Could not get unique port.
 */

public class TransactionStatusManager
{
   public TransactionStatusManager()
   {
      int port = getTsmPort();

      start( _defaultTsmService, port ) ;
   }
   
   public TransactionStatusManager( int port )
   {
      start( _defaultTsmService, port ) ;
   }
    
   public TransactionStatusManager( String serviceName )
   {
      int port = getTsmPort();
       
      start( serviceName, port ) ;
   }
    
   public TransactionStatusManager( String serviceName, int port  )
   {
      start( serviceName, port ) ;
   }
   
   /**
    * The work item to be executed.
    */
   public void addService( Service service, ServerSocket serverSocket )
   {
      try
      {
         _listener = new Listener( serverSocket, service );
         _listener.setDaemon(true);

	 if (tsLogger.arjLoggerI18N.isInfoEnabled())
	 {
	     tsLogger.arjLoggerI18N.info("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_1", 
					 new Object[]{service.getClass().getName(), 
							  Integer.toString(serverSocket.getLocalPort())});
	 }

         _listener.start() ;
      }
      catch ( IOException ex )
      {
	  tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_2");
      }
   }
      
   /**
    * Removes the TransactionStatusManager from the object store
    * and closes down the listener thread.
    */
   public void finalize()
   {
       if ( ! _finalizeCalled )
       {
	  _finalizeCalled = true ;

	   _listener.stopListener() ;
	   TransactionStatusManagerItem.removeThis( Utility.getProcessUid() ) ;
      } 
   }
   
   /**
    * Create service and Transaction status manager item.
    */
   private void start( String serviceName, int port )
   {
      int localPort = 0;

      try
      {
         Class serviceClass = Thread.currentThread().getContextClassLoader().loadClass( serviceName ) ;
   
         Service service = (Service) serviceClass.newInstance() ;
            
         ServerSocket socketServer = getTsmServerSocket(port);
         
         localPort = socketServer.getLocalPort();

         addService( service, socketServer ) ;
   
         TransactionStatusManagerItem.createAndSave( socketServer.getLocalPort() ) ;

         if (tsLogger.arjLoggerI18N.isInfoEnabled())
	 {
	     tsLogger.arjLoggerI18N.info("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_3", 
					  new Object[]{Integer.toString(localPort), serviceName});
	 }
      }
      catch ( ClassNotFoundException ex )
      {
	  if (tsLogger.arjLoggerI18N.isWarnEnabled())
	  {
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_4", 
					  new Object[]{serviceName});
	  }
      }
      catch ( InstantiationException ex )
      {
	  if (tsLogger.arjLoggerI18N.isWarnEnabled())
	  {
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_5", 
					  new Object[]{serviceName});
	  }
      }
      catch ( IllegalAccessException ex )
      {
	  if (tsLogger.arjLoggerI18N.isWarnEnabled())
	  {
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_6", 
					  new Object[]{serviceName});
	  }
      }
      catch ( IOException ex )
      {
	  if (tsLogger.arjLoggerI18N.isWarnEnabled())
	  {
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_7", 
					  new Object[]{Integer.toString(localPort)});
	  }

	  throw new com.arjuna.ats.arjuna.exceptions.FatalError(tsLogger.log_mesg.getString("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_9"));
      }
   }
   
    /**
     * If the socket based version of getpid is being used, then it will
     * have already assigned a port and created a ServerSocket. We should
     * use this - don't want too many ports assigned to a given process,
     * especially if they aren't used. In which case, the port parameter
     * is not used, because we got it from the getpid class anyway.
     */

private static final ServerSocket getTsmServerSocket (int port) throws IOException
    {
	ServerSocket socket = SocketProcessId.getSocket();
	
	return ((socket == null) ? new ServerSocket(port) : socket);
    }
    
   /**
    * Return the port specified by the property
    * com.arjuna.ats.arjuna.recovery.TransactionStatusManagerPort,
    * otherwise return a default port.
    *
    * If the socket based version of getpid is being used, then it will
    * already have assigned our port, so use that.
    */

private static final int getTsmPort ()
   {
       if (SocketProcessId.getSocket() == null)
       {
	   int port = _defaultTsmPort ;
	   
	   // TODO these properties should be documented!!

	   String tsmPortStr = arjPropertyManager.propertyManager.getProperty("com.arjuna.ats.arjuna.recovery.transactionStatusManagerPort" ) ;
	   
	   if ( tsmPortStr != null )
	   {
	       try
	       {
		   port = Integer.parseInt( tsmPortStr ) ;
	       }
	       catch ( Exception ex )
	       {
		   if (tsLogger.arjLoggerI18N.isWarnEnabled())
		   {
		       tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.TransactionStatusManager_8", 
						   new Object[]{ex});
		   }
	       }
	   }

	   return port ;
       }
       else
	   return SocketProcessId.getSocket().getLocalPort();
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
     * Default port is any free port.
     */
    private static final int _defaultTsmPort = 0 ;
    
    /**
     * Flag used to ensure finalize gets called just once.
     */ 
    private boolean _finalizeCalled = false ;

}







