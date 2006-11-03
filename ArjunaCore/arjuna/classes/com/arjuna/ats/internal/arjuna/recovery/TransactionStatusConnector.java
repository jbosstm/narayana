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
 * $Id: TransactionStatusConnector.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.recovery ;

import java.io.* ;
import java.net.* ;
import java.util.* ;

import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.coordinator.ActionStatus ;
import com.arjuna.ats.arjuna.utils.Utility ;

import com.arjuna.ats.arjuna.logging.FacilityCode ;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.common.util.logging.*;

/**
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_1 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_1] - TransactionStatusConnector.delete called erroneously
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_2 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_2] - Connection lost to TransactionStatusManagers' process
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_3 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_3] - Connection lost to TransactionStatusManagers' process
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_4 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_4] - TransactionStatusManager process for uid {0} is ALIVE. connected to host: {1}, port: {2} on socket: {3}
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_5 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_5] - TransactionStatusManager process for uid {0} is DEAD.
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_6 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_6] - Failed to establish connection to server
 */

public class TransactionStatusConnector
{
   /**
    * Recreate TransactionStatusManagerItem and attempt to esablish
    * a connection to the host/port of the TransactionStatusManager.
    */

   public TransactionStatusConnector( String pid, Uid pidUid )
   {
      _pid    = pid ;
      _pidUid = pidUid ;
   }
   
   /**
    * Check that a connection can be made to the Transaction Status
    * Manager's process.
    */

   public boolean test( TransactionStatusManagerItem tsmi )
   {
      _testMode = true ;
      _tsmi = tsmi ;

      boolean ok = establishConnection() ;
      
      if ( !ok )
      {
         setDeadTSM() ;
      }

      return ok ;
   }

   /**
    * If the TransactonStatusManagers' process is deemed dead,
    * then its TransactonStatusManagerItem is removed from
    * the object store.
    */

   public void delete ()
   {
      if ( _dead )
      {
         TransactionStatusManagerItem.removeThis( _pidUid ) ;
      }
      else
      {
	  if (tsLogger.arjLoggerI18N.isWarnEnabled())
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_1");
      }
   }
   
   /**
    * Has the TransactionStatusManagers' process died.
    */

   public boolean isDead ()
   {
      return _dead ;
   }
   
   /**
    * Retrieve the transaction status for the specified transaction,
    * attempts to re-establish connection if necessary.
    */

