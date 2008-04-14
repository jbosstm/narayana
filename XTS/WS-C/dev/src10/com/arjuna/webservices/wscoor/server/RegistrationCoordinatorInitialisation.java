/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
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
package com.arjuna.webservices.wscoor.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arjuna.webservices.HandlerRegistry;
import com.arjuna.webservices.SoapRegistry;
import com.arjuna.webservices.SoapService;
import com.arjuna.webservices.wsaddr.policy.AddressingPolicy;
import com.arjuna.webservices.wsarj.policy.ArjunaPolicy;
import com.arjuna.webservices.wscoor.CoordinationConstants;
import com.arjuna.webservices.wscoor.policy.RegistrationCoordinatorPolicy;
import com.arjuna.services.framework.startup.Sequencer;

/**
 * Activate the Registration Coordinator service
 * @author kevin
 */
public class RegistrationCoordinatorInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        Sequencer.Callback callback = new Sequencer.Callback(Sequencer.SEQUENCE_WSCOOR10, Sequencer.WEBAPP_WSC10) {
           public void run() {
               final HandlerRegistry handlerRegistry = new HandlerRegistry() ;

               // Add WS-Addressing
               AddressingPolicy.register(handlerRegistry) ;
               // Add Arjuna handlers
               ArjunaPolicy.register(handlerRegistry) ;
               // Add Registration coordinator.
               RegistrationCoordinatorPolicy.register(handlerRegistry) ;

               final SoapRegistry soapRegistry = SoapRegistry.getRegistry() ;
               soapRegistry.registerSoapService(CoordinationConstants.SERVICE_REGISTRATION_COORDINATOR,
                       new SoapService(handlerRegistry)) ;
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
