/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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