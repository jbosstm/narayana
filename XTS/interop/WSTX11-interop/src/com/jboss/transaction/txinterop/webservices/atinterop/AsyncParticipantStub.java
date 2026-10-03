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
package com.jboss.transaction.txinterop.webservices.atinterop;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.SoapFault11;
import com.jboss.transaction.txinterop.webservices.atinterop.client.AsyncParticipantClient;
import com.jboss.transaction.txinterop.webservices.atinterop.processors.ATInitiatorCallback;
import com.jboss.transaction.txinterop.webservices.atinterop.processors.ATInitiatorProcessor;

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
     * Send a completion commit request.
     * @param serviceURI The target service URI.
     * @param coordinatorURI The coordinator URI.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void completionCommit(final String serviceURI, final String coordinatorURI)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendCompletionCommit(map, coordinatorURI) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a completion rollback request.
     * @param serviceURI The target service URI.
     * @param coordinatorURI The coordinator URI.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void completionRollback(final String serviceURI, final String coordinatorURI)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendCompletionRollback(map, coordinatorURI) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a commit request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void commit(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendCommit(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a rollback request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void rollback(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendRollback(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a phase2Rollback request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void phase2Rollback(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendPhase2Rollback(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a readonly request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void readonly(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendReadonly(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a volatileAndDurable request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void volatileAndDurable(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendVolatileAndDurable(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send an earlyReadonly request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void earlyReadonly(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendEarlyReadonly(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a earlyAborted request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void earlyAborted(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendEarlyAborted(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a replayCommit request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void replayCommit(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendReplayCommit(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a retryPreparedCommit request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void retryPreparedCommit(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendRetryPreparedCommit(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a retryPreparedAbort request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void retryPreparedAbort(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendRetryPreparedAbort(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a retryCommit request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void retryCommit(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendRetryCommit(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a preparedAfterTimeout request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void preparedAfterTimeout(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendPreparedAfterTimeout(coordinationContext, map) ;
            callback.waitUntilTriggered(15000) ;
        }
        finally
        {
            initiator.removeCallback(messageId) ;
        }
        
        handleCallback(callback) ;
    }

    /**
     * Send a lostCommitted request.
     * @param serviceURI The target service URI.
     * @param coordinationContext The coordination context.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void lostCommitted(final String serviceURI, final CoordinationContextType coordinationContext)
        throws SoapFault, IOException
    {
        final String messageId = MessageId.getMessageId() ;
        final MAP map = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        final RequestCallback callback = new RequestCallback() ;
        final ATInitiatorProcessor initiator = ATInitiatorProcessor.getInitiator() ;
        initiator.registerCallback(messageId, callback) ;
        try
        {
            AsyncParticipantClient.getClient().sendLostCommitted(coordinationContext, map) ;
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
    private static final class RequestCallback extends ATInitiatorCallback
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
