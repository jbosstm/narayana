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
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: TwoPCParticipantTestCase.java,v 1.6.2.1 2005/11/22 10:37:46 kconner Exp $
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
import com.arjuna.webservices.wsat.client.CoordinatorClient;
import com.arjuna.webservices.wsat.processors.CoordinatorProcessor;
import com.arjuna.wst.tests.junit.TestCoordinatorProcessor.CoordinatorDetails;

public class TwoPCParticipantTestCase 
{
    private CoordinatorProcessor origCoordinatorProcessor ;
    private TestCoordinatorProcessor testCoordinatorProcessor = new TestCoordinatorProcessor();

    @Before
    public void setUp()
        throws Exception
    {
        origCoordinatorProcessor = CoordinatorProcessor.getProcessor();
        CoordinatorProcessor.setProcessor(testCoordinatorProcessor);

        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        coordinatorServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COORDINATOR) ;
        participantServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_PARTICIPANT) ;
    }

    @Test
    public void testSendPrepared()
        throws Exception
    {
        final String messageId = "testSendPrepared" ;
        AttributedURIType address = new AttributedURIType(coordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorClient.getClient().sendPrepared(addressingContext, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasPrepared());
        // expect reply to address and identifier
        checkDetails(details, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendAborted()
        throws Exception
    {
        final String messageId = "testSendAborted" ;
        AttributedURIType address = new AttributedURIType(coordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorClient.getClient().sendAborted(addressingContext, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasAborted());
        // don't expect reply to address but do expect identifier
        checkDetails(details, false, messageId, instanceIdentifier);
   }

    @Test
    public void testSendReadOnly()
        throws Exception
    {
        final String messageId = "testSendReadOnly" ;
        AttributedURIType address = new AttributedURIType(coordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorClient.getClient().sendReadOnly(addressingContext, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasReadOnly());
        // don't expect reply to address but do expect identifier
        checkDetails(details, false, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCommitted()
        throws Exception
    {
        final String messageId = "testSendCommitted" ;
        AttributedURIType address = new AttributedURIType(coordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorClient.getClient().sendCommitted(addressingContext, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasCommitted());
        // don't expect reply to address but do expect identifier
        checkDetails(details, false, messageId, instanceIdentifier);
    }

    @Test
    public void testSendReplay()
        throws Exception
    {
        final String messageId = "testSendReplay" ;
        AttributedURIType address = new AttributedURIType(coordinatorServiceURI);
        EndpointReferenceType epr = new EndpointReferenceType(address);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5") ;
        InstanceIdentifier.setEndpointInstanceIdentifier(epr, instanceIdentifier);
        final AddressingContext addressingContext = AddressingContext.createRequestContext(epr, messageId) ;

        CoordinatorClient.getClient().sendReplay(addressingContext, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasReplay());
        // expect reply to address and identifier
        checkDetails(details, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendReplay" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(coordinatorServiceURI, messageId);
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5");
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault10(soapFaultType, subcode, reason) ;

        // for this test we use the soap fault client to send a message where we have no valid instance identifier
        // we could also test the case where we have an instance identifier but it never gets exercised anyway!
        CoordinatorClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertNotNull(details.hasSoapFault());
        assertEquals(details.hasSoapFault().getSoapFaultType(), soapFault.getSoapFaultType());
        assertEquals(details.hasSoapFault().getReason(), soapFault.getReason());
        assertEquals(details.hasSoapFault().getSubcode(), soapFault.getSubcode());
        // don't expect reply to address nor an identifier
        checkDetails(details, false, messageId, null);
    }

    @After
    public void tearDown()
        throws Exception
    {
        CoordinatorProcessor.setProcessor(origCoordinatorProcessor);
    }

    /**
     * check the message details to see that they have the correct to and from address and message id, a null
     * reply to address and an arjuna context containing the correct instannce identifier
     * @param details
     * @param replyTo
     * @param messageId
     * @param instanceIdentifier
     */

    private void checkDetails(CoordinatorDetails details, boolean replyTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        AddressingContext inAddressingContext = details.getAddressingContext();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inAddressingContext.getTo().getValue(), coordinatorServiceURI);
        assertEquals(inAddressingContext.getFrom().getAddress().getValue(), participantServiceURI);
        if (replyTo) {
            assertEquals(inAddressingContext.getReplyTo().getAddress().getValue(), participantServiceURI);
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

    private String coordinatorServiceURI ;
    private String participantServiceURI ;
}
