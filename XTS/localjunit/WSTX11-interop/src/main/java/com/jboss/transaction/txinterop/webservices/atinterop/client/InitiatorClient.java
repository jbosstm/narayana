/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.txinterop.webservices.atinterop.client;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.jboss.transaction.txinterop.webservices.soapfault.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.wsc11.messaging.MessageId;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;
import com.jboss.transaction.txinterop.webservices.atinterop.generated.InitiatorPortType;

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
    private static final String responseAction = ATInteropConstants.INTEROP_ACTION_RESPONSE ;
    
    /**
     * Construct the interop synch client.
     */
    private InitiatorClient()
    {
        //final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        //AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        //ClientPolicy.register(handlerRegistry) ;
        
        //soapService = new SoapService(handlerRegistry) ;
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
        InitiatorPortType port = ATInteropClient.getInitiatorPort(map, responseAction);
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
        MAP replyProperties = AddressingHelper.createFaultContext(map, MessageId.getMessageId());
        AddressingHelper.installNoneReplyTo(replyProperties);
        SoapFaultClient.sendSoapFault(soapFault, map, soapFaultAction);
    }

    private static final String faultAction = AtomicTransactionConstants.WSAT_ACTION_FAULT;
    /**
     * Get the Interop client singleton.
     * @return The Interop client singleton.
     */
    public static InitiatorClient getClient()
    {
        return CLIENT ;
    }
}