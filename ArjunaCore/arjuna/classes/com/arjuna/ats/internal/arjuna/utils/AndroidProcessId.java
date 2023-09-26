/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.utils;

import java.lang.reflect.Method;

/**
 * Obtains a unique value to represent the process id via sockets and
 * ports.
 *
 * @author Mark Little (mark_little@hp.com)
 * @version $Id: SocketProcessId.java 2342 2006-03-30 13:06:17Z  $
 * @since HPTS 3.0.
 */

public class AndroidProcessId implements com.arjuna.ats.arjuna.utils.Process
{
    /**
     * Use the Android Process instance to get myPid.
     */
    
    public AndroidProcessId()
    {
        try
        {
            /*
             * Use reflection so we can build this in an environment that does
             * not have the various Android libraries available.
             */
                 
            Class<?> instance = Class.forName(_className);
            Method[] mthds = instance.getDeclaredMethods();
            Method m = null;
            
            for (int i = 0; (i < mthds.length) && (m == null); i++)
            {
                if (_methodName.equals(mthds[i].getName()))
                    m = mthds[i];
            }
            
            _thePort = ((Integer) m.invoke(null)).intValue();
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            _thePort = -1;
        }
    }

    /**
     * @return the process id. This had better be unique between processes
     * on the same machine. If not we're in trouble!
     */
    public int getpid ()
    {
    	return _thePort;
    }

    private int _thePort;
    
    private static final String _className = "android.os.Process";
    private static final String _methodName = "myPid";
}