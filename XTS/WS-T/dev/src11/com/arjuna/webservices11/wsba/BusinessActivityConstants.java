/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices11.wsba;

import javax.xml.namespace.QName;

/**
 * Interface containing WS-BusinessActivity constants.
 */
public interface BusinessActivityConstants
{
    /**
     * The Namespace.
     */
    public String WSBA_NAMESPACE = "http://docs.oasis-open.org/ws-tx/wsba/2006/06";
    /**
     * The namespace prefix.
     */
    public String WSBA_PREFIX = "wsba";

    /**
     * The WSBA Participant Completion Cordinator Service Name.
     */
    public String PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME = "BusinessAgreementWithParticipantCompletionCoordinatorService";
    /**
     * The WSBA Participant Completion Cordinator Service QName.
     */
    public QName PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_QNAME = new QName(WSBA_NAMESPACE, PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME, WSBA_PREFIX);
    /**
     * The WSBA Participant Completion Cordinator Port Name.
     */
    public String PARTICIPANT_COMPLETION_COORDINATOR_PORT_NAME = "BusinessAgreementWithParticipantCompletionCoordinatorPortType";
    /**
     * The WSBA Participant Completion Cordinator Port QName.
     */
    public QName PARTICIPANT_COMPLETION_COORDINATOR_PORT_QNAME = new QName(WSBA_NAMESPACE, PARTICIPANT_COMPLETION_COORDINATOR_PORT_NAME, WSBA_PREFIX);

    /**
     * The WSBA Participant Completion Participant Service Name.
     */
    public String PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_NAME = "BusinessAgreementWithParticipantCompletionParticipantService";
    /**
     * The WSBA Participant Completion Participant Service QName.
     */
    public QName PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_QNAME = new QName(WSBA_NAMESPACE, PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_NAME, WSBA_PREFIX);
    /**
     * The WSBA Participant Completion Participant Port Name.
     */
    public String PARTICIPANT_COMPLETION_PARTICIPANT_PORT_NAME = "BusinessAgreementWithParticipantCompletionParticipantPortType";
    /**
     * The WSBA Participant Completion Participant Port QName.
     */
    public QName PARTICIPANT_COMPLETION_PARTICIPANT_PORT_QNAME = new QName(WSBA_NAMESPACE, PARTICIPANT_COMPLETION_PARTICIPANT_PORT_NAME, WSBA_PREFIX);

    /**
     * The WSBA Coordinator Completion Coordinator Service Name.
     */
    public String COORDINATOR_COMPLETION_COORDINATOR_SERVICE_NAME = "BusinessAgreementWithCoordinatorCompletionCoordinatorService";
    /**
     * The WSBA Coordinator Completion Coordinator Service QName.
     */
    public QName COORDINATOR_COMPLETION_COORDINATOR_SERVICE_QNAME = new QName(WSBA_NAMESPACE, COORDINATOR_COMPLETION_COORDINATOR_SERVICE_NAME, WSBA_PREFIX);
    /**
     * The WSBA Coordinator Completion Coordinator Port Name.
     */
    public String COORDINATOR_COMPLETION_COORDINATOR_PORT_NAME = "BusinessAgreementWithCoordinatorCompletionCoordinatorPortType";
    /**
     * The WSBA Coordinator Completion Coordinator Port QName.
     */
    public QName COORDINATOR_COMPLETION_COORDINATOR_PORT_QNAME = new QName(WSBA_NAMESPACE, COORDINATOR_COMPLETION_COORDINATOR_PORT_NAME, WSBA_PREFIX);

    /**
     * The WSBA Coordinator Completion Participant Service Name.
     */
    public String COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_NAME = "BusinessAgreementWithCoordinatorCompletionParticipantService";
    /**
     * The WSBA Coordinator Completion Participant Service QName.
     */
    public QName COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_QNAME = new QName(WSBA_NAMESPACE, COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_NAME, WSBA_PREFIX);
    /**
     * The WSBA Cordinator Completion Participant Port Name.
     */
    public String COORDINATOR_COMPLETION_PARTICIPANT_PORT_NAME = "BusinessAgreementWithCoordinatorCompletionParticipantPortType";
    /**
     * The WSBA Cordinator Completion Participant Port QName.
     */
    public QName COORDINATOR_COMPLETION_PARTICIPANT_PORT_QNAME = new QName(WSBA_NAMESPACE, COORDINATOR_COMPLETION_PARTICIPANT_PORT_NAME, WSBA_PREFIX);

    /* extra ops for 1.1. protocol*/

