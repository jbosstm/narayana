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
import jakarta.annotation.Resource;
import jakarta.jws.HandlerChain;
import org.xmlsoap.schemas.soap.envelope.Fault;

import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebParam;
import jakarta.jws.WebService;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.Addressing;
import org.jboss.ws.api.addressing.MAP;

/**
 * Asynchronous endpoint to receive fault responses to requests that used 
 * the <em>org.jboss.jbossts.xts.useAsynchronousRequest</em> property.
 * Microsoft WSCOOR implementation needs to receive a valid <em>FaultTo</em>
 * ws-addressing header in order to send the error. This endpoint is not
 * part of the standard.
 * 
 * @author rmartinc
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06",
        name = "CoordinationFaultPortType", 
        serviceName = "CoordinationFaultService",
        portName = "CoordinationFaultPortType")
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
@HandlerChain(file="/ws-c_handlers.xml")
@Addressing(required=true)
public class CoordinationFaultPortTypeImpl {

    @Resource private WebServiceContext webServiceCtx;
    
    @WebMethod(operationName = "SoapFault", action = "http://www.w3.org/2005/08/addressing/soap/fault")
    @Oneway
    @Action(input="http://www.w3.org/2005/08/addressing/soap/fault")
    public void soapFault(
        @WebParam(name = "Fault", targetNamespace = "http://schemas.xmlsoap.org/soap/envelope/", partName = "fault")
        Fault fault) {
    
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        if (inboundMap.getRelatesTo() != null)  {
            WSCLogger.logger.tracev("CoordinationFaultPortTypeImpl receiving fault for message={0} - string={1} code={2} details={3}",
                inboundMap.getRelatesTo().getRelatesTo(), fault.getFaultstring(), 
                fault.getFaultcode(), fault.getDetail() == null? "null" : fault.getDetail().getAny());
            AsynchronousRegistrationMapper.getInstance().assignFault(inboundMap.getRelatesTo().getRelatesTo(), fault);
        } else {
            WSCLogger.i18NLogger.error_empty_messageId_received_by_async_endpoint();
        }
    }

}
