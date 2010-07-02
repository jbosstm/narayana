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
package com.arjuna.webservices11.wsba.client;

import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices11.wsba.client.WSBAClient;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsaddr.NativeEndpointReference;
import com.arjuna.webservices11.wsaddr.EndpointHelper;
import org.jboss.jbossts.xts.soapfault.Fault;
import org.jboss.wsf.common.addressing.MAPEndpoint;
import org.oasis_open.docs.ws_tx.wsba._2006._06.BusinessAgreementWithCoordinatorCompletionParticipantPortType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.NotificationType;
import org.oasis_open.docs.ws_tx.wsba._2006._06.StatusType;
import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAPBuilderFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;

/**
 * The Client side of the Coordinator Participant Coordinator.
 * @author kevin
 */
public class CoordinatorCompletionParticipantClient
{
    /**
     * The client singleton.
     */
    private static final CoordinatorCompletionParticipantClient CLIENT = new CoordinatorCompletionParticipantClient() ;

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
     * The compensat action.
     */
    private String compensateAction = null;
    /**
     * The faulted action.
     */
    private String failedAction = null;
    /**
     * The exited action.
     */
    private String exitedAction = null;
    /**
     * The not completed action.
     */
    private String notCompletedAction = null;
    /**
     * The get status action.
     */
    private String getStatusAction = null;
    /**
     * The status action.
     */
    private String statusAction = null;

    /**
     * The coordinator completion coordinator URI for replies.
     */
    private MAPEndpoint coordinatorCompletionCoordinator = null;

    /**
     * The coordinator completion coordinator URI for secure replies.
     */
    private MAPEndpoint secureCoordinatorCompletionCoordinator = null;

    /**
     * Construct the participant completion coordinator client.
     */
    private CoordinatorCompletionParticipantClient()
    {
        final MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();
        completeAction = BusinessActivityConstants.WSBA_ACTION_COMPLETE;
        closeAction = BusinessActivityConstants.WSBA_ACTION_CLOSE;
        cancelAction = BusinessActivityConstants.WSBA_ACTION_CANCEL;
        compensateAction = BusinessActivityConstants.WSBA_ACTION_COMPENSATE;
        failedAction = BusinessActivityConstants.WSBA_ACTION_FAILED;
        exitedAction = BusinessActivityConstants.WSBA_ACTION_EXITED;
        notCompletedAction = BusinessActivityConstants.WSBA_ACTION_NOT_COMPLETED;
        getStatusAction = BusinessActivityConstants.WSBA_ACTION_GET_STATUS;
        statusAction = BusinessActivityConstants.WSBA_ACTION_STATUS;

        final String coordinatorCompletionCoordinatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_NAME, false) ;
        final String secureCoordinatorCompletionCoordinatorURIString =
            ServiceRegistry.getRegistry().getServiceURI(BusinessActivityConstants.COORDINATOR_COMPLETION_COORDINATOR_SERVICE_NAME, true) ;
        coordinatorCompletionCoordinator = builder.newEndpoint(coordinatorCompletionCoordinatorURIString);
        secureCoordinatorCompletionCoordinator = builder.newEndpoint(secureCoordinatorCompletionCoordinatorURIString);
    }

    /**
     * Send a complete request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendComplete(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, completeAction);
        NotificationType complete = new NotificationType();

        port.completeOperation(complete);
    }

    /**
     * Send a close request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendClose(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, closeAction);
        NotificationType close = new NotificationType();

        port.closeOperation(close);
    }

    /**
     * Send a cancel request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCancel(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, cancelAction);
        NotificationType cancel = new NotificationType();

        port.cancelOperation(cancel);
    }

    /**
     * Send a compensate request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendCompensate(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, compensateAction);
        NotificationType compensate = new NotificationType();

        port.compensateOperation(compensate);
    }

    /**
     * Send a failed request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendFailed(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, failedAction);
        NotificationType failed = new NotificationType();

        port.failedOperation(failed);
    }

    /**
     * Send an exited request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendExited(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, exitedAction);
        NotificationType exit = new NotificationType();

        port.exitedOperation(exit);
    }

    /**
     * Send an exited request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendNotCompleted(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, notCompletedAction);
        NotificationType notCompleted = new NotificationType();

        port.notCompleted(notCompleted);
    }

    /**
     * Send a get status request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendGetStatus(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, getStatusAction);
        NotificationType getStatus = new NotificationType();

        port.getStatusOperation(getStatus);
    }

    /**
     * Send a status request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public void sendStatus(W3CEndpointReference endpoint, final MAP map, final InstanceIdentifier identifier,
        final QName state)
        throws SoapFault, IOException
    {
        MAPEndpoint coordinator = getCoordinator(endpoint, map);
        AddressingHelper.installFromFaultTo(map, coordinator, identifier);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(endpoint, map, statusAction);
        StatusType status = new StatusType();
        status.setState(state);

        port.statusOperation(status);
    }

    /**
     * send a soap fault
     * @param soapFault the fault to be sent
     * @param participant the endpoint to send the fault to
     * @param map addressing context to be used to send the fault
     * @param faultAction the action to associate with the message
     */
    public void sendSoapFault(SoapFault11 soapFault, W3CEndpointReference participant, MAP map, String faultAction)
            throws SoapFault, IOException
    {
        AddressingHelper.installNoneReplyTo(map);
        map.setAction(faultAction);
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port;
        port = getPort(participant, map, faultAction);
        Fault fault = ((SoapFault11)soapFault).toFault();
        port.soapFault(fault);
    }
    /**
     * return a coordinator endpoint appropriate to the type of participant
     * @param endpoint
     * @return either the secure coordinator endpoint or the non-secure endpoint
     */
    MAPEndpoint getCoordinator(W3CEndpointReference endpoint, MAP map)
    {
        String address;
        if (endpoint != null) {
            NativeEndpointReference nativeRef = EndpointHelper.transform(NativeEndpointReference.class, endpoint);
            address = nativeRef.getAddress();
        } else {
            address = map.getTo();
        }

        if (address.startsWith("https")) {
            return secureCoordinatorCompletionCoordinator;
        } else {
            return coordinatorCompletionCoordinator;
        }
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CoordinatorCompletionParticipantClient getClient()
    {
        return CLIENT;
    }

    /**
     * obtain a port from the participant endpoint configured with the instance identifier handler and the supplied
     * addressing properties supplemented with the given action
     * @param participant
     * @param map
     * @param action
     * @return
     */
    private BusinessAgreementWithCoordinatorCompletionParticipantPortType
    getPort(final W3CEndpointReference participant, final MAP map, final String action)
    {
        AddressingHelper.installNoneReplyTo(map);
        if (participant != null) {
            return WSBAClient.getCoordinatorCompletionParticipantPort(participant, action, map);
        } else {
            return WSBAClient.getCoordinatorCompletionParticipantPort(action, map);
        }
    }
}