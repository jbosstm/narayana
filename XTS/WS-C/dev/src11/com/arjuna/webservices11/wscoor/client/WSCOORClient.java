package com.arjuna.webservices11.wscoor.client;

import com.arjuna.webservices11.wsarj.handler.InstanceIdentifierHandler;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.ActivationPortType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.ActivationService;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegistrationPortType;
import org.oasis_open.docs.ws_tx.wscoor._2006._06.RegistrationService;
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
public class WSCOORClient
{
    // TODO -- do we really need a thread local here or can we just use one service?
    /**
     *  thread local which maintains a per thread activation service instance
     */
    private static ThreadLocal<ActivationService> activationService = new ThreadLocal<ActivationService>();

    /**
     *  thread local which maintains a per thread activation service instance
     */
    private static ThreadLocal<RegistrationService> registrationService = new ThreadLocal<RegistrationService>();

    /**
     *  builder used to construct addressing info for calls
     */
    private static AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();

    /**
     * fetch a coordinator activation service unique to the current thread
     * @return
     */
    private static synchronized ActivationService getActivationService()
    {
        if (activationService.get() == null) {
            activationService.set(new ActivationService());
        }
        return activationService.get();
    }

    /**
     * fetch a coordinator registration service unique to the current thread
     * @return
     */
    private static synchronized RegistrationService getRegistrationService()
    {
        if (registrationService.get() == null) {
            registrationService.set(new RegistrationService());
        }
        return registrationService.get();
    }

    public static ActivationPortType getActivationPort(AddressingProperties addressingProperties,
                                                       String action)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        ActivationService service = getActivationService();
        // ActivationPortType port = service.getPort(ActivationPortType.class, new AddressingFeature(true, true));
        ActivationPortType port = service.getPort(ActivationPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        AttributedURI toUri = addressingProperties.getTo();
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannot specify the WSAddressing feature
         */
        List<Handler> customHandlerChain = new ArrayList<Handler>();
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
    public static RegistrationPortType getRegistrationPort(W3CEndpointReference endpointReference, String action, String messageID)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        RegistrationService service = getRegistrationService();
        // RegistrationPortType port = service.getPort(endpointReference, RegistrationPortType.class, new AddressingFeature(true, true));
        RegistrationPortType port = service.getPort(endpointReference, RegistrationPortType.class);
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
        // but we still need to set the action and message id -- this is all distinctly non-portable :-/
        // n.b. Metro installs the address in requestContext under ENDPOINT_ADDRESS_PROPERTY. it also seems to ensure
        // that the reference parameters get installed -- but how?

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        // String address = (String)requestContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        // AddressingProperties addressingProperties = AddressingHelper.createRequestContext(address, action, messageID);
        // requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
	    // jbossws should do this for us . . .
	    // requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
        AddressingProperties addressingProperties = (AddressingProperties)requestContext.get(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND);
        AddressingHelper.installActionMessageID(addressingProperties, action, messageID);
        // we should not need to do this but JBossWS does not pick up the value in the addressing properties
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, addressingProperties.getTo().getURI().toString());
        return port;
    }
}
