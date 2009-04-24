package com.jboss.transaction.txinterop.webservices.atinterop.sei;

import com.jboss.transaction.txinterop.webservices.atinterop.processors.ATInitiatorProcessor;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices11.SoapFault11;

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
import javax.jws.WebParam;
import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.jboss.jbossts.xts.soapfault.Fault;

import java.util.Iterator;
import java.util.Vector;


/**
 * Implementation class for WSTX 1.1 AT Interop Test Initiator service
 */
@WebService(name = "InitiatorPortType",
        targetNamespace = "http://fabrikam123.com",
        portName = "InitiatorPortType",
        wsdlLocation="/WEB-INF/wsdl/interopat-initiator-binding.wsdl",
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
    @WebMethod(operationName = "Response", action = "http://fabrikam123.com/Response")
    @Oneway
    @RequestWrapper(localName = "Response", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void response()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);

        ATInitiatorProcessor.getInitiator().handleResponse(inboundAddressProperties) ;
    }

    @WebMethod(operationName = "SoapFault", action = "http://fabrikam123.com/SoapFault")
    @Oneway
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void soapFault(
            @WebParam(name = "Fault", targetNamespace = "http://schemas.xmlsoap.org/soap/envelope/", partName = "parameters")
            Fault fault)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);

        SoapFault11 soapFaultInternal = SoapFault11.fromFault(fault);
        ATInitiatorProcessor.getInitiator().handleSoapFault(soapFaultInternal, inboundAddressProperties) ;
    }

}