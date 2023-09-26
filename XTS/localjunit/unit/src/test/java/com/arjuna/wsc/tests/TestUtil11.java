/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wsc.tests;

import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

import javax.xml.namespace.QName;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import jakarta.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

public class TestUtil11
{
    final private static String TAG = "wsc";
    final private static String NAMESPACE = "http://wsc.example.org/";
    final private static String PROTOCOL_PARTICIPANT_ENDPOINT_NAME ="ProtocolParticipantEndpoint";
    final private static String PROTOCOL_PARTICIPANT_SERVICE_NAME ="ProtocolParticipantService";
    final private static String PROTOCOL_COORDINATOR_ENDPOINT_NAME ="ProtocolCoordinatorEndpoint";
    final private static String PROTOCOL_COORDINATOR_SERVICE_NAME ="ProtocolCoordinatorService";

    final private static String bindHost = XTSPropertyManager.getWSCEnvironmentBean().getBindAddress11();

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