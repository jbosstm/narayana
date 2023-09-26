/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices.base.processors;

/**
 * Base class for callbacks.
 * @author kevin
 */
public abstract class Callback
{
    /**
     * The triggered flag.
     */
    private boolean triggered ;
    /**
     * The failed flag.
     */
    private boolean failed ;
    
    /**
     * Has the callback triggered.
     * @return true if triggered, false otherwise.
     */
    public final synchronized boolean hasTriggered()
    {
        return triggered ;
    }
    
    /**
     * Set the triggered flag.
     */
    public final synchronized void setTriggered()
    {
        triggered = true ;
        notifyAll() ;
    }
    
    /**
     * Has the callback failed.
     * @return true if failed, false otherwise.
     */
    public final synchronized boolean hasFailed()
    {
        return failed ;
    }
    
    /**
     * Set the failed flag.
     */
    public final synchronized void setFailed()
    {
        failed = true ;
        notifyAll() ;
    }
    
    /**
     * Wait until the callback has triggered or failed.
     */
    public final void waitUntilTriggered()
    {
        waitUntilTriggered(0) ;
    }
    
    /**
     * Wait until the callback has triggered or failed.
     * @param delay the timeout period in milliseconds.
     */
    public final synchronized void waitUntilTriggered(final long delay)
    {
        final long endTime = (delay <= 0 ? Long.MAX_VALUE : System.currentTimeMillis() + delay) ;
        long now = System.currentTimeMillis() ;
        while((endTime > now) && !(triggered || failed))
        {
            try
            {
                wait(endTime - now) ;
            }
            catch (final InterruptedException ie) {}
            now = System.currentTimeMillis() ;
        }
    }
}