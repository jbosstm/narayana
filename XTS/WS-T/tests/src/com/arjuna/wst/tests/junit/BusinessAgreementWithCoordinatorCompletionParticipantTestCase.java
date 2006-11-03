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
/*
 * Copyright (c) 2004, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithCoordinatorCompletionParticipantTestCase.java,v 1.1.2.1 2004/05/26 10:04:55 nmcl Exp $
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
import com.arjuna.webservices.wsba.client.CoordinatorCompletionCoordinatorClient;
import com.arjuna.webservices.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.wst.tests.junit.TestCoordinatorCompletionCoordinatorProcessor.CoordinatorCompletionCoordinatorDetails;

public class BusinessAgreementWithCoordinatorCompletionParticipantTestCase extends TestCase
{
    private CoordinatorCompletionCoordinatorProcessor origCoordinatorCompletionCoordinatorProcessor ;
    private TestCoordinatorCompletionCoordinatorProcessor testCoordinatorCompletionCoordinatorProcessor = new TestCoordinatorCompletionCoordinatorProcessor() ;
    private String coordinatorCompletionCoordinatorServiceURI ;
    private String coordinatorCompletionParticipantServiceURI ;

    protected void setUp()
        throws Exception
    {
        origCoordinatorCompletionCoordinatorProcessor = CoordinatorCompletionCoordinatorProcessor.setCoordinator(testCoordinatorCompletionCoordinatorProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        coordinatorCompletionCoordinatorServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_COORDINATOR) ;
        coordinatorCompletionParticipantServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_PARTICIPANT) ;
    }

    public void testSendClosed()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendClosed" ;
        final EndpointReferenceType coordinatorCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorService, messageId) ;
        
        final TestCoordinatorCompletionCoordinatorCallback callback = new TestCoordinatorCompletionCoordinatorCallback() {
            public void closed(final NotificationType closed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionCoordinatorProcessor coordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendClosed(addressingContext, new InstanceIdentifier("sender")) ;
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
        final EndpointReferenceType coordinatorCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorService, messageId) ;
        
        final TestCoordinatorCompletionCoordinatorCallback callback = new TestCoordinatorCompletionCoordinatorCallback() {
            public void cancelled(final NotificationType cancelled, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionCoordinatorProcessor coordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCancelled(addressingContext, new InstanceIdentifier("sender")) ;
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
        final EndpointReferenceType coordinatorCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorService, messageId) ;
        
        final TestCoordinatorCompletionCoordinatorCallback callback = new TestCoordinatorCompletionCoordinatorCallback() {
            public void compensated(final NotificationType compensated, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionCoordinatorProcessor coordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCompensated(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendFault()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendFault" ;
        final EndpointReferenceType coordinatorCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorService, messageId) ;
        
        final String exceptionIdentifier = "testSendFaultExceptionIdentifier" ;
        
        final TestCoordinatorCompletionCoordinatorCallback callback = new TestCoordinatorCompletionCoordinatorCallback() {
            public void fault(final ExceptionType fault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertEquals(exceptionIdentifier, fault.getExceptionIdentifier()) ;
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionCoordinatorProcessor coordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendFault(addressingContext, new InstanceIdentifier("sender"), exceptionIdentifier) ;
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
        final EndpointReferenceType coordinatorCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorService, messageId) ;
        
        final TestCoordinatorCompletionCoordinatorCallback callback = new TestCoordinatorCompletionCoordinatorCallback() {
            public void completed(final NotificationType completed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionCoordinatorProcessor coordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendCompleted(addressingContext, new InstanceIdentifier("sender")) ;
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
        final EndpointReferenceType coordinatorCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorService, messageId) ;
        
        final State state = State.STATE_COMPENSATING ;
        
        final TestCoordinatorCompletionCoordinatorCallback callback = new TestCoordinatorCompletionCoordinatorCallback() {
            public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertEquals(state, status.getState()) ;
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorCompletionCoordinatorProcessor coordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendStatus(addressingContext, new InstanceIdentifier("sender"), state) ;
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
        final EndpointReferenceType coordinatorCompletionCoordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorCompletionCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorCompletionCoordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorService, messageId) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        
        final TestCoordinatorCompletionCoordinatorCallback callback = new TestCoordinatorCompletionCoordinatorCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
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
        final CoordinatorCompletionCoordinatorProcessor coordinator = CoordinatorCompletionCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender")) ;
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
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CoordinatorCompletionCoordinatorClient.getClient().sendExit(addressingContext, instanceIdentifier) ;
        
        final CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;
        
        assertTrue(details.hasExit()) ;
    }
    
    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionCoordinatorServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CoordinatorCompletionCoordinatorClient.getClient().sendGetStatus(addressingContext, instanceIdentifier) ;
        
        final CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;
        
        assertTrue(details.hasGetStatus()) ;
    }

    protected void tearDown()
        throws Exception
    {
        CoordinatorCompletionCoordinatorProcessor.setCoordinator(origCoordinatorCompletionCoordinatorProcessor) ;
    }
}
