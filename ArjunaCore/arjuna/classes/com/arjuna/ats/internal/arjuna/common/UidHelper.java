/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.ats.internal.arjuna.common;

import java.io.IOException;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.OutputBuffer;

public class UidHelper
{
    public static final Uid unpackFrom (InputBuffer buff) throws IOException
    {
        if (buff == null)
            throw new IllegalArgumentException();
        
        return new Uid(buff.unpackBytes());
    }

    public static final void packInto (Uid u, OutputBuffer buff)
            throws IOException
    {
        if ((u == null) || (buff == null))
            throw new IllegalArgumentException();
        
        if (u.valid())
            buff.packBytes(u.getBytes());
        else
            throw new IllegalArgumentException();
    }

    private UidHelper()
    {
    }
}