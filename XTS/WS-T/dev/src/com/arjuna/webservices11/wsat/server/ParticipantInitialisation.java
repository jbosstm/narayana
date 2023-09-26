/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices11.wsat.server;

import com.arjuna.webservices11.util.PrivilegedServiceRegistryFactory;
import com.arjuna.webservices11.wsat.AtomicTransactionConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.WSTEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Activate the Participant service
 * @author kevin
 */
public class ParticipantInitialisation
{
    public static void startup()
    {
               final ServiceRegistry serviceRegistry = PrivilegedServiceRegistryFactory.getInstance().getServiceRegistry();
               WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
               String bindAddress = wscEnvironmentBean.getBindAddress11();
               int bindPort = wscEnvironmentBean.getBindPort11();
               int secureBindPort = wscEnvironmentBean.getBindPortSecure11();
        WSTEnvironmentBean wstEnvironmentBean = XTSPropertyManager.getWSTEnvironmentBean();
        String participantServiceURLPath = wstEnvironmentBean.getParticipantServiceURLPath();
        if (participantServiceURLPath == null) {
            participantServiceURLPath = "/ws-t11-participant";
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

               final String baseUri = "http://" +  bindAddress + ":" + bindPort + participantServiceURLPath;
               final String uri = baseUri + "/" + AtomicTransactionConstants.PARTICIPANT_SERVICE_NAME;
               final String secureBaseUri = "https://" +  bindAddress + ":" + secureBindPort + participantServiceURLPath;
               final String secureUri = secureBaseUri + "/" +  AtomicTransactionConstants.PARTICIPANT_SERVICE_NAME;

               serviceRegistry.registerServiceProvider(AtomicTransactionConstants.PARTICIPANT_SERVICE_NAME, uri) ;
               serviceRegistry.registerSecureServiceProvider(AtomicTransactionConstants.PARTICIPANT_SERVICE_NAME, secureUri) ;
    }

    public static void shutdown()
    {
    }
}