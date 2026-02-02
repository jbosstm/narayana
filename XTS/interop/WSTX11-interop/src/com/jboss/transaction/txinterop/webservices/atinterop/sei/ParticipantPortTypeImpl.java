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

import com.jboss.transaction.txinterop.webservices.atinterop.processors.ATParticipantProcessor;
import com.jboss.transaction.txinterop.webservices.atinterop.client.InitiatorClient;
import com.jboss.transaction.txinterop.webservices.CoordinationContextManager;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.wsc11.messaging.MessageId;

import javax.jws.*;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.Action;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.xmlsoap.schemas.soap.envelope.Fault;

/**
 * Implementation class for WSTX 1.1 AT Interop Test Initiator service
 */
@WebService(name = "ParticipantPortType",
        targetNamespace = "http://fabrikam123.com",
        portName="ParticipantPortType",
        // wsdlLocation="/WEB-INF/wsdl/interopat-participant-binding.wsdl",
        serviceName="ParticipantService")
@Addressing(required=true)
@HandlerChain(file="participanthandlers.xml")
public class ParticipantPortTypeImpl // implements ParticipantPortType, SoapFaultPortType
{

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
    @Action(input="http://fabrikam123.com/CompletionCommit")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void completionCommit(
            @WebParam(name = "CompletionCommit", targetNamespace = "http://fabrikam123.com", partName = "parameters")
            String parameters)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        try {
            ATParticipantProcessor.getParticipant().completionCommit(parameters, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     * @param parameters
     */
    @WebMethod(operationName = "CompletionRollback", action = "http://fabrikam123.com/CompletionRollback")
    @Oneway
    @Action(input="http://fabrikam123.com/CompletionRollback")
    @SOAPBinding(parameterStyle = SOAPBinding.ParameterStyle.BARE)
    public void completionRollback(
        @WebParam(name = "CompletionRollback", targetNamespace = "http://fabrikam123.com", partName = "parameters")
        String parameters)
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        try {
            ATParticipantProcessor.getParticipant().completionRollback(parameters, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "Commit", action = "http://fabrikam123.com/Commit")
    @Oneway
    @Action(input="http://fabrikam123.com/Commit")
    @RequestWrapper(localName = "Commit", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void commit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().commit(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "Rollback", action = "http://fabrikam123.com/Rollback")
    @Oneway
    @Action(input="http://fabrikam123.com/Rollback")
    @RequestWrapper(localName = "Rollback", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void rollback()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().rollback(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "Phase2Rollback", action = "http://fabrikam123.com/Phase2Rollback")
    @Oneway
    @Action(input="http://fabrikam123.com/Phase2Rollback")
    @RequestWrapper(localName = "Phase2Rollback", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void phase2Rollback()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().phase2Rollback(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "Readonly", action = "http://fabrikam123.com/Readonly")
    @Oneway
    @Action(input="http://fabrikam123.com/Readonly")
    @RequestWrapper(localName = "Readonly", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void readonly()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().readonly(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "VolatileAndDurable", action = "http://fabrikam123.com/VolatileAndDurable")
    @Oneway
    @Action(input="http://fabrikam123.com/VolatileAndDurable")
    @RequestWrapper(localName = "VolatileAndDurable", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void volatileAndDurable()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().volatileAndDurable(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "EarlyReadonly", action = "http://fabrikam123.com/EarlyReadonly")
    @Oneway
    @Action(input="http://fabrikam123.com/EarlyReadonly")
    @RequestWrapper(localName = "EarlyReadonly", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void earlyReadonly()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().earlyReadonly(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "EarlyAborted", action = "http://fabrikam123.com/EarlyAborted")
    @Oneway
    @Action(input="http://fabrikam123.com/EarlyAborted")
    @RequestWrapper(localName = "EarlyAborted", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void earlyAborted()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().earlyAborted(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "ReplayCommit", action = "http://fabrikam123.com/ReplayCommit")
    @Oneway
    @Action(input="http://fabrikam123.com/ReplayCommit")
    @RequestWrapper(localName = "ReplayCommit", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void replayCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().replayCommit(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryPreparedCommit", action = "http://fabrikam123.com/RetryPreparedCommit")
    @Oneway
    @Action(input="http://fabrikam123.com/RetryPreparedCommit")
    @RequestWrapper(localName = "RetryPreparedCommit", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void retryPreparedCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().retryPreparedCommit(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryPreparedAbort", action = "http://fabrikam123.com/RetryPreparedAbort")
    @Oneway
    @Action(input="http://fabrikam123.com/RetryPreparedAbort")
    @RequestWrapper(localName = "RetryPreparedAbort", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void retryPreparedAbort()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().retryPreparedAbort(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "RetryCommit", action = "http://fabrikam123.com/RetryCommit")
    @Oneway
    @Action(input="http://fabrikam123.com/RetryCommit")
    @RequestWrapper(localName = "RetryCommit", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void retryCommit()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().retryCommit(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "PreparedAfterTimeout", action = "http://fabrikam123.com/PreparedAfterTimeout")
    @Oneway
    @Action(input="http://fabrikam123.com/PreparedAfterTimeout")
    @RequestWrapper(localName = "PreparedAfterTimeout", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void preparedAfterTimeout()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().preparedAfterTimeout(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     *
     */
    @WebMethod(operationName = "LostCommitted", action = "http://fabrikam123.com/LostCommitted")
    @Oneway
    @Action(input="http://fabrikam123.com/LostCommitted")
    @RequestWrapper(localName = "LostCommitted", targetNamespace = "http://fabrikam123.com", className = "com.jboss.transaction.txinterop.webservices.atinterop.generated.TestMessageType")
    public void lostCommitted()
    {
        MessageContext ctx = webServiceCtx.getMessageContext();
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            ATParticipantProcessor.getParticipant().lostCommitted(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
    }

    /**
     * send an acknowledgement notifying a successfuly processed request
     *
     * @param inboundMap identifes who to reply to and what message id the response should relate to
     */
    private void sendResponse(MAP inboundMap)
    {
        MAP outboundMap = AddressingHelper.createResponseContext(inboundMap, MessageId.getMessageId());

        try {
            InitiatorClient.getClient().sendResponse(outboundMap);
        } catch (Throwable th) {
            String to = outboundMap.getTo();
            System.out.println("com.jboss.transaction.txinterop.webservices.atinterop.sei.ParticipantPortTypeImpl_1: unable to send response to " + to);
            throw new ProtocolException(th);
        }
    }

    /**
     * send a soap fault notifying an unsuccessfuly processed request
     *
     * @param inboundMap identifes who to reply to and what message id the fault message should relate to
     */
    private void sendSoapFault(MAP inboundMap, SoapFault11 sf)
    {
        try {
            InitiatorClient.getClient().sendSoapFault(inboundMap, sf);
        } catch (Throwable th) {
            System.out.println("com.jboss.transaction.txinterop.webservices.atinterop.sei.ParticipantPortTypeImpl_2: unable to log soap fault " + sf);
            throw new ProtocolException(th);
        }
    }

    public void soapFault(
            @WebParam(name = "Fault", targetNamespace = "http://schemas.xmlsoap.org/soap/envelope/", partName = "fault")
            Fault fault)
    {
        // hmm, probably ought not to happen -- just log this as an error
        SoapFault11 soapFaultInternal = SoapFault11.fromFault(fault);
        System.out.println("com.jboss.transaction.txinterop.webservices.atinterop.sei.ParticipantPortTypeImpl_3: unexpected soap fault " + soapFaultInternal);
    }
}
