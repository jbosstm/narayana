/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
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
package com.arjuna.mw.wsc.deploy;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arjuna.mw.wsas.utils.Configuration;
import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mw.wscf.protocols.ProtocolRegistry;
import com.arjuna.services.framework.startup.Sequencer;

/**
 * Initialise WSCF.
 * @author kevin
 */
public class WSCFInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     * 
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        // ensure we load any required protocol implementations

        ProtocolRegistry.sharedManager().initialise();

        Sequencer.close(Sequencer.SEQUENCE_WSCOOR10, Sequencer.WEBAPP_WSCF10);
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
    }
}
