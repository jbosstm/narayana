package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices11.wsarj.handler.InstanceIdentifierHandler;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wsat._2006._06.*;
import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Oct 7, 2007
 * Time: 3:14:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class WSATClient
{
    // TODO -- do we really need a thread local here or can we just use one service?
    /**
     *  thread local which maintains a per thread coordinator service instance
     */
    private static ThreadLocal<CoordinatorService> coordinatorService = new ThreadLocal<CoordinatorService>();

    /**
     *  thread local which maintains a per thread participant service instance
     */
    private static ThreadLocal<ParticipantService> participantService = new ThreadLocal<ParticipantService>();

    /**
     *  thread local which maintains a per thread completion coordinator service instance
     */
    private static ThreadLocal<CompletionCoordinatorService> completionCoordinatorService = new ThreadLocal<CompletionCoordinatorService>();

    /**
     *  thread local which maintains a per thread completion initiator service instance
     */
    private static ThreadLocal<CompletionInitiatorService> completionInitiatorService = new ThreadLocal<CompletionInitiatorService>();

    /**
     *  builder used to construct addressing info for calls
     */
    private static AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();

    /**
     * fetch a coordinator service unique to the current thread
     * @return
     */
    private static synchronized CoordinatorService getCoordinatorService()
    {
        if (coordinatorService.get() == null) {
            coordinatorService.set(new CoordinatorService());
        }
        return coordinatorService.get();
    }

    /**
     * fetch a participant service unique to the current thread
     * @return
     */
    private static synchronized ParticipantService getParticipantService()
    {
        if (participantService.get() == null) {
            participantService.set(new ParticipantService());
        }
        return participantService.get();
    }

    /**
     * fetch a completion coordinator service unique to the current thread
     * @return
     */
    private static synchronized CompletionCoordinatorService getCompletionCoordinatorService()
    {
        if (completionCoordinatorService.get() == null) {
            completionCoordinatorService.set(new CompletionCoordinatorService());
        }
        return completionCoordinatorService.get();
    }

    /**
     * fetch a completion initiator service unique to the current thread
     * @return
     */
    private static synchronized CompletionInitiatorService getCompletionInitiatorService()
    {
        if (completionInitiatorService.get() == null) {
            completionInitiatorService.set(new CompletionInitiatorService());
        }
        return completionInitiatorService.get();
    }

    public static CoordinatorPortType getCoordinatorPort(W3CEndpointReference endpointReference,
                                                         AttributedURI action,
                                                         AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        CoordinatorService service = getCoordinatorService();
        // CoordinatorPortType port = service.getPort(endpointReference, CoordinatorPortType.class, new AddressingFeature(true, true));
        CoordinatorPortType port = service.getPort(endpointReference, CoordinatorPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we need to configure an instance identifier handler for this port to pass the tx context
         */
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        customHandlerChain.add(new InstanceIdentifierHandler());
        /*
         * we also have to add the JaxWS WSAddressingClientHandler because we cannoy specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
        // ok, JBossWS native has hacked this by pulling the address and reference parameters out of the endpoint
        // and storing them in an AddressingProperties instance hung off the context under CLIENT_ADDRESSING_PROPERTIES_OUTBOUND.
        // we still need to set the action and message id and possibly relatesTo -- this is all distinctly non-portable :-/
        // n.b. Metro installs the address in requestContext under ENDPOINT_ADDRESS_PROPERTY. it also seems to ensure
        // that the reference parameters get installed -- but how?

        // the address will have been pulled out of the endpoint by getPort but we still have to set it in the
        // addressing properties along with the action and message id
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        AddressingProperties requestProperties = (AddressingProperties)requestContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        addressingProperties.setAction(action);
        AddressingHelper.installCallerProperties(addressingProperties, requestProperties);
        // we should not need to do this but JBossWS does not pick up the value in the addressing properties
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requestProperties.getTo().getURI().toString());
        return port;
    }

    // don't think we ever need this as we get a registration port from the endpoint ref returned by
    // the activation port request
    public static ParticipantPortType getParticipantPort(W3CEndpointReference endpointReference,
                                                         AttributedURI action,
                                                         AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        ParticipantService service = getParticipantService();
        // ParticipantPortType port = service.getPort(endpointReference, ParticipantPortType.class, new AddressingFeature(true, true));
        ParticipantPortType port = service.getPort(endpointReference, ParticipantPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we need to configure an instance identifier handler for this port to pass the tx context
         * TODO - don't think this is correct here
         */
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        customHandlerChain.add(new InstanceIdentifierHandler());
        /*
         * we also have to add the JaxWS WSAddressingClientHandler because we cannoy specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
        // ok, JBossWS native has hacked this by pulling the address and reference parameters out of the endpoint
        // and storing them in an AddressingProperties instance hung off the context under CLIENT_ADDRESSING_PROPERTIES_OUTBOUND.
        // we still need to set the action and message id and possibly relatesTo -- this is all distinctly non-portable :-/
        // n.b. Metro installs the address in requestContext under ENDPOINT_ADDRESS_PROPERTY. it also seems to ensure
        // that the reference parameters get installed -- but how?

        // the address will have been pulled out of the endpoint by getPort but we still have to set it in the
        // addressing properties along with the action and message id
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        AddressingProperties requestProperties = (AddressingProperties)requestContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        addressingProperties.setAction(action);
        AddressingHelper.installCallerProperties(addressingProperties, requestProperties);
        // we should not need to do this but JBossWS does not pick up the value in the addressing properties
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requestProperties.getTo().getURI().toString());
        return port;
    }

    public static CompletionCoordinatorPortType getCompletionCoordinatorPort(W3CEndpointReference endpointReference,
                                                                             AttributedURI action,
                                                                             AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        CompletionCoordinatorService service = getCompletionCoordinatorService();
        // CompletionCoordinatorPortType port = service.getPort(endpointReference, CompletionCoordinatorPortType.class, new AddressingFeature(true, true));
        CompletionCoordinatorPortType port = service.getPort(endpointReference, CompletionCoordinatorPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we need to configure an instance identifier handler for this port to pass the tx context
         */
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        customHandlerChain.add(new InstanceIdentifierHandler());
        /*
         * we also have to add the JaxWS WSAddressingClientHandler because we cannoy specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
        // ok, JBossWS native has hacked this by pulling the address and reference parameters out of the endpoint
        // and storing them in an AddressingProperties instance hung off the context under CLIENT_ADDRESSING_PROPERTIES_OUTBOUND.
        // we still need to set the action and message id and possibly relatesTo -- this is all distinctly non-portable :-/
        // n.b. Metro installs the address in requestContext under ENDPOINT_ADDRESS_PROPERTY. it also seems to ensure
        // that the reference parameters get installed -- but how?

        // the address will have been pulled out of the endpoint by getPort but we still have to set it in the
        // addressing properties along with the action and message id
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        AddressingProperties requestProperties = (AddressingProperties)requestContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        addressingProperties.setAction(action);
        AddressingHelper.installCallerProperties(addressingProperties, requestProperties);
        // we should not need to do this but JBossWS does not pick up the value in the addressing properties
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requestProperties.getTo().getURI().toString());
        return port;
    }

    public static CompletionInitiatorPortType getCompletionInitiatorPort(W3CEndpointReference endpointReference,
                                                                         AttributedURI action,
                                                                         AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        CompletionInitiatorService service = getCompletionInitiatorService();
        // CompletionInitiatorPortType port = service.getPort(endpointReference, CompletionInitiatorPortType.class, new AddressingFeature(true, true));
        CompletionInitiatorPortType port = service.getPort(endpointReference, CompletionInitiatorPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we need to configure an instance identifier handler for this port to pass the tx context either
         * outgoing or returning.
         * TODO - don't think this is correct here
         */
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        customHandlerChain.add(new InstanceIdentifierHandler());
        /*
         * we also have to add the JaxWS WSAddressingClientHandler because we cannoy specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
        // ok, JBossWS native has hacked this by pulling the address and reference parameters out of the endpoint
        // and storing them in an AddressingProperties instance hung off the context under CLIENT_ADDRESSING_PROPERTIES_OUTBOUND.
        // we still need to set the action and message id and possibly relatesTo -- this is all distinctly non-portable :-/
        // n.b. Metro installs the address in requestContext under ENDPOINT_ADDRESS_PROPERTY. it also seems to ensure
        // that the reference parameters get installed -- but how?

        // the address will have been pulled out of the endpoint by getPort but we still have to set it in the
        // addressing properties along with the action and message id
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        AddressingProperties requestProperties = (AddressingProperties)requestContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        addressingProperties.setAction(action);
        AddressingHelper.installCallerProperties(addressingProperties, requestProperties);
        // we should not need to do this but JBossWS does not pick up the value in the addressing properties
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, requestProperties.getTo().getURI().toString());
        return port;
    }
}