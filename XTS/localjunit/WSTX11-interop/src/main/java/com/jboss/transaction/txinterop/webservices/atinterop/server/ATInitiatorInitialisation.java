/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.txinterop.webservices.atinterop.server;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContext;

import com.arjuna.webservices11.ServiceRegistry;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Initialise the interop initiator service.
 * @author kevin
 */
public class ATInitiatorInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        ServletContext context = servletContextEvent.getServletContext();
        WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
        String bindAddress = wscEnvironmentBean.getBindAddress11();
        int bindPort = wscEnvironmentBean.getBindPort11();
        String baseURI = "http://" + bindAddress + ":" +  bindPort + "/interop11";
        final String uri = baseURI + "/ATInitiatorService";

        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        serviceRegistry.registerServiceProvider(ATInteropConstants.SERVICE_INITIATOR, uri) ;
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        serviceRegistry.removeServiceProvider(ATInteropConstants.SERVICE_INITIATOR) ;
    }
}