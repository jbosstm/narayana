/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */


package com.arjuna.wsc.tests.arq;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.ws.api.addressing.MAP;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegistrationPortType;

import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsarj.ArjunaContext;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.wscoor.client.WSCOORClient;
import com.arjuna.webservices11.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.wsc.tests.TestUtil11;
import com.arjuna.wsc.tests.WarDeployment;
import com.arjuna.wsc.tests.arq.TestRegistrationCoordinatorProcessor.RegisterDetails;

@RunWith(Arquillian.class)
public class RegistrationTest extends BaseWSCTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment(
                TestRegistrationCoordinatorProcessor.class,
                RegisterDetails.class);
    }

    private RegistrationCoordinatorProcessor origRegistrationCoordinatorProcessor ;

    private TestRegistrationCoordinatorProcessor testRegistrationCoordinatorProcessor = new TestRegistrationCoordinatorProcessor() ;

    @Before
    public void setUp()
            throws Exception
            {
        origRegistrationCoordinatorProcessor = RegistrationCoordinatorProcessor.setCoordinator(testRegistrationCoordinatorProcessor) ;
        ServiceRegistry.getRegistry();
            }

    @Test
    public void testRequestWithoutInstanceIdentifier()
            throws Exception
            {
        executeRequest("testRequestWithoutInstanceIdentifier", null) ;
            }

    @Test
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
        registerType.setProtocolIdentifier(protocolIdentifier);
        registerType.setParticipantProtocolService(participantProtocolEndpoint);
        RegistrationPortType port = WSCOORClient.getRegistrationPort(registerEndpoint, CoordinationConstants.WSCOOR_ACTION_REGISTER, messageId);
        port.registerOperation(registerType);

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

    @After
    public void tearDown()
            throws Exception
            {
        RegistrationCoordinatorProcessor.setCoordinator(origRegistrationCoordinatorProcessor) ;
        origRegistrationCoordinatorProcessor = null ;
        testRegistrationCoordinatorProcessor = null ;
            }
}