   public int getTransactionStatus ( String transaction_type, Uid tranUid )
   {
      int status = ActionStatus.INVALID ;

      if ( ! _dead )
      {
         if ( ! _tsmFound )
         {
            // try to establish/re-establish the connection
            _tsmFound = recreateTransactionStatusManagerItem() ;
            
            if ( _tsmFound )
            {
               _tsmFound = establishConnection() ;
            }
         }

         if ( _tsmFound )
         {
            try
            {
               // Send transaction type and transaction Uid to the
               // TransactionStatusManager.
               _to_server.println ( transaction_type ) ;
               _to_server.println ( tranUid.toString() ) ;
               _to_server.flush() ;

	       /*
		* TODO we should optimise this so we only close once
		* all transactions for a particular host have been sent.
		*/

               // Retrieve current status from the TransactionStatusManager.
               String server_data = _from_server.readLine() ;
               status = Integer.parseInt ( server_data ) ;

	       //	       _to_server.close();
	       //	       _from_server.close();
            }
            catch ( IOException ex )
            {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_2");
		}
		
		_tsmFound = false ;
            }
            catch ( Exception other )
            {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_3");
		}
		
		_tsmFound = false ;
            }
         }
      }

      return status ;
   }
   
   /**
    * Assume the Transaction Status Managers' process has died.
    */
   private void setDeadTSM()
   {
      _dead = true ;

      if (_tsmi != null)
	  _tsmi.markAsDead();
   }

   /**
    * Create socket and input/output streams to/from the
    * TransactionStatusManager.
    */

   private boolean establishConnection()
   {
      boolean connectionEstablished = false ;
      
      if ( _tsmi != null )
      {
         try
         {
            String serverHost = _tsmi.host() ;
            int serverPort = _tsmi.port() ;

            _connector_socket = new Socket ( serverHost, serverPort ) ;
            _connector_socket.setSoTimeout ( _socket_timeout_in_msecs ) ;
   
            // streams to and from the TransactionStatusManager
            _from_server = new BufferedReader ( new InputStreamReader( _connector_socket.getInputStream() )) ;
                              
            _to_server = new PrintWriter ( new OutputStreamWriter( _connector_socket.getOutputStream() ) ) ;

            // Check that the process id of the server is the same as
            // this connectors process id.

            String server_pid = _from_server.readLine() ;
          
            if ( Utility.hexStringToInt(server_pid) == Utility.hexStringToInt(_pid) )
            {
               if ( ! _testMode )
               {
                  _to_server.println ( "OK" ) ;
                  _to_server.flush() ;
		  
		  if (tsLogger.arjLoggerI18N.isInfoEnabled())
		  {
		      tsLogger.arjLoggerI18N.info
			  ( "com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_4", 
			    new Object[]{_pid, serverHost, Integer.toString(serverPort), _connector_socket});
		  }
               }
               else
               {
                  _to_server.println ( "TEST" ) ;
                  _to_server.flush() ;
		  //		  _to_server.close();

                  _connector_socket.close() ;
               }
               
               connectionEstablished = true ;
            }
            else
            {
               _to_server.println ( "DEAD" ) ;
               _to_server.flush() ;
	       //	       _to_server.close();

               _connector_socket.close() ;
               
               setDeadTSM() ;
	       
       	       if (tsLogger.arjLoggerI18N.isInfoEnabled())
	       {
		   tsLogger.arjLoggerI18N.info( "com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_5", new Object[]{_pid});
	       }
	       
            }
         }
         catch ( IOException ex )
         {
	     if (tsLogger.arjLoggerI18N.isInfoEnabled())
	     {
		 tsLogger.arjLoggerI18N.info
		     ("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusConnector_6");
	     }
         }

         _attempts_to_establish_connection = connectionEstablished ? 0 : _attempts_to_establish_connection + 1 ;
         
         if ( _attempts_to_establish_connection > _max_attempts_to_establish_connection )
         {
            setDeadTSM() ;
         }
      }
      
      return connectionEstablished ;
   }

   /**
    * Retrieve host/port item stored in the object store.
    */
   private boolean recreateTransactionStatusManagerItem()
   {
      boolean tsmiFound = false ;
      
      if ( _tsmi == null )
      {
         try
         {
            _tsmi = TransactionStatusManagerItem.recreate( _pidUid ) ;

            tsmiFound = true ;
            _attempts_to_recreate_tsmi = 0 ;
         }
         catch ( Exception ex )
         {
            if ( ++_attempts_to_recreate_tsmi > _max_attempts_to_recreate_tsmi )
            {
               setDeadTSM() ;
            }

            _tsmi = null ;
         }
      }

      return tsmiFound ;
   }

   // Process id & process Uid.
   private String _pid ;        // HexString format
   private Uid    _pidUid ;
   
   // Host/port pair for TransactionStatusManager.
   private TransactionStatusManagerItem _tsmi = null ;
   
   // If transaction status manager item exists AND able to 
   // connect to its host/port then _tsmFound = true.
   private boolean _tsmFound = false ;
   
   // Several attempts are made to recreate _tsmi,
   // if limit reached then this connector is marked dead.
   private int _attempts_to_recreate_tsmi = 0 ;
   private int _max_attempts_to_recreate_tsmi = 3 ;
   
   // Several attempts are made to establish a connection to the
   // Transaction Status Manager, if limit reached then this connector
   // is marked dead.
   private int _attempts_to_establish_connection = 0 ;
   private int _max_attempts_to_establish_connection = 3 ;
   
   // Socket to connect to host/port pair maintained in _tsmi.
   private Socket _connector_socket ;
   private int    _socket_timeout_in_msecs = 1000 ;
   
   // IO to/from TransactionStatusManager
   private BufferedReader _from_server;
   private PrintWriter    _to_server;
   
   // Indicates the TransactionStatusManagers' process does not exist.
   private boolean _dead = false ;
   
   // Used to check that a connection can be established to the
   // TransactionStatusManagers' process.
   private boolean _testMode = false ;

}




