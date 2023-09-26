/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsarjtx.server;

import com.arjuna.webservices11.util.PrivilegedServiceRegistryFactory;
import com.arjuna.webservices11.ServiceRegistry;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.WSTEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Activate the Terminator Participant service
 * @author kevin
 */
public class TerminationCoordinatorRPCInitialisation
{
    public static void startup()
    {
        final ServiceRegistry serviceRegistry = PrivilegedServiceRegistryFactory.getInstance().getServiceRegistry();
        WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
        String bindAddress = wscEnvironmentBean.getBindAddress11();
        int bindPort = wscEnvironmentBean.getBindPort11();
        int secureBindPort = wscEnvironmentBean.getBindPortSecure11();
        WSTEnvironmentBean wstEnvironmentBean = XTSPropertyManager.getWSTEnvironmentBean();
        String coordinatorServiceURLPath = wstEnvironmentBean.getCoordinatorServiceURLPath();
        if (coordinatorServiceURLPath == null) {
            coordinatorServiceURLPath = "/ws-t11-coordinator";
        }

        if (bindAddress == null) {
            bindAddress = "localhost";
        }

        if (bindPort == 0) {
            bindPort = 8080;
        }

        if (secureBindPort == 0) {
            secureBindPort = 8443;
        }

        final String baseUri = "http://" +  bindAddress + ":" + bindPort + coordinatorServiceURLPath;
        final String uri = baseUri + "/" + ArjunaTX11Constants.TERMINATION_COORDINATOR_RPC_SERVICE_NAME;
        final String secureBaseUri = "https://" +  bindAddress + ":" + secureBindPort + coordinatorServiceURLPath;
        final String secureUri = secureBaseUri + "/" + ArjunaTX11Constants.TERMINATION_COORDINATOR_RPC_SERVICE_NAME;

        serviceRegistry.registerServiceProvider(ArjunaTX11Constants.TERMINATION_COORDINATOR_RPC_SERVICE_NAME, uri) ;
        serviceRegistry.registerSecureServiceProvider(ArjunaTX11Constants.TERMINATION_COORDINATOR_RPC_SERVICE_NAME, secureUri) ;
    }

    public static void shutdown()
    {
    }
}