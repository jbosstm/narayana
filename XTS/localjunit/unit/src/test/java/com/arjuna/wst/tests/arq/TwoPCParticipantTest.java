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

import javax.inject.Inject;
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
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.client.CoordinatorClient;
import com.arjuna.webservices11.wsat.processors.CoordinatorProcessor;
import com.arjuna.wst.tests.TestUtil;
import com.arjuna.wst.tests.WarDeployment;
import com.arjuna.wst.tests.arq.TestCoordinatorProcessor.CoordinatorDetails;

@RunWith(Arquillian.class)
public class TwoPCParticipantTest extends BaseWSTTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                TestCoordinatorProcessor.class,
                CoordinatorDetails.class);
    }

    private CoordinatorProcessor origCoordinatorProcessor ;
    private TestCoordinatorProcessor testCoordinatorProcessor = new TestCoordinatorProcessor();

    @Before
    public void setUp()
            throws Exception
            {
        origCoordinatorProcessor = CoordinatorProcessor.getProcessor();
        CoordinatorProcessor.setProcessor(testCoordinatorProcessor);
            }

    @Test
    public void testSendPrepared()
            throws Exception
            {
        final String messageId = "testSendPrepared" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        W3CEndpointReference coordinatorEndpoint = TestUtil.getCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorServiceURI, messageId) ;

        CoordinatorClient.getClient().sendPrepared(coordinatorEndpoint, map, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasPrepared());

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendAborted()
            throws Exception
            {
        final String messageId = "testSendAborted" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        W3CEndpointReference coordinatorEndpoint = TestUtil.getCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorServiceURI, messageId) ;

        CoordinatorClient.getClient().sendAborted(coordinatorEndpoint, map, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasAborted());

        checkDetails(details, false, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendReadOnly()
            throws Exception
            {
        final String messageId = "testSendReadOnly" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        W3CEndpointReference coordinatorEndpoint = TestUtil.getCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorServiceURI, messageId) ;

        CoordinatorClient.getClient().sendReadOnly(coordinatorEndpoint, map, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasReadOnly());

        checkDetails(details, false, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendCommitted()
            throws Exception
            {
        final String messageId = "testSendCommitted" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        W3CEndpointReference coordinatorEndpoint = TestUtil.getCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorServiceURI, messageId) ;

        CoordinatorClient.getClient().sendCommitted(coordinatorEndpoint, map, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertTrue(details.hasCommitted());

        checkDetails(details, false, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendError()
            throws Exception
            {
        final String messageId = "testSendError" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5");
        W3CEndpointReference coordinatorEndpoint = TestUtil.getCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorServiceURI, messageId);
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault11 soapFault = new SoapFault11(soapFaultType, subcode, reason) ;

        CoordinatorClient.getClient().sendSoapFault(coordinatorEndpoint, map, soapFault, new InstanceIdentifier("sender"));

        CoordinatorDetails details = testCoordinatorProcessor.getCoordinatorDetails(messageId, 10000);
        assertNotNull(details.hasSoapFault());
        assertEquals(details.hasSoapFault().getSoapFaultType(), soapFault.getSoapFaultType());
        assertEquals(details.hasSoapFault().getReason(), soapFault.getReason());
        assertEquals(details.hasSoapFault().getSubcode(), soapFault.getSubcode());

        checkDetails(details, false, false, messageId, instanceIdentifier);
            }

    @After
    public void tearDown()
            throws Exception
            {
        CoordinatorProcessor.setProcessor(origCoordinatorProcessor);
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

    private void checkDetails(CoordinatorDetails details, boolean hasFrom, boolean hasFaultTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        MAP inMAP = details.getMAP();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inMAP.getTo(), TestUtil.coordinatorServiceURI);
        assertNotNull(inMAP.getReplyTo());
        assertTrue(AddressingHelper.isNoneReplyTo(inMAP));
        if (hasFrom) {
            assertNotNull(inMAP.getFrom());
            assertEquals(inMAP.getFrom().getAddress(), TestUtil.participantServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        if (hasFaultTo) {
            assertNotNull(inMAP.getFaultTo());
            assertEquals(inMAP.getFaultTo().getAddress(), TestUtil.participantServiceURI);
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
