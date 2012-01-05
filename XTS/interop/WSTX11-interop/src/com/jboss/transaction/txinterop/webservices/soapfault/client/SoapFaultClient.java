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
package com.jboss.transaction.txinterop.webservices.soapfault.client;

import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.jbossts.xts.soapfault.SoapFaultPortType;
import org.jboss.jbossts.xts.soapfault.SoapFaultService;
import org.jboss.ws.api.addressing.MAP;
import org.xmlsoap.schemas.soap.envelope.Fault;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;
import java.util.Map;

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

        final org.jboss.jbossts.xts.soapfault.SoapFaultPortType faultPort = getSoapFaultPort(map, action);
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
            // we don't supply wsdl on the client side -- we want this client to address the various
            // different versions of the service which bind the fault WebMethod using different
            // soap actions. the annotations on the service and port supply all the info needed
            // to create the service and port on the client side.
            // soapFaultService.set(new SoapFaultService(null, new QName("http://jbossts.jboss.org/xts/soapfault", "SoapFaultService")));
            soapFaultService.set(new SoapFaultService());
        }
        return soapFaultService.get();
    }

    private static org.jboss.jbossts.xts.soapfault.SoapFaultPortType getSoapFaultPort(final MAP map,
                                                      final String action)
    {
        SoapFaultService service = getSoapFaultService();
        SoapFaultPortType port = service.getPort(org.jboss.jbossts.xts.soapfault.SoapFaultPortType.class, new AddressingFeature(true, true));
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

    private static org.jboss.jbossts.xts.soapfault.SoapFaultPortType getSoapFaultPort(final W3CEndpointReference endpoint,
                                                      final MAP map,
                                                      final String action)
    {
        SoapFaultService service = getSoapFaultService();
        org.jboss.jbossts.xts.soapfault.SoapFaultPortType port = service.getPort(endpoint, org.jboss.jbossts.xts.soapfault.SoapFaultPortType.class, new AddressingFeature(true, true));
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