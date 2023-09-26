/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.services.framework.task;

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
                WSCLogger.i18NLogger.error_services_framework_task_TaskWorker_run_1(th);
            }
        }
    }
}