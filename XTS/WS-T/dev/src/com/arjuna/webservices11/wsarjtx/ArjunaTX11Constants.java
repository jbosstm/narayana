/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsarjtx;

import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;

import javax.xml.namespace.QName;

/**
 * Interface containing Arjuna WS constants.
 */
public interface ArjunaTX11Constants
{

    /**
     * The termination participant service name.
     */
    public String TERMINATION_PARTICIPANT_SERVICE_NAME = "TerminationParticipantService" ;
    /**
     * The termination participant service qname.
     */
    public QName TERMINATION_PARTICIPANT_SERVICE_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_PARTICIPANT_SERVICE_NAME, ArjunaTXConstants.WSARJTX_PREFIX) ;

    /**
     * The termination participant soap service port name
     */
     public String TERMINATION_PARTICIPANT_PORT_NAME = "TerminationParticipantPortType";
    /**
     * The termination participant soap service port qname
     */
     public QName TERMINATION_PARTICIPANT_PORT_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_PARTICIPANT_PORT_NAME, ArjunaTXConstants.WSARJTX_PREFIX);

    /**
     * The termination coordinator service name.
     */
    public String TERMINATION_COORDINATOR_SERVICE_NAME = "TerminationCoordinatorService" ;
    /**
     * The termination coordinator service qname.
     */
    public QName TERMINATION_COORDINATOR_SERVICE_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_COORDINATOR_SERVICE_NAME, ArjunaTXConstants.WSARJTX_PREFIX) ;

    /**
     * The termination coordinator soap service port name
     */
     public String TERMINATION_COORDINATOR_PORT_NAME = "TerminationCoordinatorPortType";
    /**
     * The termination coordinator soap service port qname
     */
     public QName TERMINATION_COORDINATOR_PORT_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_COORDINATOR_PORT_NAME, ArjunaTXConstants.WSARJTX_PREFIX);

    /**
     * The termination coordinator service name.
     */
    public String TERMINATION_COORDINATOR_RPC_SERVICE_NAME = "TerminationCoordinatorRPCService" ;
    /**
     * The termination coordinator service qname.
     */
    public QName TERMINATION_COORDINATOR_RPC_SERVICE_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_COORDINATOR_RPC_SERVICE_NAME, ArjunaTXConstants.WSARJTX_PREFIX) ;

    /**
     * The termination coordinator soap service port name
     */
     public String TERMINATION_COORDINATOR_RPC_PORT_NAME = "TerminationCoordinatorRPCPortType";
    /**
     * The termination coordinator soap service port qname
     */
     public QName TERMINATION_COORDINATOR_RPC_PORT_QNAME = new QName(ArjunaTXConstants.WSARJTX_NAMESPACE, TERMINATION_COORDINATOR_RPC_PORT_NAME, ArjunaTXConstants.WSARJTX_PREFIX);
}