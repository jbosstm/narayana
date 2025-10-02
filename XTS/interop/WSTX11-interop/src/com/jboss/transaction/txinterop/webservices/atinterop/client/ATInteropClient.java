/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.jboss.transaction.txinterop.webservices.atinterop.client;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.handler.Handler;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.ws.api.addressing.MAP;
import org.jboss.ws.api.addressing.MAPEndpoint;
import org.jboss.ws.api.addressing.MAPBuilder;
import org.jboss.ws.api.addressing.MAPBuilderFactory;
import com.arjuna.webservices11.wsaddr.AddressingHelper;
import com.jboss.transaction.txinterop.webservices.atinterop.generated.InitiatorService;
import com.jboss.transaction.txinterop.webservices.atinterop.generated.ParticipantService;
import com.jboss.transaction.txinterop.webservices.atinterop.generated.InitiatorPortType;
import com.jboss.transaction.txinterop.webservices.atinterop.generated.ParticipantPortType;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;
import com.jboss.transaction.txinterop.webservices.handlers.CoordinationContextHandler;

/**
 * Created by IntelliJ IDEA.
 * User: adinn
 * Date: Apr 17, 2008
 * Time: 4:18:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class ATInteropClient {
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
    private static MAPBuilder builder = MAPBuilderFactory.getInstance().getBuilderInstance();

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

    public static InitiatorPortType getInitiatorPort(MAP map,
                                                       String action)
    {
        InitiatorService service = getInitiatorService();
        InitiatorPortType port = service.getPort(InitiatorPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        String to = map.getTo();
        /*
         * we have to add the JaxWS WSAddressingClientHandler because we cannot specify the WSAddressing feature
        List<Handler> customHandlerChain = new ArrayList<Handler>();
		customHandlerChain.add(new WSAddressingClientHandler());
        bindingProvider.getBinding().setHandlerChain(customHandlerChain);
         */
        Map<String, Object> requestContext = bindingProvider.getRequestContext();
        map.setAction(action);
        map.setFrom(getParticipant());
        AddressingHelper.configureRequestContext(requestContext, map, to, action);

        return port;
    }

    // don't think we ever need this as we get a registration port from the endpoint ref returned by
    // the activation port request
    public static ParticipantPortType getParticipantPort(MAP map, String action)
    {
        ParticipantService service = getParticipantService();
        ParticipantPortType port = service.getPort(ParticipantPortType.class, new AddressingFeature(true, true));
        BindingProvider bindingProvider = (BindingProvider)port;
        String to = map.getTo();
        List<Handler> customHandlerChain = new ArrayList<Handler>();
        /*
         * we need to add the coordination context handler in the case where we are passing a
         * coordination context via a header element
         */
        customHandlerChain.add(new CoordinationContextHandler());
        /*
         * we no longer have to add the JaxWS WSAddressingClientHandler because we can specify the WSAddressing feature
		customHandlerChain.add(new WSAddressingClientHandler());
         */
		bindingProvider.getBinding().setHandlerChain(customHandlerChain);
        Map<String, Object> requestContext = bindingProvider.getRequestContext();

        map.setAction(action);
        map.setFrom(getInitiator());
        AddressingHelper.configureRequestContext(requestContext, map, to, action);

        return port;
    }

    private static MAPEndpoint initiator;
    private static MAPEndpoint participant;

    private static synchronized MAPEndpoint getInitiator()
    {
        if (initiator == null) {
            final String initiatorURIString =
                    ServiceRegistry.getRegistry().getServiceURI(ATInteropConstants.SERVICE_INITIATOR);
            initiator = builder.newEndpoint(initiatorURIString);
        }
        return initiator;
    }

    private static synchronized MAPEndpoint getParticipant()
    {
        if (participant == null) {
            final String participantURIString =
                    ServiceRegistry.getRegistry().getServiceURI(ATInteropConstants.SERVICE_PARTICIPANT);
            participant = builder.newEndpoint(participantURIString);
        }
        return participant;
    }
}
