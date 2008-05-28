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
 * EnduranceTestCase.java
 */

package com.arjuna.wsc11.tests.junit;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.addressing.AddressingProperties;

import junit.framework.TestCase;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.client.ActivationCoordinatorClient;
import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.webservices11.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc.*;
import com.arjuna.wsc11.tests.TestUtil11;
import com.arjuna.wsc11.tests.junit.TestActivationCoordinatorProcessor.CreateCoordinationContextDetails;
import com.arjuna.wsc11.tests.junit.TestRegistrationCoordinatorProcessor.RegisterDetails;
import com.arjuna.wsc11.tests.junit.TestActivationCoordinatorProcessor;
import com.arjuna.wsc11.tests.junit.TestRegistrationCoordinatorProcessor;
import com.arjuna.wsc11.ActivationCoordinator;
import com.arjuna.wsc11.RegistrationCoordinator;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.*;

public class EnduranceTestCase extends TestCase
{
    private ActivationCoordinatorProcessor origActivationCoordinatorProcessor ;
    private RegistrationCoordinatorProcessor origRegistrationCoordinatorProcessor ;

    private TestActivationCoordinatorProcessor testActivationCoordinatorProcessor = new TestActivationCoordinatorProcessor() ;

    private TestRegistrationCoordinatorProcessor testRegistrationCoordinatorProcessor = new TestRegistrationCoordinatorProcessor() ;

    private static final long TEST_DURATION = 30 * 1000;

    protected void setUp()
        throws Exception
    {
        origActivationCoordinatorProcessor = ActivationCoordinatorProcessor.setCoordinator(testActivationCoordinatorProcessor) ;

        origRegistrationCoordinatorProcessor = RegistrationCoordinatorProcessor.setCoordinator(testRegistrationCoordinatorProcessor) ;
    }

    public void testCreateCoordinationContextRequest()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doCreateCoordinationContextRequest(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    public void testCreateCoordinationContextError()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doCreateCoordinationContextError(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    public void testRegisterRequest()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doRegisterRequest(Integer.toString(dialogIdentifierNumber));
            dialogIdentifierNumber++;
        }
    }

