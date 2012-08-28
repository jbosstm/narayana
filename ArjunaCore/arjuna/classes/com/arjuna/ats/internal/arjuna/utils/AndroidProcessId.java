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
