/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
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
package com.arjuna.webservices.wsat;

import javax.xml.namespace.QName;

/**
 * Interface containing WS-AtomicTransaction constants.
 */
public interface AtomicTransactionConstants
{
    /**
     * The completion coordinator service name.
     */
    public String SERVICE_COMPLETION_COORDINATOR = "ATCompletionCoordinator" ;
    /**
     * The completion initiator service name.
     */
    public String SERVICE_COMPLETION_INITIATOR = "ATCompletionInitiator" ;
    /**
     * The coordinator service name.
     */
    public String SERVICE_COORDINATOR = "ATCoordinator" ;
    /**
     * The participant service name.
     */
    public String SERVICE_PARTICIPANT = "ATParticipant" ;
    /**
     * The Namespace.
     */
    public String WSAT_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/10/wsat" ;
    /**
     * The Attribute Namespace.
     */
    public String WSAT_ATTRIBUTE_NAMESPACE = "" ;
    /**
     * The namespace prefix.
     */
    public String WSAT_PREFIX = "wsat" ;
    /**
     * The attribute namespace prefix.
     */
    public String WSAT_ATTRIBUTE_PREFIX = "" ;
    
    /**
     * The Prepare element.
     */
    public String WSAT_ELEMENT_PREPARE = "Prepare" ;
    /**
     * The Prepare QName.
     */
    public QName WSAT_ELEMENT_PREPARE_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_PREPARE, WSAT_PREFIX) ;
    /**
     * The Prepare Action.
     */
    public String WSAT_ACTION_PREPARE = WSAT_NAMESPACE + "/" + WSAT_ELEMENT_PREPARE ;
    /**
     * The Prepared element.
     */
    public String WSAT_ELEMENT_PREPARED = "Prepared" ;
    /**
     * The Prepared QName.
     */
    public QName WSAT_ELEMENT_PREPARED_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_PREPARED, WSAT_PREFIX) ;
    /**
     * The Prepared Action.
     */
    public String WSAT_ACTION_PREPARED = WSAT_NAMESPACE + "/" + WSAT_ELEMENT_PREPARED ;
    /**
     * The Aborted element.
     */
    public String WSAT_ELEMENT_ABORTED = "Aborted" ;
    /**
     * The Aborted QName.
     */
    public QName WSAT_ELEMENT_ABORTED_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_ABORTED, WSAT_PREFIX) ;
    /**
     * The Aborted Action.
     */
    public String WSAT_ACTION_ABORTED = WSAT_NAMESPACE + "/" + WSAT_ELEMENT_ABORTED ;
    /**
     * The ReadOnly element.
     */
    public String WSAT_ELEMENT_READ_ONLY = "ReadOnly" ;
    /**
     * The ReadOnly QName.
     */
    public QName WSAT_ELEMENT_READ_ONLY_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_READ_ONLY, WSAT_PREFIX) ;
    /**
     * The ReadOnly Action.
     */
    public String WSAT_ACTION_READ_ONLY = WSAT_NAMESPACE + "/" + WSAT_ELEMENT_READ_ONLY ;
    /**
     * The Commit element.
     */
    public String WSAT_ELEMENT_COMMIT = "Commit" ;
    /**
     * The Commit QName.
     */
    public QName WSAT_ELEMENT_COMMIT_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_COMMIT, WSAT_PREFIX) ;
    /**
     * The Commit Action.
     */
    public String WSAT_ACTION_COMMIT = WSAT_NAMESPACE + "/" + WSAT_ELEMENT_COMMIT ;
    /**
     * The Rollback element.
     */
    public String WSAT_ELEMENT_ROLLBACK = "Rollback" ;
    /**
     * The Rollback QName.
     */
    public QName WSAT_ELEMENT_ROLLBACK_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_ROLLBACK, WSAT_PREFIX) ;
    /**
     * The Rollback Action.
     */
    public String WSAT_ACTION_ROLLBACK = WSAT_NAMESPACE + "/" + WSAT_ELEMENT_ROLLBACK ;
    /**
     * The Committed element.
     */
    public String WSAT_ELEMENT_COMMITTED = "Committed" ;
    /**
     * The Committed QName.
     */
    public QName WSAT_ELEMENT_COMMITTED_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_COMMITTED, WSAT_PREFIX) ;
    /**
     * The Committed Action.
     */
    public String WSAT_ACTION_COMMITTED = WSAT_NAMESPACE + "/" + WSAT_ELEMENT_COMMITTED ;
    /**
     * The Replay element.
     */
    public String WSAT_ELEMENT_REPLAY = "Replay" ;
    /**
     * The Replay QName.
     */
    public QName WSAT_ELEMENT_REPLAY_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_REPLAY, WSAT_PREFIX) ;
    /**
     * The Replay Action.
     */
    public String WSAT_ACTION_REPLAY = WSAT_NAMESPACE + "/" + WSAT_ELEMENT_REPLAY ;
    /**
     * The PrepareResponse element.
     */
    public String WSAT_ELEMENT_PREPARE_RESPONSE = "PrepareResponse" ;
    /**
     * The PrepareResponse QName.
     */
    public QName WSAT_ELEMENT_PREPARE_RESPONSE_QNAME = new QName(WSAT_NAMESPACE, WSAT_ELEMENT_PREPARE_RESPONSE, WSAT_PREFIX) ;
    /**
     * The Fault Action.
     */
    public String WSAT_ACTION_FAULT = WSAT_NAMESPACE + "/fault" ;
    /**
     * The SOAP Fault Action.
     */
    public String WSAT_ACTION_SOAP_FAULT = WSAT_NAMESPACE + "/soapFault" ;
    
    /**
     * The vote attribute.
     */
    public String WSAT_ATTRIBUTE_VOTE = "vote" ;
    /**
     * The vote QName.
     */
    public QName WSAT_ATTRIBUTE_VOTE_QNAME = new QName(WSAT_ATTRIBUTE_NAMESPACE, WSAT_ATTRIBUTE_VOTE, WSAT_ATTRIBUTE_PREFIX) ;
    /**
     * The outcome attribute.
     */
    public String WSAT_ATTRIBUTE_OUTCOME = "outcome" ;
    /**
     * The outcome QName.
     */
    public QName WSAT_ATTRIBUTE_OUTCOME_QNAME = new QName(WSAT_ATTRIBUTE_NAMESPACE, WSAT_ATTRIBUTE_OUTCOME, WSAT_ATTRIBUTE_PREFIX) ;
    
    /**
     * The InconsistentInternalState error code.
     */
    public String WSAT_ERROR_CODE_INCONSISTENT_INTERNAL_STATE = "InconsistentInternalState" ;
    /**
     * The InconsistentInternalState error code QName.
     */
    public QName WSAT_ERROR_CODE_INCONSISTENT_INTERNAL_STATE_QNAME = new QName(WSAT_NAMESPACE, WSAT_ERROR_CODE_INCONSISTENT_INTERNAL_STATE, WSAT_PREFIX) ;
}