    public void testRegisterError()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            doRegisterError(Integer.toString(dialogIdentifierNumber), dialogIdentifierNumber % 3);
            dialogIdentifierNumber++;
        }
    }

    public void testEachInTurn()
        throws Exception
    {
        long startTime = System.currentTimeMillis();

        int count                  = 0;
        int dialogIdentifierNumber = 0;
        while ((System.currentTimeMillis() - startTime) < TEST_DURATION)
        {
            if (count == 0)
                doCreateCoordinationContextRequest(Integer.toString(dialogIdentifierNumber));
            else if (count == 1)
                doCreateCoordinationContextError(Integer.toString(dialogIdentifierNumber));
            else if (count == 2)
                doRegisterRequest(Integer.toString(dialogIdentifierNumber));
            else
                doRegisterError(Integer.toString(dialogIdentifierNumber), (dialogIdentifierNumber / 4) % 4);

            count = (count + 1) % 4;
            dialogIdentifierNumber++;
        }
    }

    public void doCreateCoordinationContextRequest(final String messageId)
        throws Exception
    {
        final String coordinationType = TestUtil.COORDINATION_TYPE ;
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(TestUtil11.activationCoordinatorService, messageId) ;
        CreateCoordinationContextResponseType response =
                ActivationCoordinatorClient.getClient().sendCreateCoordination(addressingProperties, coordinationType, null, null) ;

        final CreateCoordinationContextDetails details = testActivationCoordinatorProcessor.getCreateCoordinationContextDetails(messageId, 10000) ;
        final CreateCoordinationContextType requestCreateCoordinationContext = details.getCreateCoordinationContext() ;
        final AddressingProperties requestAddressingProperties = details.getAddressingProperties() ;

        assertEquals(requestAddressingProperties.getTo().getURI().toString(), TestUtil11.activationCoordinatorService);
        assertEquals(requestAddressingProperties.getMessageID().getURI().toString(), messageId);

        assertNull(requestCreateCoordinationContext.getExpires()) ;
        assertNull(requestCreateCoordinationContext.getCurrentContext()) ;
        assertEquals(requestCreateCoordinationContext.getCoordinationType(), coordinationType);

        CoordinationContext context = response.getCoordinationContext();
        assertNotNull(context);
        assertNull(context.getExpires());
        assertEquals(context.getCoordinationType(), coordinationType);
        assertNotNull(context.getIdentifier());
    }

    public void doCreateCoordinationContextError(final String messageId)
        throws Exception
    {
        final String coordinationType = TestUtil.INVALID_CREATE_PARAMETERS_COORDINATION_TYPE;
        try {
            ActivationCoordinator.createCoordinationContext(TestUtil11.activationCoordinatorService, messageId, coordinationType, null, null) ;
        } catch (InvalidCreateParametersException icpe) {
            final CreateCoordinationContextDetails details = testActivationCoordinatorProcessor.getCreateCoordinationContextDetails(messageId, 10000) ;
            final CreateCoordinationContextType requestCreateCoordinationContext = details.getCreateCoordinationContext() ;
            final AddressingProperties requestAddressingProperties = details.getAddressingProperties() ;
            assertEquals(requestAddressingProperties.getTo().getURI().toString(), TestUtil11.activationCoordinatorService);
            assertEquals(requestAddressingProperties.getMessageID().getURI().toString(), messageId);

            assertNull(requestCreateCoordinationContext.getExpires()) ;
            assertNull(requestCreateCoordinationContext.getCurrentContext()) ;
            assertEquals(requestCreateCoordinationContext.getCoordinationType(), coordinationType);
            return;
        }
        fail("expected invalid create parameters exception");
    }

    public void doRegisterRequest(final String messageId)
        throws Exception
    {
        final String protocolIdentifier = TestUtil.PROTOCOL_IDENTIFIER ;
        final W3CEndpointReference participantProtocolService = TestUtil11.getProtocolParticipantEndpoint("participant");
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance) ;
        identifierInstance.setValue("identifier");
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifierInstance.getValue())) ;

        W3CEndpointReference coordinator = RegistrationCoordinator.register(coordinationContext, messageId, participantProtocolService, protocolIdentifier) ;

        final RegisterDetails details = testRegistrationCoordinatorProcessor.getRegisterDetails(messageId, 10000) ;
        final RegisterType requestRegister = details.getRegister() ;
        final AddressingProperties requestAddressingProperties = details.getAddressingProperties() ;
        final ArjunaContext requestArjunaContext = details.getArjunaContext() ;

        assertEquals(requestAddressingProperties.getTo().getURI().toString(), TestUtil11.registrationCoordinatorService);
        assertEquals(requestAddressingProperties.getMessageID().getURI().toString(), messageId);

        assertNotNull(requestArjunaContext) ;
        assertEquals(requestArjunaContext.getInstanceIdentifier().getInstanceIdentifier(), identifierInstance.getValue()) ;

        assertEquals(protocolIdentifier, requestRegister.getProtocolIdentifier()) ;
        assertNotNull(protocolIdentifier, requestRegister.getParticipantProtocolService()) ;

        assertNotNull(coordinator);
    }

    public void doRegisterError(final String messageId, int count)
        throws Exception
    {
        final String protocolIdentifier;
        final W3CEndpointReference participantProtocolService = TestUtil11.getProtocolParticipantEndpoint("participant");
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance) ;
        identifierInstance.setValue("identifier");
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifierInstance.getValue())) ;

        W3CEndpointReference coordinator = null;

        switch (count) {
            case 0:
                protocolIdentifier = TestUtil.INVALID_PROTOCOL_PROTOCOL_IDENTIFIER;
                try {
                    coordinator = RegistrationCoordinator.register(coordinationContext, messageId, participantProtocolService, protocolIdentifier) ;
                } catch (InvalidProtocolException ipe) {
                }
                if (coordinator != null) {
                    fail("expected invalid protocol exception");
                }
                break;
            case 1:
                protocolIdentifier = TestUtil.INVALID_STATE_PROTOCOL_IDENTIFIER;
                try {
                    coordinator = RegistrationCoordinator.register(coordinationContext, messageId, participantProtocolService, protocolIdentifier) ;
                } catch (InvalidStateException ise) {
                }
                if (coordinator != null) {
                    fail("expected invalid state exception");
                }
                break;
            case 3:
                protocolIdentifier = TestUtil.NO_ACTIVITY_PROTOCOL_IDENTIFIER;
                try {
                    coordinator = RegistrationCoordinator.register(coordinationContext, messageId, participantProtocolService, protocolIdentifier) ;
                } catch (NoActivityException nae) {
                }
                if (coordinator != null) {
                    fail("expected no activity exception");
                }
                break;
            default:
                protocolIdentifier = TestUtil.ALREADY_REGISTERED_PROTOCOL_IDENTIFIER;
                try {
                    coordinator = RegistrationCoordinator.register(coordinationContext, messageId, participantProtocolService, protocolIdentifier) ;
                } catch (AlreadyRegisteredException are) {
                }
                if (coordinator != null) {
                    fail("expected already registered exception");
                }
                break;
        }

        final RegisterDetails details = testRegistrationCoordinatorProcessor.getRegisterDetails(messageId, 10000) ;
        final RegisterType requestRegister = details.getRegister() ;
        final AddressingProperties requestAddressingProperties = details.getAddressingProperties() ;
        final ArjunaContext requestArjunaContext = details.getArjunaContext() ;

        assertEquals(requestAddressingProperties.getTo().getURI().toString(), TestUtil11.registrationCoordinatorService);
        assertEquals(requestAddressingProperties.getMessageID().getURI().toString(), messageId);

        assertNotNull(requestArjunaContext) ;
        assertEquals(requestArjunaContext.getInstanceIdentifier().getInstanceIdentifier(), identifierInstance.getValue()); ;

        assertEquals(protocolIdentifier, requestRegister.getProtocolIdentifier()) ;
        assertNotNull(protocolIdentifier, requestRegister.getParticipantProtocolService()) ;
    }

    protected void tearDown()
        throws Exception
    {
        ActivationCoordinatorProcessor.setCoordinator(origActivationCoordinatorProcessor) ;
        origActivationCoordinatorProcessor = null ;
        testActivationCoordinatorProcessor = null ;

        RegistrationCoordinatorProcessor.setCoordinator(origRegistrationCoordinatorProcessor) ;
        origRegistrationCoordinatorProcessor = null ;
        testRegistrationCoordinatorProcessor = null ;
    }
}