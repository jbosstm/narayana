/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
