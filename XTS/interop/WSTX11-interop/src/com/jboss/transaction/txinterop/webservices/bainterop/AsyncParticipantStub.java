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
package com.jboss.transaction.txinterop.webservices.bainterop;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.jboss.transaction.txinterop.webservices.bainterop.client.AsyncParticipantClient;
import com.jboss.transaction.txinterop.webservices.bainterop.processors.BAInitiatorCallback;
import com.jboss.transaction.txinterop.webservices.bainterop.processors.BAInitiatorProcessor;

/**
 * The participant stub.
 */
public class AsyncParticipantStub implements ParticipantStub
{
    /***
     * The participant stub singletong.
     */
    private static final ParticipantStub PARTICIPANT_STUB = new AsyncParticipantStub() ;
    
    /**
     * Get the participant stub singleton.
     * @return The participant stub singleton.
     */
    public static ParticipantStub getParticipantStub()
    {
        return PARTICIPANT_STUB ;
    }
    
    /**
     * Send a cancel request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void cancel(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendCancel(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a exit request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void exit(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendExit(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a fail request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void fail(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendFail(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a cannotComplete request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void cannotComplete(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendCannotComplete(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a participantCompleteClose request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void participantCompleteClose(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendParticipantCompleteClose(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a coordinatorCompleteClose request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void coordinatorCompleteClose(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendCoordinatorCompleteClose(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a unsolicitedComplete request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void unsolicitedComplete(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendUnsolicitedComplete(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a compensate request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void compensate(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendCompensate(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a compensationFail request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void compensationFail(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendCompensationFail(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a participantCancelCompletedRace request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void participantCancelCompletedRace(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendParticipantCancelCompletedRace(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a messageLossAndRecovery request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void messageLossAndRecovery(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendMessageLossAndRecovery(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }
    
    /**
     * Send a mixedOutcome request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void mixedOutcome(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final BAInitiatorProcessor initiator = BAInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendMixedOutcome(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Handle the callback.
     * @param callback The callback.
     * @throws SoapFault for errors.
     */
    private static void handleCallback(final RequestCallback callback)
        throws SoapFault
    {
        if (callback.hasFailed())
        {
            throw new SoapFault11(SoapFaultType.FAULT_RECEIVER, null, "Callback execution failed") ;
        }
        else if (!callback.hasTriggered())
        {
            throw new SoapFault11(SoapFaultType.FAULT_RECEIVER, null, "Callback wasn't triggered") ;
        }
        else if (!callback.isResponse())
        {
            throw callback.getSoapFault() ;
        }
    }
    
    /**
     * The request callback class.
     */
    private static final class RequestCallback extends BAInitiatorCallback
    {
        /**
         * The response flag.
         */
        private boolean response ;
        /**
         * The SOAP fault response.
         */
        private SoapFault soapFault ;
        
        /**
         * A response.
         * @param map The current addressing context.
         */
        public void response(final MAP map)
        {
            this.response = true ;
        }

        /**
         * A SOAP fault response.
         * @param soapFault The SOAP fault response.
         * @param map The current addressing context.
         */
        public void soapFault(final SoapFault soapFault, final MAP map)
        {
            this.soapFault = soapFault ;
        }
        
        /**
         * Was a response received?
         * @return true if a response was received, false otherwise.
         */
        boolean isResponse()
        {
            return response ;
        }
        
        /**
         * Get the SOAP fault.
         * @return The SOAP fault or null.
         */
        SoapFault getSoapFault()
        {
            return soapFault ;
        }
    }
}
