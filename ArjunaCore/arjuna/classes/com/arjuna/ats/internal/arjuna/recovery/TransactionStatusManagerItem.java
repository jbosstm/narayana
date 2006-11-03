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
 * $Id: TransactionStatusManagerItem.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.recovery ;

import java.io.* ;
import java.net.* ;
import java.util.* ;

import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.coordinator.TxControl ;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException ;
import com.arjuna.ats.arjuna.objectstore.ObjectStore ;
import com.arjuna.ats.arjuna.state.InputObjectState ;
import com.arjuna.ats.arjuna.state.OutputObjectState ;
import com.arjuna.ats.arjuna.utils.Utility ;

import com.arjuna.ats.arjuna.logging.FacilityCode ;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.common.util.logging.*;

/**
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_1 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_1] - Problem with removing host/port item {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_2 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_2] - Problem with storing host/port {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_3 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_3] - Problem retrieving host/port {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_4 [com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_4] - Failed to obtain host {0}
 */

// similar to FactoryContactItem
public class TransactionStatusManagerItem
{
    /**
     * Create the instance of a Transaction Status Manager
     * contact item.
     */
    public static boolean createAndSave( int port )
    {
	boolean ret_status = true ;
	
	if ( _singularItem == null )
	    {
		_singularItem = new TransactionStatusManagerItem( port );
		
		ret_status = _singularItem.saveThis();
	    }
	return ret_status ;
    }
    
    /**
     * Get a reference to the Object Store.
     */
    public static ObjectStore getStore()
    {
	if ( _objectStore == null )
	    {
		_objectStore = TxControl.getStore();
	    }
	return _objectStore;
    }
    
    /**
     * Accessor method for host in format xxx.xxx.xxx.xxx
     */
    public String host()
    {
	return _host ;
    }
    
    /**
     * Accessor method for the port used by this object.
     */
    public int port()
    {
	return _port ;
    }
    
    /**
     * The process has died.
     */
    public void markAsDead()
    {
	// ignore if done previously
	if ( ! _markedDead )
	    {
		// the host/port won't work any more, so forget it
		_markedDead = true ;
		_deadTime = new Date() ;
		saveThis() ;
	    }
    }
    
    /**
     * Return time when process marked dead.
     */ 
    public Date getDeadTime()
    {
	return _deadTime ;
    }
    
    /**
     * Returns reference to this transaction status manager item.
     */   
    public static TransactionStatusManagerItem get()
    {
	return _singularItem ;
    }
    
    /**
    * Crash Recovery uses this method to recreate a
    * representation of the Transaction Status Managers
    * host/port pair contact.
    */
    public static TransactionStatusManagerItem recreate ( Uid uid )
    {
	TransactionStatusManagerItem 
	    theItem = new TransactionStatusManagerItem( uid ) ;
	
	if ( theItem.restoreThis() )
	    {
		return theItem ;
	    }
	else
	    {
         return null;
	    }
    }
    
    /**
     * Destroy the host/port pair for the specified process Uid.
     */ 
    public static boolean removeThis( Uid pidUid )
    {
	boolean ret_status = false ;
	
      try
	  {
	      ret_status = getStore().remove_committed( pidUid, _typeName ) ;
	  }
      catch ( ObjectStoreException ex )
	  {
	      if (tsLogger.arjLoggerI18N.isWarnEnabled())
	      {
		  tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_1",
					      new Object[]{ex});
	      }
	  }
      
