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
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * ActivationTestCase.java
 */

package com.arjuna.wsc11.tests.junit;

import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.map.MAP;
import com.arjuna.webservices11.wscoor.client.ActivationCoordinatorClient;
import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc11.tests.TestUtil11;
import com.arjuna.wsc11.tests.junit.TestActivationCoordinatorProcessor.CreateCoordinationContextDetails;
import junit.framework.TestCase;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.*;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class ActivationTestCase extends TestCase
{
    private ActivationCoordinatorProcessor origActivationCoordinatorProcessor ;

    private TestActivationCoordinatorProcessor testActivationCoordinatorProcessor = new TestActivationCoordinatorProcessor() ;

    protected void setUp()
        throws Exception
    {
        origActivationCoordinatorProcessor = ActivationCoordinatorProcessor.setCoordinator(testActivationCoordinatorProcessor) ;
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
    }

    public void testRequestWithoutExpiresWithoutCurrentContext()
        throws Exception
    {
        final String messageId = "testRequestWithoutExpiresWithoutCurrentContext" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = null ;
        final CoordinationContext coordinationContext = null ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithExpiresWithoutCurrentContext()
        throws Exception
    {
        final String messageId = "testRequestWithExpiresWithoutCurrentContext" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = new Long(123456L) ;
        final CoordinationContext coordinationContext = null ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithoutExpiresWithCurrentContextWithoutExpires()
        throws Exception
    {
        final String messageId = "testRequestWithoutExpiresWithCurrentContextWithoutExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = null ;
        final CoordinationContext coordinationContext = new CoordinationContext() ;
        coordinationContext.setCoordinationType(coordinationType) ;
        CoordinationContextType.Identifier identifier = new CoordinationContextType.Identifier();
        identifier.setValue(TestUtil.PROTOCOL_IDENTIFIER);
        coordinationContext.setIdentifier(identifier) ;
        W3CEndpointReference registrationService = TestUtil11.getRegistrationEndpoint(identifier.getValue());
        coordinationContext.setRegistrationService(registrationService) ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithoutExpiresWithCurrentContextWithExpires()
        throws Exception
    {
        final String messageId = "testRequestWithoutExpiresWithCurrentContextWithExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = null ;
        final CoordinationContext coordinationContext = new CoordinationContext() ;
        coordinationContext.setCoordinationType(coordinationType) ;
        CoordinationContextType.Identifier identifier = new CoordinationContextType.Identifier();
        identifier.setValue(TestUtil.PROTOCOL_IDENTIFIER);
        coordinationContext.setIdentifier(identifier) ;
        Expires expiresInstance = new Expires();
        expiresInstance.setValue(123456L);
        coordinationContext.setExpires(expiresInstance);
        W3CEndpointReference registrationService = TestUtil11.getRegistrationEndpoint(identifier.getValue());
        coordinationContext.setRegistrationService(registrationService) ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithExpiresWithCurrentContextWithoutExpires()
        throws Exception
    {
        final String messageId = "testRequestWithExpiresWithCurrentContextWithoutExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = new Long(123456L) ;
        final CoordinationContext coordinationContext = new CoordinationContext() ;
        coordinationContext.setCoordinationType(coordinationType) ;
        CoordinationContextType.Identifier identifier = new CoordinationContextType.Identifier();
        identifier.setValue(TestUtil.PROTOCOL_IDENTIFIER);
        coordinationContext.setIdentifier(identifier) ;
        W3CEndpointReference registrationService = TestUtil11.getRegistrationEndpoint(identifier.getValue());
        coordinationContext.setRegistrationService(registrationService) ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    public void testRequestWithExpiresWithCurrentContextWithExpires()
        throws Exception
    {
        final String messageId = "testRequestWithExpiresWithCurrentContextWithExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = new Long(123456L) ;
        final CoordinationContext coordinationContext = new CoordinationContext() ;
        coordinationContext.setCoordinationType(coordinationType) ;
        CoordinationContextType.Identifier identifier = new CoordinationContextType.Identifier();
        identifier.setValue(TestUtil.PROTOCOL_IDENTIFIER);
        coordinationContext.setIdentifier(identifier) ;
        Expires expiresInstance = new Expires();
        expiresInstance.setValue(1234567L);
        coordinationContext.setExpires(expiresInstance);
        W3CEndpointReference registrationService = TestUtil11.getRegistrationEndpoint(identifier.getValue());
        coordinationContext.setRegistrationService(registrationService) ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
    }

    private void executeRequestTest(final String messageId, final String coordinationType, final Long expires, final CoordinationContext coordinationContext)
        throws Exception
    {
        final MAP map = AddressingHelper.createRequestContext(TestUtil11.activationCoordinatorService, messageId) ;
        Expires expiresInstance;
        if (expires == null) {
            expiresInstance = null;
        } else {
            expiresInstance = new Expires();
            expiresInstance.setValue(expires.longValue());
        }
        CreateCoordinationContextResponseType createCoordinationContextResponseType;

        createCoordinationContextResponseType =
                ActivationCoordinatorClient.getClient().sendCreateCoordination(map, coordinationType, expiresInstance, coordinationContext) ;

        final CreateCoordinationContextDetails details = testActivationCoordinatorProcessor.getCreateCoordinationContextDetails(messageId, 10000) ;
        final CreateCoordinationContextType requestCreateCoordinationContext = details.getCreateCoordinationContext() ;
        final MAP requestMAP = details.getMAP() ;

        assertNotNull(requestMAP.getTo());
        assertEquals(requestMAP.getTo(), TestUtil11.activationCoordinatorService);
        // we don't care about the reply to field --  this is an RPC style message
        // assertNotNull(requestMAP.getReplyTo());
        // assertTrue(AddressingHelper.isNoneReplyTo(requestMAP));
        assertNotNull(requestMAP.getMessageID());
        assertEquals(requestMAP.getMessageID(), messageId);

        if (expires == null)
        {
            assertNull(requestCreateCoordinationContext.getExpires()) ;
        }
        else
        {
            assertNotNull(requestCreateCoordinationContext.getExpires()) ;
            assertEquals(expires.longValue(), requestCreateCoordinationContext.getExpires().getValue());
        }
        if (coordinationContext == null)
        {
            assertNull(requestCreateCoordinationContext.getCurrentContext()) ;
        }
        else
        {
            assertNotNull(requestCreateCoordinationContext.getCurrentContext()) ;
            assertEquals(requestCreateCoordinationContext.getCurrentContext().getIdentifier().getValue(),
                coordinationContext.getIdentifier().getValue()) ;
            if (coordinationContext.getExpires() == null)
            {
                assertNull(requestCreateCoordinationContext.getCurrentContext().getExpires()) ;
            }
            else
            {
                assertNotNull(requestCreateCoordinationContext.getCurrentContext().getExpires()) ;
                assertEquals(requestCreateCoordinationContext.getCurrentContext().getExpires().getValue(),
                    coordinationContext.getExpires().getValue()) ;
            }
            assertNotNull(requestCreateCoordinationContext.getCurrentContext().getIdentifier());
            assertEquals(requestCreateCoordinationContext.getCurrentContext().getIdentifier().getValue(),
                coordinationContext.getIdentifier().getValue()) ;
        }
        assertNotNull(requestCreateCoordinationContext.getCoordinationType());
        assertEquals(requestCreateCoordinationContext.getCoordinationType(), coordinationType);

        // make sure we got a sensible response

        checkResponse(coordinationType, expiresInstance, createCoordinationContextResponseType);
    }

    public void checkResponse(final String coordinationType, final Expires expiresInstance,
                              final CreateCoordinationContextResponseType createCoordinationContextResponseType)
        throws Exception
    {
        CoordinationContext outContext = createCoordinationContextResponseType.getCoordinationContext();

        assertNotNull(outContext);
        assertEquals(coordinationType, outContext.getCoordinationType());
        if (expiresInstance != null) {
            assertNotNull(outContext.getExpires());
            assertEquals(expiresInstance.getValue(), outContext.getExpires().getValue());
        } else {
            assertNull(outContext.getExpires());
        }

        assertNotNull(outContext.getRegistrationService());
    }

    protected void tearDown()
        throws Exception
    {
        ActivationCoordinatorProcessor.setCoordinator(origActivationCoordinatorProcessor) ;
        origActivationCoordinatorProcessor = null ;
        testActivationCoordinatorProcessor = null ;
    }
}