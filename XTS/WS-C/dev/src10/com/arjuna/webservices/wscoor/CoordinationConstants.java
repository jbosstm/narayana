/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.webservices.wscoor;

import javax.xml.namespace.QName;

/**
 * Interface containing WS-Coordination constants.
 */
public interface CoordinationConstants
{
    /**
     * The activation coordinator service name.
     */
    public String SERVICE_ACTIVATION_COORDINATOR = "ActivationCoordinator" ;
    /**
     * The activation requester service name.
     */
    public String SERVICE_ACTIVATION_REQUESTER = "ActivationRequester" ;
    /**
     * The registration coordinator service name.
     */
    public String SERVICE_REGISTRATION_COORDINATOR = "RegistrationCoordinator" ;
    /**
     * The registration requester service name.
     */
    public String SERVICE_REGISTRATION_REQUESTER = "RegistrationRequester" ;
    
    /**
     * The Namespace.
     */
    public String WSCOOR_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/10/wscoor" ;
    /**
     * The namespace prefix.
     */
    public String WSCOOR_PREFIX = "wscoor" ;
    
    /**
     * The Expires element.
     */
    public String WSCOOR_ELEMENT_EXPIRES = "Expires" ;
    /**
     * The Expires QName.
     */
    public QName WSCOOR_ELEMENT_EXPIRES_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_EXPIRES, WSCOOR_PREFIX) ;
    
    /**
     * The Identifier element.
     */
    public String WSCOOR_ELEMENT_IDENTIFIER = "Identifier" ;
    /**
     * The Identifier QName.
     */
    public QName WSCOOR_ELEMENT_IDENTIFIER_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_IDENTIFIER, WSCOOR_PREFIX) ;
    
    /**
     * The CoordinationType element.
     */
    public String WSCOOR_ELEMENT_COORDINATION_TYPE = "CoordinationType" ;
    /**
     * The CoordinationType QName.
     */
    public QName WSCOOR_ELEMENT_COORDINATION_TYPE_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_COORDINATION_TYPE, WSCOOR_PREFIX) ;
    
    /**
     * The RegistrationService element.
     */
    public String WSCOOR_ELEMENT_REGISTRATION_SERVICE = "RegistrationService" ;
    /**
     * The RegistrationService QName.
     */
    public QName WSCOOR_ELEMENT_REGISTRATION_SERVICE_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_REGISTRATION_SERVICE, WSCOOR_PREFIX) ;
    
    /**
     * The CoordinationContext element.
     */
    public String WSCOOR_ELEMENT_COORDINATION_CONTEXT = "CoordinationContext" ;
    /**
     * The CoordinationContext QName.
     */
    public QName WSCOOR_ELEMENT_COORDINATION_CONTEXT_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_COORDINATION_CONTEXT, WSCOOR_PREFIX) ;
    
    /**
     * The CurrentContext element.
     */
    public String WSCOOR_ELEMENT_CURRENT_CONTEXT = "CurrentContext" ;
    /**
     * The CurrentContext QName.
     */
    public QName WSCOOR_ELEMENT_CURRENT_CONTEXT_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_CURRENT_CONTEXT, WSCOOR_PREFIX) ;
    
    /**
     * The CreateCoordinationContext element.
     */
    public String WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT = "CreateCoordinationContext" ;
    /**
     * The CreateCoordinationContext QName.
     */
    public QName WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT, WSCOOR_PREFIX) ;
    /**
     * The CreateCoordinationContext Action.
     */
    public String WSCOOR_ACTION_CREATE_COORDINATION_CONTEXT = WSCOOR_NAMESPACE + "/" + WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT ;
    
    /**
     * The CreateCoordinationContextResponse element.
     */
    public String WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT_RESPONSE = "CreateCoordinationContextResponse" ;
    /**
     * The CreateCoordinationContextResponse QName.
     */
    public QName WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT_RESPONSE_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT_RESPONSE, WSCOOR_PREFIX) ;
    /**
     * The CreateCoordinationContextResponse Action.
     */
    public String WSCOOR_ACTION_CREATE_COORDINATION_CONTEXT_RESPONSE = WSCOOR_NAMESPACE + "/" + WSCOOR_ELEMENT_CREATE_COORDINATION_CONTEXT_RESPONSE ;

    /**
     * The ProtocolIdentifier element.
     */
    public String WSCOOR_ELEMENT_PROTOCOL_IDENTIFIER = "ProtocolIdentifier" ;
    /**
     * The ProtocolIdentifier QName.
     */
    public QName WSCOOR_ELEMENT_PROTOCOL_IDENTIFIER_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_PROTOCOL_IDENTIFIER, WSCOOR_PREFIX) ;
    
    /**
     * The ParticipantProtocolService element.
     */
    public String WSCOOR_ELEMENT_PARTICIPANT_PROTOCOL_SERVICE = "ParticipantProtocolService" ;
    /**
     * The ParticipantProtocolService QName.
     */
    public QName WSCOOR_ELEMENT_PARTICIPANT_PROTOCOL_SERVICE_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_PARTICIPANT_PROTOCOL_SERVICE, WSCOOR_PREFIX) ;
    
    /**
     * The Register element.
     */
    public String WSCOOR_ELEMENT_REGISTER = "Register" ;
    /**
     * The Register QName.
     */
    public QName WSCOOR_ELEMENT_REGISTER_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_REGISTER, WSCOOR_PREFIX) ;
    /**
     * The Register Action.
     */
    public String WSCOOR_ACTION_REGISTER = WSCOOR_NAMESPACE + "/" + WSCOOR_ELEMENT_REGISTER ;
    
    /**
     * The CoordinatorProtocolService element.
     */
    public String WSCOOR_ELEMENT_COORDINATOR_PROTOCOL_SERVICE = "CoordinatorProtocolService" ;
    /**
     * The CoordinatorProtocolService QName.
     */
    public QName WSCOOR_ELEMENT_COORDINATOR_PROTOCOL_SERVICE_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_COORDINATOR_PROTOCOL_SERVICE, WSCOOR_PREFIX) ;
    
    /**
     * The RegisterResponse element.
     */
    public String WSCOOR_ELEMENT_REGISTER_RESPONSE = "RegisterResponse" ;
    /**
     * The RegisterResponse QName.
     */
    public QName WSCOOR_ELEMENT_REGISTER_RESPONSE_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ELEMENT_REGISTER_RESPONSE, WSCOOR_PREFIX) ;
    /**
     * The Register Action.
     */
    public String WSCOOR_ACTION_REGISTER_RESPONSE = WSCOOR_NAMESPACE + "/" + WSCOOR_ELEMENT_REGISTER_RESPONSE ;
    
    /**
     * The Fault Action.
     */
    public String WSCOOR_ACTION_FAULT = WSCOOR_NAMESPACE + "/fault" ;
    /**
     * The AlreadyRegistered error code.
     */
    public String WSCOOR_ERROR_CODE_ALREADY_REGISTERED = "AlreadyRegistered" ;
    /**
     * The AlreadyRegistered error code QName.
     */
    public QName WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ERROR_CODE_ALREADY_REGISTERED, WSCOOR_PREFIX) ;
    /**
     * The ContextRefused error code.
     */
    public String WSCOOR_ERROR_CODE_CONTEXT_REFUSED = "ContextRefused" ;
    /**
     * The ContextRefused error code QName.
     */
    public QName WSCOOR_ERROR_CODE_CONTEXT_REFUSED_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ERROR_CODE_CONTEXT_REFUSED, WSCOOR_PREFIX) ;
    /**
     * The InvalidParameters error code.
     */
    public String WSCOOR_ERROR_CODE_INVALID_PARAMETERS = "InvalidParameters" ;
    /**
     * The InvalidParameters error code QName.
     */
    public QName WSCOOR_ERROR_CODE_INVALID_PARAMETERS_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ERROR_CODE_INVALID_PARAMETERS, WSCOOR_PREFIX) ;
    /**
     * The InvalidProtocol error code.
     */
    public String WSCOOR_ERROR_CODE_INVALID_PROTOCOL = "InvalidProtocol" ;
    /**
     * The InvalidProtocol error code QName.
     */
    public QName WSCOOR_ERROR_CODE_INVALID_PROTOCOL_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ERROR_CODE_INVALID_PROTOCOL, WSCOOR_PREFIX) ;
    /**
     * The InvalidState error code.
     */
    public String WSCOOR_ERROR_CODE_INVALID_STATE = "InvalidState" ;
    /**
     * The InvalidState error code QName.
     */
    public QName WSCOOR_ERROR_CODE_INVALID_STATE_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ERROR_CODE_INVALID_STATE, WSCOOR_PREFIX) ;
    /**
     * The NoActivity error code.
     */
    public String WSCOOR_ERROR_CODE_NO_ACTIVITY = "NoActivity" ;
    /**
     * The NoActivity error code QName.
     */
    public QName WSCOOR_ERROR_CODE_NO_ACTIVITY_QNAME = new QName(WSCOOR_NAMESPACE, WSCOOR_ERROR_CODE_NO_ACTIVITY, WSCOOR_PREFIX) ;
}
