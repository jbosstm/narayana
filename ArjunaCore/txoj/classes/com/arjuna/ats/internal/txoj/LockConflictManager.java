/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors 
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors. 
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
/*
 * Copyright (C) 2000, 2001,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: LockConflictManager.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.txoj;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import com.arjuna.ats.txoj.LockManager;

/**
 * An instance of this class is used to determine what to do in the event of a
 * lock conflict for a given object. If the timeout and retry values are >=0
 * then we use them to sleep the thread which tried to get the lock. If the
 * retry value is -100 (LockManager.waitTotalTimeout) then the thread will block
 * for up to the total timeout and be signaled either when the timeout occurs,
 * or when the lock is actually released.
 */

public class LockConflictManager
{
    public LockConflictManager (ReentrantLock instance)
    {
        _lock = new Object();
        _instance = instance;
    }

    /**
     * Wait for the specified timeout (in milliseconds) and retry. We may either sleep the thread,
     * or block it on a mutex. Returns the time taken to wait.
     */

    /*
     * Sometimes we must sleep when holding the LockManager mutex. In those situations
     * we really want to release the mutex, sleep, and then re-acquire it
     * so that other threads can make progress.
     *
     * This routine *must* only be called after having acquired the mutex!
     */
    
    public int wait (int retry, int waitTime)
    {
        /*
         * Release the mutex on the LockManager instance.
         */
        
        boolean lock = false;
        
        if (_instance.isHeldByCurrentThread())
        {
            _instance.unlock();
            lock = true;
        }
            
        Date d1 = Calendar.getInstance().getTime();
        
        if (retry == LockManager.waitTotalTimeout)
        {
            try
            {
                Thread.sleep(waitTime);
            }
            catch (final Throwable ex)
            {
            }
        }
        else
        {
            synchronized (_lock)
            {
                try
                {
                    _lock.wait(waitTime);
                }
                catch (InterruptedException e)
                {
                }
            }
        }

        Date d2 = Calendar.getInstance().getTime();
        
        if (lock)
            _instance.lock();

        return (int) (d2.getTime() - d1.getTime());
    }

    /**
     * Signal that the lock has been released.
     */

    public void signal ()
    {
        synchronized (_lock)
        {
            _lock.notifyAll();
        }
    }

    private Object _lock;
    private ReentrantLock _instance;
}
