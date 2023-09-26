/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.arjuna.webservices.util;

import org.jboss.jbossts.xts.environment.WSCEnvironmentBean;
import org.jboss.jbossts.xts.environment.XTSPropertyManager;

import java.util.Timer;

/**
 * Utility class providing access to a timer and associated properties.
 * @author kevin
 */
public class TransportTimer
{
    /**
     * Daemon timer.
     */
    private static final Timer TIMER = new Timer(true) ;
    
    /**
     * The transport timeout.
     */
    private static long TIMEOUT = 30000 ;
    /**
     * The initial transport period.
     */
    private static long PERIOD = 5000 ;

    /**
     * The maximum transport period.
     */
    private static long MAX_PERIOD = 300000 ;

    /**
     * Get the transport timer.
     * @return The transport timer.
     */
    public static Timer getTimer()
    {
        return TIMER ;
    }
    
    /**
     * Set the transport timeout.
     * @param timeout The transport timeout in milliseconds.
     */
    public static void setTransportTimeout(final long timeout)
    {
       TIMEOUT = timeout ; 
    }
    
    /**
     * Get the transport timeout.
     * @return The transport timeout in milliseconds.
     */
    public static long getTransportTimeout()
    {
        return TIMEOUT ;
    }
    
    /**
     * Set the initial transport period.
     * @param period The transport period in milliseconds.
     */
    public static void setTransportPeriod(final long period)
    {
       PERIOD = period ;
    }
    
    /**
     * Get the transport period.
     * @return The transport period in milliseconds.
     */
    public static long getTransportPeriod()
    {
        return PERIOD ;
    }

    /**
     * Set the maximum transport period for engines which require an exponentially increasing period between
     * message resends.
     * @param period The transport period in milliseconds.
     */
    public static void setMaximumTransportPeriod(final long period)
    {
       MAX_PERIOD = period ;
    }

    /**
     * Get the maximum transport period for engines which require an exponentially increasing period between
     * message resends.
     * @return The transport period in milliseconds.
     */
    public static long getMaximumTransportPeriod()
    {
        if (MAX_PERIOD < PERIOD) {
            return PERIOD;
        }

        return MAX_PERIOD ;
    }

    // initialise timings from configuration values
    static
    {
        WSCEnvironmentBean wscEnvironmentBean = XTSPropertyManager.getWSCEnvironmentBean();
        setTransportPeriod(wscEnvironmentBean.getInitialTransportPeriod());
        setMaximumTransportPeriod(wscEnvironmentBean.getMaximumTransportPeriod());
        setTransportTimeout(wscEnvironmentBean.getTransportTimeout());
    }
}