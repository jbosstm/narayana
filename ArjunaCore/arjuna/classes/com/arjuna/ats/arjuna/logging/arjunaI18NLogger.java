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
package com.arjuna.ats.arjuna.logging;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the arjuna module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface arjunaI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

	@Message(id = 12001, value = "ActivationRecord::set_value() called illegally", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_ActivationRecord_1();

	@Message(id = 12002, value = "Invocation of ActivationRecord::restore_state for {0} inappropriate - ignored for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_ActivationRecord_2(String arg0, Uid arg1);

	@Message(id = 12003, value = "Attempted abort operation on deleted object id {0} of type {1} ignored", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_CadaverRecord_1(Uid arg0, String arg1);

//	@Message(id = 12004, value = "DisposeRecord::save_state - type of store is unknown", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_DisposeRecord_1();

	@Message(id = 12005, value = "DisposeRecord::save_state - failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_DisposeRecord_2();

	@Message(id = 12006, value = "DisposeRecord::save_state - no object store defined.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_DisposeRecord_3();

//	@Message(id = 12007, value = "DisposeRecord::restore_state - invalid store type {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_DisposeRecord_4(String arg0);

	@Message(id = 12008, value = "DisposeRecord::topLevelCommit - exception while deleting state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_DisposeRecord_5(@Cause() Throwable arg0);

	@Message(id = 12009, value = "PersistenceRecord::restore_state: Failed to unpack object store type", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_10();

//	@Message(id = 12010, value = "PersistenceRecord::save_state - type of store is unknown", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_PersistenceRecord_11();

	@Message(id = 12011, value = "PersistenceRecord::save_state - packing top level state failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_14();

	@Message(id = 12012, value = "PersistenceRecord::save_state - failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_15();

	@Message(id = 12013, value = "PersistenceRecord::save_state - no object store defined for object", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_16();

//	@Message(id = 12014, value = "PersistenceRecord::topLevelAbort() - Expecting state but found none!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_PersistenceRecord_18();

	@Message(id = 12015, value = "PersistenceRecord::topLevelAbort() - Could not remove state from object store!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_19();

	@Message(id = 12016, value = "PersistenceRecord::topLevelCommit - commit_state call failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_2(Uid arg0);

	@Message(id = 12017, value = "PersistenceRecord::topLevelAbort() - Received ObjectStoreException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_20(@Cause() Throwable arg0);

	@Message(id = 12018, value = "PersistenceRecord.topLevelPrepare - write_uncommitted error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_21(@Cause() Throwable arg0);

	@Message(id = 12019, value = "PersistenceRecord::topLevelCommit - no state to commit!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_3();

	@Message(id = 12020, value = "PersistenceRecord::topLevelCommit - caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_4(@Cause() Throwable arg0);

	@Message(id = 12021, value = "PersistenceRecord::topLevelCommit - no object store specified!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_5();

	@Message(id = 12022, value = "PersistenceRecord::topLevelCommit - commit_state error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_6();

	@Message(id = 12023, value = "PersistenceRecord deactivate error, object probably already deactivated!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_7();

	@Message(id = 12024, value = "PersistenceRecord.topLevelPrepare - setup error!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_PersistenceRecord_8();

	@Message(id = 12025, value = "RecoveryRecord::setValue not given OutputObjectState.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_RecoveryRecord_1();

	@Message(id = 12026, value = "RecoveryRecord::nestedAbort - restore_state on object failed!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_RecoveryRecord_2();

	@Message(id = 12027, value = "LockManager::terminate() should be invoked in every destructor", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_1();

	@Message(id = 12028, value = "StateManager::modified() invocation on an object whose state has not been restored - activating object", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_10();

	@Message(id = 12029, value = "Delete called on object with uid {0} and type {1} within atomic action.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_11(Uid arg0, String arg1);

	@Message(id = 12030, value = "StateManager.cleanup - could not save_state from terminate!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_12();

	@Message(id = 12031, value = "Attempt to use volatile store.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_13();

	@Message(id = 12032, value = "Volatile store not implemented!", format = MESSAGE_FORMAT)
	public String get_StateManager_14();

	@Message(id = 12033, value = "Invalid object state.", format = MESSAGE_FORMAT)
	public String get_StateManager_15();

//	@Message(id = 12034, value = "Invalid object store type:", format = MESSAGE_FORMAT)
//	public String get_StateManager_16();

	@Message(id = 12035, value = "Activate of object with id = {0} and type {1} unexpectedly failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_2(Uid arg0, String arg1);

	@Message(id = 12036, value = "StateManager::deactivate - object store error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_3(@Cause() Throwable arg0);

	@Message(id = 12037, value = "StateManager::deactivate - save_state error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_4();

	@Message(id = 12038, value = "StateManager.destroy - failed to add abstract record to transaction {0}; check transaction status.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_6(Uid arg0);

	@Message(id = 12039, value = "StateManager.destroy - caught object store exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_7(@Cause() Throwable arg0);

	@Message(id = 12040, value = "StateManager.destroy - called on non-persistent or new object!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_8();

	@Message(id = 12041, value = "StateManager.restore_state - could not find StateManager state in object state!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_StateManager_9();

//	@Message(id = 12042, value = "Mutex being destroyed with waiters.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_common_Mutex_1();

	@Message(id = 12043, value = "Mutex.unlock - called by non-owning thread!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_Mutex_2();

	@Message(id = 12044, value = "cannot get local host.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_Uid_1();

//	@Message(id = 12045, value = "Cannot unpack into nullUid!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_common_Uid_10();

	@Message(id = 12046, value = "Uid.Uid recreate constructor could not recreate Uid!", format = MESSAGE_FORMAT)
	public String get_common_Uid_11();

	@Message(id = 12047, value = "Uid.Uid string constructor could not create nullUid", format = MESSAGE_FORMAT)
	public String get_common_Uid_2();

	@Message(id = 12048, value = "Uid general parsing error: {0}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
	public void warn_common_Uid_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 12049, value = "Uid.Uid string constructor could not create nullUid for incorrect string: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_common_Uid_4(String arg0);

	@Message(id = 12050, value = "Uid.Uid string constructor incorrect: {0}", format = MESSAGE_FORMAT)
	public String get_common_Uid_5(String arg0);

	@Message(id = 12051, value = "Uid.generateHash called for invalid Uid. Will ignore.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_Uid_6();

//	@Message(id = 12052, value = "nullUid error for", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_common_Uid_7();

//	@Message(id = 12053, value = "Invalid string:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_common_Uid_8();

//	@Message(id = 12054, value = "Invalid Uid object.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_common_Uid_9();

	@Message(id = 12055, value = "Exception thrown creating Uid from bytes!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_Uid_bytes(@Cause() Throwable arg0);

	@Message(id = 12056, value = "Exception thrown getting bytes!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_Uid_getbytes(@Cause() Throwable arg0);

	@Message(id = 12057, value = "Uid.Uid string constructor {0} caught other throwable", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_Uid_npe(String arg0, @Cause() Throwable arg1);

	@Message(id = 12058, value = "AbstractRecord.create {0} failed to find record.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_AbstractRecord_npe(String arg0);

	@Message(id = 12059, value = "Memory exhausted.", format = MESSAGE_FORMAT)
	public String get_coordinator_ActionHierarchy_1();

	@Message(id = 12060, value = "Action nesting error - deletion of action id {0} invoked while child actions active", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_1(Uid arg0);

	@Message(id = 12061, value = "Aborting child {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_2(Uid arg0);

	@Message(id = 12062, value = "BasicAction.restore_state - could not recover {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_21(String arg0);

	@Message(id = 12063, value = "BasicAction.restore_state - error unpacking action status.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_24();

//	@Message(id = 12064, value = "BasicAction.destroy called on {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_coordinator_BasicAction_28(String arg0);

	@Message(id = 12065, value = "BasicAction.Begin of action {0} ignored - incorrect invocation sequence {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_29(Uid arg0, String arg1);

	@Message(id = 12066, value = "Destructor of still running action id {0} invoked - Aborting", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_3(Uid arg0);

	@Message(id = 12067, value = "BasicAction.Begin of action {0} ignored - no parent and set as nested action!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_30(Uid arg0);

	@Message(id = 12068, value = "BasicAction.Begin of action {0} ignored - parent action {1} is not running: {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_31(Uid arg0, Uid arg1, String arg2);

//	@Message(id = 12069, value = "The Arjuna licence does not permit any further transactions to be committed!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_coordinator_BasicAction_32();

	@Message(id = 12070, value = "End called on non-running atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_33(Uid arg0);

	@Message(id = 12071, value = "End called on already committed atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_34(Uid arg0);

	@Message(id = 12072, value = "End called illegally on atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_35(Uid arg0);

	@Message(id = 12073, value = "BasicAction.End() - prepare phase of action-id {0} failed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_36(Uid arg0);

	@Message(id = 12074, value = "Received heuristic: {0} .", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_37(String arg0);

	@Message(id = 12075, value = "Action Aborting", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_38();

	@Message(id = 12076, value = "Abort called on non-running atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_39(Uid arg0);

	@Message(id = 12077, value = "Abort called on already aborted atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_40(Uid arg0);

	@Message(id = 12078, value = "Abort called illegaly on atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_41(Uid arg0);

	@Message(id = 12079, value = "BasicAction {0} - non-empty ( {1} ) pendingList {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_42(Uid arg0, String arg1, String arg2);

	@Message(id = 12080, value = "Transaction {0} marked as rollback only. Will abort.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_43(Uid arg0);

	@Message(id = 12081, value = "Cannot force parent to rollback - no handle!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_44();

	@Message(id = 12082, value = "BasicAction::prepare - creating intentions list failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_45(Uid arg0);

	@Message(id = 12083, value = "BasicAction::prepare - intentions list write failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_46(Uid arg0);

	@Message(id = 12084, value = "One-phase commit of action {0} received heuristic decision: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_47(Uid arg0, String arg1);

	@Message(id = 12085, value = "BasicAction.onePhaseCommit failed - no object store for atomic action state!", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_coordinator_BasicAction_48();

	@Message(id = 12086, value = "Prepare phase of nested action {0} received inconsistent outcomes.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_49(Uid arg0);

	@Message(id = 12087, value = "Activate of atomic action with id {0} and type {1} unexpectedly failed, could not load state.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_5(Uid arg0, String arg1);

	@Message(id = 12088, value = "Prepare phase of action {0} received heuristic decision: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_50(Uid arg0, String arg1);

	@Message(id = 12089, value = "Top-level abort of action {0} received heuristic decision: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_52(Uid arg0, String arg1);

	@Message(id = 12090, value = "Nested abort of action {0} received heuristic decision: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_53(Uid arg0, String arg1);

	@Message(id = 12091, value = "Top-level abort of action {0} received {1} from {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_54(Uid arg0, String arg1, String arg2);

	@Message(id = 12092, value = "Nested abort of action {0} received {1} from {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_55(Uid arg0, String arg1, String arg2);

	@Message(id = 12093, value = "BasicAction.checkIsCurrent {0} - terminating non-current transaction: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_56(Uid arg0, Uid arg1);

	@Message(id = 12094, value = "Commit of action id {0} invoked while multiple threads active within it.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_57(Uid arg0);

	@Message(id = 12095, value = "Abort of action id {0} invoked while multiple threads active within it.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_58(Uid arg0);

	@Message(id = 12096, value = "Commit of action id {0} invoked while child actions active", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_59(Uid arg0);

	@Message(id = 12097, value = "Deactivate of atomic action with id {0} and type {1} unexpectedly failed, could not save state.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_5a(Uid arg0, String arg1);

	@Message(id = 12098, value = "Abort of action id {0} invoked while child actions active", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_60(Uid arg0);

	@Message(id = 12099, value = "Aborting child: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_61(Uid arg0);

	@Message(id = 12100, value = "Now aborting self: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_62(String arg0);

	@Message(id = 12101, value = "BasicAction.updateState - Could not create ObjectState for failedList", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_64();

	@Message(id = 12102, value = "BasicAction.End - Could not write failed list", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_65();

	@Message(id = 12103, value = "(Internal) BasicAction.merge - record rejected", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_68();

	@Message(id = 12104, value = "No object store for:", format = MESSAGE_FORMAT)
	public String get_coordinator_BasicAction_69();

	@Message(id = 12105, value = "Could not remove intentions list:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_BasicAction_70(@Cause() Throwable arg0);

//	@Message(id = 12106, value = "Deactivation of atomic action with id {0} and type {1} unexpectedly failed", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_coordinator_BasicAction_71(String arg0, String arg1);

	@Message(id = 12107, value = "CheckedAction::check - atomic action {0} commiting with {1} threads active!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_CheckedAction_1(Uid arg0, String arg1);

	@Message(id = 12108, value = "CheckedAction::check - atomic action {0} aborting with {1} threads active!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_CheckedAction_2(Uid arg0, String arg1);

	@Message(id = 12109, value = "TransactionReaper - attempting to insert an element that is already present.", format = MESSAGE_FORMAT)
	public String get_coordinator_TransactionReaper_1();

	@Message(id = 12110, value = "TransactionReaper::check successfuly marked TX {0} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_10(Uid arg0);

	@Message(id = 12111, value = "TransactionReaper::check failed to mark TX {0}  as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_11(Uid arg0);

	@Message(id = 12112, value = "TransactionReaper::check exception while marking TX {0} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_12(Uid arg0, @Cause() Throwable arg1);

	@Message(id = 12113, value = "TransactionReaper::doCancellations worker {0} missed interrupt when cancelling TX {1} -- exiting as zombie (zombie count decremented to {2})", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_13(String arg0, Uid arg1, String arg2);

	@Message(id = 12114, value = "TransactionReaper::doCancellations worker {0} successfuly marked TX {1} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_14(String arg0, Uid arg1);

	@Message(id = 12115, value = "TransactionReaper::doCancellations worker {0} failed to mark TX {1}  as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_15(String arg0, Uid arg1);

	@Message(id = 12116, value = "TransactionReaper::doCancellations worker {0} exception while marking TX {1} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_16(String arg0, Uid arg1, @Cause() Throwable arg2);

	@Message(id = 12117, value = "TransactionReaper::check timeout for TX {0} in state  {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_18(Uid arg0, String arg1);

	@Message(id = 12118, value = "TransactionReaper NORMAL mode is deprecated. Update config to use PERIODIC for equivalent behaviour.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_19();

	@Message(id = 12119, value = "TransactionReaper::check worker zombie count {0} exceeds specified limit", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_coordinator_TransactionReaper_5(String arg0);

	@Message(id = 12120, value = "TransactionReaper::check worker {0} not responding to interrupt when cancelling TX {1} -- worker marked as zombie and TX scheduled for mark-as-rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_6(String arg0, Uid arg1);

	@Message(id = 12121, value = "TransactionReaper::doCancellations worker {0} successfully canceled TX {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_7(String arg0, Uid arg1);

	@Message(id = 12122, value = "TransactionReaper::doCancellations worker {0} failed to cancel TX {1} -- rescheduling for mark-as-rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_8(String arg0, Uid arg1);

	@Message(id = 12123, value = "TransactionReaper::doCancellations worker {0} exception during cancel of TX {1} -- rescheduling for mark-as-rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TransactionReaper_9(String arg0, Uid arg1, @Cause() Throwable arg2);

	@Message(id = 12124, value = "TwoPhaseCoordinator.beforeCompletion - attempted rollback_only failed!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TwoPhaseCoordinator_1();

	@Message(id = 12125, value = "TwoPhaseCoordinator.beforeCompletion - failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TwoPhaseCoordinator_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 12126, value = "TwoPhaseCoordinator.beforeCompletion TwoPhaseCoordinator.afterCompletion called on still running transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TwoPhaseCoordinator_3();

	@Message(id = 12127, value = "TwoPhaseCoordinator.afterCompletion - returned failure for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TwoPhaseCoordinator_4(String arg0);

	@Message(id = 12128, value = "TwoPhaseCoordinator.afterCompletion - failed for {0} with exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TwoPhaseCoordinator_4a(String arg0, @Cause() Throwable arg1);

	@Message(id = 12129, value = "TwoPhaseCoordinator.afterCompletion - failed for {0} with error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TwoPhaseCoordinator_4b(String arg0, @Cause() Throwable arg1);

	@Message(id = 12130, value = "Name of XA node not defined. Using {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TxControl_1(String arg0);

	@Message(id = 12131, value = "Supplied name of node is too long. Using {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TxControl_2(String arg0);

	@Message(id = 12132, value = "Supplied name of node contains reserved character '-'. Using {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_TxControl_3(String arg0);

//	@Message(id = 12133, value = "Cannot continue due to CheckedActionFactory resolution problem with", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_coordinator_cafactoryerror();

//	@Message(id = 12134, value = "Failed to resolve CheckedActionFactory class {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_coordinator_checkedactionfactory(String arg0);

	@Message(id = 12135, value = "Could not create Store type:", format = MESSAGE_FORMAT)
    public String get_StoreManager_invalidtype();

	@Message(id = 12136, value = "Could not recreate abstract record {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_norecordfound(String arg0);

	@Message(id = 12137, value = "Cannot begin new transaction as TM is disabled. Marking as rollback-only.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_notrunning();

	@Message(id = 12138, value = "Node name cannot exceed 64 bytes!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_coordinator_toolong();

	@Message(id = 12139, value = "You have chosen to disable the Multiple Last Resources warning. You will see it only once.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_lastResource_disableWarning();

	@Message(id = 12140, value = "Adding multiple last resources is disallowed. Current resource is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_lastResource_disallow(String arg0);

	@Message(id = 12141, value = "Multiple last resources have been added to the current transaction. This is transactionally unsafe and should not be relied upon. Current resource is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_lastResource_multipleWarning(String arg0);

	@Message(id = 12142, value = "You have chosen to enable multiple last resources in the transaction manager. This is transactionally unsafe and should not be relied upon.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_lastResource_startupWarning();

	@Message(id = 12143, value = "unknown store: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ObjectStoreType_1(String arg0);

	@Message(id = 12144, value = "unknown store:", format = MESSAGE_FORMAT)
	public String get_objectstore_ObjectStoreType_2();

//	@Message(id = 12145, value = "No implementation!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_ObjectStore_1();

	@Message(id = 12146, value = "ActionStatusService: searching for uid: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_ActionStatusService_5(Uid arg0, @Cause() Throwable arg1);

	@Message(id = 12147, value = "transactionType: {0} uid: {1}   Status is {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_ActionStatusService_1(String arg0, String arg1, String arg2);

	@Message(id = 12148, value = "Other Exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_ActionStatusService_2(@Cause() Throwable arg0);

	@Message(id = 12149, value = "Exception retrieving action status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_ActionStatusService_3(@Cause() Throwable arg0);

	@Message(id = 12150, value = "matching Uid {0} found", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_ActionStatusService_4(Uid arg0);

	@Message(id = 12151, value = "Exception when accessing transaction store", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_ActionStatusService_6(@Cause() Throwable arg0);

	@Message(id = 12152, value = "Connection Lost to Recovery Manager", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_ActionStatusService_7();

	@Message(id = 12153, value = "RecoverAtomicAction.replayPhase2: Unexpected status: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_RecoverAtomicAction_2(String arg0);

	@Message(id = 12154, value = "RecoverAtomicAction: transaction {0} not activated, unable to replay phase 2 commit. Check state has not already been completed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_RecoverAtomicAction_4(Uid arg0);

	@Message(id = 12155, value = "RecoverAtomicAction - tried to move failed activation log {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_RecoverAtomicAction_5(Uid arg0);

//	@Message(id = 12156, value = "Invalid recovery manager port specified {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryManager_1(String arg0);

//	@Message(id = 12157, value = "Invalid recovery manager host specified {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryManager_2(String arg0);

//	@Message(id = 12158, value = "Recovery manager bound to {0}:{1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryManager_3(String arg0, String arg1);

	@Message(id = 12159, value = "Connected to recovery manager on {0}:{1}", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
	public void info_recovery_RecoveryManager_4(String arg0, String arg1);
//
//	@Message(id = 12160, value = "Invalid recovery manager port specified", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryManager_5();

	@Message(id = 12161, value = "Exception when accessing data store", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusConnectionManager_1(@Cause() Throwable arg0);

	@Message(id = 12162, value = "Object store exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusConnectionManager_2(@Cause() Throwable arg0);

	@Message(id = 12163, value = "Starting service {0} on port {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_TransactionStatusManager_1(String arg0, String arg1);

//	@Message(id = 12164, value = "Unknown host {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_TransactionStatusManager_10(String arg0);

//	@Message(id = 12165, value = "Invalid port specified", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_TransactionStatusManager_11();

//	@Message(id = 12166, value = "Unknown host specified", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_TransactionStatusManager_12();

	@Message(id = 12167, value = "Invalid host or port", format = MESSAGE_FORMAT)
	public String get_recovery_TransactionStatusManager_13();

	@Message(id = 12168, value = "Failed to create server socket on address {0} and port: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusManager_14(String arg0, String arg1);

	@Message(id = 12169, value = "Listener failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusManager_2();

	@Message(id = 12170, value = "TransactionStatusManager started on port {0} and host {1} with service {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_TransactionStatusManager_3(String arg0, String arg1, String arg2);

	@Message(id = 12171, value = "Failed to setup class for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusManager_4(String arg0);

//	@Message(id = 12172, value = "Failed to instantiate service class: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_TransactionStatusManager_5(String arg0);

//	@Message(id = 12173, value = "Illegal access to service class: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_TransactionStatusManager_6(String arg0);

//	@Message(id = 12174, value = "Failed to create server socket on port: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_TransactionStatusManager_7(String arg0);

//	@Message(id = 12175, value = "Invalid port specified {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_TransactionStatusManager_8(String arg0);

	@Message(id = 12176, value = "Could not get unique port.", format = MESSAGE_FORMAT)
	public String get_recovery_TransactionStatusManager_9();

	@Message(id = 12177, value = "com.arjuna.ats.arjuna.state.InputBuffer_1 - Invalid input buffer: byte.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_1();

	@Message(id = 12178, value = "com.arjuna.ats.arjuna.state.InputBuffer_10 - Invalid input buffer: string.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_10();

	@Message(id = 12179, value = "com.arjuna.ats.arjuna.state.InputBuffer_11 - Invalid from buffer", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_11();

	@Message(id = 12180, value = "com.arjuna.ats.arjuna.state.InputBuffer_2 - Invalid input buffer: bytes.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_2();

	@Message(id = 12181, value = "com.arjuna.ats.arjuna.state.InputBuffer_3 - Invalid input buffer: boolean.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_3();

	@Message(id = 12182, value = "com.arjuna.ats.arjuna.state.InputBuffer_4 - Invalid input buffer: char.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_4();

	@Message(id = 12183, value = "com.arjuna.ats.arjuna.state.InputBuffer_5 - Invalid input buffer: short.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_5();

	@Message(id = 12184, value = "com.arjuna.ats.arjuna.state.InputBuffer_6 - Invalid input buffer: int.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_6();

	@Message(id = 12185, value = "com.arjuna.ats.arjuna.state.InputBuffer_7 - Invalid input buffer: long.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_7();

	@Message(id = 12186, value = "com.arjuna.ats.arjuna.state.InputBuffer_8 - Invalid input buffer: float.", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_8();

	@Message(id = 12187, value = "com.arjuna.ats.arjuna.state.InputBuffer_9 - Invalid input buffer: double", format = MESSAGE_FORMAT)
	public String get_state_InputBuffer_9();

	@Message(id = 12188, value = "com.arjuna.ats.arjuna.state.OutputBuffer_1 - Invalid input buffer: byte.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_1();

	@Message(id = 12189, value = "com.arjuna.ats.arjuna.state.OutputBuffer_10 - Invalid input buffer: string.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_10();

	@Message(id = 12190, value = "com.arjuna.ats.arjuna.state.OutputBuffer_11 - Invalid from buffer", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_11();

	@Message(id = 12191, value = "com.arjuna.ats.arjuna.state.OutputBuffer_2 - Invalid input buffer: bytes.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_2();

	@Message(id = 12192, value = "com.arjuna.ats.arjuna.state.OutputBuffer_3 - Invalid input buffer: boolean.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_3();

	@Message(id = 12193, value = "com.arjuna.ats.arjuna.state.OutputBuffer_4 - Invalid input buffer: char.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_4();

	@Message(id = 12194, value = "com.arjuna.ats.arjuna.state.OutputBuffer_5 - Invalid input buffer: short.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_5();

	@Message(id = 12195, value = "com.arjuna.ats.arjuna.state.OutputBuffer_6 - Invalid input buffer: int.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_6();

	@Message(id = 12196, value = "com.arjuna.ats.arjuna.state.OutputBuffer_7 - Invalid input buffer: long.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_7();

	@Message(id = 12197, value = "com.arjuna.ats.arjuna.state.OutputBuffer_8 - Invalid input buffer: float.", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_8();

	@Message(id = 12198, value = "com.arjuna.ats.arjuna.state.OutputBuffer_9 - Invalid input buffer: double", format = MESSAGE_FORMAT)
	public String get_state_OutputBuffer_9();

//	@Message(id = 12199, value = "remove committed failed.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_tools_osb_mbean_m_1();

//	@Message(id = 12200, value = "remove ok.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_tools_osb_mbean_m_2();

//	@Message(id = 12201, value = "remove committed exception: {0}.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_tools_osb_mbean_m_3(String arg0);

	@Message(id = 12202, value = "registering bean {0}.", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_tools_osb_util_JMXServer_m_1(String arg0);

	@Message(id = 12203, value = "Instance already exists: {0}.", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_tools_osb_util_JMXServer_m_2(String arg0);

	@Message(id = 12204, value = "Error registering {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_tools_osb_util_JMXServer_m_3(String arg0, @Cause() Throwable arg1);

//	@Message(id = 12205, value = "Try to unregister mbean with invalid name {0}.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_tools_osb_util_JMXServer_m_4(String arg0);

	@Message(id = 12206, value = "Unable to unregister bean {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_tools_osb_util_JMXServer_m_5(String arg0, @Cause() Throwable arg1);

	@Message(id = 12207, value = "Unable to unregister bean {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_tools_osb_util_JMXServer_m_6(String arg0, @Cause() Throwable arg1);

	@Message(id = 12208, value = "An error occurred while creating file {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_utils_FileLock_4(String arg0);

	@Message(id = 12209, value = "Utility.getDefaultProcess failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_utils_Utility_1();

	@Message(id = 12210, value = "Unable to use InetAddress.getLocalHost() to resolve address.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_utils_Utility_2();

	@Message(id = 12211, value = "Attempt to suspend a non-AtomicAction transaction. Type is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_ats_atomicaction_1(String arg0);

	@Message(id = 12212, value = "StateManagerFriend.forgetAction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_abstractrecords_smf1(@Cause() Throwable arg0);

	@Message(id = 12213, value = "StateManagerFriend.destroyed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_abstractrecords_smf2(@Cause() Throwable arg0);

	@Message(id = 12214, value = "StateManagerFriend.rememberAction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_abstractrecords_smf3(@Cause() Throwable arg0);

	@Message(id = 12215, value = "className is null", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_ClassloadingUtility_1();

	@Message(id = 12216, value = "attempt to load {0} threw ClassNotFound. Wrong classloader?", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_ClassloadingUtility_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 12217, value = "class {0} does not implement {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_ClassloadingUtility_3(String arg0, String arg1, @Cause() Throwable arg2);

	@Message(id = 12218, value = "can't create new instance of {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_ClassloadingUtility_4(String arg0, @Cause() Throwable arg1);

	@Message(id = 12219, value = "can't access {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_ClassloadingUtility_5(String arg0, @Cause() Throwable arg1);

	@Message(id = 12220, value = "can't initialize from string {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_common_ClassloadingUtility_6(String arg0, @Cause() Throwable arg1);

	@Message(id = 12221, value = "Commit state failed for {0} and {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_CacheStore_1(Uid arg0, String arg1);

	@Message(id = 12222, value = "Remove state failed for {0} and {1} and {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_CacheStore_2(Uid arg0, String arg1, String arg2);

	@Message(id = 12223, value = "Write state failed for {0} and {1} and {2} and {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_CacheStore_3(Uid arg0, String arg1, String arg2, String arg3);

	@Message(id = 12224, value = "Unknown work type {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_CacheStore_4(String arg0);

	@Message(id = 12225, value = "FileSystemStore::setupStore - cannot access root of object store: {0}", format = MESSAGE_FORMAT)
	public String get_objectstore_FileSystemStore_1(String arg0);

	@Message(id = 12226, value = "FileSystemStore.removeFromCache - no entry for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_FileSystemStore_2(String arg0);

	@Message(id = 12227, value = "FileSystemStore.renameFromTo - from {0} not present. Possibly renamed by crash recovery.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_FileSystemStore_20(String arg0);

	@Message(id = 12228, value = "FileSystemStore::allObjUids - could not pack Uid.", format = MESSAGE_FORMAT)
	public String get_objectstore_FileSystemStore_2a();

	@Message(id = 12229, value = "FileSystemStore::allObjUids - could not pack end of list Uid.", format = MESSAGE_FORMAT)
	public String get_objectstore_FileSystemStore_3();

	@Message(id = 12230, value = "FileSytemStore::allTypes - could not pack entry string.", format = MESSAGE_FORMAT)
	public String get_objectstore_FileSystemStore_4();

	@Message(id = 12231, value = "FileSystemStore::allTypes - could not pack end of list string.", format = MESSAGE_FORMAT)
	public String get_objectstore_FileSystemStore_5();

	@Message(id = 12232, value = "FileSystemStore::setupStore - error from unpack object store.", format = MESSAGE_FORMAT)
	public String get_objectstore_FileSystemStore_6();

	@Message(id = 12233, value = "FileSystemStore::allTypes - could not pack entry string.", format = MESSAGE_FORMAT)
	public String get_objectstore_FileSystemStore_7();

	@Message(id = 12234, value = "FileSystemStore::createHierarchy - null directory name.", format = MESSAGE_FORMAT)
	public String get_objectstore_FileSystemStore_8();

//	@Message(id = 12235, value = "HashedStore.create caught: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_HashedStore_1(String arg0);

	@Message(id = 12236, value = "invalid number of hash directories: {0}. Will use default.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_HashedStore_2(String arg0);

	@Message(id = 12237, value = "HashedStore.allObjUids - could not pack Uid.", format = MESSAGE_FORMAT)
	public String get_objectstore_HashedStore_5();

	@Message(id = 12238, value = "HashedStore.allObjUids - could not pack end of list Uid.", format = MESSAGE_FORMAT)
	public String get_objectstore_HashedStore_6();

	@Message(id = 12239, value = "hide_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_1(@Cause() Throwable arg0);

	@Message(id = 12240, value = "remove_state - type() operation of object with uid {0} returns NULL", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_10(Uid arg0);

	@Message(id = 12241, value = "invalid initial pool size: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_11(String arg0);

	@Message(id = 12242, value = "invalid maximum pool size: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_12(String arg0);

	@Message(id = 12243, value = "initialise caught exceptionatorLoader_3", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_13(@Cause() Throwable arg0);

	@Message(id = 12244, value = "getState caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_14(@Cause() Throwable arg0);

	@Message(id = 12245, value = "removeFromCache - no entry for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_15(String arg0);

	@Message(id = 12246, value = "getPool caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_16(@Cause() Throwable arg0);

	@Message(id = 12247, value = "getPool - interrupted while waiting for a free connection", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_objectstore_JDBCImple_17();

	@Message(id = 12248, value = "freePool - freeing a connection which is already free!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_18();

	@Message(id = 12249, value = "reveal_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_2(@Cause() Throwable arg0);

	@Message(id = 12250, value = "currentState caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_3(@Cause() Throwable arg0);

	@Message(id = 12251, value = "allObjUids caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_4(@Cause() Throwable arg0);

	@Message(id = 12252, value = "allObjUids - pack of Uid failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_5(@Cause() Throwable arg0);

	@Message(id = 12253, value = "allTypes caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_6(@Cause() Throwable arg0);

	@Message(id = 12254, value = "allTypes - pack of Uid failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_7(@Cause() Throwable arg0);

	@Message(id = 12255, value = "remove_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_8(@Cause() Throwable arg0);

	@Message(id = 12256, value = "remove_state() attempted removal of {0} state for object with uid {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_9(String arg0, Uid arg1);

	@Message(id = 12257, value = "JDBCImple:read_state failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_readfailed();

	@Message(id = 12258, value = "JDBCImple:write_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCImple_writefailed(@Cause() Throwable arg0);

	@Message(id = 12259, value = "JDBCStore could not setup store < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_objectstore_JDBCStore_1(String arg0, String arg1);

	@Message(id = 12260, value = "Received exception for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_objectstore_JDBCStore_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 12261, value = "JDBCStore.setupStore failed to initialise!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_JDBCStore_3();

//	@Message(id = 12262, value = "JDBCStore invalid Object parameter: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_JDBCStore_4(String arg0);

	@Message(id = 12263, value = "No JDBCAccess implementation provided!", format = MESSAGE_FORMAT)
	public String get_objectstore_JDBCStore_5();

//	@Message(id = 12264, value = "ShadowingStore.commit_state - store invalid!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_ShadowingStore_1();

	@Message(id = 12265, value = "ShadowingStore::remove_state() - state {0} does not exist for type {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_10(Uid arg0, String arg1);

	@Message(id = 12266, value = "ShadowingStore::remove_state() - unlink failed on {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_11(String arg0);

	@Message(id = 12267, value = "ShadowingStore.remove_state() - fd error for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_12(Uid arg0);

//	@Message(id = 12268, value = "ShadowingStore::remove_state() attempted removal of", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_ShadowingStore_13();

	@Message(id = 12269, value = "UNKNOWN state for object with uid {0} , type {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_objectstore_ShadowingStore_14(Uid arg0, String arg1);

	@Message(id = 12270, value = "HIDDEN state for object with uid {0} , type {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_objectstore_ShadowingStore_15(Uid arg0, String arg1);

//	@Message(id = 12271, value = "state for object with uid {0} , type {1}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_ShadowingStore_16(String arg0, String arg1);

	@Message(id = 12272, value = "ShadowingStore.remove_state - type() operation of object with uid {0} returns NULL", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_17(Uid arg0);

	@Message(id = 12273, value = "ShadowingStore::write_state() - openAndLock failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_18(String arg0);

	@Message(id = 12274, value = "ShadowingStore::write_state - unlock or close of {0} failed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_19(String arg0);

	@Message(id = 12275, value = "ShadowStore::commit_state - failed to rename {0} to {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_2(String arg0, String arg1);

//	@Message(id = 12276, value = "ShadowingStore.renameFromTo - from {0} not present. Possibly renamed by crash recovery.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_ShadowingStore_20(String arg0);

//	@Message(id = 12277, value = "ShadowingStore.renameFromTo - failed to lock: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_ShadowingStore_21(String arg0);

	@Message(id = 12278, value = "ShadowStore::hide_state - failed to rename {0} to {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_3(String arg0, String arg1);

	@Message(id = 12279, value = "ShadowStore::reveal_state - failed to rename {0} to {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_4(String arg0, String arg1);

//	@Message(id = 12280, value = "ShadowingStore.create caught: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_ShadowingStore_5(String arg0);

//	@Message(id = 12281, value = "ShadowingStore.read_state - store invalid!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_objectstore_ShadowingStore_6();

	@Message(id = 12282, value = "ShadowingStore::read_state() failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_7();

	@Message(id = 12283, value = "ShadowingStore::read_state - unlock or close of {0} failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_8(String arg0);

	@Message(id = 12284, value = "ShadowingStore::remove_state() - access problems on {0} and {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_ShadowingStore_9(Uid arg0, String arg1);

	@Message(id = 12285, value = "oracle:read_state failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_jdbc_oracle_1();

	@Message(id = 12286, value = "oracle:write_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_objectstore_jdbc_oracle_2(@Cause() Throwable arg0);

	@Message(id = 12287, value = "No typename for object:", format = MESSAGE_FORMAT)
	public String get_objectstore_notypenameuid();

	@Message(id = 12288, value = "allTypes - could not pack end of list string.", format = MESSAGE_FORMAT)
	public String get_objectstore_packProblem();

	@Message(id = 12289, value = "RecoveryManagerStatusModule: Object store exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_AtomicActionRecoveryModule_1(@Cause() Throwable arg0);

	@Message(id = 12290, value = "failed to recover Transaction {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_AtomicActionRecoveryModule_2(Uid arg0, @Cause() Throwable arg1);

	@Message(id = 12291, value = "failed to access transaction store {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_AtomicActionRecoveryModule_3(Uid arg0, @Cause() Throwable arg1);

	@Message(id = 12292, value = "Connection - IOException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_Connection_1();

	@Message(id = 12293, value = "Setting timeout exception.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_Connection_2();

//	@Message(id = 12294, value = "Loading expiry scanner: could not find class {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredEntryMonitor_10(String arg0);

//	@Message(id = 12295, value = "{0} has inappropriate value ({1})", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredEntryMonitor_11(String arg0, String arg1);

	@Message(id = 12296, value = "ExpiredEntryMonitor running at {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_ExpiredEntryMonitor_12(String arg0);

	@Message(id = 12297, value = "ExpiredEntryMonitor - no scans on first iteration", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_ExpiredEntryMonitor_5();

//	@Message(id = 12298, value = "Attempt to load expiry scanner with null class name!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredEntryMonitor_7();

//	@Message(id = 12299, value = "Expiry scanner {0} does not conform to ExpiryScanner interface", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredEntryMonitor_9(String arg0);

//	@Message(id = 12300, value = "ExpiredTransactionScanner created, with expiry time of {0} seconds", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredTransactionScanner_1(String arg0);

	@Message(id = 12301, value = "ExpiredTransactionScanner - exception during attempted move {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_ExpiredTransactionScanner_2(Uid arg0, @Cause() Throwable arg1);

//	@Message(id = 12302, value = "ExpiredTransactionScanner - could not moved log {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredTransactionScanner_3(String arg0);

	@Message(id = 12303, value = "ExpiredTransactionScanner - log {0} is assumed complete and will be moved.", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_ExpiredTransactionScanner_4(Uid arg0);

	@Message(id = 12304, value = "Removing old transaction status manager item {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_ExpiredTransactionStatusManagerScanner_3(Uid arg0);

//	@Message(id = 12305, value = "{0}  has inappropriate value ({1})", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_ExpiredTransactionStatusManagerScanner_5(String arg0, String arg1);

//	@Message(id = 12306, value = "Attempt to load recovery module with null class name!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_1();

//	@Message(id = 12307, value = "Ignoring request to scan because RecoveryManager state is: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_10(String arg0);

//	@Message(id = 12308, value = "Invalid host specified {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_11(String arg0);

//	@Message(id = 12309, value = "Could not create recovery listener", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_12();

	@Message(id = 12310, value = "Recovery manager listening on endpoint {0}:{1}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_PeriodicRecovery_13(String arg0, String arg1);

//	@Message(id = 12311, value = "Recovery module {0} does not conform to RecoveryModule interface", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_2(String arg0);

//	@Message(id = 12312, value = "Loading recovery module", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_3(@Cause() Throwable arg0);

//	@Message(id = 12313, value = "Loading recovery module", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_4(@Cause() Throwable arg0);

//	@Message(id = 12314, value = "Loading recovery module: could not find class {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_5(String arg0);

//	@Message(id = 12315, value = "{0} has inappropriate value ( {1} )", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_6(String arg0, String arg1);

//	@Message(id = 12316, value = "{0} has inappropriate value ( {1} )", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_7(String arg0, String arg1);

//	@Message(id = 12317, value = "Invalid port specified {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_PeriodicRecovery_8(String arg0);

	@Message(id = 12318, value = "Could not create recovery listener", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_PeriodicRecovery_9(@Cause() Throwable arg0);

//	@Message(id = 12319, value = "Attempt to load recovery activator with null class name!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecActivatorLoader_1();

//	@Message(id = 12320, value = "Recovery module {0} does not conform to RecoveryActivator interface", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecActivatorLoader_2(String arg0);

//	@Message(id = 12321, value = "Loading recovery activator", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecActivatorLoader_3(@Cause() Throwable arg0);

//	@Message(id = 12322, value = "Loading recovery activator", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecActivatorLoader_4(@Cause() Throwable arg0);

//	@Message(id = 12323, value = "Loading recovery module: could not find class {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecActivatorLoader_5(String arg0);

	@Message(id = 12324, value = "Start RecoveryActivators", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_RecActivatorLoader_6();

//	@Message(id = 12325, value = "property io exception {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_recovery_RecoveryManagerImple_1(String arg0);

	@Message(id = 12326, value = "socket I/O exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_RecoveryManagerImple_2(@Cause() Throwable arg0);

	@Message(id = 12327, value = "TransactionStatusConnector.delete called erroneously", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusConnector_1();

	@Message(id = 12328, value = "Connection lost to TransactionStatusManagers' process", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusConnector_2();

	@Message(id = 12329, value = "Connection lost to TransactionStatusManagers' process", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusConnector_3();

	@Message(id = 12330, value = "TransactionStatusManager process for uid {0} is ALIVE. connected to host: {1}, port: {2} on socket: {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_TransactionStatusConnector_4(String arg0, String arg1, String arg2, String arg3);

	@Message(id = 12331, value = "TransactionStatusManager process for uid {0} is DEAD.", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_TransactionStatusConnector_5(String arg0);

	@Message(id = 12332, value = "Failed to establish connection to server", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_TransactionStatusConnector_6();

	@Message(id = 12333, value = "Problem with removing host/port item", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusManagerItem_1(@Cause() Throwable arg0);

	@Message(id = 12334, value = "Problem with storing host/port", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusManagerItem_2(@Cause() Throwable arg0);

	@Message(id = 12335, value = "Problem retrieving host/port", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusManagerItem_3(@Cause() Throwable arg0);

	@Message(id = 12336, value = "Failed to obtain host", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_TransactionStatusManagerItem_4(@Cause() Throwable arg0);

	@Message(id = 12337, value = "TransactionStatusManagerItem host: {0} port: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_TransactionStatusManagerItem_5(String arg0, String arg1);

	@Message(id = 12338, value = "Other Exception:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_WorkerService_1(@Cause() Throwable arg0);

	@Message(id = 12339, value = "IOException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_WorkerService_2();

	@Message(id = 12340, value = "RecoveryManager scan scheduled to begin.", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_WorkerService_3();

	@Message(id = 12341, value = "RecoveryManager scan completed.", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_WorkerService_4();

	@Message(id = 12342, value = "RecoveryManagerImple: cannot bind to socket on address {0} and port {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = FATAL)
	public void fatal_recovery_fail(String arg0, String arg1);

	@Message(id = 12343, value = "RecoveryManagerImple is ready. Socket listener is turned off.", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_localready();

	@Message(id = 12344, value = "RecoveryManagerImple is ready on port {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_recovery_socketready(String arg0);

	@Message(id = 12345, value = "Transaction {0} and {1} not activate.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_tools_log_eaa1(Uid arg0, String arg1);

	@Message(id = 12346, value = "Error - could not get resource to forget heuristic. Left on Heuristic List.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_tools_log_eaa2();

	@Message(id = 12347, value = "Could not get back a valid pid.", format = MESSAGE_FORMAT)
	public String get_utils_ExecProcessId_1();

	@Message(id = 12348, value = "Problem executing getpids utility:", format = MESSAGE_FORMAT)
	public String get_utils_ExecProcessId_2();

	@Message(id = 12349, value = "Problem executing command:", format = MESSAGE_FORMAT)
	public String get_utils_ExecProcessId_3();

	@Message(id = 12350, value = "Problem getting pid information from stream:", format = MESSAGE_FORMAT)
	public String get_utils_ExecProcessId_4();

	@Message(id = 12351, value = "Encountered a problem when closing the data stream", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_utils_ExecProcessId_5(@Cause() Throwable arg0);

	@Message(id = 12352, value = "FileProcessId.getpid - could not locate temporary directory.", format = MESSAGE_FORMAT)
	public String get_utils_FileProcessId_1();

	@Message(id = 12353, value = "FileProcessId.getpid could not create unique file.", format = MESSAGE_FORMAT)
	public String get_utils_FileProcessId_2();

	@Message(id = 12354, value = "Could not get back a valid pid.", format = MESSAGE_FORMAT)
	public String get_utils_MBeanProcessId_1();

	@Message(id = 12355, value = "getName returned unrecognized format:", format = MESSAGE_FORMAT)
	public String get_utils_MBeanProcessId_2();

	@Message(id = 12356, value = "Could not get back a valid pid.", format = MESSAGE_FORMAT)
	public String get_utils_ManualProcessId_1();

//	@Message(id = 12357, value = "No process identifier specified in configuration!", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_utils_ManualProcessId_2();

//	@Message(id = 12358, value = "Invalid process identifier specified:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_utils_ManualProcessId_3();

	@Message(id = 12359, value = "SocketProcessId.getpid could not get unique port.", format = MESSAGE_FORMAT)
	public String get_utils_SocketProcessId_2();

//    @Message(id = 12360, value = "Unable to instantiate ExpiryScanner", format = MESSAGE_FORMAT)
//    @LogMessage(level = WARN)
//    public void warn_recovery_ExpiredEntryMonitor_6(@Cause() Throwable arg0);

    @Message(id = 12361, value = "Error constructing mbean", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
    public void info_osb_MBeanCtorFail(@Cause() Throwable arg0);

    @Message(id = 12362, value = "Failed to create StateManagerWrapper", format = MESSAGE_FORMAT)
    @LogMessage(level = INFO)
    public void info_osb_StateManagerWrapperFail(@Cause() Throwable arg0);

    @Message(id = 12363, value = "Invalid rootName. Expected {0} but was {1}", format = MESSAGE_FORMAT)
    public String get_StoreManager_invalidroot(String arg0, String arg1);

    @Message(id = 12364, value = "RecoveryActivator init failed for {0}", format = MESSAGE_FORMAT)
	public String get_recovery_RecActivatorLoader_initfailed(String arg0);

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in numeric sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
          By convention methods with String return type have prefix get_,
            all others are log methods and have prefix <level>_
     */
}
