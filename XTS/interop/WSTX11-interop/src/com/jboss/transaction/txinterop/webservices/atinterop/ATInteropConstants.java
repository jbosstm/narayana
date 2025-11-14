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
package com.jboss.transaction.txinterop.webservices.atinterop;

import javax.xml.namespace.QName;

/**
 * Interface containing WS-TX AT Interop constants.
 */
public interface ATInteropConstants
{
    /**
     * The interop test initiator service name.
     */
    public String SERVICE_INITIATOR = "ATInitiatorService" ;

    /**
     * The interop test participant service name.
     */
    public String SERVICE_PARTICIPANT = "ATParticipantService" ;

    /**
     * The interop Namespace.
     */
    public String INTEROP_NAMESPACE = "http://fabrikam123.com" ;
    /**
     * The interop namespace prefix.
     */
    public String INTEROP_PREFIX = "atinterop" ;
    
    /**
     * The participant action prefix.
     */
    public String INTEROP_ACTION_PARTICIPANT_PREFIX = INTEROP_NAMESPACE + "/";
    /**
     * The initiator action prefix.
     */
    public String INTEROP_ACTION_INITIATOR_PREFIX = INTEROP_NAMESPACE + "/";

    /**
     * The completion commit element.
     */
    public String INTEROP_ELEMENT_COMPLETION_COMMIT = "CompletionCommit" ;
    /**
     * The completion commit QName.
     */
    public QName INTEROP_ELEMENT_QNAME_COMPLETION_COMMIT = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_COMPLETION_COMMIT, INTEROP_PREFIX) ;
    /**
     * The completion commit Action.
     */
    public String INTEROP_ACTION_COMPLETION_COMMIT = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_COMPLETION_COMMIT ;
    
    /**
     * The completion rollback element.
     */
    public String INTEROP_ELEMENT_COMPLETION_ROLLBACK = "CompletionRollback" ;
    /**
     * The completion rollback QName.
     */
    public QName INTEROP_ELEMENT_QNAME_COMPLETION_ROLLBACK = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_COMPLETION_ROLLBACK, INTEROP_PREFIX) ;
    /**
     * The completion rollback Action.
     */
    public String INTEROP_ACTION_COMPLETION_ROLLBACK = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_COMPLETION_ROLLBACK ;
    
    /**
     * The commit element.
     */
    public String INTEROP_ELEMENT_COMMIT = "Commit" ;
    /**
     * The commit QName.
     */
    public QName INTEROP_ELEMENT_QNAME_COMMIT = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_COMMIT, INTEROP_PREFIX) ;
    /**
     * The commit Action.
     */
    public String INTEROP_ACTION_COMMIT = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_COMMIT ;
    
    /**
     * The rollback element.
     */
    public String INTEROP_ELEMENT_ROLLBACK = "Rollback" ;
    /**
     * The rollback QName.
     */
    public QName INTEROP_ELEMENT_QNAME_ROLLBACK = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_ROLLBACK, INTEROP_PREFIX) ;
    /**
     * The rollback Action.
     */
    public String INTEROP_ACTION_ROLLBACK = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_ROLLBACK ;
    
    /**
     * The phase 2 rollback element.
     */
    public String INTEROP_ELEMENT_PHASE_2_ROLLBACK = "Phase2Rollback" ;
    /**
     * The phase 2 rollback QName.
     */
    public QName INTEROP_ELEMENT_QNAME_PHASE_2_ROLLBACK = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_PHASE_2_ROLLBACK, INTEROP_PREFIX) ;
    /**
     * The phase 2 rollback Action.
     */
    public String INTEROP_ACTION_PHASE_2_ROLLBACK = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_PHASE_2_ROLLBACK ;
    
    /**
     * The readonly element.
     */
    public String INTEROP_ELEMENT_READONLY = "Readonly" ;
    /**
     * The readonly QName.
     */
    public QName INTEROP_ELEMENT_QNAME_READONLY = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_READONLY, INTEROP_PREFIX) ;
    /**
     * The readonly Action.
     */
    public String INTEROP_ACTION_READONLY = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_READONLY ;
    
    /**
     * The volatile and durable element.
     */
    public String INTEROP_ELEMENT_VOLATILE_AND_DURABLE = "VolatileAndDurable" ;
    /**
     * The volatile and durable QName.
     */
    public QName INTEROP_ELEMENT_QNAME_VOLATILE_AND_DURABLE = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_VOLATILE_AND_DURABLE, INTEROP_PREFIX) ;
    /**
     * The volatile and durable Action.
     */
    public String INTEROP_ACTION_VOLATILE_AND_DURABLE = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_VOLATILE_AND_DURABLE ;
    
    /**
     * The early readonly element.
     */
    public String INTEROP_ELEMENT_EARLY_READONLY = "EarlyReadonly" ;
    /**
     * The early readonly QName.
     */
    public QName INTEROP_ELEMENT_QNAME_EARLY_READONLY = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_EARLY_READONLY, INTEROP_PREFIX) ;
    /**
     * The early readonly Action.
     */
    public String INTEROP_ACTION_EARLY_READONLY = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_EARLY_READONLY ;
    
    /**
     * The early aborted element.
     */
    public String INTEROP_ELEMENT_EARLY_ABORTED = "EarlyAborted" ;
    /**
     * The early aborted QName.
     */
    public QName INTEROP_ELEMENT_QNAME_EARLY_ABORTED = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_EARLY_ABORTED, INTEROP_PREFIX) ;
    /**
     * The early aborted Action.
     */
    public String INTEROP_ACTION_EARLY_ABORTED = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_EARLY_ABORTED ;
    
    /**
     * The replay commit element.
     */
    public String INTEROP_ELEMENT_REPLAY_COMMIT = "ReplayCommit" ;
    /**
     * The replay commit QName.
     */
    public QName INTEROP_ELEMENT_QNAME_REPLAY_COMMIT = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_REPLAY_COMMIT, INTEROP_PREFIX) ;
    /**
     * The replay commit Action.
     */
    public String INTEROP_ACTION_REPLAY_COMMIT = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_REPLAY_COMMIT ;
    
    /**
     * The retry prepared commit element.
     */
    public String INTEROP_ELEMENT_RETRY_PREPARED_COMMIT = "RetryPreparedCommit" ;
    /**
     * The retry prepared commit QName.
     */
    public QName INTEROP_ELEMENT_QNAME_RETRY_PREPARED_COMMIT = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_RETRY_PREPARED_COMMIT, INTEROP_PREFIX) ;
    /**
     * The retry prepared commit Action.
     */
    public String INTEROP_ACTION_RETRY_PREPARED_COMMIT = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_RETRY_PREPARED_COMMIT ;
    
    /**
     * The retry prepared abort element.
     */
    public String INTEROP_ELEMENT_RETRY_PREPARED_ABORT = "RetryPreparedAbort" ;
    /**
     * The retry prepared abort QName.
     */
    public QName INTEROP_ELEMENT_QNAME_RETRY_PREPARED_ABORT = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_RETRY_PREPARED_ABORT, INTEROP_PREFIX) ;
    /**
     * The retry prepared abort Action.
     */
    public String INTEROP_ACTION_RETRY_PREPARED_ABORT = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_RETRY_PREPARED_ABORT ;
    
    /**
     * The retry commit element.
     */
    public String INTEROP_ELEMENT_RETRY_COMMIT = "RetryCommit" ;
    /**
     * The retry commit QName.
     */
    public QName INTEROP_ELEMENT_QNAME_RETRY_COMMIT = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_RETRY_COMMIT, INTEROP_PREFIX) ;
    /**
     * The retry commit Action.
     */
    public String INTEROP_ACTION_RETRY_COMMIT = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_RETRY_COMMIT ;
    
    /**
     * The prepared after timeout element.
     */
    public String INTEROP_ELEMENT_PREPARED_AFTER_TIMEOUT = "PreparedAfterTimeout" ;
    /**
     * The prepared after timeout QName.
     */
    public QName INTEROP_ELEMENT_QNAME_PREPARED_AFTER_TIMEOUT = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_PREPARED_AFTER_TIMEOUT, INTEROP_PREFIX) ;
    /**
     * The prepared after timeout Action.
     */
    public String INTEROP_ACTION_PREPARED_AFTER_TIMEOUT = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_PREPARED_AFTER_TIMEOUT ;
    
    /**
     * The lost committed element.
     */
    public String INTEROP_ELEMENT_LOST_COMMITTED = "LostCommitted" ;
    /**
     * The lost committed QName.
     */
    public QName INTEROP_ELEMENT_QNAME_LOST_COMMITTED = new QName(INTEROP_NAMESPACE, INTEROP_ELEMENT_LOST_COMMITTED, INTEROP_PREFIX) ;
    /**
     * The lost committed Action.
     */
    public String INTEROP_ACTION_LOST_COMMITTED = INTEROP_ACTION_PARTICIPANT_PREFIX + INTEROP_ELEMENT_LOST_COMMITTED ;
    
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
