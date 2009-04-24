package com.jboss.transaction.txinterop.webservices.bainterop.sei;

import com.jboss.transaction.txinterop.webservices.atinterop.processors.ATInitiatorProcessor;
import com.jboss.transaction.txinterop.webservices.bainterop.processors.BAInitiatorProcessor;

import javax.xml.ws.RequestWrapper;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;

/**
 * Implementor class for OASIS WS-Interop 1.1 Initiator Service
 */
import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.RequestWrapper;


/**
 * Implementation class for WSTX 1.1 AT Interop Test Initiator service
 */
@WebService(name = "InitiatorPortType",
        targetNamespace = "http://fabrikam123.com/wsba",
        portName = "InitiatorPortType",
        wsdlLocation="/WEB-INF/wsdl/interopba-initiator-binding.wsdl",
        serviceName="InitiatorService")
// @EndpointConfig(configName = "Standard WSAddressing Endpoint")
@HandlerChain(file="initiatorhandlers.xml")
public class InitiatorPortTypeImpl {

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
    @RequestWrapper(localName = "Response", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void response()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);

        BAInitiatorProcessor.getInitiator().handleResponse(inboundAddressProperties) ;
    }

}