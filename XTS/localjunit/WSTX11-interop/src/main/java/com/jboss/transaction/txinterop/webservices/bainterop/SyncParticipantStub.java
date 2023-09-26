/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.txinterop.webservices.bainterop;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import org.jboss.ws.api.addressing.MAP;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.jboss.transaction.txinterop.webservices.bainterop.client.SyncParticipantClient;

/**
 * The participant stub.
 */
public class SyncParticipantStub implements ParticipantStub
{
    /***
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
        
        SyncParticipantClient.getClient().sendCancel(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendExit(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendFail(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendCannotComplete(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendParticipantCompleteClose(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendCoordinatorCompleteClose(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendUnsolicitedComplete(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendCompensate(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendCompensationFail(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendParticipantCancelCompletedRace(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendMessageLossAndRecovery(coordinationContext, map) ;
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
        
        SyncParticipantClient.getClient().sendMixedOutcome(coordinationContext, map) ;
    }
}