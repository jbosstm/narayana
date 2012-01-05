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
package com.jboss.transaction.txinterop.proxy;

import javax.xml.namespace.QName;

/**
 * constants used in WS Addressing 2005/08
 */
public class AddressingConstants {
    /**
     * The Namespace.
     */
    public static String WSA_NAMESPACE = "http://www.w3.org/2005/08/addressing" ;
    /**
     * The Attribute Namespace.
     */
    public static String WSA_ATTRIBUTE_NAMESPACE = "" ;
    /**
     * The namespace prefix.
     */
    public static String WSA_PREFIX = "wsa" ;
    /**
     * The attribute namespace prefix.
     */
    public static String WSA_ATTRIBUTE_PREFIX = "" ;

    /**
     * The Action element.
     */
    public static String WSA_ELEMENT_ACTION = "Action" ;
    /**
     * The Action QName.
     */
    public static QName WSA_ELEMENT_QNAME_ACTION = new QName(WSA_NAMESPACE, WSA_ELEMENT_ACTION, WSA_PREFIX) ;
    /**
     * The Endpoint Reference element.
     */
    public static String WSA_ELEMENT_ENDPOINT_REFERENCE = "EndpointReference" ;
    /**
     * The Endpoint Reference QName.
     */
    public static QName WSA_ELEMENT_QNAME_ENDPOINT_REFERENCE = new QName(WSA_NAMESPACE, WSA_ELEMENT_ENDPOINT_REFERENCE, WSA_PREFIX) ;
    /**
     * The Fault To element.
     */
    public static String WSA_ELEMENT_FAULT_TO = "FaultTo" ;
    /**
     * The Fault To QName.
     */
    public static QName WSA_ELEMENT_QNAME_FAULT_TO = new QName(WSA_NAMESPACE, WSA_ELEMENT_FAULT_TO, WSA_PREFIX) ;
    /**
     * The From element.
     */
    public static String WSA_ELEMENT_FROM = "From" ;
    /**
     * The From QName.
     */
    public static QName WSA_ELEMENT_QNAME_FROM = new QName(WSA_NAMESPACE, WSA_ELEMENT_FROM, WSA_PREFIX) ;
    /**
     * The Message ID element.
     */
    public static String WSA_ELEMENT_MESSAGE_ID = "MessageID" ;
    /**
     * The Message ID QName.
     */
    public static QName WSA_ELEMENT_QNAME_MESSAGE_ID = new QName(WSA_NAMESPACE, WSA_ELEMENT_MESSAGE_ID, WSA_PREFIX) ;
    /**
     * The Relates To element.
     */
    public static String WSA_ELEMENT_RELATES_TO = "RelatesTo" ;
    /**
     * The Relates To QName.
     */
    public static QName WSA_ELEMENT_QNAME_RELATES_TO = new QName(WSA_NAMESPACE, WSA_ELEMENT_RELATES_TO, WSA_PREFIX) ;
    /**
     * The Reply To element.
     */
    public static String WSA_ELEMENT_REPLY_TO = "ReplyTo" ;
    /**
     * The Reply To QName.
     */
    public static QName WSA_ELEMENT_QNAME_REPLY_TO = new QName(WSA_NAMESPACE, WSA_ELEMENT_REPLY_TO, WSA_PREFIX) ;
    /**
     * The To element.
     */
    public static String WSA_ELEMENT_TO = "To" ;
    /**
     * The To QName.
     */
    public static QName WSA_ELEMENT_QNAME_TO = new QName(WSA_NAMESPACE, WSA_ELEMENT_TO, WSA_PREFIX) ;
    /**
     * The Address element.
     */
    public static String WSA_ELEMENT_ADDRESS = "Address" ;
    /**
     * The Address QName.
     */
    public static QName WSA_ELEMENT_QNAME_ADDRESS = new QName(WSA_NAMESPACE, WSA_ELEMENT_ADDRESS, WSA_PREFIX) ;
    /**
     * The Reference Parameters element.
     */
    public static String WSA_ELEMENT_REFERENCE_PARAMETERS = "ReferenceParameters" ;
    /**
     * The Reference Parameters QName.
     */
    public static QName WSA_ELEMENT_QNAME_REFERENCE_PARAMETERS = new QName(WSA_NAMESPACE, WSA_ELEMENT_REFERENCE_PARAMETERS, WSA_PREFIX) ;
    /**
     * The Metadata element.
     */
    public static String WSA_ELEMENT_METADATA = "Metadata" ;
    /**
     * The Metadata QName.
     */
    public static QName WSA_ELEMENT_QNAME_METADATA = new QName(WSA_NAMESPACE, WSA_ELEMENT_METADATA, WSA_PREFIX) ;

