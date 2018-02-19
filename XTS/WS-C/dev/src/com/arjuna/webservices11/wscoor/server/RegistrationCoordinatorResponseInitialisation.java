/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2018, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package com.arjuna.webservices11.wscoor.server;

import com.arjuna.webservices11.util.PrivilegedServiceRegistryFactory;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.ServiceRegistry;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * The RegistrationResponseService is the endpoint in charge of receiving 
 * asynchronous register responses when <em>useAsynchronousRequest</em> is 
 * configured. This is mainly implemented for inter-operability with Microsoft
 * WS-Coordination implementation and it is not a standard endpoint.
 * 
 * @author rmatinc
 */
public class RegistrationCoordinatorResponseInitialisation
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
        final String uri = baseUri + "/RegistrationResponseService";
        final String secureBaseUri = "https://" + bindAddress + ":" + secureBindPort + serviceURLPath;
        final String secureUri = secureBaseUri + "/RegistrationResponseService";

        serviceRegistry.registerServiceProvider(CoordinationConstants.REGISTRATION_RESPONSE_SERVICE_NAME, uri) ;
        serviceRegistry.registerSecureServiceProvider(CoordinationConstants.REGISTRATION_RESPONSE_SERVICE_NAME, secureUri); ;
    }

    public static void shutdown()
    {
    }
}
