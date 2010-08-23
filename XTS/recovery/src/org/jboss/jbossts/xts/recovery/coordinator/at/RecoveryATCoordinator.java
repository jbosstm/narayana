package org.jboss.jbossts.xts.recovery.coordinator.at;

import org.jboss.jbossts.xts.recovery.logging.RecoveryLogger;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;

import com.arjuna.mwlabs.wscf.model.twophase.arjunacore.ATCoordinator;

/**
 * This class is a plug-in module for the recovery manager.
 * It is responsible for recovering failed WSAT ACCoordinator transactions.
 *
 */
public class RecoveryATCoordinator extends ATCoordinator {

   /**
    * Re-creates/activates an AtomicAction for the specified
    * transaction Uid.
    */
   public RecoveryATCoordinator( Uid rcvUid )
   {
      super( rcvUid ) ;
      _activated = activate() ;
   }

    /**
    * Replays phase 2 of the commit protocol.
    */
   public void replayPhase2()
   {
       final int status = status();

       if (RecoveryLogger.logger.isDebugEnabled()) {
           RecoveryLogger.logger.debugv("RecoveryATCoordinator.replayPhase2 recovering {0} ActionStatus is {1}", new Object[]{get_uid(), ActionStatus.stringForm(status)});
       }

       if ( _activated )
       {
           // we only need to rerun phase2 if the action status is  PREPARED, which happens
           // when we crash before or during commit, or COMMITTING, which happens when we get
           // a comms timeout from one of the participants after sending it a COMMIT message.
           // in the former case all participant records will be listed in the prepared list.
           // in the latter case the failed participant record(s) will have been reinstated in the
           // prepared list and the participant stub engine reactivated, where necessary,
           // under the call to activate() when this coordinator was created.

           // we can also arrive here when the action status is ABORTING. This happens when we
           // get a comms timeout from one of the participants after sending it a PREPARE message
           // or if we get a comms timeout from one of the participants after sending it a ROLLBACK
           // message. In either case the failed participant record(s) will be listed in the heuristic
           // list. Since this list is ignored completely by phase2Abort there is no point doing
           // anything here. there are also cases where actionStatus is a heuristic outcome. Once
           // again it is pointless calling phase2Abort since the prepared list is empty. 

       if ((status == ActionStatus.PREPARED) ||
               (status == ActionStatus.COMMITTING) ||
               (status == ActionStatus.COMMITTED) ||
               (status == ActionStatus.H_COMMIT) ||
               (status == ActionStatus.H_MIXED) ||
               (status == ActionStatus.H_HAZARD))
	   {
	       super.phase2Commit( _reportHeuristics ) ;
	   } else if ((status ==  ActionStatus.ABORTED) ||
               (status == ActionStatus.H_ROLLBACK) ||
               (status == ActionStatus.ABORTING) ||
               (status == ActionStatus.ABORT_ONLY))
       {
           super.phase2Abort( _reportHeuristics ) ;
       }

       if (RecoveryLogger.logger.isDebugEnabled()) {
           RecoveryLogger.logger.debugv("RecoveryATCoordinator.replayPhase2( {0} )  finished", new Object[]{get_uid()});
       }
       }
       else
       {
           RecoveryLogger.i18NLogger.warn_coordinator_at_RecoveryATCoordinator_4(get_uid());
       }
   }

   // Flag to indicate that this transaction has been re-activated
   // successfully.
   private boolean _activated = false ;

   // whether heuristic reporting on phase 2 commit is enabled.
   private boolean _reportHeuristics = true ;
}
