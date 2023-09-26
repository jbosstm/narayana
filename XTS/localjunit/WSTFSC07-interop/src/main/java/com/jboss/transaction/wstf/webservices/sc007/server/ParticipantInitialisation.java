/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.jboss.transaction.wstf.webservices.sc007.server;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletContext;

import com.arjuna.webservices11.ServiceRegistry;
import com.jboss.transaction.wstf.webservices.sc007.InteropConstants;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Initialise the interop initiator service.
 * @author kevin
 */
public class ParticipantInitialisation implements ServletContextListener
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
        String baseURI = "http://" + bindAddress + ":" +  bindPort + "/sc007";
        final String uri = baseURI + "/ParticipantService";

        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        serviceRegistry.registerServiceProvider(InteropConstants.SERVICE_PARTICIPANT, uri) ;
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        serviceRegistry.removeServiceProvider(InteropConstants.SERVICE_PARTICIPANT) ;
    }
}