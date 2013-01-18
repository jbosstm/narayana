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
import javax.xml.ws.wsaddressing.W3CEndpointReference;

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
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.client.ParticipantCompletionParticipantClient;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.wst.tests.TestUtil;
import com.arjuna.wst.tests.WarDeployment;
import com.arjuna.wst.tests.arq.TestParticipantCompletionParticipantProcessor.ParticipantCompletionParticipantDetails;

@RunWith(Arquillian.class)
public class BusinessAgreementWithParticipantCompletionCoordinatorTest extends BaseWSTTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                ParticipantCompletionParticipantDetails.class,
                TestParticipantCompletionParticipantProcessor.class);
    }

    private ParticipantCompletionParticipantProcessor origParticipantCompletionParticipantProcessor ;
    private TestParticipantCompletionParticipantProcessor testParticipantCompletionParticipantProcessor = new TestParticipantCompletionParticipantProcessor() ;
    
    @Before
    public void setUp()
            throws Exception
            {
        origParticipantCompletionParticipantProcessor = ParticipantCompletionParticipantProcessor.setProcessor(testParticipantCompletionParticipantProcessor) ;
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
            }

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

        ParticipantCompletionParticipantClient.getClient().sendSoapFault(soapFault, null, map, TestUtil.getBusinessActivityFaultAction()) ;

        final ParticipantCompletionParticipantDetails details = testParticipantCompletionParticipantProcessor.getParticipantCompletionParticipantDetails(messageId, 10000) ;

        assertNotNull(details.getSoapFault());
        assertEquals(details.getSoapFault().getSoapFaultType(), soapFault.getSoapFaultType()) ;
        assertEquals(details.getSoapFault().getReason(), soapFault.getReason()) ;
        assertEquals(details.getSoapFault().getSubcode(), soapFault.getSubcode()) ;

        checkDetails(details, false, false, messageId, null);
            }

    @Test
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

    @After
    public void tearDown()
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