    /**
     * The Relationship Type attribute.
     */
    public static String WSA_ATTRIBUTE_RELATIONSHIP_TYPE = "RelationshipType" ;
    /**
     * The Relationship Type QName.
     */
    public static QName WSA_ATTRIBUTE_QNAME_RELATIONSHIP_TYPE = new QName(WSA_ATTRIBUTE_NAMESPACE, WSA_ATTRIBUTE_RELATIONSHIP_TYPE, WSA_ATTRIBUTE_PREFIX) ;

    /**
     * The value of the Action element for faults.
     */
    public static String WSA_ACTION_FAULT = WSA_NAMESPACE + "/fault" ;
    /**
     * The value of the Action element for SOAP faults.
     */
    public static String WSA_ACTION_SOAP_FAULT = WSA_NAMESPACE + "/soap/fault" ;

    /**
     * The unspecified Message ID for use in relationships.
     * KEV check
     */
    public static String WSA_MESSAGE_ID_UNSPECIFIED = WSA_NAMESPACE + "/unspecified" ;

    /**
     * The address URI representing an anonymous endpoint.
     */
    public static String WSA_ADDRESS_ANONYMOUS = WSA_NAMESPACE + "/anonymous" ;
    /**
     * The address URI representing the none endpoint.
     */
    public static String WSA_ADDRESS_NONE = WSA_NAMESPACE + "/none" ;

    /**
     * The relates to reply URI.
     */
    public static String WSA_RELATES_TO_REPLY = WSA_NAMESPACE + "/reply" ;

    /**
     * The Is Reference Parameter attribute.
     */
    public static String WSA_ATTRIBUTE_IS_REFERENCE_PARAMETER = "IsReferenceParameter" ;
    /**
     * The Is Reference Parameter QName.
     */
    public static QName WSA_ATTRIBUTE_QNAME_IS_REFERENCE_PARAMETER = new QName(WSA_NAMESPACE, WSA_ATTRIBUTE_IS_REFERENCE_PARAMETER, WSA_PREFIX) ;

    /**
     * The Fault Detail element.
     */
    public static String WSA_ELEMENT_FAULT_DETAIL = "FaultDetail" ;
    /**
     * The Fault Detail QName.
     */
    public static QName WSA_ELEMENT_QNAME_FAULT_DETAIL = new QName(WSA_NAMESPACE, WSA_ELEMENT_FAULT_DETAIL, WSA_PREFIX) ;

