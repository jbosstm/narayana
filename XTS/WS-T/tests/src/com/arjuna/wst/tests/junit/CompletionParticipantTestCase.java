/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
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

package com.arjuna.wst.tests.junit;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.client.CompletionCoordinatorClient;
import com.arjuna.webservices.wsat.processors.CompletionCoordinatorProcessor;
import com.arjuna.wst.tests.junit.TestCompletionCoordinatorProcessor.CompletionCoordinatorDetails;

public class CompletionParticipantTestCase extends TestCase
{
    private CompletionCoordinatorProcessor origCompletionCoordinatorProcessor ;
    
    private TestCompletionCoordinatorProcessor testCompletionCoordinatorProcessor = new TestCompletionCoordinatorProcessor() ;
    private String completionCoordinatorService ;

    protected void setUp()
        throws Exception
    {
        origCompletionCoordinatorProcessor = CompletionCoordinatorProcessor.setCoordinator(testCompletionCoordinatorProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        completionCoordinatorService = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_COMPLETION_COORDINATOR) ;
    }

    public void testSendCommit()
        throws Exception
    {
        final String messageId = "testSendCommit" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(completionCoordinatorService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CompletionCoordinatorClient.getClient().sendCommit(addressingContext, instanceIdentifier) ;
        
        final CompletionCoordinatorDetails details = testCompletionCoordinatorProcessor.getCompletionCoordinatorDetails(messageId, 10000) ;
        
        assertTrue(details.hasCommit()) ;
    }

    public void testSendRollback()
        throws Exception
    {
        final String messageId = "testSendRollback" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(completionCoordinatorService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        CompletionCoordinatorClient.getClient().sendRollback(addressingContext, instanceIdentifier) ;
        
        final CompletionCoordinatorDetails details = testCompletionCoordinatorProcessor.getCompletionCoordinatorDetails(messageId, 10000) ;
        
        assertTrue(details.hasRollback()) ;
    }

    protected void tearDown()
        throws Exception
    {
        CompletionCoordinatorProcessor.setCoordinator(origCompletionCoordinatorProcessor) ;
    }
}
