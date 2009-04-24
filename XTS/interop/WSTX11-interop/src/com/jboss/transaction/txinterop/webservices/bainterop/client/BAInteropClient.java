package com.jboss.transaction.txinterop.webservices.bainterop.client;

import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;

import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.net.URISyntaxException;

import com.jboss.transaction.txinterop.webservices.bainterop.generated.InitiatorService;
import com.jboss.transaction.txinterop.webservices.bainterop.generated.ParticipantService;
import com.jboss.transaction.txinterop.webservices.bainterop.generated.InitiatorPortType;
import com.jboss.transaction.txinterop.webservices.bainterop.generated.ParticipantPortType;
import com.jboss.transaction.txinterop.webservices.handlers.CoordinationContextHandler;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Apr 17, 2008
 * Time: 4:18:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class BAInteropClient {
    // TODO -- do we really need a thread local here or can we just use one service?
    /**
     *  thread local which maintains a per thread activation service instance
     */
    private static ThreadLocal<InitiatorService> initiatorService = new ThreadLocal<InitiatorService>();

    /**
     *  thread local which maintains a per thread activation service instance
     */
    private static ThreadLocal<ParticipantService> participantService = new ThreadLocal<ParticipantService>();

    /**
     *  builder used to construct addressing info for calls
     */
    private static AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();

    /**
     * fetch a coordinator activation service unique to the current thread
     * @return
     */
    private static synchronized InitiatorService getInitiatorService()
    {
        if (initiatorService.get() == null) {
            initiatorService.set(new InitiatorService());
        }
        return initiatorService.get();
    }

    /**
     * fetch a coordinator registration service unique to the current thread
     * @return
     */
    private static synchronized ParticipantService getParticipantService()
    {
        if (participantService.get() == null) {
            participantService.set(new ParticipantService());
        }
        return participantService.get();
    }

    public static InitiatorPortType getInitiatorPort(AddressingProperties addressingProperties,
                                                       String action)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        InitiatorService service = getInitiatorService();
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
        ParticipantService service = getParticipantService();
        ParticipantPortType port = service.getPort(ParticipantPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        AttributedURI toUri = addressingProperties.getTo();
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannot specify the WSAddressing feature
         */
		customHandlerChain.add(new WSAddressingClientHandler());
        /*
         * we need to add the coordination context handler in the case where we are passing a
         * coordination context via a header element
         */
        customHandlerChain.add(new CoordinationContextHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
	    // jbossws should do this for us . . .
	    requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, toUri.getURI().toString());
        try {
            addressingProperties.setAction(builder.newURI(action));
        } catch (URISyntaxException use) {
            // TODO log this error
        }

        return port;
    }
}