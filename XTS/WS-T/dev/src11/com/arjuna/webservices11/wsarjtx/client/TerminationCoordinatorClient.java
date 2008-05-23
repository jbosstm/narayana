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

import com.arjuna.schemas.ws._2005._10.wsarjtx.NotificationType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationCoordinatorPortType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsaddr.client.SoapFaultClient;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;

import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.EndpointReference;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URI;

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
    private AttributedURI completeAction = null;
    /**
     * The close action.
     */
    private AttributedURI closeAction = null;
    /**
     * The cancel action.
     */
    private AttributedURI cancelAction = null;
    /**
     * The SOAP fault action.
     */
    private AttributedURI faultAction = null;

    /**
     * The participant URI for replies.
     */
    private EndpointReference terminationParticipant ;


    /**
     * Construct the terminator participant client.
     */
    private TerminationCoordinatorClient()
    {
        final AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();
        try {
            completeAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_COMPLETE);
            closeAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_CLOSE);
            cancelAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_CANCEL);
            faultAction = builder.newURI(ArjunaTXConstants.WSARJTX_ACTION_SOAP_FAULT) ;
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
        //final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        //AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        //ClientPolicy.register(handlerRegistry) ;
        final String terminationParticipantURIString =
            ServiceRegistry.getRegistry().getServiceURI(ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME);
        try {
            URI terminationParticipantURI = new URI(terminationParticipantURIString);
            terminationParticipant = builder.newEndpointReference(terminationParticipantURI);
        } catch (URISyntaxException use) {
            // TODO - log fault and throw exception
        }
    }

    /**
     * Send a complete request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendComplete(final W3CEndpointReference coordinator, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, terminationParticipant, identifier);
        final TerminationCoordinatorPortType port = getPort(coordinator, addressingProperties, identifier, completeAction);
        final NotificationType complete = new NotificationType();

        port.completeOperation(complete);
    }

    /**
     * Send a close request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClose(final W3CEndpointReference coordinator, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, terminationParticipant, identifier);
        final TerminationCoordinatorPortType port = getPort(coordinator, addressingProperties, identifier, closeAction);
        final NotificationType close = new NotificationType();

        port.closeOperation(close);
    }

    /**
     * Send a cancel request.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancel(final W3CEndpointReference coordinator, final AddressingProperties addressingProperties, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installFromFaultTo(addressingProperties, terminationParticipant, identifier);
        final TerminationCoordinatorPortType port = getPort(coordinator, addressingProperties, identifier, cancelAction);
        final NotificationType cancel = new NotificationType();

        port.cancelOperation(cancel);
    }

    /**
     * Send a fault.
     * @param addressingProperties addressing context initialised with to and message ID.
     * @param soapFault The SOAP fault.
     * @param identifier The arjuna instance identifier.
     * @throws SoapFault For any errors.
     * @throws IOException for any transport errors.
     */
    public void sendSoapFault(final W3CEndpointReference endpoint,
                              final AddressingProperties addressingProperties,
                              final SoapFault soapFault,
                              final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        AddressingHelper.installNoneReplyTo(addressingProperties);
        // use the SoapFaultService to format a soap fault and send it back to the faultto or from address
        SoapFaultClient.sendSoapFault((SoapFault11)soapFault, endpoint, addressingProperties, faultAction);
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
                                                   final AddressingProperties addressingProperties,
                                                   final InstanceIdentifier identifier,
                                                   final AttributedURI action)
    {
        // we only need the message id from the addressing properties as the address is already wrapped up
        // in the ednpoint reference. also the identifier should already be installed in the endpoint
        // reference as a reference parameter so we don't need that either
        AddressingHelper.installNoneReplyTo(addressingProperties);
        return WSARJTXClient.getTerminationCoordinatorPort(endpoint, action, addressingProperties);
    }
}