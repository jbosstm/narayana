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
 * TestContextFactory.java
 */

package com.arjuna.wsc11.tests;

import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.wsc11.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;
import com.arjuna.wsc.tests.TestUtil;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.Expires;

import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;

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

    public CoordinationContext create(final String coordinationTypeURI,
        final Long expires, final CoordinationContextType currentContext)
        throws InvalidCreateParametersException
    {
        if (coordinationTypeURI.equals(TestUtil.INVALID_CREATE_PARAMETERS_COORDINATION_TYPE)) {
            throw new InvalidCreateParametersException();
        }

        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        final String registrationURI = serviceRegistry.getServiceURI(CoordinationConstants.REGISTRATION_SERVICE_NAME) ;
        final W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(CoordinationConstants.REGISTRATION_SERVICE_QNAME);
        builder.endpointName(CoordinationConstants.REGISTRATION_ENDPOINT_QNAME);
        builder.address(registrationURI);
        W3CEndpointReference registrationService = builder.build();

        CoordinationContext testCoordinationContext = new CoordinationContext();
        CoordinationContext.Identifier identifier = new CoordinationContext.Identifier();
        identifier.setValue(Integer.toString(nextIdentifier()));
        testCoordinationContext.setIdentifier(identifier);
        if (expires != null && expires.longValue() > 0) {
            Expires expiresInstance = new Expires();
            expiresInstance.setValue(expires);
            testCoordinationContext.setExpires(expiresInstance);
        }
        testCoordinationContext.setCoordinationType(_coordinationType) ;
        testCoordinationContext.setRegistrationService(registrationService) ;

        try {
            SOAPFactory factory = SOAPFactory.newInstance();
            SOAPElement element = factory.createElement(TestUtil.TEST_ELEMENT_EXTENSION_VALUE_QNAME);
            SOAPElement textElement = element.addTextNode(TestUtil.TEST_EXTENSION_VALUE);
            testCoordinationContext.getAny().add(element);
        } catch (SOAPException e) {
            // TODO log error here
        }

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