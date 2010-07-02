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
 * $Id: BusinessAgreementWithCoordinatorCompletionParticipantTestCase.java,v 1.1.2.1 2004/05/26 10:04:55 nmcl Exp $
 */

package com.arjuna.wst11.tests.junit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsba.State;
import com.arjuna.webservices11.wsba.client.CoordinatorCompletionCoordinatorClient;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionCoordinatorProcessor;
import com.arjuna.wst11.tests.junit.TestCoordinatorCompletionCoordinatorProcessor.CoordinatorCompletionCoordinatorDetails;
import com.arjuna.wst11.tests.TestUtil;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.namespace.QName;

public class BusinessAgreementWithCoordinatorCompletionParticipantTestCase 
{
    private CoordinatorCompletionCoordinatorProcessor origCoordinatorCompletionCoordinatorProcessor ;
    private TestCoordinatorCompletionCoordinatorProcessor testCoordinatorCompletionCoordinatorProcessor = new TestCoordinatorCompletionCoordinatorProcessor() ;

    @Before
    public void setUp()
        throws Exception
    {
        origCoordinatorCompletionCoordinatorProcessor = CoordinatorCompletionCoordinatorProcessor.setProcessor(testCoordinatorCompletionCoordinatorProcessor) ;
    }

    @Test
    public void testSendClosed()
        throws Exception
    {
        final String messageId = "testSendClosed" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendClosed(endpoint, map, new InstanceIdentifier("sender")) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasClosed());

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCancelled()
        throws Exception
    {
        final String messageId = "testSendCancelled" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendCancelled(endpoint, map, new InstanceIdentifier("sender")) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCancelled());

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCompensated()
        throws Exception
    {
        final String messageId = "testSendCompensated" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("3") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendCompensated(endpoint, map, new InstanceIdentifier("sender")) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCompensated());

        checkDetails(details, false, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendFault()
        throws Exception
    {
        final String messageId = "testSendFault" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("4") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;

        final State state = State.STATE_FAILING_COMPENSATING ;

        CoordinatorCompletionCoordinatorClient.getClient().sendFail(endpoint, map, new InstanceIdentifier("sender"), state.getValue()) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasFail());
        assertEquals(details.hasFail().getExceptionIdentifier(), state.getValue());

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendCompleted()
        throws Exception
    {
        final String messageId = "testSendCompleted" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("5") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendCompleted(endpoint, map, new InstanceIdentifier("sender")) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCompleted());

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendStatus()
        throws Exception
    {
        final String messageId = "testSendStatus" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("6") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;

        final State state = State.STATE_COMPENSATING;

        CoordinatorCompletionCoordinatorClient.getClient().sendStatus(endpoint, map, new InstanceIdentifier("sender"), state.getValue()) ;

        CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasStatus());
        assertEquals(details.hasStatus().getState(), state.getValue());

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendError" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("7") ;
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault11 soapFault = new SoapFault11(soapFaultType, subcode, reason) ;

        // this would be a better test if we could set the identifier as a reference parameter here 
        CoordinatorCompletionCoordinatorClient.getClient().sendSoapFault(soapFault, null, map, TestUtil.getBusinessActivityFaultAction()) ;

        final CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertNotNull(details.hasSoapFault()) ;
        assertEquals(details.hasSoapFault().getSoapFaultType(), soapFaultType) ;
        assertEquals(details.hasSoapFault().getReason(), reason) ;
        assertEquals(details.hasSoapFault().getSubcode(), subcode) ;

        checkDetails(details, false, false, messageId, null);
    }

    @Test
    public void testSendExit()
        throws Exception
    {
        final String messageId = "testSendExit" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("8") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendExit(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasExit()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @Test
    public void testSendGetStatus()
        throws Exception
    {
        final String messageId = "testSendGetStatus" ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("9") ;
        W3CEndpointReference endpoint = TestUtil.getCoordinatorCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());
        final MAP map = AddressingHelper.createRequestContext(TestUtil.coordinatorCompletionCoordinatorServiceURI, messageId) ;

        CoordinatorCompletionCoordinatorClient.getClient().sendGetStatus(endpoint, map, new InstanceIdentifier("sender")) ;

        final CoordinatorCompletionCoordinatorDetails details = testCoordinatorCompletionCoordinatorProcessor.getCoordinatorCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasGetStatus()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
    }

    @After
    public void tearDown()
        throws Exception
    {
        CoordinatorCompletionCoordinatorProcessor.setProcessor(origCoordinatorCompletionCoordinatorProcessor) ;
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

    private void checkDetails(CoordinatorCompletionCoordinatorDetails details, boolean hasFrom, boolean hasFaultTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        MAP inMAP = details.getMAP();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inMAP.getTo(), TestUtil.coordinatorCompletionCoordinatorServiceURI);
        assertNotNull(inMAP.getReplyTo());
        assertTrue(AddressingHelper.isNoneReplyTo(inMAP));
        if (hasFrom) {
            assertNotNull(inMAP.getFrom());
            assertEquals(inMAP.getFrom().getAddress(), TestUtil.coordinatorCompletionParticipantServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        if (hasFaultTo) {
            assertNotNull(inMAP.getFaultTo());
            assertEquals(inMAP.getFaultTo().getAddress(), TestUtil.coordinatorCompletionParticipantServiceURI);
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