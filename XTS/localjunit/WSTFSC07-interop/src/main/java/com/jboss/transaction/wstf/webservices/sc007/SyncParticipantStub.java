/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package com.jboss.transaction.wstf.webservices.sc007;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.jboss.transaction.wstf.webservices.sc007.client.SyncParticipantClient;

/**
 * The participant stub.
 */
public class SyncParticipantStub implements ParticipantStub
{
    /**
     * The participant stub singletong.
     */
    private static final ParticipantStub PARTICIPANT_STUB = new SyncParticipantStub() ;
    
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

        SyncParticipantClient.getClient().sendCompletionCommit(map, coordinatorURI) ;
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

        SyncParticipantClient.getClient().sendCompletionRollback(map, coordinatorURI) ;
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

        SyncParticipantClient.getClient().sendCommit(coordinationContext, map) ;
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

        SyncParticipantClient.getClient().sendRollback(coordinationContext, map) ;
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

        SyncParticipantClient.getClient().sendPhase2Rollback(coordinationContext, map) ;
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

        SyncParticipantClient.getClient().sendReadonly(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendVolatileAndDurable(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendEarlyReadonly(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendEarlyAborted(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendReplayCommit(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendRetryPreparedCommit(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendRetryPreparedAbort(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendRetryCommit(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendPreparedAfterTimeout(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendLostCommitted(coordinationContext, map) ;
    }
}