package com.arjuna.mw.wst.service;

import com.arjuna.mw.wst.service.*;

import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.soap.SOAPMessage;

/**
 * specialised version which creates and interposes a subordinate AT transaction when
 * it finds an incoming AT context in the message headers
 */
public class JaxWSSubordinateHeaderContextProcessor extends JaxWSHeaderContextProcessor
{
    /**
     * Process the tx context header that is attached to the received message.
     *
     * @param msgContext
     * @return true
     */
    protected boolean handleInbound(MessageContext msgContext)
    {
        final SOAPMessageContext soapMessageContext = (SOAPMessageContext)msgContext ;
        final SOAPMessage soapMessage = soapMessageContext.getMessage() ;

        // the generic handler can do the job for us -- just pass the correct flag

        return handleInboundMessage(soapMessage, false);
    }
}