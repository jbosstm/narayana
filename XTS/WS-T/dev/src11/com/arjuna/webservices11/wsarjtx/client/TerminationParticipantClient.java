/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.webservices11.wsarjtx.client;

import com.arjuna.schemas.ws._2005._10.wsarjtx.ExceptionType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationParticipantPortType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import org.jboss.ws.api.addressing.MAPEndpoint;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;

import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPBuilderFactory;
import org.xmlsoap.schemas.soap.envelope.Fault;

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
    private String completedAction = null;
    /**
     * The closed action.
     */
    private String closedAction = null;
    /**
     * The cancelled action.
     */
    private String cancelledAction = null;
    /**
     * The faulted action.
     */
    private String faultedAction = null;
    /**
     * The SOAP fault action.
     */
    private String soapFaultAction = null;

    /**
     * The participant URI for replies.
     */
    private MAPEndpoint terminationCoordinator ;

    /**
     * The participant URI for securereplies.
     */
    private MAPEndpoint secureTerminationCoordinator ;

    /**
     * Construct the terminator coordinator client.
     */
    private TerminationParticipantClient()
    {
        final MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
        completedAction = ArjunaTXConstants.WSARJTX_ACTION_COMPLETED;
        closedAction = ArjunaTXConstants.WSARJTX_ACTION_CLOSED;
        cancelledAction = ArjunaTXConstants.WSARJTX_ACTION_CANCELLED;
        faultedAction = ArjunaTXConstants.WSARJTX_ACTION_FAULTED;
        soapFaultAction = ArjunaTXConstants.WSARJTX_ACTION_SOAP_FAULT;

        final String terminationCoordinatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(ArjunaTX11Constants.TERMINATION_COORDINATOR_SERVICE_NAME, false);
        final String secureTerminationCoordinatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(ArjunaTX11Constants.TERMINATION_COORDINATOR_SERVICE_NAME, true);
        terminationCoordinator = builder.newEndpoint(terminationCoordinatorURIString);
        secureTerminationCoordinator = builder.newEndpoint(secureTerminationCoordinatorURIString);
    }

    /**
     * Send a completed request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompleted(final W3CEndpointReference participant, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(participant);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, map, identifier, completedAction);
        final NotificationType completed = new NotificationType();

        port.completedOperation(completed);
    }

    /**
     * Send a closed request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClosed(final W3CEndpointReference participant, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(participant);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, map, identifier, closedAction);
        final NotificationType closed = new NotificationType();

        port.closedOperation(closed);
    }

    /**
     * Send a cancelled request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancelled(final W3CEndpointReference participant,final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(participant);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, map, identifier, cancelledAction);
        final NotificationType cancelled = new NotificationType();

        port.cancelledOperation(cancelled);
    }

    /**
     * Send a faulted request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendFaulted(final W3CEndpointReference participant, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(participant);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        final TerminationParticipantPortType port = getPort(participant, map, identifier, faultedAction);
        final NotificationType faulted = new NotificationType();

        port.faultedOperation(faulted);
    }

    /**
     * Send a fault.
     * @param endpoint the endpoint reference to notify
     * @param map The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna  instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final W3CEndpointReference endpoint, final MAP map, final SoapFault soapFault, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        //AddressingHelper.installFrom(map, terminationCoordinator, identifier);
        AddressingHelper.installNoneReplyTo(map);
        final TerminationParticipantPortType port = getPort(endpoint, map, identifier, soapFaultAction);
        SoapFault11 soapFault11 = (SoapFault11)soapFault;
        Fault fault = soapFault11.toFault();
        port.faultOperation(fault);
    }

    /**
     * Send a fault.
     * @param map The addressing context.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna  instance identifier.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendSoapFault(final SoapFault soapFault, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        final TerminationParticipantPortType port = getPort(map, identifier, soapFaultAction);
        SoapFault11 soapFault11 = (SoapFault11)soapFault;
        Fault fault = soapFault11.toFault();
        port.faultOperation(fault);
    }

    /**
     * return a coordinator endpoint appropriate to the type of participant
     * @param participant
     * @return either the secure terminaton participant endpoint or the non-secure endpoint
     */
    MAPEndpoint getCoordinator(W3CEndpointReference participant)
    {
        NativeEndpointReference nativeRef = EndpointHelper.transform(NativeEndpointReference.class, participant);
        String address = nativeRef.getAddress();
        if (address.startsWith("https")) {
            return secureTerminationCoordinator;
        } else {
            return terminationCoordinator;
        }
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
                                                   final MAP map,
                                                   final InstanceIdentifier identifier,
                                                   final String action)
    {
        // we only need the message id from the addressing properties as the address is already wrapped up
        // in the ednpoint reference. also the identifier should already be installed in the endpoint
        // reference as a reference parameter so we don't need that either
        return WSARJTXClient.getTerminationParticipantPort(endpoint, action, map);
    }

    private TerminationParticipantPortType getPort(final MAP map,
                                                   final InstanceIdentifier identifier,
                                                   final String action)
    {
        // create a port specific to the incoming addressing properties
        AddressingHelper.installNoneReplyTo(map);
        return WSARJTXClient.getTerminationParticipantPort(identifier, action, map);
    }
}