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
	@LogMessage(level = WARN)
	public void warn_webservices_wsat_Outcome_1(String arg0);

	@Message(id = 43002, value = "PrepareResponse elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsat_PrepareResponseType_1();

	@Message(id = 43003, value = "ReplayResponse elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsat_ReplayResponseType_1();

	@Message(id = 43004, value = "Invalid fault type enumeration: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsat_State_1(String arg0);

	@Message(id = 43005, value = "Invalid vote enumeration: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsat_Vote_1(String arg0);

	@Message(id = 43006, value = "State elements cannot have embedded elements.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsba_StateType_1();

	@Message(id = 43007, value = "Invalid state enumeration: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices_wsba_State_1(String arg0);

	@Message(id = 43008, value = "Invalid soap fault type", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices11_wsarjtx_sei_TerminationParticipantPortTypeImpl_1();

	@Message(id = 43009, value = "Invalid fault type enumeration: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices11_wsat_State_1(String arg0);

	@Message(id = 43010, value = "Invalid state enumeration: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_webservices11_wsba_State_1(String arg0);

	@Message(id = 43011, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_1();

	@Message(id = 43012, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_10();

	@Message(id = 43013, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_2(String arg0);

	@Message(id = 43014, value = "Unexpected exception thrown from commit:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_3();

	@Message(id = 43015, value = "Commit called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_4(String arg0);

	@Message(id = 43016, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_5();

	@Message(id = 43017, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_6();

	@Message(id = 43018, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_7(String arg0);

	@Message(id = 43019, value = "Unexpected exception thrown from rollback:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_8();

	@Message(id = 43020, value = "Rollback called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CompletionCoordinatorProcessorImpl_9(String arg0);

	@Message(id = 43021, value = "Unexpected exception thrown from cancelled:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_cancelled_1();

	@Message(id = 43022, value = "Cancelled called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_cancelled_2(String arg0);

	@Message(id = 43023, value = "Unexpected exception thrown from closed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_closed_1();

	@Message(id = 43024, value = "Closed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_closed_2(String arg0);

	@Message(id = 43025, value = "Unexpected exception thrown from compensated:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_compensated_1();

	@Message(id = 43026, value = "Compensated called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_compensated_2(String arg0);

	@Message(id = 43027, value = "Unexpected exception thrown from completed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_completed_1();

	@Message(id = 43028, value = "Completed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_completed_2(String arg0);

	@Message(id = 43029, value = "Ignoring completed called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_completed_3(String arg0);

	@Message(id = 43030, value = "Unexpected exception thrown from exit:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_exit_1();

	@Message(id = 43031, value = "Exit called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_exit_2(String arg0);

	@Message(id = 43032, value = "Unexpected exception thrown from fault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_fault_1();

	@Message(id = 43033, value = "Fault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_fault_2(String arg0);

	@Message(id = 43034, value = "Ignoring fault called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_fault_3(String arg0);

	@Message(id = 43035, value = "Unexpected exception thrown from getStatus:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_1();

	@Message(id = 43036, value = "GetStatus called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_2(String arg0);

	@Message(id = 43037, value = "Unexpected exception while sending InvalidStateFault to participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_3(String arg0);

	@Message(id = 43038, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_4();

	@Message(id = 43039, value = "GetStatus dropped for unknown coordinator completion participant {0} awaiting recovery scan completion", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_5(String arg0);

	@Message(id = 43040, value = "Unexpected exception while sending Cancelled", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendCancelled_1();

	@Message(id = 43041, value = "Unexpected exception while sending Closed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendClosed_1();

	@Message(id = 43042, value = "Unexpected exception while sending Compensated", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendCompensated_1();

	@Message(id = 43043, value = "Unexpected exception while sending Exited", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendExited_1();

	@Message(id = 43044, value = "Unexpected exception while sending Fail", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendFault_1();

	@Message(id = 43045, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendFaulted_1();

	@Message(id = 43046, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_soapFault_1();

	@Message(id = 43047, value = "SoapFault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43048, value = "Unexpected exception thrown from status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_status_1();

	@Message(id = 43049, value = "Status called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionCoordinatorProcessorImpl_status_2(String arg0);

	@Message(id = 43050, value = "Unexpected exception thrown from cancel:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_1();

	@Message(id = 43051, value = "Cancel called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_2(String arg0);

	@Message(id = 43052, value = "Unexpected exception thrown from close:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_close_1();

	@Message(id = 43053, value = "Close called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_close_2(String arg0);

	@Message(id = 43054, value = "Unexpected exception thrown from compensate:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_1();

	@Message(id = 43055, value = "Compensate called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_2(String arg0);

	@Message(id = 43056, value = "Unexpected exception thrown from complete:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_1();

	@Message(id = 43057, value = "Complete called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_2(String arg0);

	@Message(id = 43058, value = "Complete called on unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_3();

	@Message(id = 43059, value = "Unexpected exception thrown from exited:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_exited_1();

	@Message(id = 43060, value = "Exited called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_exited_2(String arg0);

	@Message(id = 43061, value = "Unexpected exception thrown from faulted:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_faulted_1();

	@Message(id = 43062, value = "Faulted called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_faulted_2(String arg0);

	@Message(id = 43063, value = "Unexpected exception thrown from getStatus:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_1();

	@Message(id = 43064, value = "GetStatus called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_2(String arg0);

	@Message(id = 43065, value = "Unexpected exception while sending InvalidStateFault to coordinator for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_3(String arg0);

	@Message(id = 43066, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_4();

	@Message(id = 43067, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_soapFault_1();

	@Message(id = 43068, value = "SoapFault called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43069, value = "Unexpected exception thrown from status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_status_1();

	@Message(id = 43070, value = "Status called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorCompletionParticipantProcessorImpl_status_2(String arg0);

	@Message(id = 43071, value = "Unexpected exception thrown from aborted:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_aborted_1();

	@Message(id = 43072, value = "Aborted called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_aborted_2(String arg0);

	@Message(id = 43073, value = "Unexpected exception thrown from committed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_committed_1();

	@Message(id = 43074, value = "Committed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_committed_2(String arg0);

	@Message(id = 43075, value = "Unexpected exception thrown from prepared:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_prepared_1();

	@Message(id = 43076, value = "Prepared called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_prepared_2(String arg0);

	@Message(id = 43077, value = "Ignoring prepared called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_prepared_3(String arg0);

	@Message(id = 43078, value = "Unexpected exception thrown from readOnly:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_readOnly_1();

	@Message(id = 43079, value = "ReadOnly called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_readOnly_2(String arg0);

	@Message(id = 43080, value = "Unexpected exception thrown from replay:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_replay_1();

	@Message(id = 43081, value = "Replay called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_replay_2(String arg0);

	@Message(id = 43082, value = "Unknown Transaction.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_sendInvalidState_1();

	@Message(id = 43083, value = "Unexpecting exception while sending InvalidState", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_sendInvalidState_2();

	@Message(id = 43084, value = "Unexpected exception while sending Rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_sendRollback_1();

	@Message(id = 43085, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_soapFault_1();

	@Message(id = 43086, value = "SoapFault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_CoordinatorProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43087, value = "Unexpected exception thrown from cancelled:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_cancelled_1();

	@Message(id = 43088, value = "Cancelled called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_cancelled_2(String arg0);

	@Message(id = 43089, value = "Unexpected exception thrown from closed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_closed_1();

	@Message(id = 43090, value = "Closed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_closed_2(String arg0);

	@Message(id = 43091, value = "Unexpected exception thrown from compensated:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_compensated_1();

	@Message(id = 43092, value = "Compensated called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_compensated_2(String arg0);

	@Message(id = 43093, value = "Unexpected exception thrown from completed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_completed_1();

	@Message(id = 43094, value = "Completed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_completed_2(String arg0);

	@Message(id = 43095, value = "Ignoring completed called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_completed_3(String arg0);

	@Message(id = 43096, value = "Unexpected exception thrown from exit:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_exit_1();

	@Message(id = 43097, value = "Exit called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_exit_2(String arg0);

	@Message(id = 43098, value = "Unexpected exception thrown from fault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_fault_1();

	@Message(id = 43099, value = "Fault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_fault_2(String arg0);

	@Message(id = 43100, value = "Ignoring fault called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_fault_3(String arg0);

	@Message(id = 43101, value = "Unexpected exception thrown from getStatus:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_1();

	@Message(id = 43102, value = "GetStatus called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_2(String arg0);

	@Message(id = 43103, value = "Unexpected exception while sending InvalidStateFault to participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_3(String arg0);

	@Message(id = 43104, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_4();

	@Message(id = 43105, value = "GetStatus dropped for unknown coordinator completion participant {0} awaiting recovery scan completion", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_5(String arg0);

	@Message(id = 43106, value = "Unexpected exception while sending Exited", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_sendExited_1();

	@Message(id = 43107, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_sendFaulted_1();

	@Message(id = 43108, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_soapFault_1();

	@Message(id = 43109, value = "SoapFault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43110, value = "Unexpected exception thrown from status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_status_1();

	@Message(id = 43111, value = "Status called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionCoordinatorProcessorImpl_status_2(String arg0);

	@Message(id = 43112, value = "Unexpected exception thrown from cancel:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_1();

	@Message(id = 43113, value = "Cancel called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_2(String arg0);

	@Message(id = 43114, value = "Unexpected exception thrown from close:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_close_1();

	@Message(id = 43115, value = "Close called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_close_2(String arg0);

	@Message(id = 43116, value = "Unexpected exception thrown from compensate:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_1();

	@Message(id = 43117, value = "Compensate called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_2(String arg0);

	@Message(id = 43118, value = "Unexpected exception thrown from exited:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_exited_1();

	@Message(id = 43119, value = "Exited called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_exited_2(String arg0);

	@Message(id = 43120, value = "Unexpected exception thrown from faulted:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_faulted_1();

	@Message(id = 43121, value = "Faulted called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_faulted_2(String arg0);

	@Message(id = 43122, value = "Unexpected exception thrown from getStatus:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_1();

	@Message(id = 43123, value = "Complete called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_2(String arg0);

	@Message(id = 43124, value = "Unexpected exception while sending InvalidStateFault to coordinator for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_3(String arg0);

	@Message(id = 43125, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_4();

	@Message(id = 43126, value = "Unexpected exception while sending Cancelled", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_sendCancelled_1();

	@Message(id = 43127, value = "Unexpected exception while sending Closed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_sendClosed_1();

	@Message(id = 43128, value = "Unexpected exception while sending Compensated", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_sendCompensated_1();

	@Message(id = 43129, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_soapFault_1();

	@Message(id = 43130, value = "SoapFault called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43131, value = "Unexpected exception thrown from status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_status_1();

	@Message(id = 43132, value = "Status called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantCompletionParticipantProcessorImpl_status_2(String arg0);

	@Message(id = 43133, value = "Unexpected exception thrown from commit:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_1();

	@Message(id = 43134, value = "Commit called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_2(String arg0);

	@Message(id = 43135, value = "Commit request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_3(String arg0);

	@Message(id = 43136, value = "Commit request dropped pending WS-AT participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_4(String arg0);

	@Message(id = 43137, value = "Commit request dropped pending registration of application-specific recovery module for WS-AT participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_commit_5(String arg0);

	@Message(id = 43138, value = "Unexpected exception thrown from prepare:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_prepare_1();

	@Message(id = 43139, value = "Prepare called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_prepare_2(String arg0);

	@Message(id = 43140, value = "Prepare request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_prepare_3(String arg0);

	@Message(id = 43141, value = "Unexpected exception thrown from rollback:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_1();

	@Message(id = 43142, value = "Rollback called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_2(String arg0);

	@Message(id = 43143, value = "Rollback request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_3(String arg0);

	@Message(id = 43144, value = "Rollback request dropped pending WS-AT participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_4(String arg0);

	@Message(id = 43145, value = "Rollback request dropped pending registration of application-specific recovery module for WS-AT participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_rollback_5(String arg0);

	@Message(id = 43146, value = "Unexpected exception while sending Aborted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_sendAborted_1();

	@Message(id = 43147, value = "Unexpected exception while sending Committed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_sendCommitted_1();

	@Message(id = 43148, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_soapFault_1();

	@Message(id = 43149, value = "SoapFault called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_ParticipantProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43150, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_1();

	@Message(id = 43151, value = "Close called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_10(String arg0);

	@Message(id = 43152, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_11();

	@Message(id = 43153, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_12();

	@Message(id = 43154, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_13(String arg0);

	@Message(id = 43155, value = "Unexpected exception thrown from complete:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_14();

	@Message(id = 43156, value = "Complete called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_15(String arg0);

	@Message(id = 43157, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_16();

	@Message(id = 43158, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_2(String arg0);

	@Message(id = 43159, value = "Unexpected exception thrown from cancel:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_3();

	@Message(id = 43160, value = "Cancel called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_4(String arg0);

	@Message(id = 43161, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_5();

	@Message(id = 43162, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_6();

	@Message(id = 43163, value = "Transaction rolled back", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_7();

	@Message(id = 43164, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_8(String arg0);

	@Message(id = 43165, value = "Unexpected exception thrown from close:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_TerminatorParticipantProcessorImpl_9();

	@Message(id = 43166, value = "Unexpected exception from coordinator completed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_executeCompleted_1();

	@Message(id = 43167, value = "Unexpected exception from coordinator exit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_executeExit_1();

	@Message(id = 43168, value = "Unexpected exception from coordinator fault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_executeFault_1();

	@Message(id = 43169, value = "Unexpected exception while sending Cancel", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendCancel_1();

	@Message(id = 43170, value = "Unexpected exception while sending Close", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendClose_1();

	@Message(id = 43171, value = "Unexpected exception while sending Compensate", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendCompensate_1();

	@Message(id = 43172, value = "Unexpected exception while sending Complete", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendComplete_1();

	@Message(id = 43173, value = "Unexpected exception while sending Exited", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendExited_1();

	@Message(id = 43174, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendFaulted_1();

	@Message(id = 43175, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendStatus_1();

	@Message(id = 43176, value = "Unable to write recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_completed_1(String arg0);

	@Message(id = 43177, value = "Unable to delete recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_completed_2(String arg0);

	@Message(id = 43178, value = "Unexpected exception from participant cancel for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeCancel_1(String arg0);

	@Message(id = 43179, value = "Unexpected exception from participant close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeClose_1(String arg0);

	@Message(id = 43180, value = "Unable to delete recovery record during close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeClose_2(String arg0);

	@Message(id = 43181, value = "Faulted exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_1(String arg0);

	@Message(id = 43182, value = "Unexpected exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_2(String arg0);

	@Message(id = 43183, value = "Unable to delete recovery record during compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_3(String arg0);

	@Message(id = 43184, value = "Unexpected exception from participant complete for WS-BA  parfticipant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeComplete_1(String arg0);

	@Message(id = 43185, value = "Unable to write log record during participant complete for WS-BA  parfticipant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_executeComplete_2(String arg0);

	@Message(id = 43186, value = "Unable to delete recovery record during faulted for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_faulted_1(String arg0);

	@Message(id = 43187, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_getStatus_1(String arg0);

	@Message(id = 43188, value = "Unexpected exception while sending Cancelled", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_sendCancelled_1();

	@Message(id = 43189, value = "Unexpected exception while sending Closed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_sendClosed_1();

	@Message(id = 43190, value = "Unexpected exception while sending Compensated", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_sendCompensated_1();

	@Message(id = 43191, value = "Unexpected exception while sending Completed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_sendCompleted_1();

	@Message(id = 43192, value = "Unexpected exception while sending Exit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_sendExit_1();

	@Message(id = 43193, value = "Unexpected exception while sending Fault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_sendFault_1();

	@Message(id = 43194, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_sendStatus_1();

	@Message(id = 43195, value = "Unable to delete recovery record during soapFault processing for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_1(String arg0);

	@Message(id = 43196, value = "Cancelling participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_2(String arg0);

	@Message(id = 43197, value = "Notifying unexpected error for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43198, value = "Unexpecting exception while sending Commit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorEngine_sendCommit_1();

	@Message(id = 43199, value = "Inconsistent internal state.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorEngine_sendInvalidState_1();

	@Message(id = 43200, value = "Unexpecting exception while sending InvalidState", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorEngine_sendInvalidState_2();

	@Message(id = 43201, value = "Unexpecting exception while sending Prepare", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorEngine_sendPrepare_1();

	@Message(id = 43202, value = "Unexpecting exception while sending Rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorEngine_sendRollback_1();

	@Message(id = 43203, value = "Unexpected SOAP fault for coordinator {0}: {1} {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_CoordinatorEngine_soapFault_1(String arg0, String arg1, String arg2);

	@Message(id = 43204, value = "Unexpected exception from coordinator completed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_executeCompleted_1();

	@Message(id = 43205, value = "Unexpected exception from coordinator exit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_executeExit_1();

	@Message(id = 43206, value = "Unexpected exception from coordinator fault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_executeFault_1();

	@Message(id = 43207, value = "Unexpected exception while sending Cancel", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_sendCancel_1();

	@Message(id = 43208, value = "Unexpected exception while sending Close", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_sendClose_1();

	@Message(id = 43209, value = "Unexpected exception while sending Compensate", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_sendCompensate_1();

	@Message(id = 43210, value = "Unexpected exception while sending Exited", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_sendExited_1();

	@Message(id = 43211, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_sendFaulted_1();

	@Message(id = 43212, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionCoordinatorEngine_sendStatus_1();

	@Message(id = 43213, value = "Unable to write recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_completed_1(String arg0);

	@Message(id = 43214, value = "Unable to delete recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_completed_2(String arg0);

	@Message(id = 43215, value = "Unexpected exception from participant cancel fro WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeCancel_1(String arg0);

	@Message(id = 43216, value = "Unexpected exception from participant close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeClose_1(String arg0);

	@Message(id = 43217, value = "Unable to delete recovery record during close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeClose_2(String arg0);

	@Message(id = 43218, value = "Faulted exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_1(String arg0);

	@Message(id = 43219, value = "Unexpected exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_2(String arg0);

	@Message(id = 43220, value = "Unable to delete recovery record during compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_3(String arg0);

	@Message(id = 43221, value = "Unable to delete recovery record during faulted for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_faulted_1(String arg0);

	@Message(id = 43222, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_getStatus_1(String arg0);

	@Message(id = 43223, value = "Unexpected exception while sending Cancelled", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_sendCancelled_1();

	@Message(id = 43224, value = "Unexpected exception while sending Closed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_sendClosed_1();

	@Message(id = 43225, value = "Unexpected exception while sending Compensated", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_sendCompensated_1();

	@Message(id = 43226, value = "Unexpected exception while sending Completed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_sendCompleted_1();

	@Message(id = 43227, value = "Unexpected exception while sending Exit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_sendExit_1();

	@Message(id = 43228, value = "Unexpected exception while sending Fault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_sendFault_1();

	@Message(id = 43229, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_sendStatus_1();

	@Message(id = 43230, value = "Unable to delete recovery record during soapFault processing for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_1(String arg0);

	@Message(id = 43231, value = "Cancelling participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_2(String arg0);

	@Message(id = 43232, value = "Notifying unexpected error for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43233, value = "Unable to delete recovery record during prepare for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_commitDecision_2(String arg0);

	@Message(id = 43234, value = "Unable to delete recovery record at commit for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_commitDecision_3(String arg0);

	@Message(id = 43235, value = "Unexpected exception from participant commit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_executeCommit_1();

	@Message(id = 43236, value = "Unexpected exception from participant prepare", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_executePrepare_1();

	@Message(id = 43237, value = "Unexpected result from participant prepare: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_executePrepare_2(String arg0);

	@Message(id = 43238, value = "Unexpected exception from participant rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_executeRollback_1();

	@Message(id = 43239, value = "could not delete recovery record for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_rollback_1(String arg0);

	@Message(id = 43240, value = "Unexpected exception while sending Aborted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_sendAborted_1();

	@Message(id = 43241, value = "Unexpected exception while sending Committed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_sendCommitted_1();

	@Message(id = 43242, value = "Unexpected exception while sending Prepared", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_sendPrepared_1();

	@Message(id = 43243, value = "Unexpected exception while sending ReadOnly", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_sendReadOnly_1();

	@Message(id = 43244, value = "Unexpected exception while sending Replay", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_sendReplay_1();

	@Message(id = 43245, value = "Unexpected SOAP fault for participant {0}: {1} {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_soapFault_1(String arg0, String arg1, String arg2);

	@Message(id = 43246, value = "Unrecoverable error for participant {0} : {1} {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_soapFault_2(String arg0, String arg1, String arg2);

	@Message(id = 43247, value = "Unable to delete recovery record at commit for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_messaging_engines_ParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43248, value = "Unknown error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_BusinessActivityTerminatorStub_1();

	@Message(id = 43249, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_BusinessAgreementWithCoordinatorCompletionStub_2();

	@Message(id = 43250, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_BusinessAgreementWithCoordinatorCompletionStub_3();

	@Message(id = 43251, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_BusinessAgreementWithParticipantCompletionStub_2();

	@Message(id = 43252, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_BusinessAgreementWithParticipantCompletionStub_3();

	@Message(id = 43253, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_ParticipantStub_1();

	@Message(id = 43254, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_stub_ParticipantStub_2();

	@Message(id = 43255, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_1();

	@Message(id = 43256, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_10();

	@Message(id = 43257, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_2(String arg0);

	@Message(id = 43258, value = "Unexpected exception thrown from commit:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_3();

	@Message(id = 43259, value = "Commit called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_4(String arg0);

	@Message(id = 43260, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_5();

	@Message(id = 43261, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_6();

	@Message(id = 43262, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_7(String arg0);

	@Message(id = 43263, value = "Unexpected exception thrown from rollback:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_8();

	@Message(id = 43264, value = "Rollback called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CompletionCoordinatorProcessorImpl_9(String arg0);

	@Message(id = 43265, value = "Unexpected exception thrown from cancelled:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_cancelled_1();

	@Message(id = 43266, value = "Cancelled called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_cancelled_2(String arg0);

	@Message(id = 43267, value = "Unexpected exception thrown from cannotComplete:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_cannotComplete_1();

	@Message(id = 43268, value = "cannotComplete called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_cannotComplete_2(String arg0);

	@Message(id = 43269, value = "Unexpected exception thrown from closed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_closed_1();

	@Message(id = 43270, value = "Closed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_closed_2(String arg0);

	@Message(id = 43271, value = "Unexpected exception thrown from compensated:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_compensated_1();

	@Message(id = 43272, value = "Compensated called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_compensated_2(String arg0);

	@Message(id = 43273, value = "Unexpected exception thrown from completed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_completed_1();

	@Message(id = 43274, value = "Completed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_completed_2(String arg0);

	@Message(id = 43275, value = "Ignoring completed called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_completed_3(String arg0);

	@Message(id = 43276, value = "Unexpected exception thrown from exit:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_exit_1();

	@Message(id = 43277, value = "Exit called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_exit_2(String arg0);

	@Message(id = 43278, value = "Unexpected exception thrown from failed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_fail_1();

	@Message(id = 43279, value = "Failed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_fail_2(String arg0);

	@Message(id = 43280, value = "Ignoring fail called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_fail_3(String arg0);

	@Message(id = 43281, value = "Unexpected exception thrown from getStatus:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_1();

	@Message(id = 43282, value = "GetStatus called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_2(String arg0);

	@Message(id = 43283, value = "Unexpected exception while sending InvalidStateFault to participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_3(String arg0);

	@Message(id = 43284, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_4();

	@Message(id = 43285, value = "GetStatus dropped for unknown coordinator completion participant {0} while waiting on recovery scan", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_getStatus_5(String arg0);

	@Message(id = 43286, value = "Unexpected exception while sending Cancelled", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendCancelled_1();

	@Message(id = 43287, value = "Unexpected exception while sending Closed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendClosed_1();

	@Message(id = 43288, value = "Unexpected exception while sending Compensated", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendCompensated_1();

	@Message(id = 43289, value = "Unexpected exception while sending Exited", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendExited_1();

	@Message(id = 43290, value = "Unexpected exception while sending Fail", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendFail_1();

	@Message(id = 43291, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendFailed_1();

	@Message(id = 43292, value = "Unexpected exception while sending NotCompleted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_sendNotCompleted_1();

	@Message(id = 43293, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_soapFault_1();

	@Message(id = 43294, value = "SoapFault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43295, value = "Unexpected exception thrown from status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_status_1();

	@Message(id = 43296, value = "Status called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionCoordinatorProcessorImpl_status_2(String arg0);

	@Message(id = 43297, value = "Unexpected exception thrown from cancel:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_1();

	@Message(id = 43298, value = "Cancel called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_2(String arg0);

	@Message(id = 43299, value = "Cancel request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_3(String arg0);

	@Message(id = 43300, value = "Cancel request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_4(String arg0);

	@Message(id = 43301, value = "Cancel request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_cancel_5(String arg0);

	@Message(id = 43302, value = "Unexpected exception thrown from close:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_close_1();

	@Message(id = 43303, value = "Close called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_close_2(String arg0);

	@Message(id = 43304, value = "Close request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_close_3(String arg0);

	@Message(id = 43305, value = "Close request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_close_4(String arg0);

	@Message(id = 43306, value = "Close request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_close_5(String arg0);

	@Message(id = 43307, value = "Unexpected exception thrown from compensate:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_1();

	@Message(id = 43308, value = "Compensate called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_2(String arg0);

	@Message(id = 43309, value = "Compensate request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_3(String arg0);

	@Message(id = 43310, value = "Compensate request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_4(String arg0);

	@Message(id = 43311, value = "Compensate request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_compensate_5(String arg0);

	@Message(id = 43312, value = "Unexpected exception thrown from complete:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_1();

	@Message(id = 43313, value = "Complete called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_2(String arg0);

	@Message(id = 43314, value = "Complete called on unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_3();

	@Message(id = 43315, value = "Complete request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_4(String arg0);

	@Message(id = 43316, value = "Complete request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_5(String arg0);

	@Message(id = 43317, value = "Complete request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_complete_6(String arg0);

	@Message(id = 43318, value = "Unexpected exception thrown from exited:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_exited_1();

	@Message(id = 43319, value = "Exited called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_exited_2(String arg0);

	@Message(id = 43320, value = "Unexpected exception thrown from failed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_failed_1();

	@Message(id = 43321, value = "Failed called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_failed_2(String arg0);

	@Message(id = 43322, value = "Unexpected exception thrown from getStatus:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_1();

	@Message(id = 43323, value = "GetStatus called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_2(String arg0);

	@Message(id = 43324, value = "Unexpected exception while sending InvalidStateFault to coordinator for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_3(String arg0);

	@Message(id = 43325, value = "GetStatus requested for unknown coordinator completion participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_getStatus_4();

	@Message(id = 43326, value = "Unexpected exception thrown from notCompleted:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_notCompleted_1();

	@Message(id = 43327, value = "NotCompleted called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_notCompleted_2(String arg0);

	@Message(id = 43328, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_soapFault_1();

	@Message(id = 43329, value = "SoapFault called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43330, value = "Unexpected exception thrown from status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_status_1();

	@Message(id = 43331, value = "Status called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorCompletionParticipantProcessorImpl_status_2(String arg0);

	@Message(id = 43332, value = "Unexpected exception thrown from aborted:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_aborted_1();

	@Message(id = 43333, value = "Aborted called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_aborted_2(String arg0);

	@Message(id = 43334, value = "Unexpected exception thrown from committed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_committed_1();

	@Message(id = 43335, value = "Committed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_committed_2(String arg0);

	@Message(id = 43336, value = "Unexpected exception thrown from prepared:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_prepared_1();

	@Message(id = 43337, value = "Prepared called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_prepared_2(String arg0);

	@Message(id = 43338, value = "Ignoring prepared called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_prepared_3(String arg0);

	@Message(id = 43339, value = "Unexpected exception thrown from readOnly:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_readOnly_1();

	@Message(id = 43340, value = "ReadOnly called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_readOnly_2(String arg0);

	@Message(id = 43341, value = "Unexpected exception while sending Rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_sendRollback_1();

	@Message(id = 43342, value = "Unknown Transaction.", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_sendUnknownTransaction_1();

	@Message(id = 43343, value = "Unexpecting exception while sending InvalidState", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_sendUnknownTransaction_2();

	@Message(id = 43344, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_soapFault_1();

	@Message(id = 43345, value = "SoapFault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_CoordinatorProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43346, value = "Unexpected exception thrown from cancelled:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_cancelled_1();

	@Message(id = 43347, value = "Cancelled called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_cancelled_2(String arg0);

	@Message(id = 43348, value = "Unexpected exception thrown from cannot complete:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_cannotComplete_1();

	@Message(id = 43349, value = "Cannot complete called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_cannotComplete_2(String arg0);

	@Message(id = 43350, value = "Unexpected exception thrown from closed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_closed_1();

	@Message(id = 43351, value = "Closed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_closed_2(String arg0);

	@Message(id = 43352, value = "Unexpected exception thrown from compensated:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_compensated_1();

	@Message(id = 43353, value = "Compensated called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_compensated_2(String arg0);

	@Message(id = 43354, value = "Unexpected exception thrown from completed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_completed_1();

	@Message(id = 43355, value = "Completed called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_completed_2(String arg0);

	@Message(id = 43356, value = "Ignoring completed called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_completed_3(String arg0);

	@Message(id = 43357, value = "Unexpected exception thrown from exit:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_exit_1();

	@Message(id = 43358, value = "Exit called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_exit_2(String arg0);

	@Message(id = 43359, value = "Unexpected exception thrown from fail:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_fail_1();

	@Message(id = 43360, value = "Fail called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_fail_2(String arg0);

	@Message(id = 43361, value = "Ignoring fail called on unidentified coordinator until recovery pass is complete: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_fail_3(String arg0);

	@Message(id = 43362, value = "Unexpected exception thrown from getStatus:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_1();

	@Message(id = 43363, value = "GetStatus called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_2(String arg0);

	@Message(id = 43364, value = "Unexpected exception while sending InvalidStateFault to participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_3(String arg0);

	@Message(id = 43365, value = "GetStatus requested for unknown participant completion participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_4();

	@Message(id = 43366, value = "GetStatus dropped for unknown coordinator completion participant {0} while waiting on recovery scan", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_getStatus_5(String arg0);

	@Message(id = 43367, value = "Unexpected exception while sending Exited", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_sendExited_1();

	@Message(id = 43368, value = "Unexpected exception while sending Failed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_sendFailed_1();

	@Message(id = 43369, value = "Unexpected exception while sending NotCompleted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_sendNotCompleted_1();

	@Message(id = 43370, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_soapFault_1();

	@Message(id = 43371, value = "SoapFault called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43372, value = "Unexpected exception thrown from status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_status_1();

	@Message(id = 43373, value = "Status called on unknown coordinator: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionCoordinatorProcessorImpl_status_2(String arg0);

	@Message(id = 43374, value = "Unexpected exception thrown from cancel:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_1();

	@Message(id = 43375, value = "Cancel called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_2(String arg0);

	@Message(id = 43376, value = "Cancel request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_3(String arg0);

	@Message(id = 43377, value = "Cancel request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_4(String arg0);

	@Message(id = 43378, value = "Cancel request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_cancel_5(String arg0);

	@Message(id = 43379, value = "Unexpected exception thrown from close:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_close_1();

	@Message(id = 43380, value = "Close called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_close_2(String arg0);

	@Message(id = 43381, value = "Close request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_close_3(String arg0);

	@Message(id = 43382, value = "Close request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_close_4(String arg0);

	@Message(id = 43383, value = "Close request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_close_5(String arg0);

	@Message(id = 43384, value = "Unexpected exception thrown from compensate:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_1();

	@Message(id = 43385, value = "Compensate called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_2(String arg0);

	@Message(id = 43386, value = "Compensate request dropped pending WS-BA participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_3(String arg0);

	@Message(id = 43387, value = "Compensate request dropped pending WS-BA participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_4(String arg0);

	@Message(id = 43388, value = "Compensate request dropped pending registration of application-specific recovery module for WS-BA participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_compensate_5(String arg0);

	@Message(id = 43389, value = "Unexpected exception thrown from exited:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_exited_1();

	@Message(id = 43390, value = "Exited called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_exited_2(String arg0);

	@Message(id = 43391, value = "Unexpected exception thrown from failed:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_failed_1();

	@Message(id = 43392, value = "Failed called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_failed_2(String arg0);

	@Message(id = 43393, value = "Unexpected exception thrown from getStatus:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_1();

	@Message(id = 43394, value = "GetStatus called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_2(String arg0);

	@Message(id = 43395, value = "Unexpected exception while sending InvalidStateFault to coordinator for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_3(String arg0);

	@Message(id = 43396, value = "GetStatus requested for unknown participant completion participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_getStatus_4();

	@Message(id = 43397, value = "Unexpected exception thrown from notCompleted:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_notCompleted_1();

	@Message(id = 43398, value = "Exited called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_notCompleted_2(String arg0);

	@Message(id = 43399, value = "Unexpected exception while sending Cancelled", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_sendCancelled_1();

	@Message(id = 43400, value = "Unexpected exception while sending Closed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_sendClosed_1();

	@Message(id = 43401, value = "Unexpected exception while sending Compensated", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_sendCompensated_1();

	@Message(id = 43402, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_soapFault_1();

	@Message(id = 43403, value = "SoapFault called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43404, value = "Unexpected exception thrown from status:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_status_1();

	@Message(id = 43405, value = "Status called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantCompletionParticipantProcessorImpl_status_2(String arg0);

	@Message(id = 43406, value = "Unexpected exception thrown from commit:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_1();

	@Message(id = 43407, value = "Commit called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_2(String arg0);

	@Message(id = 43408, value = "Commit request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_3(String arg0);

	@Message(id = 43409, value = "Commit request dropped pending WS-AT participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_4(String arg0);

	@Message(id = 43410, value = "Commit request dropped pending registration of application-specific recovery module for WS-AT participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_commit_5(String arg0);

	@Message(id = 43411, value = "Unexpected exception thrown from prepare:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_prepare_1();

	@Message(id = 43412, value = "Prepare called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_prepare_2(String arg0);

	@Message(id = 43413, value = "Unexpected exception thrown from rollback:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_1();

	@Message(id = 43414, value = "Rollback called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_2(String arg0);

	@Message(id = 43415, value = "Rollback request dropped pending WS-AT participant recovery manager initialization for participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_3(String arg0);

	@Message(id = 43416, value = "Rollback request dropped pending WS-AT participant recovery manager scan for unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_4(String arg0);

	@Message(id = 43417, value = "Rollback request dropped pending registration of application-specific recovery module for WS-AT participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_rollback_5(String arg0);

	@Message(id = 43418, value = "Unexpected exception while sending Aborted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_sendAborted_1();

	@Message(id = 43419, value = "Unexpected exception while sending Committed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_sendCommitted_1();

	@Message(id = 43420, value = "Unexpected exception thrown from soapFault:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_soapFault_1();

	@Message(id = 43421, value = "SoapFault called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_ParticipantProcessorImpl_soapFault_2(String arg0);

	@Message(id = 43422, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_1();

	@Message(id = 43423, value = "Close called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_10(String arg0);

	@Message(id = 43424, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_11();

	@Message(id = 43425, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_12();

	@Message(id = 43426, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_13(String arg0);

	@Message(id = 43427, value = "Unexpected exception thrown from complete:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_14();

	@Message(id = 43428, value = "Complete called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_15(String arg0);

	@Message(id = 43429, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_16();

	@Message(id = 43430, value = "Service {0} received unexpected fault: {1}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_17(String arg0, String arg1);

	@Message(id = 43431, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_2(String arg0);

	@Message(id = 43432, value = "Unexpected exception thrown from cancel:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_3();

	@Message(id = 43433, value = "Cancel called on unknown participant: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_4(String arg0);

	@Message(id = 43434, value = "Unknown participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_5();

	@Message(id = 43435, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_6();

	@Message(id = 43436, value = "Transaction rolled back", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_7();

	@Message(id = 43437, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_8(String arg0);

	@Message(id = 43438, value = "Unexpected exception thrown from close:", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_TerminationCoordinatorProcessorImpl_9();

	@Message(id = 43439, value = "Unexpected exception from coordinator cannotComplete", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_executeCannotComplete_1();

	@Message(id = 43440, value = "Unexpected exception from coordinator completed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_executeCompleted_1();

	@Message(id = 43441, value = "Unexpected exception from coordinator exit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_executeExit_1();

	@Message(id = 43442, value = "Unexpected exception from coordinator fail", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_executeFail_1();

	@Message(id = 43443, value = "Unexpected exception while sending Cancel", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendCancel_1();

	@Message(id = 43444, value = "Unexpected exception while sending Close", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendClose_1();

	@Message(id = 43445, value = "Unexpected exception while sending Compensate", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendCompensate_1();

	@Message(id = 43446, value = "Unexpected exception while sending Complete", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendComplete_1();

	@Message(id = 43447, value = "Unexpected exception while sending Exited", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendExited_1();

	@Message(id = 43448, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendFailed_1();

	@Message(id = 43449, value = "Unexpected exception while sending InvalidStateFault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendInvalidStateFault_1();

	@Message(id = 43450, value = "Invalid coordinator completion coordinator state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendInvalidStateFault_2();

	@Message(id = 43451, value = "Unexpected exception while sending NotCompleted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendNotCompleted_1();

	@Message(id = 43452, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionCoordinatorEngine_sendStatus_1();

	@Message(id = 43453, value = "Unable to write recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_completed_1(String arg0);

	@Message(id = 43454, value = "Unable to delete recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_completed_2(String arg0);

	@Message(id = 43455, value = "Faulted exception from participant cancel for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeCancel_1(String arg0);

	@Message(id = 43456, value = "Unexpected exception from participant cancel for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeCancel_2(String arg0);

	@Message(id = 43457, value = "Unexpected exception from participant close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeClose_1(String arg0);

	@Message(id = 43458, value = "Unable to delete recovery record during close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeClose_2(String arg0);

	@Message(id = 43459, value = "Faulted exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_1(String arg0);

	@Message(id = 43460, value = "Unexpected exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_2(String arg0);

	@Message(id = 43461, value = "Unable to delete recovery record during compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeCompensate_3(String arg0);

	@Message(id = 43462, value = "Unexpected exception from participant complete for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeComplete_1(String arg0);

	@Message(id = 43463, value = "Unable to write log record during participant complete for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_executeComplete_2(String arg0);

	@Message(id = 43464, value = "Unable to delete recovery record during failed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_failed_1(String arg0);

	@Message(id = 43465, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_getStatus_1(String arg0);

	@Message(id = 43466, value = "Unexpected exception while sending Cancelled", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_sendCancelled_1();

	@Message(id = 43467, value = "Unexpected exception while sending CannotComplete", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_sendCannotComplete_1();

	@Message(id = 43468, value = "Unexpected exception while sending Closed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_sendClosed_1();

	@Message(id = 43469, value = "Unexpected exception while sending Compensated", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_sendCompensated_1();

	@Message(id = 43470, value = "Unexpected exception while sending Completed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_sendCompleted_1();

	@Message(id = 43471, value = "Unexpected exception while sending Exit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_sendExit_1();

	@Message(id = 43472, value = "Unexpected exception while sending Fail", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_sendFail_1();

	@Message(id = 43473, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_sendStatus_1();

	@Message(id = 43474, value = "Unable to delete recovery record during soapFault processing for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_1(String arg0);

	@Message(id = 43475, value = "Cancelling participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_2(String arg0);

	@Message(id = 43476, value = "Notifying unexpected error for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorCompletionParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43477, value = "Unexpecting exception while sending Commit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorEngine_sendCommit_1();

	@Message(id = 43478, value = "Unexpecting exception while sending Prepare", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorEngine_sendPrepare_1();

	@Message(id = 43479, value = "Unexpecting exception while sending Rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorEngine_sendRollback_1();

	@Message(id = 43480, value = "Unknown transaction", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorEngine_sendUnknownTransaction_1();

	@Message(id = 43481, value = "Unexpected exception while sending UnknownTransaction for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorEngine_sendUnknownTransaction_2(String arg0);

	@Message(id = 43482, value = "Unexpected SOAP fault for coordinator {0}: {1} {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_CoordinatorEngine_soapFault_1(String arg0, String arg1, String arg2);

	@Message(id = 43483, value = "Unexpected exception from coordinator error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_executeCannotComplete_1();

	@Message(id = 43484, value = "Unexpected exception from coordinator completed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_executeCompleted_1();

	@Message(id = 43485, value = "Unexpected exception from coordinator exit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_executeExit_1();

	@Message(id = 43486, value = "Unexpected exception from coordinator fault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_executeFault_1();

	@Message(id = 43487, value = "Unexpected exception while sending Cancel", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_sendCancel_1();

	@Message(id = 43488, value = "Unexpected exception while sending Close", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_sendClose_1();

	@Message(id = 43489, value = "Unexpected exception while sending Compensate", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_sendCompensate_1();

	@Message(id = 43490, value = "Unexpected exception while sending Exited", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_sendExited_1();

	@Message(id = 43491, value = "Unexpected exception while sending Faulted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_sendFailed_1();

	@Message(id = 43492, value = "Unexpected exception while sending NotCompleted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_sendNotCompleted_1();

	@Message(id = 43493, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionCoordinatorEngine_sendStatus_1();

	@Message(id = 43494, value = "Unable to write recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_completed_1(String arg0);

	@Message(id = 43495, value = "Unable to delete recovery record during completed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_completed_2(String arg0);

	@Message(id = 43496, value = "Faulted exception from participant cancel for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeCancel_1(String arg0);

	@Message(id = 43497, value = "Unexpected exception from participant cancel for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeCancel_2(String arg0);

	@Message(id = 43498, value = "Unexpected exception from participant close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeClose_1(String arg0);

	@Message(id = 43499, value = "Unable to delete recovery record during close for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeClose_2(String arg0);

	@Message(id = 43500, value = "Faulted exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_1(String arg0);

	@Message(id = 43501, value = "Unexpected exception from participant compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_2(String arg0);

	@Message(id = 43502, value = "Unable to delete recovery record during compensate for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_executeCompensate_3(String arg0);

	@Message(id = 43503, value = "Unable to delete recovery record during failed for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_failed_1(String arg0);

	@Message(id = 43504, value = "Unknown error: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_getStatus_1(String arg0);

	@Message(id = 43505, value = "Unexpected exception while sending Cancelled", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_sendCancelled_1();

	@Message(id = 43506, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_sendCannotComplete_1();

	@Message(id = 43507, value = "Unexpected exception while sending Closed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_sendClosed_1();

	@Message(id = 43508, value = "Unexpected exception while sending Compensated", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_sendCompensated_1();

	@Message(id = 43509, value = "Unexpected exception while sending Completed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_sendCompleted_1();

	@Message(id = 43510, value = "Unexpected exception while sending Exit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_sendExit_1();

	@Message(id = 43511, value = "Unexpected exception while sending Fault", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_sendFail_1();

	@Message(id = 43512, value = "Unexpected exception while sending Status", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_sendStatus_1();

	@Message(id = 43513, value = "Unable to delete recovery record during soapFault processing for WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_1(String arg0);

	@Message(id = 43514, value = "Cancelling participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_2(String arg0);

	@Message(id = 43515, value = "Notifying unexpected error for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantCompletionParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43516, value = "Exception rolling back participant", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_commitDecision_1();

	@Message(id = 43517, value = "Unable to delete recovery record during prepare for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_commitDecision_2(String arg0);

	@Message(id = 43518, value = "Unable to delete recovery record at commit for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_commitDecision_3(String arg0);

	@Message(id = 43519, value = "Unexpected exception from participant commit", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_executeCommit_1();

	@Message(id = 43520, value = "Unexpected exception from participant prepare", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_executePrepare_1();

	@Message(id = 43521, value = "Unexpected result from participant prepare: {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_executePrepare_2(String arg0);

	@Message(id = 43522, value = "Unexpected exception from participant rollback", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_executeRollback_1();

	@Message(id = 43523, value = "could not delete recovery record for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_rollback_1(String arg0);

	@Message(id = 43524, value = "Unexpected exception while sending Aborted", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_sendAborted_1();

	@Message(id = 43525, value = "Unexpected exception while sending Committed", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_sendCommitted_1();

	@Message(id = 43526, value = "Unexpected exception while sending Prepared", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_sendPrepared_1();

	@Message(id = 43527, value = "Unexpected exception while sending ReadOnly", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_sendReadOnly_1();

	@Message(id = 43528, value = "Unexpected SOAP fault for participant {0}: {1} {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_soapFault_1(String arg0, String arg1, String arg2);

	@Message(id = 43529, value = "Unrecoverable error for participant {0} : {1} {2}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_soapFault_2(String arg0, String arg1, String arg2);

	@Message(id = 43530, value = "Unable to delete recovery record at commit for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_messaging_engines_ParticipantEngine_soapFault_3(String arg0);

	@Message(id = 43531, value = "Unknown error", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_stub_BusinessActivityTerminatorStub_1();

	@Message(id = 43532, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_stub_BusinessAgreementWithCoordinatorCompletionStub_2();

	@Message(id = 43533, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_stub_BusinessAgreementWithCoordinatorCompletionStub_3();

	@Message(id = 43534, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_stub_BusinessAgreementWithParticipantCompletionStub_2();

	@Message(id = 43535, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_stub_BusinessAgreementWithParticipantCompletionStub_3();

	@Message(id = 43536, value = "Error persisting participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_stub_ParticipantStub_1();

	@Message(id = 43537, value = "Error restoring participant state", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_wst11_stub_ParticipantStub_2();

	@Message(id = 43538, value = "participant {0} has no saved recovery state to recover", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_at_ATParticipantRecoveryRecord_restoreParticipant_1(String arg0);

	@Message(id = 43539, value = "XML stream exception restoring recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_at_ATParticipantRecoveryRecord_restoreState_1(String arg0);

	@Message(id = 43540, value = "I/O exception saving restoring state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_at_ATParticipantRecoveryRecord_restoreState_2(String arg0);

	@Message(id = 43541, value = "Could not save recovery state for non-serializable durable WS-AT participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_at_ATParticipantRecoveryRecord_saveState_1(String arg0);

	@Message(id = 43542, value = "XML stream exception saving recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_at_ATParticipantRecoveryRecord_saveState_2(String arg0);

	@Message(id = 43543, value = "I/O exception saving recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_at_ATParticipantRecoveryRecord_saveState_3(String arg0);

	@Message(id = 43544, value = "participant {0} has no saved recovery state to recover", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_ba_BAParticipantRecoveryRecord_restoreParticipant_1(String arg0);

	@Message(id = 43545, value = "XML stream exception restoring recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_ba_BAParticipantRecoveryRecord_restoreState_1(String arg0);

	@Message(id = 43546, value = "I/O exception saving restoring state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_ba_BAParticipantRecoveryRecord_restoreState_2(String arg0);

	@Message(id = 43547, value = "Could not save recovery state for non-serializable WS-BA participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_ba_BAParticipantRecoveryRecord_saveState_1(String arg0);

	@Message(id = 43548, value = "XML stream exception saving recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_ba_BAParticipantRecoveryRecord_saveState_2(String arg0);

	@Message(id = 43549, value = "I/O exception saving recovery state for participant {0}", format = MESSAGE_FORMAT)
	@LogMessage(level = WARN)
	public void warn_transactions_xts_recovery_participant_ba_BAParticipantRecoveryRecord_saveState_3(String arg0);

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
