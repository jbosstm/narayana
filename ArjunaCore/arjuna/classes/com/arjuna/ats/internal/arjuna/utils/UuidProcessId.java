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

import java.util.UUID;

/**
 * Obtains a unique value to represent the process id via UUID.
 */

public class UuidProcessId implements com.arjuna.ats.arjuna.utils.Process
{
    /**
     * @return the process id. This had better be unique between processes on
     *         the same machine. If not we're in trouble!
     */

    public int getpid ()
    {
        /*
         * UUID contains 2*64 bit fields, which we need to convert to a 32 bit number.
         * We will lose accuracy and increase the probability of a process id clash.
         */
        
        synchronized (UuidProcessId._theUid)
        {
            if (_pid == -1)
            {
                _pid = (int) (_theUid.getLeastSignificantBits() ^ _theUid.getMostSignificantBits());
            }
        }

        return _pid;
    }

    private static UUID _theUid = UUID.randomUUID();
    
    private int _pid = -1;
}
