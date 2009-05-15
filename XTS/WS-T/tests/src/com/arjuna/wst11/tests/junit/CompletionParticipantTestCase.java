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
 * CompletionParticipantTestCase.java
 */

package com.arjuna.wst11.tests.junit;

import junit.framework.TestCase;

import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.jbossts.xts.wsaddr.map.MAP;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsat.client.CompletionCoordinatorClient;
import com.arjuna.webservices11.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wst11.tests.junit.TestCompletionCoordinatorProcessor.CompletionCoordinatorDetails;
import com.arjuna.wst11.tests.TestUtil;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class CompletionParticipantTestCase extends TestCase
{
    private CompletionCoordinatorProcessor origCompletionCoordinatorProcessor ;

    private TestCompletionCoordinatorProcessor testCompletionCoordinatorProcessor = new TestCompletionCoordinatorProcessor() ;

    protected void setUp()
        throws Exception
    {
        origCompletionCoordinatorProcessor = CompletionCoordinatorProcessor.setProcessor(testCompletionCoordinatorProcessor) ;
    }

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

    protected void tearDown()
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