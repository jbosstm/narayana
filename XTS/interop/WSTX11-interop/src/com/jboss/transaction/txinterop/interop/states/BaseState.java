/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, Red Hat Middleware LLC, and individual contributors
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
package com.jboss.transaction.txinterop.interop.states;

import org.xml.sax.ContentHandler;

import com.jboss.transaction.txinterop.proxy.ProxyConversationState;

/**
 * The base state class for proxy conversations.
 */
abstract class BaseState implements ProxyConversationState
{
    /**
     * The complete flag.
     */
    private boolean complete ;
    /**
     * The success flag.
     */
    private boolean success ;
    
    /**
     * Mark the conversation as successfully completed.
     */
    protected synchronized void success()
    {
        if (!complete)
        {
            System.out.println("KEV: success!") ;
            complete = true ;
            success = true ;
            notifyAll() ;
        }
        else
        {
            System.out.println("KEV: AAAAAARRRRRRRGGGGGGGHHHHHHHHH, multiple successes called for conversation!") ;
        }
    }
    
    /**
     * Wait a specified period for the conversation to complete.
     * @param timeout The timeout period of the conversation.
     */
    public synchronized void waitForCompletion(final long timeout)
    {
        if (!complete && (timeout > 0))
        {
            final long end = System.currentTimeMillis() + timeout ;
            do
            {
                final long delay = end - System.currentTimeMillis() ;
                if (delay > 0)
                {
                    try
                    {
                        wait(delay) ;
                    }
                    catch (final InterruptedException ie) {} // ignore
                }
                else
                {
                    break ;
                }
            }
            while(!complete) ;
        }
        
        complete = true ;
    }
    
    /**
     * Has the conversation complete?
     * @return true if the conversation has complete, false otherwise.
     */
    public synchronized boolean isComplete()
    {
        return complete ;
    }
    
    /**
     * Was the conversation successful?
     * @return true if the conversation was successful, false otherwise.
     */
    public synchronized boolean isSuccessful()
    {
        return success ;
    }

    /**
     * Get the Handler for rewriting the XML.
     * @param nextHandler The next handler in the sequence.
     * @return The handler or null if no rewriting required.
     */
    public ContentHandler getHandler(final ContentHandler nextHandler)
    {
	return null ;
    }
}
