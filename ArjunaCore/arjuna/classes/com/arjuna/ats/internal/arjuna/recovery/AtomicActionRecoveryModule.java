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
 * $Id: AtomicActionRecoveryModule.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.recovery ;

import java.io.* ;
import java.net.* ;
import java.util.* ;

import com.arjuna.ats.arjuna.AtomicAction ;
import com.arjuna.ats.arjuna.common.Uid ;
import com.arjuna.ats.arjuna.coordinator.ActionStatus ;
import com.arjuna.ats.arjuna.coordinator.BasicAction ;
import com.arjuna.ats.arjuna.coordinator.TxControl ;
import com.arjuna.ats.arjuna.exceptions.ObjectStoreException ;
import com.arjuna.ats.arjuna.objectstore.ObjectStore ;
import com.arjuna.ats.arjuna.recovery.RecoverAtomicAction ;
import com.arjuna.ats.arjuna.recovery.RecoveryModule ;
import com.arjuna.ats.arjuna.recovery.TransactionStatusConnectionManager ;
import com.arjuna.ats.arjuna.state.InputObjectState ;

import com.arjuna.ats.arjuna.logging.FacilityCode ;
import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.common.util.logging.*;

/**
 * This class is a plug-in module for the recovery manager.
 * It is responsible for recovering failed AtomicAction transactions.
 *
 * @message com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_1 [com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_1] - RecoveryManagerStatusModule: Object store exception: {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_2 [com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_2] - failed to recover Transaction {0} {1}
 * @message com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_3 [com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_3] - failed to access transaction store {0} {1}
*/

public class AtomicActionRecoveryModule implements RecoveryModule
{
   public AtomicActionRecoveryModule()
   {
       if (tsLogger.arjLogger.isDebugEnabled())
       {
	   tsLogger.arjLogger.debug
            ( DebugLevel.CONSTRUCTORS, 
              VisibilityLevel.VIS_PUBLIC,
              FacilityCode.FAC_CRASH_RECOVERY,
              "AtomicActionRecoveryModule created" );
       }

      if (_transactionStore == null)
      {
         _transactionStore = TxControl.getStore() ;
      }
      
      _transactionStatusConnectionMgr = new TransactionStatusConnectionManager() ;
   }

   /**
    * This is called periodically by the RecoveryManager
    */
   public void periodicWorkFirstPass()
   {
      // Transaction type
      boolean AtomicActions = false ;

      // uids per transaction type
      InputObjectState aa_uids = new InputObjectState() ;

      try
      {
	  if (tsLogger.arjLogger.isInfoEnabled())
	  {
	      tsLogger.arjLogger.info( "StatusModule: first pass " );
	  }

	  AtomicActions = _transactionStore.allObjUids( _transactionType, aa_uids );

      }
      catch ( ObjectStoreException ex )
      {
	  if (tsLogger.arjLoggerI18N.isWarnEnabled())
	  {
	      tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_1",
					  new Object[]{ex});
	  }
      }

