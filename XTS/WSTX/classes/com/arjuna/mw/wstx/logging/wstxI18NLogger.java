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
package com.arjuna.mw.wstx.logging;


import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

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

    @Message(id = 45001, value = "Error in:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_client_JaxHCP_1();

	@Message(id = 45002, value = "Stack trace:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_client_JaxHCP_2();

	@Message(id = 45003, value = "Unknown context type:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_client_JaxHCP_3();

	@Message(id = 45004, value = "WSTX Initialisation: init failed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_deploy_WSTXI_1();

	@Message(id = 45005, value = "{0} not found.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_deploy_WSTXI_21(String arg0);

	@Message(id = 45006, value = "Failed to create document: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_deploy_WSTXI_22(String arg0);

	@Message(id = 45007, value = "Missing WSTX Initialisation", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_deploy_WSTXI_23();

	@Message(id = 45008, value = "Error in:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_service_JaxHCP_1();

	@Message(id = 45009, value = "Stack trace:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_service_JaxHCP_2();

	@Message(id = 45010, value = "Unknown context type:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst_service_JaxHCP_3();

	@Message(id = 45011, value = "Error in:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_client_JaxHC11P_1();

	@Message(id = 45012, value = "Stack trace:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_client_JaxHC11P_2();

	@Message(id = 45013, value = "Unknown context type:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_client_JaxHC11P_3();

	@Message(id = 45014, value = "WSTX11 Initialisation: init failed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_deploy_WSTXI_1();

	@Message(id = 45015, value = "{0} not found.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_deploy_WSTXI_21(String arg0);

	@Message(id = 45016, value = "Failed to create document: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_deploy_WSTXI_22(String arg0);

	@Message(id = 45017, value = "Missing WSTX Initialisation", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_deploy_WSTXI_23();

	@Message(id = 45018, value = "Error in:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_service_JaxHC11P_1();

	@Message(id = 45019, value = "Stack trace:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_service_JaxHC11P_2();

	@Message(id = 45020, value = "Unknown context type:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wst11_service_JaxHC11P_3();

	@Message(id = 45021, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_Context11FactoryImple_1(String arg0, String arg1);

	@Message(id = 45022, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_Context11FactoryImple_3();

	@Message(id = 45023, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_ContextFactoryImple_1(String arg0, String arg1);

	@Message(id = 45024, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_ContextFactoryImple_3();

	@Message(id = 45025, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_Registrar11Imple_1(String arg0, String arg1);

	@Message(id = 45026, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_RegistrarImple_1(String arg0, String arg1);

	@Message(id = 45027, value = "ignoring context:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_context_ArjunaContextImple_1();

	@Message(id = 45028, value = "One context was null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_local_ContextManager_1();

	@Message(id = 45029, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_local_LocalContextFactoryImple_1(String arg0, String arg1);

	@Message(id = 45030, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_local_LocalContextFactoryImple_11();

	@Message(id = 45031, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_local_LocalRegistrarImple_1(String arg0, String arg1);

	@Message(id = 45032, value = "Not implemented!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_local_TransactionManagerImple_1();

	@Message(id = 45033, value = "comms timeout attempting to cancel WS-AT participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_cancel_1(String arg0);

	@Message(id = 45034, value = "comms timeout attempting to commit WS-AT participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_confirm_1(String arg0);

	@Message(id = 45035, value = "comms timeout attempting to prepare WS-AT participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_participants_DurableTwoPhaseCommitParticipant_prepare_1(String arg0);

	@Message(id = 45036, value = "Not implemented!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_remote_Transaction11ManagerImple_1();

	@Message(id = 45037, value = "Not implemented!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_remote_TransactionManagerImple_1();

	@Message(id = 45038, value = "Received context is null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_remote_UserTransaction11Imple__2();

	@Message(id = 45039, value = "Received context is null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_at_remote_UserTransactionImple_2();

	@Message(id = 45040, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_Context11FactoryImple_1(String arg0, String arg1);

	@Message(id = 45041, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_Context11FactoryImple_3();

	@Message(id = 45042, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_ContextFactoryImple_1(String arg0, String arg1);

	@Message(id = 45043, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_ContextFactoryImple_3();

	@Message(id = 45044, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_LocalContextFactoryImple_1(String arg0, String arg1);

	@Message(id = 45045, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_Registrar11Imple_1(String arg0, String arg1);

	@Message(id = 45046, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_RegistrarImple_1(String arg0, String arg1);

	@Message(id = 45047, value = "ignoring context:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_context_ArjunaContextImple_1();

	@Message(id = 45048, value = "One context was null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_local_ContextManager_1();

	@Message(id = 45049, value = "Invalid type URI:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_local_LocalContextFactoryImple_11();

	@Message(id = 45050, value = "Invalid type URI: < {0} , {1} >", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_local_LocalRegistrarImple_1(String arg0, String arg1);

	@Message(id = 45051, value = "Invalid address.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_remote_UserBusinessActivityImple_1();

	@Message(id = 45052, value = "Received context is null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_remote_UserBusinessActivityImple_2();

	@Message(id = 45053, value = "No termination context!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_ba_remote_UserBusinessActivityImple_3();

	@Message(id = 45054, value = "Participant not persistable.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_util_PersistableParticipantHelper_1();

	@Message(id = 45055, value = "Error persisting participant.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_util_PersistableParticipantHelper_2();

	@Message(id = 45056, value = "Error restoring participant.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst_util_PersistableParticipantHelper_3();

	@Message(id = 45057, value = "ignoring context:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst11_at_context_ArjunaContextImple_1();

	@Message(id = 45058, value = "ignoring context:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst11_ba_context_ArjunaContextImple_1();

	@Message(id = 45059, value = "Invalid address.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst11_ba_remote_UserBusinessActivityImple_1();

	@Message(id = 45060, value = "Received context is null!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst11_ba_remote_UserBusinessActivityImple_2();

	@Message(id = 45061, value = "No termination context!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mwlabs_wst11_ba_remote_UserBusinessActivityImple_3();

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
