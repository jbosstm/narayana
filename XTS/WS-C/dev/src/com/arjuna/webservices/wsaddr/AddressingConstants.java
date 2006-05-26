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
package com.arjuna.webservices.wsaddr;

import javax.xml.namespace.QName;

/**
 * Interface containing WS-Addressing constants.
 */
public interface AddressingConstants
{
    /**
     * The Namespace.
     */
    public String WSA_NAMESPACE = "http://schemas.xmlsoap.org/ws/2004/08/addressing" ;
    /**
     * The Attribute Namespace.
     */
    public String WSA_ATTRIBUTE_NAMESPACE = "" ;
    /**
     * The namespace prefix.
     */
    public String WSA_PREFIX = "wsa" ;
    /**
     * The attribute namespace prefix.
     */
    public String WSA_ATTRIBUTE_PREFIX = "" ;
    
    /**
     * The Action element.
     */
    public String WSA_ELEMENT_ACTION = "Action" ;
    /**
     * The Action QName.
     */
    public QName WSA_ELEMENT_ACTION_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_ACTION, WSA_PREFIX) ;
    /**
     * The Endpoint Reference element.
     */
    public String WSA_ELEMENT_ENDPOINT_REFERENCE = "EndpointReference" ;
    /**
     * The Endpoint Reference QName.
     */
    public QName WSA_ELEMENT_ENDPOINT_REFERENCE_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_ENDPOINT_REFERENCE, WSA_PREFIX) ;
    /**
     * The Fault To element.
     */
    public String WSA_ELEMENT_FAULT_TO = "FaultTo" ;
    /**
     * The Fault To QName.
     */
    public QName WSA_ELEMENT_FAULT_TO_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_FAULT_TO, WSA_PREFIX) ;
    /**
     * The From element.
     */
    public String WSA_ELEMENT_FROM = "From" ;
    /**
     * The From QName.
     */
    public QName WSA_ELEMENT_FROM_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_FROM, WSA_PREFIX) ;
    /**
     * The Message ID element.
     */
    public String WSA_ELEMENT_MESSAGE_ID = "MessageID" ;
    /**
     * The Message ID QName.
     */
    public QName WSA_ELEMENT_MESSAGE_ID_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_MESSAGE_ID, WSA_PREFIX) ;
    /**
     * The Relates To element.
     */
    public String WSA_ELEMENT_RELATES_TO = "RelatesTo" ;
    /**
     * The Relates To QName.
     */
    public QName WSA_ELEMENT_RELATES_TO_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_RELATES_TO, WSA_PREFIX) ;
    /**
     * The Reply To element.
     */
    public String WSA_ELEMENT_REPLY_TO = "ReplyTo" ;
    /**
     * The Reply To QName.
     */
    public QName WSA_ELEMENT_REPLY_TO_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_REPLY_TO, WSA_PREFIX) ;
    /**
     * The To element.
     */
    public String WSA_ELEMENT_TO = "To" ;
    /**
     * The To QName.
     */
    public QName WSA_ELEMENT_TO_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_TO, WSA_PREFIX) ;
    /**
     * The Address element.
     */
    public String WSA_ELEMENT_ADDRESS = "Address" ;
    /**
     * The Address QName.
     */
    public QName WSA_ELEMENT_ADDRESS_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_ADDRESS, WSA_PREFIX) ;
    /**
     * The Reference Properties element.
     */
    public String WSA_ELEMENT_REFERENCE_PROPERTIES = "ReferenceProperties" ;
    /**
     * The Reference Properties QName.
     */
    public QName WSA_ELEMENT_REFERENCE_PROPERTIES_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_REFERENCE_PROPERTIES, WSA_PREFIX) ;
    /**
     * The Reference Parameters element.
     */
    public String WSA_ELEMENT_REFERENCE_PARAMETERS = "ReferenceParameters" ;
    /**
     * The Reference Parameters QName.
     */
    public QName WSA_ELEMENT_REFERENCE_PARAMETERS_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_REFERENCE_PARAMETERS, WSA_PREFIX) ;
    /**
     * The Port Type element.
     */
    public String WSA_ELEMENT_PORT_TYPE = "PortType" ;
    /**
     * The Port Type QName.
     */
    public QName WSA_ELEMENT_PORT_TYPE_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_PORT_TYPE, WSA_PREFIX) ;
    /**
     * The Service Name element.
     */
    public String WSA_ELEMENT_SERVICE_NAME = "ServiceName" ;
    /**
     * The Service Name QName.
     */
    public QName WSA_ELEMENT_SERVICE_NAME_QNAME = new QName(WSA_NAMESPACE, WSA_ELEMENT_SERVICE_NAME, WSA_PREFIX) ;
    
    /**
     * The Relationship Type attribute.
     */
    public String WSA_ATTRIBUTE_RELATIONSHIP_TYPE = "RelationshipType" ;
    /**
     * The Relationship Type QName.
     */
    public QName WSA_ATTRIBUTE_RELATIONSHIP_TYPE_QNAME = new QName(WSA_ATTRIBUTE_NAMESPACE, WSA_ATTRIBUTE_RELATIONSHIP_TYPE, WSA_ATTRIBUTE_PREFIX) ;
    /**
     * The Port Name attribute.
     */
    public String WSA_ATTRIBUTE_PORT_NAME = "PortName" ;
    /**
     * The Port Name QName.
     */
    public QName WSA_ATTRIBUTE_PORT_NAME_QNAME = new QName(WSA_ATTRIBUTE_NAMESPACE, WSA_ATTRIBUTE_PORT_NAME, WSA_ATTRIBUTE_PREFIX) ;
    
    /**
     * The value of the Action element for faults.
     */
    public String WSA_ACTION_FAULT = WSA_NAMESPACE + "/fault" ;

    /**
     * The unspecified Message ID for use in relationships. 
     */
    public String WSA_MESSAGE_ID_UNSPECIFIED = WSA_NAMESPACE + "/id/unspecified" ;
    
    /**
     * The addres URI representing an anonymous endpoint.
     */
    public String WSA_ADDRESS_ANONYMOUS = WSA_NAMESPACE + "/role/anonymous" ;
    
    /**
     * The reply relates to local name.
     */
    public String WSA_RELATES_TO_REPLY = "Reply" ;
    /**
     * The reply RelatesTo QName.
     */
    public QName WSA_RELATES_TO_REPLY_QNAME = new QName(WSA_NAMESPACE, WSA_RELATES_TO_REPLY, WSA_PREFIX) ;
}
