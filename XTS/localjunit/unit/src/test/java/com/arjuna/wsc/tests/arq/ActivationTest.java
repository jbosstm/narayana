/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
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

package com.arjuna.wsc.tests.arq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.api.addressing.MAP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.Expires;

import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wscoor.client.ActivationCoordinatorClient;
import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc.tests.TestUtil11;
import com.arjuna.wsc.tests.WarDeployment;
import com.arjuna.wsc.tests.arq.TestActivationCoordinatorProcessor.CreateCoordinationContextDetails;


@RunWith(Arquillian.class)
public class ActivationTest extends BaseWSCTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                TestActivationCoordinatorProcessor.class,
                CreateCoordinationContextDetails.class);
    }

    private ActivationCoordinatorProcessor origActivationCoordinatorProcessor ;

    private TestActivationCoordinatorProcessor testActivationCoordinatorProcessor = new TestActivationCoordinatorProcessor() ;

    @Before
    public void setUp()
            throws Exception
            {
        origActivationCoordinatorProcessor = ActivationCoordinatorProcessor.setCoordinator(testActivationCoordinatorProcessor) ;
        ServiceRegistry.getRegistry() ;
            }

    @Test
    public void testRequestWithoutExpiresWithoutCurrentContext()
            throws Exception
            {
        final String messageId = "testRequestWithoutExpiresWithoutCurrentContext" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = null ;
        final CoordinationContext coordinationContext = null ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
            }

    @Test
    public void testRequestWithExpiresWithoutCurrentContext()
            throws Exception
            {
        final String messageId = "testRequestWithExpiresWithoutCurrentContext" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = 123456L;
        final CoordinationContext coordinationContext = null ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
            }

    @Test
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

    @Test
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

    @Test
    public void testRequestWithExpiresWithCurrentContextWithoutExpires()
            throws Exception
            {
        final String messageId = "testRequestWithExpiresWithCurrentContextWithoutExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = 123456L;
        final CoordinationContext coordinationContext = new CoordinationContext() ;
        coordinationContext.setCoordinationType(coordinationType) ;
        CoordinationContextType.Identifier identifier = new CoordinationContextType.Identifier();
        identifier.setValue(TestUtil.PROTOCOL_IDENTIFIER);
        coordinationContext.setIdentifier(identifier) ;
        W3CEndpointReference registrationService = TestUtil11.getRegistrationEndpoint(identifier.getValue());
        coordinationContext.setRegistrationService(registrationService) ;

        executeRequestTest(messageId, coordinationType, expires, coordinationContext) ;
            }

    @Test
    public void testRequestWithExpiresWithCurrentContextWithExpires()
            throws Exception
            {
        final String messageId = "testRequestWithExpiresWithCurrentContextWithExpires" ;
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final Long expires = 123456L;
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
            expiresInstance.setValue(expires);
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
    @After
    public void tearDown()
            throws Exception
            {
        ActivationCoordinatorProcessor.setCoordinator(origActivationCoordinatorProcessor) ;
        origActivationCoordinatorProcessor = null ;
        testActivationCoordinatorProcessor = null ;
            }
}
