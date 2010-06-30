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
package com.arjuna.mw.wscf.logging;

import com.arjuna.ats.arjuna.common.Uid;
import org.jboss.logging.*;
import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the wscf module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface wscfI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

    @Message(id = 44001, value = "WSCF Initialisation: init failed", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_mw_wsc_deploy_WSCFI_1(Throwable arg0);

	@Message(id = 44002, value = "WSCF11 Initialisation: init failed", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_mw_wsc11_deploy_WSCFI_1(Throwable arg0);

	@Message(id = 44003, value = "Failed to create {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_CMF_1(String arg0);

	@Message(id = 44004, value = "Failed to create {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_UCF_1(String arg0);

	@Message(id = 44005, value = "Failed to create {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_CMF_1(String arg0);

	@Message(id = 44006, value = "Failed to create {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_UCF_1(String arg0);

	@Message(id = 44007, value = "Could not find protocol:", format = MESSAGE_FORMAT)
	public String get_protocols_ProtocolManager_1();

	@Message(id = 44008, value = "Failed to find document:", format = MESSAGE_FORMAT)
	public String get_protocols_ProtocolManager_2();

	@Message(id = 44009, value = "Failed to create {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wscf11_model_sagas_UCF_1(String arg0);

	@Message(id = 44010, value = "Failed to create {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wscf11_model_sagas_CMF_1(String arg0);

	@Message(id = 44011, value = "Failed to create {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wscf11_model_twophase_CMF_1(String arg0);

	@Message(id = 44012, value = "Failed to create {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_mw_wscf11_model_twophase_UCF_1(String arg0);

	@Message(id = 44013, value = "Could not find protocol:", format = MESSAGE_FORMAT)
	public String get_mw_wscf11_protocols_ProtocolManager_1();

	@Message(id = 44014, value = "Failed to find document:", format = MESSAGE_FORMAT)
	public String get_mw_wscf11_protocols_ProtocolManager_2();

	@Message(id = 44015, value = "Participant failed to complete in activity {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_arjunacore_BACoordinator_1(Uid arg0);

	@Message(id = 44016, value = "Null is an invalid parameter.", format = MESSAGE_FORMAT)
	public String get_model_sagas_arjunacore_BACoordinator_2();

	@Message(id = 44017, value = "Wrong state for operation!", format = MESSAGE_FORMAT)
	public String get_model_sagas_arjunacore_BACoordinator_3();

	@Message(id = 44018, value = "Removal of business activity synchronization is not allowed", format = MESSAGE_FORMAT)
	public String get_model_sagas_arjunacore_BACoordinator_4();

	@Message(id = 44019, value = "Unknown response!", format = MESSAGE_FORMAT)
	public String get_model_sagas_arjunacore_CoordinatorServiceImple_1();

	@Message(id = 44020, value = "CoordinatorControl.begin:", format = MESSAGE_FORMAT)
	public String get_model_sagas_arjunacore_CoordinatorControl_1();

	@Message(id = 44021, value = "SynchronizationRecord {0} - null participant provided!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_arjunacore_SynchronizationRecord_1(String arg0);

	@Message(id = 44022, value = "ParticipantRecord {0} - null participant provided!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_1(Uid arg0);

	@Message(id = 44023, value = "ParticipantRecord.topLevelOnePhaseCommit {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_10(Uid arg0, Throwable arg1);

	@Message(id = 44024, value = "ParticipantRecord.forgetHeuristic for {0} called without a resource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_11(Uid arg0);

	@Message(id = 44025, value = "ParticipantRecord.forgetHeuristic {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_12(Uid arg0, Throwable arg1);

	@Message(id = 44026, value = "ParticipantRecord.complete {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_13(Uid arg0, Throwable arg1);

	@Message(id = 44027, value = "ParticipantRecord.restore_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_14(Throwable arg0);

	@Message(id = 44028, value = "ParticipantRecord.save_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_15(Throwable arg0);

	@Message(id = 44029, value = "ParticipantRecord.setValue() called illegally.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_2();

	@Message(id = 44030, value = "ParticipantRecord.nestedAbort {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_3(Uid arg0, Throwable arg1);

	@Message(id = 44031, value = "ParticipantRecord.nestedCommit {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_4(Uid arg0, Throwable arg1);

	@Message(id = 44032, value = "ParticipantRecord.nestedPrepare {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_5(Uid arg0, Throwable arg1);

	@Message(id = 44033, value = "ParticipantRecord.topLevelAbort {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_6(Uid arg0, Throwable arg1);

	@Message(id = 44034, value = "ParticipantRecord.topLevelCommit {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_7(Uid arg0, Throwable arg1);

	@Message(id = 44035, value = "ParticipantRecord.topLevelPrepare {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_8(Uid arg0, Throwable arg1);

	@Message(id = 44036, value = "ParticipantRecord.nestedOnePhaseCommit {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_sagas_coordinator_arjunacore_ParticipantRecord_9(Uid arg0, Throwable arg1);

	@Message(id = 44037, value = "ArjunaCore does not support removal of participants", format = MESSAGE_FORMAT)
	public String get_model_twophase_arjunacore_ATCoordinator_1();

	@Message(id = 44038, value = "Null is an invalid parameter!", format = MESSAGE_FORMAT)
	public String get_model_twophase_arjunacore_ATCoordinator_2();

	@Message(id = 44039, value = "Wrong state for operation!", format = MESSAGE_FORMAT)
	public String get_model_twophase_arjunacore_ATCoordinator_3();

	@Message(id = 44040, value = "Unknown response!", format = MESSAGE_FORMAT)
	public String get_model_twophase_arjunacore_CoordinatorServiceImple_1();

	@Message(id = 44041, value = "CoordinatorControl.begin:", format = MESSAGE_FORMAT)
	public String get_model_twophase_arjunacore_CoordinatorControl_1();

	@Message(id = 44042, value = "ParticipantRecord {0} - null participant provided!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_1(Uid arg0);

	@Message(id = 44043, value = "ParticipantRecord.topLevelOnePhaseCommit {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_10(Uid arg0, Throwable arg1);

	@Message(id = 44044, value = "ParticipantRecord.forgetHeuristic for {0} called without a resource!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_11(Uid arg0);

	@Message(id = 44045, value = "ParticipantRecord.forgetHeuristic {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_12(Uid arg0, Throwable arg1);

	@Message(id = 44046, value = "ParticipantRecord.restore_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_13(Throwable arg0);

	@Message(id = 44047, value = "ParticipantRecord.save_state caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_14(Throwable arg0);

	@Message(id = 44048, value = "ParticipantRecord.setValue() called illegally.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_2();

	@Message(id = 44049, value = "ParticipantRecord.nestedAbort {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_3(Uid arg0, Throwable arg1);

	@Message(id = 44050, value = "ParticipantRecord.nestedCommit {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_4(Uid arg0, Throwable arg1);

	@Message(id = 44051, value = "ParticipantRecord.nestedPrepare {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_5(Uid arg0, Throwable arg1);

	@Message(id = 44052, value = "ParticipantRecord.topLevelAbort {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_6(Uid arg0, Throwable arg1);

	@Message(id = 44053, value = "ParticipantRecord.topLevelCommit {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_7(Uid arg0, Throwable arg1);

	@Message(id = 44054, value = "ParticipantRecord.topLevelPrepare {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_8(Uid arg0, Throwable arg1);

	@Message(id = 44055, value = "ParticipantRecord.nestedOnePhaseCommit {0} caught exception", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_ParticipantRecord_9(Uid arg0, Throwable arg1);

	@Message(id = 44056, value = "SynchronizationRecord {0} - null participant provided!", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_model_twophase_arjunacore_SynchronizationRecord_1(Uid arg0);

	@Message(id = 44057, value = "Failed to create:", format = MESSAGE_FORMAT)
	public String get_protocols_Initializer_1();

	@Message(id = 44058, value = "First parameter is null!", format = MESSAGE_FORMAT)
	public String get_utils_DocComparitor_1();

	@Message(id = 44059, value = "Second parameter is null!", format = MESSAGE_FORMAT)
	public String get_utils_DocComparitor_2();

	@Message(id = 44060, value = "not found", format = MESSAGE_FORMAT)
	public String get_utils_ProtocolLocator_1();

	@Message(id = 44061, value = "Failed to create:", format = MESSAGE_FORMAT)
	public String get_utils_ProtocolLocator_2();

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
