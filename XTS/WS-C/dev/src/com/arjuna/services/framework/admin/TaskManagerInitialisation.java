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
package com.arjuna.services.framework.admin;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.arjuna.services.framework.task.TaskManager;
import com.arjuna.webservices.logging.WSCLogger;

/**
 * Context listener used to initialise the Task Manager.
 * @author kevin
 */
public class TaskManagerInitialisation implements ServletContextListener
{
    /**
     * The context has been initialized.
     * @param servletContextEvent The sevlet context event.
     * @message com.arjuna.services.framework.admin.TaskManagerInitialisation_1 [com.arjuna.services.framework.admin.TaskManagerInitialisation_1] -
     *      Invalid minimum worker count.
     * @message com.arjuna.services.framework.admin.TaskManagerInitialisation_2 [com.arjuna.services.framework.admin.TaskManagerInitialisation_2] -
     *      Invalid maximum worker count.
     */
    public void contextInitialized(final ServletContextEvent servletContextEvent)
    {
        final ServletContext servletContext = servletContextEvent.getServletContext() ;
        final String minWorkerCountParam = servletContext.getInitParameter("TaskManager.minWorkerCount") ;
        final String maxWorkerCountParam = servletContext.getInitParameter("TaskManager.maxWorkerCount") ;
        
        final TaskManager taskManager = TaskManager.getManager() ;
        if (minWorkerCountParam != null)
        {
            try
            {
                final int minWorkerCount = Integer.parseInt(minWorkerCountParam) ;
                taskManager.setMinimumWorkerCount(minWorkerCount) ;
            }
            catch (final NumberFormatException nfe)
            {
                WSCLogger.arjLoggerI18N.debug("com.arjuna.services.framework.admin.TaskManagerInitialisation_1") ;
            }
        }
        if (maxWorkerCountParam != null)
        {
            try
            {
                final int maxWorkerCount = Integer.parseInt(maxWorkerCountParam) ;
                taskManager.setMaximumWorkerCount(maxWorkerCount) ;
            }
            catch (final NumberFormatException nfe)
            {
                WSCLogger.arjLoggerI18N.debug("com.arjuna.services.framework.admin.TaskManagerInitialisation_2") ;
            }
        }
    }

    /**
     * The context is about to be destroyed.
     * @param servletContextEvent The servlet context event.
     */
    public void contextDestroyed(final ServletContextEvent servletContextEvent)
    {
        TaskManager.getManager().shutdown() ;
    }
}
