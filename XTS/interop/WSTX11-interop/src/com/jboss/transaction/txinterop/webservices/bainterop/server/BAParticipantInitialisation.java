/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package com.jboss.transaction.txinterop.webservices.bainterop.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContext;

import com.arjuna.webservices11.ServiceRegistry;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;
import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

/**
 * Initialise the interop initiator service.
 * @author kevin
 */
public class BAParticipantInitialisation implements ServletContextListener
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
        final String uri = baseURI + "/BAParticipantService";

        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        serviceRegistry.registerServiceProvider(BAInteropConstants.SERVICE_PARTICIPANT, uri) ;
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
        serviceRegistry.removeServiceProvider(BAInteropConstants.SERVICE_PARTICIPANT) ;
    }
}