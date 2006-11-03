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
 * ActivationTestCase.java
 */

package com.arjuna.wsc.tests.junit;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wsaddr.RelationshipType;
import com.arjuna.webservices.wscoor.AttributedUnsignedIntType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextType;
import com.arjuna.webservices.wscoor.client.ActivationCoordinatorClient;
import com.arjuna.webservices.wscoor.client.ActivationRequesterClient;
import com.arjuna.webservices.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.webservices.wscoor.processors.ActivationRequesterProcessor;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc.tests.junit.TestActivationCoordinatorProcessor.CreateCoordinationContextDetails;

public class ActivationTestCase extends TestCase
{
    private ActivationCoordinatorProcessor origActivationCoordinatorProcessor ;
    
    private TestActivationCoordinatorProcessor testActivationCoordinatorProcessor = new TestActivationCoordinatorProcessor() ;
    private EndpointReferenceType activationCoordinatorService ;
    private EndpointReferenceType activationRequesterService ;
    
    protected void setUp()
        throws Exception
    {
        origActivationCoordinatorProcessor = ActivationCoordinatorProcessor.setCoordinator(testActivationCoordinatorProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String activationCoordinatorServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_COORDINATOR) ;
        activationCoordinatorService = new EndpointReferenceType(new AttributedURIType(activationCoordinatorServiceURI)) ;
        final String activationRequesterServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_REQUESTER) ;
        activationRequesterService = new EndpointReferenceType(new AttributedURIType(activationRequesterServiceURI)) ;
    }

    public void testRequestWithoutExpiresWithoutCurrentContext()
        throws Exception
    {
        final String messageId = "testRequestWithoutExpiresWithoutCurrentContext" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = null ;
        final CoordinationContextType coordinationContext = null ;
        
        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }
    
    public void testRequestWithExpiresWithoutCurrentContext()
        throws Exception
    {
        final String messageId = "testRequestWithExpiresWithoutCurrentContext" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = new Long(123456L) ;
        final CoordinationContextType coordinationContext = null ;
        
        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithoutExpiresWithCurrentContextWithoutExpires()
        throws Exception
    {
        final String messageId = "testRequestWithoutExpiresWithCurrentContextWithoutExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = null ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        coordinationContext.setCoordinationType(new URI(coordinationType)) ;
        coordinationContext.setRegistrationService(activationCoordinatorService) ;
        coordinationContext.setIdentifier(new AttributedURIType(TestUtil.PROTOCOL_IDENTIFIER)) ;
        
        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithoutExpiresWithCurrentContextWithExpires()
        throws Exception
    {
        final String messageId = "testRequestWithoutExpiresWithCurrentContextWithExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = null ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        coordinationContext.setCoordinationType(new URI(coordinationType)) ;
        coordinationContext.setRegistrationService(activationCoordinatorService) ;
        coordinationContext.setIdentifier(new AttributedURIType(TestUtil.PROTOCOL_IDENTIFIER)) ;
        coordinationContext.setExpires(new AttributedUnsignedIntType(123456L)) ;
        
        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithExpiresWithCurrentContextWithoutExpires()
        throws Exception
    {
        final String messageId = "testRequestWithExpiresWithCurrentContextWithoutExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = new Long(123456L) ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        coordinationContext.setCoordinationType(new URI(coordinationType)) ;
        coordinationContext.setRegistrationService(activationCoordinatorService) ;
        coordinationContext.setIdentifier(new AttributedURIType(TestUtil.PROTOCOL_IDENTIFIER)) ;
        
        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithExpiresWithCurrentContextWithExpires()
        throws Exception
    {
        final String messageId = "testRequestWithExpiresWithCurrentContextWithExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = new Long(123456L) ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        coordinationContext.setCoordinationType(new URI(coordinationType)) ;
        coordinationContext.setRegistrationService(activationCoordinatorService) ;
        coordinationContext.setIdentifier(new AttributedURIType(TestUtil.PROTOCOL_IDENTIFIER)) ;
        coordinationContext.setExpires(new AttributedUnsignedIntType(1234567L)) ;
        
        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }
    
    private void executeRequestTest(final String messageId, final String coordinationType, final Long expires, final CoordinationContextType coordinationContext)
        throws Exception
    {
        final AddressingContext addressingContext = AddressingContext.createRequestContext(activationCoordinatorService, messageId) ;
        final AttributedUnsignedIntType expiresValue = (expires == null ? null : new AttributedUnsignedIntType(expires.longValue())) ;
        ActivationCoordinatorClient.getClient().sendCreateCoordination(addressingContext, coordinationType, expiresValue, coordinationContext) ;
        
        final CreateCoordinationContextDetails details = testActivationCoordinatorProcessor.getCreateCoordinationContextDetails(messageId, 10000) ;
        final CreateCoordinationContextType requestCreateCoordinationContext = details.getCreateCoordinationContext() ;
        final AddressingContext requestAddressingContext = details.getAddressingContext() ;
    
        assertEquals(requestAddressingContext.getTo().getValue(), activationCoordinatorService.getAddress().getValue());
        assertEquals(requestAddressingContext.getFrom().getAddress().getValue(), activationRequesterService.getAddress().getValue());
        assertEquals(requestAddressingContext.getReplyTo().getAddress().getValue(), activationRequesterService.getAddress().getValue());
        assertEquals(requestAddressingContext.getMessageID().getValue(), messageId);
        
        if (expires == null)
        {
            assertNull(requestCreateCoordinationContext.getExpires()) ;
        }
        else
        {
            assertEquals(expires.longValue(), requestCreateCoordinationContext.getExpires().getValue());
        }
        if (coordinationContext == null)
        {
            assertNull(requestCreateCoordinationContext.getCurrentContext()) ;
        }
        else
        {
            assertNotNull(requestCreateCoordinationContext.getCurrentContext()) ;
            assertEquals(requestCreateCoordinationContext.getCurrentContext().getIdentifier().getValue(),
                coordinationContext.getIdentifier().getValue()) ;
            if (coordinationContext.getExpires() == null)
            {
                assertNull(requestCreateCoordinationContext.getCurrentContext().getExpires()) ;
            }
            else
            {
                assertEquals(requestCreateCoordinationContext.getCurrentContext().getExpires().getValue(),
                    coordinationContext.getExpires().getValue()) ;
            }
            assertEquals(requestCreateCoordinationContext.getCurrentContext().getIdentifier().getValue(),
                coordinationContext.getIdentifier().getValue()) ;
        }
        assertEquals(requestCreateCoordinationContext.getCoordinationType().getValue(), coordinationType);
    }

    public void testResponse()
        throws Exception
    {
        final String messageId = "123456" ;
        final String relatesTo = "testResponse" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(activationRequesterService, messageId) ;
        addressingContext.addRelatesTo(new RelationshipType(relatesTo)) ;
        
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final String identifier = TestUtil.PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        coordinationContext.setCoordinationType(new URI(coordinationType)) ;
        coordinationContext.setRegistrationService(activationCoordinatorService) ;
        coordinationContext.setIdentifier(new AttributedURIType(identifier)) ;
        
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

    public void testError()
        throws Exception
    {
        final String messageId = "123456" ;
        final String relatesTo = "testResponse" ;
        final String reason = "testResponseReason" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(activationRequesterService, messageId) ;
        addressingContext.addRelatesTo(new RelationshipType(relatesTo)) ;
        
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = CoordinationConstants.WSCOOR_ERROR_CODE_ALREADY_REGISTERED_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        
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
    
    protected void tearDown()
        throws Exception
    {
        ActivationCoordinatorProcessor.setCoordinator(origActivationCoordinatorProcessor) ;
        origActivationCoordinatorProcessor = null ;
        testActivationCoordinatorProcessor = null ;
        activationCoordinatorService = null ;
        activationRequesterService = null ;
    }
}
