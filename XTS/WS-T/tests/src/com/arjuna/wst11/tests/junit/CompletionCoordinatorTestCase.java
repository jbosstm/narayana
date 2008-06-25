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
import javax.xml.ws.addressing.AddressingProperties;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.client.CompletionInitiatorClient;
import com.arjuna.webservices11.wsat.processors.CompletionInitiatorProcessor;
import com.arjuna.wst11.tests.junit.TestCompletionInitiatorCallback;
import com.arjuna.wst11.tests.TestUtil;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;

public class CompletionCoordinatorTestCase extends TestCase
{
    protected void setUp()
        throws Exception
    {
    }

    public void testSendCommitted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendCommitted" ;
        final W3CEndpointReference completionInitiatorEndpoint = TestUtil.getCompletionInitiatorEndpoint(instanceIdentifier);
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(TestUtil.completionInitiatorServiceURI, messageId) ;

        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void committed(final Notification committed, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingProperties.getTo().getURI().toString(), TestUtil.completionInitiatorServiceURI);
                assertNull(addressingProperties.getFrom());
                assertNotNull(addressingProperties.getFaultTo());
                assertEquals(addressingProperties.getFaultTo().getAddress().getURI().toString(), TestUtil.completionCoordinatorServiceURI);
                assertNotNull(addressingProperties.getReplyTo());
                assertTrue(AddressingHelper.isNoneReplyTo(addressingProperties));
                assertEquals(addressingProperties.getMessageID().getURI().toString(), messageId);

                assertNotNull(arjunaContext) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CompletionInitiatorProcessor initiator = CompletionInitiatorProcessor.getProcessor() ;
        initiator.registerCallback(instanceIdentifier, callback) ;

        try
        {
            CompletionInitiatorClient.getClient().sendCommitted(completionInitiatorEndpoint, addressingProperties, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }

        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendAborted()
        throws Exception
    {
        final String messageId = "123456" ;
        final String instanceIdentifier = "testSendAborted" ;
        final W3CEndpointReference completionInitiatorEndpoint = TestUtil.getCompletionInitiatorEndpoint(instanceIdentifier);
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(TestUtil.completionInitiatorServiceURI, messageId) ;

        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void aborted(final Notification aborted, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingProperties.getTo().getURI().toString(), TestUtil.completionInitiatorServiceURI);
                assertNull(addressingProperties.getFrom());
                assertNotNull(addressingProperties.getFaultTo());
                assertEquals(addressingProperties.getFaultTo().getAddress().getURI().toString(), TestUtil.completionCoordinatorServiceURI);
                assertNotNull(addressingProperties.getReplyTo());
                assertTrue(AddressingHelper.isNoneReplyTo(addressingProperties));
                assertNotNull(addressingProperties.getMessageID());
                assertEquals(addressingProperties.getMessageID().getURI().toString(), messageId);

                assertNotNull(arjunaContext) ;
                assertEquals(instanceIdentifier, arjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
            }
        };
        final CompletionInitiatorProcessor initiator = CompletionInitiatorProcessor.getProcessor() ;
        initiator.registerCallback(instanceIdentifier, callback) ;

        try
        {
            CompletionInitiatorClient.getClient().sendAborted(completionInitiatorEndpoint, addressingProperties, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }

        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    public void testSendError()
        throws Exception
    {
        final String messageId = "123456" ;
        final String reason = "testSendErrorReason" ;
        final String instanceIdentifier = "testSendError" ;
        final W3CEndpointReference completionInitiatorEndpoint = TestUtil.getCompletionInitiatorEndpoint(instanceIdentifier);
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(TestUtil.completionInitiatorServiceURI, messageId) ;

        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault11(soapFaultType, subcode, reason) ;

        final TestCompletionInitiatorCallback callback = new TestCompletionInitiatorCallback() {
            public void soapFault(final SoapFault soapFault, final AddressingProperties addressingProperties, final ArjunaContext arjunaContext)
            {
                assertEquals(addressingProperties.getTo().getURI().toString(), TestUtil.completionInitiatorServiceURI);
                assertNull(addressingProperties.getFrom());
                assertNull(addressingProperties.getFaultTo());
                assertNotNull(addressingProperties.getReplyTo());
                assertTrue(AddressingHelper.isNoneReplyTo(addressingProperties));
                assertNotNull(addressingProperties.getMessageID());
                assertEquals(addressingProperties.getMessageID().getURI().toString(), messageId);

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
            CompletionInitiatorClient.getClient().sendSoapFault(completionInitiatorEndpoint, addressingProperties, soapFault, new InstanceIdentifier("sender")) ;
            callback.waitUntilTriggered() ;
        }
        finally
        {
            initiator.removeCallback(instanceIdentifier) ;
        }

        assertTrue(callback.hasTriggered()) ;
        assertFalse(callback.hasFailed()) ;
    }

    protected void tearDown()
        throws Exception
    {
    }
}