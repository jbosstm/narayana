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
package com.jboss.transaction.txinterop.webservices.atinterop.client;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPBuilderFactory;
import com.jboss.transaction.txinterop.webservices.CoordinationContextManager;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;
import com.jboss.transaction.txinterop.webservices.atinterop.generated.ParticipantPortType;

/**
 * The participant client.
 * @author kevin
 */
public class AsyncParticipantClient
{
    /**
     * The client singleton.
     */
    private static final AsyncParticipantClient CLIENT = new AsyncParticipantClient() ;

    /**
     * The completion commit action.
     */
    private static final String completionCommitAction = ATInteropConstants.INTEROP_ACTION_COMPLETION_COMMIT ;
    /**
     * The completion rollback Action.
     */
    private static final String completionRollbackAction = ATInteropConstants.INTEROP_ACTION_COMPLETION_ROLLBACK ;
    /**
     * The commit Action.
     */
    private static final String commitAction = ATInteropConstants.INTEROP_ACTION_COMMIT ;
    /**
     * The rollback Action.
     */
    private static final String rollbackAction = ATInteropConstants.INTEROP_ACTION_ROLLBACK ;
    /**
     * The phase 2 rollback Action.
     */
    private static final String phase2RollbackAction = ATInteropConstants.INTEROP_ACTION_PHASE_2_ROLLBACK ;
    /**
     * The readonly Action.
     */
    private static final String readonlyAction = ATInteropConstants.INTEROP_ACTION_READONLY ;
    /**
     * The volatile and durable Action.
     */
    private static final String volatileAndDurableAction = ATInteropConstants.INTEROP_ACTION_VOLATILE_AND_DURABLE ;
    /**
     * The early readonly Action.
     */
    private static final String earlyReadonlyAction = ATInteropConstants.INTEROP_ACTION_EARLY_READONLY ;
    /**
     * The early aborted Action.
     */
    private static final String earlyAbortedAction = ATInteropConstants.INTEROP_ACTION_EARLY_ABORTED ;
    /**
     * The replay commit Action.
     */
    private static final String replayCommitAction = ATInteropConstants.INTEROP_ACTION_REPLAY_COMMIT ;
    /**
     * The retry prepared commit Action.
     */
    private static final String retryPreparedCommitAction = ATInteropConstants.INTEROP_ACTION_RETRY_PREPARED_COMMIT ;
    /**
     * The retry prepared abort Action.
     */
    private static final String retryPreparedAbortAction = ATInteropConstants.INTEROP_ACTION_RETRY_PREPARED_ABORT ;
    /**
     * The retry commit Action.
     */
    private static final String retryCommitAction = ATInteropConstants.INTEROP_ACTION_RETRY_COMMIT ;
    /**
     * The prepared after timeout Action.
     */
    private static final String preparedAfterTimeoutAction = ATInteropConstants.INTEROP_ACTION_PREPARED_AFTER_TIMEOUT ;
    /**
     * The lost committed Action.
     */
    private static final String lostCommittedAction = ATInteropConstants.INTEROP_ACTION_LOST_COMMITTED ;

    /**
     * The initiator URI for replies.
     */
    private MAPEndpoint initiator = null;

    /**
     * Construct the interop synch client.
     */
    private AsyncParticipantClient()
    {
        //final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        //AddressingPolicy.register(handlerRegistry) ;
        // Add coordination context
        //CoordinationContextPolicy.register(handlerRegistry) ;
        // Add client policies
        //ClientPolicy.register(handlerRegistry) ;

        //soapService = new SoapService(handlerRegistry) ;
        MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
        final String initiatorURIString = ServiceRegistry.getRegistry().getServiceURI(ATInteropConstants.SERVICE_INITIATOR) ;
        initiator = builder.newEndpoint(initiatorURIString);
    }
    /**
     * Send a completion commit request.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @param coordinatorURI The coordinator URI.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompletionCommit(final MAP map, final String coordinatorURI)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, completionCommitAction);
        port.completionCommit(coordinatorURI);
    }

    /**
     * Send a completion rollback request.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @param coordinatorURI The coordinator URI.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompletionRollback(final MAP map, final String coordinatorURI)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, completionRollbackAction);
        port.completionRollback(coordinatorURI);
    }

    /**
     * Send a commit request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCommit(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, commitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.commit();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a rollback request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRollback(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, rollbackAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.rollback();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a phase2Rollback request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendPhase2Rollback(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, phase2RollbackAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.phase2Rollback();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a readonly request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendReadonly(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, readonlyAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.readonly();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a volatileAndDurable request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendVolatileAndDurable(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, volatileAndDurableAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.volatileAndDurable();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send an earlyReadonly request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendEarlyReadonly(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, earlyReadonlyAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.earlyReadonly();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a earlyAborted request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendEarlyAborted(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, earlyAbortedAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.earlyAborted();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a replayCommit request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendReplayCommit(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, replayCommitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.replayCommit();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a retryPreparedCommit request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRetryPreparedCommit(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, retryPreparedCommitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.retryPreparedCommit();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a retryPreparedAbort request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRetryPreparedAbort(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, retryPreparedAbortAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.retryPreparedAbort();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a retryCommit request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws IOException for any transport errors.
     */
    public void sendRetryCommit(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, retryCommitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.retryCommit();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a preparedAfterTimeout request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendPreparedAfterTimeout(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, preparedAfterTimeoutAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.preparedAfterTimeout();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Send a lostCommitted request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendLostCommitted(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
        map.setReplyTo(initiator) ;
        ParticipantPortType port = ATInteropClient.getParticipantPort(map, lostCommittedAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try
        {
            port.lostCommitted();
        }
        finally
        {
            CoordinationContextManager.setThreadContext(null) ;
        }
    }

    /**
     * Get the Interop client singleton.
     * @return The Interop client singleton.
     */
    public static AsyncParticipantClient getClient()
    {
        return CLIENT ;
    }
}
