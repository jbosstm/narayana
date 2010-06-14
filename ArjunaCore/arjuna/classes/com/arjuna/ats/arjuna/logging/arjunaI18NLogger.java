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
	public void log_ActivationRecord_1();

	@Message(id = 12002, value = "Invocation of ActivationRecord::restore_state for {0} inappropriate - ignored for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_ActivationRecord_2(String arg0, String arg1);

	@Message(id = 12003, value = "Attempted abort operation on deleted object id {0} of type {1} ignored", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_CadaverRecord_1(String arg0, String arg1);

	@Message(id = 12004, value = "DisposeRecord::save_state - type of store is unknown", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_DisposeRecord_1();

	@Message(id = 12005, value = "DisposeRecord::save_state - failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_DisposeRecord_2();

	@Message(id = 12006, value = "DisposeRecord::save_state - no object store defined.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_DisposeRecord_3();

	@Message(id = 12007, value = "DisposeRecord::restore_state - invalid store type {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_DisposeRecord_4(String arg0);

	@Message(id = 12008, value = "DisposeRecord::topLevelCommit - exception while deleting state {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_DisposeRecord_5(String arg0);

	@Message(id = 12009, value = "PersistenceRecord::topLevelCommit() : About to commit state, uid = {0}, ObjType = {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_1(String arg0, String arg1);

	@Message(id = 12010, value = "PersistenceRecord::restore_state: Failed to unpack object store type", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_10();

	@Message(id = 12011, value = "PersistenceRecord::save_state - type of store is unknown", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_11();

	@Message(id = 12012, value = "PersistenceRecord::save_state: Packed object store type = {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_12(String arg0);

	@Message(id = 12013, value = "PersistenceRecord::save_state: Packed object store root", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_13();

	@Message(id = 12014, value = "PersistenceRecord::save_state - packing top level state failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_14();

	@Message(id = 12015, value = "PersistenceRecord::save_state - failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_15();

	@Message(id = 12016, value = "PersistenceRecord::save_state - no object store defined for object", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_16();

	@Message(id = 12017, value = "PersistenceRecord::PersistenceRecord() - crash recovery constructor", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_17();

	@Message(id = 12018, value = "PersistenceRecord::topLevelAbort() - Expecting state but found none!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_18();

	@Message(id = 12019, value = "PersistenceRecord::topLevelAbort() - Could not remove state from object store!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_19();

	@Message(id = 12020, value = "PersistenceRecord::topLevelCommit - commit_state call failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_2(String arg0);

	@Message(id = 12021, value = "PersistenceRecord::topLevelAbort() - Received ObjectStoreException {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_20(String arg0);

	@Message(id = 12022, value = "PersistenceRecord.topLevelPrepare - write_uncommitted error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_21();

	@Message(id = 12023, value = "PersistenceRecord::topLevelCommit - no state to commit!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_3();

	@Message(id = 12024, value = "PersistenceRecord::topLevelCommit - caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_4(String arg0);

	@Message(id = 12025, value = "PersistenceRecord::topLevelCommit - no object store specified!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_5();

	@Message(id = 12026, value = "PersistenceRecord::topLevelCommit - commit_state error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_6();

	@Message(id = 12027, value = "PersistenceRecord deactivate error, object probably already deactivated!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_7();

	@Message(id = 12028, value = "PersistenceRecord.topLevelPrepare - setup error!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_8();

	@Message(id = 12029, value = "PersistenceRecord::restore_state: Just unpacked object store type = {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_PersistenceRecord_9(String arg0);

	@Message(id = 12030, value = "RecoveryRecord::setValue not given OutputObjectState.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_RecoveryRecord_1();

	@Message(id = 12031, value = "RecoveryRecord::nestedAbort - restore_state on object failed!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_RecoveryRecord_2();

	@Message(id = 12032, value = "StateManager::terminate() should be invoked in every destructor", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_1();

	@Message(id = 12033, value = "StateManager::modified() invocation on an object whose state has not been restored - activating object", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_10();

	@Message(id = 12034, value = "Delete called on object with uid {0} and type {1} within atomic action.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_11(String arg0, String arg1);

	@Message(id = 12035, value = "StateManager.cleanup - could not save_state from terminate!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_12();

	@Message(id = 12036, value = "Attempt to use volatile store.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_13();

	@Message(id = 12037, value = "Volatile store not implemented!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_14();

	@Message(id = 12038, value = "Invalid object state.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_15();

	@Message(id = 12039, value = "Invalid object store type:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_16();

	@Message(id = 12040, value = "Activate of object with id = {0} and type {1} unexpectedly failed\"", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_2(String arg0, String arg1);

	@Message(id = 12041, value = "StateManager::deactivate - object store error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_3();

	@Message(id = 12042, value = "StateManager::deactivate - save_state error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_4();

	@Message(id = 12043, value = "StateManager::destroy for object-id {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_5(String arg0);

	@Message(id = 12044, value = "StateManager.destroy - failed to add abstract record to transaction {0}; check transaction status.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_6(String arg0);

	@Message(id = 12045, value = "StateManager.destroy - caught object store exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_7(String arg0);

	@Message(id = 12046, value = "StateManager.destroy - called on non-persistent or new object!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_8();

	@Message(id = 12047, value = "StateManager.restore_state - could not find StateManager state in object state!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_StateManager_9();

	@Message(id = 12048, value = "Mutex being destroyed with waiters.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Mutex_1();

	@Message(id = 12049, value = "Mutex.unlock - called by non-owning thread!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Mutex_2();

	@Message(id = 12050, value = "cannot get local host.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_1();

	@Message(id = 12051, value = "Cannot unpack into nullUid!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_10();

	@Message(id = 12052, value = "Uid.Uid recreate constructor could not recreate Uid!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_11();

	@Message(id = 12053, value = "Uid.Uid string constructor could not create nullUid", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_2();

	@Message(id = 12054, value = "Uid general parsing error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_3(String arg0);

	@Message(id = 12055, value = "Uid.Uid string constructor could not create nullUid for incorrect string: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_4(String arg0);

	@Message(id = 12056, value = "Uid.Uid string constructor incorrect: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_5(String arg0);

	@Message(id = 12057, value = "Uid.generateHash called for invalid Uid. Will ignore.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_6();

	@Message(id = 12058, value = "nullUid error for", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_7();

	@Message(id = 12059, value = "Invalid string:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_8();

	@Message(id = 12060, value = "Invalid Uid object.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_9();

	@Message(id = 12061, value = "Exception thrown creating Uid from bytes!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_bytes();

	@Message(id = 12062, value = "Exception thrown getting bytes!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_getbytes();

	@Message(id = 12063, value = "Uid.Uid string constructor {0} caught other throwable: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_Uid_npe(String arg0, String arg1);

	@Message(id = 12064, value = "AbstractRecord::AbstractRecord () - crash recovery constructor", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_AbstractRecord_1();

	@Message(id = 12065, value = "AbstractRecord.create {0} failed to find record.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_AbstractRecord_npe(String arg0);

	@Message(id = 12066, value = "Memory exhausted.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_ActionHierarchy_1();

	@Message(id = 12067, value = "Action nesting error - deletion of action id {0} invoked while child actions active", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_1(String arg0);

	@Message(id = 12068, value = "BasicAction::addAction () action {0} adding {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_10(String arg0, String arg1);

	@Message(id = 12069, value = "BasicAction::addChildAction () action {0} adding {1} result = {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_11(String arg0, String arg1, String arg2);

	@Message(id = 12070, value = "BasicAction::removeChildAction () action {0} removing {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_12(String arg0, String arg1);

	@Message(id = 12071, value = "BasicAction::removeChildAction () action {0} removing {1} result = {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_13(String arg0, String arg1, String arg2);

	@Message(id = 12072, value = "BasicAction::save_state - next record to pack is a   {0} record ({1}) should save it? = {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_14(String arg0, String arg1, String arg2);

	@Message(id = 12073, value = "Packing a {0} record", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_15(String arg0);

	@Message(id = 12074, value = "Packing a NONE_RECORD", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_16();

	@Message(id = 12075, value = "HeuristicList - packing a {0} record", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_17(String arg0);

	@Message(id = 12076, value = "HeuristicList - packing a NONE_RECORD", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_18();

	@Message(id = 12077, value = "Packing action status of {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_19(String arg0);

	@Message(id = 12078, value = "Aborting child {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_2(String arg0);

	@Message(id = 12079, value = "Unpacked a {0} record", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_20(String arg0);

	@Message(id = 12080, value = "BasicAction.restore_state - could not recover {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_21(String arg0);

	@Message(id = 12081, value = "HeuristicList - Unpacked heuristic list size of {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_22(String arg0);

	@Message(id = 12082, value = "HeuristicList - Unpacked a {0} record", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_23(String arg0);

	@Message(id = 12083, value = "BasicAction.restore_state - error unpacking action status.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_24();

	@Message(id = 12084, value = "Restored action status of {0} {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_25(String arg0, String arg1);

	@Message(id = 12085, value = "Restored action type {0} {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_26(String arg0, String arg1);

	@Message(id = 12086, value = "Restored heuristic decision of {0} {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_27(String arg0, String arg1);

	@Message(id = 12087, value = "BasicAction.destroy called on {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_28(String arg0);

	@Message(id = 12088, value = "BasicAction.Begin of action {0} ignored - incorrect invocation sequence {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_29(String arg0, String arg1);

	@Message(id = 12089, value = "Destructor of still running action id {0} invoked - Aborting", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_3(String arg0);

	@Message(id = 12090, value = "BasicAction.Begin of action {0} ignored - no parent and set as nested action!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_30(String arg0);

	@Message(id = 12091, value = "BasicAction.Begin of action {0} ignored - parent action {1} is not running: {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_31(String arg0, String arg1, String arg2);

	@Message(id = 12092, value = "The Arjuna licence does not permit any further transactions to be committed!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_32();

	@Message(id = 12093, value = "End called on non-running atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_33(String arg0);

	@Message(id = 12094, value = "End called on already committed atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_34(String arg0);

	@Message(id = 12095, value = "End called illegally on atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_35(String arg0);

	@Message(id = 12096, value = "BasicAction.End() - prepare phase of action-id {0} failed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_36(String arg0);

	@Message(id = 12097, value = "Received heuristic: {0} .", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_37(String arg0);

	@Message(id = 12098, value = "Action Aborting", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_38();

	@Message(id = 12099, value = "Abort called on non-running atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_39(String arg0);

	@Message(id = 12100, value = "Abort called on already aborted atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_40(String arg0);

	@Message(id = 12101, value = "Abort called illegaly on atomic action {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_41(String arg0);

	@Message(id = 12102, value = "BasicAction {0} - non-empty ( {1} ) pendingList {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_42(String arg0, String arg1, String arg2);

	@Message(id = 12103, value = "Transaction {0} marked as rollback only. Will abort.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_43(String arg0);

	@Message(id = 12104, value = "Cannot force parent to rollback - no handle!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_44();

	@Message(id = 12105, value = "BasicAction::prepare - creating intentions list failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_45(String arg0);

	@Message(id = 12106, value = "BasicAction::prepare - intentions list write failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_46(String arg0);

	@Message(id = 12107, value = "One-phase commit of action {0} received heuristic decision: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_47(String arg0, String arg1);

	@Message(id = 12108, value = "BasicAction.onePhaseCommit failed - no object store for atomic action state!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_48();

	@Message(id = 12109, value = "Prepare phase of nested action {0} received inconsistent outcomes.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_49(String arg0);

	@Message(id = 12110, value = "Activate of atomic action with id {0} and type {1} unexpectedly failed, could not load state.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_5(String arg0, String arg1);

	@Message(id = 12111, value = "Prepare phase of action {0} received heuristic decision: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_50(String arg0, String arg1);

	@Message(id = 12112, value = "BasicAction.doCommit for {0} received {1} from {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_51(String arg0, String arg1, String arg2);

	@Message(id = 12113, value = "Top-level abort of action {0} received heuristic decision: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_52(String arg0, String arg1);

	@Message(id = 12114, value = "Nested abort of action {0} received heuristic decision: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_53(String arg0, String arg1);

	@Message(id = 12115, value = "Top-level abort of action {0} received {1} from {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_54(String arg0, String arg1, String arg2);

	@Message(id = 12116, value = "Nested abort of action {0} received {1} from {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_55(String arg0, String arg1, String arg2);

	@Message(id = 12117, value = "BasicAction.checkIsCurrent {0} - terminating non-current transaction: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_56(String arg0, String arg1);

	@Message(id = 12118, value = "Commit of action id {0} invoked while multiple threads active within it.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_57(String arg0);

	@Message(id = 12119, value = "Abort of action id {0} invoked while multiple threads active within it.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_58(String arg0);

	@Message(id = 12120, value = "Commit of action id {0} invoked while child actions active", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_59(String arg0);

	@Message(id = 12121, value = "Deactivate of atomic action with id {0} and type {1} unexpectedly failed, could not save state.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_5a(String arg0, String arg1);

	@Message(id = 12122, value = "BasicAction::addChildThread () action {0} adding {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_6(String arg0, String arg1);

	@Message(id = 12123, value = "Abort of action id {0} invoked while child actions active", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_60(String arg0);

	@Message(id = 12124, value = "Aborting child: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_61(String arg0);

	@Message(id = 12125, value = "Now aborting self: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_62(String arg0);

	@Message(id = 12126, value = "BasicAction::removeAllChildThreads () action {0} removing {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_63(String arg0, String arg1);

	@Message(id = 12127, value = "BasicAction.updateState - Could not create ObjectState for failedList", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_64();

	@Message(id = 12128, value = "BasicAction.End - Could not write failed list", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_65();

	@Message(id = 12129, value = "Action {0} with parent status {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_66(String arg0, String arg1);

	@Message(id = 12130, value = "Running Top Level Action {0} from within nested action ({1})", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_67(String arg0, String arg1);

	@Message(id = 12131, value = "(Internal) BasicAction.merge - record rejected", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_68();

	@Message(id = 12132, value = "No object store for:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_69();

	@Message(id = 12133, value = "BasicAction::addChildThread () action {0} adding {1} result = {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_7(String arg0, String arg1, String arg2);

	@Message(id = 12134, value = "Could not remove intentions list:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_70();

	@Message(id = 12135, value = "Deactivation of atomic action with id {0} and type {1} unexpectedly failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_71(String arg0, String arg1);

	@Message(id = 12136, value = "BasicAction::removeChildThread () action {0} removing {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_8(String arg0, String arg1);

	@Message(id = 12137, value = "BasicAction::removeChildThread () action {0} removing {1} result = {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_BasicAction_9(String arg0, String arg1, String arg2);

	@Message(id = 12138, value = "CheckedAction::check - atomic action {0} commiting with {1} threads active!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_CheckedAction_1(String arg0, String arg1);

	@Message(id = 12139, value = "CheckedAction::check - atomic action {0} aborting with {1} threads active!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_CheckedAction_2(String arg0, String arg1);

	@Message(id = 12140, value = "RecordList::insert({0}) : merging {1} and {2} for {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_RecordList_1(String arg0, String arg1, String arg2, String arg3);

	@Message(id = 12141, value = "RecordList::insert({0}) : replacing {1} and {2} for {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_RecordList_2(String arg0, String arg1, String arg2, String arg3);

	@Message(id = 12142, value = "RecordList::insert({0}) : adding extra record of type {1} before {2} for {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_RecordList_3(String arg0, String arg1, String arg2, String arg3);

	@Message(id = 12143, value = "RecordList::insert({0}) : inserting {1} for {2} before {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_RecordList_4(String arg0, String arg1, String arg2, String arg3);

	@Message(id = 12144, value = "RecordList::insert({0}) : appending {1} for {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_RecordList_5(String arg0, String arg1, String arg2);

	@Message(id = 12145, value = "RecordList::insert({0}) : inserting {1} for {2} before {3} for {4}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_RecordList_6(String arg0, String arg1, String arg2, String arg3, String arg4);

	@Message(id = 12146, value = "TransactionReaper - attempting to insert an element that is already present.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_1();

	@Message(id = 12147, value = "TransactionReaper::check successfuly marked TX {0} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_10(String arg0);

	@Message(id = 12148, value = "TransactionReaper::check failed to mark TX {0}  as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_11(String arg0);

	@Message(id = 12149, value = "TransactionReaper::check exception while marking TX {0} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_12(String arg0);

	@Message(id = 12150, value = "TransactionReaper::doCancellations worker {0} missed interrupt when cancelling TX {1} -- exiting as zombie (zombie count decremented to {2})", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_13(String arg0, String arg1, String arg2);

	@Message(id = 12151, value = "TransactionReaper::doCancellations worker {0} successfuly marked TX {1} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_14(String arg0, String arg1);

	@Message(id = 12152, value = "TransactionReaper::doCancellations worker {0} failed to mark TX {1}  as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_15(String arg0, String arg1);

	@Message(id = 12153, value = "TransactionReaper::doCancellations worker {0} exception while marking TX {1} as rollback only", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_16(String arg0, String arg1);

	@Message(id = 12154, value = "TransactionReaper::getRemainingTimeoutMillis for {0} returning {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_17(String arg0, String arg1);

	@Message(id = 12155, value = "TransactionReaper::check timeout for TX {0} in state  {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_18(String arg0, String arg1);

	@Message(id = 12156, value = "TransactionReaper NORMAL mode is deprecated. Update config to use PERIODIC for equivalent behaviour.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_19();

	@Message(id = 12157, value = "TransactionReaper::check - comparing {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_2(String arg0);

	@Message(id = 12158, value = "TransactionReaper::getTimeout for {0} returning {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_3(String arg0, String arg1);

	@Message(id = 12159, value = "TransactionReaper::check interrupting cancel in progress for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_4(String arg0);

	@Message(id = 12160, value = "TransactionReaper::check worker zombie count {0} exceeds specified limit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_5(String arg0);

	@Message(id = 12161, value = "TransactionReaper::check worker {0} not responding to interrupt when cancelling TX {1} -- worker marked as zombie and TX scheduled for mark-as-rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_6(String arg0, String arg1);

	@Message(id = 12162, value = "TransactionReaper::doCancellations worker {0} successfully canceled TX {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_7(String arg0, String arg1);

	@Message(id = 12163, value = "TransactionReaper::doCancellations worker {0} failed to cancel TX {1} -- rescheduling for mark-as-rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_8(String arg0, String arg1);

	@Message(id = 12164, value = "TransactionReaper::doCancellations worker {0} exception during cancel of TX {1} -- rescheduling for mark-as-rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TransactionReaper_9(String arg0, String arg1);

	@Message(id = 12165, value = "TwoPhaseCoordinator.beforeCompletion - attempted rollback_only failed!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TwoPhaseCoordinator_1();

	@Message(id = 12166, value = "TwoPhaseCoordinator.beforeCompletion - failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TwoPhaseCoordinator_2(String arg0);

	@Message(id = 12167, value = "TwoPhaseCoordinator.beforeCompletion TwoPhaseCoordinator.afterCompletion called on still running transaction!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TwoPhaseCoordinator_3();

	@Message(id = 12168, value = "TwoPhaseCoordinator.afterCompletion - returned failure for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TwoPhaseCoordinator_4(String arg0);

	@Message(id = 12169, value = "TwoPhaseCoordinator.afterCompletion - failed for {0} with exception {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TwoPhaseCoordinator_4a(String arg0, String arg1);

	@Message(id = 12170, value = "TwoPhaseCoordinator.afterCompletion - failed for {0} with error {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TwoPhaseCoordinator_4b(String arg0, String arg1);

	@Message(id = 12171, value = "Name of XA node not defined. Using {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TxControl_1(String arg0);

	@Message(id = 12172, value = "Supplied name of node is too long. Using {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TxControl_2(String arg0);

	@Message(id = 12173, value = "Supplied name of node contains reserved character '-'. Using {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_TxControl_3(String arg0);

	@Message(id = 12174, value = "Cannot continue due to CheckedActionFactory resolution problem with", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_cafactoryerror();

	@Message(id = 12175, value = "Failed to resolve CheckedActionFactory class {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_checkedactionfactory(String arg0);

	@Message(id = 12176, value = "Could not create ObjectStore type:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_invalidos();

	@Message(id = 12177, value = "Could not recreate abstract record {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_norecordfound(String arg0);

	@Message(id = 12178, value = "Cannot begin new transaction as TM is disabled. Marking as rollback-only.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_notrunning();

	@Message(id = 12179, value = "Node name cannot exceed 64 bytes!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_toolong();

	@Message(id = 12180, value = "You have chosen to disable the Multiple Last Resources warning. You will see it only once.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_lastResource_disableWarning();

	@Message(id = 12181, value = "Adding multiple last resources is disallowed. Current resource is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_lastResource_disallow(String arg0);

	@Message(id = 12182, value = "Multiple last resources have been added to the current transaction. This is transactionally unsafe and should not be relied upon. Current resource is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_lastResource_multipleWarning(String arg0);

	@Message(id = 12183, value = "You have chosen to enable multiple last resources in the transaction manager. This is transactionally unsafe and should not be relied upon.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_lastResource_startupWarning();

	@Message(id = 12184, value = "unknown store: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ObjectStoreType_1(String arg0);

	@Message(id = 12185, value = "unknown store:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ObjectStoreType_2();

	@Message(id = 12186, value = "No implementation!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ObjectStore_1();

	@Message(id = 12187, value = "ActionStatusService: searching for uid: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ActionStatucService_5(String arg0);

	@Message(id = 12188, value = "transactionType: {0} uid: {1}   Status is {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ActionStatusService_1(String arg0, String arg1, String arg2);

	@Message(id = 12189, value = "Other Exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ActionStatusService_2(String arg0);

	@Message(id = 12190, value = "Exception retrieving action status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ActionStatusService_3();

	@Message(id = 12191, value = "matching Uid {0} found", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ActionStatusService_4(String arg0);

	@Message(id = 12192, value = "Exception when accessing transaction store {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ActionStatusService_6(String arg0);

	@Message(id = 12193, value = "Connection Lost to Recovery Manager", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ActionStatusService_7();

	@Message(id = 12194, value = "RecoverAtomicAction.replayPhase2 recovering {0} ActionStatus is {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoverAtomicAction_1(String arg0, String arg1);

	@Message(id = 12195, value = "RecoverAtomicAction.replayPhase2: Unexpected status: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoverAtomicAction_2(String arg0);

	@Message(id = 12196, value = "RecoverAtomicAction.replayPhase2( {0} )  finished", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoverAtomicAction_3(String arg0);

	@Message(id = 12197, value = "RecoverAtomicAction: transaction {0} not activated, unable to replay phase 2 commit. Check state has not already been completed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoverAtomicAction_4(String arg0);

	@Message(id = 12198, value = "RecoverAtomicAction - tried to move failed activation log {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoverAtomicAction_5(String arg0);

	@Message(id = 12199, value = "Invalid recovery manager port specified {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoveryManager_1(String arg0);

	@Message(id = 12200, value = "Invalid recovery manager host specified {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoveryManager_2(String arg0);

	@Message(id = 12201, value = "Recovery manager bound to {0}:{1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoveryManager_3(String arg0, String arg1);

	@Message(id = 12202, value = "Connected to recovery manager on {0}:{1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoveryManager_4(String arg0, String arg1);

	@Message(id = 12203, value = "Invalid recovery manager port specified", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoveryManager_5();

	@Message(id = 12204, value = "Exception when accessing data store {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnectionManager_1(String arg0);

	@Message(id = 12205, value = "Object store exception {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnectionManager_2(String arg0);

	@Message(id = 12206, value = "found process uid {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnectionManager_3(String arg0);

	@Message(id = 12207, value = "added TransactionStatusConnector to table for process uid {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnectionManager_4(String arg0);

	@Message(id = 12208, value = "Starting service {0} on port {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_1(String arg0, String arg1);

	@Message(id = 12209, value = "Unknown host {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_10(String arg0);

	@Message(id = 12210, value = "Invalid port specified", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_11();

	@Message(id = 12211, value = "Unknown host specified", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_12();

	@Message(id = 12212, value = "Invalid host or port", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_13();

	@Message(id = 12213, value = "Failed to create server socket on address {0} and port: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_14(String arg0, String arg1);

	@Message(id = 12214, value = "Listener failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_2();

	@Message(id = 12215, value = "TransactionStatusManager started on port {0} and host {1} with service {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_3(String arg0, String arg1, String arg2);

	@Message(id = 12216, value = "Class not found: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_4(String arg0);

	@Message(id = 12217, value = "Failed to instantiate service class: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_5(String arg0);

	@Message(id = 12218, value = "Illegal access to service class: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_6(String arg0);

	@Message(id = 12219, value = "Failed to create server socket on port: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_7(String arg0);

	@Message(id = 12220, value = "Invalid port specified {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_8(String arg0);

	@Message(id = 12221, value = "Could not get unique port.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManager_9();

	@Message(id = 12222, value = "com.arjuna.ats.arjuna.state.InputBuffer_1 - Invalid input buffer: byte.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_1();

	@Message(id = 12223, value = "com.arjuna.ats.arjuna.state.InputBuffer_10 - Invalid input buffer: string.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_10();

	@Message(id = 12224, value = "com.arjuna.ats.arjuna.state.InputBuffer_11 - Invalid from buffer", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_11();

	@Message(id = 12225, value = "com.arjuna.ats.arjuna.state.InputBuffer_2 - Invalid input buffer: bytes.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_2();

	@Message(id = 12226, value = "com.arjuna.ats.arjuna.state.InputBuffer_3 - Invalid input buffer: boolean.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_3();

	@Message(id = 12227, value = "com.arjuna.ats.arjuna.state.InputBuffer_4 - Invalid input buffer: char.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_4();

	@Message(id = 12228, value = "com.arjuna.ats.arjuna.state.InputBuffer_5 - Invalid input buffer: short.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_5();

	@Message(id = 12229, value = "com.arjuna.ats.arjuna.state.InputBuffer_6 - Invalid input buffer: int.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_6();

	@Message(id = 12230, value = "com.arjuna.ats.arjuna.state.InputBuffer_7 - Invalid input buffer: long.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_7();

	@Message(id = 12231, value = "com.arjuna.ats.arjuna.state.InputBuffer_8 - Invalid input buffer: float.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_8();

	@Message(id = 12232, value = "com.arjuna.ats.arjuna.state.InputBuffer_9 - Invalid input buffer: double", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_InputBuffer_9();

	@Message(id = 12233, value = "com.arjuna.ats.arjuna.state.OutputBuffer_1 - Invalid input buffer: byte.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_1();

	@Message(id = 12234, value = "com.arjuna.ats.arjuna.state.OutputBuffer_10 - Invalid input buffer: string.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_10();

	@Message(id = 12235, value = "com.arjuna.ats.arjuna.state.OutputBuffer_11 - Invalid from buffer", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_11();

	@Message(id = 12236, value = "com.arjuna.ats.arjuna.state.OutputBuffer_2 - Invalid input buffer: bytes.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_2();

	@Message(id = 12237, value = "com.arjuna.ats.arjuna.state.OutputBuffer_3 - Invalid input buffer: boolean.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_3();

	@Message(id = 12238, value = "com.arjuna.ats.arjuna.state.OutputBuffer_4 - Invalid input buffer: char.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_4();

	@Message(id = 12239, value = "com.arjuna.ats.arjuna.state.OutputBuffer_5 - Invalid input buffer: short.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_5();

	@Message(id = 12240, value = "com.arjuna.ats.arjuna.state.OutputBuffer_6 - Invalid input buffer: int.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_6();

	@Message(id = 12241, value = "com.arjuna.ats.arjuna.state.OutputBuffer_7 - Invalid input buffer: long.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_7();

	@Message(id = 12242, value = "com.arjuna.ats.arjuna.state.OutputBuffer_8 - Invalid input buffer: float.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_8();

	@Message(id = 12243, value = "com.arjuna.ats.arjuna.state.OutputBuffer_9 - Invalid input buffer: double", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_state_OutputBuffer_9();

	@Message(id = 12244, value = "remove committed failed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_mbean_m_1();

	@Message(id = 12245, value = "remove ok.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_mbean_m_2();

	@Message(id = 12246, value = "remove committed exception: {0}.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_mbean_m_3(String arg0);

	@Message(id = 12247, value = "registering bean {0}.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_util_JMXServer_m_1(String arg0);

	@Message(id = 12248, value = "Instance already exists: {0}.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_util_JMXServer_m_2(String arg0);

	@Message(id = 12249, value = "Error registrating {0} - {1}.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_util_JMXServer_m_3(String arg0, String arg1);

	@Message(id = 12250, value = "Try to unregister mbean with invalid name {0}.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_util_JMXServer_m_4(String arg0);

	@Message(id = 12251, value = "Unable to unregister bean {0} error: {1}.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_util_JMXServer_m_5(String arg0, String arg1);

	@Message(id = 12252, value = "Unable to unregister bean {0} error: {1}.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_osb_util_JMXServer_m_6(String arg0, String arg1);

	@Message(id = 12253, value = "FileLock.lock called for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_FileLock_1(String arg0);

	@Message(id = 12254, value = "FileLock.unlock called for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_FileLock_2(String arg0);

	@Message(id = 12255, value = "FileLock.createFile called for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_FileLock_3(String arg0);

	@Message(id = 12256, value = "An error occurred while creating file {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_FileLock_4(String arg0);

	@Message(id = 12257, value = "FileLock.lockFile called for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_FileLock_5(String arg0);

	@Message(id = 12258, value = "FileLock.unlockFile called for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_FileLock_6(String arg0);

	@Message(id = 12259, value = "Utility.getDefaultProcess - failed with", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_Utility_1();

	@Message(id = 12260, value = "Unable to use InetAddress.getLocalHost() to resolve address.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_Utility_2();

	@Message(id = 12261, value = "Attempt to suspend a non-AtomicAction transaction. Type is {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_ats_atomicaction_1(String arg0);

	@Message(id = 12262, value = "StateManagerFriend.forgetAction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_abstractrecords_smf1();

	@Message(id = 12263, value = "StateManagerFriend.destroyed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_abstractrecords_smf2();

	@Message(id = 12264, value = "StateManagerFriend.rememberAction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_abstractrecords_smf3();

	@Message(id = 12265, value = "className is null", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_ClassloadingUtility_1();

	@Message(id = 12266, value = "attempt to load {0} threw ClassNotFound. Wrong classloader?", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_ClassloadingUtility_2(String arg0);

	@Message(id = 12267, value = "class {0} does not implement {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_ClassloadingUtility_3(String arg0, String arg1);

	@Message(id = 12268, value = "can't create new instance of {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_ClassloadingUtility_4(String arg0);

	@Message(id = 12269, value = "can't access {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_ClassloadingUtility_5(String arg0);

	@Message(id = 12270, value = "can't initialize from string {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_common_ClassloadingUtility_6(String arg0);

	@Message(id = 12271, value = "Thread {0} sleeping for {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_ReaperThread_1(String arg0, String arg1);

	@Message(id = 12272, value = "Thread {0} waiting for cancelled TXs", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_ReaperWorkerThread_1(String arg0);

	@Message(id = 12273, value = "Thread {0} performing cancellations", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_coordinator_ReaperWorkerThread_2(String arg0);

	@Message(id = 12274, value = "ActionStore.currentState({0}, {1}) - returning {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ActionStore_1(String arg0, String arg1, String arg2);

	@Message(id = 12275, value = "Commit state failed for {0} and {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_CacheStore_1(String arg0, String arg1);

	@Message(id = 12276, value = "Remove state failed for {0} and {1} and {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_CacheStore_2(String arg0, String arg1, String arg2);

	@Message(id = 12277, value = "Write state failed for {0} and {1} and {2} and {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_CacheStore_3(String arg0, String arg1, String arg2, String arg3);

	@Message(id = 12278, value = "Unknown work type {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_CacheStore_4(String arg0);

	@Message(id = 12279, value = "FileSystemStore::setupStore - cannot access root of object store: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_1(String arg0);

	@Message(id = 12280, value = "FileSystemStore.removeFromCache - no entry for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_2(String arg0);

	@Message(id = 12281, value = "FileSystemStore.renameFromTo - from {0} not present. Possibly renamed by crash recovery.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_20(String arg0);

	@Message(id = 12282, value = "FileSystemStore::allObjUids - could not pack Uid.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_2a();

	@Message(id = 12283, value = "FileSystemStore::allObjUids - could not pack end of list Uid.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_3();

	@Message(id = 12284, value = "FileSytemStore::allTypes - could not pack entry string.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_4();

	@Message(id = 12285, value = "FileSystemStore::allTypes - could not pack end of list string.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_5();

	@Message(id = 12286, value = "FileSystemStore::setupStore - error from unpack object store.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_6();

	@Message(id = 12287, value = "FileSystemStore::allTypes - could not pack entry string.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_7();

	@Message(id = 12288, value = "FileSystemStore::createHierarchy - null directory name.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_FileSystemStore_8();

	@Message(id = 12289, value = "HashedStore.create caught: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_HashedStore_1(String arg0);

	@Message(id = 12290, value = "invalid number of hash directories: {0}. Will use default.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_HashedStore_2(String arg0);

	@Message(id = 12291, value = "HashedStore.allObjUids - could not pack Uid.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_HashedStore_5();

	@Message(id = 12292, value = "HashedStore.allObjUids - could not pack end of list Uid.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_HashedStore_6();

	@Message(id = 12293, value = "hide_state caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_1(String arg0);

	@Message(id = 12294, value = "remove_state - type() operation of object with uid {0} returns NULL", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_10(String arg0);

	@Message(id = 12295, value = "invalid initial pool size: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_11(String arg0);

	@Message(id = 12296, value = "invalid maximum pool size: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_12(String arg0);

	@Message(id = 12297, value = "initialise caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_13(String arg0);

	@Message(id = 12298, value = "getState caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_14(String arg0);

	@Message(id = 12299, value = "removeFromCache - no entry for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_15(String arg0);

	@Message(id = 12300, value = "getPool caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_16(String arg0);

	@Message(id = 12301, value = "getPool - interrupted while waiting for a free connection", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_17();

	@Message(id = 12302, value = "freePool - freeing a connection which is already free!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_18();

	@Message(id = 12303, value = "reveal_state caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_2(String arg0);

	@Message(id = 12304, value = "currentState caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_3(String arg0);

	@Message(id = 12305, value = "allObjUids caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_4(String arg0);

	@Message(id = 12306, value = "allObjUids - pack of Uid failed: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_5(String arg0);

	@Message(id = 12307, value = "allTypes caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_6(String arg0);

	@Message(id = 12308, value = "allTypes - pack of Uid failed:{0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_7(String arg0);

	@Message(id = 12309, value = "remove_state caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_8(String arg0);

	@Message(id = 12310, value = "remove_state() attempted removal of {0} state for object with uid {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_9(String arg0, String arg1);

	@Message(id = 12311, value = "JDBCImple:read_state failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_readfailed();

	@Message(id = 12312, value = "JDBCImple:write_state caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCImple_writefailed(String arg0);

	@Message(id = 12313, value = "JDBCStore could not setup store < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCStore_1(String arg0, String arg1);

	@Message(id = 12314, value = "Received: {0} for: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCStore_2(String arg0, String arg1);

	@Message(id = 12315, value = "JDBCStore.setupStore failed to initialise!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCStore_3();

	@Message(id = 12316, value = "JDBCStore invalid Object parameter: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCStore_4(String arg0);

	@Message(id = 12317, value = "No JDBCAccess implementation provided!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_JDBCStore_5();

	@Message(id = 12318, value = "ShadowingStore.commit_state - store invalid!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_1();

	@Message(id = 12319, value = "ShadowingStore::remove_state() - state {0} does not exist for type {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_10(String arg0, String arg1);

	@Message(id = 12320, value = "ShadowingStore::remove_state() - unlink failed on {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_11(String arg0);

	@Message(id = 12321, value = "ShadowingStore.remove_state() - fd error for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_12(String arg0);

	@Message(id = 12322, value = "ShadowingStore::remove_state() attempted removal of", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_13();

	@Message(id = 12323, value = "UNKNOWN state for object with uid {0} , type {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_14(String arg0, String arg1);

	@Message(id = 12324, value = "HIDDEN state for object with uid {0} , type {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_15(String arg0, String arg1);

	@Message(id = 12325, value = "state for object with uid {0} , type {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_16(String arg0, String arg1);

	@Message(id = 12326, value = "ShadowingStore.remove_state - type() operation of object with uid {0} returns NULL", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_17(String arg0);

	@Message(id = 12327, value = "ShadowingStore::write_state() - openAndLock failed for {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_18(String arg0);

	@Message(id = 12328, value = "ShadowingStore::write_state - unlock or close of {0} failed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_19(String arg0);

	@Message(id = 12329, value = "ShadowStore::commit_state - failed to rename {0} to {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_2(String arg0, String arg1);

	@Message(id = 12330, value = "ShadowingStore.renameFromTo - from {0} not present. Possibly renamed by crash recovery.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_20(String arg0);

	@Message(id = 12331, value = "ShadowingStore.renameFromTo - failed to lock: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_21(String arg0);

	@Message(id = 12332, value = "ShadowingStore.currentState({0}, {1}) - returning {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_22(String arg0, String arg1, String arg2);

	@Message(id = 12333, value = "ShadowStore::hide_state - failed to rename {0} to {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_3(String arg0, String arg1);

	@Message(id = 12334, value = "ShadowStore::reveal_state - failed to rename {0} to {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_4(String arg0, String arg1);

	@Message(id = 12335, value = "ShadowingStore.create caught: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_5(String arg0);

	@Message(id = 12336, value = "ShadowingStore.read_state - store invalid!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_6();

	@Message(id = 12337, value = "ShadowingStore::read_state() failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_7();

	@Message(id = 12338, value = "ShadowingStore::read_state - unlock or close of {0} failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_8(String arg0);

	@Message(id = 12339, value = "ShadowingStore::remove_state() - access problems on {0} and {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_ShadowingStore_9(String arg0, String arg1);

	@Message(id = 12340, value = "oracle:read_state failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_jdbc_oracle_1();

	@Message(id = 12341, value = "oracle:write_state caught exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_jdbc_oracle_2(String arg0);

	@Message(id = 12342, value = "No typename for object:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_notypenameuid();

	@Message(id = 12343, value = "allTypes - could not pack end of list string.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_objectstore_packProblem();

	@Message(id = 12344, value = "RecoveryManagerStatusModule: Object store exception: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_AtomicActionRecoveryModule_1(String arg0);

	@Message(id = 12345, value = "failed to recover Transaction {0} {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_AtomicActionRecoveryModule_2(String arg0, String arg1);

	@Message(id = 12346, value = "failed to access transaction store {0} {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_AtomicActionRecoveryModule_3(String arg0, String arg1);

	@Message(id = 12347, value = "AtomicActionRecoveryModule first pass", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_AtomicActionRecoveryModule_4();

	@Message(id = 12348, value = "AtomicActionRecoveryModule second pass", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_AtomicActionRecoveryModule_5();

	@Message(id = 12349, value = "Connection - IOException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_Connection_1();

	@Message(id = 12350, value = "Setting timeout exception.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_Connection_2();

	@Message(id = 12351, value = "Expiry scan interval set to {0} seconds", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_1(String arg0);

	@Message(id = 12352, value = "Loading expiry scanner: could not find class {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_10(String arg0);

	@Message(id = 12353, value = "{0} has inappropriate value ({1})", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_11(String arg0, String arg1);

	@Message(id = 12354, value = "ExpiredEntryMonitor running at {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_12(String arg0);

	@Message(id = 12355, value = "Expiry scan zero - not scanning", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_2();

	@Message(id = 12356, value = "No Expiry scanners loaded - not scanning", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_3();

	@Message(id = 12357, value = "ExpiredEntryMonitor - constructed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_4();

	@Message(id = 12358, value = "ExpiredEntryMonitor - no scans on first iteration", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_5();

	@Message(id = 12359, value = "Loading expiry scanner {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_6(String arg0);

	@Message(id = 12360, value = "Attempt to load expiry scanner with null class name!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_7();

	@Message(id = 12361, value = "Loading expiry scanner {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_8(String arg0);

	@Message(id = 12362, value = "Expiry scanner {0} does not conform to ExpiryScanner interface", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredEntryMonitor_9(String arg0);

	@Message(id = 12363, value = "ExpiredTransactionScanner created, with expiry time of {0} seconds", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionScanner_1(String arg0);

	@Message(id = 12364, value = "ExpiredTransactionScanner - exception during attempted move {0} {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionScanner_2(String arg0, String arg1);

	@Message(id = 12365, value = "ExpiredTransactionScanner - could not moved log {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionScanner_3(String arg0);

	@Message(id = 12366, value = "ExpiredTransactionScanner - log {0} is assumed complete and will be moved.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionScanner_4(String arg0);

	@Message(id = 12367, value = "ExpiredTransactionStatusManagerScanner created, with expiry time of {0}  seconds", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionStatusManagerScanner_1(String arg0);

	@Message(id = 12368, value = "ExpiredTransactionStatusManagerScanner - scanning to remove items from before {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionStatusManagerScanner_2(String arg0);

	@Message(id = 12369, value = "Removing old transaction status manager item {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionStatusManagerScanner_3(String arg0);

	@Message(id = 12370, value = "Expiry scan interval set to  {0}  seconds", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionStatusManagerScanner_4(String arg0);

	@Message(id = 12371, value = "{0}  has inappropriate value ({1})", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_ExpiredTransactionStatusManagerScanner_5(String arg0, String arg1);

	@Message(id = 12372, value = "Listener - IOException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_Listener_2();

	@Message(id = 12373, value = "Attempt to load recovery module with null class name!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_1();

	@Message(id = 12374, value = "Ignoring request to scan because RecoveryManager state is: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_10(String arg0);

	@Message(id = 12375, value = "Invalid host specified {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_11(String arg0);

	@Message(id = 12376, value = "Could not create recovery listener", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_12();

	@Message(id = 12377, value = "Recovery manager listening on endpoint {0}:{1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_13(String arg0, String arg1);

	@Message(id = 12378, value = "Periodic recovery first pass at {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_14(String arg0);

	@Message(id = 12379, value = "Periodic recovery second pass at {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_15(String arg0);

	@Message(id = 12380, value = "Recovery module {0} does not conform to RecoveryModule interface", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_2(String arg0);

	@Message(id = 12381, value = "Loading recovery module: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_3(String arg0);

	@Message(id = 12382, value = "Loading recovery module: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_4(String arg0);

	@Message(id = 12383, value = "Loading recovery module: could not find class {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_5(String arg0);

	@Message(id = 12384, value = "{0} has inappropriate value ( {1} )", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_6(String arg0, String arg1);

	@Message(id = 12385, value = "{0} has inappropriate value ( {1} )", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_7(String arg0, String arg1);

	@Message(id = 12386, value = "Invalid port specified {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_8(String arg0);

	@Message(id = 12387, value = "Could not create recovery listener {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_PeriodicRecovery_9(String arg0);

	@Message(id = 12388, value = "Attempt to load recovery activator with null class name!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecActivatorLoader_1();

	@Message(id = 12389, value = "Recovery module {0} does not conform to RecoveryActivator interface", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecActivatorLoader_2(String arg0);

	@Message(id = 12390, value = "Loading recovery activator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecActivatorLoader_3(String arg0);

	@Message(id = 12391, value = "Loading recovery activator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecActivatorLoader_4(String arg0);

	@Message(id = 12392, value = "Loading recovery module: could not find class {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecActivatorLoader_5(String arg0);

	@Message(id = 12393, value = "Start RecoveryActivators", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecActivatorLoader_6();

	@Message(id = 12394, value = "property io exception {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoveryManagerImple_1(String arg0);

	@Message(id = 12395, value = "socket io exception {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_RecoveryManagerImple_2(String arg0);

	@Message(id = 12396, value = "TransactionStatusConnector.delete called erroneously", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnector_1();

	@Message(id = 12397, value = "Connection lost to TransactionStatusManagers' process", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnector_2();

	@Message(id = 12398, value = "Connection lost to TransactionStatusManagers' process", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnector_3();

	@Message(id = 12399, value = "TransactionStatusManager process for uid {0} is ALIVE. connected to host: {1}, port: {2} on socket: {3}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnector_4(String arg0, String arg1, String arg2, String arg3);

	@Message(id = 12400, value = "TransactionStatusManager process for uid {0} is DEAD.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnector_5(String arg0);

	@Message(id = 12401, value = "Failed to establish connection to server", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusConnector_6();

	@Message(id = 12402, value = "Problem with removing host/port item {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManagerItem_1(String arg0);

	@Message(id = 12403, value = "Problem with storing host/port {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManagerItem_2(String arg0);

	@Message(id = 12404, value = "Problem retrieving host/port {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManagerItem_3(String arg0);

	@Message(id = 12405, value = "Failed to obtain host {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManagerItem_4(String arg0);

	@Message(id = 12406, value = "TransactionStatusManagerItem host: {0} port: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_TransactionStatusManagerItem_5(String arg0, String arg1);

	@Message(id = 12407, value = "Other Exception:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_WorkerService_1();

	@Message(id = 12408, value = "IOException", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_WorkerService_2();

	@Message(id = 12409, value = "RecoveryManager scan scheduled to begin.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_WorkerService_3();

	@Message(id = 12410, value = "RecoveryManager scan completed.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_WorkerService_4();

	@Message(id = 12411, value = "RecoveryManagerImple: cannot bind to socket on address {0} and port {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_fail(String arg0, String arg1);

	@Message(id = 12412, value = "RecoveryManagerImple is ready. Socket listener is turned off.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_localready();

	@Message(id = 12413, value = "RecoveryManagerImple is ready on port {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_recovery_socketready(String arg0);

	@Message(id = 12414, value = "Transaction {0} and {1} not activate.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_log_eaa1(String arg0, String arg1);

	@Message(id = 12415, value = "Error - could not get resource to forget heuristic. Left on Heuristic List.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_tools_log_eaa2();

	@Message(id = 12416, value = "Could not get back a valid pid.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_ExecProcessId_1();

	@Message(id = 12417, value = "Problem executing getpids utility:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_ExecProcessId_2();

	@Message(id = 12418, value = "Problem executing command:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_ExecProcessId_3();

	@Message(id = 12419, value = "Problem getting pid information from stream:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_ExecProcessId_4();

	@Message(id = 12420, value = "Encountered a problem when closing the data stream {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_ExecProcessId_5(String arg0);

	@Message(id = 12421, value = "FileProcessId.getpid - could not locate temporary directory.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_FileProcessId_1();

	@Message(id = 12422, value = "FileProcessId.getpid could not create unique file.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_FileProcessId_2();

	@Message(id = 12423, value = "Could not get back a valid pid.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_MBeanProcessId_1();

	@Message(id = 12424, value = "getName returned unrecognized format:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_MBeanProcessId_2();

	@Message(id = 12425, value = "Could not get back a valid pid.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_ManualProcessId_1();

	@Message(id = 12426, value = "No process identifier specified in configuration!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_ManualProcessId_2();

	@Message(id = 12427, value = "Invalid process identifier specified:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_ManualProcessId_3();

	@Message(id = 12428, value = "SocketProcessId.getpid could not get unique port.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void log_utils_SocketProcessId_2();

    /*
        Allocate new messages directly above this notice.
          - id: use the next id number in numeric sequence. Don't reuse ids.
          The first two digits of the id(XXyyy) denote the module
            all message in this file should have the same prefix.
          - value: default (English) version of the log message.
          - level: according to severity semantics defined at http://docspace.corp.redhat.com/docs/DOC-30217
          Debug and trace don't get i18n. Everything else MUST be i18n.
     */
}
