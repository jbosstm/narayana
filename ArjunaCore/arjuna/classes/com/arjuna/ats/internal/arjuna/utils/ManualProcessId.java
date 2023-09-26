/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.utils;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.FatalError;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Obtains a unique value to represent the process id via configuration.
 *
 * Other options include ...
 *
 * int pid = Integer.parseInt((new File("/proc/self")).getCanonicalFile().getName());  // linux specific
 *
 * Un*x specific ...
 *
 * String[] cmd = {"/bin/sh", "-c", "echo $PPID"};
 *                     Process proc = Runtime.getRuntime().exec(cmd);
 *
 *                     Field field = proc.getClass().getDeclaredField("pid");
 *                       field.setAccessible(true);
 *
 *                       _pid = field.getInt(proc);  // although this is the child pid!
 *
 *                       proc.destroy();
 *
 * byte[] ba = new byte[100];
 * String[] cmd = {"/bin/sh", "-c", "echo $PPID"};
 * Process proc = Runtime.getRuntime().exec(cmd);
 * proc.getInputStream().read(ba);
 * System.out.println(new String(ba));
 *
 * http://java.sun.com/javase/6/docs/jdk/api/attach/spec/com/sun/tools/attach/VirtualMachine.html  // JDK 6 only
 *
 * and ...
 *
 * MonitoredHost host = MonitoredHost.getMonitoredHost(null);
 *
 * for (Object activeVmPid : host.activeVms())
 * int pid = (Integer) activeVmPid;
 *
 * and ...
 *
 * Process proc = Runtime.getRuntime().exec(cmd);
 *
 * Field field = proc.getClass().getDeclaredField("pid");
 * field.setAccessible(true);
 *
 * _pid = field.getInt(proc);  // although this is the child pid!
 */

public class ManualProcessId implements com.arjuna.ats.arjuna.utils.Process
{

    /**
     * @return the process id. This had better be unique between processes on
     *         the same machine. If not we're in trouble!
     */

    public int getpid ()
    {
        synchronized (ManualProcessId._lock)
        {
            if (_pid == -1)
            {
                _pid = arjPropertyManager.getCoreEnvironmentBean().getPid();
            }
        }

        if (_pid == -1)
            throw new FatalError(tsLogger.i18NLogger.get_utils_ManualProcessId_1());

        return _pid;
    }

    private static final Object _lock = new Object();

    private int _pid = -1;
}