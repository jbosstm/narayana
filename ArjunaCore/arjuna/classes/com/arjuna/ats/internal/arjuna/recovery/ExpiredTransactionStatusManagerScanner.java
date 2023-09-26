/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.recovery ;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.recoveryPropertyManager;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.objectstore.StoreManager;
import com.arjuna.ats.arjuna.recovery.ExpiryScanner;
import com.arjuna.ats.arjuna.state.InputObjectState;
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
       if (tsLogger.logger.isDebugEnabled()) {
           tsLogger.logger.debug("ExpiredTransactionStatusManagerScanner created, with expiry time of "+Integer.toString(_expiryTime)+"  seconds");
       }

      _recoveryStore = StoreManager.getRecoveryStore();
      _itemTypeName = TransactionStatusManagerItem.typeName() ;
   }

   /**
    * This is called periodically by the RecoveryManager
    */
   public void scan ()
   {
      // calculate the time before which items will be removed
      Date oldestSurviving = new Date( new Date().getTime() - _expiryTime * 1000 ) ;

      if (tsLogger.logger.isDebugEnabled()) {
          tsLogger.logger.debug("ExpiredTransactionStatusManagerScanner - scanning to remove items from before "+_timeFormat.format(oldestSurviving));
      }

      try
      {
         InputObjectState uids = new InputObjectState() ;

         // find the uids of all the transaction status manager items
         if ( _recoveryStore.allObjUids(_itemTypeName, uids) )
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
			 tsLogger.logger.debugf("Removing old transaction status manager item %s", newUid);
			 
			 _recoveryStore.remove_committed( newUid, _itemTypeName ) ;
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
                            tsLogger.logger.debugf("Removing old transaction status manager item %s", newUid);
			    
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
    private RecoveryStore _recoveryStore;
    
    private static final int _expiryTime = recoveryPropertyManager.getRecoveryEnvironmentBean()
            .getTransactionStatusManagerExpiryTime() * 60 * 60;
    
    private static final SimpleDateFormat _timeFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");

    /**
     * Block writes expiry scan interval to the log.
     */
    static
    {
        if (tsLogger.logger.isDebugEnabled()) {
            tsLogger.logger.debug("Expiry scan interval set to  "+Integer.toString(_expiryTime)+"  seconds");
        }
    }

 
}