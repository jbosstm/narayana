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
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * TestUtil.java
 */

package com.arjuna.wst11.tests;

import com.arjuna.webservices11.soapfault.SoapFaultConstants;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.namespace.QName;

public class TestUtil
{
    public static final String NOEXCEPTION_TRANSACTION_IDENTIFIER                    = "NE123456TI";
    public static final String TRANSACTIONROLLEDBACKEXCEPTION_TRANSACTION_IDENTIFIER = "TRBE123456TI";
    public static final String UNKNOWNTRANSACTIONEXCEPTION_TRANSACTION_IDENTIFIER    = "UTE123456TI";
    public static final String SYSTEMEXCEPTION_TRANSACTION_IDENTIFIER                = "SE123456TI";

    public static final String NONEXISTENT_TRANSACTION_IDENTIFIER                    = "NONE123456TI";

    public static final String PREPAREDVOTE_PARTICIPANT_IDENTIFIER           = "PV123456PI";
    public static final String ABORTEDVOTE_PARTICIPANT_IDENTIFIER            = "AV123456PI";
    public static final String READONLYVOTE_PARTICIPANT_IDENTIFIER           = "ROV123456PI";

    public static final String NOEXCEPTION_PARTICIPANT_IDENTIFIER                    = "NE123456PI";

    public static final String FAULTEDEXCEPTION_PARTICIPANT_IDENTIFIER               = "FE123456PI";

    public static final String TRANSACTIONROLLEDBACKEXCEPTION_PARTICIPANT_IDENTIFIER = "TRBE123456PI";
    public static final String WRONGSTATEEXCEPTION_PARTICIPANT_IDENTIFIER            = "WSE123456PI";
    public static final String SYSTEMEXCEPTION_PARTICIPANT_IDENTIFIER                = "SE123456PI";

    public static final String NONEXISTENT_PARTICIPANT_IDENTIFIER                    = "NONE123456PI";

    final private static String bindHost = XTSPropertyManager.getWSCEnvironmentBean().getBindAddress11();

    public static String participantServiceURI = "http://" + bindHost + ":8080/ws-t11/ParticipantService";
    public static String coordinatorServiceURI = "http://" + bindHost + ":8080/ws-t11/CoordinatorService";
    public static String completionInitiatorServiceURI = "http://" + bindHost + ":8080/ws-t11/CompletionInitiatorService";
    public static String completionCoordinatorServiceURI = "http://" + bindHost + ":8080/ws-t11/CompletionCoordinatorService";
    private final static String ATOMIC_TRANSACTION_FAULT_ACTION = "http://docs.oasis-open.org/ws-tx/wsat/2006/06/fault";
    private final static String BUSINESS_ACTIVITY_FAULT_ACTION = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06/fault";

    public static String participantCompletionParticipantServiceURI = "http://" + bindHost + ":8080/ws-t11/BusinessAgreementWithParticipantCompletionParticipantService";
    public static String participantCompletionCoordinatorServiceURI = "http://" + bindHost + ":8080/ws-t11/BusinessAgreementWithParticipantCompletionCoordinatorService";
    public static String coordinatorCompletionParticipantServiceURI = "http://" + bindHost + ":8080/ws-t11/BusinessAgreementWithCoordinatorCompletionParticipantService";
    public static String coordinatorCompletionCoordinatorServiceURI = "http://" + bindHost + ":8080/ws-t11/BusinessAgreementWithCoordinatorCompletionCoordinatorService";

    public static synchronized String getAtomicTransactionFaultAction()
    {
        return ATOMIC_TRANSACTION_FAULT_ACTION;
    }

    public static synchronized String getBusinessActivityFaultAction()
    {
        return BUSINESS_ACTIVITY_FAULT_ACTION;
    }

    public static W3CEndpointReference getParticipantEndpoint(String id)
    {
        return getEndpoint(AtomicTransactionConstants.PARTICIPANT_SERVICE_QNAME,
                AtomicTransactionConstants.PARTICIPANT_PORT_QNAME,
                participantServiceURI,
                id);
    }

    public static W3CEndpointReference getCoordinatorEndpoint(String id)
    {
        return getEndpoint(AtomicTransactionConstants.COORDINATOR_SERVICE_QNAME,
                AtomicTransactionConstants.COORDINATOR_PORT_QNAME,
                coordinatorServiceURI,
                id);
    }

    public static W3CEndpointReference getCompletionInitiatorEndpoint(String id)
    {
        return getEndpoint(AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_QNAME,
                AtomicTransactionConstants.COMPLETION_INITIATOR_PORT_QNAME,
                completionInitiatorServiceURI,
                id);
    }

    public static W3CEndpointReference getCompletionCoordinatorEndpoint(String id)
    {
        return getEndpoint(AtomicTransactionConstants.COMPLETION_COORDINATOR_SERVICE_QNAME,
                AtomicTransactionConstants.COMPLETION_COORDINATOR_PORT_QNAME,
                completionCoordinatorServiceURI,
                id);
    }

    public static W3CEndpointReference getParticipantCompletionParticipantEndpoint(String id)
    {
        return getEndpoint(BusinessActivityConstants.PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_QNAME,
                BusinessActivityConstants.PARTICIPANT_COMPLETION_PARTICIPANT_PORT_QNAME,
                participantCompletionParticipantServiceURI,
                id);
    }

    public static W3CEndpointReference getParticipantCompletionCoordinatorEndpoint(String id)
    {
        return getEndpoint(BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_QNAME,
                BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_PORT_QNAME,
                participantCompletionCoordinatorServiceURI,
                id);
    }

    public static W3CEndpointReference getCoordinatorCompletionParticipantEndpoint(String id)
    {
        return getEndpoint(BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_QNAME,
                BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_PORT_QNAME,
                coordinatorCompletionParticipantServiceURI,
                id);
    }

    public static W3CEndpointReference getCoordinatorCompletionCoordinatorEndpoint(String id)
    {
        return getEndpoint(BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_QNAME,
                BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_PORT_QNAME,
                coordinatorCompletionCoordinatorServiceURI,
                id);
    }

    private static W3CEndpointReference getEndpoint(QName service, QName port, String address, String id)
    {
        try {
            W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
            builder.serviceName(service);
            builder.endpointName(port);
            builder.address(address);
            if (id != null) {
                InstanceIdentifier.setEndpointInstanceIdentifier(builder, id);
            }
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }
}