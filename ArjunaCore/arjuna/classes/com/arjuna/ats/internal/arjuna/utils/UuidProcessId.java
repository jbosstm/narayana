/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.utils;

import java.util.UUID;

/**
 * Obtains a unique value to represent the process id via UUID.
 */

public class UuidProcessId implements com.arjuna.ats.arjuna.utils.Process
{
    public UuidProcessId ()
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
    }
    
    /**
     * @return the process id. This had better be unique between processes on
     *         the same machine. If not we're in trouble!
     */

    public int getpid ()
    {
        return _pid;
    }

    private static UUID _theUid = UUID.randomUUID();
    
    private int _pid = -1;
}