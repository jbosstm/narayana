/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.txinterop.webservices.bainterop.client;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;

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
     * Send a cancel request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCancel(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a exit request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendExit(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a fail request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendFail(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a cannot complete request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCannotComplete(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a participant complete close request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendParticipantCompleteClose(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a coordinator complete close request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCoordinatorCompleteClose(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a unsolicited complete request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendUnsolicitedComplete(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a compensate request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompensate(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a compensation fail request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendCompensationFail(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a participant cancel completed race request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendParticipantCancelCompletedRace(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a message loss and recovery request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendMessageLossAndRecovery(final CoordinationContextType coordinationContext, final MAP map)
        throws SoapFault, IOException
    {
    }
    
    /**
     * Send a mixed outcome request.
     * @param coordinationContext The coordination context.
     * @param map The addressing context initialised with to, message ID and relates to.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendMixedOutcome(final CoordinationContextType coordinationContext, final MAP map)
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