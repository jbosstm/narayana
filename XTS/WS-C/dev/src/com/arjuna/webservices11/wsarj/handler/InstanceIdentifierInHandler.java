package com.arjuna.webservices11.wsarj.handler;

import com.arjuna.webservices.logging.WSCLogger;

import jakarta.xml.ws.handler.soap.SOAPMessageContext;
import jakarta.xml.ws.ProtocolException;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Feb 27, 2008
 * Time: 6:16:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class InstanceIdentifierInHandler extends InstanceIdentifierHandler {
    /**
     * override the parent behaviour so that this handler processes incoming arjuna instance identifiers
     * but avoids inserting them into outgoing messages
     *
     * @param context
     * @return
     * @throws jakarta.xml.ws.ProtocolException
     */
    protected boolean handleMessageOutbound(SOAPMessageContext context) throws ProtocolException {
        if (WSCLogger.logger.isTraceEnabled()) {
            WSCLogger.logger.trace("InstanceIdentifierInHandler.handleMessageOutbound()");
            WSCLogger.traceMessage(context);
        }

        return true;
    }
}
