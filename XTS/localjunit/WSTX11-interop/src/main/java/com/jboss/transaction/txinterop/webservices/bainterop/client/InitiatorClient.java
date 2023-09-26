/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.txinterop.webservices.bainterop.client;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import com.jboss.transaction.txinterop.webservices.soapfault.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.SoapFault11;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;
import com.jboss.transaction.txinterop.webservices.bainterop.generated.InitiatorPortType;

/**
 * The initiator client.
 * @author kevin
 */
public class InitiatorClient
{
    /**
     * The client singleton.
     */
    private static final InitiatorClient CLIENT = new InitiatorClient() ;
    
    /**
     * The response action.
     */
    private static final String responseAction = BAInteropConstants.INTEROP_ACTION_RESPONSE ;
    
    /**
     * Construct the interop synch client.
     */
    private InitiatorClient()
    {
    }

    /**
     * Send a response.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendResponse(final MAP map)
        throws SoapFault, IOException
    {
        InitiatorPortType port = BAInteropClient.getInitiatorPort(map, responseAction);
        port.response();
    }

    /**
     * Send a fault.
     * @param map The addressing context.
     * @param soapFault The SOAP fault.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendSoapFault(final MAP map, final SoapFault11 soapFault)
        throws SoapFault, IOException
    {
        String soapFaultAction = soapFault.getAction() ;
        if (soapFaultAction == null)
        {
            soapFaultAction = faultAction;
        }

        AddressingHelper.installNoneReplyTo(map);
        SoapFaultClient.sendSoapFault(soapFault, map, soapFaultAction);
    }

    /**
     * Get the Interop client singleton.
     * @return The Interop client singleton.
     */
    public static InitiatorClient getClient()
    {
        return CLIENT ;
    }

    private static final String faultAction = "http://fabrikam123.com/SoapFault";
}