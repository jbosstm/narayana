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
 * RegistrationTestCase.java
 */

package com.arjuna.wsc11.tests.junit;

import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.wsf.common.addressing.MAP;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.client.WSCOORClient;
import com.arjuna.webservices11.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.wsc11.tests.TestUtil11;
import com.arjuna.wsc11.tests.junit.TestRegistrationCoordinatorProcessor.RegisterDetails;
import junit.framework.TestCase;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegistrationPortType;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class RegistrationTestCase extends TestCase
{
    private RegistrationCoordinatorProcessor origRegistrationCoordinatorProcessor ;

    private TestRegistrationCoordinatorProcessor testRegistrationCoordinatorProcessor = new TestRegistrationCoordinatorProcessor() ;

    protected void setUp()
        throws Exception
    {
        origRegistrationCoordinatorProcessor = RegistrationCoordinatorProcessor.setCoordinator(testRegistrationCoordinatorProcessor) ;
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
    }

    public void testRequestWithoutInstanceIdentifier()
        throws Exception
    {
        executeRequest("testRequestWithoutInstanceIdentifier", null) ;
    }

    public void testRequestWithInstanceIdentifier()
        throws Exception
    {
        executeRequest("testRequestWithInstanceIdentifier", new InstanceIdentifier("identifier")) ;
    }

    private void executeRequest(final String messageId, final InstanceIdentifier instanceIdentifier)
        throws Exception
    {
        final String protocolIdentifier = "http://foo.example.org/bar" ;
        final W3CEndpointReference participantProtocolEndpoint = TestUtil11.getProtocolParticipantEndpoint("participant");
        String identifier = (instanceIdentifier != null ? instanceIdentifier.getInstanceIdentifier() : null);
        W3CEndpointReference registerEndpoint = TestUtil11.getRegistrationEndpoint(identifier);
        RegisterType registerType = new RegisterType();
        RegisterResponseType response;

        registerType.setProtocolIdentifier(protocolIdentifier);
        registerType.setParticipantProtocolService(participantProtocolEndpoint);
        RegistrationPortType port = WSCOORClient.getRegistrationPort(registerEndpoint, CoordinationConstants.WSCOOR_ACTION_REGISTER, messageId);
        response = port.registerOperation(registerType);

        final RegisterDetails details = testRegistrationCoordinatorProcessor.getRegisterDetails(messageId, 10000) ;
        final RegisterType requestRegister = details.getRegister() ;
        final MAP requestMap = details.getMAP() ;
        final ArjunaContext requestArjunaContext = details.getArjunaContext() ;

        assertEquals(requestMap.getTo(), TestUtil11.registrationCoordinatorService);
        assertEquals(requestMap.getMessageID(), messageId);

        if (instanceIdentifier == null)
        {
            assertNull(requestArjunaContext) ;
        }
        else
        {
            assertEquals(instanceIdentifier.getInstanceIdentifier(), requestArjunaContext.getInstanceIdentifier().getInstanceIdentifier()) ;
        }

        assertEquals(protocolIdentifier, requestRegister.getProtocolIdentifier()) ;
    }

    protected void tearDown()
        throws Exception
    {
        RegistrationCoordinatorProcessor.setCoordinator(origRegistrationCoordinatorProcessor) ;
        origRegistrationCoordinatorProcessor = null ;
        testRegistrationCoordinatorProcessor = null ;
    }
}