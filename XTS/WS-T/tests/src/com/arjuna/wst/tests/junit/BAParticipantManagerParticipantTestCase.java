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
 * $Id: BAParticipantManagerParticipantTestCase.java,v 1.7.6.1 2005/11/22 10:37:46 kconner Exp $
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
import com.arjuna.webservices.wsarjtx.NotificationType;
import com.arjuna.webservices.wsarjtx.client.ParticipantManagerCoordinatorClient;
import com.arjuna.webservices.wsarjtx.processors.ParticipantManagerCoordinatorProcessor;

public class BAParticipantManagerParticipantTestCase extends TestCase
{
    protected void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        participantManagerParticipantServiceURI = soapRegistry.getServiceURI(ArjunaTXConstants.SERVICE_PARTICIPANT_MANAGER_PARTICIPANT) ;
        participantManagerCoordinatorServiceURI = soapRegistry.getServiceURI(ArjunaTXConstants.SERVICE_PARTICIPANT_MANAGER_COORDINATOR) ;
    }

    public void testSendComplete()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendComplete" ;
        final EndpointReferenceType participantManagerParticipantService = new EndpointReferenceType(new AttributedURIType(participantManagerCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantManagerParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantManagerParticipantService, messageId) ;
        
        final TestParticipantManagerCoordinatorCallback callback = new TestParticipantManagerCoordinatorCallback() {
            public void complete(final NotificationType complete, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantManagerCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantManagerParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantManagerParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantManagerCoordinatorProcessor coordinator = ParticipantManagerCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantManagerCoordinatorClient.getClient().sendComplete(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendExited()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendExited" ;
        final EndpointReferenceType participantManagerParticipantService = new EndpointReferenceType(new AttributedURIType(participantManagerCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantManagerParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantManagerParticipantService, messageId) ;
        
        final TestParticipantManagerCoordinatorCallback callback = new TestParticipantManagerCoordinatorCallback() {
            public void exited(final NotificationType exited, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantManagerCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantManagerParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantManagerParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantManagerCoordinatorProcessor coordinator = ParticipantManagerCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantManagerCoordinatorClient.getClient().sendExited(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendFaulted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendFaulted" ;
        final EndpointReferenceType participantManagerParticipantService = new EndpointReferenceType(new AttributedURIType(participantManagerCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantManagerParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantManagerParticipantService, messageId) ;
        
        final TestParticipantManagerCoordinatorCallback callback = new TestParticipantManagerCoordinatorCallback() {
            public void faulted(final NotificationType faulted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantManagerCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantManagerParticipantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantManagerParticipantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final ParticipantManagerCoordinatorProcessor coordinator = ParticipantManagerCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantManagerCoordinatorClient.getClient().sendFaulted(addressingContext, new InstanceIdentifier("sender")) ;
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
        final EndpointReferenceType participantManagerParticipantService = new EndpointReferenceType(new AttributedURIType(participantManagerCoordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(participantManagerParticipantService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantManagerParticipantService, messageId) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        
        final TestParticipantManagerCoordinatorCallback callback = new TestParticipantManagerCoordinatorCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), participantManagerCoordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantManagerParticipantServiceURI);
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
        final ParticipantManagerCoordinatorProcessor coordinator = ParticipantManagerCoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            ParticipantManagerCoordinatorClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    protected void tearDown()
        throws Exception
    {
    }

    private String participantManagerParticipantServiceURI ;
    private String participantManagerCoordinatorServiceURI ;
}
