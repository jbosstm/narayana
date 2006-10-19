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
package com.arjuna.mw.wsc.deploy;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arjuna.mw.wsas.utils.Configuration;
import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mwlabs.wsc.ContextFactoryMapperImple;
import com.arjuna.wsc.ContextFactoryMapper;

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
     * @message com.arjuna.mw.wsc.deploy.WSCFI_1 [com.arjuna.mw.wsc.deploy.WSCFI_1] - WSCF Initialisation: init failed: 
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        try
        {
            Configuration.initialise("/wscf.xml");
            
            final ContextFactoryMapper wscfImpl = ContextFactoryMapper.getFactory() ;

            wscfImpl.setSubordinateContextFactoryMapper(new ContextFactoryMapperImple());
        }
        catch (Exception exception)
        {
            wscfLogger.arjLoggerI18N.error("com.arjuna.mw.wsc.deploy.WSCFI_1", exception);
        }
        catch (Error error)
        {
            wscfLogger.arjLoggerI18N.error("com.arjuna.mw.wsc.deploy.WSCFI_1", error);
        }
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
    }
}