    /**
     * The Invalid Addressing Header fault code.
     */
    public static String WSA_FAULT_CODE_INVALID_ADDRESSING_HEADER = "InvalidAddressingHeader" ;
    /**
     * The Invalid Addressing Header fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_INVALID_ADDRESSING_HEADER = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_INVALID_ADDRESSING_HEADER, WSA_PREFIX) ;
    /**
     * The Invalid Address fault code.
     */
    public static String WSA_FAULT_CODE_INVALID_ADDRESS = "InvalidAddress" ;
    /**
     * The Invalid Address fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_INVALID_ADDRESS = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_INVALID_ADDRESS, WSA_PREFIX) ;
    /**
     * The Invalid EPR fault code.
     */
    public static String WSA_FAULT_CODE_INVALID_EPR = "InvalidEPR" ;
    /**
     * The Invalid EPR fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_INVALID_EPR = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_INVALID_EPR, WSA_PREFIX) ;
    /**
     * The Invalid Cardinality fault code.
     */
    public static String WSA_FAULT_CODE_INVALID_CARDINALITY = "InvalidCardinality" ;
    /**
     * The Invalid Cardinality fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_INVALID_CARDINALITY = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_INVALID_CARDINALITY, WSA_PREFIX) ;
    /**
     * The Missing Address In EPR fault code.
     */
    public static String WSA_FAULT_CODE_MISSING_ADDRESS_IN_EPR = "MissingAddressInEPR" ;
    /**
     * The Missing Address In EPR fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_MISSING_ADDRESS_IN_EPR = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_MISSING_ADDRESS_IN_EPR, WSA_PREFIX) ;
    /**
     * The Duplicate Message ID fault code.
     */
    public static String WSA_FAULT_CODE_DUPLICATE_MESSAGE_ID = "DuplicateMessageID" ;
    /**
     * The Duplicate Message ID fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_DUPLICATE_MESSAGE_ID = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_DUPLICATE_MESSAGE_ID, WSA_PREFIX) ;
    /**
     * The Action Mismatch fault code.
     */
    public static String WSA_FAULT_CODE_ACTION_MISMATCH = "ActionMismatch" ;
    /**
     * The Action Mismatch fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_ACTION_MISMATCH = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_ACTION_MISMATCH, WSA_PREFIX) ;
    /**
     * The Message Addressing Header Required fault code.
     */
    public static String WSA_FAULT_CODE_MESSAGING_ADDRESSING_HEADER_REQUIRED = "MessageAddressingHeaderRequired" ;
    /**
     * The Message Addressing Header Required fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_MESSAGING_ADDRESSING_HEADER_REQUIRED = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_MESSAGING_ADDRESSING_HEADER_REQUIRED, WSA_PREFIX) ;
    /**
     * The Destination Unreachable fault code.
     */
    public static String WSA_FAULT_CODE_DESTINATION_UNREACHABLE = "DestinationUnreachable" ;
    /**
     * The Destination Unreachable fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_DESTINATION_UNREACHABLE = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_DESTINATION_UNREACHABLE, WSA_PREFIX) ;
    /**
     * The Action Not Supported fault code.
     */
    public static String WSA_FAULT_CODE_ACTION_NOT_SUPPORTED = "ActionNotSupported" ;
    /**
     * The Action Not Supported fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_ACTION_NOT_SUPPORTED = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_ACTION_NOT_SUPPORTED, WSA_PREFIX) ;
    /**
     * The Endpoint Unavailable fault code.
     */
    public static String WSA_FAULT_CODE_ENDPOINT_UNAVAILABLE = "EndpointUnavailable" ;
    /**
     * The Endpoint Unavailable fault code QName.
     */
    public static QName WSA_FAULT_CODE_QNAME_ENDPOINT_UNAVAILABLE = new QName(WSA_NAMESPACE, WSA_FAULT_CODE_ENDPOINT_UNAVAILABLE, WSA_PREFIX) ;

    /**
     * The Problem Header element.
     */
    public static String WSA_ELEMENT_PROBLEM_HEADER = "ProblemHeader" ;
    /**
     * The Problem Header QName.
     */
    public static QName WSA_ELEMENT_QNAME_PROBLEM_HEADER = new QName(WSA_NAMESPACE, WSA_ELEMENT_PROBLEM_HEADER, WSA_PREFIX) ;
    /**
     * The Problem Header QName element.
     */
    public static String WSA_ELEMENT_PROBLEM_HEADER_QNAME = "ProblemHeaderQName" ;
    /**
     * The Problem Header QName QName.
     */
    public static QName WSA_ELEMENT_QNAME_PROBLEM_HEADER_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_PROBLEM_HEADER_QNAME, WSA_PREFIX) ;
    /**
     * The Problem IRI element.
     */
    public static String WSA_ELEMENT_PROBLEM_IRI = "ProblemIRI" ;
    /**
     * The Problem IRI QName.
     */
    public static QName WSA_ELEMENT_QNAME_PROBLEM_IRI = new QName(WSA_NAMESPACE, WSA_ELEMENT_PROBLEM_IRI, WSA_PREFIX) ;
    /**
     * The Problem Action element.
     */
    public static String WSA_ELEMENT_PROBLEM_ACTION = "ProblemAction" ;
    /**
     * The Problem Action QName.
     */
    public static QName WSA_ELEMENT_QNAME_PROBLEM_ACTION = new QName(WSA_NAMESPACE, WSA_ELEMENT_PROBLEM_ACTION, WSA_PREFIX) ;
    /**
     * The Soap Action element.
     */
    public static String WSA_ELEMENT_SOAP_ACTION = "SoapAction" ;
    /**
     * The Soap Action QName.
     */
    public static QName WSA_ELEMENT_QNAME_SOAP_ACTION = new QName(WSA_NAMESPACE, WSA_ELEMENT_SOAP_ACTION, WSA_PREFIX) ;
    /**
     * The Retry After element.
     */
    public static String WSA_ELEMENT_RETRY_AFTER = "RetryAfter" ;
    /**
     * The Retry After QName.
     */
    public static QName WSA_ELEMENT_QNAME_RETRY_AFTER = new QName(WSA_NAMESPACE, WSA_ELEMENT_METADATA, WSA_PREFIX) ;
}
