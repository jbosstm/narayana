/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsarjtx.server;

import com.arjuna.webservices11.util.PrivilegedServiceRegistryFactory;
import com.arjuna.webservices11.wsarjtx.ArjunaTX11Constants;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.WSTEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Activate the Terminator Coordinator service
 * @author kevin
 */
public class TerminationParticipantInitialisation
{
    public static void startup()
    {
        final ServiceRegistry serviceRegistry = PrivilegedServiceRegistryFactory.getInstance().getServiceRegistry();
        WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
        String bindAddress = wscEnvironmentBean.getBindAddress11();
        int bindPort = wscEnvironmentBean.getBindPort11();
        int secureBindPort = wscEnvironmentBean.getBindPortSecure11();
        WSTEnvironmentBean wstEnvironmentBean = XTSPropertyManager.getWSTEnvironmentBean();
        String clientServiceURLPath = wstEnvironmentBean.getClientServiceURLPath();
        if (clientServiceURLPath == null) {
            clientServiceURLPath = "/ws-t11-client";
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

        final String baseUri = "http://" +  bindAddress + ":" + bindPort + clientServiceURLPath;
        final String uri = baseUri + "/" + ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME;
        final String secureBaseUri = "https://" +  bindAddress + ":" + secureBindPort + clientServiceURLPath;
        final String secureUri = secureBaseUri + "/" + ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME;

        serviceRegistry.registerServiceProvider(ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME, uri) ;
        serviceRegistry.registerSecureServiceProvider(ArjunaTX11Constants.TERMINATION_PARTICIPANT_SERVICE_NAME, secureUri) ;
    }

    public static void shutdown()
    {
    }
}