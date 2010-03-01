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
 * RegistrationServiceTestCase.java
 */

package com.arjuna.wsc.tests.junit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.InvalidProtocolException;
import com.arjuna.wsc.RegistrationCoordinator;
import com.arjuna.wsc.tests.TestUtil;

public class RegistrationServiceTestCase
{
    private EndpointReferenceType registrationRequester ;
    private EndpointReferenceType registrationCoordinator ;

    @Before
    public void setUp()
        throws Exception
    {
        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String registrationRequesterURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_REQUESTER) ;
        registrationRequester = new EndpointReferenceType(new AttributedURIType(registrationRequesterURI)) ;
        final String registrationCoordinatorURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
        registrationCoordinator = new EndpointReferenceType(new AttributedURIType(registrationCoordinatorURI)) ;
    }

    @Test
    public void testKnownCoordinationType()
        throws Exception
    {
        final String messageId = "testKnownCoordinationType" ;
        final String protocolIdentifier = TestUtil.PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        coordinationContext.setCoordinationType(new URI(TestUtil.COORDINATION_TYPE)) ;
        coordinationContext.setIdentifier(new AttributedURIType("identifier")) ;
        coordinationContext.setRegistrationService(registrationCoordinator) ;
        try
        {
            final EndpointReferenceType registerResponse = RegistrationCoordinator.register(coordinationContext, messageId, registrationRequester, protocolIdentifier) ;
            
            assertNotNull(registerResponse) ;
            assertEquals(TestUtil.PROTOCOL_COORDINATOR_SERVICE, registerResponse.getAddress().getValue()) ;
        }
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }

    @Test
    public void testUnknownCoordinationType()
        throws Exception
    {
        final String messageId = "testUnknownCoordinationType" ;
        final String protocolIdentifier = TestUtil.UNKNOWN_PROTOCOL_IDENTIFIER ;
        final CoordinationContextType coordinationContext = new CoordinationContextType() ;
        coordinationContext.setCoordinationType(new URI(TestUtil.COORDINATION_TYPE)) ;
        coordinationContext.setIdentifier(new AttributedURIType("identifier")) ;
        coordinationContext.setRegistrationService(registrationCoordinator) ;
        try
        {
            RegistrationCoordinator.register(coordinationContext, messageId, registrationRequester, protocolIdentifier) ;
        }
        catch (final InvalidProtocolException ipe) {}
        catch (final Throwable th)
        {
            fail("Unexpected exception: " + th) ;
        }
    }

    @After
    public void tearDown()
        throws Exception
    {
        registrationCoordinator = null ;
        registrationRequester = null ;
    }
}
