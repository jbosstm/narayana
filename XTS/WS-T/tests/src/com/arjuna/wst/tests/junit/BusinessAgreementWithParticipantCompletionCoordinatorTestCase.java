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
        origParticipantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.setProcessor(testParticipantCompletionParticipantProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        participantCompletionParticipantServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_PARTICIPANT) ;
        participantCompletionCoordinatorServiceURI = soapRegistry.getServiceURI(BusinessActivityConstants.SERVICE_PARTICIPANT_COMPLETION_COORDINATOR) ;
    }

    public void testSendClose()
        throws Exception
    {
        final String messageId = "testSendClose" ;
        AttributedURIType address = new AttributedURIType(participantCompletionParticipantServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendClose(addressingContext, new InstanceIdentifier("sender")) ;
        
        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasClose()) ;

        checkDetails(details, true, messageId, instanceIdentifier);
    }

    public void testSendCancel()
        throws Exception
    {
        final String messageId = "testSendCancel" ;
        AttributedURIType address = new AttributedURIType(participantCompletionParticipantServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendCancel(addressingContext, new InstanceIdentifier("sender")) ;
        
        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCancel()) ;

        checkDetails(details, true, messageId, instanceIdentifier);
    }

    public void testSendCompensate()
        throws Exception
    {
        final String messageId = "testSendCompensate" ;
        AttributedURIType address = new AttributedURIType(participantCompletionParticipantServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendCompensate(addressingContext, new InstanceIdentifier("sender")) ;
        
        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCompensate()) ;

        checkDetails(details, true, messageId, instanceIdentifier);
    }

    public void testSendFaulted()
        throws Exception
    {
        final String messageId = "testSendFaulted" ;
        AttributedURIType address = new AttributedURIType(participantCompletionParticipantServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendFaulted(addressingContext, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasFaulted()) ;

        checkDetails(details, false, messageId, instanceIdentifier);
    }

    public void testSendExited()
        throws Exception
    {
        final String messageId = "testSendExited" ;
        AttributedURIType address = new AttributedURIType(participantCompletionParticipantServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendExited(addressingContext, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasExited()) ;

        checkDetails(details, false, messageId, instanceIdentifier);
    }

    public void testSendStatus()
        throws Exception
    {
        final String messageId = "testSendStatus" ;
        AttributedURIType address = new AttributedURIType(participantCompletionParticipantServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("6") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        final State state = State.STATE_ACTIVE ;

        ParticipantCompletionParticipantClient.getClient().sendStatus(addressingContext, new InstanceIdentifier("sender"), state) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertNotNull(details.hasStatus()); ;
        assertEquals(details.hasStatus().getState(), state);

        checkDetails(details, true, messageId, instanceIdentifier);
    }
    
    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        AttributedURIType address = new AttributedURIType(participantCompletionParticipantServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("7") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendGetStatus(addressingContext, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasGetStatus()) ;

        checkDetails(details, true, messageId, instanceIdentifier);
    }
    /*
     * cannot test this any longer as client does not provide API to send soap fault

    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("8") ;
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;

        ParticipantCompletionParticipantClient.getClient().sendSoapFault(addressingContext, soapFault, instanceIdentifier) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertNotNull(details.hasSoapFault()); ;
        assertEquals(details.hasSoapFault().getSoapfaultType(), soapFault.getSoapFaultType()) ;
        assertEquals(details.hasSoapFault().getReason(), soapFault.getReason()) ;
        assertEquals(details.hasSoapFault().getSubcode(), soapFault.getSubcode()) ;

        checkDetails(details, false, messageId, instanceIdentifier);
    }
     */
    protected void tearDown()
        throws Exception
    {
        ParticipantCompletionParticipantProcessor.setProcessor(origParticipantCompletionParticipantProcessor) ;
    }

    /**
     * check the message details to see that they have the correct to and from address and message id, a null
     * reply to address and an arjuna context containing the correct instannce identifier
     * @param details
     * @param replyTo
     * @param messageId
     * @param instanceIdentifier
     */

    private void checkDetails(ParticipantCompletionParticipantDetails details, boolean replyTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        AddressingContext inAddressingContext = details.getAddressingContext();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inAddressingContext.getTo().getValue(), participantCompletionParticipantServiceURI);
        assertEquals(inAddressingContext.getFrom().getAddress().getValue(), participantCompletionCoordinatorServiceURI);
        if (replyTo) {
            assertEquals(inAddressingContext.getReplyTo().getAddress().getValue(), participantCompletionCoordinatorServiceURI);
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
