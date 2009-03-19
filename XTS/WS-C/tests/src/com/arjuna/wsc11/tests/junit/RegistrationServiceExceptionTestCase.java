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
 * RegistrationServiceExceptionTestCase.java
 */

package com.arjuna.wsc11.tests.junit;

import com.arjuna.wsc.AlreadyRegisteredException;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.NoActivityException;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc11.RegistrationCoordinator;
import com.arjuna.wsc11.tests.TestUtil11;
import junit.framework.TestCase;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import javax.xml.ws.wsaddressing.W3CEndpointReference;

public class RegistrationServiceExceptionTestCase extends TestCase
{
    public void testAlreadyRegisteredProtocolIdentifierException()
        throws Exception
    {
        final String messageId = "testAlreadyRegisteredProtocolIdentifierException" ;
        final String protocolIdentifier = TestUtil.ALREADY_REGISTERED_PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        final CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance);
        identifierInstance.setValue("identifier") ;
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifierInstance.getValue())) ;
        W3CEndpointReference participantEndpoint = TestUtil11.getProtocolParticipantEndpoint("participant");
        try
        {
            RegistrationCoordinator.register(coordinationContext, messageId, participantEndpoint, protocolIdentifier) ;
        }
        catch (final AlreadyRegisteredException are) {}
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }

    public void testInvalidProtocolProtocolIdentifierException()
        throws Exception
    {
        final String messageId = "testInvalidProtocolProtocolIdentifierException" ;
        final String protocolIdentifier = TestUtil.INVALID_PROTOCOL_PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        final CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance);
        identifierInstance.setValue("identifier") ;
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

    public void testInvalidStateProtocolIdentifierException()
        throws Exception
    {
        final String messageId = "testInvalidStateProtocolIdentifierException" ;
        final String protocolIdentifier = TestUtil.INVALID_STATE_PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        final CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance);
        identifierInstance.setValue("identifier") ;
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifierInstance.getValue())) ;
        W3CEndpointReference participantEndpoint = TestUtil11.getProtocolParticipantEndpoint("participant");
        try
        {
            RegistrationCoordinator.register(coordinationContext, messageId, participantEndpoint, protocolIdentifier) ;
        }
        catch (final InvalidStateException ise) {}
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }

    public void testNoActivityProtocolIdentifierException()
        throws Exception
    {
        final String messageId = "testNoActivityProtocolIdentifierException" ;
        final String protocolIdentifier = TestUtil.NO_ACTIVITY_PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        final CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance);
        identifierInstance.setValue("identifier") ;
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifierInstance.getValue())) ;
        W3CEndpointReference participantEndpoint = TestUtil11.getProtocolParticipantEndpoint("participant");
        try
        {
            RegistrationCoordinator.register(coordinationContext, messageId, participantEndpoint, protocolIdentifier) ;
        }
        catch (final NoActivityException nae) {}
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }
}