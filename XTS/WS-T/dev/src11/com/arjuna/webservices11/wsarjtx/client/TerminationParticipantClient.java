/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices11.wsarjtx.client;

import com.arjuna.schemas.ws._2005._10.wsarjtx.ExceptionType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationParticipantPortType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;

import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReferenceBuilder;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;

/**
 * The Client side of the Terminator Coordinator.
 * @author kevin
 */
public class TerminationParticipantClient
{
    /**
     * The client singleton.
     */
    private static final TerminationParticipantClient CLIENT = new TerminationParticipantClient() ;

    /**
     * The completed action.
     */
    private AttributedURI completedAction = null;
    /**
     * The closed action.
     */
    private AttributedURI closedAction = null;
    /**
     * The cancelled action.
     */
    private AttributedURI cancelledAction = null;
    /**
     * The faulted action.
     */
    private AttributedURI faultedAction = null;
    /**
     * The SOAP fault action.
     */
    private AttributedURI soapFaultAction = null;

    /**
     * The participant URI for replies.
     */
    private EndpointReference terminationCoordinator ;

    /**
     * Construct the terminator coordinator client.
     */
    private TerminationParticipantClient()
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            completedAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_COMPLETED);
            closedAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_CLOSED);
            cancelledAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_CANCELLED);
            faultedAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_FAULTED);
            soapFaultAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_SOAP_FAULT);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;
        final String terminationCoordinatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(ArjunaTX11Constants.TERMINATION_COORDINATOR_SERVICE_NAME);
        try {
            URI terminationCoordinatorURI = new URI(terminationCoordinatorURIString);
            terminationCoordinator = builder.newEndpointReference(terminationCoordinatorURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
    }

    /**
     * Send a completed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompleted(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, terminationCoordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, addressingProperties, identifier, completedAction);
        final NotificationType completed = new NotificationType();

        port.completedOperation(completed);
    }

    /**
     * Send a closed request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClosed(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, terminationCoordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, addressingProperties, identifier, closedAction);
        final NotificationType closed = new NotificationType();

        port.closedOperation(closed);
    }

    /**
     * Send a cancelled request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancelled(final W3CEndpointReference participant,final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, terminationCoordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, addressingProperties, identifier, cancelledAction);
        final NotificationType cancelled = new NotificationType();

        port.cancelledOperation(cancelled);
    }

    /**
     * Send a faulted request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendFaulted(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromReplyTo(addressingProperties, terminationCoordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, addressingProperties, identifier, faultedAction);
        final NotificationType faulted = new NotificationType();

        port.faultedOperation(faulted);
    }

    /**
     * Send a fault.
     * @param participant the endpoint reference for the participant to notify
     * @param addressingProperties The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna  instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final W3CEndpointReference participant, final AddressingProperties addressingProperties, final SoapFault soapFault, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFrom(addressingProperties, terminationCoordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, addressingProperties, identifier, soapFaultAction);
        final ExceptionType fault = new ExceptionType();
        // we pass the fault type, reason and subcode. we cannot pass the detail and header elements as they are
        // built from Kev's element types rather than dom element types. this is all we need anyway since we only
        // see faults containing those values
        fault.setSoapFaultType(soapFault.getSoapFaultType().getValue());
        fault.setReason(soapFault.getReason());
        fault.setSubCode(soapFault.getSubcode());

        port.faultOperation(fault);
    }

    /**
     * Send a fault.
     * @param addressingProperties The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna  instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final SoapFault soapFault, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final TerminationParticipantPortType port = getPort(addressingProperties, identifier, soapFaultAction);
        final ExceptionType fault = new ExceptionType();
        // we pass the fault type, reason and subcode. we cannot pass the detail and header elements as they are
        // built from Kev's element types rather than dom element types. this is all we need anyway since we only
        // see faults containing those values
        fault.setSoapFaultType(soapFault.getSoapFaultType().getValue());
        fault.setReason(soapFault.getReason());
        fault.setSubCode(soapFault.getSubcode());

        port.faultOperation(fault);
    }

    /**
     * Get the Terminator Coordinator client singleton.
     * @return The Terminator Coordinator client singleton.
     */
    public static TerminationParticipantClient getClient()
    {
        return CLIENT ;
    }

    private TerminationParticipantPortType getPort(final W3CEndpointReference endpoint,
                                                   final AddressingProperties addressingProperties,
                                                   final InstanceIdentifier identifier,
                                                   final AttributedURI action)
    {
        // we only need the message id from the addressing properties as the address is already wrapped up
        // in the ednpoint reference. also the identifier should already be installed in the endpoint
        // reference as a reference parameter so we don't need that either
        return WSARJTXClient.getTerminationParticipantPort(endpoint, action, addressingProperties);
    }

    private TerminationParticipantPortType getPort(final AddressingProperties addressingProperties,
                                                   final InstanceIdentifier identifier,
                                                   final AttributedURI action)
    {
        // create a port specific to the incoming addressing properties
        return WSARJTXClient.getTerminationParticipantPort(identifier, action, addressingProperties);
    }
}