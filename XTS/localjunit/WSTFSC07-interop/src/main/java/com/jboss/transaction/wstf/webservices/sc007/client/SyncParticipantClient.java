/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.wstf.webservices.sc007.client;

import java.io.IOException;

import com.arjuna.webservices.*;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.jboss.transaction.wstf.webservices.sc007.InteropConstants;

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