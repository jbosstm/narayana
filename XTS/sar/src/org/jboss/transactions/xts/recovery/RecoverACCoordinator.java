package org.jboss.transactions.xts.recovery;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.logging.tsLogger;
import com.arjuna.ats.arjuna.logging.FacilityCode;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ACCoordinator;
import com.arjuna.common.util.logging.DebugLevel;
import com.arjuna.common.util.logging.VisibilityLevel;

/**
 * This class is a plug-in module for the recovery manager.
 * It is responsible for recovering failed ACCoordinator transactions.
 *
 * @message com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_1 [com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_1] - RecoveryManagerStatusModule: Object store exception: {0}
 * @message com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_2 [com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_2] - failed to recover Transaction {0} {1}
 * @message com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_3 [com.arjuna.ats.internal.arjuna.recovery.AtomicActionRecoveryModule_3] - failed to access transaction store {0} {1}
*/
public class RecoverACCoordinator extends ACCoordinator {

    // TODO: refactor RecoverAtomicAction so that this can subclass it to remove dupl?

   /**
    * Re-creates/activates an AtomicAction for the specified
    * transaction Uid.
    */
   public RecoverACCoordinator ( Uid rcvUid, int theStatus )
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
       if (tsLogger.arjLoggerI18N.debugAllowed())
       {
	   tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					FacilityCode.FAC_CRASH_RECOVERY,
					"com.arjuna.ats.arjuna.recovery.RecoverAtomicAction_1",
					new Object[]{get_uid(), ActionStatus.stringForm(_theStatus)});
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
	   else
	   {
	       if (tsLogger.arjLoggerI18N.isWarnEnabled())
	       {
		   tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.RecoverAtomicAction_2",
					       new Object[]{ActionStatus.stringForm(_theStatus)});
	       }
	   }

	   if (tsLogger.arjLoggerI18N.debugAllowed())
	   {
	       tsLogger.arjLoggerI18N.debug(DebugLevel.FUNCTIONS, VisibilityLevel.VIS_PUBLIC,
					    FacilityCode.FAC_CRASH_RECOVERY,
					    "com.arjuna.ats.arjuna.recovery.RecoverAtomicAction_3",
					    new Object[]{get_uid()});
	   }
       }
       else
       {
	   tsLogger.arjLoggerI18N.warn("com.arjuna.ats.arjuna.recovery.RecoverAtomicAction_4");
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
