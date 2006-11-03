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
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * RegistrationTestCase.java
 */

package com.arjuna.wsc.tests.junit;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.RelationshipType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.RegisterResponseType;
import com.arjuna.webservices.wscoor.RegisterType;
import com.arjuna.webservices.wscoor.client.RegistrationCoordinatorClient;
import com.arjuna.webservices.wscoor.client.RegistrationRequesterClient;
import com.arjuna.webservices.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.webservices.wscoor.processors.RegistrationRequesterProcessor;
import com.arjuna.wsc.tests.junit.TestRegistrationCoordinatorProcessor.RegisterDetails;

public class RegistrationTestCase extends TestCase
{
    private RegistrationCoordinatorProcessor origRegistrationCoordinatorProcessor ;
    
    private TestRegistrationCoordinatorProcessor testRegistrationCoordinatorProcessor = new TestRegistrationCoordinatorProcessor() ;
    private EndpointReferenceType registrationCoordinatorService ;
    private EndpointReferenceType registrationRequesterService ;
    
    protected void setUp()
        throws Exception
    {
        origRegistrationCoordinatorProcessor = RegistrationCoordinatorProcessor.setCoordinator(testRegistrationCoordinatorProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String registrationCoordinatorServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
        registrationCoordinatorService = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorServiceURI)) ;
        final String registrationRequesterServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_REQUESTER) ;
        registrationRequesterService = new EndpointReferenceType(new AttributedURIType(registrationRequesterServiceURI)) ;
    }

    public void testRequestWithoutInstanceIdentifier()
        throws Exception
    {
        executeRequest("testRequestWithoutInstanceIdentifier", null) ;
    }

    public void testRequestWithInstanceIdentifier()
        throws Exception
    {
        executeRequest("testRequestWithInstanceIdentifier", new InstanceIdentifier("identifier")) ;
    }
    
    private void executeRequest(final String messageId, final InstanceIdentifier instanceIdentifier)
        throws Exception
    {
        final String protocolIdentifier = "http://foo.example.org/bar" ;
        final EndpointReferenceType participantProtocolService = new EndpointReferenceType(new AttributedURIType("http://bar.example.org/foo")) ;
        final EndpointReferenceType toService ;
        if (instanceIdentifier == null)
        {
            toService = registrationCoordinatorService ;
        }
        else
        {
            toService = new EndpointReferenceType(registrationCoordinatorService.getAddress()) ;
            InstanceIdentifier.setEndpointInstanceIdentifier(toService, instanceIdentifier) ;
        }
        
        final AddressingContext addressingContext = AddressingContext.createRequestContext(toService, messageId) ;
        RegistrationCoordinatorClient.getClient().sendRegister(addressingContext, protocolIdentifier, participantProtocolService) ;
        
        final RegisterDetails details = testRegistrationCoordinatorProcessor.getRegisterDetails(messageId, 10000) ;
        final RegisterType requestRegister = details.getRegister() ;
        final AddressingContext requestAddressingContext = details.getAddressingContext() ;
        final ArjunaContext requestArjunaContext = details.getArjunaContext() ;

        assertEquals(requestAddressingContext.getTo().getValue(), registrationCoordinatorService.getAddress().getValue());
        assertEquals(requestAddressingContext.getFrom().getAddress().getValue(), registrationRequesterService.getAddress().getValue());
        assertEquals(requestAddressingContext.getReplyTo().getAddress().getValue(), registrationRequesterService.getAddress().getValue());
        assertEquals(requestAddressingContext.getMessageID().getValue(), messageId);
        
        if (instanceIdentifier == null)
        {
            assertNull(requestArjunaContext.getInstanceIdentifier()) ;
        }
        else
        {
            assertEquals(instanceIdentifier.getInstanceIdentifier(), requestArjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
        }

        assertEquals(protocolIdentifier, requestRegister.getProtocolIdentifier().getValue()) ;
        assertEquals(participantProtocolService.getAddress().getValue(),
            requestRegister.getParticipantProtocolService().getAddress().getValue()) ;
    }

    public void testResponse()
        throws Exception
    {
        final String messageId = "123456" ;
        final String relatesTo = "testResponse" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(registrationRequesterService, messageId) ;
        addressingContext.addRelatesTo(new RelationshipType(relatesTo)) ;
        
        final EndpointReferenceType coordinationProtocolService = new EndpointReferenceType(new AttributedURIType("http://foo.example.org/bar")) ;
        
        final TestRegistrationRequesterCallback callback = new TestRegistrationRequesterCallback() {
            public void registerResponse(final RegisterResponseType registerResponse, final AddressingContext addressingContext)
            {
                assertEquals(addressingContext.getTo().getValue(), registrationRequesterService.getAddress().getValue());
                assertEquals(addressingContext.getFrom().getAddress().getValue(), registrationCoordinatorService.getAddress().getValue());
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertEquals(coordinationProtocolService.getAddress().getValue(),
                    registerResponse.getCoordinatorProtocolService().getAddress().getValue()) ;
            } 
        } ;
        
        final RegistrationRequesterProcessor requester = RegistrationRequesterProcessor.getRequester() ;
        requester.registerCallback(relatesTo, callback) ;
        try
        {
            RegistrationRequesterClient.getClient().sendRegisterResponse(addressingContext, coordinationProtocolService) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            requester.removeCallback(relatesTo) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testError()
        throws Exception
    {
        final String messageId = "123456" ;
        final String relatesTo = "testResponse" ;
        final String reason = "testResponseReason" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(registrationRequesterService, messageId) ;
        addressingContext.addRelatesTo(new RelationshipType(relatesTo)) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = CoordinationConstants.WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        
        final TestRegistrationRequesterCallback callback = new TestRegistrationRequesterCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext)
            {
                assertEquals(addressingContext.getTo().getValue(), registrationRequesterService.getAddress().getValue());
                assertEquals(addressingContext.getFrom().getAddress().getValue(), registrationCoordinatorService.getAddress().getValue());
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(soapFault) ;
                assertEquals(soapFaultType, soapFault.getSoapFaultType()) ;
                assertEquals(subcode, soapFault.getSubcode()) ;
                assertEquals(reason, soapFault.getReason()) ;
            }
        };
        final RegistrationRequesterProcessor requester = RegistrationRequesterProcessor.getRequester() ;
        requester.registerCallback(relatesTo, callback) ;
        
        try
        {
            RegistrationRequesterClient.getClient().sendSoapFault(addressingContext, soapFault) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            requester.removeCallback(relatesTo) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    protected void tearDown()
        throws Exception
    {
        RegistrationCoordinatorProcessor.setCoordinator(origRegistrationCoordinatorProcessor) ;
        origRegistrationCoordinatorProcessor = null ;
        testRegistrationCoordinatorProcessor = null ;
        registrationCoordinatorService = null ;
        registrationRequesterService = null ;
    }
}
