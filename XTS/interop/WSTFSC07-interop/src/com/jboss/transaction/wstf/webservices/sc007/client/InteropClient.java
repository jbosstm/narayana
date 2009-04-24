package com.jboss.transaction.wstf.webservices.sc007.client;

import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;

import javax.xml.ws.addressing.*;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URISyntaxException;
import java.net.URI;

import com.arjuna.webservices11.ServiceRegistry;
import com.jboss.transaction.wstf.webservices.sc007.generated.Sc007Service;
import com.jboss.transaction.wstf.webservices.sc007.generated.InitiatorPortType;
import com.jboss.transaction.wstf.webservices.sc007.generated.ParticipantPortType;
import com.jboss.transaction.wstf.webservices.sc007.InteropConstants;
import com.jboss.transaction.wstf.webservices.handlers.CoordinationContextHandler;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Apr 17, 2008
 * Time: 4:18:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class InteropClient {
    // TODO -- do we really need a thread local here or can we just use one service?
    /**
     *  thread local which maintains a per thread Sc007 service instance
     */
    private static ThreadLocal<Sc007Service> sc007Service = new ThreadLocal<Sc007Service>();

    /**
     *  builder used to construct addressing info for calls
     */
    private static AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();

    /**
     * fetch an Sc007 service unique to the current thread
     * @return
     */
    private static synchronized Sc007Service getSc007Service()
    {
        if (sc007Service.get() == null) {
            sc007Service.set(new Sc007Service());
        }
        return sc007Service.get();
    }

    public static InitiatorPortType getInitiatorPort(AddressingProperties addressingProperties,
                                                       String action)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        Sc007Service service = getSc007Service();
        InitiatorPortType port = service.getPort(InitiatorPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        AttributedURI toUri = addressingProperties.getTo();
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannot specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
        bindingProvider.getBinding().setHandlerChain(customHandlerChain);

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
	    // jbossws should do this for us . . .
	    requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, toUri.getURI().toString());
        try {
            addressingProperties.setAction(builder.newURI(action));
            addressingProperties.setFrom(getParticipant());
        } catch (URISyntaxException use) {
            // TODO log this error
        }

        return port;
    }

    // don't think we ever need this as we get a registration port from the endpoint ref returned by
    // the activation port request
    public static ParticipantPortType getParticipantPort(AddressingProperties addressingProperties, String action)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        Sc007Service service = getSc007Service();
        ParticipantPortType port = service.getPort(ParticipantPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        AttributedURI toUri = addressingProperties.getTo();
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        /*
         * we need to add the coordination context handler in the case where we are passing a
         * coordination context via a header element
         */
        customHandlerChain.add(new CoordinationContextHandler());
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannot specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
	    // jbossws should do this for us . . .
	    requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, toUri.getURI().toString());
        try {
            addressingProperties.setAction(builder.newURI(action));
            addressingProperties.setFrom(getInitiator());
        } catch (URISyntaxException use) {
            // TODO log this error
        }

        return port;
    }

    private static EndpointReference initiator;
    private static EndpointReference participant;

    private static synchronized EndpointReference getInitiator()
    {
        if (initiator == null) {
            final String initiatorURIString =
                ServiceRegistry.getRegistry().getServiceURI(InteropConstants.SERVICE_INITIATOR);
            try {
                URI initiatorURI = new URI(initiatorURIString);
                initiator = builder.newEndpointReference(initiatorURI);
            } catch (URISyntaxException use) {
                // TODO - log fault and throw exception
            }
        }
        return initiator;
    }

    private static synchronized EndpointReference getParticipant()
    {
        if (participant == null) {
            final String participantURIString =
                    ServiceRegistry.getRegistry().getServiceURI(InteropConstants.SERVICE_PARTICIPANT);
            try {
                URI participantURI = new URI(participantURIString);
                participant = builder.newEndpointReference(participantURI);
            } catch (URISyntaxException use) {
                // TODO - log fault and throw exception
            }
        }
        return participant;
    }
}
