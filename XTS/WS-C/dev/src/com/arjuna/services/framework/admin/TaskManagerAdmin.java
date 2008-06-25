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
package com.arjuna.services.framework.admin ;

import com.arjuna.services.framework.task.TaskManager;

/**
 * The task manager administration MBean.
 * @author kevin
 */
public class TaskManagerAdmin implements TaskManagerAdminMBean
{
    /**
     * Set the minimum worker count for the pool.
     * 
     * @param minimumWorkerCount The minimum worker count.
     */
    public void setMinimumWorkerCount(final int minimumWorkerCount)
    {
        TaskManager.getManager().setMinimumWorkerCount(minimumWorkerCount) ;
    }

    /**
     * Get the minimum worker count for the pool.
     * 
     * @return The minimum worker count.
     */
    public int getMinimumWorkerCount()
    {
        return TaskManager.getManager().getMinimumWorkerCount() ;
    }

    /**
     * Set the maximum worker count for the pool.
     * 
     * @param maximumWorkerCount The maximum worker count.
     */
    public void setMaximumWorkerCount(final int maximumWorkerCount)
    {
        TaskManager.getManager().setMaximumWorkerCount(maximumWorkerCount) ;
    }

    /**
     * Get the maximum worker count for the pool.
     * 
     * @return The maximum worker count.
     */
    public int getMaximumWorkerCount()
    {
        return TaskManager.getManager().getMaximumWorkerCount() ;
    }

    /**
     * Get the current worker count for the pool.
     * 
     * @return The current worker count.
     */
    public int getWorkerCount()
    {
        return TaskManager.getManager().getWorkerCount() ;
    }

    /**
     * Close all threads and reset the task list. This method waits until all
     * threads have finished before returning.
     */
    public void shutdown()
    {
        TaskManager.getManager().shutdown() ;
    }
}
