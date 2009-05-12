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
package com.arjuna.webservices11.wsaddr.client;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.handler.Handler;

import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.map.MAP;
import org.jboss.jbossts.xts.soapfault.SoapFaultPortType;
import org.jboss.jbossts.xts.soapfault.Fault;
import org.jboss.jbossts.xts.soapfault.SoapFaultService;

/**
 * Base client.
 * @author kevin
 */
public class SoapFaultClient
{
    /**
     * Send a fault.
     * @param soapFault The SOAP fault.
     * @param map addressing context initialised with to and message ID.
     * @param action The action URI for the request.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public static void sendSoapFault(final SoapFault11 soapFault,
                                     final MAP map,
                                     final String action)
        throws SoapFault11, IOException
    {
        if (action != null)
        {
            soapFault.setAction(action) ;
        }

        final SoapFaultPortType faultPort = getSoapFaultPort(map, action);
        Fault fault = soapFault.toFault();
        faultPort.soapFault(fault);
    }

    /**
     * Send a fault to a specific endpoint.
     * @param soapFault The SOAP fault.
     * @param endpoint an endpoint ot dispatch the fault to.
     * @param map addressing context initialised with to and message ID.
     * @param action The action URI for the request.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public static void sendSoapFault(final SoapFault11 soapFault,
                                     W3CEndpointReference endpoint,
                                     final MAP map,
                                     final String action)
        throws SoapFault11, IOException
    {
        if (action != null)
        {
            soapFault.setAction(action) ;
        }

        final SoapFaultPortType faultPort = getSoapFaultPort(endpoint, map, action);
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

    private static SoapFaultPortType getSoapFaultPort(final MAP map,
                                                      final String action)
    {
        SoapFaultService service = getSoapFaultService();
        SoapFaultPortType port = service.getPort(SoapFaultPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        String to = map.getTo();
        /*
         * we no longer have to add the JaxWS WSAddressingClientHandler because we can specify the WSAddressing feature
        List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingClientHandler());
        bindingProvider.getBinding().setHandlerChain(customHandlerChain);
         */

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        if (action != null) {
            map.setAction(action);
        }
        AddressingHelper.configureRequestContext(requestContext, map, to, action);

        return port;
    }

    private static SoapFaultPortType getSoapFaultPort(final W3CEndpointReference endpoint,
                                                      final MAP map,
                                                      final String action)
    {
        SoapFaultService service = getSoapFaultService();
        SoapFaultPortType port = service.getPort(endpoint, SoapFaultPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        MAP requestMap = AddressingHelper.outboundMap(requestContext);
        if (action != null) {
            map.setAction(action);
        }
        AddressingHelper.installCallerProperties(map, requestMap);
        String to = requestMap.getTo();
        /*
         * we no longer have to add the JaxWS WSAddressingClientHandler because we can specify the WSAddressing feature
        List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingClientHandler());
        bindingProvider.getBinding().setHandlerChain(customHandlerChain);
        */
        AddressingHelper.configureRequestContext(requestContext, to, action);

        return port;
    }

    private static final ThreadLocal<SoapFaultService> soapFaultService = new ThreadLocal<SoapFaultService>();
}