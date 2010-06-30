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
package com.arjuna.mw.wsc11.deploy;

import com.arjuna.mw.wsas.utils.Configuration;
import com.arjuna.mw.wscf.logging.wscfLogger;
import com.arjuna.mwlabs.wsc11.ContextFactoryMapperImple;
import com.arjuna.services.framework.startup.Sequencer;
import com.arjuna.wsc11.ContextFactoryMapper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

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
        Sequencer.Callback callback = new Sequencer.Callback(Sequencer.SEQUENCE_WSCOOR11, Sequencer.WEBAPP_WSCF11) {
           public void run() {
               try
               {
                   Configuration.initialise("/wscf11.xml");

                   final ContextFactoryMapper WSCFImpl = ContextFactoryMapper.getFactory() ;

                   WSCFImpl.setSubordinateContextFactoryMapper(new ContextFactoryMapperImple());
               }
               catch (Exception exception) {
                   wscfLogger.i18NLogger.error_mw_wsc11_deploy_WSCFI_1(exception);
               }
               catch (Error error)
               {
                   wscfLogger.i18NLogger.error_mw_wsc11_deploy_WSCFI_1(error);
               }
           }
        };
        // this is the last WSCF callback to be initialised so close the list
        Sequencer.close(Sequencer.SEQUENCE_WSCOOR11, Sequencer.WEBAPP_WSCF11);

    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
    }
}