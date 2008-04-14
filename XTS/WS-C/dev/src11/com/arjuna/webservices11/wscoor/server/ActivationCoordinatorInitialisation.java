/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, JBoss Inc., and individual contributors as indicated
 * by the @authors tag.  All rights reserved. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU General Public License, v. 2.0.
 * This program is distributed in the hope that it will be useful, but WITHOUT A 
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A 
 * PARTICULAR PURPOSE.  See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License,
 * v. 2.0 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
package com.arjuna.webservices11.wscoor.server;

import com.arjuna.services.framework.startup.Sequencer;
import com.arjuna.webservices11.wscoor.CoordinationConstants;
import com.arjuna.webservices11.ServiceRegistry;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Activate the Activation Coordinator service
 * @author kevin
 */
public class ActivationCoordinatorInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        ServletContext context = servletContextEvent.getServletContext();
        String baseURI = context.getInitParameter("BaseURI");
        final String uri = baseURI + "/ActivationService";

        Sequencer.Callback callback = new Sequencer.Callback(Sequencer.SEQUENCE_WSCOOR11, Sequencer.WEBAPP_WSC11) {
           public void run() {
               // TODO if we rely upon JaxWS to automatically publish implementation classes it will only
               // do so under a URL based on service name. which means we cannot define a service using
               // multiple port bindings because thsi causes a namespace clash
               final ServiceRegistry serviceRegistry = ServiceRegistry.getRegistry() ;
               serviceRegistry.registerServiceProvider(CoordinationConstants.ACTIVATION_SERVICE_NAME, uri) ;
           }
        };
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
    }
}
