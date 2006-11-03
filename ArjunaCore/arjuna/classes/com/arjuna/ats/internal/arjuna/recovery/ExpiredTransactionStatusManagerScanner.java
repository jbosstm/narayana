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
 * $Id: ExpiredTransactionStatusManagerScanner.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.recovery ;

import java.util.* ;
import java.io.PrintWriter ;
import java.text.* ;

import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStore ;
import com.arjuna.common.util.propertyservice.PropertyManager ;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner ;
import com.arjuna.ats.arjuna.recovery.RecoveryEnvironment ;
import com.arjuna.ats.arjuna.state.InputObjectState ;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;

import com.arjuna.common.util.logging.*;

/**
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_1 [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_1] - ExpiredTransactionStatusManagerScanner created, with expiry time of {0}  seconds
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_2 [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_2] - ExpiredTransactionStatusManagerScanner - scanning to remove items from before {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_3 [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_3] - Removing old transaction status manager item {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_4 [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_4] - Expiry scan interval set to  {0}  seconds
 * @message com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_5 [com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_5] - {0}  has inappropriate value ({1})

/**
 * This class is a plug-in module for the recovery manager.  This
 * class is responsible for the removing ransaction status manager items 
 * that are too old.
 */

public class ExpiredTransactionStatusManagerScanner implements ExpiryScanner
{

   public ExpiredTransactionStatusManagerScanner()
   {
       if (tsLogger.arjLoggerI18N.isDebugEnabled())
       {
	   tsLogger.arjLoggerI18N.debug(DebugLevel.CONSTRUCTORS, VisibilityLevel.VIS_PUBLIC,
					FacilityCode.FAC_CRASH_RECOVERY, 
					"com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_1",
					new Object[]{Integer.toString(_expiryTime)});
      }

      _objectStore  = TransactionStatusManagerItem.getStore() ;
      _itemTypeName = TransactionStatusManagerItem.typeName() ;
   }

   /**
    * This is called periodically by the RecoveryManager
    */
   public void scan ()
   {
      // calculate the time before which items will be removed
      Date oldestSurviving = new Date( new Date().getTime() - _expiryTime * 1000 ) ;

      if (tsLogger.arjLoggerI18N.isDebugEnabled())
      {
	  tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					FacilityCode.FAC_CRASH_RECOVERY, 
					"com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_2",
					new Object[]{_timeFormat.format(oldestSurviving)});
      }

      try
      {
         InputObjectState uids = new InputObjectState() ;

         // find the uids of all the transaction status manager items
         if ( _objectStore.allObjUids(_itemTypeName, uids) )
         {
            Uid theUid = new Uid(Uid.nullUid()) ;

            boolean endOfUids = false ;

            while (!endOfUids)
            {
               // extract a uid
               theUid.unpack(uids) ;

               if (theUid.equals(Uid.nullUid()))
                  endOfUids = true ;
               else
               {
                  Uid newUid = new Uid(theUid) ;

                  TransactionStatusManagerItem 
                     tsmItem = TransactionStatusManagerItem.recreate( newUid ) ;
           
                  if ( tsmItem != null )
                  {
                     Date timeOfDeath = tsmItem.getDeadTime() ;
              
                     if ( timeOfDeath != null && timeOfDeath.before(oldestSurviving) )
                     {
			 if (tsLogger.arjLoggerI18N.isInfoEnabled())
			 {
			     tsLogger.arjLoggerI18N.info("com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_3", new Object[]{newUid});
			 }
			 
			 _objectStore.remove_committed( newUid, _itemTypeName ) ;
                     }
                     else
                     {
                        // if it is not possible to establish a connection
                        // to the Transaction Status Managers' process
                        // then it is removed from the object store.
                        Uid currentUid = newUid ;
                  
                        String process_id = get_process_id( currentUid ) ;
                      
                        TransactionStatusConnector tsc = 
                           new TransactionStatusConnector ( process_id, currentUid ) ;
                         
                        tsc.test( tsmItem ) ;
                  
                        if ( tsc.isDead() )
                        {
			    if (tsLogger.arjLoggerI18N.isInfoEnabled())
			    {
				tsLogger.arjLoggerI18N.info("com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_3", new Object[]{newUid});
			    }
			    
                           tsc.delete() ;
                           tsc = null ;
                        }
                     }
                  }
               }
            }
         }
      }
      catch ( Exception e )
	  {
	      // end of uids!
	  }
   }
    
    public boolean toBeUsed()
    {
	return _expiryTime != 0 ;
    }
    
    /**
     * Extract the process identifier from the supplied Uid.
     */
    private String get_process_id ( Uid uid )
    {
	// process id accessor on Uid needed!
	// this is just a hack
	String strUid = uid.toString() ;
	StringTokenizer st = new StringTokenizer( strUid, ":" ) ;
	st.nextToken() ;
	String process_id_in_Hex = st.nextToken() ;
	
	return process_id_in_Hex ;
    }
    
    private String      _itemTypeName ;
    private ObjectStore _objectStore ;
    
    private static int _expiryTime = 12 * 60 * 60 ; // default is 12 hours
    
    private static SimpleDateFormat _timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
    
    static
    {
	
	String expiryTimeString = arjPropertyManager.propertyManager.getProperty(RecoveryEnvironment.TRANSACTION_STATUS_MANAGER_EXPIRY_TIME);
	
	if ( expiryTimeString != null )
	 {
	   try
	    {
		Integer expiryTimeInteger = new Integer(expiryTimeString) ;
		// convert to seconds
		_expiryTime = expiryTimeInteger.intValue() * 60 * 60 ;
		
		if (tsLogger.arjLoggerI18N.isDebugEnabled())
		{
		    tsLogger.arjLoggerI18N.debug( DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PRIVATE,
						  FacilityCode.FAC_CRASH_RECOVERY, 
						  "com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_4", new Object[]{Integer.toString(_expiryTime)});
		}
	    }
         catch ( NumberFormatException e )
         {
	     if (tsLogger.arjLoggerI18N.isWarnEnabled())
	     {
		 tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.ExpiredTransactionStatusManagerScanner_5", new Object[]{RecoveryEnvironment.TRANSACTION_STATUS_MANAGER_EXPIRY_TIME, expiryTimeString});
	     }
         }
      }
   }

 
}
