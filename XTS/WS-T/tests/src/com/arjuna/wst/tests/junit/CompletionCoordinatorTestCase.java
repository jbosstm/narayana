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
 * CompletionCoordinatorTestCase.java
 */

package com.arjuna.wst.tests.junit;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.NotificationType;
import com.arjuna.webservices.wsat.client.CompletionInitiatorClient;
import com.arjuna.webservices.wsat.processors.CompletionInitiatorProcessor;

public class CompletionCoordinatorTestCase
{
    @Before
    public void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        completionInitiatorServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COMPLETION_INITIATOR) ;
        completionCoordinatorServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COMPLETION_COORDINATOR) ;
    }

    @Test
    public void testSendCommitted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendCommitted" ;
        final EndpointReferenceType completionInitiatorService = new EndpointReferenceType(new AttributedURIType(completionInitiatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(completionInitiatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(completionInitiatorService, messageId) ;
        
        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void committed(final NotificationType committed, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), completionInitiatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), completionCoordinatorServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CompletionInitiatorProcessor initiator = CompletionInitiatorProcessor.getProcessor() ;
        initiator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CompletionInitiatorClient.getClient().sendCommitted(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    @Test
    public void testSendAborted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendAborted" ;
        final EndpointReferenceType completionInitiatorService = new EndpointReferenceType(new AttributedURIType(completionInitiatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(completionInitiatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(completionInitiatorService, messageId) ;
        
        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void aborted(final NotificationType aborted, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), completionInitiatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), completionCoordinatorServiceURI);
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(arjunaContext.getInstanceIdentifier()) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CompletionInitiatorProcessor initiator = CompletionInitiatorProcessor.getProcessor() ;
        initiator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CompletionInitiatorClient.getClient().sendAborted(addressingContext, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    @Test
    public void testSendError()
        throws Exception
    {
        final String messageId = "123456" ;
        final String reason = "testSendErrorReason" ;
        final String instanceIdentifier = "testSendError" ;
        final EndpointReferenceType completionInitiatorService = new EndpointReferenceType(new AttributedURIType(completionInitiatorServiceURI)) ;
        InstanceIdentifier.setEndpointInstanceIdentifier(completionInitiatorService, instanceIdentifier) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(completionInitiatorService, messageId) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault10(soapFaultType, subcode, reason) ;
        
        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingContext.getTo().getValue(), completionInitiatorServiceURI);
                assertEquals(addressingContext.getFrom().getAddress().getValue(), completionCoordinatorServiceURI);
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
        final CompletionInitiatorProcessor initiator = CompletionInitiatorProcessor.getProcessor() ;
        initiator.registerCallback(instanceIdentifier, callback) ;
        
        try
        {
            CompletionInitiatorClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    @After
    public void tearDown()
        throws Exception
    {
    }

    private String completionInitiatorServiceURI ;
    private String completionCoordinatorServiceURI ;
}
