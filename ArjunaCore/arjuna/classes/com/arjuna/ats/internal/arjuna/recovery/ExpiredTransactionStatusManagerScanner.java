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
import java.text.* ;

import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.objectstore.ObjectStore ;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner ;
import com.arjuna.ats.arjuna.state.InputObjectState ;

import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.common.UidHelper;

/**
 * This class is a plug-in module for the recovery manager.  This
 * class is responsible for the removing transaction status manager items 
 * that are too old.
 */

public class ExpiredTransactionStatusManagerScanner implements ExpiryScanner
{
   public ExpiredTransactionStatusManagerScanner()
   {
       if (tsLogger.arjLogger.isDebugEnabled()) {
           tsLogger.arjLogger.debug("ExpiredTransactionStatusManagerScanner created, with expiry time of "+Integer.toString(_expiryTime)+"  seconds");
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

      if (tsLogger.arjLogger.isDebugEnabled()) {
          tsLogger.arjLogger.debug("ExpiredTransactionStatusManagerScanner - scanning to remove items from before "+_timeFormat.format(oldestSurviving));
      }

      try
      {
         InputObjectState uids = new InputObjectState() ;

         // find the uids of all the transaction status manager items
         if ( _objectStore.allObjUids(_itemTypeName, uids) )
         {
            Uid theUid = null;

            boolean endOfUids = false ;

            while (!endOfUids)
            {
               // extract a uid
                
                theUid = UidHelper.unpackFrom(uids);

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
			 tsLogger.i18NLogger.info_recovery_ExpiredTransactionStatusManagerScanner_3(newUid);
			 
			 _objectStore.remove_committed( newUid, _itemTypeName ) ;
                     }
                     else
                     {
                        // if it is not possible to establish a connection
                        // to the Transaction Status Managers' process
                        // then it is removed from the object store.
                        Uid currentUid = newUid ;
                  
                        String process_id = currentUid.getHexPid();
                      
                        TransactionStatusConnector tsc = 
                           new TransactionStatusConnector ( process_id, currentUid ) ;
                         
                        tsc.test( tsmItem ) ;
                  
                        if ( tsc.isDead() )
                        {
                            tsLogger.i18NLogger.info_recovery_ExpiredTransactionStatusManagerScanner_3(newUid);
			    
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
    
    private String      _itemTypeName ;
    private ObjectStore _objectStore ;
    
    private static int _expiryTime = 12 * 60 * 60 ; // default is 12 hours
    
    private static SimpleDateFormat _timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    static
    {
        _expiryTime = recoveryPropertyManager.getRecoveryEnvironmentBean().getTransactionStatusManagerExpiryTime() * 60 * 60;

        if (tsLogger.arjLogger.isDebugEnabled()) {
            tsLogger.arjLogger.debug("Expiry scan interval set to  "+Integer.toString(_expiryTime)+"  seconds");
        }
    }

 
}
