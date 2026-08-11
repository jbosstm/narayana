/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
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
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.jboss.transaction.txinterop.webservices.bainterop;

import javax.xml.namespace.QName;

/**
 * Interface containing WS-TX BA Interop constants.
 */
public interface BAInteropConstants
{
    /**
     * The interop test initiator service name.
     */
    public String SERVICE_INITIATOR = "BAInitiatorService" ;

    /**
     * The interop test initiator service name.
     */
    public String SERVICE_PARTICIPANT = "BAParticipantService" ;

    /**
     * The interop Namespace.
     */
    public String INTEROP_NAMESPACE = "http://fabrikam123.com/wsba" ;
    /**
     * The interop namespace prefix.
     */
    public String INTEROP_PREFIX = "bainterop" ;
    
    /**
     * The participant action prefix.
     */
    public String INTEROP_ACTION_PARTICIPANT_PREFIX = INTEROP_NAMESPACE + "/" ;
    /**
     * The initiator action prefix.
     */
    public String INTEROP_ACTION_INITIATOR_PREFIX = INTEROP_NAMESPACE + "/" ;

    /**
     * The Cancel element.
     */
    public String INTEROP_ELEMENT_CANCEL = "Cancel" ;
    /**
     * The Cancel QName.
     */
    public QName INTEROP_ELEMENT_QNAME_CANCEL = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_CANCEL, INTEROP_PREFIX) ;
    /**
     * The Cancel Action.
     */
    public String INTEROP_ACTION_CANCEL = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_CANCEL ;
    
    /**
     * The Exit element.
     */
    public String INTEROP_ELEMENT_EXIT = "Exit" ;
    /**
     * The Exit QName.
     */
    public QName INTEROP_ELEMENT_QNAME_EXIT = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_EXIT, INTEROP_PREFIX) ;
    /**
     * The Exit Action.
     */
    public String INTEROP_ACTION_EXIT = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_EXIT ;
    
    /**
     * The Fail element.
     */
    public String INTEROP_ELEMENT_FAIL = "Fail" ;
    /**
     * The Fail QName.
     */
    public QName INTEROP_ELEMENT_QNAME_FAIL = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_FAIL, INTEROP_PREFIX) ;
    /**
     * The Fail Action.
     */
    public String INTEROP_ACTION_FAIL = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_FAIL ;
    
    /**
     * The Cannot Complete element.
     */
    public String INTEROP_ELEMENT_CANNOT_COMPLETE = "CannotComplete" ;
    /**
     * The Cannot Complete QName.
     */
    public QName INTEROP_ELEMENT_QNAME_CANNOT_COMPLETE = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_CANNOT_COMPLETE, INTEROP_PREFIX) ;
    /**
     * The Cannot Complete Action.
     */
    public String INTEROP_ACTION_CANNOT_COMPLETE = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_CANNOT_COMPLETE ;
    
    /**
     * The Participant Complete Close element.
     */
    public String INTEROP_ELEMENT_PARTICIPANT_COMPLETE_CLOSE = "ParticipantCompleteClose" ;
    /**
     * The Participant Complete Close QName.
     */
    public QName INTEROP_ELEMENT_QNAME_PARTICIPANT_COMPLETE_CLOSE = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_PARTICIPANT_COMPLETE_CLOSE, INTEROP_PREFIX) ;
    /**
     * The Participant Complete Close Action.
     */
    public String INTEROP_ACTION_PARTICIPANT_COMPLETE_CLOSE = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_PARTICIPANT_COMPLETE_CLOSE ;
    
    /**
     * The Coordinator Complete Close element.
     */
    public String INTEROP_ELEMENT_COORDINATOR_COMPLETE_CLOSE = "CoordinatorCompleteClose" ;
    /**
     * The Coordinator Complete Close QName.
     */
    public QName INTEROP_ELEMENT_QNAME_COORDINATOR_COMPLETE_CLOSE = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_COORDINATOR_COMPLETE_CLOSE, INTEROP_PREFIX) ;
    /**
     * The Coordinator Complete Close Action.
     */
    public String INTEROP_ACTION_COORDINATOR_COMPLETE_CLOSE = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_COORDINATOR_COMPLETE_CLOSE ;
    
    /**
     * The Unsolicited Complete element.
     */
    public String INTEROP_ELEMENT_UNSOLICITED_COMPLETE = "UnsolicitedComplete" ;
    /**
     * The Unsolicited Complete QName.
     */
    public QName INTEROP_ELEMENT_QNAME_UNSOLICITED_COMPLETE = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_UNSOLICITED_COMPLETE, INTEROP_PREFIX) ;
    /**
     * The Unsolicited Complete Action.
     */
    public String INTEROP_ACTION_UNSOLICITED_COMPLETE = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_UNSOLICITED_COMPLETE ;
    
    /**
     * The Compensate element.
     */
    public String INTEROP_ELEMENT_COMPENSATE = "Compensate" ;
    /**
     * The Compensate QName.
     */
    public QName INTEROP_ELEMENT_QNAME_COMPENSATE = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_COMPENSATE, INTEROP_PREFIX) ;
    /**
     * The Compensate Action.
     */
    public String INTEROP_ACTION_COMPENSATE = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_COMPENSATE ;
    
    /**
     * The Compensation Fail element.
     */
    public String INTEROP_ELEMENT_COMPENSATION_FAIL = "CompensationFail" ;
    /**
     * The Compensation Fail QName.
     */
    public QName INTEROP_ELEMENT_QNAME_COMPENSATION_FAIL = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_COMPENSATION_FAIL, INTEROP_PREFIX) ;
    /**
     * The Compensation Fail Action.
     */
    public String INTEROP_ACTION_COMPENSATION_FAIL = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_COMPENSATION_FAIL ;
    
    /**
     * The Participant Cancel Completed Race element.
     */
    public String INTEROP_ELEMENT_PARTICIPANT_CANCEL_COMPLETED_RACE = "ParticipantCancelCompletedRace" ;
    /**
     * The Participant Cancel Completed Race QName.
     */
    public QName INTEROP_ELEMENT_QNAME_PARTICIPANT_CANCEL_COMPLETED_RACE = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_PARTICIPANT_CANCEL_COMPLETED_RACE, INTEROP_PREFIX) ;
    /**
     * The Participant Cancel Completed Race Action.
     */
    public String INTEROP_ACTION_PARTICIPANT_CANCEL_COMPLETED_RACE = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_PARTICIPANT_CANCEL_COMPLETED_RACE ;
    
    /**
     * The Message Loss And Recovery element.
     */
    public String INTEROP_ELEMENT_MESSAGE_LOSS_AND_RECOVERY = "MessageLossAndRecovery" ;
    /**
     * The Message Loss And Recovery QName.
     */
    public QName INTEROP_ELEMENT_QNAME_MESSAGE_LOSS_AND_RECOVERY = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_MESSAGE_LOSS_AND_RECOVERY, INTEROP_PREFIX) ;
    /**
     * The Message Loss And Recovery Action.
     */
    public String INTEROP_ACTION_MESSAGE_LOSS_AND_RECOVERY = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_MESSAGE_LOSS_AND_RECOVERY ;
    
    /**
     * The Mixed Outcome element.
     */
    public String INTEROP_ELEMENT_MIXED_OUTCOME = "MixedOutcome" ;
    /**
     * The Mixed Outcome QName.
     */
    public QName INTEROP_ELEMENT_QNAME_MIXED_OUTCOME = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_MIXED_OUTCOME, INTEROP_PREFIX) ;
    /**
     * The Mixed Outcome Action.
     */
    public String INTEROP_ACTION_MIXED_OUTCOME = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_MIXED_OUTCOME ;
    
    /**
     * The response element.
     */
    public String INTEROP_ELEMENT_RESPONSE = "Response" ;
    /**
     * The response QName.
     */
    public QName INTEROP_ELEMENT_QNAME_RESPONSE = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_RESPONSE, INTEROP_PREFIX) ;
    /**
     * The response Action.
     */
    public String INTEROP_ACTION_RESPONSE = INTEROP_ACTION_INITIATOR_PREFIX + INTEROP_ELEMENT_RESPONSE ;
}
