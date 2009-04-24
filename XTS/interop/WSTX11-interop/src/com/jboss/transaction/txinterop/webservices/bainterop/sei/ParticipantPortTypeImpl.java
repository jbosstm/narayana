package com.jboss.transaction.txinterop.webservices.bainterop.sei;

import com.jboss.transaction.txinterop.webservices.bainterop.client.InitiatorClient;
import com.jboss.transaction.txinterop.webservices.bainterop.processors.BAParticipantProcessor;
import com.jboss.transaction.txinterop.webservices.CoordinationContextManager;
import com.arjuna.webservices.SoapFault;
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

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContext;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

import java.net.URI;


/**
 * Implementation class for WSTX 1.1 AT Interop Test Initiator service
 */
@WebService(name = "ParticipantPortType",
        targetNamespace = "http://fabrikam123.com/wsba",
        portName="ParticipantPortType",
        wsdlLocation="/WEB-INF/wsdl/interopba-participant-binding.wsdl",
        serviceName="ParticipantService")
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
     */
    @WebMethod(operationName = "Cancel", action = "http://fabrikam123.com/wsba/Cancel")
    @Oneway
    @RequestWrapper(localName = "Cancel", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void cancel()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().cancel(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Exit", action = "http://fabrikam123.com/wsba/Exit")
    @Oneway
    @RequestWrapper(localName = "Exit", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void exit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().exit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Fail", action = "http://fabrikam123.com/wsba/Fail")
    @Oneway
    @RequestWrapper(localName = "Fail", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void fail()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().fail(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "CannotComplete", action = "http://fabrikam123.com/wsba/CannotComplete")
    @Oneway
    @RequestWrapper(localName = "CannotComplete", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void cannotComplete()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().cannotComplete(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "ParticipantCompleteClose", action = "http://fabrikam123.com/wsba/ParticipantCompleteClose")
    @Oneway
    @RequestWrapper(localName = "ParticipantCompleteClose", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void participantCompleteClose()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().participantCompleteClose(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "CoordinatorCompleteClose", action = "http://fabrikam123.com/wsba/CoordinatorCompleteClose")
    @Oneway
    @RequestWrapper(localName = "CoordinatorCompleteClose", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void coordinatorCompleteClose()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().coordinatorCompleteClose(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "UnsolicitedComplete", action = "http://fabrikam123.com/wsba/UnsolicitedComplete")
    @Oneway
    @RequestWrapper(localName = "UnsolicitedComplete", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void unsolicitedComplete()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().unsolicitedComplete(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Compensate", action = "http://fabrikam123.com/wsba/Compensate")
    @Oneway
    @RequestWrapper(localName = "Compensate", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void compensate()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().compensate(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "CompensationFail", action = "http://fabrikam123.com/wsba/CompensationFail")
    @Oneway
    @RequestWrapper(localName = "CompensationFail", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void compensationFail()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().participantCompensationFail(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "ParticipantCancelCompletedRace", action = "http://fabrikam123.com/wsba/ParticipantCancelCompletedRace")
    @Oneway
    @RequestWrapper(localName = "ParticipantCancelCompletedRace", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void participantCancelCompletedRace()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().participantCancelCompletedRace(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "MessageLossAndRecovery", action = "http://fabrikam123.com/wsba/MessageLossAndRecovery")
    @Oneway
    @RequestWrapper(localName = "MessageLossAndRecovery", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void messageLossAndRecovery()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().messageLossAndRecovery(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "MixedOutcome", action = "http://fabrikam123.com/wsba/MixedOutcome")
    @Oneway
    @RequestWrapper(localName = "MixedOutcome", targetNamespace = "http://fabrikam123.com/wsba", className = "com.jboss.transaction.txinterop.webservices.bainterop.generated.TestMessageType")
    public void mixedOutcome()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().mixedOutcome(coordinationContext, inboundAddressProperties);
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
            System.out.println("com.jboss.transaction.txinterop.webservices.bainterop.sei.ParticipantPortTypeImpl_1: unable to send response to " + uri);
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
        AddressingProperties outboundAddressProperties = AddressingHelper.createResponseContext(inboundAddressProperties, MessageId.getMessageId());

        try {
            InitiatorClient.getClient().sendSoapFault(outboundAddressProperties, sf);
        } catch (Throwable th) {
            System.out.println("com.jboss.transaction.txinterop.webservices.bainterop.sei.ParticipantPortTypeImpl_2: unable to log soap fault " + sf);
            throw new ProtocolException(th);
        }
    }
}