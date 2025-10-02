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
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * EnduranceTestCase.java
 */

package com.arjuna.wsc.tests.junit;

import javax.xml.namespace.QName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;


import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapFault10;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.RelationshipType;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextType;
import com.arjuna.webservices.wscoor.RegisterResponseType;
import com.arjuna.webservices.wscoor.RegisterType;
import com.arjuna.webservices.wscoor.client.ActivationCoordinatorClient;
import com.arjuna.webservices.wscoor.client.ActivationRequesterClient;
import com.arjuna.webservices.wscoor.client.RegistrationCoordinatorClient;
import com.arjuna.webservices.wscoor.client.RegistrationRequesterClient;
import com.arjuna.webservices.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.webservices.wscoor.processors.ActivationRequesterProcessor;
import com.arjuna.webservices.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.webservices.wscoor.processors.RegistrationRequesterProcessor;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc.tests.junit.TestActivationCoordinatorProcessor.CreateCoordinationContextDetails;
import com.arjuna.wsc.tests.junit.TestRegistrationCoordinatorProcessor.RegisterDetails;

public class EnduranceTestCase
{
    private ActivationCoordinatorProcessor origActivationCoordinatorProcessor ;
    private RegistrationCoordinatorProcessor origRegistrationCoordinatorProcessor ;
    
    private TestActivationCoordinatorProcessor testActivationCoordinatorProcessor = new TestActivationCoordinatorProcessor() ;
    private EndpointReferenceType activationCoordinatorService ;
    private EndpointReferenceType activationRequesterService ;
    
    private TestRegistrationCoordinatorProcessor testRegistrationCoordinatorProcessor = new TestRegistrationCoordinatorProcessor() ;
    private EndpointReferenceType registrationCoordinatorService ;
    private EndpointReferenceType registrationRequesterService ;
    
    private static final long TEST_DURATION = 30 * 1000;

    @Before
    public void setUp()
        throws Exception
    {
        origActivationCoordinatorProcessor = ActivationCoordinatorProcessor.setCoordinator(testActivationCoordinatorProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String activationCoordinatorServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_COORDINATOR) ;
        activationCoordinatorService = new EndpointReferenceType(new AttributedURIType(activationCoordinatorServiceURI)) ;
        final String activationRequesterServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_REQUESTER) ;
        activationRequesterService = new EndpointReferenceType(new AttributedURIType(activationRequesterServiceURI)) ;
        
