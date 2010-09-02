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

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationCoordinatorPortType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import org.jboss.wsf.common.addressing.MAPEndpoint;
import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAPBuilderFactory;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;

/**
 * The Client side of the Terminator Participant.
 * @author kevin
 */
public class TerminationCoordinatorClient
{
    /**
     * The client singleton.
     */
    private static final TerminationCoordinatorClient CLIENT = new TerminationCoordinatorClient() ;

    /**
     * The complete action.
     */
    private String completeAction = null;
    /**
     * The close action.
     */
    private String closeAction = null;
    /**
     * The cancel action.
     */
    private String cancelAction = null;

    /**
     * The participant URI for replies.
     */
    private MAPEndpoint terminationParticipant ;

    /**
     * The participant URI for secure replies.
     */
    private MAPEndpoint secureTerminationParticipant ;


    /**
     * Construct the terminator participant client.
     */
    private TerminationCoordinatorClient()
    {
        MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
        completeAction = ArjunaTXConstants.WSARJTX_ACTION_COMPLETE;
        closeAction = ArjunaTXConstants.WSARJTX_ACTION_CLOSE;
        cancelAction = ArjunaTXConstants.WSARJTX_ACTION_CANCEL;
        final String terminationParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME, false);
        final String secureTerminationParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME, true);

        terminationParticipant = builder.newEndpoint(terminationParticipantURIString);
        secureTerminationParticipant = builder.newEndpoint(secureTerminationParticipantURIString);
    }

    /**
     * Send a complete request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendComplete(final W3CEndpointReference coordinator, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(coordinator);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        final TerminationCoordinatorPortType port = getPort(coordinator, map, identifier, completeAction);
        final NotificationType complete = new NotificationType();

        port.completeOperation(complete);
    }

    /**
     * Send a close request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClose(final W3CEndpointReference coordinator, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(coordinator);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        final TerminationCoordinatorPortType port = getPort(coordinator, map, identifier, closeAction);
        final NotificationType close = new NotificationType();

        port.closeOperation(close);
    }

    /**
     * Send a cancel request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancel(final W3CEndpointReference coordinator, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint participant = getParticipant(coordinator);
        AddressingHelper.installFromFaultTo(map, participant, identifier);
        final TerminationCoordinatorPortType port = getPort(coordinator, map, identifier, cancelAction);
        final NotificationType cancel = new NotificationType();

        port.cancelOperation(cancel);
    }

    /**
     * return a participant endpoint appropriate to the type of coordinator
     * @param coordinator
     * @return either the secure terminaton coordinator endpoint or the non-secure endpoint
     */
    MAPEndpoint getParticipant(W3CEndpointReference coordinator)
    {
        NativeEndpointReference nativeRef = EndpointHelper.transform(NativeEndpointReference.class, coordinator);
        String address = nativeRef.getAddress();
        if (address.startsWith("https")) {
            return secureTerminationParticipant;
        } else {
            return terminationParticipant;
        }
    }

    /**
     * Get the Terminator Coordinator client singleton.
     * @return The Terminator Coordinator client singleton.
     */
    public static TerminationCoordinatorClient getClient()
    {
        return CLIENT ;
    }

    private TerminationCoordinatorPortType getPort(final W3CEndpointReference endpoint,
                                                   final MAP map,
                                                   final InstanceIdentifier identifier,
                                                   final String action)
    {
        // we only need the message id from the addressing properties as the address is already wrapped up
        // in the ednpoint reference. also the identifier should already be installed in the endpoint
        // reference as a reference parameter so we don't need that either
        AddressingHelper.installNoneReplyTo(map);
        return WSARJTXClient.getTerminationCoordinatorPort(endpoint, action, map);
    }
}