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
package com.arjuna.webservices.wsarjtx;

import javax.xml.namespace.QName;

/**
 * Interface containing Arjuna WS constants.
 */
public interface ArjunaTXConstants
{
    /**
     * The termination coordinator service name.
     */
    public String SERVICE_TERMINATION_COORDINATOR = "TerminationCoordinator" ;
    /**
     * The termination participant service name.
     */
    public String SERVICE_TERMINATION_PARTICIPANT = "TerminationParticipant" ;
    
    /**
     * The Namespace.
     */
    public String WSARJTX_NAMESPACE = "http://schemas.arjuna.com/ws/2005/10/wsarjtx" ;
    /**
     * The Attribute Namespace.
     */
    public String WSARJTX_ATTRIBUTE_NAMESPACE = "" ;
    /**
     * The namespace prefix.
     */
    public String WSARJTX_PREFIX = "wsarjtx" ;
    /**
     * The attribute namespace prefix.
     */
    public String WSARJTX_ATTRIBUTE_PREFIX = "" ;

    /**
     * The TerminationService element.
     */
    public String WSARJTX_ELEMENT_TERMINATION_SERVICE = "TerminationService" ;
    /**
     * The TerminationService QName.
     */
    public QName WSARJTX_ELEMENT_TERMINATION_SERVICE_QNAME = new QName(WSARJTX_NAMESPACE, WSARJTX_ELEMENT_TERMINATION_SERVICE, WSARJTX_PREFIX) ;

    /**
     * The Complete element.
     */
    public String WSARJTX_ELEMENT_COMPLETE = "Complete" ;
    /**
     * The Complete QName.
     */
    public QName WSARJTX_ELEMENT_COMPLETE_QNAME = new QName(WSARJTX_NAMESPACE, WSARJTX_ELEMENT_COMPLETE, WSARJTX_PREFIX) ;
    /**
     * The Complete Action.
     */
    public String WSARJTX_ACTION_COMPLETE = WSARJTX_NAMESPACE + "/" + WSARJTX_ELEMENT_COMPLETE ;
    /**
     * The Completed element.
     */
    public String WSARJTX_ELEMENT_COMPLETED = "Completed" ;
    /**
     * The Completed QName.
     */
    public QName WSARJTX_ELEMENT_COMPLETED_QNAME = new QName(WSARJTX_NAMESPACE, WSARJTX_ELEMENT_COMPLETED, WSARJTX_PREFIX) ;
    /**
     * The Completed Action.
     */
    public String WSARJTX_ACTION_COMPLETED = WSARJTX_NAMESPACE + "/" + WSARJTX_ELEMENT_COMPLETED ;
    /**
     * The Faulted element.
     */
    public String WSARJTX_ELEMENT_FAULTED = "Faulted" ;
    /**
     * The Faulted QName.
     */
    public QName WSARJTX_ELEMENT_FAULTED_QNAME = new QName(WSARJTX_NAMESPACE, WSARJTX_ELEMENT_FAULTED, WSARJTX_PREFIX) ;
    /**
     * The Faulted Action.
     */
    public String WSARJTX_ACTION_FAULTED = WSARJTX_NAMESPACE + "/" + WSARJTX_ELEMENT_FAULTED ;
    /**
     * The Close element.
     */
    public String WSARJTX_ELEMENT_CLOSE = "Close" ;
    /**
     * The Close QName.
     */
    public QName WSARJTX_ELEMENT_CLOSE_QNAME = new QName(WSARJTX_NAMESPACE, WSARJTX_ELEMENT_CLOSE, WSARJTX_PREFIX) ;
    /**
     * The Close Action.
     */
    public String WSARJTX_ACTION_CLOSE = WSARJTX_NAMESPACE + "/" + WSARJTX_ELEMENT_CLOSE ;
    /**
     * The Closed element.
     */
    public String WSARJTX_ELEMENT_CLOSED = "Closed" ;
    /**
     * The Closed QName.
     */
    public QName WSARJTX_ELEMENT_CLOSED_QNAME = new QName(WSARJTX_NAMESPACE, WSARJTX_ELEMENT_CLOSED, WSARJTX_PREFIX) ;
    /**
     * The Closed Action.
     */
    public String WSARJTX_ACTION_CLOSED = WSARJTX_NAMESPACE + "/" + WSARJTX_ELEMENT_CLOSED ;
    /**
     * The Cancel element.
     */
    public String WSARJTX_ELEMENT_CANCEL = "Cancel" ;
    /**
     * The Cancel QName.
     */
    public QName WSARJTX_ELEMENT_CANCEL_QNAME = new QName(WSARJTX_NAMESPACE, WSARJTX_ELEMENT_CANCEL, WSARJTX_PREFIX) ;
    /**
     * The Cancel Action.
     */
    public String WSARJTX_ACTION_CANCEL = WSARJTX_NAMESPACE + "/" + WSARJTX_ELEMENT_CANCEL ;
    /**
     * The Cancelled element.
     */
    public String WSARJTX_ELEMENT_CANCELLED = "Cancelled" ;
    /**
     * The Cancelled QName.
     */
    public QName WSARJTX_ELEMENT_CANCELLED_QNAME = new QName(WSARJTX_NAMESPACE, WSARJTX_ELEMENT_CANCELLED, WSARJTX_PREFIX) ;
    /**
     * The Cancelled Action.
     */
    public String WSARJTX_ACTION_CANCELLED = WSARJTX_NAMESPACE + "/" + WSARJTX_ELEMENT_CANCELLED ;
    
    /**
     * The SOAP Fault Action.
     */
    public String WSARJTX_ACTION_SOAP_FAULT = WSARJTX_NAMESPACE + "/soapFault" ;

    
    /**
     * The unknown transaction error code.
     */
    public static final String UNKNOWNTRANSACTION_ERROR_CODE = "UnknownTransaction";
    /**
     * The unknown transaction error code.
     */
    public static final QName  UNKNOWNTRANSACTION_ERROR_CODE_QNAME = new QName(WSARJTX_NAMESPACE, UNKNOWNTRANSACTION_ERROR_CODE, WSARJTX_PREFIX);
    /**
     * The transaction rolled back error code.
     */
    public static final String TRANSACTIONROLLEDBACK_ERROR_CODE = "TransactionRolledBack";
    /**
     * The transaction rolled back error code.
     */
    public static final QName  TRANSACTIONROLLEDBACK_ERROR_CODE_QNAME = new QName(WSARJTX_NAMESPACE, TRANSACTIONROLLEDBACK_ERROR_CODE, WSARJTX_PREFIX);
    /**
     * The wrong state error code.
     */
    public static final String WRONGSTATE_ERROR_CODE         = "WrongState";
    /**
     * The wrong state error code.
     */
    public static final QName  WRONGSTATE_ERROR_CODE_QNAME = new QName(WSARJTX_NAMESPACE, WRONGSTATE_ERROR_CODE, WSARJTX_PREFIX);
    /**
     * The unknown error error code.
     */
    public static final String UNKNOWNERROR_ERROR_CODE       = "UnknownError";
    /**
     * The unknown error error code.
     */
    public static final QName  UNKNOWNERROR_ERROR_CODE_QNAME = new QName(WSARJTX_NAMESPACE, UNKNOWNERROR_ERROR_CODE, WSARJTX_PREFIX);
    
    /**
     * The termination protocol.
     */
    public String WSARJTX_PROTOCOL_TERMINATION = WSARJTX_NAMESPACE + "/BATermination";
}