        origRegistrationCoordinatorProcessor = RegistrationCoordinatorProcessor.setCoordinator(testRegistrationCoordinatorProcessor) ;
        final String registrationCoordinatorServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
        registrationCoordinatorService = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorServiceURI)) ;
        final String registrationRequesterServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_REQUESTER) ;
        registrationRequesterService = new EndpointReferenceType(new AttributedURIType(registrationRequesterServiceURI)) ;
    }

    @Test
    public void testCreateCoordinationContextRequest()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doCreateCoordinationContextRequest(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    @Test
    public void testCreateCoordinationContextResponse()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doCreateCoordinationContextResponse(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    @Test
    public void testCreateCoordinationContextError()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doCreateCoordinationContextError(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    @Test
    public void testRegisterRequest()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doRegisterRequest(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    @Test
    public void testRegisterResponse()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doRegisterResponse(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    @Test
    public void testRegisterError()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doRegisterError(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    @Test
    public void testEachInTurn()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int count                  = 0;
        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            if (count == 0)
                doCreateCoordinationContextRequest(Integer.toString(dialogIdentifierNumber));
            else if (count == 1)
                doCreateCoordinationContextResponse(Integer.toString(dialogIdentifierNumber));
            else if (count == 2)
                doCreateCoordinationContextError(Integer.toString(dialogIdentifierNumber));
            else if (count == 3)
                doRegisterRequest(Integer.toString(dialogIdentifierNumber));
            else if (count == 4)
                doRegisterResponse(Integer.toString(dialogIdentifierNumber));
            else
                doRegisterError(Integer.toString(dialogIdentifierNumber));

            count = (count + 1) % 6;
            dialogIdentifierNumber++;
        }
    }

    public void doCreateCoordinationContextRequest(final String messageId)
        throws Exception
    {
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(activationCoordinatorService, messageId) ;
        ActivationCoordinatorClient.getClient().sendCreateCoordination(addressingContext, coordinationType, null, null) ;
        
        final CreateCoordinationContextDetails details = testActivationCoordinatorProcessor.getCreateCoordinationContextDetails(messageId, 10000) ;
        final CreateCoordinationContextType requestCreateCoordinationContext = details.getCreateCoordinationContext() ;
        final AddressingContext requestAddressingContext = details.getAddressingContext() ;
    
        assertEquals(requestAddressingContext.getTo().getValue(), activationCoordinatorService.getAddress().getValue());
        assertEquals(requestAddressingContext.getFrom().getAddress().getValue(), activationRequesterService.getAddress().getValue());
        assertEquals(requestAddressingContext.getReplyTo().getAddress().getValue(), activationRequesterService.getAddress().getValue());
        assertEquals(requestAddressingContext.getMessageID().getValue(), messageId);
        
        assertNull(requestCreateCoordinationContext.getExpires()) ;
        assertNull(requestCreateCoordinationContext.getCurrentContext()) ;
        assertEquals(requestCreateCoordinationContext.getCoordinationType().getValue(), coordinationType);
    }

    public void doCreateCoordinationContextResponse(final String messageId)
        throws Exception
    {
        final String relatesTo = "doCreateCoordinationContextResponse" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(activationRequesterService, messageId) ;
        addressingContext.addRelatesTo(new RelationshipType(relatesTo)) ;
        
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final String identifier = TestUtil.PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        coordinationContext.setCoordinationType(new URI(coordinationType)) ;
        coordinationContext.setIdentifier(new AttributedURIType(identifier)) ;
        final EndpointReferenceType registrationService = new EndpointReferenceType(new AttributedURIType("http://www.example.com/registrationService")) ;
        coordinationContext.setRegistrationService(registrationService) ;
        
        final TestActivationRequesterCallback callback = new TestActivationRequesterCallback() {
            public void createCoordinationContextResponse(final CreateCoordinationContextResponseType createCoordinationContextResponse, final AddressingContext addressingContext)
            {
                assertEquals(addressingContext.getTo().getValue(), activationRequesterService.getAddress().getValue());
                assertEquals(addressingContext.getFrom().getAddress().getValue(), activationCoordinatorService.getAddress().getValue());
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                final CoordinationContextType coordinationContext = createCoordinationContextResponse.getCoordinationContext() ;
                assertNotNull(coordinationContext) ;
                assertEquals(coordinationType, coordinationContext.getCoordinationType().getValue()) ;
                assertEquals(identifier, coordinationContext.getIdentifier().getValue()) ;
                assertEquals(registrationService.getAddress().getValue(), coordinationContext.getRegistrationService().getAddress().getValue()) ;
            }
        };
        final ActivationRequesterProcessor requester = ActivationRequesterProcessor.getRequester() ;
        requester.registerCallback(relatesTo, callback) ;
        try
        {
            ActivationRequesterClient.getClient().sendCreateCoordinationResponse(addressingContext, coordinationContext) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            requester.removeCallback(relatesTo) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void doCreateCoordinationContextError(final String messageId)
        throws Exception
    {
        final String relatesTo = "doCreateCoordinationContextError" ;
        final String reason = "doCreateCoordinationContextErrorReason" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(activationRequesterService, messageId) ;
        addressingContext.addRelatesTo(new RelationshipType(relatesTo)) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = CoordinationConstants.WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME ;
        final SoapFault soapFault = new SoapFault10(soapFaultType, subcode, reason) ;
        
        final TestActivationRequesterCallback callback = new TestActivationRequesterCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingContext addressingContext)
            {
                assertEquals(addressingContext.getTo().getValue(), activationRequesterService.getAddress().getValue());
                assertEquals(addressingContext.getFrom().getAddress().getValue(), activationCoordinatorService.getAddress().getValue());
                assertNull(addressingContext.getReplyTo());
                assertEquals(addressingContext.getMessageID().getValue(), messageId);
                
                assertNotNull(soapFault) ;
                assertEquals(soapFaultType, soapFault.getSoapFaultType()) ;
                assertEquals(subcode, soapFault.getSubcode()) ;
                assertEquals(reason, soapFault.getReason()) ;
            }
        };
        final ActivationRequesterProcessor requester = ActivationRequesterProcessor.getRequester() ;
        requester.registerCallback(relatesTo, callback) ;
        
        try
        {
            ActivationRequesterClient.getClient().sendSoapFault(addressingContext, soapFault) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            requester.removeCallback(relatesTo) ;
        }
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void doRegisterRequest(final String messageId)
        throws Exception
    {
        final String protocolIdentifier = TestUtil.PROTOCOL_IDENTIFIER ;
        final EndpointReferenceType participantProtocolService = new EndpointReferenceType(new AttributedURIType(TestUtil.PROTOCOL_PARTICIPANT_SERVICE)) ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(registrationCoordinatorService, messageId) ;
        RegistrationCoordinatorClient.getClient().sendRegister(addressingContext, protocolIdentifier, participantProtocolService) ;
        
        final RegisterDetails details = testRegistrationCoordinatorProcessor.getRegisterDetails(messageId, 10000) ;
        final RegisterType requestRegister = details.getRegister() ;
        final AddressingContext requestAddressingContext = details.getAddressingContext() ;
        final ArjunaContext requestArjunaContext = details.getArjunaContext() ;

        assertEquals(requestAddressingContext.getTo().getValue(), registrationCoordinatorService.getAddress().getValue());
        assertEquals(requestAddressingContext.getFrom().getAddress().getValue(), registrationRequesterService.getAddress().getValue());
        assertEquals(requestAddressingContext.getReplyTo().getAddress().getValue(), registrationRequesterService.getAddress().getValue());
        assertEquals(requestAddressingContext.getMessageID().getValue(), messageId);
        
        assertNull(requestArjunaContext.getInstanceIdentifier()) ;

        assertEquals(protocolIdentifier, requestRegister.getProtocolIdentifier().getValue()) ;
        assertEquals(participantProtocolService.getAddress().getValue(),
            requestRegister.getParticipantProtocolService().getAddress().getValue()) ;
    }

    public void doRegisterResponse(final String messageId)
        throws Exception
    {
        final String relatesTo = "doRegisterResponse" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(registrationRequesterService, messageId) ;
        addressingContext.addRelatesTo(new RelationshipType(relatesTo)) ;
        
        final EndpointReferenceType coordinationProtocolService = new EndpointReferenceType(new AttributedURIType(TestUtil.PROTOCOL_COORDINATOR_SERVICE)) ;
        
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

    public void doRegisterError(final String messageId)
        throws Exception
    {
        final String relatesTo = "doRegisterError" ;
        final String reason = "doRegisterErrorReason" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(registrationRequesterService, messageId) ;
        addressingContext.addRelatesTo(new RelationshipType(relatesTo)) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = CoordinationConstants.WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME ;
        final SoapFault soapFault = new SoapFault10(soapFaultType, subcode, reason) ;
        
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

    @After
    public void tearDown()
        throws Exception
    {
        ActivationCoordinatorProcessor.setCoordinator(origActivationCoordinatorProcessor) ;
        origActivationCoordinatorProcessor = null ;
        testActivationCoordinatorProcessor = null ;
        activationCoordinatorService = null ;
        activationRequesterService = null ;
        
        RegistrationCoordinatorProcessor.setCoordinator(origRegistrationCoordinatorProcessor) ;
        origRegistrationCoordinatorProcessor = null ;
        testRegistrationCoordinatorProcessor = null ;
        registrationCoordinatorService = null ;
        registrationRequesterService = null ;
    }
}
