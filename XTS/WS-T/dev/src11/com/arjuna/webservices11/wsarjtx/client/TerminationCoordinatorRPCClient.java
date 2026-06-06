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
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationCoordinatorRPCPortType;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.wsarjtx.ArjunaTXConstants;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPBuilderFactory;
import org.jboss.ws.api.addressing.MAPEndpoint;

import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;

/**
 * The client side which makes synchronous requests to the BA termination coordinator using an RPC MEP.
 * @author kevin
 */
public class TerminationCoordinatorRPCClient
{
    /**
     * The client singleton.
     */
    private static final TerminationCoordinatorRPCClient CLIENT = new TerminationCoordinatorRPCClient() ;

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
     * Construct the terminator participant client.
     */
    private TerminationCoordinatorRPCClient()
    {
        MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
        completeAction = ArjunaTXConstants.WSARJTX_ACTION_COMPLETE;
        closeAction = ArjunaTXConstants.WSARJTX_ACTION_CLOSE;
        cancelAction = ArjunaTXConstants.WSARJTX_ACTION_CANCEL;
    }

    /**
     * Send a complete request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendComplete(final W3CEndpointReference coordinator, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault11, IOException
    {
        final TerminationCoordinatorRPCPortType port = getPort(coordinator, map, completeAction);
        final NotificationType complete = new NotificationType();

        try {
            port.completeOperation(complete);
        } catch (SOAPFaultException sfe) {
            throw SoapFault11.create(sfe);
        }
    }

    /**
     * Send a close request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClose(final W3CEndpointReference coordinator, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault11, IOException
    {
        final TerminationCoordinatorRPCPortType port = getPort(coordinator, map, closeAction);
        final NotificationType close = new NotificationType();

        try {
            port.closeOperation(close);
        } catch (SOAPFaultException sfe) {
            throw SoapFault11.create(sfe);
        }
    }

    /**
     * Send a cancel request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancel(final W3CEndpointReference coordinator, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault11, IOException
    {
        final TerminationCoordinatorRPCPortType port = getPort(coordinator, map, cancelAction);
        final NotificationType cancel = new NotificationType();

        try {
            port.cancelOperation(cancel);
        } catch (SOAPFaultException sfe) {
            throw SoapFault11.create(sfe);
        }
    }

    /**
     * Get the Terminator Coordinator client singleton.
     * @return The Terminator Coordinator client singleton.
     */
    public static TerminationCoordinatorRPCClient getClient()
    {
        return CLIENT ;
    }

    private TerminationCoordinatorRPCPortType getPort(final W3CEndpointReference endpoint,
                                                      final MAP map,
                                                      final String action)
    {
        return WSARJTXClient.getTerminationCoordinatorRPCPort(endpoint,  action, map);
    }
}