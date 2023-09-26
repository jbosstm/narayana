/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.star.logging;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.WARN;
import static org.jboss.logging.Logger.Level.DEBUG;

import javax.transaction.xa.XAException;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

/**
 * atI18N log messages for the rest-at module.
 */
@MessageLogger(projectCode = "AT")
public interface atI18NLogger {

    /*
     * Message IDs are unique and non-recyclable. Don't change the purpose of
     * existing messages. (tweak the message text or params for clarification if you
     * like). Allocate new messages by following instructions at the bottom of the
     * file.
     */


    @LogMessage(level = WARN)
    @Message(id = 27001, value = "Failure while removing participant information from the object store. '%s'")
    void warn_failureRemovingParticipantObjectStore(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27002, value = "Failure while synchronizing participant url with RecoveryManager. '%s'")
    void warn_synchronizeParticipantUrlWithCoordinatorRecoveryManager(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27003, value = "Failed to start the bridge. '%s'")
    void warn_failedToStartBridge(String response, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27004, value = "Failed to stop the bridge. '%s'")
    void warn_failedToStopBridge(String response, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27005, value = "Failed to enlist inbound bridge to the transaction '%s'")
    void warn_failedToEnlistTransaction(String response, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27006, value = "Failed to import transaction. '%s'")
    void warn_failedToImportTransaction(String response, @Cause Throwable t);

    @LogMessage(level = ERROR)
    @Message(id = 27007, value = "FATAL System Exception '%s'")
    void error_systemException(String cause);

    @LogMessage(level = WARN)
    @Message(id = 27008, value = "Exception while  verifying/loading id isInStore InboundBridgeOrphanFilter.")
    void warn_loadInStoreInboundBridgeOrphanFilter(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27009, value = "Exception while  verifying id isInStore InboundBridgeOrphanFilter.")
    void warn_isInStoreInboundBridgeOrphanFilter(@Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27010, value = "XAException occured while subordinate rollback. '%s'")
    void warn_subordinateRollbackXAException(String cause, @Cause XAException e);

    @LogMessage(level = WARN)
    @Message(id = 27011, value = "XAException occured while subordinate commit. '%s'")
    void warn_subordinateCommitXAException(String cause, @Cause XAException e);

    @LogMessage(level = WARN)
    @Message(id = 27012, value = "XAException occured while subordinate vote. '%s'")
    void warn_subordinateVoteXAException(String cause, @Cause XAException e);

    @LogMessage(level = WARN)
    @Message(id = 27013, value = "XAException occured while InboundBridgeRecoveryModule periodicWorkSecondPass. '%s'")
    void warn_inboundBridgeRecoveryModulePeriodicWorkSecondPass(String cause, @Cause XAException e);

    @LogMessage(level = WARN)
    @Message(id = 27014, value = "XAException occured while InboundBridgeRecoveryModule addBridgesToMapping. '%s'")
    void warn_inboundBridgeRecoveryModuleAddBridgesToMapping(String cause, @Cause XAException e);

    @LogMessage(level = WARN)
    @Message(id = 27015, value = "Exception occured while InboundBridgeRecoveryModule getUidsToRecover. '%s'")
    void warn_InboundBridgeRecoveryModulegetUidsToRecover(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27016, value = "Exception occured while Tx Support getIntValue. '%s'")
    void warn_txSupportGetIntValue(String cause, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 27017, value = "Exception occured while Tx Support HttpRequest. '%s'")
    void info_txSupportHttpRequest(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27018, value = "Exception occured while Tx Support AddLocationHeader. '%s'")
    void warn_txSupportAddLocationHeader(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27019, value = "Exception occured while InboundBridgeParticipantDeserializer Participant deserialize. '%s'")
    void warn_deserializeInboundBridgeParticipantDeserializer(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27020, value = "Exception while recovering participant in Recovery Manager. '%s'")
    void warn_recoverParticipantsRecoveryManager(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27021, value = "Exception while recovering participant in Recovery Manager. '%s'")
    void warn_ioRecoverParticipantsRecoveryManager(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27022, value = "Heuristic Exception while recreateParticipantInformation in Recovery Manager. '%s'")
    void warn_heuristicCreateParticipantsRecoveryManager(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27023, value = "Participant Exception while recreateParticipantInformation in Recovery Manager. '%s'")
    void warn_participantCreateParticipantsRecoveryManager(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27024, value = "Failure while persisting participant information. '%s'")
    void warn_persistParticipantInformationRecoveryManager(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27025, value = "Participant Exception while participant rollback in ParticipantResource. '%s'")
    void warn_participantRollbackParticipantResource(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27026, value = "Exception while participant rollback in ParticipantResource. '%s'")
    void warn_heuristicRollbackParticipantResource(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27027, value = "Exception while readOnly participant info in ParticipantResource. '%s'")
    void warn_readOnlyParticipantResource(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27028, value = "ParticipantException while commitOnePhase in ParticipantResource. '%s'")
    void warn_commitOnePhaseParticipantResource(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27029, value = "ParticipantException while prepare in ParticipantResource. '%s'")
    void warn_prepareParticipantResource(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27030, value = "Exception Before completion failed in VolatileParticipantResource. '%s'")
    void warn_beforeVolatileParticipantResource(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27031, value = "Exception After completion failed in VolatileParticipantResource. '%s'")
    void warn_afterVolatileParticipantResource(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27032, value = "Exception while getUids in VolatileParticipantResource. '%s'")
    void warn_getUidsVolatileParticipantResource(String cause, @Cause Throwable t);

    @LogMessage(level = WARN)
    @Message(id = 27033, value = "Exception could not reactivate pending transaction.'%s','%s'")
    void warn_getRecoveringTransactions(String cause, @Cause Throwable t,String uid);

    @LogMessage(level = WARN)
    @Message(id = 27034, value = "Exception TM JAX-RS application failed to start.'%s'")
    void warn_jaxrsTM(String cause, @Cause Throwable t);

    @LogMessage(level = DEBUG)
    @Message(id = 27035, value = "Exception TM JAX-RS application failed to start.")
    void warn_failedToStartTransactionCorrdinator(@Cause Throwable t);


   /*
    Allocate new messages directly above this notice.
      - id: use the next id number in numeric sequence. Don't reuse ids.
      The first two digits of the id(XXyyy) denote the module
        all message in this file should have the same prefix.
      - value: default (English) version of the log message.
      - level: according to severity semantics

      Debug and trace don't get i18n. Everything else MUST be i18n.
      By convention methods with String return type have prefix get_,
        all others are log methods and have prefix <level>_
   */

}