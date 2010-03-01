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
 * CompletionCoordinatorTestCase.java
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
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.client.CompletionInitiatorClient;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorProcessor;
import com.arjuna.wst11.tests.TestUtil;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

public class CompletionCoordinatorTestCase 
{
    @Before
    public void setUp()
        throws Exception
    {
    }

    @Test
    public void testSendCommitted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendCommitted" ;
        final W3CEndpointReference completionInitiatorEndpoint = TestUtil.getCompletionInitiatorEndpoint(instanceIdentifier);
        final MAP map = AddressingHelper.createRequestContext(TestUtil.completionInitiatorServiceURI, messageId) ;

        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void committed(final Notification committed, final MAP map, final ArjunaContext arjunaContext)
            {
                assertEquals(map.getTo(), TestUtil.completionInitiatorServiceURI);
                assertNull(map.getFrom());
                assertNotNull(map.getFaultTo());
                assertEquals(map.getFaultTo().getAddress(), TestUtil.completionCoordinatorServiceURI);
                assertNotNull(map.getReplyTo());
                assertTrue(AddressingHelper.isNoneReplyTo(map));
                assertEquals(map.getMessageID(), messageId);

                assertNotNull(arjunaContext) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CompletionInitiatorProcessor initiator = CompletionInitiatorProcessor.getProcessor() ;
        initiator.registerCallback(instanceIdentifier, callback) ;

        try
        {
            CompletionInitiatorClient.getClient().sendCommitted(completionInitiatorEndpoint, map, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }

        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    @Test
    public void testSendAborted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendAborted" ;
        final W3CEndpointReference completionInitiatorEndpoint = TestUtil.getCompletionInitiatorEndpoint(instanceIdentifier);
        final MAP map = AddressingHelper.createRequestContext(TestUtil.completionInitiatorServiceURI, messageId) ;

        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void aborted(final Notification aborted, final MAP map, final ArjunaContext arjunaContext)
            {
                assertEquals(map.getTo(), TestUtil.completionInitiatorServiceURI);
                assertNull(map.getFrom());
                assertNotNull(map.getFaultTo());
                assertEquals(map.getFaultTo().getAddress(), TestUtil.completionCoordinatorServiceURI);
                assertNotNull(map.getReplyTo());
                assertTrue(AddressingHelper.isNoneReplyTo(map));
                assertNotNull(map.getMessageID());
                assertEquals(map.getMessageID(), messageId);

                assertNotNull(arjunaContext) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CompletionInitiatorProcessor initiator = CompletionInitiatorProcessor.getProcessor() ;
        initiator.registerCallback(instanceIdentifier, callback) ;

        try
        {
            CompletionInitiatorClient.getClient().sendAborted(completionInitiatorEndpoint, map, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }

        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    @Test
    public void testSendError()
        throws Exception
    {
        final String messageId = "123456" ;
        final String reason = "testSendErrorReason" ;
        final String instanceIdentifier = "testSendError" ;
        final W3CEndpointReference completionInitiatorEndpoint = TestUtil.getCompletionInitiatorEndpoint(instanceIdentifier);
        final MAP map = AddressingHelper.createRequestContext(TestUtil.completionInitiatorServiceURI, messageId) ;

        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault11(soapFaultType, subcode, reason) ;

        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void soapFault(final SoapFault soapFault, final MAP map, final ArjunaContext arjunaContext)
            {
                assertEquals(map.getTo(), TestUtil.completionInitiatorServiceURI);
                assertNull(map.getFrom());
                assertNull(map.getFaultTo());
                assertNotNull(map.getReplyTo());
                assertTrue(AddressingHelper.isNoneReplyTo(map));
                assertNotNull(map.getMessageID());
                assertEquals(map.getMessageID(), messageId);

                assertNotNull(arjunaContext) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;

                assertNotNull(soapFault) ;
                assertEquals(soapFaultType, soapFault.getSoapFaultType()) ;
                assertEquals(subcode, soapFault.getSubcode()) ;
                assertEquals(reason, soapFault.getReason()) ;
            }
        };
        final CompletionInitiatorProcessor initiator = CompletionInitiatorProcessor.getProcessor() ;
        initiator.registerCallback(instanceIdentifier, callback) ;

        try
        {
            CompletionInitiatorClient.getClient().sendSoapFault(completionInitiatorEndpoint, map, soapFault, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }

        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    @After
    public void tearDown()
        throws Exception
    {
    }
}