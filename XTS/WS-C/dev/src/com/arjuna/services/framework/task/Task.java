/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package com.arjuna.services.framework.task;

/**
 * The task interface for tasks scheduled by the task manager.
 * 
 * @author kevin
 */
public interface Task
{
    /**
     * Execute the task.
     */
    public void executeTask() ;
}