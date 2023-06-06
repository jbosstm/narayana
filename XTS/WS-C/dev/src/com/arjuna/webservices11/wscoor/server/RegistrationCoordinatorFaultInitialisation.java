/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package com.arjuna.webservices11.wscoor.server;

import com.arjuna.webservices11.util.PrivilegedServiceRegistryFactory;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * The RegistrationCoordinatorFaultService is the endpoint in charge of 
 * receiving asynchronous soap faults when <em>useAsynchronousRequest</em> is 
 * configured. This is mainly implemented for inter-operability with Microsoft
 * WS-Coordination implementation and it is not a standard endpoint.
 * 
 * @author rmartinc
 */
public class RegistrationCoordinatorFaultInitialisation
{
    /**
     * The context has been initialized.
     */
    public static void startup()
    {
        // TODO work out how to configure the endpoint name here
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
        final String uri = baseUri + "/CoordinationFaultService";
        final String secureBaseUri = "https://" + bindAddress + ":" + secureBindPort + serviceURLPath;
        final String secureUri = secureBaseUri + "/CoordinationFaultService";

        serviceRegistry.registerServiceProvider(CoordinationConstants.COORDINATION_SOAP_FAULT_SERVICE_NAME, uri) ;
        serviceRegistry.registerSecureServiceProvider(CoordinationConstants.COORDINATION_SOAP_FAULT_SERVICE_NAME, secureUri); ;
    }

    public static void shutdown()
    {
    }
}