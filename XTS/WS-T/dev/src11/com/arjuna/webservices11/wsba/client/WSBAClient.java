package com.arjuna.webservices11.wsba.client;

import com.arjuna.webservices11.wsarj.handler.InstanceIdentifierHandler;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.oasis_open.docs.ws_tx.wsba._2006._06.*;
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
public class WSBAClient
{
    // TODO -- do we really need a thread local here or can we just use one service?
    /**
     *  thread local which maintains a per thread participant completion coordinator service instance
     */
    private static ThreadLocal<BusinessAgreementWithParticipantCompletionCoordinatorService> participantCompletionCoordinatorService = new ThreadLocal<BusinessAgreementWithParticipantCompletionCoordinatorService>();

    /**
     *  thread local which maintains a per thread participant completion participant service instance
     */
    private static ThreadLocal<BusinessAgreementWithParticipantCompletionParticipantService> participantCompletionParticipantService = new ThreadLocal<BusinessAgreementWithParticipantCompletionParticipantService>();

    /**
     *  thread local which maintains a per thread coordinator completion coordinator service instance
     */
    private static ThreadLocal<BusinessAgreementWithCoordinatorCompletionCoordinatorService> coordinatorCompletionCoordinatorService = new ThreadLocal<BusinessAgreementWithCoordinatorCompletionCoordinatorService>();

    /**
     *  thread local which maintains a per thread coordinator completion participant service instance
     */
    private static ThreadLocal<BusinessAgreementWithCoordinatorCompletionParticipantService> coordinatorCompletionParticipantService = new ThreadLocal<BusinessAgreementWithCoordinatorCompletionParticipantService>();

    /**
     *  builder used to construct addressing info for calls
     */
    private static AddressingBuilder builder = AddressingBuilder.getAddressingBuilder();

    /**
     * fetch a participant completion coordinator service unique to the current thread
     * @return
     */
    private static synchronized BusinessAgreementWithParticipantCompletionCoordinatorService getParticipantCompletionCoordinatorService()
    {
        if (participantCompletionCoordinatorService.get() == null) {
            participantCompletionCoordinatorService.set(new BusinessAgreementWithParticipantCompletionCoordinatorService());
        }
        return participantCompletionCoordinatorService.get();
    }

    /**
     * fetch a participant completion participant service unique to the current thread
     * @return
     */
    private static synchronized BusinessAgreementWithParticipantCompletionParticipantService getParticipantCompletionParticipantService()
    {
        if (participantCompletionParticipantService.get() == null) {
            participantCompletionParticipantService.set(new BusinessAgreementWithParticipantCompletionParticipantService());
        }
        return participantCompletionParticipantService.get();
    }

    /**
     * fetch a coordinator completion coordinator service unique to the current thread
     * @return
     */
    private static synchronized BusinessAgreementWithCoordinatorCompletionCoordinatorService getCoordinatorCompletionCoordinatorService()
    {
        if (coordinatorCompletionCoordinatorService.get() == null) {
            coordinatorCompletionCoordinatorService.set(new BusinessAgreementWithCoordinatorCompletionCoordinatorService());
        }
        return coordinatorCompletionCoordinatorService.get();
    }

    /**
     * fetch a coordinator completion participant service unique to the current thread
     * @return
     */
    private static synchronized BusinessAgreementWithCoordinatorCompletionParticipantService getCoordinatorCompletionParticipantService()
    {
        if (coordinatorCompletionParticipantService.get() == null) {
            coordinatorCompletionParticipantService.set(new BusinessAgreementWithCoordinatorCompletionParticipantService());
        }
        return coordinatorCompletionParticipantService.get();
    }

    public static BusinessAgreementWithParticipantCompletionCoordinatorPortType getParticipantCompletionCoordinatorPort(W3CEndpointReference endpointReference,
                                                         AttributedURI action,
                                                         AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        BusinessAgreementWithParticipantCompletionCoordinatorService service = getParticipantCompletionCoordinatorService();
        // BusinessAgreementWithParticipantCompletionCoordinatorPortType port = service.getPort(endpointReference, BusinessAgreementWithParticipantCompletionCoordinatorPortType.class, new AddressingFeature(true, true));
        BusinessAgreementWithParticipantCompletionCoordinatorPortType port = service.getPort(endpointReference, BusinessAgreementWithParticipantCompletionCoordinatorPortType.class);
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
    public static BusinessAgreementWithParticipantCompletionParticipantPortType getParticipantCompletionParticipantPort(W3CEndpointReference endpointReference,
                                                         AttributedURI action,
                                                         AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        BusinessAgreementWithParticipantCompletionParticipantService service = getParticipantCompletionParticipantService();
        // BusinessAgreementWithParticipantCompletionParticipantPortType port = service.getPort(endpointReference, BusinessAgreementWithParticipantCompletionParticipantPortType.class, new AddressingFeature(true, true));
        BusinessAgreementWithParticipantCompletionParticipantPortType port = service.getPort(endpointReference, BusinessAgreementWithParticipantCompletionParticipantPortType.class);
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

    public static BusinessAgreementWithCoordinatorCompletionCoordinatorPortType getCoordinatorCompletionCoordinatorPort(W3CEndpointReference endpointReference,
                                                                             AttributedURI action,
                                                                             AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        BusinessAgreementWithCoordinatorCompletionCoordinatorService service = getCoordinatorCompletionCoordinatorService();
        // BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port = service.getPort(endpointReference, BusinessAgreementWithCoordinatorCompletionCoordinatorPortType.class, new AddressingFeature(true, true));
        BusinessAgreementWithCoordinatorCompletionCoordinatorPortType port = service.getPort(endpointReference, BusinessAgreementWithCoordinatorCompletionCoordinatorPortType.class);
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

    public static BusinessAgreementWithCoordinatorCompletionParticipantPortType getCoordinatorCompletionParticipantPort(W3CEndpointReference endpointReference,
                                                                         AttributedURI action,
                                                                         AddressingProperties addressingProperties)
    {
        // TODO - we need the 2.1 verison of Service so we can specify that we want to use the WS Addressing feature
        BusinessAgreementWithCoordinatorCompletionParticipantService service = getCoordinatorCompletionParticipantService();
        // BusinessAgreementWithCoordinatorCompletionParticipantPortType port = service.getPort(endpointReference, BusinessAgreementWithCoordinatorCompletionParticipantPortType.class, new AddressingFeature(true, true));
        BusinessAgreementWithCoordinatorCompletionParticipantPortType port = service.getPort(endpointReference, BusinessAgreementWithCoordinatorCompletionParticipantPortType.class);
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
}