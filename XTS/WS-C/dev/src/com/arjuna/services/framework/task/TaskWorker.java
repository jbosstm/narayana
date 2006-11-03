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
package com.arjuna.services.framework.task ;

import com.arjuna.webservices.logging.WSCLogger;

/**
 * Class responsible for executing tasks.
 * 
 * @author kevin
 */
public class TaskWorker implements Runnable
{
    /**
     * The associated task manager.
     */
    private final TaskManager taskManager ;

    /**
     * Construct the task worker.
     * 
     * @param taskManager The task manager.
     */
    TaskWorker(final TaskManager taskManager)
    {
        this.taskManager = taskManager ;
    }

    /**
     * Execute the tasks.
     * @message com.arjuna.services.framework.task.TaskWorker.run_1 [com.arjuna.services.framework.task.TaskWorker.run_1] -
     *      Unhandled error executing task
     */
    public void run()
    {
        while(true)
        {
            final Task task = taskManager.getTask() ;

            if (task == null)
            {
                break ;
            }

            try
            {
                task.executeTask() ;
            }
            catch (final Throwable th)
            {
                WSCLogger.arjLoggerI18N.error("com.arjuna.services.framework.task.TaskWorker.run_1", th) ;
            }
        }
    }
}
