package com.arjuna.webservices11.wsat.client;

import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilderFactory;
import org.oasis_open.docs.ws_tx.wsat._2006._06.*;

import javax.xml.namespace.QName;
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
     *  builder used to construct addressing info for calls
     */
    private static MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();

    /**
     * fetch a coordinator service unique to the current thread
     * @return
     */
    private static synchronized CoordinatorService getCoordinatorService()
    {
        if (coordinatorService.get() == null) {
            //coordinatorService.set(new CoordinatorService(null, new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "CoordinatorService")));
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
            //participantService.set(new ParticipantService(null, new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "ParticipantService")));
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
            //completionCoordinatorService.set(new CompletionCoordinatorService(null, new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "CompletionCoordinatorService")));
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
            //completionInitiatorService.set(new CompletionInitiatorService(null, new QName("http://docs.oasis-open.org/ws-tx/wsat/2006/06", "CompletionInitiatorService")));
            completionInitiatorService.set(new CompletionInitiatorService());
        }
        return completionInitiatorService.get();
    }

    // fetch ports when we HAVE an endpoint

    public static CoordinatorPortType getCoordinatorPort(W3CEndpointReference endpointReference,
                                                         String action,
                                                         MAP map)
    {
        CoordinatorService service = getCoordinatorService();
        CoordinatorPortType port = service.getPort(endpointReference, CoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    public static ParticipantPortType getParticipantPort(W3CEndpointReference endpointReference,
                                                         String action,
                                                         MAP map)
    {
        ParticipantService service = getParticipantService();
        ParticipantPortType port = service.getPort(endpointReference, ParticipantPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    public static CompletionCoordinatorPortType getCompletionCoordinatorPort(W3CEndpointReference endpointReference,
                                                                             String action,
                                                                             MAP map)
    {
        CompletionCoordinatorService service = getCompletionCoordinatorService();
        CompletionCoordinatorPortType port = service.getPort(endpointReference, CompletionCoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    public static CompletionInitiatorPortType getCompletionInitiatorPort(W3CEndpointReference endpointReference,
                                                                         String action,
                                                                         MAP map)
    {
        CompletionInitiatorService service = getCompletionInitiatorService();
        CompletionInitiatorPortType port = service.getPort(endpointReference, CompletionInitiatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configureEndpointPort(bindingProvider, action, map);

        return port;
    }

    // fetch ports when we have NO endpoint

    public static CoordinatorPortType getCoordinatorPort(String action,
                                                         MAP map)
    {
        CoordinatorService service = getCoordinatorService();
        CoordinatorPortType port = service.getPort(CoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configurePort(bindingProvider, action, map);

        return port;
    }

    public static ParticipantPortType getParticipantPort(String action,
                                                         MAP map)
    {
        ParticipantService service = getParticipantService();
        ParticipantPortType port = service.getPort(ParticipantPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configurePort(bindingProvider, action, map);

        return port;
    }

    public static CompletionCoordinatorPortType getCompletionCoordinatorPort(String action,
                                                                             MAP map)
    {
        CompletionCoordinatorService service = getCompletionCoordinatorService();
        CompletionCoordinatorPortType port = service.getPort(CompletionCoordinatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;

        configurePort(bindingProvider, action, map);

        return port;
    }

    public static CompletionInitiatorPortType getCompletionInitiatorPort(String action,
                                                                         MAP map)
    {
        CompletionInitiatorService service = getCompletionInitiatorService();
        CompletionInitiatorPortType port = service.getPort(CompletionInitiatorPortType.class, new AddressingFeature(true, true));
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