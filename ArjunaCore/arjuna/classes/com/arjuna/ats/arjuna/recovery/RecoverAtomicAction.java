/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.arjuna.recovery ;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.internal.arjuna.recovery.AtomicActionExpiryScanner;

public class RecoverAtomicAction extends AtomicAction
{
   /**
    * Re-creates/activates an AtomicAction for the specified
    * transaction Uid.
    */
   public RecoverAtomicAction ( Uid rcvUid, int theStatus )
   {
      super( rcvUid ) ;
      _theStatus = theStatus ;
      _activated = activate() ;
   }
   
   /**
    * Replays phase 2 of the commit protocol.
    */
   public void replayPhase2()
   {
       if (tsLogger.logger.isDebugEnabled()) {
           tsLogger.logger.debug("RecoverAtomicAction.replayPhase2 recovering "+get_uid()+" ActionStatus is "+ActionStatus.stringForm(_theStatus));
       }

       if ( _activated )
       {
	   if ( (_theStatus == ActionStatus.PREPARED) ||
		(_theStatus == ActionStatus.COMMITTING) ||
		(_theStatus == ActionStatus.COMMITTED) ||
		(_theStatus == ActionStatus.H_COMMIT) ||
		(_theStatus == ActionStatus.H_MIXED) ||
		(_theStatus == ActionStatus.H_HAZARD) )
	   {
	       super.phase2Commit( _reportHeuristics ) ;
	   }
	   else if ( (_theStatus == ActionStatus.ABORTED) ||
		     (_theStatus == ActionStatus.H_ROLLBACK) ||
		     (_theStatus == ActionStatus.ABORTING) ||
		     (_theStatus == ActionStatus.ABORT_ONLY) )
	   {
	       super.phase2Abort( _reportHeuristics ) ;
	   }
	   else {
           tsLogger.i18NLogger.warn_recovery_RecoverAtomicAction_2(ActionStatus.stringForm(_theStatus));
       }

	   if (tsLogger.logger.isDebugEnabled()) {
           tsLogger.logger.debug("RecoverAtomicAction.replayPhase2( "+get_uid()+" )  finished");
       }
       }
       else {
           tsLogger.i18NLogger.warn_recovery_RecoverAtomicAction_4(get_uid());

           /*
          * Failure to activate so move the log. Unlikely to get better automatically!
          */

           AtomicActionExpiryScanner scanner = new AtomicActionExpiryScanner();

           try {
               scanner.moveEntry(get_uid());
           }
           catch (final Exception ex) {
               tsLogger.i18NLogger.warn_recovery_RecoverAtomicAction_5(get_uid());
           }
       }
   }
   
   // Current transaction status 
   // (retrieved from the TransactionStatusManager)
   private int _theStatus ;

   // Flag to indicate that this transaction has been re-activated
   // successfully.
   private boolean _activated = false ;

   // whether heuristic reporting on phase 2 commit is enabled.
   private boolean _reportHeuristics = true ;

}