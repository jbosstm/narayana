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

package com.arjuna.wst11.tests.arq;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;

import static org.junit.Assert.*;

import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsat.client.CoordinatorClient;
import com.arjuna.webservices11.wsat.processors.CoordinatorProcessor;
import com.arjuna.wst11.tests.arq.TestCoordinatorProcessor.CoordinatorDetails;
import com.arjuna.wst11.tests.TestUtil;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;

public class TwoPCParticipantTestCase 
{
    private CoordinatorProcessor origCoordinatorProcessor ;
    private TestCoordinatorProcessor testCoordinatorProcessor = new TestCoordinatorProcessor();

    public void setUp()
        throws Exception
    {
        origCoordinatorProcessor = CoordinatorProcessor.getProcessor();
        CoordinatorProcessor.setProcessor(testCoordinatorProcessor);
    }

    
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