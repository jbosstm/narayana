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

import com.arjuna.webservices.*;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;

/**
 * The participant client.
 * @author kevin
 */
public class SyncParticipantClient
{
    /**
     * The client singleton.
     */
    private static final SyncParticipantClient CLIENT = new SyncParticipantClient() ;
    
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
     * Construct the interop synch client.
     */
    private SyncParticipantClient()
    {
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
    }

    /**
     * Send a retryCommit request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendRetryCommit(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
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
    }
    
    /**
     * Get the Interop client singleton.
     * @return The Interop client singleton.
     */
    public static SyncParticipantClient getClient()
    {
        return CLIENT ;
    }
}