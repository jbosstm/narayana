/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wscoor.sei;

import com.arjuna.webservices.logging.WSCLogger;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.wsc11.AsynchronousRegistrationMapper;
import jakarta.jws.*;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.soap.Addressing;
import jakarta.annotation.Resource;
import jakarta.xml.ws.handler.MessageContext;
import org.jboss.ws.api.addressing.MAP;

/**
 * Asynchronous endpoint to receive Registration responses that are requested 
 * using the <em>org.jboss.jbossts.xts.useAsynchronousRequest</em> property.
 * Microsoft WSCOOR implementation needs to receive a valid <em>ReplyTo</em>
 * ws-addressing header in order to respond correctly. This endpoint is not
 * part of the standard.
 * 
 * @author rmartinc
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", 
        name = "RegistrationResponsePortType",
        serviceName = "RegistrationResponseService",
        portName = "RegistrationResponsePortType"
)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@HandlerChain(file="/ws-c_handlers.xml")
@Addressing(required=true)
public class RegistrationResponsePortTypeImpl {
    
    @Resource private WebServiceContext webServiceCtx;

    @WebMethod(operationName = "RegisterResponseOperation", action = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06/RegisterResponse")
    @Oneway
    @Action(input="http://docs.oasis-open.org/ws-tx/wscoor/2006/06/RegisterResponse")
    public void registerResponseOperation(
        @WebParam(targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", partName = "parameters", name = "RegisterResponse")
        org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType parameters) {
        
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        if (inboundMap.getRelatesTo() != null)  {
            WSCLogger.logger.tracev("RegistrationResponsePortTypeImpl received response for messageId {0}", inboundMap.getRelatesTo().getRelatesTo());
            AsynchronousRegistrationMapper.getInstance().assignResponse(inboundMap.getRelatesTo().getRelatesTo(), parameters);
        } else {
            WSCLogger.i18NLogger.error_empty_messageId_received_by_async_endpoint();
        }
    }
}