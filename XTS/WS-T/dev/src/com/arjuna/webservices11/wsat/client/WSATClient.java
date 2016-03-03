package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices11.util.PrivilegedServiceFactory;
import com.arjuna.webservices11.util.PrivilegedServiceHelper;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.ws.api.addressing.MAP;
import org.oasis_open.docs.ws_tx.wsat._2006._06.*;

import javax.xml.ws.wsaddressing.W3CEndpointReference;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;
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
     *  thread local which maintains a per thread completion coordinator service instance
     */
    private static ThreadLocal<CompletionCoordinatorRPCService> completionCoordinatorRPCService = new ThreadLocal<CompletionCoordinatorRPCService>();

    /**
     * fetch a coordinator service unique to the current thread
     * @return
     */
    private static synchronized CoordinatorService getCoordinatorService()
    {
        if (coordinatorService.get() == null) {
            coordinatorService.set(PrivilegedServiceFactory.getInstance(CoordinatorService.class).getService());
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
            participantService.set(PrivilegedServiceFactory.getInstance(ParticipantService.class).getService());
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
            completionCoordinatorService.set(
                    PrivilegedServiceFactory.getInstance(CompletionCoordinatorService.class).getService());
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
            completionInitiatorService.set(
                    PrivilegedServiceFactory.getInstance(CompletionInitiatorService.class).getService());
        }
        return completionInitiatorService.get();
    }

    /**
     * fetch an RPC completion coordinator service unique to the current thread
     * @return
     */
    private static synchronized CompletionCoordinatorRPCService getCompletionCoordinatorRPCService()
    {
        if (completionCoordinatorRPCService.get() == null) {
            completionCoordinatorRPCService.set(
                    PrivilegedServiceFactory.getInstance(CompletionCoordinatorRPCService.class).getService());
        }
        return completionCoordinatorRPCService.get();
    }

    // fetch ports when we HAVE an endpoint

    public static CoordinatorPortType getCoordinatorPort(final W3CEndpointReference endpointReference,
                                                         String action,
                                                         MAP map)
    {
        final CoordinatorService service = getCoordinatorService();
        final CoordinatorPortType port = PrivilegedServiceHelper.getInstance().getPort(service, endpointReference,
                CoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    public static ParticipantPortType getParticipantPort(final W3CEndpointReference endpointReference,
                                                         String action,
                                                         MAP map)
    {
        final ParticipantService service = getParticipantService();
        final ParticipantPortType port = PrivilegedServiceHelper.getInstance().getPort(service, endpointReference,
                ParticipantPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    public static CompletionCoordinatorPortType getCompletionCoordinatorPort(final W3CEndpointReference endpointReference,
                                                                             String action,
                                                                             MAP map)
    {
        final CompletionCoordinatorService service = getCompletionCoordinatorService();
        final CompletionCoordinatorPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                endpointReference, CompletionCoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    public static CompletionInitiatorPortType getCompletionInitiatorPort(final W3CEndpointReference endpointReference,
                                                                         String action,
                                                                         MAP map)
    {
        final CompletionInitiatorService service = getCompletionInitiatorService();
        final CompletionInitiatorPortType port = PrivilegedServiceHelper.getInstance().getPort(service, endpointReference,
                        CompletionInitiatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    public static CompletionCoordinatorRPCPortType getCompletionCoordinatorRPCPort(final W3CEndpointReference endpointReference,
                                                                             String action,
                                                                             MAP map)
    {
        final CompletionCoordinatorRPCService service = getCompletionCoordinatorRPCService();
        final CompletionCoordinatorRPCPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                endpointReference, CompletionCoordinatorRPCPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    // fetch ports when we have NO endpoint

    public static CoordinatorPortType getCoordinatorPort(String action,
                                                         MAP map)
    {
        final CoordinatorService service = getCoordinatorService();
        final CoordinatorPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                CoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configurePort(bindingProvider, action, map);

        return port;
    }

    public static ParticipantPortType getParticipantPort(String action,
                                                         MAP map)
    {
        final ParticipantService service = getParticipantService();
        final ParticipantPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                ParticipantPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configurePort(bindingProvider, action, map);

        return port;
    }

    public static CompletionCoordinatorPortType getCompletionCoordinatorPort(String action,
                                                                             MAP map)
    {
        final CompletionCoordinatorService service = getCompletionCoordinatorService();
        final CompletionCoordinatorPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                CompletionCoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configurePort(bindingProvider, action, map);

        return port;
    }

    public static CompletionInitiatorPortType getCompletionInitiatorPort(String action,
                                                                         MAP map)
    {
        final CompletionInitiatorService service = getCompletionInitiatorService();
        final CompletionInitiatorPortType port = PrivilegedServiceHelper.getInstance().getPort(service,
                CompletionInitiatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configurePort(bindingProvider, action, map);

        return port;
    }

    private static void configureEndpointPort(BindingProvider bindingProvider, String action, MAP map)
    {
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
    }

    private static void configurePort(BindingProvider bindingProvider,
                                      String action,
                                      MAP map)
    {
        /*
         * we no longer have to add the JaxWS WSAddressingClientHandler because we can specify the WSAddressing feature
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        customHandlerChain.add(new WSAddressingClientHandler());
        bindingProvider.getBinding().setHandlerChain(customHandlerChain);
         */
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        map.setAction(action);
        AddressingHelper.configureRequestContext(requestContext, map, map.getTo(), action);
    }
}
