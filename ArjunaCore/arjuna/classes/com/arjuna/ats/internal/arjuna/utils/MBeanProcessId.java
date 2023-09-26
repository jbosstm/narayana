/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.utils;

import java.lang.management.ManagementFactory;

import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Obtains a unique value to represent the process id via ManagementFactory.getRuntimeMXBean().getName().
 *
 * WARNING: use with care because the contents of getName may change between OSes and versions of
 * JDK.
 */

public class MBeanProcessId implements com.arjuna.ats.arjuna.utils.Process
{

    /**
     * @return the process id. This had better be unique between processes on
     *         the same machine. If not we're in trouble!
     */

    public int getpid ()
    {
        synchronized (MBeanProcessId._lock)
        {
            if (_pid == -1)
            {
                String name = ManagementFactory.getRuntimeMXBean().getName();
                String[] parsed = name.split("@");

                try
                {
                    _pid = Integer.valueOf(parsed[0]);
                }
                catch (final Exception ex)
                {
                    throw new FatalError(tsLogger.i18NLogger.get_utils_MBeanProcessId_2() + " "+name, ex);
                }
            }
        }

        if (_pid == -1)
            throw new FatalError(tsLogger.i18NLogger.get_utils_MBeanProcessId_1());

        return _pid;
    }

    private static final Object _lock = new Object();

    private int _pid = -1;
}