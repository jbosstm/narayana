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
package com.jboss.transaction.txinterop.webservices.bainterop.sei;

import com.jboss.transaction.txinterop.webservices.bainterop.client.InitiatorClient;
import com.jboss.transaction.txinterop.webservices.bainterop.processors.BAParticipantProcessor;
import com.jboss.transaction.txinterop.webservices.CoordinationContextManager;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.map.MAP;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.wsc11.messaging.MessageId;

import javax.jws.*;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.soap.Addressing;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;

import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;

/**
 * Implementation class for WSTX 1.1 AT Interop Test Initiator service
 */
@WebService(name = "ParticipantPortType",
        targetNamespace = "http://fabrikam123.com/wsba",
        portName="ParticipantPortType",
        wsdlLocation="/WEB-INF/wsdl/interopba-participant-binding.wsdl",
        serviceName="ParticipantService")
@Addressing(required=true)
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().cancel(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().exit(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().fail(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().cannotComplete(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().participantCompleteClose(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().coordinatorCompleteClose(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().unsolicitedComplete(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().compensate(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().participantCompensationFail(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().participantCancelCompletedRace(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().messageLossAndRecovery(coordinationContext, inboundMap);
        } catch (SoapFault11 sf) {
            sendSoapFault(inboundMap, sf);
            return;
        }
        sendResponse(inboundMap);
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
        MAP inboundMap = AddressingHelper.inboundMap(ctx);
        CoordinationContextType coordinationContext = CoordinationContextManager.getContext(ctx);
        try {
            BAParticipantProcessor.getParticipant().mixedOutcome(coordinationContext, inboundMap);
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
        MAP outboundAddressProperties = AddressingHelper.createResponseContext(inboundMap, MessageId.getMessageId());

        try {
            InitiatorClient.getClient().sendResponse(outboundAddressProperties);
        } catch (Throwable th) {
            String to = outboundAddressProperties.getTo();
            System.out.println("com.jboss.transaction.txinterop.webservices.bainterop.sei.ParticipantPortTypeImpl_1: unable to send response to " + to);
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
        MAP outboundAddressProperties = AddressingHelper.createResponseContext(inboundMap, MessageId.getMessageId());

        try {
            InitiatorClient.getClient().sendSoapFault(outboundAddressProperties, sf);
        } catch (Throwable th) {
            System.out.println("com.jboss.transaction.txinterop.webservices.bainterop.sei.ParticipantPortTypeImpl_2: unable to log soap fault " + sf);
            throw new ProtocolException(th);
        }
    }
}