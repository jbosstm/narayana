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
package com.jboss.transaction.txinterop.webservices.bainterop.client;

import java.io.IOException;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilderFactory;
import com.jboss.transaction.txinterop.webservices.CoordinationContextManager;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;
import com.jboss.transaction.txinterop.webservices.bainterop.generated.ParticipantPortType;

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
     * The cancel action.
     */
    private static final String cancelAction = BAInteropConstants.INTEROP_ACTION_CANCEL ;
    /**
     * The exit action.
     */
    private static final String exitAction = BAInteropConstants.INTEROP_ACTION_EXIT ;
    /**
     * The fail action.
     */
    private static final String failAction = BAInteropConstants.INTEROP_ACTION_FAIL ;
    /**
     * The cannot complete action.
     */
    private static final String cannotCompleteAction = BAInteropConstants.INTEROP_ACTION_CANNOT_COMPLETE ;
    /**
     * The participant complete close action.
     */
    private static final String participantCompleteCloseAction = BAInteropConstants.INTEROP_ACTION_PARTICIPANT_COMPLETE_CLOSE ;
    /**
     * The coordinator complete close action.
     */
    private static final String coordinatorCompleteCloseAction = BAInteropConstants.INTEROP_ACTION_COORDINATOR_COMPLETE_CLOSE ;
    /**
     * The unsolicited complete action.
     */
    private static final String unsolicitedCompleteAction = BAInteropConstants.INTEROP_ACTION_UNSOLICITED_COMPLETE ;
    /**
     * The compensate action.
     */
    private static final String compensateAction = BAInteropConstants.INTEROP_ACTION_COMPENSATE ;
    /**
     * The compensation fail action.
     */
    private static final String participantCompensationFailAction = BAInteropConstants.INTEROP_ACTION_COMPENSATION_FAIL ;
    /**
     * The participant cancel completed race action.
     */
    private static final String participantCancelCompletedRaceAction = BAInteropConstants.INTEROP_ACTION_PARTICIPANT_CANCEL_COMPLETED_RACE ;
    /**
     * The message loss and recovery action.
     */
    private static final String messageLossAndRecoveryAction = BAInteropConstants.INTEROP_ACTION_MESSAGE_LOSS_AND_RECOVERY ;
    /**
     * The mixed outcome action.
     */
    private static final String mixedOutcomeAction = BAInteropConstants.INTEROP_ACTION_MIXED_OUTCOME ;
    
    /**
     * The initiator URI for replies.
     */
    private MAPEndpoint initiator = null;
    
    /**
     * Construct the interop synch client.
     */
    private AsyncParticipantClient()
    {
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;
        
        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add coordination context
        // CoordinationContextPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;
        
        // soapService = new SoapService(handlerRegistry) ;
        final String initiatorURIString = ServiceRegistry.getRegistry().getServiceURI(BAInteropConstants.SERVICE_INITIATOR) ;
        MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
        initiator = builder.newEndpoint(initiatorURIString);
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, cancelAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.cancel();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, exitAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.exit();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, failAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.fail();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, cannotCompleteAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.cannotComplete();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, participantCompleteCloseAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.participantCompleteClose();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, coordinatorCompleteCloseAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.coordinatorCompleteClose();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, unsolicitedCompleteAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.unsolicitedComplete();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, compensateAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.compensate();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, participantCompensationFailAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.compensationFail();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, participantCancelCompletedRaceAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.participantCancelCompletedRace();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, messageLossAndRecoveryAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.messageLossAndRecovery();
        } finally {
            CoordinationContextManager.setThreadContext(null) ;
        }
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
        map.setReplyTo(initiator) ;
        ParticipantPortType port = BAInteropClient.getParticipantPort(map, mixedOutcomeAction);
        CoordinationContextManager.setThreadContext(coordinationContext) ;
        try {
            port.mixedOutcome();
        } finally {
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
