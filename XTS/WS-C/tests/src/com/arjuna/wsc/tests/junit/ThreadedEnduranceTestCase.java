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
 * ThreadedEnduranceTestCase.java
 */

package com.arjuna.wsc.tests.junit;

import java.util.Random;

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
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.webservices.wscoor.CreateCoordinationContextResponseType;
import com.arjuna.webservices.wscoor.RegisterResponseType;
import com.arjuna.webservices.wscoor.client.ActivationRequesterClient;
import com.arjuna.webservices.wscoor.client.RegistrationRequesterClient;
import com.arjuna.webservices.wscoor.processors.ActivationRequesterProcessor;
import com.arjuna.webservices.wscoor.processors.RegistrationRequesterProcessor;
import com.arjuna.wsc.tests.TestUtil;

public class ThreadedEnduranceTestCase extends TestCase
{
    private EndpointReferenceType activationCoordinatorService ;
    private EndpointReferenceType activationRequesterService ;
    
    private EndpointReferenceType registrationCoordinatorService ;
    private EndpointReferenceType registrationRequesterService ;
    
    private static final long TEST_DURATION = 60 * 1000;
    private static final int  TEST_THREADS  = 4;

    protected void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String activationCoordinatorServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_COORDINATOR) ;
        activationCoordinatorService = new EndpointReferenceType(new AttributedURIType(activationCoordinatorServiceURI)) ;
        final String activationRequesterServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_ACTIVATION_REQUESTER) ;
        activationRequesterService = new EndpointReferenceType(new AttributedURIType(activationRequesterServiceURI)) ;
        
        final String registrationCoordinatorServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
        registrationCoordinatorService = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorServiceURI)) ;
        final String registrationRequesterServiceURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_REQUESTER) ;
        registrationRequesterService = new EndpointReferenceType(new AttributedURIType(registrationRequesterServiceURI)) ;
    }

    public void testCreateCoordinationContextResponse()
        throws Exception
    {
        invoke(0);
    }

    public void testCreateCoordinationContextError()
        throws Exception
    {
        invoke(1);
    }

    public void testRegisterResponse()
        throws Exception
    {
        invoke(2);
    }

    public void testRegisterError()
        throws Exception
    {
        invoke(3);
    }

    public void invoke(int operation)
        throws Exception
    {
        InvokerThread[] threads = new InvokerThread[TEST_THREADS];

        for (int threadIndex = 0; threadIndex < TEST_THREADS; threadIndex++)
        {
            threads[threadIndex] = new InvokerThread(operation, Integer.toString(threadIndex));
            threads[threadIndex].start();
        }

        for (int threadIndex = 0; threadIndex < TEST_THREADS; threadIndex++)
            threads[threadIndex].join();

        for (int threadIndex = 0; threadIndex < TEST_THREADS; threadIndex++)
            assertFalse(threads[threadIndex].isFailed());
    }

    private class InvokerThread extends Thread
    {
        public InvokerThread(int operation, String dialogIdentifier)
        {
            super("JUnit Invoker Thread: " + operation + ", " + dialogIdentifier) ;
            _operation        = operation;
            _dialogIdentifier = dialogIdentifier;
        }

        public void run()
        {
            try
            {
                long startTime = System.currentTimeMillis();

                if (_operation == 0)
                {
                    while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
                        doCreateCoordinationContextResponse(_dialogIdentifier);
                }
                else if (_operation == 1)
                {
                    while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
                        doCreateCoordinationContextError(_dialogIdentifier);
                }
                else if (_operation == 2)
                {
                    while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
                        doRegisterResponse(_dialogIdentifier);
                }
                else
                {
                    while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
                        doRegisterError(_dialogIdentifier);
                }
            }
            catch (Exception exception)
            {
                System.out.print("Exception: ");
                exception.printStackTrace(System.out);
                _failed = true;
            }
            catch (Error error)
            {
                System.out.print("Error: ");
                error.printStackTrace(System.out);
                _failed = true;
            }
        }

        public boolean isFailed()
        {
            return _failed;
        }

        private boolean _failed           = false;
        private int     _operation        = 0;
        private String  _dialogIdentifier = null;
    }

    public void doCreateCoordinationContextResponse(final String messageId)
        throws Exception
    {
        final String relatesTo = "doCreateCoordinationContextResponse" + messageId ;
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
        
        Thread.sleep(_random.nextInt(6));
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void doCreateCoordinationContextError(final String messageId)
        throws Exception
    {
        final String relatesTo = "doCreateCoordinationContextError" + messageId ;
        final String reason = "doCreateCoordinationContextErrorReason" ;
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
        
        Thread.sleep(_random.nextInt(6));
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void doRegisterResponse(final String messageId)
        throws Exception
    {
        final String relatesTo = "doRegisterResponse" + messageId ;
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
        
        Thread.sleep(_random.nextInt(6));
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void doRegisterError(final String messageId)
        throws Exception
    {
        final String relatesTo = "doRegisterError" + messageId ;
        final String reason = "doRegisterErrorReason" ;
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
        
        Thread.sleep(_random.nextInt(6));
        
        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    protected void tearDown()
        throws Exception
    {
        activationCoordinatorService = null ;
        activationRequesterService = null ;
        registrationCoordinatorService = null ;
        registrationRequesterService = null ;
    }
    
    private Random                  _random                            = new Random();
}
