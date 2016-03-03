package com.arjuna.webservices11.wsarjtx.client;

import com.arjuna.schemas.ws._2005._10.wsarjtx.*;
import com.arjuna.webservices11.util.PrivilegedServiceFactory;
import com.arjuna.webservices11.util.PrivilegedServiceHelper;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.util.Map;
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
     *  thread local which maintains a per thread termination coordinator service instance
     */
    private static ThreadLocal<TerminationCoordinatorRPCService> terminationCoordinatorRPCService = new ThreadLocal<TerminationCoordinatorRPCService>();

    /**
     * fetch a termination coordinator service unique to the current thread
     * @return
     */
    private static synchronized TerminationCoordinatorService getTerminationCoordinatorService()
    {
        if (terminationCoordinatorService.get() == null) {
            terminationCoordinatorService.set(
                    PrivilegedServiceFactory.getInstance(TerminationCoordinatorService.class).getService());
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
            terminationParticipantService.set(
                    PrivilegedServiceFactory.getInstance(TerminationParticipantService.class).getService());
        }
        return terminationParticipantService.get();
    }

    /**
     * fetch a termination coordinator service unique to the current thread
     * @return
     */
    private static synchronized TerminationCoordinatorRPCService getTerminationCoordinatorRPCService()
    {
        if (terminationCoordinatorRPCService.get() == null) {
            terminationCoordinatorRPCService.set(
                    PrivilegedServiceFactory.getInstance(TerminationCoordinatorRPCService.class).getService());
        }
        return terminationCoordinatorRPCService.get();
    }

    public static TerminationCoordinatorPortType getTerminationCoordinatorPort(W3CEndpointReference endpointReference,
                                                                                String action,
                                                                                MAP map)
    {
        final TerminationCoordinatorService service = getTerminationCoordinatorService();
        final TerminationCoordinatorPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                endpointReference, TerminationCoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we no longer have to add the JaxWS WSAddressingClientHandler because we can specify the WSAddressing feature
        List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
         */

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        MAP requestMap = AddressingHelper.outboundMap(requestContext);
        map.setAction(action);
        AddressingHelper.installCallerProperties(map, requestMap);
        AddressingHelper.configureRequestContext(requestContext, requestMap.getTo(), action);
        return port;
    }

    // don't think we ever need this as we get a registration port from the endpoint ref returned by
    // the activation port request
    public static TerminationParticipantPortType getTerminationParticipantPort(W3CEndpointReference endpointReference,
                                                                               String action,
                                                                               MAP map)
    {
        final TerminationParticipantService service = getTerminationParticipantService();
        final TerminationParticipantPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                endpointReference, TerminationParticipantPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we no longer have to add the JaxWS WSAddressingClientHandler because we can specify the WSAddressing feature
        List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
         */
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        MAP requestMap = AddressingHelper.outboundMap(requestContext);
        map.setAction(action);
        AddressingHelper.installCallerProperties(map, requestMap);
        AddressingHelper.configureRequestContext(requestContext, requestMap.getTo(), action);
        return port;
    }
    // we use this in situations where we don't have a proper endpoint but we do have caller addressing properties
    public static TerminationParticipantPortType getTerminationParticipantPort(InstanceIdentifier identifier,
                                                                               String action,
                                                                               MAP map)
    {
        final TerminationParticipantService service = getTerminationParticipantService();
        final TerminationParticipantPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                TerminationParticipantPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we no longer have to add the JaxWS WSAddressingClientHandler because we can specify the WSAddressing feature
        List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
         */
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        Element refParam = InstanceIdentifier.createInstanceIdentifierElement(identifier.getInstanceIdentifier());
        map.addReferenceParameter(refParam);
        map.setAction(action);
        AddressingHelper.configureRequestContext(requestContext, map, map.getTo(), action);
        return port;
    }
    public static TerminationCoordinatorRPCPortType getTerminationCoordinatorRPCPort(W3CEndpointReference endpointReference,
                                                                                  String action,
                                                                                  MAP map)
    {
        final TerminationCoordinatorRPCService service = getTerminationCoordinatorRPCService();
        final TerminationCoordinatorRPCPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                endpointReference, TerminationCoordinatorRPCPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        /*
         * we no longer have to add the JaxWS WSAddressingClientHandler because we can specify the WSAddressing feature
        List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingClientHandler());
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
         */

        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        MAP requestMap = AddressingHelper.outboundMap(requestContext);
        map.setAction(action);
        AddressingHelper.installCallerProperties(map, requestMap);
        AddressingHelper.configureRequestContext(requestContext, requestMap.getTo(), action);
        return port;
    }
}
