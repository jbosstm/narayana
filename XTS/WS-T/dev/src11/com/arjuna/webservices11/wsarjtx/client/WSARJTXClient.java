package com.arjuna.webservices11.wsarjtx.client;

import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationCoordinatorPortType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationCoordinatorService;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationParticipantPortType;
import com.arjuna.schemas.ws._2005._10.wsarjtx.TerminationParticipantService;
import com.arjuna.webservices11.wsarj.InstanceIdentifier;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import org.jboss.wsf.common.addressing.MAPBuilder;
import org.jboss.wsf.common.addressing.MAP;
import org.jboss.wsf.common.addressing.MAPBuilderFactory;

import javax.xml.namespace.QName;
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
     *  builder used to construct addressing info for calls
     */
    private static MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();

    /**
     * fetch a termination coordinator service unique to the current thread
     * @return
     */
    private static synchronized TerminationCoordinatorService getTerminationCoordinatorService()
    {
        if (terminationCoordinatorService.get() == null) {
            //terminationCoordinatorService.set(new TerminationCoordinatorService(null, new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "TerminationCoordinatorService")));
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
            //terminationParticipantService.set(new TerminationParticipantService(null, new QName("http://schemas.arjuna.com/ws/2005/10/wsarjtx", "TerminationParticipantService")));
            terminationParticipantService.set(new TerminationParticipantService());
        }
        return terminationParticipantService.get();
    }

    public static TerminationCoordinatorPortType getTerminationCoordinatorPort(W3CEndpointReference endpointReference,
                                                                                String action,
                                                                                MAP map)
    {
        TerminationCoordinatorService service = getTerminationCoordinatorService();
        TerminationCoordinatorPortType port = service.getPort(endpointReference, TerminationCoordinatorPortType.class, new AddressingFeature(true, true));
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
        TerminationParticipantService service = getTerminationParticipantService();
        TerminationParticipantPortType port = service.getPort(endpointReference, TerminationParticipantPortType.class, new AddressingFeature(true, true));
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
        TerminationParticipantService service = getTerminationParticipantService();
        TerminationParticipantPortType port = service.getPort(TerminationParticipantPortType.class, new AddressingFeature(true, true));
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
}