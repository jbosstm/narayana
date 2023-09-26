/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wscoor.server;

import com.arjuna.webservices11.util.PrivilegedServiceRegistryFactory;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Activate the Activation Coordinator service
 * @author kevin
 */
public class ActivationCoordinatorInitialisation
{
    public static void startup()
    {
        final ServiceRegistry serviceRegistry = PrivilegedServiceRegistryFactory.getInstance().getServiceRegistry();
        WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
        String bindAddress = wscEnvironmentBean.getBindAddress11();
        int bindPort = wscEnvironmentBean.getBindPort11();
        int secureBindPort = wscEnvironmentBean.getBindPortSecure11();
        String serviceURLPath = wscEnvironmentBean.getServiceURLPath();
        if (serviceURLPath == null) {
            serviceURLPath = "/ws-c11";
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

        final String baseUri = "http://" +  bindAddress + ":" + bindPort + serviceURLPath;
        final String uri = baseUri + "/ActivationService";
        final String secureBaseUri = "https://" + bindAddress + ":" + secureBindPort + serviceURLPath;
        final String secureUri = secureBaseUri + "/ActivationService";

        serviceRegistry.registerServiceProvider(CoordinationConstants.ACTIVATION_SERVICE_NAME, uri) ;
        serviceRegistry.registerSecureServiceProvider(CoordinationConstants.ACTIVATION_SERVICE_NAME, secureUri) ;
    }

    public static void shutdown()
    {
    }
}