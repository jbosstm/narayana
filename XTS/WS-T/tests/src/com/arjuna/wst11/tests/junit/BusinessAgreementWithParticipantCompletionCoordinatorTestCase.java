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

package com.arjuna.wst11.tests.junit;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.map.MAP;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.client.ParticipantCompletionParticipantClient;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wst11.tests.junit.TestParticipantCompletionParticipantProcessor.ParticipantCompletionParticipantDetails;
import com.arjuna.wst11.tests.TestUtil;

public class BusinessAgreementWithParticipantCompletionCoordinatorTestCase extends TestCase
{
    private ParticipantCompletionParticipantProcessor origParticipantCompletionParticipantProcessor ;
    private TestParticipantCompletionParticipantProcessor testParticipantCompletionParticipantProcessor = new TestParticipantCompletionParticipantProcessor() ;

    protected void setUp()
        throws Exception
    {
        origParticipantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.setProcessor(testParticipantCompletionParticipantProcessor) ;
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
    }

    public void testSendClose()
        throws Exception
    {
        final String messageId = "testSendClose" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendClose(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasClose()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    public void testSendCancel()
        throws Exception
    {
        final String messageId = "testSendCancel" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendCancel(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasCancel()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    public void testSendCompensate()
        throws Exception
    {
        final String messageId = "testSendCompensate" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendCompensate(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasCompensate()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    public void testSendFaulted()
        throws Exception
    {
        final String messageId = "testSendFaulted" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendFailed(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasFaulted()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }

    public void testSendExited()
        throws Exception
    {
        final String messageId = "testSendExited" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendExited(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasExited()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }

    public void testSendStatus()
        throws Exception
    {
        final String messageId = "testSendStatus" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("6") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;

        final State state = State.STATE_ACTIVE ;

        ParticipantCompletionParticipantClient.getClient().sendStatus(endpoint, map, new InstanceIdentifier("sender"), state.getValue()) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertNotNull(details.hasStatus()); ;
        assertEquals(details.hasStatus().getState(), state.getValue());

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("7") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendGetStatus(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasGetStatus()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("8") ;
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault11 soapFault = new SoapFault11(soapFaultType, subcode, reason) ;

        AddressingHelper.installNoneReplyTo(map);
        SoapFaultClient.sendSoapFault(soapFault, map, TestUtil.getBusinessActivityFaultAction()) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertNotNull(details.getSoapFault());
        assertEquals(details.getSoapFault().getSoapFaultType(), soapFault.getSoapFaultType()) ;
        assertEquals(details.getSoapFault().getReason(), soapFault.getReason()) ;
        assertEquals(details.getSoapFault().getSubcode(), soapFault.getSubcode()) ;

        checkDetails(details, false, false, messageId, null);
    }

    public void testSendNotCompleted()
        throws Exception
    {
        final String messageId = "testSendNotCompleted" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("9") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionParticipantEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionParticipantServiceURI, messageId) ;

        ParticipantCompletionParticipantClient.getClient().sendNotCompleted(endpoint, map, new InstanceIdentifier("sender")); ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertTrue(details.hasNotCompleted()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }
    protected void tearDown()
        throws Exception
    {
        ParticipantCompletionParticipantProcessor.setProcessor(origParticipantCompletionParticipantProcessor) ;
    }

    /**
     * check the message details to see that they have the correct to and from address and message id, a null
     * reply to address and an arjuna context containing the correct instannce identifier
     * @param details
     * @param hasFrom
     * @param hasFaultTo
     * @param messageId
     * @param instanceIdentifier
     */

    private void checkDetails(ParticipantCompletionParticipantDetails details, boolean hasFrom, boolean hasFaultTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        MAP inMAP = details.getMAP();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inMAP.getTo(), TestUtil.participantCompletionParticipantServiceURI);
        assertNotNull(inMAP.getReplyTo());
        assertTrue(AddressingHelper.isNoneReplyTo(inMAP));
        if (hasFrom) {
            assertNotNull(inMAP.getFrom());
            assertEquals(inMAP.getFrom().getAddress(), TestUtil.participantCompletionCoordinatorServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        if (hasFaultTo) {
            assertNotNull(inMAP.getFaultTo());
            assertEquals(inMAP.getFaultTo().getAddress(), TestUtil.participantCompletionCoordinatorServiceURI);
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