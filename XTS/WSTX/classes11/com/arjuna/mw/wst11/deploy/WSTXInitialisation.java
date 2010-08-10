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

import com.arjuna.mw.wsas.utils.Configuration;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.mw.wst11.*;
import com.arjuna.services.framework.startup.Sequencer;
import com.arjuna.webservices.util.ClassLoaderHelper;
import com.arjuna.wsc.common.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.FileNotFoundException;
import java.io.InputStream;

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
                   fixCoordinatorURL();

                   listener.configure();
                   // Start recovery
//            RecoveryManager.manager() ;
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
        // this is the last WST callback to be initialised so close the list
        Sequencer.close(Sequencer.SEQUENCE_WSCOOR11, Sequencer.WEBAPP_WSTX11);
    }

    /**
     * Configure WS-T.
     *
     */
    private void configure()
        throws Exception
    {
        final String userTx = System.getProperty("org.jboss.jbossts.xts.wsat11.UserTransaction");
        final String txManager = System.getProperty("org.jboss.jbossts.xts.wsat11.TransactionManager") ;
        final String userBa = System.getProperty("org.jboss.jbossts.xts.wsba11.UserBusinessActivity") ;
        final String baManager = System.getProperty("org.jboss.jbossts.xts.wsba11.BusinessActivityManager") ;

        if ((userTx == null) || (txManager == null) || (userBa == null) || (baManager == null))
        {
            throw new FileNotFoundException(wstxLogger.i18NLogger.get_mw_wst11_deploy_WSTXI_23());
        }
        UserTransaction.setUserTransaction((UserTransaction)ClassLoaderHelper.forName(getClass(), userTx).newInstance()) ;
        TransactionManager.setTransactionManager((TransactionManager)ClassLoaderHelper.forName(getClass(), txManager).newInstance()) ;
        UserBusinessActivity.setUserBusinessActivity((UserBusinessActivity)ClassLoaderHelper.forName(getClass(), userBa).newInstance()) ;
        // we only have one choice for the 1.1 business activity manager
        // BusinessActivityManager.setBusinessActivityManager(BusinessActivityManagerImple.class.newInstance());
        BusinessActivityManager.setBusinessActivityManager((BusinessActivityManager)ClassLoaderHelper.forName(getClass(), baManager).newInstance());
    }

    private final String SERVER_BIND_ADDRESS_KEY = "server.bind.address";

    private final String JBOSS_WEB_BIND_PORT_KEY = "jboss.web.bind.port";

    private void fixCoordinatorURL()
    {
        // ok, if we just loaded a coordinator URL and one was already defined on
        // the command line then reinstate the command line version
        String commandLineCoordinatrURL  = System.getProperty(com.arjuna.wsc.common.Environment.XTS11_COMMAND_LINE_COORDINATOR_URL);
        if (commandLineCoordinatrURL != null) {
            System.setProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR11_URL, commandLineCoordinatrURL);
        }

        // if the coordinatorURL contains the symbolic names server.bind.address
        // or jboss.web.bind.port then we must substitute these with the actual
        // bind address and jboss web http port

        String coordinatorURL = System.getProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR11_URL);

        if (coordinatorURL != null) {
            boolean updated = false;
            int idx = coordinatorURL.indexOf(SERVER_BIND_ADDRESS_KEY);
            if (idx >= 0) {
                String bindAddress = System.getProperty(Environment.XTS11_BIND_ADDRESS);
                if (bindAddress == null) {
                    bindAddress = "127.0.0.1";
                }
                coordinatorURL = coordinatorURL.substring(0, idx) + bindAddress + coordinatorURL.substring(idx + SERVER_BIND_ADDRESS_KEY.length());
                updated = true;
            }

            idx = coordinatorURL.indexOf(JBOSS_WEB_BIND_PORT_KEY);
            if (idx >= 0) {
                String bindPort = System.getProperty(Environment.XTS11_BIND_PORT);
                if (bindPort == null) {
                    bindPort = "8080";
                }
                coordinatorURL = coordinatorURL.substring(0, idx) + bindPort + coordinatorURL.substring(idx + JBOSS_WEB_BIND_PORT_KEY.length());
                updated = true;
            }

            if (updated) {
                System.setProperty(com.arjuna.mw.wst.common.Environment.COORDINATOR11_URL, coordinatorURL);
            }
        }
    }

    /**
     * Get the specified service.
     * @param root The root element.
     * @param name The name of the service.
     * @return The service name or null if not present.
     */
    private static String getService(final Node root, final String name)
    {
        final NodeList children = root.getChildNodes();

        for (int i = 0; i < children.getLength(); i++)
        {
            final Node item = children.item(i) ;

            if ("service".equals(item.getNodeName()))
            {
                final Element type = (Element)item;

                if (name.equals(type.getAttribute("name")))
                {
                    return getImplementation(type);
                }
            }
        }

        return null;
    }

    /**
     * Get the specified service.
     * @param service The service element.
     * @return The service name or null if not present.
     */
    private static String getImplementation(final Node service)
    {
        final NodeList children = service.getChildNodes();

        for (int i = 0; i < children.getLength(); i++)
        {
            final Node item = children.item(i);

            if ("parameter".equals(item.getNodeName()))
            {
                final Element type = (Element)item;

                if ("className".equals(type.getAttribute("name")))
                {
                    return type.getAttribute("value");
                }
            }
        }

        return null;
    }


    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
//        RecoveryManager.manager().stop() ;
    }
}