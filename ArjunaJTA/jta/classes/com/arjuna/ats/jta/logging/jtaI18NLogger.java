/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.ats.jta.logging;

import static org.jboss.logging.Logger.Level.ERROR;
import static org.jboss.logging.Logger.Level.FATAL;
import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;
import static org.jboss.logging.annotations.Message.Format.MESSAGE_FORMAT;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.objectstore.RecoveryStore;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.jta.recovery.arjunacore.XARecoveryModule;

/**
 * i18n log messages for the jta module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface jtaI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

    @Message(id = 16001, value = "could not get all object Uids.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_alluids();

	@Message(id = 16002, value = "Cannot add resource to table: no XID value available.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_cannotadd();

//	@Message(id = 16003, value = "{0} - could not get class name for {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_classloadfail(String arg0, String arg1);

	@Message(id = 16004, value = "XARecoveryModule setup failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_constfail();

	@Message(id = 16005, value = "{0} - failed to recover XAResource. status is {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_failedtorecover(String arg0, String arg1);

	@Message(id = 16006, value = "{0} - forget threw exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_forgetfailed(String arg0, @Cause() Throwable arg1);

//	@Message(id = 16007, value = "Caught exception: {0} for {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_general(String arg0, String arg1);

	@Message(id = 16008, value = "{0} - caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_generalrecoveryerror(String arg0, @Cause() Throwable arg1);

	@Message(id = 16009, value = "Caught:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_getxaresource(@Cause() Throwable arg0);

//	@Message(id = 16010, value = "{0} - first pass", format = MESSAGE_FORMAT)
//	@LogMessage(level = INFO)
//	public void info_recovery_firstpass(String arg0);

//	@Message(id = 16011, value = "{0} loading {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_info_loading(String arg0, String arg1);

//	@Message(id = 16012, value = "Told not to rollback {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_info_notrollback(String arg0);

	@Message(id = 16013, value = "Rolling back {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_rollingback(String arg0);

//	@Message(id = 16014, value = "Ignoring Xid {0} and leaving for transaction recovery to drive.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_info_rollingbackignore(String arg0);

//	@Message(id = 16015, value = "{0} - second pass", format = MESSAGE_FORMAT)
//	@LogMessage(level = INFO)
//	public void info_recovery_secondpass(String arg0);

	@Message(id = 16016, value = "{0} not an Arjuna XID", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_notaxid(String arg0);

	@Message(id = 16017, value = "No XA recovery nodes specified. May not recover orphans.", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_noxanodes();

	@Message(id = 16018, value = "XARecoveryModule periodicWork failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_objstoreerror(@Cause() Throwable arg0);

	@Message(id = 16019, value = "{0} exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_periodicfirstpass(String arg0, @Cause() Throwable arg1);

	@Message(id = 16020, value = "{0} exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_periodicsecondpass(String arg0, @Cause() Throwable arg1);

	@Message(id = 16021, value = "JTA recovery delayed for {0}; got status {1} so waiting for coordinator driven recovery", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_recoverydelayed(Uid arg0, String arg1);

	@Message(id = 16022, value = "Recovery threw:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_recoveryerror(@Cause() Throwable arg0);

	@Message(id = 16023, value = "JTA failed to recovery {0}; got status {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_recoveryfailed(Uid arg0, String arg1);

//	@Message(id = 16024, value = "{0} - could not remove record for {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_removefailed(String arg0, String arg1);

	@Message(id = 16025, value = "Unexpected recovery error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_unexpectedrecoveryerror(@Cause() Throwable arg0);

//	@Message(id = 16026, value = "{0} - first pass", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_xafirstpass(String arg0);

	@Message(id = 16027, value = "{0} got XA exception {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_xarecovery1(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 16028, value = "{0} got exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_xarecovery2(String arg0, @Cause() Throwable arg1);

	@Message(id = 16029, value = "SynchronizationImple.afterCompletion - failed for {0} with exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_SynchronizationImple(String arg0, @Cause() Throwable arg1);

	@Message(id = 16030, value = "XAOnePhaseResource.pack failed to serialise resource", format = MESSAGE_FORMAT)
	public String get_resources_arjunacore_XAOnePhaseResource_pack();

	@Message(id = 16031, value = "XAOnePhaseResource.rollback for {0} failed with exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_XAOnePhaseResource_rollbackexception(String arg0, @Cause() Throwable arg1);

	@Message(id = 16032, value = "failed to deserialise resource", format = MESSAGE_FORMAT)
	public String get_resources_arjunacore_XAOnePhaseResource_unpack();

	@Message(id = 16033, value = "Unknown recovery type {0}", format = MESSAGE_FORMAT)
	public String get_resources_arjunacore_XAOnePhaseResource_unpackType(String arg0);

	@Message(id = 16034, value = "Being told to assume complete on Xid {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_resources_arjunacore_assumecomplete(String arg0);

	@Message(id = 16035, value = "{0} - null transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_commitnulltx(String arg0);

	@Message(id = 16036, value = "commit on {0} ({1}) failed with exception ${2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_commitxaerror(String arg0, String arg1, String arg2, @Cause() Throwable arg3);

	@Message(id = 16037, value = "Could not find new XAResource to use for recovering non-serializable XAResource {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_norecoveryxa(String arg0);

	@Message(id = 16038, value = "No XAResource to recover {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_noresource(String arg0);

	@Message(id = 16039, value = "onePhaseCommit on {0} ({1}) failed with exception {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_opcerror(String arg0, String arg1, String arg2, @Cause() Throwable arg3);

	@Message(id = 16040, value = "{0} - null transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_opcnulltx(String arg0);

	@Message(id = 16041, value = "prepare on {0} ({1}) failed with exception {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_preparefailed(String arg0, String arg1, String arg2, @Cause() Throwable arg3);

	@Message(id = 16042, value = "{0} - null transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_preparenulltx(String arg0);

	@Message(id = 16043, value = "Exception on attempting to restore XAResource", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_restorestate(@Cause() Throwable arg0);

	@Message(id = 16044, value = "An error occurred during restore_state for XAResource {0} and transaction {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_restorestateerror(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 16045, value = "attempted rollback of {0} ({1}) failed with exception code {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_rollbackerror(String arg0, String arg1, String arg2, @Cause() Throwable arg3);

	@Message(id = 16046, value = "{0} - null transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_rollbacknulltx(String arg0);

	@Message(id = 16047, value = "Could not serialize a Serializable XAResource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_savestate();

	@Message(id = 16048, value = "An error occurred during save_state for XAResource {0} and transaction {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_savestateerror(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 16049, value = "{0} called illegally.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_resources_arjunacore_setvalue(String arg0);

//	@Message(id = 16050, value = "rollback failed with status:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_transaction_arjunacore_abfailunknownstatus();

	@Message(id = 16051, value = "thread is already associated with a transaction!", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_alreadyassociated();

//	@Message(id = 16052, value = "commit failed with status:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_transaction_arjunacore_cmfailunknownstatus();

	@Message(id = 16053, value = "Could not commit transaction.", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_commitwhenaborted();

	@Message(id = 16054, value = "could not register transaction", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_couldnotregister();

	@Message(id = 16055, value = "{0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_delistgeneral(String arg0, @Cause() Throwable arg1);

	@Message(id = 16056, value = "{0} - caught exception during delist : {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_delistresource(String arg0, String arg1, @Cause() Throwable arg2);

//	@Message(id = 16057, value = "Cannot enlist the resource because the transaction is marked for rollback", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_transaction_arjunacore_elistwhenmarkedrollback();

	@Message(id = 16058, value = "Ending suspended RMs failed when rolling back the transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_endsuspendfailed1();

	@Message(id = 16059, value = "Ending suspended RMs failed when rolling back the transaction, but transaction rolled back.", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_endsuspendfailed2();

	@Message(id = 16060, value = "{0} - caught: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_enlisterror(String arg0, String arg1);

	@Message(id = 16061, value = "{0} - XAResource.start returned: {2} for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_enliststarterror(String arg0, String arg1, String arg2, @Cause() Throwable arg3);

	@Message(id = 16062, value = "illegal resource state", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_illresstate();

	@Message(id = 16063, value = "The transaction is not active!", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_inactive();

	@Message(id = 16064, value = "The transaction is in an invalid state!", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_invalidstate();

//	@Message(id = 16065, value = "Caught unexpected exception: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_transaction_arjunacore_isnewrm(String arg0);

	@Message(id = 16066, value = "Failed to create instance of TransactionImporter", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
	public void error_transaction_arjunacore_jca_SubordinationManager_importerfailure(@Cause() Throwable arg0);

	@Message(id = 16067, value = "Failed to create instance of XATerminator", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_transaction_arjunacore_jca_SubordinationManager_terminatorfailure(@Cause() Throwable arg0);

	@Message(id = 16068, value = "Work already active!", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_jca_busy();

	@Message(id = 16069, value = "failed to load Last Resource Optimisation Interface {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_lastResourceOptimisationInterface(String arg0);

	@Message(id = 16070, value = "{0} - could not mark {1} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_markrollback(String arg0, String arg1);

	@Message(id = 16071, value = "{0} caught XAException: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_newtmerror(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 16072, value = "No such transaction!", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_nosuchtx();

	@Message(id = 16073, value = "Current transaction is not an AtomicAction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_notatomicaction();

	@Message(id = 16074, value = "no transaction!", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_notx();

	@Message(id = 16075, value = "null synchronization parameter!", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_nullparam();

	@Message(id = 16076, value = "Resource paramater is null!", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_nullres();

//	@Message(id = 16077, value = "Could not mark transaction as rollback only.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_transaction_arjunacore_rbofail();

	@Message(id = 16078, value = "resource already suspended.", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_ressuspended();

	@Message(id = 16079, value = "Transaction rollback status is:", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_rollbackstatus();

	@Message(id = 16080, value = "Not allowed to terminate subordinate transaction directly.", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_subordinate_invalidstate();

	@Message(id = 16081, value = "The transaction implementation threw a RollbackException", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_syncrollbackexception();

	@Message(id = 16082, value = "Synchronizations are not allowed! Transaction status is", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_syncsnotallowed();

	@Message(id = 16083, value = "Cannot register synchronization because the transaction is in aborted state", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_syncwhenaborted();

	@Message(id = 16084, value = "The transaction implementation threw a SystemException", format = MESSAGE_FORMAT)
	public String get_transaction_arjunacore_systemexception();

	@Message(id = 16085, value = "Caught the following error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_threadexception(@Cause() Throwable arg0);

	@Message(id = 16086, value = "{0} setTransactionTimeout on XAResource {1} threw: {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_timeouterror(String arg0, String arg1, String arg2, @Cause() Throwable arg3);

	@Message(id = 16087, value = "{0} - unknown resource", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_unknownresource(String arg0);

	@Message(id = 16088, value = "Could not call end on a suspended resource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_xaenderror(@Cause() Throwable arg0);

	@Message(id = 16089, value = "{0} - caught: {2} for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transaction_arjunacore_xastart(String arg0, String arg1, String arg2, @Cause() Throwable arg3);

//	@Message(id = 16090, value = "Failed to create instance of TransactionManager", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_TransactionManager_generalfailure();

	@Message(id = 16091, value = "Failed to lookup transaction manager in JNDI context", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_TransactionManager_jndifailure(@Cause() Throwable arg0);

//	@Message(id = 16092, value = "Failed to create instance of UserTransaction", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_UserTransaction_generalfailure();

	@Message(id = 16093, value = "Failed to lookup user transaction in JNDI context", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_UserTransaction_jndifailure(@Cause() Throwable arg0);

//	@Message(id = 16094, value = "Failed to bind the JTA implementations with the appropriate JNDI contexts: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_utils_JNDIManager_jndibindfailure(String arg0);

//	@Message(id = 16095, value = "Failed to bind the JTA transaction manager implementation with the approprite JNDI context: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_utils_JNDIManager_tmjndibindfailure(String arg0);

	@Message(id = 16096, value = "Unable to instantiate TransactionSynchronizationRegistry implementation class!", format = MESSAGE_FORMAT)
	public String get_utils_JNDIManager_tsr1();

//	@Message(id = 16097, value = "Failed to bind the JTA user transaction implementation with the appropriate JNDI context: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_utils_JNDIManager_utjndibindfailure(String arg0);

	@Message(id = 16098, value = "Null exception!", format = MESSAGE_FORMAT)
	public String get_utils_nullexception();

	@Message(id = 16099, value = "Unknown error code:", format = MESSAGE_FORMAT)
	public String get_utils_unknownerrorcode();

	@Message(id = 16100, value = "Xid unset", format = MESSAGE_FORMAT)
	public String get_xa_xidunset();

	@Message(id = 16101, value = "Could not pack XidImple {0}", format = MESSAGE_FORMAT)
	public String get_xid_packerror(Xid xid);

    @Message(id = 16102, value = "The transaction is not active! Uid is {0}", format = MESSAGE_FORMAT)
   	public String get_transaction_arjunacore_inactive(Uid arg0);

    @Message(id = 16103, value = "Error getting the status of the current transaction", format = MESSAGE_FORMAT)
   	public String get_error_getting_tx_status();

    @Message(id = 16104, value = "Error getting the current transaction", format = MESSAGE_FORMAT)
   	public String get_error_getting_current_tx();

    @Message(id = 16105, value = "Could not lookup the TransactionManager", format = MESSAGE_FORMAT)
   	public String get_could_not_lookup_tm();

    @Message(id = 16106, value = "Could not lookup the TransactionSynchronizationRegistry", format = MESSAGE_FORMAT)
   	public String get_could_not_lookup_tsr();

    @Message(id = 16107, value = "Expected an @Transactional annotation at class and/or method level", format = MESSAGE_FORMAT)
   	public String get_expected_transactional_annotation();

    @Message(id = 16108, value = "Wrong transaction on thread", format = MESSAGE_FORMAT)
   	public String get_wrong_tx_on_thread();

    @Message(id = 16109, value = "Contextual is null", format = MESSAGE_FORMAT)
   	public String get_contextual_is_null();

    @Message(id = 16110, value = "Transaction is required for invocation", format = MESSAGE_FORMAT)
   	public String get_tx_required();

    @Message(id = 16111, value = "The node identifier cannot be null", format = MESSAGE_FORMAT)
    public String get_nodename_null();

    @Message(id = 16112, value = "Could not determine commit status of CMR resource {0} and transaction {1}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_resources_arjunacore_restorecrstateerror(String arg0, String arg1, @Cause() Throwable arg2);

    @Message(id = 16113, value = "Xid {0} was committed by resource manager", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
    public void info_resources_arjunacore_rmcompleted(String arg0);

    @Message(id = 16114, value = "Could not load {0} will try to get XAResource from the recovery helpers", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_resources_arjunacore_classnotfound(String arg0);

    @Message(id = 16115, value = "Could not access object store to check for log so will leave record alone", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_could_not_access_object_store(@Cause() Exception e);

	@Message(id = 16116, value = "Failed to create JMS connection", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_create_jms_connection(@Cause() Exception e);

	@Message(id = 16117, value = "Failed to close JMS connection {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_close_jms_connection(String arg0, @Cause() Exception e);

	@Message(id = 16118, value = "Failed to close JMS session {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_close_jms_session(String arg0, @Cause() Exception e);

	@Message(id = 16119, value = "Failed to get transaction", format = MESSAGE_FORMAT)
	String get_failed_to_get_transaction();

	@Message(id = 16120, value = "Failed to get transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_get_transaction(@Cause() Exception e);

	@Message(id = 16121, value = "Failed to get transaction status", format = MESSAGE_FORMAT)
	String get_failed_to_get_transaction_status();

	@Message(id = 16122, value = "Failed to get transaction status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_get_transaction_status(@Cause() Exception e);

	@Message(id = 16123, value = "Failed to register synchronization", format = MESSAGE_FORMAT)
	String get_failed_to_register_synchronization();

	@Message(id = 16124, value = "Failed to register synchronization", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_register_synchronization(@Cause() Exception e);

	@Message(id = 16125, value = "Failed to enlist XA resource", format = MESSAGE_FORMAT)
	String get_failed_to_enlist_xa_resource();

	@Message(id = 16126, value = "Failed to enlist XA resource", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_enlist_xa_resource(@Cause() Exception e);

	@Message(id = 16127, value = "Failed to delist XA resource", format = MESSAGE_FORMAT)
	String get_failed_to_delist_xa_resource();

	@Message(id = 16128, value = "Failed to delist XA resource", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_delist_xa_resource(@Cause() Exception e);

	@Message(id = 16129, value = "Could not end XA resource {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_could_not_end_xar(XAResource xar, @Cause() XAException e1);

	@Message(id = 16130, value = "Subordinate transaction was committed during prepare, this will look like a rollback {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatalSubordinate1PCDuringPrepare(Xid xid);

	@Message(id = 16131, value = "Subordinate transaction was not recovered successfully {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	void warn_could_not_recover_subordinate(Uid uid, @Cause() Exception e);

	@Message(id = 16132, value = "Cannot packt into output object state {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_cant_pack_into_output_object_state(OutputObjectState os, @Cause() Exception e);

	@Message(id = 16133, value = "Cannot create a new instance of Xid of uid {0}, is branch: {1}, eisname: {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_cant_create_xid_of_branch(Uid id, Boolean branch, Integer eisName, @Cause() Exception e);

	@Message(id = 16134, value = "Cannot create a new instance of Xid of base xid {0}, is branch: {1}, eisname: {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_cant_create_xid_of_xid(Xid id, Boolean branch, Integer eisName, @Cause() Exception e);

	@Message(id = 16135, value = "Cannot read object {0} store for xid {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_reading_from_object_store(RecoveryStore recoveryStore, Xid xid, @Cause() Exception e);

	@Message(id = 16136, value = "Cannot unpact state of the xid {0} loaded from recovery store {1} of txn type {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_unpacking_xid_state(Xid xid, RecoveryStore recoveryStore, String type, @Cause() Exception e);

	@Message(id = 16137, value = "Failed to get transaction status of {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	void error_failed_to_get_transaction_status(jakarta.transaction.Transaction txn, @Cause() Exception e);

	@Message(id = 16138, value = "Failed to enlist XA resource {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	void warn_failed_to_enlist_xa_resource(XAResource xares, @Cause() Exception e);

	@Message(id = 16139, value = "Fail to cast class of transaction action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	void error_transaction_class_cast_fail(StateManager action, @Cause() Exception e);

    @Message(id = 16140, value = "No subordinate transaction to  drive for commit with xid: {0}", format = MESSAGE_FORMAT)
    String get_no_subordinate_txn_for_commit(Xid xid);

    @Message(id = 16141, value = "Error committing transaction ''{0}'' for xid: {1}", format = MESSAGE_FORMAT)
    String get_error_committing_transaction(jakarta.transaction.Transaction txn, Xid xid);

    @Message(id = 16142, value = "Not actived transaction ''{0}'' for xid: {1}", format = MESSAGE_FORMAT)
    String get_not_activated_transaction(jakarta.transaction.Transaction txn, Xid xid);

    @Message(id = 16143, value = "Problem during waiting for lock ''{0}'' whilst in state {1}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_intteruptedExceptionOnWaitingXARecoveryModuleLock(XARecoveryModule recModule, String state, @Cause() InterruptedException arg0);

    @Message(id = 16144, value = "No subordinate transaction to drive {0}, xid: {1}", format = MESSAGE_FORMAT)
    String get_no_subordinate_txn_for(String actionToDrive, Xid xid);

    @Message(id = 16145, value = "One phase commit for transaction ''{0}'' does not store data in the object store. "
            + "Recovery is won''t able to decide about outcome. Transaction is marked as heuristic to be decided by administrator.", format = MESSAGE_FORMAT)
    String get_onephase_heuristic_commit_failure(StateManager action);

    @Message(id = 16146, value = "Cannot work with the imported transaction as UID is null.", format = MESSAGE_FORMAT)
    String get_error_imported_transaction_uid_is_null();

    @Message(id = 16147, value = "Cannot recover imported transaction of UID ''{0}'' of transaction ''{1}'' as transaction base Xid is null.", format = MESSAGE_FORMAT)
    String get_error_imported_transaction_base_id_is_null(Uid uid, jakarta.transaction.Transaction txn);

    @Message(id = 16148, value = "Cannot work further as the argument Xid is null.", format = MESSAGE_FORMAT)
    String get_error_xid_is_null();

    @Message(id = 16149, value = "Returned global transaction identifier and branch qualifier are null but format id is not -1. {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_recovery_transaction_id_and_branch_qualifier_are_null_wrong_format_id(String xaResourceAsString);

    @Message(id = 16150, value = "Returned global transaction identifier or branch qualifier is null. {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
    public void info_recovery_transaction_id_or_branch_qualifier_is_null(String xaResourceAsString);

    @Message(id = 16151, value = "Not supported for interception factory with non-weld CDI implementation for bean {0}.", format = MESSAGE_FORMAT)
    String get_not_supported_non_weld_interception(String beanName);

    @Message(id = 16152, value = "TransactionScoped context is not active as there is no active transaction on the thread", format = MESSAGE_FORMAT)
    String get_contextual_is_not_active();

    @Message(id = 16153, value = "Transaction is not allowed for invocation", format = MESSAGE_FORMAT)
    public String get_tx_never();

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
          By convention methods with String return type have prefix get_,
            all others are log methods and have prefix <level>_
    */

}