      if ( AtomicActions )
      {
         _transactionUidVector = processTransactions( aa_uids ) ;
      }
   }

   public void periodicWorkSecondPass()
   {
       if (tsLogger.arjLogger.isInfoEnabled())
       {
         tsLogger.arjLogger.info( "AtomicActionRecoveryModule: Second pass " );
       }
       
       processTransactionsStatus() ;
   }

    protected AtomicActionRecoveryModule (String type)
    {
       if (tsLogger.arjLogger.isDebugEnabled())
       {
	   tsLogger.arjLogger.debug
            ( DebugLevel.CONSTRUCTORS, 
              VisibilityLevel.VIS_PUBLIC,
              FacilityCode.FAC_CRASH_RECOVERY,
              "AtomicActionRecoveryModule created" );
       }

      if (_transactionStore == null)
      {
         _transactionStore = TxControl.getStore() ;
      }
      
      _transactionStatusConnectionMgr = new TransactionStatusConnectionManager() ;
      _transactionType = type;

    }
    
   private void doRecoverTransaction( Uid recoverUid )
   {
      boolean commitThisTransaction = true ;
      
      // Retrieve the transaction status from its original process.
      int theStatus = _transactionStatusConnectionMgr.getTransactionStatus( _transactionType, recoverUid ) ;
                      
      boolean inFlight = isTransactionInMidFlight( theStatus ) ;

      String Status = ActionStatus.stringForm( theStatus ) ;

      if (tsLogger.arjLogger.isDebugEnabled())
      {
	  tsLogger.arjLogger.debug
	      ( DebugLevel.FUNCTIONS,
		VisibilityLevel.VIS_PUBLIC,
		FacilityCode.FAC_CRASH_RECOVERY,
		"transaction type is "+ _transactionType + " uid is " +
		recoverUid.toString() + "\n ActionStatus is " + Status +
		" in flight is " + inFlight ) ;
      }
	 
      if ( ! inFlight )
      {
         try
         {
            RecoverAtomicAction rcvAtomicAction = 
               new RecoverAtomicAction( recoverUid, theStatus ) ;

            rcvAtomicAction.replayPhase2() ;
         }
         catch ( Exception ex )
         {
	     if (tsLogger.arjLoggerI18N.isWarnEnabled())
	     {
		 tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_2",
					     new Object[]{recoverUid.toString(), ex});
	     }
         }
      }
   }

   private boolean isTransactionInMidFlight( int status )
   {
      boolean inFlight = false ;
      
      switch ( status )
      {
         // these states can only come from a process that is still alive
         case ActionStatus.RUNNING    :
         case ActionStatus.ABORT_ONLY :
         case ActionStatus.PREPARING  :
         case ActionStatus.COMMITTING :
         case ActionStatus.ABORTING   :
         case ActionStatus.PREPARED   :
            inFlight = true ;
            break ;
            
         // the transaction is apparently still there, but has completed its
         // phase2. should be safe to redo it.
         case ActionStatus.COMMITTED  :
         case ActionStatus.H_COMMIT   :
         case ActionStatus.H_MIXED    :
         case ActionStatus.H_HAZARD   :
         case ActionStatus.ABORTED    :
         case ActionStatus.H_ROLLBACK :
            inFlight = false ;
            break ;

         // this shouldn't happen 
         case ActionStatus.INVALID :
         default:
            inFlight = false ; 
      }

      return inFlight ;
   }

   private Vector processTransactions( InputObjectState uids )
   {
      Vector uidVector = new Vector() ;
      
      if (tsLogger.arjLogger.isDebugEnabled())
      {
	  tsLogger.arjLogger.debug( DebugLevel.FUNCTIONS,
				    VisibilityLevel.VIS_PUBLIC,
				    FacilityCode.FAC_CRASH_RECOVERY,
				    "processing " + _transactionType 
				    + " transactions" ) ;
      }
      
      Uid theUid = new Uid( Uid.nullUid() );

      boolean moreUids = true ;

      while (moreUids)
      {
         try
         {
            theUid.unpack( uids ) ;

            if (theUid.equals( Uid.nullUid() ))
            {
               moreUids = false;
            }
            else
            {
               Uid newUid = new Uid( theUid ) ;

	       if (tsLogger.arjLogger.isDebugEnabled())
	       {
		   tsLogger.arjLogger.debug
		       ( DebugLevel.FUNCTIONS,
			 VisibilityLevel.VIS_PUBLIC,
			 FacilityCode.FAC_CRASH_RECOVERY,
			 "found transaction "+ newUid ) ;
	       }
	       
               uidVector.addElement( newUid ) ;
            }
         }
         catch ( Exception ex )
         {
            moreUids = false;
         }
      }
      return uidVector ;
   }

   private void processTransactionsStatus()
   {
      // Process the Vector of transaction Uids
      Enumeration transactionUidEnum = _transactionUidVector.elements() ;

      while ( transactionUidEnum.hasMoreElements() )
      {
         Uid currentUid = (Uid) transactionUidEnum.nextElement();

         try
         {
            if ( _transactionStore.currentState( currentUid, _transactionType ) != ObjectStore.OS_UNKNOWN )
            {
               doRecoverTransaction( currentUid ) ;
            }
         }
         catch ( ObjectStoreException ex )
         {
	     if (tsLogger.arjLogger.isWarnEnabled())
	     {
		 tsLogger.arjLoggerI18N.warn("com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_3",
					     new Object[]{currentUid.toString(), ex});
	     }
         }
      }
   }
   
   // 'type' within the Object Store for AtomicActions. 
   private String _transactionType = new AtomicAction().type() ;
   
   // Array of transactions found in the object store of the
   // AtomicAction type.
   private Vector _transactionUidVector = null ;

   // Reference to the Object Store.
   private static ObjectStore _transactionStore = null ;

   // This object manages the interface to all TransactionStatusManagers
   // processes(JVMs) on this system/node.
   private TransactionStatusConnectionManager _transactionStatusConnectionMgr ;
    
}

