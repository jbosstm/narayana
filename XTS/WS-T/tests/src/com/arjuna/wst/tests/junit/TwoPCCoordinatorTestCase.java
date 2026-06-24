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
 * $Id: TwoPCCoordinatorTestCase.java,v 1.6.2.1 2005/11/22 10:37:45 kconner Exp $
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
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarj.ArjunaContext;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.client.ParticipantClient;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.wst.tests.junit.TestParticipantProcessor.ParticipantDetails;

public class TwoPCCoordinatorTestCase 
{
    private ParticipantProcessor origParticipantProcessor ;
    
    private TestParticipantProcessor testParticipantProcessor = new TestParticipantProcessor() ;
    private String participanURIService;
    private String coordinatorServiceURI;

    @Before
    public void setUp()
        throws Exception
    {
        origParticipantProcessor = ParticipantProcessor.setProcessor(testParticipantProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        participanURIService = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_PARTICIPANT) ;
        coordinatorServiceURI = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COORDINATOR) ;
    }

    @Test
    public void testSendPrepare()
        throws Exception
    {
        final String messageId = "testSendPrepare" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participanURIService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;

        ParticipantClient.getClient().sendPrepare(addressingContext, new InstanceIdentifier("sender")) ;
        
        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasPrepare()) ;
        // expect reply to address but don't expect identifier
        checkDetails(details, true, messageId, null);
    }

    @Test
    public void testSendCommit()
        throws Exception
    {
        final String messageId = "testSendCommit" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participanURIService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;

        ParticipantClient.getClient().sendCommit(addressingContext, new InstanceIdentifier("sender")) ;
        
        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCommit()) ;
        // expect reply to address but don't expect identifier
        checkDetails(details, true, messageId, null);
    }

    @Test
    public void testSendRollback()
        throws Exception
    {
        final String messageId = "testSendRollback" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participanURIService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;

        ParticipantClient.getClient().sendRollback(addressingContext, new InstanceIdentifier("sender")) ;
        
        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasRollback()) ;
        // expect reply to address but don't expect identifier
        checkDetails(details, true, messageId, null);
    }

    @Test
    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendError" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participanURIService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault10(soapFaultType, subcode, reason) ;
        
        ParticipantClient.getClient().sendSoapFault(addressingContext, soapFault, new InstanceIdentifier("sender")) ;
        
        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        final SoapFault receivedSoapFault = details.getSoapFault() ;
        
        assertNotNull(receivedSoapFault) ;
        assertEquals(soapFaultType, receivedSoapFault.getSoapFaultType()) ;
        assertEquals(subcode, receivedSoapFault.getSubcode()) ;
        assertEquals(reason, receivedSoapFault.getReason()) ;
        // don't expect reply to address or identifier
        checkDetails(details, false, messageId, null);
    }

    @After
    public void tearDown()
        throws Exception
    {
        ParticipantProcessor.setProcessor(origParticipantProcessor) ;
    }
    /**
     * check the message details to see that they have the correct to and from address and message id, a null
     * reply to address and an arjuna context containing the correct instannce identifier
     * @param details
     * @param replyTo
     * @param messageId
     * @param instanceIdentifier
     */

    private void checkDetails(ParticipantDetails details, boolean replyTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        AddressingContext inAddressingContext = details.getAddressingContext();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inAddressingContext.getTo().getValue(), participanURIService);
        assertEquals(inAddressingContext.getFrom().getAddress().getValue(), coordinatorServiceURI);
        if (replyTo) {
            assertEquals(inAddressingContext.getReplyTo().getAddress().getValue(), coordinatorServiceURI);
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