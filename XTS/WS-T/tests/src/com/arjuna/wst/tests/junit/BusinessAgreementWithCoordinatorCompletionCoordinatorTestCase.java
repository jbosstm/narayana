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
/*
 * Copyright (c) 2004, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithCoordinatorCompletionCoordinatorTestCase.java,v 1.1.2.1 2004/05/26 10:04:55 nmcl Exp $
 */

package com.arjuna.wst.tests.junit;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsba.BusinessActivityConstants;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.CoordinatorCompletionParticipantClient;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.wst.tests.junit.TestCoordinatorCompletionParticipantProcessor.CoordinatorCompletionParticipantDetails;

public class BusinessAgreementWithCoordinatorCompletionCoordinatorTestCase extends TestCase
{
    private CoordinatorCompletionParticipantProcessor origCoordinatorCompletionParticipantProcessor ;
    private TestCoordinatorCompletionParticipantProcessor testCoordinatorCompletionParticipantProcessor = new TestCoordinatorCompletionParticipantProcessor() ;
    private String coordinatorCompletionParticipantServiceURI ;
    private String coordinatorCompletionCoordinatorServiceURI ;

    protected void setUp()
        throws Exception
    {
        origCoordinatorCompletionParticipantProcessor = CoordinatorCompletionParticipantProcessor.setParticipant(testCoordinatorCompletionParticipantProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        coordinatorCompletionParticipantServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_PARTICIPANT) ;
        coordinatorCompletionCoordinatorServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_COORDINATOR) ;
    }

    public void testSendClose()
        throws Exception
    {
        final String messageId = "testSendClose" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CoordinatorCompletionParticipantClient.getClient().sendClose(addressingContext, instanceIdentifier) ;
        
        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasClose()) ;
    }

    public void testSendCancel()
        throws Exception
    {
        final String messageId = "testSendCancel" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CoordinatorCompletionParticipantClient.getClient().sendCancel(addressingContext, instanceIdentifier) ;
        
        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCancel()) ;
    }

    public void testSendCompensate()
        throws Exception
    {
        final String messageId = "testSendCompensate" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CoordinatorCompletionParticipantClient.getClient().sendCompensate(addressingContext, instanceIdentifier) ;
        
        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCompensate()) ;
    }

    public void testSendFaulted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendFaulted" ;
        final EndpointReferenceType coordinatorCompletionParticipantService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionParticipantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantService, messageId) ;
        
        final TestCoordinatorCompletionParticipantCallback callback = new TestCoordinatorCompletionParticipantCallback() {
            public void faulted(final NotificationType faulted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionParticipantProcessor participant = CoordinatorCompletionParticipantProcessor.getParticipant() ;
        participant.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendFaulted(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            participant.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendExited()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendExited" ;
        final EndpointReferenceType coordinatorCompletionParticipantService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionParticipantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantService, messageId) ;
        
        final TestCoordinatorCompletionParticipantCallback callback = new TestCoordinatorCompletionParticipantCallback() {
            public void exited(final NotificationType exited, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionParticipantProcessor participant = CoordinatorCompletionParticipantProcessor.getParticipant() ;
        participant.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendExited(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            participant.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendStatus()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendStatus" ;
        final EndpointReferenceType coordinatorCompletionParticipantService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionParticipantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantService, messageId) ;
        
        final State state = State.STATE_ACTIVE ;
        
        final TestCoordinatorCompletionParticipantCallback callback = new TestCoordinatorCompletionParticipantCallback() {
            public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertEquals(state, status.getState()) ;
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionParticipantProcessor participant = CoordinatorCompletionParticipantProcessor.getParticipant() ;
        participant.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendStatus(addressingContext, new InstanceIdentifier("sender"), state) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            participant.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendComplete()
        throws Exception
    {
        final String messageId = "testSendComplete" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CoordinatorCompletionParticipantClient.getClient().sendComplete(addressingContext, instanceIdentifier) ;
        
        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasComplete()) ;
    }

    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CoordinatorCompletionParticipantClient.getClient().sendGetStatus(addressingContext, instanceIdentifier) ;
        
        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasGetStatus()) ;
    }

    public void testSendError()
        throws Exception
    {
        final String messageId = "123456" ;
        final String reason = "testSendErrorReason" ;
        final String instanceIdentifier = "testSendError" ;
        final EndpointReferenceType coordinatorCompletionParticipantService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionParticipantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantService, messageId) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        
        final TestCoordinatorCompletionParticipantCallback callback = new TestCoordinatorCompletionParticipantCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(soapFault) ;
                assertEquals(soapFaultType, soapFault.getSoapFaultType()) ;
                assertEquals(subcode, soapFault.getSubcode()) ;
                assertEquals(reason, soapFault.getReason()) ;
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionParticipantProcessor participant = CoordinatorCompletionParticipantProcessor.getParticipant() ;
        participant.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionParticipantClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            participant.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    protected void tearDown()
        throws Exception
    {
        CoordinatorCompletionParticipantProcessor.setParticipant(origCoordinatorCompletionParticipantProcessor) ;
    }
}
