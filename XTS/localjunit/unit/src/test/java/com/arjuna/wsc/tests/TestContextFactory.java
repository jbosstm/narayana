/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.wsc.tests;

import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.wsc11.ContextFactory;
import com.arjuna.wsc.InvalidCreateParametersException;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.Expires;

import jakarta.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import jakarta.xml.ws.wsaddressing.W3CEndpointReference;
import jakarta.xml.soap.SOAPFactory;
import jakarta.xml.soap.SOAPElement;
import jakarta.xml.soap.SOAPException;

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
        final Long expires, final CoordinationContextType currentContext, boolean isSecure)
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
            element.addTextNode(TestUtil.TEST_EXTENSION_VALUE);
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