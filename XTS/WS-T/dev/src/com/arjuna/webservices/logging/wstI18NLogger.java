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
package com.arjuna.webservices.logging;

import org.jboss.logging.*;

import javax.xml.namespace.QName;

import static org.jboss.logging.Logger.Level.*;
import static org.jboss.logging.Message.Format.*;

/**
 * i18n log messages for the wst module.
 *
 * @author Jonathan Halliday (jonathan.halliday@redhat.com) 2010-06
 */
@MessageLogger(projectCode = "ARJUNA")
public interface wstI18NLogger {

    /*
        Message IDs are unique and non-recyclable.
        Don't change the purpose of existing messages.
          (tweak the message text or params for clarification if you like).
        Allocate new messages by following instructions at the bottom of the file.
     */

	@Message(id = 43001, value = "Invalid outcome enumeration: {0}", format = MESSAGE_FORMAT)
	public String get_webservices_wsat_Outcome_1(String arg0);

	@Message(id = 43002, value = "PrepareResponse elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	public String get_webservices_wsat_PrepareResponseType_1();

	@Message(id = 43003, value = "ReplayResponse elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	public String get_webservices_wsat_ReplayResponseType_1();

	@Message(id = 43004, value = "Invalid fault type enumeration: {0}", format = MESSAGE_FORMAT)
	public String get_webservices_wsat_State_1(String arg0);

	@Message(id = 43005, value = "Invalid vote enumeration: {0}", format = MESSAGE_FORMAT)
	public String get_webservices_wsat_Vote_1(String arg0);

	@Message(id = 43006, value = "State elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	public String get_webservices_wsba_StateType_1();

	@Message(id = 43007, value = "Invalid state enumeration: {0}", format = MESSAGE_FORMAT)
	public String get_webservices_wsba_State_1(QName arg0);

	@Message(id = 43008, value = "Invalid soap fault type", format = MESSAGE_FORMAT)
	public String get_webservices11_wsarjtx_sei_TerminationParticipantPortTypeImpl_1();

	@Message(id = 43009, value = "Invalid fault type enumeration: {0}", format = MESSAGE_FORMAT)
	public String get_webservices11_wsat_State_1(String arg0);

	@Message(id = 43010, value = "Invalid state enumeration: {0}", format = MESSAGE_FORMAT)
	public String get_webservices11_wsba_State_1(QName arg0);

	@Message(id = 43011, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_messaging_CompletionCoordinatorProcessorImpl_1();

	@Message(id = 43012, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_messaging_CompletionCoordinatorProcessorImpl_10();

	@Message(id = 43013, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_messaging_CompletionCoordinatorProcessorImpl_2();

	@Message(id = 43014, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_messaging_CompletionCoordinatorProcessorImpl_5();

	@Message(id = 43015, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_messaging_CompletionCoordinatorProcessorImpl_6();

	@Message(id = 43016, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_messaging_CompletionCoordinatorProcessorImpl_7();

	@Message(id = 43017, value = "Unexpected exception while sending InvalidStateFault to participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43018, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	public String get_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_4();

//	@Message(id = 43019, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendFaulted_1();

	@Message(id = 43020, value = "Complete called on unknown participant", format = MESSAGE_FORMAT)
	public String get_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_3();

	@Message(id = 43021, value = "Unexpected exception while sending InvalidStateFault to coordinator for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43022, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	public String get_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_4();

	@Message(id = 43023, value = "Unexpected exception thrown from aborted:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_aborted_1(@Cause() Throwable arg0);

	@Message(id = 43024, value = "Aborted called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_aborted_2(String arg0);

	@Message(id = 43025, value = "Unexpected exception thrown from committed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_committed_1(@Cause() Throwable arg0);

	@Message(id = 43026, value = "Committed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_committed_2(String arg0);

	@Message(id = 43027, value = "Unexpected exception thrown from prepared", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_prepared_1(@Cause() Throwable arg0);

	@Message(id = 43028, value = "Prepared called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_prepared_2(String arg0);

	@Message(id = 43029, value = "Ignoring prepared called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_prepared_3(String arg0);

	@Message(id = 43030, value = "Unexpected exception thrown from readOnly", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_readOnly_1(@Cause() Throwable arg0);

	@Message(id = 43031, value = "ReadOnly called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_readOnly_2(String arg0);

	@Message(id = 43032, value = "Unexpected exception thrown from replay", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_replay_1(@Cause() Throwable arg0);

	@Message(id = 43033, value = "Replay called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_replay_2(String arg0);

	@Message(id = 43034, value = "Unknown Transaction.", format = MESSAGE_FORMAT)
	public String get_messaging_CoordinatorProcessorImpl_sendInvalidState_1();

	@Message(id = 43035, value = "Unexpected exception thrown from soapFault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_soapFault_1(@Cause() Throwable arg0);

	@Message(id = 43036, value = "SoapFault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_soapFault_2(String arg0);

//	@Message(id = 43037, value = "Ignoring completed called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_completed_3(String arg0);

	@Message(id = 43038, value = "Unexpected exception while sending InvalidStateFault to participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43039, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	public String get_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_4();

//	@Message(id = 43040, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_sendFaulted_1();

	@Message(id = 43041, value = "Unexpected exception while sending InvalidStateFault to coordinator for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43042, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	public String get_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_4();

	@Message(id = 43043, value = "Unexpected exception thrown from commit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_1(@Cause() Throwable arg0);

	@Message(id = 43044, value = "Commit called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_2(String arg0);

	@Message(id = 43045, value = "Commit request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_3(String arg0);

	@Message(id = 43046, value = "Commit request dropped pending WS-AT participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_4(String arg0);

	@Message(id = 43047, value = "Commit request dropped pending registration of application-specific recovery module for WS-AT participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_5(String arg0);

	@Message(id = 43048, value = "Unexpected exception thrown from prepare", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_prepare_1(@Cause() Throwable arg0);

	@Message(id = 43049, value = "Prepare called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_prepare_2(String arg0);

//	@Message(id = 43050, value = "Prepare request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_ParticipantProcessorImpl_prepare_3(String arg0);

	@Message(id = 43051, value = "Unexpected exception thrown from rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_1(@Cause() Throwable arg0);

	@Message(id = 43052, value = "Rollback called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_2(String arg0);

	@Message(id = 43053, value = "Rollback request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_3(String arg0);

	@Message(id = 43054, value = "Rollback request dropped pending WS-AT participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_4(String arg0);

	@Message(id = 43055, value = "Rollback request dropped pending registration of application-specific recovery module for WS-AT participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_5(String arg0);

	@Message(id = 43056, value = "Unexpected exception thrown from soapFault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_soapFault_1(@Cause() Throwable arg0);

	@Message(id = 43057, value = "SoapFault called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43058, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_1();

	@Message(id = 43059, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_11();

	@Message(id = 43060, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_12();

	@Message(id = 43061, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_13();

	@Message(id = 43062, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_16();

	@Message(id = 43063, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_2();

	@Message(id = 43064, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_5();

	@Message(id = 43065, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_6();

	@Message(id = 43066, value = "Transaction rolled back", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_7();

	@Message(id = 43067, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_messaging_TerminatorParticipantProcessorImpl_8();

//	@Message(id = 43068, value = "Unable to write recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_completed_1(String arg0);

//	@Message(id = 43069, value = "Unable to delete recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_completed_2(String arg0);

//	@Message(id = 43070, value = "Unable to delete recovery record during close for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeClose_2(String arg0);

	@Message(id = 43071, value = "Faulted exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_1(String arg0, @Cause() Throwable arg1);

//	@Message(id = 43072, value = "Unable to delete recovery record during compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_3(String arg0);

	@Message(id = 43073, value = "Unable to write log record during participant complete for WS-BA  parfticipant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeComplete_2(String arg0);

	@Message(id = 43074, value = "Unable to delete recovery record during faulted for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_faulted_1(String arg0);

//	@Message(id = 43075, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_getStatus_1(String arg0);

	@Message(id = 43076, value = "Unable to delete recovery record during soapFault processing for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_1(String arg0);

	@Message(id = 43077, value = "Compensating participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_2(String arg0);

	@Message(id = 43078, value = "Notifying unexpected error for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43079, value = "Inconsistent internal state.", format = MESSAGE_FORMAT)
	public String get_messaging_engines_CoordinatorEngine_sendInvalidState_1();

	@Message(id = 43080, value = "Unable to write recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_completed_1(String arg0);

	@Message(id = 43081, value = "Unable to delete recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_completed_2(String arg0);

	@Message(id = 43082, value = "Unable to delete recovery record during close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeClose_2(String arg0);

	@Message(id = 43083, value = "Faulted exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_1(String arg0, @Cause() Throwable arg1);

//	@Message(id = 43084, value = "Unexpected exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_2(String arg0);

	@Message(id = 43085, value = "Unable to delete recovery record during compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_3(String arg0);

	@Message(id = 43086, value = "Unable to delete recovery record during faulted for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_faulted_1(String arg0);

//	@Message(id = 43087, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_getStatus_1(String arg0);

	@Message(id = 43088, value = "Unable to delete recovery record during soapFault processing for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_1(String arg0);

	@Message(id = 43089, value = "Cancelling participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_2(String arg0);

	@Message(id = 43090, value = "Notifying unexpected error for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43091, value = "Unable to delete recovery record during prepare for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_commitDecision_2(String arg0);

	@Message(id = 43092, value = "Unable to delete recovery record at commit for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_commitDecision_3(String arg0);

//	@Message(id = 43093, value = "Unexpected result from participant prepare: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_messaging_engines_ParticipantEngine_executePrepare_2(String arg0);

	@Message(id = 43094, value = "could not delete recovery record for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_rollback_1(String arg0);

	@Message(id = 43095, value = "Unrecoverable error for participant {0} : {1} {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_messaging_engines_ParticipantEngine_soapFault_2(String arg0, String arg1, QName arg2);

	@Message(id = 43096, value = "Unable to delete recovery record at commit for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_messaging_engines_ParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43097, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_stub_BusinessActivityTerminatorStub_1();

	@Message(id = 43098, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_stub_BusinessAgreementWithCoordinatorCompletionStub_2(@Cause() Throwable arg0);

	@Message(id = 43099, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_stub_BusinessAgreementWithCoordinatorCompletionStub_3(@Cause() Throwable arg0);

	@Message(id = 43100, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_BusinessAgreementWithParticipantCompletionStub_2(@Cause() Throwable arg0);

	@Message(id = 43101, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_stub_BusinessAgreementWithParticipantCompletionStub_3(@Cause() Throwable arg0);

	@Message(id = 43102, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_ParticipantStub_1(@Cause() Throwable arg0);

	@Message(id = 43103, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_ParticipantStub_2(@Cause() Throwable arg0);

	@Message(id = 43104, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CompletionCoordinatorProcessorImpl_1();

	@Message(id = 43105, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CompletionCoordinatorProcessorImpl_10();

	@Message(id = 43106, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CompletionCoordinatorProcessorImpl_2();

	@Message(id = 43107, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CompletionCoordinatorProcessorImpl_5();

	@Message(id = 43108, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CompletionCoordinatorProcessorImpl_6();

	@Message(id = 43109, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CompletionCoordinatorProcessorImpl_7();

	@Message(id = 43110, value = "Unexpected exception while sending InvalidStateFault to participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43111, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_4();

	@Message(id = 43112, value = "Cancel request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_3(String arg0);

	@Message(id = 43113, value = "Cancel request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_4(String arg0);

	@Message(id = 43114, value = "Cancel request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_5(String arg0);

	@Message(id = 43115, value = "Close request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_close_3(String arg0);

	@Message(id = 43116, value = "Close request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_close_4(String arg0);

	@Message(id = 43117, value = "Close request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_close_5(String arg0);

	@Message(id = 43118, value = "Compensate request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_3(String arg0);

	@Message(id = 43119, value = "Compensate request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_4(String arg0);

	@Message(id = 43120, value = "Compensate request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_5(String arg0);

//	@Message(id = 43121, value = "Complete called on unknown participant", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_3();

	@Message(id = 43122, value = "Complete request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_4(String arg0);

	@Message(id = 43123, value = "Complete request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_5(String arg0);

	@Message(id = 43124, value = "Complete request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_6(String arg0);

//	@Message(id = 43125, value = "Unexpected exception thrown from failed:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_failed_1();

//	@Message(id = 43126, value = "Failed called on unknown participant: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_failed_2(String arg0);

	@Message(id = 43127, value = "Unexpected exception while sending InvalidStateFault to coordinator for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43128, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_4();

	@Message(id = 43129, value = "Unexpected exception thrown from aborted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_aborted_1(@Cause() Throwable arg0);

	@Message(id = 43130, value = "Aborted called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_aborted_2(String arg0);

	@Message(id = 43131, value = "Unexpected exception thrown from committed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_committed_1(@Cause() Throwable arg0);

	@Message(id = 43132, value = "Committed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_committed_2(String arg0);

	@Message(id = 43133, value = "Unexpected exception thrown from prepared", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_prepared_1(@Cause() Throwable arg0);

	@Message(id = 43134, value = "Prepared called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_prepared_2(String arg0);

	@Message(id = 43135, value = "Ignoring prepared called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_prepared_3(String arg0);

	@Message(id = 43136, value = "Unexpected exception thrown from readOnly", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_readOnly_1(@Cause() Throwable arg0);

	@Message(id = 43137, value = "ReadOnly called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_readOnly_2(String arg0);

	@Message(id = 43138, value = "Unknown Transaction.", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_CoordinatorProcessorImpl_sendUnknownTransaction_1();

	@Message(id = 43139, value = "Unexpected exception thrown from soapFault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_soapFault_1(@Cause() Throwable arg0);

	@Message(id = 43140, value = "SoapFault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_soapFault_2(String arg0);

//	@Message(id = 43141, value = "Unexpected exception thrown from cannot complete:", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_cannotComplete_1();

//	@Message(id = 43142, value = "Cannot complete called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_cannotComplete_2(String arg0);

	@Message(id = 43143, value = "Unexpected exception while sending InvalidStateFault to participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43144, value = "GetStatus requested for unknown participant completion participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_4();

	@Message(id = 43145, value = "Cancel request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_3(String arg0);

	@Message(id = 43146, value = "Cancel request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_4(String arg0);

	@Message(id = 43147, value = "Cancel request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_5(String arg0);

	@Message(id = 43148, value = "Close request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_close_3(String arg0);

	@Message(id = 43149, value = "Close request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_close_4(String arg0);

	@Message(id = 43150, value = "Close request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_close_5(String arg0);

	@Message(id = 43151, value = "Compensate request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_3(String arg0);

	@Message(id = 43152, value = "Compensate request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_4(String arg0);

	@Message(id = 43153, value = "Compensate request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_5(String arg0);

	@Message(id = 43154, value = "Unexpected exception while sending InvalidStateFault to coordinator for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = INFO)
	public void info_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43155, value = "GetStatus requested for unknown participant completion participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_4();

	@Message(id = 43156, value = "Unexpected exception thrown from commit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_1(@Cause() Throwable arg0);

	@Message(id = 43157, value = "Commit called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_2(String arg0);

	@Message(id = 43158, value = "Commit request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_3(String arg0);

	@Message(id = 43159, value = "Commit request dropped pending WS-AT participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_4(String arg0);

	@Message(id = 43160, value = "Commit request dropped pending registration of application-specific recovery module for WS-AT participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_5(String arg0);

	@Message(id = 43161, value = "Unexpected exception thrown from prepare", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_prepare_1(@Cause() Throwable arg0);

	@Message(id = 43162, value = "Prepare called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_prepare_2(String arg0);

	@Message(id = 43163, value = "Unexpected exception thrown from rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_1(@Cause() Throwable arg0);

	@Message(id = 43164, value = "Rollback called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_2(String arg0);

	@Message(id = 43165, value = "Rollback request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_3(String arg0);

	@Message(id = 43166, value = "Rollback request dropped pending WS-AT participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_4(String arg0);

	@Message(id = 43167, value = "Rollback request dropped pending registration of application-specific recovery module for WS-AT participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_5(String arg0);

	@Message(id = 43168, value = "Unexpected exception thrown from soapFault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_soapFault_1(@Cause() Throwable arg0);

	@Message(id = 43169, value = "SoapFault called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43170, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_1();

	@Message(id = 43171, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_11();

	@Message(id = 43172, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_12();

	@Message(id = 43173, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_13();

	@Message(id = 43174, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_16();

	@Message(id = 43175, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_2();

	@Message(id = 43176, value = "Unknown participant", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_5();

	@Message(id = 43177, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_6();

	@Message(id = 43178, value = "Transaction rolled back", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_7();

	@Message(id = 43179, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_TerminationCoordinatorProcessorImpl_8();

	@Message(id = 43180, value = "Invalid coordinator completion coordinator state", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendInvalidStateFault_2();

//	@Message(id = 43181, value = "Unable to write recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_completed_1(String arg0);

//	@Message(id = 43182, value = "Unable to delete recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_completed_2(String arg0);

//	@Message(id = 43183, value = "Unable to delete recovery record during close for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeClose_2(String arg0);

//	@Message(id = 43184, value = "Unable to delete recovery record during compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_3(String arg0);

	@Message(id = 43185, value = "Unable to write log record during participant complete for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeComplete_2(String arg0);

	@Message(id = 43186, value = "Unable to delete recovery record during failed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_failed_1(String arg0);

//	@Message(id = 43187, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_getStatus_1(String arg0);

	@Message(id = 43188, value = "Unable to delete recovery record during soapFault processing for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_1(String arg0);

	@Message(id = 43189, value = "Cancelling participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_2(String arg0);

	@Message(id = 43190, value = "Notifying unexpected error for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43191, value = "Unknown transaction", format = MESSAGE_FORMAT)
	public String get_wst11_messaging_engines_CoordinatorEngine_sendUnknownTransaction_1();

	@Message(id = 43192, value = "Unexpected exception while sending UnknownTransaction for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorEngine_sendUnknownTransaction_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 43193, value = "Unable to write recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_completed_1(String arg0);

	@Message(id = 43194, value = "Unable to delete recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_completed_2(String arg0);

	@Message(id = 43195, value = "Unable to delete recovery record during close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeClose_2(String arg0);

	@Message(id = 43196, value = "Unable to delete recovery record during compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_3(String arg0);

	@Message(id = 43197, value = "Unable to delete recovery record during failed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_failed_1(String arg0);

//	@Message(id = 43198, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_getStatus_1(String arg0);

	@Message(id = 43199, value = "Unable to delete recovery record during soapFault processing for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_1(String arg0);

	@Message(id = 43200, value = "Compensating participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_2(String arg0);

	@Message(id = 43201, value = "Notifying unexpected error for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_3(String arg0);

//	@Message(id = 43202, value = "Exception rolling back participant", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_engines_ParticipantEngine_commitDecision_1();

	@Message(id = 43203, value = "Unable to delete recovery record during prepare for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_commitDecision_2(String arg0);

	@Message(id = 43204, value = "Unable to delete recovery record at commit for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_commitDecision_3(String arg0);

//	@Message(id = 43205, value = "Unexpected result from participant prepare: {0}", format = MESSAGE_FORMAT)
//	@LogMessage(level = WARN)
//	public void warn_wst11_messaging_engines_ParticipantEngine_executePrepare_2(String arg0);

	@Message(id = 43206, value = "could not delete recovery record for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_rollback_1(String arg0);

	@Message(id = 43207, value = "Unrecoverable error for participant {0} : {1} {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_wst11_messaging_engines_ParticipantEngine_soapFault_2(String arg0, String arg1, QName arg2);

	@Message(id = 43208, value = "Unable to delete recovery record at commit for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_wst11_messaging_engines_ParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43209, value = "Unknown error", format = MESSAGE_FORMAT)
	public String get_wst11_stub_BusinessActivityTerminatorStub_1();

	@Message(id = 43210, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_wst11_stub_BusinessAgreementWithCoordinatorCompletionStub_2(@Cause() Throwable arg0);

	@Message(id = 43211, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_wst11_stub_BusinessAgreementWithCoordinatorCompletionStub_3(@Cause() Throwable arg0);

	@Message(id = 43212, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_wst11_stub_BusinessAgreementWithParticipantCompletionStub_2(@Cause() Throwable arg0);

	@Message(id = 43213, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_wst11_stub_BusinessAgreementWithParticipantCompletionStub_3(@Cause() Throwable arg0);

	@Message(id = 43214, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_wst11_stub_ParticipantStub_1(@Cause() Throwable arg0);

	@Message(id = 43215, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = ERROR)
	public void error_wst11_stub_ParticipantStub_2(@Cause() Throwable arg0);

	@Message(id = 43216, value = "participant {0} has no saved recovery state to recover", format = MESSAGE_FORMAT)
	public String get_recovery_participant_at_ATParticipantRecoveryRecord_restoreParticipant_1(String arg0);

	@Message(id = 43217, value = "XML stream exception restoring recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_at_ATParticipantRecoveryRecord_restoreState_1(String arg0, @Cause() Throwable arg1);

	@Message(id = 43218, value = "I/O exception saving restoring state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_at_ATParticipantRecoveryRecord_restoreState_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 43219, value = "Could not save recovery state for non-serializable durable WS-AT participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_at_ATParticipantRecoveryRecord_saveState_1(String arg0);

	@Message(id = 43220, value = "XML stream exception saving recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_at_ATParticipantRecoveryRecord_saveState_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 43221, value = "I/O exception saving recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_at_ATParticipantRecoveryRecord_saveState_3(String arg0, @Cause() Throwable arg1);

	@Message(id = 43222, value = "participant {0} has no saved recovery state to recover", format = MESSAGE_FORMAT)
	public String get_recovery_participant_ba_BAParticipantRecoveryRecord_restoreParticipant_1(String arg0);

	@Message(id = 43223, value = "XML stream exception restoring recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_ba_BAParticipantRecoveryRecord_restoreState_1(String arg0, @Cause() Throwable arg1);

	@Message(id = 43224, value = "I/O exception saving restoring state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_ba_BAParticipantRecoveryRecord_restoreState_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 43225, value = "Could not save recovery state for non-serializable WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_ba_BAParticipantRecoveryRecord_saveState_1(String arg0);

	@Message(id = 43226, value = "XML stream exception saving recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_ba_BAParticipantRecoveryRecord_saveState_2(String arg0, @Cause() Throwable arg1);

	@Message(id = 43227, value = "I/O exception saving recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_recovery_participant_ba_BAParticipantRecoveryRecord_saveState_3(String arg0, @Cause() Throwable arg1);

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
