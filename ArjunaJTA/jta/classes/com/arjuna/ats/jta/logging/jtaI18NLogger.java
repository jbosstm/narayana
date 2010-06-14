/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package com.arjuna.ats.jta.logging;

import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

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
	public void log_recovery_alluids();

	@Message(id = 16002, value = "Cannot add resource to table: no XID value available.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_cannotadd();

	@Message(id = 16003, value = "{0} - could not get class name for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_classloadfail(String arg0, String arg1);

	@Message(id = 16004, value = "{0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_constfail(String arg0);

	@Message(id = 16005, value = "{0} - failed to recover XAResource.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_failedtorecover(String arg0);

	@Message(id = 16006, value = "{0} - forget threw: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_forgetfailed(String arg0, String arg1);

	@Message(id = 16007, value = "Caught exception: {0} for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_general(String arg0, String arg1);

	@Message(id = 16008, value = "{0} - caught {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_generalrecoveryerror(String arg0, String arg1);

	@Message(id = 16009, value = "Caught:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_getxaresource();

	@Message(id = 16010, value = "{0} - first pass", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_info_firstpass(String arg0);

	@Message(id = 16011, value = "{0} loading {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_info_loading(String arg0, String arg1);

	@Message(id = 16012, value = "Told not to rollback {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_info_notrollback(String arg0);

	@Message(id = 16013, value = "Rolling back {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_info_rollingback(String arg0);

	@Message(id = 16014, value = "Ignoring Xid {0} and leaving for transaction recovery to drive.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_info_rollingbackignore(String arg0);

	@Message(id = 16015, value = "{0} - second pass", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_info_secondpass(String arg0);

	@Message(id = 16016, value = "{0} not an Arjuna XID", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_notaxid(String arg0);

	@Message(id = 16017, value = "No XA recovery nodes specified. May not recover orphans.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_noxanodes();

	@Message(id = 16018, value = "{0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_objstoreerror(String arg0);

	@Message(id = 16019, value = "{0} exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_periodicfirstpass(String arg0);

	@Message(id = 16020, value = "{0} exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_periodicsecondpass(String arg0);

	@Message(id = 16021, value = "JTA recovery delayed for {0}; got status {1} so waiting for coordinator driven recovery", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_recoverydelayed(String arg0, String arg1);

	@Message(id = 16022, value = "Recovery threw:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_recoveryerror();

	@Message(id = 16023, value = "JTA failed to recovery {0}; got status {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_recoveryfailed(String arg0, String arg1);

	@Message(id = 16024, value = "{0} - could not remove record for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_removefailed(String arg0, String arg1);

	@Message(id = 16025, value = "Unexpceted recovery error:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_unexpectedrecoveryerror();

	@Message(id = 16026, value = "{0} - first pass", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_xafirstpass(String arg0);

	@Message(id = 16027, value = "{0} got XA exception {1}, {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_xarecovery1(String arg0, String arg1, String arg2);

	@Message(id = 16028, value = "{0} got exception {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_xarecovery2(String arg0, String arg1);

	@Message(id = 16029, value = "SynchronizationImple.afterCompletion - failed for {0} with exception {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_SynchronizationImple(String arg0, String arg1);

	@Message(id = 16030, value = "XAOnePhaseResource.pack failed to serialise resource {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_XAOnePhaseResource_pack(String arg0);

	@Message(id = 16031, value = "XAOnePhaseResource.rollback for {0} failed with exception {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_XAOnePhaseResource_rollbackexception(String arg0, String arg1);

	@Message(id = 16032, value = "failed to deserialise resource {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_XAOnePhaseResource_unpack(String arg0);

	@Message(id = 16033, value = "Unknown recovery type {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_XAOnePhaseResource_unpackType(String arg0);

	@Message(id = 16034, value = "Being told to assume complete on Xid {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_assumecomplete(String arg0);

	@Message(id = 16035, value = "{0} - null transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_commitnulltx(String arg0);

	@Message(id = 16036, value = "commit on {0} ({1}) failed with exception {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_commitxaerror(String arg0, String arg1, String arg2);

	@Message(id = 16037, value = "Could not find new XAResource to use for recovering non-serializable XAResource {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_norecoveryxa(String arg0);

	@Message(id = 16038, value = "No XAResource to recover {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_noresource(String arg0);

	@Message(id = 16039, value = "onePhaseCommit on {0} ({1}) failed with exception {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_opcerror(String arg0, String arg1, String arg2);

	@Message(id = 16040, value = "{0} - null transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_opcnulltx(String arg0);

	@Message(id = 16041, value = "prepare on {0} ({1}) failed with exception {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_preparefailed(String arg0, String arg1, String arg2);

	@Message(id = 16042, value = "{0} - null transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_preparenulltx(String arg0);

	@Message(id = 16043, value = "Exception on attempting to restore XAResource", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_restorestate();

	@Message(id = 16044, value = "An error occurred during restore_state for XAResource {0} and transaction {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_restorestateerror(String arg0, String arg1);

	@Message(id = 16045, value = "attempted rollback of {0} ({1}) failed with exception {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_rollbackerror(String arg0, String arg1, String arg2);

	@Message(id = 16046, value = "{0} - null transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_rollbacknulltx(String arg0);

	@Message(id = 16047, value = "Could not serialize a Serializable XAResource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_savestate();

	@Message(id = 16048, value = "An error occurred during save_state for XAResource {0} and transaction {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_savestateerror(String arg0, String arg1);

	@Message(id = 16049, value = "{0} called illegally.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_resources_arjunacore_setvalue(String arg0);

	@Message(id = 16050, value = "rollback failed with status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_abfailunknownstatus();

	@Message(id = 16051, value = "thread is already associated with a transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_alreadyassociated();

	@Message(id = 16052, value = "commit failed with status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_cmfailunknownstatus();

	@Message(id = 16053, value = "Could not commit transaction.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_commitwhenaborted();

	@Message(id = 16054, value = "could not register transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_couldnotregister();

	@Message(id = 16055, value = "{0} caught exception {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_delistgeneral(String arg0, String arg1);

	@Message(id = 16056, value = "{0} - caught exception during delist : {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_delistresource(String arg0, String arg1);

	@Message(id = 16057, value = "Can't enlist the resource because the transaction is marked for rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_elistwhenmarkedrollback();

	@Message(id = 16058, value = "Ending suspended RMs failed when rolling back the transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_endsuspendfailed1();

	@Message(id = 16059, value = "Ending suspended RMs failed when rolling back the transaction, but transaction rolled back.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_endsuspendfailed2();

	@Message(id = 16060, value = "{0} - caught: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_enlisterror(String arg0, String arg1);

	@Message(id = 16061, value = "{0} - XAResource.start returned: {1} for {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_enliststarterror(String arg0, String arg1, String arg2);

	@Message(id = 16062, value = "illegal resource state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_illresstate();

	@Message(id = 16063, value = "The transaction is not active!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_inactive();

	@Message(id = 16064, value = "The transaction is in an invalid state!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_invalidstate();

	@Message(id = 16065, value = "Caught unexpected exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_isnewrm(String arg0);

	@Message(id = 16066, value = "Failed to create instance of TransactionImporter", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_jca_SubordinationManager_importerfailure();

	@Message(id = 16067, value = "Failed to create instance of XATerminator", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_jca_SubordinationManager_terminatorfailure();

	@Message(id = 16068, value = "Work already active!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_jca_busy();

	@Message(id = 16069, value = "failed to load Last Resource Optimisation Interface", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_lastResourceOptimisationInterface();

	@Message(id = 16070, value = "{0} - could not mark {0} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_markrollback(String arg0, String arg1);

	@Message(id = 16071, value = "{0} caught XAException: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_newtmerror(String arg0, String arg1);

	@Message(id = 16072, value = "No such transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_nosuchtx();

	@Message(id = 16073, value = "Current transaction is not an AtomicAction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_notatomicaction();

	@Message(id = 16074, value = "no transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_notx();

	@Message(id = 16075, value = "null synchronization parameter!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_nullparam();

	@Message(id = 16076, value = "Resource paramater is null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_nullres();

	@Message(id = 16077, value = "Could not mark transaction as rollback only.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_rbofail();

	@Message(id = 16078, value = "resource already suspended.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_ressuspended();

	@Message(id = 16079, value = "Transaction rollback status is:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_rollbackstatus();

	@Message(id = 16080, value = "Not allowed to terminate subordinate transaction directly.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_subordinate_invalidstate();

	@Message(id = 16081, value = "The transaction implementation threw a RollbackException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_syncrollbackexception();

	@Message(id = 16082, value = "Synchronizations are not allowed! Transaction status is", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_syncsnotallowed();

	@Message(id = 16083, value = "Can't register synchronization because the transaction is in aborted state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_syncwhenaborted();

	@Message(id = 16084, value = "The transaction implementation threw a SystemException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_systemexception();

	@Message(id = 16085, value = "Caught the following error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_threadexception(String arg0);

	@Message(id = 16086, value = "{0} setTransactionTimeout on XAResource threw: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_timeouterror(String arg0, String arg1);

	@Message(id = 16087, value = "{0} - unknown resource", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_unknownresource(String arg0);

	@Message(id = 16088, value = "Could not call end on a suspended resource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_xaenderror();

	@Message(id = 16089, value = "{0} - caught: {1} for {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_transaction_arjunacore_xastart(String arg0, String arg1, String arg2);

	@Message(id = 16090, value = "Failed to create instance of TransactionManager", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_TransactionManager_generalfailure();

	@Message(id = 16091, value = "Failed to lookup transaction manager in JNDI context", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_TransactionManager_jndifailure();

	@Message(id = 16092, value = "Failed to create instance of UserTransaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_UserTransaction_generalfailure();

	@Message(id = 16093, value = "Failed to lookup user transaction in JNDI context", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_UserTransaction_jndifailure();

	@Message(id = 16094, value = "Failed to bind the JTA implementations with the appropriate JNDI contexts: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_JNDIManager_jndibindfailure(String arg0);

	@Message(id = 16095, value = "Failed to bind the JTA transaction manager implementation with the approprite JNDI context: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_JNDIManager_tmjndibindfailure(String arg0);

	@Message(id = 16096, value = "Unable to instantiate TransactionSynchronizationRegistry implementation class!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_JNDIManager_tsr1();

	@Message(id = 16097, value = "Failed to bind the JTA user transaction implementation with the appropriate JNDI context: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_JNDIManager_utjndibindfailure(String arg0);

	@Message(id = 16098, value = "Null exception!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_nullexception();

	@Message(id = 16099, value = "Unknown error code:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_unknownerrorcode();

	@Message(id = 16100, value = "Xid unset", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_xa_xidunset();

	@Message(id = 16101, value = "Could not pack XidImple.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_xid_packerror();

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
     */
}