    /**
     * The Fail element.
     */
    public String WSBA_ELEMENT_FAIL = "Fail" ;
    /**
     * The Fail QName.
     */
    public QName WSBA_ELEMENT_FAIL_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_FAIL, WSBA_PREFIX) ;
    /**
     * The Fail Action.
     */
    public String WSBA_ACTION_FAIL = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_FAIL ;

    /**
     * The ExceptionIdentifier element.
     */
    public String WSBA_ELEMENT_EXCEPTION_IDENTIFIER = "ExceptionIdentifier" ;
    /**
     * The ExceptionIdentifier QName.
     */
    public QName WSBA_ELEMENT_EXCEPTION_IDENTIFIER_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_EXCEPTION_IDENTIFIER, WSBA_PREFIX) ;
    /**
     * The Canceled element.
     */
    public String WSBA_ELEMENT_CANCELLED = "Canceled" ;
    /**
     * The Canceled QName.
     */
    public QName WSBA_ELEMENT_CANCELLED_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_CANCELLED, WSBA_PREFIX) ;
    /**
     * The Canceled Action.
     */
    public String WSBA_ACTION_CANCELLED = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_CANCELLED ;
    /**
     * The Closed element.
     */
    public String WSBA_ELEMENT_CLOSED = "Closed" ;
    /**
     * The Closed QName.
     */
    public QName WSBA_ELEMENT_CLOSED_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_CLOSED, WSBA_PREFIX) ;
    /**
     * The Closed Action.
     */
    public String WSBA_ACTION_CLOSED = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_CLOSED ;
    /**
     * The Compensated element.
     */
    public String WSBA_ELEMENT_COMPENSATED = "Compensated" ;
    /**
     * The Compensated QName.
     */
    public QName WSBA_ELEMENT_COMPENSATED_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_COMPENSATED, WSBA_PREFIX) ;
    /**
     * The Compensated Action.
     */
    public String WSBA_ACTION_COMPENSATED = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_COMPENSATED ;
    /**
     * The Completed element.
     */
    public String WSBA_ELEMENT_COMPLETED = "Completed" ;
    /**
     * The Completed QName.
     */
    public QName WSBA_ELEMENT_COMPLETED_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_COMPLETED, WSBA_PREFIX) ;
    /**
     * The Completed Action.
     */
    public String WSBA_ACTION_COMPLETED = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_COMPLETED ;
    /**
     * The Exit element.
     */
    public String WSBA_ELEMENT_EXIT = "Exit" ;
    /**
     * The Exit QName.
     */
    public QName WSBA_ELEMENT_EXIT_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_EXIT, WSBA_PREFIX) ;
    /**
     * The Exit Action.
     */
    public String WSBA_ACTION_EXIT = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_EXIT ;
    /**
     * The Fault element.
     */
    public String WSBA_ELEMENT_FAULT = "Fault" ;
    /**
     * The Fault QName.
     */
    public QName WSBA_ELEMENT_FAULT_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_FAULT, WSBA_PREFIX) ;
    /**
     * The Fault Action.
     */
    public String WSBA_ACTION_FAULT = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_FAULT ;
    /**
     * The Cancel element.
     */
    public String WSBA_ELEMENT_CANCEL = "Cancel" ;
    /**
     * The Cancel QName.
     */
    public QName WSBA_ELEMENT_CANCEL_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_CANCEL, WSBA_PREFIX) ;
    /**
     * The Cancel Action.
     */
    public String WSBA_ACTION_CANCEL = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_CANCEL ;
    /**
     * The Close element.
     */
    public String WSBA_ELEMENT_CLOSE = "Close" ;
    /**
     * The Close QName.
     */
    public QName WSBA_ELEMENT_CLOSE_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_CLOSE, WSBA_PREFIX) ;
    /**
     * The Close Action.
     */
    public String WSBA_ACTION_CLOSE = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_CLOSE ;
    /**
     * The Compensate element.
     */
    public String WSBA_ELEMENT_COMPENSATE = "Compensate" ;
    /**
     * The Compensate QName.
     */
    public QName WSBA_ELEMENT_COMPENSATE_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_COMPENSATE, WSBA_PREFIX) ;
    /**
     * The Compensate Action.
     */
    public String WSBA_ACTION_COMPENSATE = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_COMPENSATE ;
    /**
     * The Complete element.
     */
    public String WSBA_ELEMENT_COMPLETE = "Complete" ;
    /**
     * The Complete QName.
     */
    public QName WSBA_ELEMENT_COMPLETE_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_COMPLETE, WSBA_PREFIX) ;
    /**
     * The Complete Action.
     */
    public String WSBA_ACTION_COMPLETE = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_COMPLETE ;
    /**
     * The CannotComplete element.
     */
    public String WSBA_ELEMENT_CANNOT_COMPLETE = "CannotComplete" ;
    /**
     * The CannotComplete QName.
     */
    public QName WSBA_ELEMENT_CANNOT_COMPLETE_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_CANNOT_COMPLETE, WSBA_PREFIX) ;
    /**
     * The CannotComplete Action.
     */
    public String WSBA_ACTION_CANNOT_COMPLETE = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_CANNOT_COMPLETE ;

    /**
     * The Failed element.
     */
    public String WSBA_ELEMENT_FAILED = "Failed" ;
    /**
     * The Failed QName.
     */
    public QName WSBA_ELEMENT_FAILED_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_FAILED, WSBA_PREFIX) ;
    /**
     * The Failed Action.
     */
    public String WSBA_ACTION_FAILED = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_FAILED ;

    /**
     * The NotCompleted element.
     */
    public String WSBA_ELEMENT_NOT_COMPLETED = "NotCompleted" ;
    /**
     * The NotCompleted QName.
     */
    public QName WSBA_ELEMENT_NOT_COMPLETED_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_NOT_COMPLETED, WSBA_PREFIX) ;
    /**
     * The NotCompleted Action.
     */
    public String WSBA_ACTION_NOT_COMPLETED = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_NOT_COMPLETED ;
    /**
     * The Exited element.
     */
    public String WSBA_ELEMENT_EXITED = "Exited" ;
    /**
     * The Exited QName.
     */
    public QName WSBA_ELEMENT_EXITED_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_EXITED, WSBA_PREFIX) ;
    /**
     * The Exited Action.
     */
    public String WSBA_ACTION_EXITED = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_EXITED ;
    /**
     * The GetStatus element.
     */
    public String WSBA_ELEMENT_GET_STATUS = "GetStatus" ;
    /**
     * The GetStatus QName.
     */
    public QName WSBA_ELEMENT_GET_STATUS_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_GET_STATUS, WSBA_PREFIX) ;
    /**
     * The GetStatus Action.
     */
    public String WSBA_ACTION_GET_STATUS = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_GET_STATUS ;
    /**
     * The State element.
     */
    public String WSBA_ELEMENT_STATE = "State" ;
    /**
     * The State QName.
     */
    public QName WSBA_ELEMENT_STATE_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_STATE, WSBA_PREFIX) ;
    /**
     * The State Action.
     */
    public String WSBA_ACTION_STATE = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_STATE ;
    /**
     * The Status element.
     */
    public String WSBA_ELEMENT_STATUS = "Status" ;
    /**
     * The Status QName.
     */
    public QName WSBA_ELEMENT_STATUS_QNAME = new QName(WSBA_NAMESPACE, WSBA_ELEMENT_STATUS, WSBA_PREFIX) ;
    /**
     * The Status Action.
     */
    public String WSBA_ACTION_STATUS = WSBA_NAMESPACE + "/" + WSBA_ELEMENT_STATUS ;

    /**
     * The InconsistentInternalState error code.
     */
    public String WSBA_ERROR_CODE_INCONSISTENT_INTERNAL_STATE = "InconsistentInternalState" ;
    /**
     * The InconsistentInternalState error code QName.
     */
    public QName WSBA_ERROR_CODE_INCONSISTENT_INTERNAL_STATE_QNAME = new QName(WSBA_NAMESPACE, WSBA_ERROR_CODE_INCONSISTENT_INTERNAL_STATE, WSBA_PREFIX) ;

    /**
     * The business activity atomic outcome protocol.
     */
    public String WSBA_PROTOCOL_ATOMIC_OUTCOME = WSBA_NAMESPACE + "/AtomicOutcome" ;
    /**
     * The business activity mixed outcome protocol.
     */
    public String WSBA_PROTOCOL_MIXED_OUTCOME = WSBA_NAMESPACE + "/MixedOutcome" ;
    /**
     * The participant completion protocol.
     */
    public String WSBA_SUB_PROTOCOL_PARTICIPANT_COMPLETION = WSBA_NAMESPACE + "/ParticipantCompletion" ;
    /**
     * The coordinator completion protocol.
     */
    public String WSBA_SUB_PROTOCOL_COORDINATOR_COMPLETION = WSBA_NAMESPACE + "/CoordinatorCompletion" ;
}