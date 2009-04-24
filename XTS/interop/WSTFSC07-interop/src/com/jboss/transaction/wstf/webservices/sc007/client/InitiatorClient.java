package com.jboss.transaction.wstf.webservices.sc007.client;

import java.io.IOException;
import java.net.URISyntaxException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.wsc11.messaging.MessageId;
import com.jboss.transaction.wstf.webservices.sc007.InteropConstants;
import com.jboss.transaction.wstf.webservices.sc007.generated.InitiatorPortType;

import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.AddressingBuilder;

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
    private static final String responseAction = InteropConstants.INTEROP_ACTION_RESPONSE ;
    
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
     * @param addressingProperties The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendResponse(final AddressingProperties addressingProperties)
        throws SoapFault, IOException
    {
        InitiatorPortType port = InteropClient.getInitiatorPort(addressingProperties, responseAction);
        port.response();
    }

    /**
     * Send a fault.
     * @param addressingProperties The addressing context.
     * @param soapFault The SOAP fault.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendSoapFault(final AddressingProperties addressingProperties, final SoapFault11 soapFault)
        throws SoapFault, IOException
    {
        String soapFaultAction = soapFault.getAction() ;
        AttributedURI actionURI = null;
        if (soapFaultAction == null)
        {
            soapFaultAction = faultAction;
        }
        try {
            actionURI = builder.newURI(soapFaultAction);
        } catch (URISyntaxException e) {
            // TODO log error here
        }
        AddressingProperties replyProperties = AddressingHelper.createFaultContext(addressingProperties, MessageId.getMessageId());
        AddressingHelper.installNoneReplyTo(replyProperties);
        SoapFaultClient.sendSoapFault(soapFault, addressingProperties, actionURI);
    }

    private static final String faultAction = AtomicTransactionConstants.WSAT_ACTION_FAULT;
    private static final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
    /**
     * Get the Interop client singleton.
     * @return The Interop client singleton.
     */
    public static InitiatorClient getClient()
    {
        return CLIENT ;
    }
}
