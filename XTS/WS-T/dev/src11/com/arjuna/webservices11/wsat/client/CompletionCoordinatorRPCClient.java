package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices.SoapFault;
import com.arjuna.webservices.SoapFaultType;
import com.arjuna.webservices.util.InvalidEnumerationException;
import com.arjuna.webservices11.SoapFault11;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import org.jboss.wsf.common.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.CompletionCoordinatorRPCPortType;
import org.oasis_open.docs.ws_tx.wsat._2006._06.Notification;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;
import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.soap.SOAPFaultException;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.IOException;
import java.util.Locale;

/**
 * The Client side of the Completion Coordinator.
 * @author kevin
 */
public class CompletionCoordinatorRPCClient
{
    /**
     * The client singleton.
     */
    private static final CompletionCoordinatorRPCClient CLIENT = new CompletionCoordinatorRPCClient() ;

    /**
     * The commit action.
     */
    private String commitAction = null;
    /**
     * The rollback action.
     */
    private String rollbackAction = null;

    /**
     * Construct the completion coordinator client.
     */
    private CompletionCoordinatorRPCClient()
    {
            commitAction = AtomicTransactionConstants.WSAT_ACTION_COMMIT;
            rollbackAction = AtomicTransactionConstants.WSAT_ACTION_ROLLBACK;
        // final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

        // Add WS-Addressing
        // AddressingPolicy.register(handlerRegistry) ;
        // Add client policies
        // ClientPolicy.register(handlerRegistry) ;
    }

    /**
     * Send a commit request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public boolean sendCommit(final W3CEndpointReference endpoint, final MAP map)
        throws SoapFault, IOException
    {
        CompletionCoordinatorRPCPortType port = getPort(endpoint, map, commitAction);
        Notification commit = new Notification();

        try {
            return port.commitOperation(commit);
        } catch (SOAPFaultException sfe) {
            throw SoapFault11.create(sfe);
        }
    }

    /**
     * Send a rollback request.
     * @param map addressing context initialised with to and message ID.
     * @param identifier The identifier of the initiator.
     * @throws com.arjuna.webservices.SoapFault For any errors.
     * @throws java.io.IOException for any transport errors.
     */
    public boolean sendRollback(final W3CEndpointReference endpoint, final MAP map)
        throws SoapFault, IOException
    {
        CompletionCoordinatorRPCPortType port = getPort(endpoint, map, rollbackAction);
        Notification rollback = new Notification();

        try {
            return port.rollbackOperation(rollback);
        } catch (SOAPFaultException sfe) {
            throw SoapFault11.create(sfe);
        }
    }

    /**
     * Get the Completion Coordinator client singleton.
     * @return The Completion Coordinator client singleton.
     */
    public static CompletionCoordinatorRPCClient getClient()
    {
        return CLIENT;
    }

    /**
     * obtain a port from the completion coordinator endpoint configured with the instance identifier handler and the supplied
     * addressing properties supplemented with the given action
     * @param endpoint
     * @param map
     * @param action
     * @return
     */
    private CompletionCoordinatorRPCPortType getPort(final W3CEndpointReference endpoint,
                                                  final MAP map,
                                                  final String action)
    {
        AddressingHelper.installNoneReplyTo(map);
        return WSATClient.getCompletionCoordinatorRPCPort(endpoint, action, map);
    }
}
