/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsba.server;

import com.arjuna.webservices11.util.PrivilegedServiceRegistryFactory;
import com.arjuna.webservices11.wsba.BusinessActivityConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.WSTEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Activate the Participant Completion Coordinator service
 * @author kevin
 */
public class ParticipantCompletionCoordinatorInitialisation
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
        final String uri = baseUri + "/" + BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME;
        final String secureBaseUri = "https://" +  bindAddress + ":" + secureBindPort + coordinatorServiceURLPath;
        final String secureUri = secureBaseUri + "/" + BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME;

        serviceRegistry.registerServiceProvider(BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME, uri) ;
        serviceRegistry.registerSecureServiceProvider(BusinessActivityConstants.PARTICIPANT_COMPLETION_COORDINATOR_SERVICE_NAME, secureUri) ;
    }

    public static void shutdown()
    {
    }
}