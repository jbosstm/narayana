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

package com.arjuna.wst11.tests.junit;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.client.ParticipantClient;
import com.arjuna.webservices11.wsat.processors.ParticipantProcessor;
import com.arjuna.wst11.tests.junit.TestParticipantProcessor.ParticipantDetails;
import com.arjuna.wst11.tests.TestUtil;

public class TwoPCCoordinatorTestCase 
{
    private ParticipantProcessor origParticipantProcessor ;

    private TestParticipantProcessor testParticipantProcessor = new TestParticipantProcessor() ;

    @Before
    public void setUp()
        throws Exception
    {
        origParticipantProcessor = ParticipantProcessor.setProcessor(testParticipantProcessor) ;
    }

    @Test
    public void testSendPrepare()
        throws Exception
    {
        final String messageId = "testSendPrepare" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        final W3CEndpointReference participantEndpointReference = TestUtil.getParticipantEndpoint(null);

        ParticipantClient.getClient().sendPrepare(participantEndpointReference, map, new InstanceIdentifier("sender")) ;

        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasPrepare()) ;

        checkDetails(details, true, true, messageId, null);
    }

    @Test
    public void testSendCommit()
        throws Exception
    {
        final String messageId = "testSendCommit" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        final W3CEndpointReference participantEndpointReference = TestUtil.getParticipantEndpoint(null);

        ParticipantClient.getClient().sendCommit(participantEndpointReference, map, new InstanceIdentifier("sender")) ;

        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasCommit()) ;

        checkDetails(details, true, true, messageId, null);
    }

    @Test
    public void testSendRollback()
        throws Exception
    {
        final String messageId = "testSendRollback" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        final W3CEndpointReference participantEndpointReference = TestUtil.getParticipantEndpoint(null);

        ParticipantClient.getClient().sendRollback(participantEndpointReference, map, new InstanceIdentifier("sender")) ;

        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasRollback()) ;

        checkDetails(details, true, true, messageId, null);
    }

    @Test
    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendError" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;

        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault11(soapFaultType, subcode, reason) ;

        ParticipantClient.getClient().sendSoapFault(map, soapFault, new InstanceIdentifier("sender")) ;

        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        final SoapFault receivedSoapFault = details.getSoapFault() ;

        assertNotNull(receivedSoapFault) ;
        assertEquals(soapFaultType, receivedSoapFault.getSoapFaultType()) ;
        assertEquals(subcode, receivedSoapFault.getSubcode()) ;
        assertEquals(reason, receivedSoapFault.getReason()) ;

        checkDetails(details, false, false, messageId, null);
    }

    @After
    public void tearDown()
        throws Exception
    {
        ParticipantProcessor.setProcessor(origParticipantProcessor) ;
    }
    /**
     * check the message details to see that they have the correct to, from and faultto address and message id, a
     * none reply to address and an arjuna context containing the correct instannce identifier
     * @param details
     * @param hasFrom
     * @param hasFaultTo
     * @param messageId
     * @param instanceIdentifier
     */

    private void checkDetails(ParticipantDetails details, boolean hasFrom, boolean hasFaultTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        MAP inMAP = details.getMAP();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inMAP.getTo(), TestUtil.participantServiceURI);
        assertNotNull(inMAP.getReplyTo());
        assertTrue(AddressingHelper.isNoneReplyTo(inMAP));
        if (hasFrom) {
            assertNotNull(inMAP.getFrom());
            assertEquals(inMAP.getFrom().getAddress(), TestUtil.coordinatorServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        if (hasFaultTo) {
            assertNotNull(inMAP.getFaultTo());
            assertEquals(inMAP.getFaultTo().getAddress(), TestUtil.coordinatorServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        assertNotNull(inMAP.getMessageID());
        assertEquals(inMAP.getMessageID(), messageId);

        if (instanceIdentifier == null) {
            assertNull(inArjunaContext);
        } else {
            assertNotNull(inArjunaContext) ;
            assertEquals(instanceIdentifier.getInstanceIdentifier(), inArjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
        }
    }
}