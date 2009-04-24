package com.jboss.transaction.wstf.webservices.sc007.sei;

import com.jboss.transaction.wstf.webservices.sc007.processors.ParticipantProcessor;
import com.jboss.transaction.wstf.webservices.sc007.client.InitiatorClient;
import com.jboss.transaction.wstf.webservices.CoordinationContextManager;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.wsc11.messaging.MessageId;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import java.net.URI;


/**
 * Implementation class for WSTX 1.1 AT Interop Test Initiator service
 */
@WebService(name = "ParticipantPortType",
        targetNamespace = "http://www.wstf.org/sc007",
        portName="sc007ParticipantPort",
        wsdlLocation="/WEB-INF/wsdl/sc007.wsdl",
        serviceName="Sc007Service")
// @EndpointConfig(configName = "Standard WSAddressing Endpoint")
@HandlerChain(file="participanthandlers.xml")
public class ParticipantPortTypeImpl {

    /**
     * injected resource providing access to WSA addressing properties
     */
    @Resource
    private WebServiceContext webServiceCtx;

    /**
     *
     * @param parameters
     */
    @WebMethod(operationName = "CompletionCommit", action = "http://www.wstf.org/docs/scenarios/sc007/CompletionCommit")
    @Oneway
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void completionCommit(
            @WebParam(name = "CompletionCommit", targetNamespace = "http://www.wstf.org/sc007", partName = "parameters")
            String parameters)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        try {
            ParticipantProcessor.getParticipant().completionCommit(parameters, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     * @param parameters
     */
    @WebMethod(operationName = "CompletionRollback", action = "http://www.wstf.org/docs/scenarios/sc007/CompletionRollback")
    @Oneway
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void completionRollback(
        @WebParam(name = "CompletionRollback", targetNamespace = "http://www.wstf.org/sc007", partName = "parameters")
        String parameters)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        try {
            ParticipantProcessor.getParticipant().completionRollback(parameters, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Commit", action = "http://www.wstf.org/docs/scenarios/sc007/Commit")
    @Oneway
    @RequestWrapper(localName = "Commit", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void commit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().commit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Rollback", action = "http://www.wstf.org/docs/scenarios/sc007/Rollback")
    @Oneway
    @RequestWrapper(localName = "Rollback", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void rollback()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().rollback(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Phase2Rollback", action = "http://www.wstf.org/docs/scenarios/sc007/Phase2Rollback")
    @Oneway
    @RequestWrapper(localName = "Phase2Rollback", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void phase2Rollback()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().phase2Rollback(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Readonly", action = "http://www.wstf.org/docs/scenarios/sc007/Readonly")
    @Oneway
    @RequestWrapper(localName = "Readonly", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void readonly()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().readonly(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "VolatileAndDurable", action = "http://www.wstf.org/docs/scenarios/sc007/VolatileAndDurable")
    @Oneway
    @RequestWrapper(localName = "VolatileAndDurable", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void volatileAndDurable()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().volatileAndDurable(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "EarlyReadonly", action = "http://www.wstf.org/docs/scenarios/sc007/EarlyReadonly")
    @Oneway
    @RequestWrapper(localName = "EarlyReadonly", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void earlyReadonly()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().earlyReadonly(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "EarlyAborted", action = "http://www.wstf.org/docs/scenarios/sc007/EarlyAborted")
    @Oneway
    @RequestWrapper(localName = "EarlyAborted", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void earlyAborted()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().earlyAborted(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "ReplayCommit", action = "http://www.wstf.org/docs/scenarios/sc007/ReplayCommit")
    @Oneway
    @RequestWrapper(localName = "ReplayCommit", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void replayCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().replayCommit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryPreparedCommit", action = "http://www.wstf.org/docs/scenarios/sc007/RetryPreparedCommit")
    @Oneway
    @RequestWrapper(localName = "RetryPreparedCommit", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void retryPreparedCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().retryPreparedCommit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryPreparedAbort", action = "http://www.wstf.org/docs/scenarios/sc007/RetryPreparedAbort")
    @Oneway
    @RequestWrapper(localName = "RetryPreparedAbort", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void retryPreparedAbort()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().retryPreparedAbort(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryCommit", action = "http://www.wstf.org/docs/scenarios/sc007/RetryCommit")
    @Oneway
    @RequestWrapper(localName = "RetryCommit", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void retryCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().retryCommit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "PreparedAfterTimeout", action = "http://www.wstf.org/docs/scenarios/sc007/PreparedAfterTimeout")
    @Oneway
    @RequestWrapper(localName = "PreparedAfterTimeout", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void preparedAfterTimeout()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().preparedAfterTimeout(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "LostCommitted", action = "http://www.wstf.org/docs/scenarios/sc007/LostCommitted")
    @Oneway
    @RequestWrapper(localName = "LostCommitted", targetNamespace = "http://www.wstf.org/sc007", className = "com.jboss.transaction.txinterop.webservices.sc007.generated.TestMessageType")
    public void lostCommitted()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ParticipantProcessor.getParticipant().lostCommitted(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     * send an acknowledgement notifying a successfuly processed request
     *
     * @param inboundAddressProperties identifes who to reply to and what message id the response should relate to
     */
    private void sendResponse(AddressingProperties inboundAddressProperties)
    {
        AddressingProperties outboundAddressProperties = AddressingHelper.createResponseContext(inboundAddressProperties, MessageId.getMessageId());

        try {
            InitiatorClient.getClient().sendResponse(outboundAddressProperties);
        } catch (Throwable th) {
            URI uri = outboundAddressProperties.getTo().getURI();
            System.out.println("com.jboss.transaction.txinterop.webservices.sc007.sei.ParticipantPortTypeImpl_1: unable to send response to " + uri);
            throw new ProtocolException(th);
        }
    }

    /**
     * send a soap fault notifying an unsuccessfuly processed request
     *
     * @param inboundAddressProperties identifes who to reply to and what message id the fault message should relate to
     */
    private void sendSoapFault(AddressingProperties inboundAddressProperties, SoapFault11 sf)
    {
        try {
            InitiatorClient.getClient().sendSoapFault(inboundAddressProperties, sf);
        } catch (Throwable th) {
            System.out.println("com.jboss.transaction.txinterop.webservices.sc007.sei.ParticipantPortTypeImpl_2: unable to log soap fault " + sf);
            throw new ProtocolException(th);
        }
    }
}
