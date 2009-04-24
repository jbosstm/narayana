package com.jboss.transaction.txinterop.webservices.bainterop.server;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletContext;

import com.arjuna.webservices11.ServiceRegistry;
import com.jboss.transaction.txinterop.webservices.atinterop.ATInteropConstants;
import com.jboss.transaction.txinterop.webservices.bainterop.BAInteropConstants;

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
        String baseURI = context.getInitParameter("BaseURI");
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