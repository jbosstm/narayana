/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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