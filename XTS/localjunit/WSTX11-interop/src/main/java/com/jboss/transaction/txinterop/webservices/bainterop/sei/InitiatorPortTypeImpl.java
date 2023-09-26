/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.txinterop.webservices.bainterop.sei;

import com.jboss.transaction.txinterop.webservices.bainterop.processors.BAInitiatorProcessor;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.SoapFault11;
import org.jboss.ws.api.addressing.MAP;
import org.xmlsoap.schemas.soap.envelope.Fault;

import jakarta.xml.ws.Action;
import jakarta.xml.ws.RequestWrapper;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.annotation.Resource;

/**
 * Implementor class for OASIS WS-Interop 1.1 Initiator Service
 */
import jakarta.jws.Oneway;
import jakarta.jws.WebMethod;
import jakarta.jws.WebService;
import jakarta.jws.WebParam;
import jakarta.xml.ws.soap.Addressing;

/**
 * Implementation class for WSTX 1.1 AT Interop Test Initiator service
 */
@WebService(name = "InitiatorPortType",
        targetNamespace = "http://fabrikam123.com/wsba",
        portName = "InitiatorPortType",
        // wsdlLocation="/WEB-INF/wsdl/interopba-initiator-binding.wsdl",
        serviceName="InitiatorService")
@Addressing(required=true)
public class InitiatorPortTypeImpl // implements InitiatorPortType, SoapFaultPortType
{

    /**
     * injected resource providing access to WSA addressing properties
     */
    @Resource
    private WebServiceContext webServiceCtx;

    /**
     *
     */
    @WebMethod(operationName = "Response", action = "http://fabrikam123.com/wsba/Response")
    @Oneway
    @Action(input="http://fabrikam123.com/wsba/Response")
    @RequestWrapper(localName = "Response", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void response()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);

        BAInitiatorProcessor.getInitiator().handleResponse(inboundMap) ;
    }

    public void soapFault(
            @WebParam(name = "Fault", targetNamespace = "http://schemas.xmlsoap.org/soap/envelope/", partName = "fault")
            Fault fault)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);

        SoapFault11 soapFaultInternal = SoapFault11.fromFault(fault);
        BAInitiatorProcessor.getInitiator().handleSoapFault(soapFaultInternal, inboundMap) ;
    }
}