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
/*
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * TestUtil.java
 */

package com.arjuna.wsc11.tests;

import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.wsc.tests.TestUtil;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

public class TestUtil11
{
    final private static String TAG = "wsc";
    final private static String NAMESPACE = "http://wsc.example.org/";
    final private static String PROTOCOL_PARTICIPANT_ENDPOINT_NAME ="ProtocolParticipantEndpoint";
    final private static String PROTOCOL_PARTICIPANT_SERVICE_NAME ="ProtocolParticipantService";
    final private static String PROTOCOL_COORDINATOR_ENDPOINT_NAME ="ProtocolCoordinatorEndpoint";
    final private static String PROTOCOL_COORDINATOR_SERVICE_NAME ="ProtocolCoordinatorService";

    final private static String bindHost = System.getProperty(com.arjuna.wsc.common.Environment.XTS11_BIND_ADDRESS);

    final public static String activationCoordinatorService = "http://" + bindHost + ":8080/ws-c11/ActivationService";
    final public static String registrationCoordinatorService = "http://" + bindHost + ":8080/ws-c11/RegistrationService";

    final public static QName PROTOCOL_PARTICIPANT_SERVICE_QNAME = new QName(NAMESPACE, PROTOCOL_PARTICIPANT_SERVICE_NAME, TAG);
    final public static QName PROTOCOL_PARTICIPANT_ENDPOINT_QNAME = new QName(NAMESPACE, PROTOCOL_PARTICIPANT_ENDPOINT_NAME, TAG);
    final public static QName PROTOCOL_COORDINATOR_SERVICE_QNAME = new QName(NAMESPACE, PROTOCOL_COORDINATOR_SERVICE_NAME, TAG);
    final public static QName PROTOCOL_COORDINATOR_ENDPOINT_QNAME = new QName(NAMESPACE, PROTOCOL_COORDINATOR_ENDPOINT_NAME, TAG);

    public static W3CEndpointReference getActivationEndpoint() {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(CoordinationConstants.ACTIVATION_SERVICE_QNAME);
        builder.endpointName(CoordinationConstants.ACTIVATION_ENDPOINT_QNAME);
        builder.address(activationCoordinatorService);
        return builder.build();
    }

    public static W3CEndpointReference getRegistrationEndpoint(String identifier) {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(CoordinationConstants.REGISTRATION_SERVICE_QNAME);
        builder.endpointName(CoordinationConstants.REGISTRATION_ENDPOINT_QNAME);
        builder.address(registrationCoordinatorService);
        if (identifier != null) {
            InstanceIdentifier.setEndpointInstanceIdentifier(builder, identifier);
        }
        return builder.build();
    }

    public static W3CEndpointReference getProtocolCoordinatorEndpoint(String identifier) {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(PROTOCOL_COORDINATOR_SERVICE_QNAME);
        builder.endpointName(PROTOCOL_COORDINATOR_ENDPOINT_QNAME);
        builder.address(TestUtil.PROTOCOL_COORDINATOR_SERVICE);
        if (identifier != null) {
            InstanceIdentifier.setEndpointInstanceIdentifier(builder, identifier);
        }
        return builder.build();
    }

    public static W3CEndpointReference getProtocolParticipantEndpoint(String identifier) {
        W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(PROTOCOL_PARTICIPANT_SERVICE_QNAME);
        builder.endpointName(PROTOCOL_PARTICIPANT_ENDPOINT_QNAME);
        builder.address(TestUtil.PROTOCOL_PARTICIPANT_SERVICE);
        if (identifier != null) {
            InstanceIdentifier.setEndpointInstanceIdentifier(builder, identifier);
        }
        return builder.build();
    }
}