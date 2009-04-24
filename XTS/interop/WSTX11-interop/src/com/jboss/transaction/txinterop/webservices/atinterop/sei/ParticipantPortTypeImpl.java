/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.jboss.transaction.txinterop.webservices.atinterop.sei;

import com.jboss.transaction.txinterop.webservices.atinterop.generated.ObjectFactory;
import com.jboss.transaction.txinterop.webservices.atinterop.processors.ATParticipantProcessor;
import com.jboss.transaction.txinterop.webservices.atinterop.client.InitiatorClient;
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
import javax.xml.ws.WebServiceException;
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
        targetNamespace = "http://fabrikam123.com",
        portName="ParticipantPortType",
        wsdlLocation="/WEB-INF/wsdl/interopat-participant-binding.wsdl",
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
     * @param parameters
     */
    @WebMethod(operationName = "CompletionCommit", action = "http://fabrikam123.com/CompletionCommit")
    @Oneway
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void completionCommit(
            @WebParam(name = "CompletionCommit", targetNamespace = "http://fabrikam123.com", partName = "parameters")
            String parameters)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        try {
            ATParticipantProcessor.getParticipant().completionCommit(parameters, inboundAddressProperties);
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
    @WebMethod(operationName = "CompletionRollback", action = "http://fabrikam123.com/CompletionRollback")
    @Oneway
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void completionRollback(
        @WebParam(name = "CompletionRollback", targetNamespace = "http://fabrikam123.com", partName = "parameters")
        String parameters)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        try {
            ATParticipantProcessor.getParticipant().completionRollback(parameters, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Commit", action = "http://fabrikam123.com/Commit")
    @Oneway
    @RequestWrapper(localName = "Commit", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void commit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().commit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Rollback", action = "http://fabrikam123.com/Rollback")
    @Oneway
    @RequestWrapper(localName = "Rollback", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void rollback()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().rollback(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Phase2Rollback", action = "http://fabrikam123.com/Phase2Rollback")
    @Oneway
    @RequestWrapper(localName = "Phase2Rollback", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void phase2Rollback()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().phase2Rollback(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "Readonly", action = "http://fabrikam123.com/Readonly")
    @Oneway
    @RequestWrapper(localName = "Readonly", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void readonly()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().readonly(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "VolatileAndDurable", action = "http://fabrikam123.com/VolatileAndDurable")
    @Oneway
    @RequestWrapper(localName = "VolatileAndDurable", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void volatileAndDurable()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().volatileAndDurable(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "EarlyReadonly", action = "http://fabrikam123.com/EarlyReadonly")
    @Oneway
    @RequestWrapper(localName = "EarlyReadonly", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void earlyReadonly()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().earlyReadonly(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "EarlyAborted", action = "http://fabrikam123.com/EarlyAborted")
    @Oneway
    @RequestWrapper(localName = "EarlyAborted", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void earlyAborted()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().earlyAborted(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "ReplayCommit", action = "http://fabrikam123.com/ReplayCommit")
    @Oneway
    @RequestWrapper(localName = "ReplayCommit", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void replayCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().replayCommit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryPreparedCommit", action = "http://fabrikam123.com/RetryPreparedCommit")
    @Oneway
    @RequestWrapper(localName = "RetryPreparedCommit", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void retryPreparedCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().retryPreparedCommit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryPreparedAbort", action = "http://fabrikam123.com/RetryPreparedAbort")
    @Oneway
    @RequestWrapper(localName = "RetryPreparedAbort", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void retryPreparedAbort()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().retryPreparedAbort(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryCommit", action = "http://fabrikam123.com/RetryCommit")
    @Oneway
    @RequestWrapper(localName = "RetryCommit", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void retryCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().retryCommit(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "PreparedAfterTimeout", action = "http://fabrikam123.com/PreparedAfterTimeout")
    @Oneway
    @RequestWrapper(localName = "PreparedAfterTimeout", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void preparedAfterTimeout()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().preparedAfterTimeout(coordinationContext, inboundAddressProperties);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundAddressProperties, sf);
            return;
        }
        sendResponse(inboundAddressProperties);
    }

    /**
     *
     */
    @WebMethod(operationName = "LostCommitted", action = "http://fabrikam123.com/LostCommitted")
    @Oneway
    @RequestWrapper(localName = "LostCommitted", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void lostCommitted()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        AddressingProperties inboundAddressProperties = (AddressingProperties)ctx.get(JAXWSAConstants.SERVER_ADDRESSING_PROPERTIES_INBOUND);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().lostCommitted(coordinationContext, inboundAddressProperties);
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
            System.out.println("com.jboss.transaction.txinterop.webservices.atinterop.sei.ParticipantPortTypeImpl_1: unable to send response to " + uri);
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
            System.out.println("com.jboss.transaction.txinterop.webservices.atinterop.sei.ParticipantPortTypeImpl_2: unable to log soap fault " + sf);
            throw new ProtocolException(th);
        }
    }
}
