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

import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.wsba.processors.CoordinatorCompletionParticipantProcessor;
import com.arjuna.webservices11.wsba.processors.ParticipantCompletionParticipantProcessor;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.CoordinationContextType;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.wsc11.ActivationCoordinator;
import com.arjuna.wsc11.RegistrationCoordinator;
import com.arjuna.wsc11.messaging.MessageId;
import com.arjuna.wst.BusinessActivityTerminator;
import com.arjuna.wst.BusinessAgreementWithCoordinatorCompletionParticipant;
import com.arjuna.wst.BusinessAgreementWithParticipantCompletionParticipant;
import com.arjuna.wst11.messaging.engines.CoordinatorCompletionParticipantEngine;
import com.arjuna.wst11.messaging.engines.ParticipantCompletionParticipantEngine;
import com.arjuna.wst11.stub.BusinessActivityTerminatorStub;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;

/**
 * Utility methods
 */
public class BAInteropUtil
{
    /**
     * Get an atomic outcome coordination context.
     * @param coordinatorURI The coordinator URI.
     * @return The coordination context.
     * @throws Exception for errors.
     */
    public static CoordinationContextType createAtomicOutcomeCoordinationContext(final String coordinatorURI)
        throws Exception
    {
        return createCoordinationContext(coordinatorURI, BusinessActivityConstants.WSBA_PROTOCOL_ATOMIC_OUTCOME) ;
    }
    
    /**
     * Get a mixed outcome coordination context.
     * @param coordinatorURI The coordinator URI.
     * @return The coordination context.
     * @throws Exception for errors.
     */
    public static CoordinationContextType createMixedOutcomeCoordinationContext(final String coordinatorURI)
        throws Exception
    {
        return createCoordinationContext(coordinatorURI, BusinessActivityConstants.WSBA_PROTOCOL_MIXED_OUTCOME) ;
    }
    
    /**
     * Get a coordination context for the specified protocol.
     * @param coordinatorURI The coordinator URI.
     * @param protocol The coordination protocol.
     * @return The coordination context.
     * @throws Exception for errors.
     */
    private static CoordinationContextType createCoordinationContext(final String coordinatorURI, final String protocol)
    	throws Exception
    {
        return ActivationCoordinator.createCoordinationContext(coordinatorURI, MessageId.getMessageId(), protocol, null, null) ;
    }

    /**
     * Register terminator.
     * @param context The coordination context.
     * @param id The id of the terminator.
     * @return The endpoint for communicating with the coordinator.
     * @throws Exception for errors.
     */
    public static BusinessActivityTerminator registerTerminator(final CoordinationContextType context, final String id)
        throws Exception
    {
        final W3CEndpointReference completionCoordinator = register(context, getTerminatorParticipant(id), ArjunaTXConstants.WSARJTX_PROTOCOL_TERMINATION) ;
        return new BusinessActivityTerminatorStub(id, completionCoordinator);
    }
    
    /**
     * Register a participant completion participant in the specified coordination context.
     * @param context The coordination context.
     * @param participant The durable 2PC participant
     * @param id The participant id.
     * @return The participant engine.
     * @throws Exception for errors.
     */
    public static ParticipantCompletionParticipantEngine registerParticipantCompletion(final CoordinationContextType context,
	final BusinessAgreementWithParticipantCompletionParticipant participant, final String id)
        throws Exception
    {
	final W3CEndpointReference coordinator = RegistrationCoordinator.register(context, MessageId.getMessageId(),
            getParticipantCompletionParticipant(id), BusinessActivityConstants.WSBA_SUB_PROTOCOL_PARTICIPANT_COMPLETION) ;
        final ParticipantCompletionParticipantEngine engine = new ParticipantCompletionParticipantEngine(id, coordinator, participant) ;
        ParticipantCompletionParticipantProcessor.getProcessor().activateParticipant(engine, id) ;
        return engine ;
    }
    
    /**
     * Register a coordinator completion participant in the specified coordination context.
     * @param context The coordination context.
     * @param participant The durable 2PC participant
     * @param id The participant id.
     * @return The participant engine.
     * @throws Exception for errors.
     */
    public static CoordinatorCompletionParticipantEngine registerCoordinatorCompletion(final CoordinationContextType context,
	final BusinessAgreementWithCoordinatorCompletionParticipant participant, final String id)
        throws Exception
    {
	final W3CEndpointReference coordinator = RegistrationCoordinator.register(context, MessageId.getMessageId(),
            getCoordinatorCompletionParticipant(id), BusinessActivityConstants.WSBA_SUB_PROTOCOL_COORDINATOR_COMPLETION) ;
        final CoordinatorCompletionParticipantEngine engine = new CoordinatorCompletionParticipantEngine(id, coordinator, participant) ;
        CoordinatorCompletionParticipantProcessor.getProcessor().activateParticipant(engine, id) ;
        return engine ;
    }
    
    /**
     * Register for a sub protocol.
     * @param context The coordination context.
     * @param participant The participant EPR.
     * @param protocol The protocol.
     * @return The coordinator endpoint.
     * @throws Exception for errors.
     */
    private static W3CEndpointReference register(final CoordinationContextType context, final W3CEndpointReference participant, final String protocol)
        throws Exception
    {
        return RegistrationCoordinator.register(context, MessageId.getMessageId(), participant, protocol) ;
    }
    
    /**
     * Get the endpoint reference for a terminator participant.
     * @param id The participant id.
     * @return The endpoint reference.
     */
    private static W3CEndpointReference getTerminatorParticipant(final String id)
    {
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        final String serviceURI = serviceRegistry.getServiceURI(ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME) ;
        final W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_QNAME);
        builder.endpointName(ArjunaTX11Constants.TERMINATION_PARTICIPANT_PORT_QNAME);
        builder.address(serviceURI);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, id) ;
        return builder.build();
    }
    
    /**
     * Get the endpoint reference for a participant completion participant.
     * @param id The participant id.
     * @return The endpoint reference.
     */
    private static W3CEndpointReference getParticipantCompletionParticipant(final String id)
    {
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        final String serviceURI = serviceRegistry.getServiceURI(BusinessActivityConstants.PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_NAME) ;
        final W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(BusinessActivityConstants.PARTICIPANT_COMPLETION_PARTICIPANT_SERVICE_QNAME);
        builder.endpointName(BusinessActivityConstants.PARTICIPANT_COMPLETION_PARTICIPANT_PORT_QNAME);
        builder.address(serviceURI);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, id) ;
        return builder.build();
    }
    
    /**
     * Get the endpoint reference for a coordinator completion participant.
     * @param id The participant id.
     * @return The endpoint reference.
     */
    private static W3CEndpointReference getCoordinatorCompletionParticipant(final String id)
    {
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        final String serviceURI = serviceRegistry.getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_NAME) ;
        final W3CEndpointReferenceBuilder builder = new W3CEndpointReferenceBuilder();
        builder.serviceName(BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_SERVICE_QNAME);
        builder.endpointName(BusinessActivityConstants.COORDINATOR_COMPLETION_PARTICIPANT_PORT_QNAME);
        builder.address(serviceURI);
        InstanceIdentifier.setEndpointInstanceIdentifier(builder, id) ;
        return builder.build();
    }
}
