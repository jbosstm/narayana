package com.jboss.transaction.wstf.webservices.sc007;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.jboss.transaction.wstf.webservices.sc007.client.SyncParticipantClient;

import javax.xml.ws.addressing.AddressingProperties;

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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        SyncParticipantClient.getClient().sendCompletionCommit(addressingProperties, coordinatorURI) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        SyncParticipantClient.getClient().sendCompletionRollback(addressingProperties, coordinatorURI) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        SyncParticipantClient.getClient().sendCommit(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        SyncParticipantClient.getClient().sendRollback(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        SyncParticipantClient.getClient().sendPhase2Rollback(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;

        SyncParticipantClient.getClient().sendReadonly(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendVolatileAndDurable(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendEarlyReadonly(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendEarlyAborted(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendReplayCommit(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendRetryPreparedCommit(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendRetryPreparedAbort(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendRetryCommit(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendPreparedAfterTimeout(coordinationContext, addressingProperties) ;
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
        final AddressingProperties addressingProperties = AddressingHelper.createRequestContext(serviceURI, messageId) ;
        
        SyncParticipantClient.getClient().sendLostCommitted(coordinationContext, addressingProperties) ;
    }
}
