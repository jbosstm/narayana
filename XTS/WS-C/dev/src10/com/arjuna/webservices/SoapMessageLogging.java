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
package com.arjuna.webservices;

import java.util.ArrayList;
import java.util.List;

/**
 * Class providing SOAP message logging.
 * @author kevin
 */
public class SoapMessageLogging
{
    /**
     * The thread local message log.
     */
    private static final ThreadLocal MESSAGE_LOG = new ThreadLocal() ;
    /**
     * The thread local message log enabled flag.
     */
    private static final ThreadLocal MESSAGE_LOG_ENABLED = new ThreadLocal() {
        protected Object initialValue() { return Boolean.FALSE; }
    };
    
    /**
     * Clear the log for the current thread.
     */
    public static void clearThreadLog()
    {
        MESSAGE_LOG.set(null) ;
    }
    
    /**
     * Get the thread log.
     * @return The thread log.
     */
    public static String[] getThreadLog()
    {
        final Object value = MESSAGE_LOG.get() ;
        if (value == null)
        {
            return null ;
        }
        final List list = (List)value ;
        return (String[]) list.toArray(new String[list.size()]) ;
    }
    
    /**
     * Append a message to the thread log.
     * @param message The thread message to append.
     */
    public static void appendThreadLog(final String message)
    {
        final Object value = MESSAGE_LOG.get() ;
        final List list ;
        if (value == null)
        {
            list = new ArrayList() ;
            MESSAGE_LOG.set(list) ;
        }
        else
        {
            list = (List)value ;
        }
        list.add(message) ;
    }
    
    /**
     * Is the thread logging enabled?
     * @return true if enabled, false otherwise.
     */
    public static boolean isThreadLogEnabled()
    {
        return ((Boolean)MESSAGE_LOG_ENABLED.get()).booleanValue() ;
    }
    
    /**
     * Set the thread log enable flag.
     * @param isEnabled True if enabled, false otherwise.
     */
    public static void setThreadLogEnabled(final boolean isEnabled)
    {
        MESSAGE_LOG_ENABLED.set(isEnabled ? Boolean.TRUE : Boolean.FALSE) ;
    }
}
