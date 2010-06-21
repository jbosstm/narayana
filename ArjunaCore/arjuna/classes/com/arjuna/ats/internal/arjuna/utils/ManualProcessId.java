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
 * Copyright (C) 2001,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: SocketProcessId.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.utils;

import com.arjuna.ats.arjuna.logging.tsLogger;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.arjuna.exceptions.FatalError;

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
