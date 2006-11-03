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
 * Copyright (c) 2003, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithParticipantCompletionCoordinatorTestCase.java,v 1.1.2.1 2004/05/26 10:04:55 nmcl Exp $
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
import com.arjuna.webservices.wsba.client.ParticipantCompletionParticipantClient;
import com.arjuna.webservices.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wst.tests.junit.TestParticipantCompletionParticipantProcessor.ParticipantCompletionParticipantDetails;

public class BusinessAgreementWithParticipantCompletionCoordinatorTestCase extends TestCase
{
    private ParticipantCompletionParticipantProcessor origParticipantCompletionParticipantProcessor ;
    private TestParticipantCompletionParticipantProcessor testParticipantCompletionParticipantProcessor = new TestParticipantCompletionParticipantProcessor() ;
    private String participantCompletionParticipantServiceURI ;
    private String participantCompletionCoordinatorServiceURI ;

    protected void setUp()
        throws Exception
    {
        origParticipantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.setParticipant(testParticipantCompletionParticipantProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        participantCompletionParticipantServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_PARTICIPANT) ;
        participantCompletionCoordinatorServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_COORDINATOR) ;
    }

    public void testSendClose()
        throws Exception
    {
        final String messageId = "testSendClose" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantCompletionParticipantClient.getClient().sendClose(addressingContext, instanceIdentifier) ;
        
        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasClose()) ;
    }

    public void testSendCancel()
        throws Exception
    {
        final String messageId = "testSendCancel" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantCompletionParticipantClient.getClient().sendCancel(addressingContext, instanceIdentifier) ;
        
        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCancel()) ;
    }

    public void testSendCompensate()
        throws Exception
    {
        final String messageId = "testSendCompensate" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantCompletionParticipantClient.getClient().sendCompensate(addressingContext, instanceIdentifier) ;
        
        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCompensate()) ;
    }

    public void testSendFaulted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendFaulted" ;
        final EndpointReferenceType participantCompletionParticipantService = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionParticipantService, messageId) ;
        
        final TestParticipantCompletionParticipantCallback callback = new TestParticipantCompletionParticipantCallback() {
            public void faulted(final NotificationType faulted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionCoordinatorServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionParticipantProcessor participant = ParticipantCompletionParticipantProcessor.getParticipant() ;
        participant.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendFaulted(addressingContext, new InstanceIdentifier("sender")) ;
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
        final EndpointReferenceType participantCompletionParticipantService = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionParticipantService, messageId) ;
        
        final TestParticipantCompletionParticipantCallback callback = new TestParticipantCompletionParticipantCallback() {
            public void exited(final NotificationType exited, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionCoordinatorServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionParticipantProcessor participant = ParticipantCompletionParticipantProcessor.getParticipant() ;
        participant.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendExited(addressingContext, new InstanceIdentifier("sender")) ;
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
        final EndpointReferenceType participantCompletionParticipantService = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantCompletionParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionParticipantService, messageId) ;
        
        final State state = State.STATE_ACTIVE ;
        
        final TestParticipantCompletionParticipantCallback callback = new TestParticipantCompletionParticipantCallback() {
            public void status(final StatusType status, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantCompletionCoordinatorServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertEquals(state, status.getState()) ;
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantCompletionParticipantProcessor participant = ParticipantCompletionParticipantProcessor.getParticipant() ;
        participant.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendStatus(addressingContext, new InstanceIdentifier("sender"), state) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            participant.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }
    
    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantCompletionParticipantClient.getClient().sendGetStatus(addressingContext, instanceIdentifier) ;
        
        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasGetStatus()) ;
    }

    public void testSendError()
        throws Exception
    {
        final String messageId = "123456" ;
        final String reason = "testSendErrorReason" ;
        final String instanceIdentifier = "testSendError" ;
        final EndpointReferenceType ParticipantCompletionParticipantService = new EndpointReferenceType(new AttributedURIType(participantCompletionParticipantServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(ParticipantCompletionParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(ParticipantCompletionParticipantService, messageId) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        
        final TestParticipantCompletionParticipantCallback callback = new TestParticipantCompletionParticipantCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantCompletionParticipantServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantCompletionCoordinatorServiceURI);
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
        final ParticipantCompletionParticipantProcessor participant = ParticipantCompletionParticipantProcessor.getParticipant() ;
        participant.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantCompletionParticipantClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender")) ;
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
        ParticipantCompletionParticipantProcessor.setParticipant(origParticipantCompletionParticipantProcessor) ;
    }
}
