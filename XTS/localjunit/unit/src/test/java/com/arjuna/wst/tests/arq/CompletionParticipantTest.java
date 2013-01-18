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

import javax.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.api.addressing.MAP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsat.client.CompletionCoordinatorClient;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wst.tests.TestUtil;
import com.arjuna.wst.tests.WarDeployment;
import com.arjuna.wst.tests.arq.TestCompletionCoordinatorProcessor.CompletionCoordinatorDetails;

@RunWith(Arquillian.class)
public class CompletionParticipantTest extends BaseWSTTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                TestCompletionCoordinatorProcessor.class,
                CompletionCoordinatorDetails.class);
    }

    private CompletionCoordinatorProcessor origCompletionCoordinatorProcessor ;

    private TestCompletionCoordinatorProcessor testCompletionCoordinatorProcessor = new TestCompletionCoordinatorProcessor() ;

    @Before
    public void setUp()
            throws Exception
            {
        origCompletionCoordinatorProcessor = CompletionCoordinatorProcessor.setProcessor(testCompletionCoordinatorProcessor) ;
            }
    
    @Test
    public void testSendCommit()
            throws Exception
            {
        final String messageId = "testSendCommit" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.completionCoordinatorServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("1") ;
        final W3CEndpointReference endpoint = TestUtil.getCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());

        CompletionCoordinatorClient.getClient().sendCommit(endpoint, map, new InstanceIdentifier("sender")) ;

        final CompletionCoordinatorDetails details = testCompletionCoordinatorProcessor.getCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasCommit()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @Test
    public void testSendRollback()
            throws Exception
            {
        final String messageId = "testSendRollback" ;
        final MAP map = AddressingHelper.createRequestContext(TestUtil.completionCoordinatorServiceURI, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        final W3CEndpointReference endpoint = TestUtil.getCompletionCoordinatorEndpoint(instanceIdentifier.getInstanceIdentifier());

        CompletionCoordinatorClient.getClient().sendRollback(endpoint, map, new InstanceIdentifier("sender")) ;

        final CompletionCoordinatorDetails details = testCompletionCoordinatorProcessor.getCompletionCoordinatorDetails(messageId, 10000) ;

        assertTrue(details.hasRollback()) ;

        checkDetails(details, true, true, messageId, instanceIdentifier);
            }

    @After
    public void tearDown()
            throws Exception
            {
        CompletionCoordinatorProcessor.setProcessor(origCompletionCoordinatorProcessor) ;
            }

    private void checkDetails(CompletionCoordinatorDetails details, boolean hasFrom, boolean hasFaultTo, String messageId, InstanceIdentifier instanceIdentifier)
    {
        MAP inMAP = details.getMAP();
        ArjunaContext inArjunaContext = details.getArjunaContext();

        assertEquals(inMAP.getTo(), TestUtil.completionCoordinatorServiceURI);
        assertNotNull(inMAP.getReplyTo());
        assertTrue(AddressingHelper.isNoneReplyTo(inMAP));
        if (hasFrom) {
            assertNotNull(inMAP.getFrom());
            assertEquals(inMAP.getFrom().getAddress(), TestUtil.completionInitiatorServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        if (hasFaultTo) {
            assertNotNull(inMAP.getFaultTo());
            assertEquals(inMAP.getFaultTo().getAddress(), TestUtil.completionInitiatorServiceURI);
        } else {
            assertNull(inMAP.getFrom());
        }
        assertNotNull(inMAP.getMessageID());
        assertEquals(inMAP.getMessageID(), messageId);

        if (instanceIdentifier == null) {
            assertNull(inArjunaContext);
        } else {
            assertNotNull(inArjunaContext);
            assertEquals(instanceIdentifier.getInstanceIdentifier(), inArjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
        }
    }
}
