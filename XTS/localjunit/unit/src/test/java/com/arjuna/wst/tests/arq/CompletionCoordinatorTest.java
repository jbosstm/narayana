/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.wst.tests.arq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
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
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.client.CompletionInitiatorClient;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorProcessor;
import com.arjuna.wst.tests.TestUtil;
import com.arjuna.wst.tests.WarDeployment;

@RunWith(Arquillian.class)
public class CompletionCoordinatorTest extends BaseWSTTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                TestCompletionInitiatorCallback.class);
    }

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