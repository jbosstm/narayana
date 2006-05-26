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
 * Copyright (c) 2002, 2003, Arjuna Technologies Limited.
 *
 * TestContextFactory.java
 */

package com.arjuna.wsc.tests;

import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.stax.NamedElement;
import com.arjuna.webservices.stax.TextElement;
import com.arjuna.webservices.stax.URI;
import com.arjuna.webservices.wsaddr.AttributedURIType;
import com.arjuna.webservices.wsaddr.EndpointReferenceType;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.CoordinationContextType;
import com.arjuna.wsc.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;

public class TestContextFactory implements ContextFactory
{
    public TestContextFactory(String coordinationType)
    {
        _identifier          = 0;
        _coordinationType    = coordinationType;
    }

    public void install(final String coordinationTypeURI)
    {
    }

    public CoordinationContextType create(final String coordinationTypeURI,
        final Long expires, final CoordinationContextType currentContext)
        throws InvalidCreateParametersException
    {
        if (coordinationTypeURI.equals(TestUtil.INVALID_CREATE_PARAMETERS_COORDINATION_TYPE))
            throw new InvalidCreateParametersException();

        CoordinationContextType testCoordinationContext = new CoordinationContextType();

        final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
        final String registrationURI = soapRegistry.getServiceURI(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR) ;
        final EndpointReferenceType registrationService = new EndpointReferenceType(new AttributedURIType(registrationURI)) ;
        
        testCoordinationContext.setIdentifier(new AttributedURIType(Integer.toString(nextIdentifier()))) ;
        testCoordinationContext.setCoordinationType(new URI(_coordinationType)) ;
        testCoordinationContext.setRegistrationService(registrationService) ;
        
        final NamedElement extension = new NamedElement(TestUtil.TEST_ELEMENT_EXTENSION_VALUE_QNAME,
            new TextElement(TestUtil.TEST_EXTENSION_VALUE)) ;
        testCoordinationContext.putAnyContent(extension) ;
        
        _identifier++;

        return testCoordinationContext;
    }

    public void uninstall(final String coordinationTypeURI)
    {
    }
    
    private synchronized int nextIdentifier()
    {
        return _identifier++ ;
    }

    private int           _identifier          = 0;
    private String        _coordinationType    = null;
}
