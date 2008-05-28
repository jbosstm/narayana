/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * RegistrationServiceTestCase.java
 */

package com.arjuna.wsc11.tests.junit;

import junit.framework.TestCase;

import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc11.RegistrationCoordinator;
import com.arjuna.wsc11.tests.TestUtil11;
import com.arjuna.wsc.tests.TestUtil;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class RegistrationServiceTestCase extends TestCase
{
    public void testKnownCoordinationType()
        throws Exception
    {
        final String messageId = "testKnownCoordinationType" ;
        final String protocolIdentifier = TestUtil.PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance) ;
        identifierInstance.setValue("identifier");
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifierInstance.getValue())) ;
        W3CEndpointReference participantEndpoint = TestUtil11.getProtocolParticipantEndpoint("participant");
        try
        {
            final W3CEndpointReference registerResponse = RegistrationCoordinator.register(coordinationContext, messageId, participantEndpoint, protocolIdentifier) ;

            assertNotNull(registerResponse) ;
        }
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }

    public void testUnknownCoordinationType()
        throws Exception
    {
        final String messageId = "testUnknownCoordinationType" ;
        final String protocolIdentifier = TestUtil.UNKNOWN_PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance) ;
        identifierInstance.setValue("identifier");
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifierInstance.getValue())) ;
        W3CEndpointReference participantEndpoint = TestUtil11.getProtocolParticipantEndpoint("participant");
        try
        {
            RegistrationCoordinator.register(coordinationContext, messageId, participantEndpoint, protocolIdentifier) ;
        }
        catch (final InvalidProtocolException ipe) {}
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }
}