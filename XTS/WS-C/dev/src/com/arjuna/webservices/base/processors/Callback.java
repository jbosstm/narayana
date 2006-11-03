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
