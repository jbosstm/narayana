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
package com.arjuna.mw.wst11.deploy;

import com.arjuna.common.internal.util.propertyservice.BeanPopulator;
import org.jboss.jbossts.xts.environment.WSTEnvironmentBean;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mw.wst11.*;
import com.arjuna.services.framework.startup.Sequencer;
import com.arjuna.webservices.util.ClassLoaderHelper;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.FileNotFoundException;

/**
 * Initialise WSTX.
 * @author kevin
 */
public class WSTXInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     *
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        final WSTXInitialisation listener = this;

        Sequencer.Callback callback = new Sequencer.Callback(Sequencer.SEQUENCE_WSCOOR11, Sequencer.WEBAPP_WSTX11) {
           public void run() {
               try
               {
                   listener.configure();
               }
               catch (Exception exception) {
                   wstxLogger.i18NLogger.error_mw_wst11_deploy_WSTXI_1(exception);
               }
               catch (Error error)
               {
                   wstxLogger.i18NLogger.error_mw_wst11_deploy_WSTXI_1(error);
               }
           }
        };
    }

    /**
     * Configure all configured WSTX client and participant implementations.
     *
     */
    private void configure()
        throws Exception
    {
        WSTEnvironmentBean wstEnvironmentBean = BeanPopulator.getSingletonInstance(WSTEnvironmentBean.class);
        final String userTx = wstEnvironmentBean.getUserTransaction11();
        final String txManager = wstEnvironmentBean.getTransactionManager11();
        final String userBa = wstEnvironmentBean.getUserBusinessActivity11();
        final String baManager = wstEnvironmentBean.getBusinessActivityManager11();

        // we only load classes which have been configured
        
        if (userTx != null) {
            UserTransaction.setUserTransaction((UserTransaction)ClassLoaderHelper.forName(getClass(), userTx).newInstance()) ;
        }
        if (txManager != null) {
            TransactionManager.setTransactionManager((TransactionManager)ClassLoaderHelper.forName(getClass(), txManager).newInstance()) ;
        }
        if (userBa != null) {
            UserBusinessActivity.setUserBusinessActivity((UserBusinessActivity)ClassLoaderHelper.forName(getClass(), userBa).newInstance()) ;
        }
        if (baManager != null) {
            BusinessActivityManager.setBusinessActivityManager((BusinessActivityManager)ClassLoaderHelper.forName(getClass(), baManager).newInstance());
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