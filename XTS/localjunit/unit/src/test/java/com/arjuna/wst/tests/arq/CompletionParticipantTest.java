/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.wst.tests.arq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

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