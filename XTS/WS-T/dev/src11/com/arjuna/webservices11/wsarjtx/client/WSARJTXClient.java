package com.arjuna.webservices11.wsarjtx.client;

import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationCoordinatorPortType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationCoordinatorService;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationParticipantPortType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationParticipantService;
import com.arjuna.webservices11.wsarj.handler.InstanceIdentifierHandler;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsaddr.AddressingHelper;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.addressing.AddressingBuilder;
import javax.xml.ws.addressing.AddressingProperties;
import javax.xml.ws.addressing.AttributedURI;
import javax.xml.ws.addressing.JAXWSAConstants;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.net.URISyntaxException;

import org.jboss.ws.extensions.addressing.jaxws.WSAddressingClientHandler;
import org.w3c.dom.Element;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Oct 7, 2007
 * Time: 3:14:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class WSARJTXClient
{
    // TODO -- do we really need a thread local here or can we just use one service?
    /**
     *  thread local which maintains a per thread termination coordinator service instance
     */
    private static ThreadLocal<TerminationCoordinatorService> terminationCoordinatorService = new ThreadLocal<TerminationCoordinatorService>();

    /**
     *  thread local which maintains a per thread termination participant service instance
     */
    private static ThreadLocal<TerminationParticipantService> terminationParticipantService = new ThreadLocal<TerminationParticipantService>();

    /**
     *  builder used to construct addressing info for calls
     */
    private static AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();

    /**
     * fetch a termination coordinator service unique to the current thread
     * @return
     */
    private static synchronized TerminationCoordinatorService getTerminationCoordinatorService()
    {
        if (terminationCoordinatorService.get() == null) {
            terminationCoordinatorService.set(new TerminationCoordinatorService());
        }
        return terminationCoordinatorService.get();
    }

    /**
     * fetch a termination participant service unique to the current thread
     * @return
     */
    private static synchronized TerminationParticipantService getTerminationParticipantService()
    {
        if (terminationParticipantService.get() == null) {
            terminationParticipantService.set(new TerminationParticipantService());
        }
        return terminationParticipantService.get();
    }

    public static TerminationCoordinatorPortType getTerminationCoordinatorPort(W3CEndpointReference endpointReference,
                                                                                AttributedURI action,
                                                                                AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        TerminationCoordinatorService service = getTerminationCoordinatorService();
        // TerminationCoordinatorPortType port = service.getPort(endpointReference, TerminationCoordinatorPortType.class, new AddressingFeature(true, true));
        TerminationCoordinatorPortType port = service.getPort(endpointReference, TerminationCoordinatorPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannoy specify the WSAddressing feature
         */
        List<Handler> customHandlerChain = new ArrayList<Handler>();
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
    public static TerminationParticipantPortType getTerminationParticipantPort(W3CEndpointReference endpointReference,
                                                                               AttributedURI action,
                                                                               AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        TerminationParticipantService service = getTerminationParticipantService();
        // TerminationParticipantPortType port = service.getPort(endpointReference, TerminationParticipantPortType.class, new AddressingFeature(true, true));
        TerminationParticipantPortType port = service.getPort(endpointReference, TerminationParticipantPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannoy specify the WSAddressing feature
         */
        List<Handler> customHandlerChain = new ArrayList<Handler>();
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
    // we use this in situations where we don't have a proper endpoint but we do have caller addressing properties
    public static TerminationParticipantPortType getTerminationParticipantPort(InstanceIdentifier identifier,
                                                                               AttributedURI action,
                                                                               AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        TerminationParticipantService service = getTerminationParticipantService();
        // TerminationParticipantPortType port = service.getPort(endpointReference, TerminationParticipantPortType.class, new AddressingFeature(true, true));
        TerminationParticipantPortType port = service.getPort(TerminationParticipantPortType.class);
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannoy specify the WSAddressing feature
         */
        List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        Element refParam = InstanceIdentifier.createInstanceIdentifierElement(identifier.getInstanceIdentifier());
        addressingProperties.getReferenceParameters().addElement(refParam);
        addressingProperties.setAction(action);
        requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES, addressingProperties);
        // JBossWS shoudl do this for us
        requestContext.put(JAXWSAConstants.CLIENT_ADDRESSING_PROPERTIES_OUTBOUND, addressingProperties);
        // we should not need to do this but JBossWS does not pick up the value in the addressing properties
        requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, addressingProperties.getTo().getURI().toString());
        return port;
    }
}