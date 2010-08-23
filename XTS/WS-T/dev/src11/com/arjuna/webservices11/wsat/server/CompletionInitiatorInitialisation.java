/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.webservices11.wsat.server;

import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Activate the Completion Initiator service
 * @author kevin
 */
public class CompletionInitiatorInitialisation
{
    public static void startup()
    {
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
        String bindAddress = wscEnvironmentBean.getBindAddress11();
        int bindPort = wscEnvironmentBean.getBindPort11();
        int secureBindPort = wscEnvironmentBean.getBindPortSecure11();


        if (bindAddress == null) {
            bindAddress = "127.0.0.1";
        }

        if (bindPort == 0) {
            bindPort = 8080;
        }

        if (secureBindPort == 0) {
            secureBindPort = 8443;
        }

        final String baseUri = "http://" +  bindAddress + ":" + bindPort + "/ws-t11/";
        final String uri = baseUri + AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME;
        final String secureBaseUri = "https://" +  bindAddress + ":" + secureBindPort + "/ws-t11/";
        final String secureUri = secureBaseUri + AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME;

        serviceRegistry.registerServiceProvider(AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME, uri) ;
        serviceRegistry.registerSecureServiceProvider(AtomicTransactionConstants.COMPLETION_INITIATOR_SERVICE_NAME, secureUri) ;
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public static void shutdwon()
    {
    }
}