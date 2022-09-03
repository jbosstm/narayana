/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package com.arjuna.wsc.tests.arq;

import com.arjuna.webservices.SoapFault;
import com.arjuna.wsc.CannotRegisterException;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import jakarta.xml.ws.wsaddressing.W3CEndpointReference;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.InvalidStateException;
import com.arjuna.wsc.tests.TestUtil;
import com.arjuna.wsc.tests.TestUtil11;
import com.arjuna.wsc.tests.WarDeployment;
import com.arjuna.wsc11.RegistrationCoordinator;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

@RunWith(Arquillian.class)
public class RegistrationServiceTest extends BaseWSCTest {

    @Deployment
    public static WebArchive createDeployment() {
        return WarDeployment.getDeployment();
    }

    private W3CEndpointReference sendRegistration(String messageId, String protocolIdentifier) 
            throws CannotRegisterException, InvalidStateException, InvalidProtocolException, SoapFault {
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        CoordinationContextType.Identifier identifierInstance = new CoordinationContextType.Identifier();
        coordinationContext.setCoordinationType(TestUtil.COORDINATION_TYPE) ;
        coordinationContext.setIdentifier(identifierInstance) ;
        identifierInstance.setValue("identifier");
        coordinationContext.setRegistrationService(TestUtil11.getRegistrationEndpoint(identifierInstance.getValue())) ;
        W3CEndpointReference participantEndpoint = TestUtil11.getProtocolParticipantEndpoint("participant");
        return RegistrationCoordinator.register(coordinationContext, messageId, participantEndpoint, protocolIdentifier);
    }
    
    public void testKnownProtocolIdentifierInternal()
            throws Exception
    {
        final String messageId = "testKnownCoordinationType" ;
        final String protocolIdentifier = TestUtil.PROTOCOL_IDENTIFIER ;
        
        try
        {
            final W3CEndpointReference registerResponse = sendRegistration(messageId, protocolIdentifier);
            assertNotNull(registerResponse) ;
        }
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }

    public void testUnknownProtocolIdentifierInternal()
            throws Exception
    {
        final String messageId = "testUnknownCoordinationType" ;
        final String protocolIdentifier = TestUtil.UNKNOWN_PROTOCOL_IDENTIFIER ;
        
        try
        {
            sendRegistration(messageId, protocolIdentifier);
            fail("Expecting exception being thrown as identifier was " + protocolIdentifier);
        }
        catch (final InvalidProtocolException ipe) {}
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }
    
    @Test
    public void testKnownProtocolIdentifierSync()
            throws Exception
    {
        final String previousValue = XTSPropertyManager.getWSCEnvironmentBean().getUseAsynchronousRequest();
        XTSPropertyManager.getWSCEnvironmentBean().setUseAsynchronousRequest(WSCEnvironmentBean.NO_ASYNC_REQUEST);
        testKnownProtocolIdentifierInternal();
        XTSPropertyManager.getWSCEnvironmentBean().setUseAsynchronousRequest(previousValue);
    }

    @Test
    public void testUnknownProtocolIdentifierSync()
            throws Exception
    {
        final String previousValue = XTSPropertyManager.getWSCEnvironmentBean().getUseAsynchronousRequest();
        XTSPropertyManager.getWSCEnvironmentBean().setUseAsynchronousRequest(WSCEnvironmentBean.NO_ASYNC_REQUEST);
        testUnknownProtocolIdentifierInternal();
        XTSPropertyManager.getWSCEnvironmentBean().setUseAsynchronousRequest(previousValue);
    }
    
    @Test
    public void testKnownProtocolIdentifierAsync()
            throws Exception
    {
        final String previousValue = XTSPropertyManager.getWSCEnvironmentBean().getUseAsynchronousRequest();
        XTSPropertyManager.getWSCEnvironmentBean().setUseAsynchronousRequest(WSCEnvironmentBean.PLAIN_ASYNC_REQUEST);
        testKnownProtocolIdentifierInternal();
        XTSPropertyManager.getWSCEnvironmentBean().setUseAsynchronousRequest(previousValue);
    }

    @Test
    public void testUnknownProtocolIdentifierAsync()
            throws Exception
    {
        final String previousValue = XTSPropertyManager.getWSCEnvironmentBean().getUseAsynchronousRequest();
        XTSPropertyManager.getWSCEnvironmentBean().setUseAsynchronousRequest(WSCEnvironmentBean.PLAIN_ASYNC_REQUEST);
        testUnknownProtocolIdentifierInternal();
        XTSPropertyManager.getWSCEnvironmentBean().setUseAsynchronousRequest(previousValue);
    }
}
