package com.arjuna.webservices11.wscoor.sei;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegistrationPortType;

// import org.jboss.ws.annotation.EndpointConfig;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.soap.Addressing;
import javax.annotation.Resource;

import com.arjuna.webservices11.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.webservices11.wsarj.ArjunaContext;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Sep 27, 2007
 * Time: 1:33:06 PM
 * To change this template use File | Settings | File Templates.
 */

@WebService(targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", name = "RegistrationPortType",
        wsdlLocation = "/WEB-INF/wsdl/wscoor-registration-binding.wsdl",
        serviceName = "RegistrationService",
        portName = "RegistrationPortType"
        // endpointInterface = "org.oasis_open.docs.ws_tx.wscoor._2006._06.RegistrationPortType",
)
@SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
// @EndpointConfig(configName = "Standard WSAddressing Endpoint")
@HandlerChain(file="handlers.xml")
@Addressing(required=true)
public class RegistrationPortTypeImpl implements RegistrationPortType
{
    @Resource private WebServiceContext webServiceCtx;

    @WebResult(targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", partName = "parameters", name = "RegisterResponse")
    @WebMethod(operationName = "RegisterOperation", action = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06/Register")
    public org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterResponseType registerOperation(
        @WebParam(targetNamespace = "http://docs.oasis-open.org/ws-tx/wscoor/2006/06", partName = "parameters", name = "Register")
        org.oasis_open.docs.ws_tx.wscoor._2006._06.RegisterType parameters
    )
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties
            = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        final ArjunaContext arjunaContext = ArjunaContext.getCurrentContext(ctx) ;

        return RegistrationCoordinatorProcessor.getCoordinator().register(parameters, inboundAddressProperties, arjunaContext);

    }
}
