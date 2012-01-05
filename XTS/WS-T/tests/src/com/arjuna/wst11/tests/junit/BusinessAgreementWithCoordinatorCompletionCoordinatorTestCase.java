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
 * $Id: BusinessAgreementWithCoordinatorCompletionCoordinatorTestCase.java,v 1.1.2.1 2004/05/26 10:04:55 nmcl Exp $
 */

package com.arjuna.wst11.tests.junit;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.client.CoordinatorCompletionParticipantClient;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.wst11.tests.junit.TestCoordinatorCompletionParticipantProcessor.CoordinatorCompletionParticipantDetails;
import com.arjuna.wst11.tests.TestUtil;

public class BusinessAgreementWithCoordinatorCompletionCoordinatorTestCase
{
    private CoordinatorCompletionParticipantProcessor origCoordinatorCompletionParticipantProcessor ;
    private TestCoordinatorCompletionParticipantProcessor testCoordinatorCompletionParticipantProcessor = new TestCoordinatorCompletionParticipantProcessor() ;

    @Before
    public void setUp()
        throws Exception
    {
        origCoordinatorCompletionParticipantProcessor = CoordinatorCompletionParticipantProcessor.setProcessor(testCoordinatorCompletionParticipantProcessor) ;
    }

    @Test
    public void testSendClose()
        throws Exception
    {
        final String messageId = "testSendClose" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        CoordinatorCompletionParticipantClient.getClient().sendClose(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasClose()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCancel()
        throws Exception
    {
        final String messageId = "testSendCancel" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        CoordinatorCompletionParticipantClient.getClient().sendCancel(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasCancel()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCompensate()
        throws Exception
    {
        final String messageId = "testSendCompensate" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        CoordinatorCompletionParticipantClient.getClient().sendCompensate(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasCompensate()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendFaulted()
        throws Exception
    {
        final String messageId = "testSendFaulted" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        CoordinatorCompletionParticipantClient.getClient().sendFailed(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasFailed()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendExited()
        throws Exception
    {
        final String messageId = "testSendExited" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        CoordinatorCompletionParticipantClient.getClient().sendExited(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasExited()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendStatus()
        throws Exception
    {
        final String messageId = "testSendStatus" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("6") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        final State state = State.STATE_ACTIVE ;

        CoordinatorCompletionParticipantClient.getClient().sendStatus(endpoint, map, new InstanceIdentifier("sender"), state.getValue()) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertNotNull(details.hasStatus()) ;
        assertEquals(details.hasStatus().getState(), state.getValue()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendComplete()
        throws Exception
    {
        final String messageId = "testSendComplete" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("7") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        CoordinatorCompletionParticipantClient.getClient().sendComplete(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasComplete()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("8") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        CoordinatorCompletionParticipantClient.getClient().sendGetStatus(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasGetStatus()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendError" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("9") ;
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault11 soapFault = new SoapFault11(soapFaultType, subcode, reason) ;

        // this would be a better test if we could set the identifier as a reference parameter here
        CoordinatorCompletionParticipantClient.getClient().sendSoapFault(soapFault, null, map, TestUtil.getBusinessActivityFaultAction()) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertNotNull(details.hasSoapFault()) ;
        assertEquals(details.hasSoapFault().getSoapFaultType(), soapFaultType) ;
        assertEquals(details.hasSoapFault().getReason(), reason) ;
        assertEquals(details.hasSoapFault().getSubcode(), subcode) ;

        checkDetails(details, false, false, messageId, null);
    }

    @Test
    public void testSendNotCompleted()
        throws Exception
    {
        final String messageId = "testSendNotCompleted" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("10") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionParticipantServiceURI, messageId) ;

        CoordinatorCompletionParticipantClient.getClient().sendNotCompleted(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionParticipantDetails details = testCoordinatorCompletionParticipantProcessor.getCoordinatorCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasNotCompleted()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }

    @After
    public void tearDown()
        throws Exception
    {
        CoordinatorCompletionParticipantProcessor.setProcessor(origCoordinatorCompletionParticipantProcessor) ;
    }

    /**
     * check the message details to see that they have the correct to and from address and message id, a null
     * reply to address and an arjuna context containing the correct instannce identifier
     * @param details
     * @param messageId
     * @param instanceIdentifier
     */

    private void checkDetails(CoordinatorCompletionParticipantDetails details, boolean hasFrom, boolean hasFaultTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        MAP inMAP = details.getMAP();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inMAP.getTo(), TestUtil.coordinatorCompletionParticipantServiceURI);
        assertNotNull(inMAP.getReplyTo());
        assertTrue(AddressingHelper.isNoneReplyTo(inMAP));
        if (hasFrom) {
            assertNotNull(inMAP.getFrom());
            assertEquals(inMAP.getFrom().getAddress(), TestUtil.coordinatorCompletionCoordinatorServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        if (hasFaultTo) {
            assertNotNull(inMAP.getFaultTo());
            assertEquals(inMAP.getFaultTo().getAddress(), TestUtil.coordinatorCompletionCoordinatorServiceURI);
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