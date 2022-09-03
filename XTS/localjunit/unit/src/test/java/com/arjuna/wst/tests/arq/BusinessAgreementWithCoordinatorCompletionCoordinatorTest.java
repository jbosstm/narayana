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
import com.arjuna.webservices11.wsba.client.CoordinatorCompletionParticipantClient;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.wst.tests.TestUtil;
import com.arjuna.wst.tests.WarDeployment;
import com.arjuna.wst.tests.arq.TestCoordinatorCompletionParticipantProcessor.CoordinatorCompletionParticipantDetails;

@RunWith(Arquillian.class)
public class BusinessAgreementWithCoordinatorCompletionCoordinatorTest extends BaseWSTTest{

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                CoordinatorCompletionParticipantDetails.class,
                TestCoordinatorCompletionParticipantProcessor.class);
    }

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
