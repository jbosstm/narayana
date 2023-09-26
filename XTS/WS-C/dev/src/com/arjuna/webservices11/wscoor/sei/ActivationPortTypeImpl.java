/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.webservices11.wscoor.sei;

import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.ActivationPortType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CreateCoordinationContextResponseType;

import jakarta.annotation.Resource;
import jakarta.jws.*;
import jakarta.jws.soap.SOAPBinding;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.Action;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.soap.Addressing;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Sep 27, 2007
 * Time: 10:31:43 AM
 * To change this template use File | Settings | File Templates.
 */
@WebService(targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", name = "ActivationPortType",
        // wsdlLocation = "/WEB-INF/wsdl/wscoor-activation-binding.wsdl",
        serviceName = "ActivationService",
        portName = "ActivationPortType"
        // endpointInterface = "org.oasis_open.docs.ws_tx.wscoor._2006._06.ActivationPortType",
)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
// @EndpointConfig(configName = "Standard WSAddressing Endpoint")
@HandlerChain(file="/ws-c_handlers.xml")
@Addressing(required=true)
public class ActivationPortTypeImpl // implements ActivationPortType
{
    @Resource private WebServiceContext webServiceCtx;

    @WebMethod(operationName = "CreateCoordinationContextOperation", action = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06/CreateCoordinationContext")
    @WebResult(name = "CreateCoordinationContextResponse", targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", partName = "parameters")
    @Action(input="http://docs.oasis-open.org/ws-tx/wscoor/2006/06/CreateCoordinationContext", output="http://docs.oasis-open.org/ws-tx/wscoor/2006/06/CreateCoordinationContextResponse")
    public CreateCoordinationContextResponseType createCoordinationContextOperation(
        @WebParam(name = "CreateCoordinationContext", targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", partName = "parameters")
        CreateCoordinationContextType parameters)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        HttpServletRequest request = (HttpServletRequest)ctx.get(MessageContext.SERVLET_REQUEST);
        boolean isSecure = "https".equals(request.getScheme());
        MAP inboundMAP = AddressingHelper.inboundMap(ctx);
        return ActivationCoordinatorProcessor.getCoordinator().createCoordinationContext(parameters, inboundMAP, isSecure);
   }
}
