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
package com.arjuna.webservices.wscoor;

import com.arjuna.webservices.MessageContext;

/**
 * The coordination context.
 * @author kevin
 */
public class CoordinationContext
{
    /**
     * The key used for the coordination context within a message exchange.
     */
    private static final byte[] COORDINATION_CONTEXT_PROPERTY = new byte[0] ;
    /**
     * The coordination context associated with the thread.
     */
    private static final ThreadLocal THREAD_CONTEXT = new ThreadLocal() ;
    
    /**
     * Get the coordination context from the message context if present.
     * @param messageContext The message context.
     * @return The coordination context or null if not present.
     */
    public static CoordinationContextType getContext(final MessageContext messageContext)
    {
        return (CoordinationContextType)messageContext.getProperty(COORDINATION_CONTEXT_PROPERTY) ;
    }

    /**
     * Set the coordination context for the message context.
     * @param messageContext The message context.
     * @param coordinationContext The coordination context.
     */
    public static void setContext(final MessageContext messageContext, final CoordinationContextType coordinationContext)
    {
        messageContext.setProperty(COORDINATION_CONTEXT_PROPERTY, coordinationContext) ;
    }
    
    /**
     * Get the coordination context from the current thread if present.
     * @return The coordination context or null if not present.
     */
    public static CoordinationContextType getThreadContext()
    {
        return (CoordinationContextType)THREAD_CONTEXT.get() ;
    }

    /**
     * Set the coordination context for the current thread.
     * @param coordinationContext The coordination context.
     */
    public static void setThreadContext(final CoordinationContextType coordinationContext)
    {
        THREAD_CONTEXT.set(coordinationContext) ;
    }
}
