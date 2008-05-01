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
package com.arjuna.webservices11.wsaddr.client;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URISyntaxException;

import javax.xml.namespace.QName;
import javax.xml.ws.addressing.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.handler.Handler;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import org.jboss.jbossts.xts.soapfault.SoapFaultPortType;
import org.jboss.jbossts.xts.soapfault.Fault;
import org.jboss.jbossts.xts.soapfault.SoapFaultService;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;
import org.w3c.dom.Element;

/**
 * Base client.
 * @author kevin
 */
public class SoapFaultClient
{
    /**
     * Send a fault.
     * @param soapFault The SOAP fault.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param action The action URI for the request.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public static void sendSoapFault(final SoapFault11 soapFault,
                                     final InstanceIdentifier instanceIdentifier,
                                     final AddressingProperties addressingProperties,
                                     final AttributedURI action)
        throws SoapFault11, IOException
    {
        if (action != null)
        {
            soapFault.setAction(action.getURI().toString()) ;
        }

        final SoapFaultPortType faultPort = getSoapFaultPort(instanceIdentifier, addressingProperties, action);
        Fault fault = soapFault.toFault();
        faultPort.soapFault(fault);
    }

    /**
     * Send a fault to a specific endpoint.
     * @param soapFault The SOAP fault.
     * @param endpoint an endpoint ot dispatch the fault to.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param action The action URI for the request.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public static void sendSoapFault(final SoapFault11 soapFault,
                                     W3CEndpointReference endpoint,
                                     final AddressingProperties addressingProperties,
                                     final AttributedURI action)
        throws SoapFault11, IOException
    {
        if (action != null)
        {
            soapFault.setAction(action.getURI().toString()) ;
        }

        final SoapFaultPortType faultPort = getSoapFaultPort(endpoint, addressingProperties, action);
        Fault fault = soapFault.toFault();
        faultPort.soapFault(fault);
    }

    /**
     * fetch a coordinator activation service unique to the current thread
     * @return
     */
    private static synchronized SoapFaultService getSoapFaultService()
    {
        if (soapFaultService.get() == null) {
            soapFaultService.set(new SoapFaultService());
        }
        return soapFaultService.get();
    }

    private static SoapFaultPortType getSoapFaultPort(final InstanceIdentifier instanceIdentifier,
                                                      final AddressingProperties addressingProperties,
                                                      final AttributedURI action)
    {
        SoapFaultService service = getSoapFaultService();
        SoapFaultPortType port = service.getPort(SoapFaultPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        AttributedURI toUri = addressingProperties.getTo();
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannot specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
        bindingProvider.getBinding().setHandlerChain(customHandlerChain);

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        addressingProperties.setAction(action);
        requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
	    // jbossws should do this for us . . .
	    requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, toUri.getURI().toString());
        // need to set soap action header based upon what the client asks for
        requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, action.getURI().toString());

        return port;
    }

    private static SoapFaultPortType getSoapFaultPort(final W3CEndpointReference endpoint,
                                                      final AddressingProperties addressingProperties,
                                                      final AttributedURI action)
    {
        SoapFaultService service = getSoapFaultService();
        SoapFaultPortType port = service.getPort(endpoint, SoapFaultPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        AddressingProperties requestProperties = (AddressingProperties)requestContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        addressingProperties.setAction(action);
        AddressingHelper.installCallerProperties(addressingProperties, requestProperties);
        AttributedURI toUri = requestProperties.getTo();
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannot specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
        bindingProvider.getBinding().setHandlerChain(customHandlerChain);
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, toUri.getURI().toString());
        // need to set soap action header based upon what the client asks for
        requestContext.put(BindingProvider.SOAPACTION_URI_PROPERTY, action.getURI().toString());

        return port;
    }

    private static final ThreadLocal<SoapFaultService> soapFaultService = new ThreadLocal<SoapFaultService>();
}