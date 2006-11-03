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
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: TwoPCParticipantTestCase.java,v 1.6.2.1 2005/11/22 10:37:46 kconner Exp $
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
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.client.CoordinatorClient;
import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;

public class TwoPCParticipantTestCase extends TestCase
{
    protected void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        coordinatorServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COORDINATOR) ;
        participantServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_PARTICIPANT) ;
    }

    public void testSendPrepared()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendPrepared" ;
        final EndpointReferenceType coordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorService, messageId) ;
        
        final TestCoordinatorCallback callback = new TestCoordinatorCallback() {
            public void prepared(final NotificationType prepared, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorClient.getClient().sendPrepared(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendAborted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendAborted" ;
        final EndpointReferenceType coordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorService, messageId) ;
        
        final TestCoordinatorCallback callback = new TestCoordinatorCallback() {
            public void aborted(final NotificationType aborted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorClient.getClient().sendAborted(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendReadOnly()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendReadOnly" ;
        final EndpointReferenceType coordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorService, messageId) ;
        
        final TestCoordinatorCallback callback = new TestCoordinatorCallback() {
            public void readOnly(final NotificationType readOnly, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorClient.getClient().sendReadOnly(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendCommitted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendCommitted" ;
        final EndpointReferenceType coordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorService, messageId) ;
        
        final TestCoordinatorCallback callback = new TestCoordinatorCallback() {
            public void committed(final NotificationType committed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorClient.getClient().sendCommitted(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            coordinator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendReplay()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendReplay" ;
        final EndpointReferenceType coordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorService, messageId) ;
        
        final TestCoordinatorCallback callback = new TestCoordinatorCallback() {
            public void replay(final NotificationType replay, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantServiceURI);
                assertEquals(addressingContext.getReplyTo().getAddress().getValue(), participantServiceURI);
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorClient.getClient().sendReplay(addressingContext, new InstanceIdentifier("sender")) ;
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
        final EndpointReferenceType coordinatorService = new EndpointReferenceType(new AttributedURIType(coordinatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(coordinatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorService, messageId) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        
        final TestCoordinatorCallback callback = new TestCoordinatorCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), coordinatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), participantServiceURI);
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
        final CoordinatorProcessor coordinator = CoordinatorProcessor.getCoordinator() ;
        coordinator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CoordinatorClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender")) ;
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

    private String coordinatorServiceURI ;
    private String participantServiceURI ;
}
