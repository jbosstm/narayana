/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.mw.wstx.logging;


import org.jboss.logging.annotations.*;

import com.arjuna.mw.wscf.model.twophase.vote.Vote;
import com.arjuna.wst.Durable2PCParticipant;
import com.arjuna.wst.Volatile2PCParticipant;

import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.annotations.Message.Format.*;

import java.util.List;

/**
 * i18n log messages for the wstx module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface wstxI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

    @Message(id = 45001, value = "Error in {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_client_JaxHCP_1(String arg0, @Cause() Throwable arg1);

	@Message(id = 45002, value = "Error in {0} Unknown context type: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_client_JaxHCP_2(String arg0, String arg1);

//	@Message(id = 45003, value = "Unknown context type:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_mw_wst_client_JaxHCP_3();

	@Message(id = 45004, value = "WSTX Initialisation: init failed", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_mw_wst_deploy_WSTXI_1(@Cause() Throwable arg0);

	@Message(id = 45005, value = "{0} not found.", format = MESSAGE_FORMAT)
	public String get_mw_wst_deploy_WSTXI_21(String arg0);

	@Message(id = 45006, value = "Failed to create document: {0}", format = MESSAGE_FORMAT)
	public String get_mw_wst_deploy_WSTXI_22(String arg0);

	@Message(id = 45007, value = "Missing WSTX Initialisation", format = MESSAGE_FORMAT)
	public String get_mw_wst_deploy_WSTXI_23();

	@Message(id = 45008, value = "Error in {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_service_JaxHCP_1(String arg0, @Cause() Throwable arg1);

	@Message(id = 45009, value = "Error in {0} Unknown context type: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_service_JaxHCP_2(String arg0, String arg2);

//	@Message(id = 45010, value = "Unknown context type:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_mw_wst_service_JaxHCP_3();

	@Message(id = 45011, value = "Error in {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_client_JaxHC11P_1(String arg0, @Cause() Throwable arg1);

	@Message(id = 45012, value = "Error in {0} Unknown context type: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_client_JaxHC11P_2(String arg0, String arg1);

//	@Message(id = 45013, value = "Unknown context type:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_mw_wst11_client_JaxHC11P_3();

	@Message(id = 45014, value = "WSTX11 Initialisation: init failed", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_mw_wst11_deploy_WSTXI_1(@Cause() Throwable arg0);

	@Message(id = 45015, value = "{0} not found.", format = MESSAGE_FORMAT)
	public String get_mw_wst11_deploy_WSTXI_21(String arg0);

	@Message(id = 45016, value = "Failed to create document: {0}", format = MESSAGE_FORMAT)
	public String get_mw_wst11_deploy_WSTXI_22(String arg0);

	@Message(id = 45017, value = "Missing WSTX Initialisation", format = MESSAGE_FORMAT)
	public String get_mw_wst11_deploy_WSTXI_23();

	@Message(id = 45018, value = "Error in {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_service_JaxHC11P_1(String arg0, @Cause() Throwable arg1);

	@Message(id = 45019, value = "Error in {0} Unknown context type: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_service_JaxHC11P_2(String arg0, String arg1);

//	@Message(id = 45020, value = "Unknown context type:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_mw_wst11_service_JaxHC11P_3();

	@Message(id = 45021, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_Context11FactoryImple_1(String arg0, String arg1);

	@Message(id = 45022, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_Context11FactoryImple_3();

	@Message(id = 45023, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_ContextFactoryImple_1(String arg0, String arg1);

	@Message(id = 45024, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_ContextFactoryImple_3();

	@Message(id = 45025, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_Registrar11Imple_1(String arg0, String arg1);

	@Message(id = 45026, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_RegistrarImple_1(String arg0, String arg1);

	@Message(id = 45027, value = "ignoring context {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_context_ArjunaContextImple_1(String arg0);

	@Message(id = 45028, value = "One context was null!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_local_ContextManager_1();

	@Message(id = 45029, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_local_LocalContextFactoryImple_1(String arg0, String arg1);

	@Message(id = 45030, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_local_LocalContextFactoryImple_11();

	@Message(id = 45031, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_local_LocalRegistrarImple_1(String arg0, String arg1);

	@Message(id = 45032, value = "Not implemented!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_local_TransactionManagerImple_1();

	@Message(id = 45033, value = "comms timeout attempting to cancel WS-AT participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_cancel_1(String arg0);

	@Message(id = 45034, value = "comms timeout attempting to commit WS-AT participant {0} : {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_confirm_1(String id, Durable2PCParticipant participant);

	@Message(id = 45035, value = "comms timeout attempting to prepare WS-AT participant {0} : {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_prepare_1(String id, Durable2PCParticipant participant);

	@Message(id = 45036, value = "Not implemented!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_remote_Transaction11ManagerImple_1();

	@Message(id = 45037, value = "Not implemented!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_remote_TransactionManagerImple_1();

	@Message(id = 45038, value = "Received context is null!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_remote_UserTransaction11Imple__2();

	@Message(id = 45039, value = "Received context is null!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_at_remote_UserTransactionImple_2();

	@Message(id = 45040, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_Context11FactoryImple_1(String arg0, String arg1);

	@Message(id = 45041, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_ba_Context11FactoryImple_3();

	@Message(id = 45042, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_ContextFactoryImple_1(String arg0, String arg1);

	@Message(id = 45043, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_ba_ContextFactoryImple_3();

	@Message(id = 45044, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_LocalContextFactoryImple_1(String arg0, String arg1);

	@Message(id = 45045, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_Registrar11Imple_1(String arg0, String arg1);

	@Message(id = 45046, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_RegistrarImple_1(String arg0, String arg1);

	@Message(id = 45047, value = "ignoring context {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_context_ArjunaContextImple_1(String arg0);

	@Message(id = 45048, value = "One context was null!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_ba_local_ContextManager_1();

	@Message(id = 45049, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_ba_local_LocalContextFactoryImple_11();

	@Message(id = 45050, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_local_LocalRegistrarImple_1(String arg0, String arg1);

//	@Message(id = 45051, value = "Invalid address.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_mwlabs_wst_ba_remote_UserBusinessActivityImple_1();

	@Message(id = 45052, value = "Received context is null!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_ba_remote_UserBusinessActivityImple_2();

	@Message(id = 45053, value = "No termination context!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst_ba_remote_UserBusinessActivityImple_3();

	@Message(id = 45054, value = "Participant not persistable.", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_mwlabs_wst_util_PersistableParticipantHelper_1();

	@Message(id = 45055, value = "Error persisting participant.", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_mwlabs_wst_util_PersistableParticipantHelper_2(@Cause() Throwable arg0);

	@Message(id = 45056, value = "Error restoring participant.", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_mwlabs_wst_util_PersistableParticipantHelper_3(@Cause() Throwable arg0);

	@Message(id = 45057, value = "ignoring context {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst11_at_context_ArjunaContextImple_1(String arg0);

	@Message(id = 45058, value = "ignoring context {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst11_ba_context_ArjunaContextImple_1(String arg0);

//	@Message(id = 45059, value = "Invalid address.", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_mwlabs_wst11_ba_remote_UserBusinessActivityImple_1();

	@Message(id = 45060, value = "Received context is null!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst11_ba_remote_UserBusinessActivityImple_2();

	@Message(id = 45061, value = "No termination context!", format = MESSAGE_FORMAT)
	public String get_mwlabs_wst11_ba_remote_UserBusinessActivityImple_3();

    @Message(id = 45062, value = "Coordinator cancelled the activity", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
   	public void warn_mwlabs_wst11_ba_coordinator_cancelled_activity();

    @Message(id = 45063, value = "Wrong vote type {0} on prepare of volatile 2PC participant {1}."
            + "Expecting one from {2}.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Volatile2PC_prepare_wrong_type(com.arjuna.wst.Vote vote,
            Volatile2PCParticipant participant, List<Class<?>> expectedVotes);

    @Message(id = 45064, value = "Calling prepare on volatile participant but participant is null.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Volatile2PC_prepare_is_null();

    @Message(id = 45065, value = "Cannot prepare participant {0}.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Volatile2PC_prepare(Volatile2PCParticipant participant, @Cause() Throwable t);

    @Message(id = 45066, value = "Calling confirm on volatile participant but participant is null.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Volatile2PC_confirm_is_null();

    @Message(id = 45067, value = "Calling cancel on volatile participant but participant is null.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Volatile2PC_cancel_is_null();

    @Message(id = 45068, value = "cannot commit durable participant {0} : {1}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Durable2PC_confirm (
            String id, Durable2PCParticipant participant, @Cause() Throwable t);

    @Message(id = 45069, value = "Calling confirm on durable participant but participant with id {0} is null.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Durable2PC_confirm_is_null(String id);

    @Message(id = 45070, value = "cannot prepare durable participant {0} : {1}", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Durable2PC_prepare (
            String id, Durable2PCParticipant participant, @Cause() Throwable t);

    @Message(id = 45071, value = "cannot commit one phase durable participant {0} : {1}", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_wst_at_participants_Durable2PC_commit_one_phase (
            String id, Durable2PCParticipant participant, @Cause() Throwable t);

    @Message(id = 45072, value = "Calling cancel on durable participant but participant with id {0} is null.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Durable2PC_cancel_is_null(String id);

    @Message(id = 45073, value = "Durable participant {0} : {1} was cancelled.", format = MESSAGE_FORMAT)
    @LogMessage(level = WARN)
    public void warn_wst_at_participants_Durable2PC_canceled(String id, Durable2PCParticipant participant);

    @Message(id = 45074, value = "Confirm one phase call of durable participant {0} : {1} failed.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Durable2PC_one_phase_failed(String id, Durable2PCParticipant participant, @Cause() Throwable t);

    @Message(id = 45075, value = "Calling confirm one phase on durable participant but participant with id {0} is null.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Durable2PC_confirm_one_phase_is_null(String id);

    @Message(id = 45076, value = "Unknown call of participant {0} : {1} failed.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Durable2PC_unknown(String id, Durable2PCParticipant participant, @Cause() Throwable t);

    @Message(id = 45077, value = "One phase confirm of participant {0} : {1} returned not expected vote {2}.", format = MESSAGE_FORMAT)
    @LogMessage(level = ERROR)
    public void error_wst_at_participants_Durable2PC_one_phase_wrong_vote(String id, Durable2PCParticipant participant, String vote);

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