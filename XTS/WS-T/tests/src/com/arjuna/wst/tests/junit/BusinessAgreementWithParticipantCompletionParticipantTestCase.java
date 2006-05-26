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
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithParticipantCompletionParticipantTestCase.java,v 1.1.2.1 2004/05/26 10:04:56 nmcl Exp $
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
import com.arjuna.webservices.wsba.ExceptionType;
import com.arjuna.webservices.wsba.NotificationType;
import com.arjuna.webservices.wsba.State;
import com.arjuna.webservices.wsba.StatusType;
import com.arjuna.webservices.wsba.client.ParticipantCompletionCoordinatorClient;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.wst.tests.junit.TestParticipantCompletionCoordinatorProcessor.ParticipantCompletionCoordinatorDetails;

public class BusinessAgreementWithParticipantCompletionParticipantTestCase extends TestCase
{
    private ParticipantCompletionCoordinatorProcessor origParticipantCompletionCoordinatorProcessor ;
    private TestParticipantCompletionCoordinatorProcessor testParticipantCompletionCoordinatorProcessor = new TestParticipantCompletionCoordinatorProcessor() ;
    private String participantCompletionCoordinatorServiceURI ;
    private String participantCompletionParticipantServiceURI ;

    protected void setUp()
        throws Exception
    {
        origParticipantCompletionCoordinatorProcessor = ParticipantCompletionCoordinatorProcessor.setCoordinator(testParticipantCompletionCoordinatorProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        participantCompletionCoordinatorServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_COORDINATOR) ;
        participantCompletionParticipantServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_PARTICIPANT) ;
    }

    public void testSendClosed()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendClosed" ;
        final EndpointReferenceType participantCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(participantCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorService, messageId) ;
        
        final TestParticipantCompletionCoordinatorCallback callback = new TestParticipantCompletionCoordinatorCallback() {
            public void closed(final NotificationType closed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionCoordinatorProcessor coordinator = ParticipantCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendClosed(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendCancelled()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendCancelled" ;
        final EndpointReferenceType participantCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(participantCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorService, messageId) ;
        
        final TestParticipantCompletionCoordinatorCallback callback = new TestParticipantCompletionCoordinatorCallback() {
            public void cancelled(final NotificationType cancelled, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionCoordinatorProcessor coordinator = ParticipantCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCancelled(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendCompensated()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendCompensated" ;
        final EndpointReferenceType participantCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(participantCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorService, messageId) ;
        
        final TestParticipantCompletionCoordinatorCallback callback = new TestParticipantCompletionCoordinatorCallback() {
            public void compensated(final NotificationType compensated, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionCoordinatorProcessor coordinator = ParticipantCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCompensated(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendCompleted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendCompleted" ;
        final EndpointReferenceType participantCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(participantCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorService, messageId) ;
        
        final TestParticipantCompletionCoordinatorCallback callback = new TestParticipantCompletionCoordinatorCallback() {
            public void completed(final NotificationType completed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionCoordinatorProcessor coordinator = ParticipantCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendCompleted(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendStatus()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendStatus" ;
        final EndpointReferenceType participantCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(participantCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorService, messageId) ;
        
        final State state = State.STATE_ENDED ;
        
        final TestParticipantCompletionCoordinatorCallback callback = new TestParticipantCompletionCoordinatorCallback() {
            public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertEquals(state, status.getState()) ;
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionCoordinatorProcessor coordinator = ParticipantCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendStatus(addressingContext, new InstanceIdentifier("sender"), state) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendError()
        throws Exception
    {
        final String messageId = "123456" ;
        final String reason = "testSendErrorReason" ;
        final String instanceIdentifier = "testSendError" ;
        final EndpointReferenceType participantCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(participantCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorService, messageId) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        
        final TestParticipantCompletionCoordinatorCallback callback = new TestParticipantCompletionCoordinatorCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionParticipantServiceURI);
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
        final ParticipantCompletionCoordinatorProcessor coordinator = ParticipantCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendExit()
        throws Exception
    {
        final String messageId = "testSendExit" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantCompletionCoordinatorClient.getClient().sendExit(addressingContext, instanceIdentifier) ;
        
        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;
        
        assertTrue(details.hasExit()) ;
    }

    public void testSendFault()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendFault" ;
        final String exceptionIdentifier = "testSendFaultExceptionIdentifier" ;
        final EndpointReferenceType participantCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(participantCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorService, messageId) ;
        
        final TestParticipantCompletionCoordinatorCallback callback = new TestParticipantCompletionCoordinatorCallback() {
            public void fault(final ExceptionType fault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertEquals(exceptionIdentifier, fault.getExceptionIdentifier()) ;
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionCoordinatorProcessor coordinator = ParticipantCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionCoordinatorClient.getClient().sendFault(addressingContext, new InstanceIdentifier("sender"), exceptionIdentifier) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionCoordinatorServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantCompletionCoordinatorClient.getClient().sendGetStatus(addressingContext, instanceIdentifier) ;
        
        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;
        
        assertTrue(details.hasGetStatus()) ;
    }

    protected void tearDown()
        throws Exception
    {
        ParticipantCompletionCoordinatorProcessor.setCoordinator(origParticipantCompletionCoordinatorProcessor) ;
    }
}
