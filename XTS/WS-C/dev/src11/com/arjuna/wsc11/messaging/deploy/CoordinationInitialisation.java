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
package com.arjuna.wsc11.messaging.deploy;

import com.arjuna.services.framework.startup.Sequencer;
import com.arjuna.webservices11.wscoor.processors.ActivationCoordinatorProcessor;
import com.arjuna.webservices11.wscoor.processors.RegistrationCoordinatorProcessor;
import com.arjuna.wsc11.messaging.ActivationCoordinatorProcessorImpl;
import com.arjuna.wsc11.messaging.RegistrationCoordinatorProcessorImpl;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initialise the coordination services.
 * @author kevin
 */
public class CoordinationInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        Sequencer.Callback callback = new Sequencer.Callback(Sequencer.SEQUENCE_WSCOOR11, Sequencer.WEBAPP_WSC11) {
           public void run() {
               ActivationCoordinatorProcessor.setCoordinator(new ActivationCoordinatorProcessorImpl()) ;
               RegistrationCoordinatorProcessor.setCoordinator(new RegistrationCoordinatorProcessorImpl()) ;
           }
        };
        // this is the last WSC callback to be initialised so close the list
        Sequencer.close(Sequencer.SEQUENCE_WSCOOR11, Sequencer.WEBAPP_WSC11);
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
    }
}