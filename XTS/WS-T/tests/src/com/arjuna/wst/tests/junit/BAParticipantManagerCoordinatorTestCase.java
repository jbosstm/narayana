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
 * $Id: BAParticipantManagerCoordinatorTestCase.java,v 1.7.6.1 2005/11/22 10:37:45 kconner Exp $
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
import com.arjuna.webservices.wsarjtx.client.ParticipantManagerParticipantClient;
import com.arjuna.webservices.wsarjtx.processors.ParticipantManagerParticipantProcessor;
import com.arjuna.wst.tests.junit.TestParticipantManagerParticipantProcessor.ParticipantManagerParticipantDetails;

public class BAParticipantManagerCoordinatorTestCase extends TestCase
{
    private ParticipantManagerParticipantProcessor origParticipantManagerParticipantProcessor ;
    
    private TestParticipantManagerParticipantProcessor testParticipantManagerParticipantProcessor = new TestParticipantManagerParticipantProcessor() ;
    private String participantManagerParticipantService ;

    protected void setUp()
        throws Exception
    {
        origParticipantManagerParticipantProcessor = ParticipantManagerParticipantProcessor.setParticipant(testParticipantManagerParticipantProcessor) ;
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        participantManagerParticipantService = soapRegistry.getServiceURI(ArjunaTXConstants.SERVICE_PARTICIPANT_MANAGER_PARTICIPANT) ;
    }

    public void testSendExit()
        throws Exception
    {
        final String messageId = "testSendExit" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantManagerParticipantService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantManagerParticipantClient.getClient().sendExit(addressingContext, instanceIdentifier) ;
        
        final ParticipantManagerParticipantDetails details = testParticipantManagerParticipantProcessor.getParticipantManagerParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasExit()) ;
    }

    public void testSendCompleted()
        throws Exception
    {
        final String messageId = "testSendCompleted" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantManagerParticipantService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantManagerParticipantClient.getClient().sendCompleted(addressingContext, instanceIdentifier) ;
        
        final ParticipantManagerParticipantDetails details = testParticipantManagerParticipantProcessor.getParticipantManagerParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasCompleted()) ;
    }

    public void testSendFault()
        throws Exception
    {
        final String messageId = "testSendFault" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantManagerParticipantService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        ParticipantManagerParticipantClient.getClient().sendFault(addressingContext, instanceIdentifier) ;
        
        final ParticipantManagerParticipantDetails details = testParticipantManagerParticipantProcessor.getParticipantManagerParticipantDetails(messageId, 10000) ;
        
        assertTrue(details.hasFault()) ;
    }

    public void testSendError()
        throws Exception
    {
        final String messageId = "testSendFault" ;
        final AddressingContext addressingContext = AddressingContext.createRequestContext(participantManagerParticipantService, messageId) ;
        final InstanceIdentifier instanceIdentifier = new InstanceIdentifier("2") ;
        
        final String reason = "testSendFaultReason" ;
        final SoapFaultType soapFaultType = SoapFaultType.FAULT_SENDER ;
        final QName subcode = ArjunaTXConstants.UNKNOWNERROR_ERROR_CODE_QNAME ;
        final SoapFault soapFault = new SoapFault(soapFaultType, subcode, reason) ;
        ParticipantManagerParticipantClient.getClient().sendSoapFault(addressingContext, soapFault, instanceIdentifier) ;
        
        final ParticipantManagerParticipantDetails details = testParticipantManagerParticipantProcessor.getParticipantManagerParticipantDetails(messageId, 10000) ;
        final SoapFault receivedSoapFault = details.getSoapFault() ;
        
        assertNotNull(receivedSoapFault) ;
        assertEquals(soapFaultType, receivedSoapFault.getSoapFaultType()) ;
        assertEquals(subcode, receivedSoapFault.getSubcode()) ;
        assertEquals(reason, receivedSoapFault.getReason()) ;
    }

    protected void tearDown()
        throws Exception
    {
        ParticipantManagerParticipantProcessor.setParticipant(origParticipantManagerParticipantProcessor) ;
    }
}
