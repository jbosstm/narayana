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
 * Copyright (c) 2004, Arjuna Technologies Limited.
 *
 * $Id: BusinessAgreementWithCoordinatorCompletionParticipantTestCase.java,v 1.1.2.1 2004/05/26 10:04:55 nmcl Exp $
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

public class BusinessAgreementWithCoordinatorCompletionParticipantTestCase
{
    private CoordinatorCompletionCoordinatorProcessor origCoordinatorCompletionCoordinatorProcessor ;
    private TestCoordinatorCompletionCoordinatorProcessor testCoordinatorCompletionCoordinatorProcessor = new TestCoordinatorCompletionCoordinatorProcessor() ;
    private String coordinatorCompletionCoordinatorServiceURI ;
    private String coordinatorCompletionParticipantServiceURI ;

    @Before
    public void setUp()
        throws Exception
    {
        origCoordinatorCompletionCoordinatorProcessor = CoordinatorCompletionCoordinatorProcessor.setProcessor(testCoordinatorCompletionCoordinatorProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        coordinatorCompletionCoordinatorServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_COORDINATOR) ;
        coordinatorCompletionParticipantServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_COORDINATOR_COMPLETION_PARTICIPANT) ;
    }

    @Test
    public void testSendClosed()
        throws Exception
    {
        final String messageId = "testSendClosed" ;
        AttributedURIType address = new AttributedURIType(coordinatorCompletionCoordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendClosed(addressingContext, new InstanceIdentifier("sender")) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasClosed());

        checkDetails(details, false, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCancelled()
        throws Exception
    {
        final String messageId = "testSendCancelled" ;
        AttributedURIType address = new AttributedURIType(coordinatorCompletionCoordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendCancelled(addressingContext, new InstanceIdentifier("sender")) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCancelled());

        checkDetails(details, false, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCompensated()
        throws Exception
    {
        final String messageId = "testSendCompensated" ;
        AttributedURIType address = new AttributedURIType(coordinatorCompletionCoordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendCompensated(addressingContext, new InstanceIdentifier("sender")) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCompensated());

        checkDetails(details, false, messageId, instanceIdentifier);
    }

    @Test
    public void testSendFault()
        throws Exception
    {
        final String messageId = "testSendFault" ;
        AttributedURIType address = new AttributedURIType(coordinatorCompletionCoordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        final String exceptionIdentifier = "testSendFaultExceptionIdentifier" ;

        CoordinatorCompletionCoordinatorClient.getClient().sendFault(addressingContext, new InstanceIdentifier("sender"), exceptionIdentifier) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasFault());
        assertEquals(details.hasFault().getExceptionIdentifier(), exceptionIdentifier);

        checkDetails(details, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCompleted()
        throws Exception
    {
        final String messageId = "testSendCompleted" ;
        AttributedURIType address = new AttributedURIType(coordinatorCompletionCoordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendCompleted(addressingContext, new InstanceIdentifier("sender")) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCompleted());

        checkDetails(details, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendStatus()
        throws Exception
    {
        final String messageId = "testSendStatus" ;
        AttributedURIType address = new AttributedURIType(coordinatorCompletionCoordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("6") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        final State state = State.STATE_COMPENSATING;

        CoordinatorCompletionCoordinatorClient.getClient().sendStatus(addressingContext, new InstanceIdentifier("sender"), state) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasStatus());
        assertEquals(details.hasStatus().getState(), state);

        checkDetails(details, true, messageId, instanceIdentifier);
    }
    /*
     * cannot test this any longer as client does not provide API to send soap fault

    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendError" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("7") ;
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(addressingContext, soapFault, instanceIdentifier) ;

        final CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasSoapFault()) ;
        assertEquals(details.hasSoapFault().getSoapFaultType(), soapFaultType) ;
        assertEquals(details.hasSoapFault().getReason(), reason) ;
        assertEquals(details.hasSoapFault().getSubcode(), subcode) ;

        checkDetails(details, false, messageId, instanceIdentifier);

    }
     */

    @Test
    public void testSendExit()
        throws Exception
    {
        final String messageId = "testSendExit" ;
        AttributedURIType address = new AttributedURIType(coordinatorCompletionCoordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("8") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendExit(addressingContext, new InstanceIdentifier("sender")) ;
        
        final CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;
        
        assertTrue(details.hasExit()) ;

        checkDetails(details, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        AttributedURIType address = new AttributedURIType(coordinatorCompletionCoordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("9") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendGetStatus(addressingContext, new InstanceIdentifier("sender")) ;
        
        final CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;
        
        assertTrue(details.hasGetStatus()) ;

        checkDetails(details, true, messageId, instanceIdentifier);
    }

    @After
    public void tearDown()
        throws Exception
    {
        CoordinatorCompletionCoordinatorProcessor.setProcessor(origCoordinatorCompletionCoordinatorProcessor) ;
    }
    /**
     * check the message details to see that they have the correct to and from address and message id, a null
     * reply to address and an arjuna context containing the correct instannce identifier
     * @param details
     * @param messageId
     * @param instanceIdentifier
     */

    private void checkDetails(CoordinatorCompletionCoordinatorDetails details, boolean replyTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        AddressingContext inAddressingContext = details.getAddressingContext();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inAddressingContext.getTo().getValue(), coordinatorCompletionCoordinatorServiceURI);
        assertEquals(inAddressingContext.getFrom().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
        if (replyTo) {
            assertEquals(inAddressingContext.getReplyTo().getAddress().getValue(), coordinatorCompletionParticipantServiceURI);
        } else {
            assertNull(inAddressingContext.getReplyTo());
        }
        assertEquals(inAddressingContext.getMessageID().getValue(), messageId);

        if (instanceIdentifier == null) {
            assertNull(inArjunaContext.getInstanceIdentifier());
        } else {
            assertNotNull(inArjunaContext.getInstanceIdentifier()) ;
            assertEquals(instanceIdentifier.getInstanceIdentifier(), inArjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
        }
    }
}
