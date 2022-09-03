/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.wst.tests.arq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import javax.xml.namespace.QName;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.api.addressing.MAP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.client.ParticipantCompletionCoordinatorClient;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionCoordinatorProcessor;
import com.arjuna.wst.tests.TestUtil;
import com.arjuna.wst.tests.WarDeployment;
import com.arjuna.wst.tests.arq.TestParticipantCompletionCoordinatorProcessor.ParticipantCompletionCoordinatorDetails;

@RunWith(Arquillian.class)
public class BusinessAgreementWithParticipantCompletionParticipantTest extends BaseWSTTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                ParticipantCompletionCoordinatorDetails.class,
                TestParticipantCompletionCoordinatorProcessor.class);
    }

    private ParticipantCompletionCoordinatorProcessor origParticipantCompletionCoordinatorProcessor ;
    private TestParticipantCompletionCoordinatorProcessor testParticipantCompletionCoordinatorProcessor = new TestParticipantCompletionCoordinatorProcessor() ;

    @Before
    public void setUp()
            throws Exception
            {
        origParticipantCompletionCoordinatorProcessor = ParticipantCompletionCoordinatorProcessor.setProcessor(testParticipantCompletionCoordinatorProcessor) ;
            }

    @Test
    public void testSendClosed()
            throws Exception
            {
        final String messageId = "testSendClosed" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        ParticipantCompletionCoordinatorClient.getClient().sendClosed(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasClosed()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendCancelled()
            throws Exception
            {
        final String messageId = "testSendCancelled" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        ParticipantCompletionCoordinatorClient.getClient().sendCancelled(endpoint, map, new InstanceIdentifier("sender")); ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCancelled()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendCompensated()
            throws Exception
            {
        final String messageId = "testSendCompensated" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        ParticipantCompletionCoordinatorClient.getClient().sendCompensated(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCompensated()) ;

        checkDetails(details, false, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendCompleted()
            throws Exception
            {
        final String messageId = "testSendCompleted" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        ParticipantCompletionCoordinatorClient.getClient().sendCompleted(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCompleted()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendStatus()
            throws Exception
            {
        final String messageId = "testSendStatus" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        final State state = State.STATE_ENDED ;

        ParticipantCompletionCoordinatorClient.getClient().sendStatus(endpoint, map, new InstanceIdentifier("sender"), state.getValue()) ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasStatus()); ;
        assertEquals(details.hasStatus().getState(), state.getValue()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendError()
            throws Exception
            {
        final String messageId = "testSendError" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("6") ;
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault11 soapFault = new SoapFault11(soapFaultType, subcode, reason) ;

        ParticipantCompletionCoordinatorClient.getClient().sendSoapFault(soapFault, null, map, TestUtil.getBusinessActivityFaultAction()) ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasSoapFault()) ;
        assertEquals(details.hasSoapFault().getSoapFaultType(), soapFault.getSoapFaultType()) ;
        assertEquals(details.hasSoapFault().getSubcode(), soapFault.getSubcode()) ;
        assertEquals(details.hasSoapFault().getReason(), soapFault.getReason()) ;

        checkDetails(details, false, false, messageId, null);
            }

    @Test
    public void testSendExit()
            throws Exception
            {
        final String messageId = "testSendExit" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("7") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        ParticipantCompletionCoordinatorClient.getClient().sendExit(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasExit()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendFault()
            throws Exception
            {
        final String messageId = "testSendFault" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("8") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        final State state = State.STATE_FAILING_ACTIVE;

        ParticipantCompletionCoordinatorClient.getClient().sendFail(endpoint, map, new InstanceIdentifier("sender"), state.getValue()); ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasFault());
        assertEquals(details.hasFault().getExceptionIdentifier(), state.getValue()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendGetStatus()
            throws Exception
            {
        final String messageId = "testSendGetStatus" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("9") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        ParticipantCompletionCoordinatorClient.getClient().sendGetStatus(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasGetStatus()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendCannotComplete()
            throws Exception
            {
        final String messageId = "testSendCannotComplete" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("10") ;
        W3CEndpointReference endpoint = TestUtil.getParticipantCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.participantCompletionCoordinatorServiceURI, messageId) ;

        ParticipantCompletionCoordinatorClient.getClient().sendCannotComplete(endpoint, map, new InstanceIdentifier("sender")) ;

        final ParticipantCompletionCoordinatorDetails details = testParticipantCompletionCoordinatorProcessor.getParticipantCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCannotComplete()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @After
    public void tearDown()
            throws Exception
            {
        ParticipantCompletionCoordinatorProcessor.setProcessor(origParticipantCompletionCoordinatorProcessor) ;
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

    private void checkDetails(ParticipantCompletionCoordinatorDetails details, boolean hasFrom, boolean hasFaultTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        MAP inMAP = details.getMAP();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inMAP.getTo(), TestUtil.participantCompletionCoordinatorServiceURI);
        assertNotNull(inMAP.getReplyTo());
        assertTrue(AddressingHelper.isNoneReplyTo(inMAP));
        if (hasFrom) {
            assertNotNull(inMAP.getFrom());
            assertEquals(inMAP.getFrom().getAddress(), TestUtil.participantCompletionParticipantServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        if (hasFaultTo) {
            assertNotNull(inMAP.getFaultTo());
            assertEquals(inMAP.getFaultTo().getAddress(), TestUtil.participantCompletionParticipantServiceURI);
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
