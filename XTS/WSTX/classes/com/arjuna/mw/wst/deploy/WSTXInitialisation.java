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
package com.arjuna.mw.wst.deploy;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.arjuna.mw.wsas.utils.Configuration;
import com.arjuna.mw.wst.BusinessActivityManager;
import com.arjuna.mw.wst.TransactionManager;
import com.arjuna.mw.wst.UserBusinessActivity;
import com.arjuna.mw.wst.UserTransaction;
import com.arjuna.mw.wstx.logging.wstxLogger;
import com.arjuna.webservices.util.ClassLoaderHelper;

/**
 * Initialise WSTX.
 * @author kevin
 */
public class WSTXInitialisation implements ServletContextListener
{
    /**
     * The name of the WS-T configuration.
     */
    private static final String WS_T_CONFIG = "/wst.xml" ;
    
    /**
     * The context has been initialized.
     * @param servletContextEvent The servlet context event.
     * 
     * @message com.arjuna.mw.wst.deploy.WSTXI_1 [com.arjuna.mw.wst.deploy.WSTXI_1] - WSTX Initialisation: init failed:
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        try
        {
            configure();

            Configuration.initialise("/wstx.xml");
            // Start recovery
//            RecoveryManager.manager() ;
        }
        catch (Exception exception)
        {
            wstxLogger.arjLoggerI18N.error("com.arjuna.mw.wst.deploy.WSTXI_1", exception);
        }
        catch (Error error)
        {
            wstxLogger.arjLoggerI18N.error("com.arjuna.mw.wst.deploy.WSTXI_21", error);
        }
    }
    
    /**
     * Configure WS-T.
     * 
     * @message com.arjuna.mw.wst.deploy.WSTXI_21 [com.arjuna.mw.wst.deploy.WSTXI_21] - {0} not found.
     * @message com.arjuna.mw.wst.deploy.WSTXI_22 [com.arjuna.mw.wst.deploy.WSTXI_22] - Failed to create document: {0}
     * @message com.arjuna.mw.wst.deploy.WSTXI_23 [com.arjuna.mw.wst.deploy.WSTXI_23] - Missing WSTX Initialisation
     */
    private void configure()
        throws Exception
    {
        // mostly original JNDI binder code.  Should be tidied up.
        final InputStream is = ClassLoaderHelper.getResourceAsStream(getClass(), WS_T_CONFIG) ;

        if (is == null)
        {
            final String pattern = wstxLogger.log_mesg.getString("com.arjuna.mw.wst.deploy.WSTXI_21") ;
            throw new FileNotFoundException(MessageFormat.format(pattern, new Object[] {WS_T_CONFIG}));
        }
        
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance() ;
        final DocumentBuilder builder = factory.newDocumentBuilder() ;
        final Document doc = builder.parse(is);
        
        if (doc == null)
        {
            final String pattern = wstxLogger.log_mesg.getString("com.arjuna.mw.wst.deploy.WSTXI_22") ;
            throw new FileNotFoundException(MessageFormat.format(pattern, new Object[] {WS_T_CONFIG}));
        }

        final Element docElem = doc.getDocumentElement() ;
        final String userTx = getService(docElem, "UserTransaction") ;
        final String txManager = getService(docElem, "TransactionManager") ;
        final String userBa = getService(docElem, "UserBusinessActivity") ;
        final String baManager = getService(docElem, "BusinessActivityManager") ;

        if ((userTx == null) || (txManager == null) || (userBa == null) || (baManager == null))
        {
            throw new FileNotFoundException(wstxLogger.log_mesg.getString("com.arjuna.mw.wst.deploy.WSTXI_23"));
        }
        UserTransaction.setUserTransaction((UserTransaction)ClassLoaderHelper.forName(getClass(), userTx).newInstance()) ;
        TransactionManager.setTransactionManager((TransactionManager)ClassLoaderHelper.forName(getClass(), txManager).newInstance()) ;
        UserBusinessActivity.setUserBusinessActivity((UserBusinessActivity)ClassLoaderHelper.forName(getClass(), userBa).newInstance()) ;
        BusinessActivityManager.setBusinessActivityManager((BusinessActivityManager)ClassLoaderHelper.forName(getClass(), baManager).newInstance()) ;
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