      return ret_status ;
    }
    
    /**
     * Type used as path into object store for a TransactionStatusManagerItem.
     */
    public static String typeName()
    {
	return _typeName ;
   }
    
    /**
     * Read host/port pair from the ObjectStore using
     * the process Uid as a unique identifier.
     */
    private boolean restoreThis()
   {
       boolean ret_status = false ;
       
       try
      { 
	  InputObjectState objstate = getStore().read_committed( _pidUid,
								 _typeName ) ;
	  
	  if ( restore_state( objstate) )
	      {
            return ret_status = true ;
	      }
      }
       catch ( ObjectStoreException ex )
	   {
	       ret_status = false ;
	       
	       if (tsLogger.arjLoggerI18N.isWarnEnabled())
	       {
		   tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_2",
					       new Object[]{ex});
	       }
	   }
       
       return ret_status ;
   }
    
    /**
     * Retrieve host/port pair from the Object Store.
    */
    private boolean restore_state ( InputObjectState objstate )
    {
	boolean ret_status = false ;
	
	try
        {
	    _host = objstate.unpackString() ;
	    _port = objstate.unpackInt() ;
	    _markedDead = objstate.unpackBoolean() ;
	    
	    if ( _markedDead )
		{
		    long deadtime = objstate.unpackLong() ;
		    _deadTime = new Date( deadtime ) ;
		}  
            
	    ret_status = true ;
	}
	catch ( IOException ex )
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_3",
						new Object[]{ex});
		}
	    }
	
	return ret_status ;
    }
   
    /**
     * Save host/port pair to the Object Store.
     */
    private boolean save_state ( OutputObjectState objstate )
    {
	boolean ret_status = false ;
	
	try
	    {
		objstate.packString( _host ) ;
		objstate.packInt( _port ) ;
		
		objstate.packBoolean( _markedDead ) ;
		
		if ( _markedDead )
		    {
            objstate.packLong( _deadTime.getTime() ) ;
		    }
		
		ret_status = true ;
	    }
	catch ( IOException ex )
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_2",
						new Object[]{ex});
	  }
	    }
	
	return ret_status ;
    }
    
    /**
     * Write host/port pair to the ObjectStore using
     * the process Uid as a unique identifier.
     */
   private boolean saveThis()
    {
	boolean ret_status = false ;
	
	try
	    {
		OutputObjectState objstate = new OutputObjectState();
		
		if ( save_state(objstate) )
		    {
			ret_status = getStore().write_committed ( _pidUid, 
								  _typeName, 
								  objstate ) ;
         }
	    }
	catch ( ObjectStoreException ex )
	    {
		if (tsLogger.arjLoggerI18N.isWarnEnabled())
		{
		    tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_2",
						new Object[]{ex});
		}
	    }
	
	return ret_status ;
   }
    
    /**
     * Constructor which obtains the process uid and host for
     * use with the specified port.
     */
   private TransactionStatusManagerItem ( int port )
    {
	_pidUid = Utility.getProcessUid() ;
	_port = port ;
	
	try
	{
	    _host = InetAddress.getLocalHost().getHostAddress() ;
         
	    if (tsLogger.arjLogger.isInfoEnabled())
	    {
		tsLogger.arjLogger.info
		    ( "TransactionStatusManagerItem - " + "host: " + _host +
		      " port: " + _port ) ;
	    }
	}
	catch ( UnknownHostException ex )
	{
	    if (tsLogger.arjLoggerI18N.isWarnEnabled())
	    {
		tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.TransactionStatusManagerItem_4",
					    new Object[]{ex});
	    }
	}
    }
    
    /**
     * Used by a Recovery Manager to recreate a Transaction
    * status manager contact item.
    */
    private TransactionStatusManagerItem( Uid uid )
    {
	_pidUid = new Uid( uid ) ;
    }
    
    // Process Uid.
    private Uid _pidUid ;
   
    // Relative location in object store for this 'type'.
    private static String _typeName = "/Recovery/TransactionStatusManager" ;
    
    // Host/port pair on which to connect to the Transaction status manager.
    private String _host ;
    private int    _port ;

    // Reference to the object store.
    private static ObjectStore _objectStore = null;
    
    // The singleton instance of this class.
    private static TransactionStatusManagerItem _singularItem = null ;
    
    // Time at which the process for this item has died.
    private Date _deadTime = null ;
    
    // flag indicates dead TSM
    private boolean _markedDead = false ;

}

