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
 * Copyright (c) 2003, 2004,
 *
 * Arjuna Technologies Limited.
 *
 * $Id: TwoPCCoordinatorTestCase.java,v 1.6.2.1 2005/11/22 10:37:45 kconner Exp $
 */

package com.arjuna.wst.tests.junit;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.wsaddr.AddressingContext;
import com.arjuna.webservices.wsarj.InstanceIdentifier;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices.wsat.AtomicTransactionConstants;
import com.arjuna.webservices.wsat.client.ParticipantClient;
import com.arjuna.webservices.wsat.processors.ParticipantProcessor;
import com.arjuna.wst.tests.junit.TestParticipantProcessor.ParticipantDetails;

public class TwoPCCoordinatorTestCase extends TestCase
{
    private ParticipantProcessor origParticipantProcessor ;
    
    private TestParticipantProcessor testParticipantProcessor = new TestParticipantProcessor() ;
    private String participantService ;

    protected void setUp()
        throws Exception
    {
        origParticipantProcessor = ParticipantProcessor.setParticipant(testParticipantProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        participantService = soapRegistry.getServiceURI(AtomicTransactionConstants.SERVICE_PARTICIPANT) ;
    }

    public void testSendPrepare()
        throws Exception
    {
        final String messageId = "testSendPrepare" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantClient.getClient().sendPrepare(addressingContext, instanceIdentifier) ;
        
        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasPrepare()) ;
    }

    public void testSendCommit()
        throws Exception
    {
        final String messageId = "testSendCommit" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantClient.getClient().sendCommit(addressingContext, instanceIdentifier) ;
        
        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCommit()) ;
    }

    public void testSendRollback()
        throws Exception
    {
        final String messageId = "testSendRollback" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantClient.getClient().sendRollback(addressingContext, instanceIdentifier) ;
        
        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasRollback()) ;
    }

    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendError" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        
        final String reason = "testSendErrorReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        ParticipantClient.getClient().sendSoapFault(addressingContext, soapFault, instanceIdentifier) ;
        
        final ParticipantDetails details = testParticipantProcessor.getParticipantDetails(messageId, 10000) ;
        final SoapFault receivedSoapFault = details.getSoapFault() ;
        
        assertNotNull(receivedSoapFault) ;
        assertEquals(soapFaultType, receivedSoapFault.getSoapFaultType()) ;
        assertEquals(subcode, receivedSoapFault.getSubcode()) ;
        assertEquals(reason, receivedSoapFault.getReason()) ;
    }

    protected void tearDown()
        throws Exception
    {
        ParticipantProcessor.setParticipant(origParticipantProcessor) ;
    }
}
