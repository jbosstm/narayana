package com.arjuna.webservices11.wsat;

import javax.xml.namespace.QName;

/**
 * Interface containing WS-AtomicTransaction constants.
 */
public interface AtomicTransactionConstants
{
    /**
     * The Namespace.
     */
    public String WSAT_NAMESPACE = "http://docs.oasis-open.org/ws-tx/wsat/2006/06";
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
     * The completion coordinator service name.
     */
    public String COMPLETION_COORDINATOR_SERVICE_NAME = "CompletionCoordinatorService" ;
    /**
     * The completion coordinator service qname.
     */
    public QName COMPLETION_COORDINATOR_SERVICE_QNAME = new QName(WSAT_NAMESPACE, COMPLETION_COORDINATOR_SERVICE_NAME, WSAT_PREFIX);

     /**
     * The WSAT Completion Cordinator Port Name.
     */
    public String COMPLETION_COORDINATOR_PORT_NAME = "CompletionCoordinatorPortType";
    /**
     * The WSAT Completion Cordinator Port QName.
     */
    public QName COMPLETION_COORDINATOR_PORT_QNAME = new QName(WSAT_NAMESPACE, COMPLETION_COORDINATOR_PORT_NAME, WSAT_PREFIX);

    /**
      * The completion initiator service name.
      */
     public String COMPLETION_INITIATOR_SERVICE_NAME = "CompletionInitiatorService" ;
     /**
      * The completion initiator service qname.
      */
     public QName COMPLETION_INITIATOR_SERVICE_QNAME = new QName(WSAT_NAMESPACE, COMPLETION_INITIATOR_SERVICE_NAME, WSAT_PREFIX);
    /**
     * The WSAT Completion Initiator Port Name.
     */
    public String COMPLETION_INITIATOR_PORT_NAME = "CompletionInitiatorPortType";
    /**
     * The WSAT Completion Initiator Port QName.
     */
    public QName COMPLETION_INITIATOR_PORT_QNAME = new QName(WSAT_NAMESPACE, COMPLETION_INITIATOR_PORT_NAME, WSAT_PREFIX);

    /**
     * The coordinator service name.
     */
    public String COORDINATOR_SERVICE_NAME = "CoordinatorService" ;
    /**
     * The coordinator service qname.
     */
    public QName COORDINATOR_SERVICE_QNAME = new QName(WSAT_NAMESPACE, COORDINATOR_SERVICE_NAME, WSAT_PREFIX);
    /**
     * The WSAT Cordinator Port Name.
     */
    public String COORDINATOR_PORT_NAME = "CoordinatorPortType";
    /**
     * The WSAT Cordinator Port QName.
     */
    public QName COORDINATOR_PORT_QNAME = new QName(WSAT_NAMESPACE, COORDINATOR_PORT_NAME, WSAT_PREFIX);

    /**
     * The participant service name.
     */
    public String PARTICIPANT_SERVICE_NAME = "ParticipantService" ;
    /**
     * The participant service qname.
     */
    public QName PARTICIPANT_SERVICE_QNAME = new QName(WSAT_NAMESPACE, PARTICIPANT_SERVICE_NAME, WSAT_PREFIX);
    /**
     * The WSAT Participant Port Name.
     */
    public String PARTICIPANT_PORT_NAME = "ParticipantPortType";
    /**
     * The WSAT Participant Port QName.
     */
    public QName PARTICIPANT_PORT_QNAME = new QName(WSAT_NAMESPACE, PARTICIPANT_PORT_NAME, WSAT_PREFIX);

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
     * The Fault Action.
     */
    public String WSAT_ACTION_FAULT = WSAT_NAMESPACE + "/fault" ;

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

    /**
     * The UnknownTransaction error code.
     */
    public String WSAT_ERROR_CODE_UNKNOWN_TRANSACTION = "UnknownTransaction" ;
    /**
     * The UnknownTransaction error code QName.
     */
    public QName WSAT_ERROR_CODE_UNKNOWN_TRANSACTION_QNAME = new QName(WSAT_NAMESPACE, WSAT_ERROR_CODE_UNKNOWN_TRANSACTION, WSAT_PREFIX) ;

    /**
     * The atomic transaction protocol.
     */
    public String WSAT_PROTOCOL = WSAT_NAMESPACE ;
    /**
     * The atomic transaction completion protocol.
     */
    public String WSAT_SUB_PROTOCOL_COMPLETION = WSAT_NAMESPACE + "/Completion" ;
    /**
     * The atomic transaction durable 2PC protocol.
     */
    public String WSAT_SUB_PROTOCOL_DURABLE_2PC = WSAT_NAMESPACE + "/Durable2PC" ;
    /**
     * The atomic transaction volatile 2PC protocol.
     */
    public String WSAT_SUB_PROTOCOL_VOLATILE_2PC = WSAT_NAMESPACE + "/Volatile2PC" ;
}
