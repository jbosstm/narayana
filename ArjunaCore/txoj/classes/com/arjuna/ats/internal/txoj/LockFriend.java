/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.internal.txoj;

import java.lang.reflect.Method;

import com.arjuna.ats.txoj.Lock;
import com.arjuna.ats.txoj.logging.txojLogger;

/**
 * A friend of Lock.
 * 
 * @author marklittle
 */

public class LockFriend
{
    public static final Lock getLink (Lock inst)
    {
        try
        {
            Method m = Lock.class.getDeclaredMethod("getLink", (Class[]) null);

            m.setAccessible(true);
            Lock l = (Lock) m.invoke(inst, (Object[]) null);
            m.setAccessible(false);
            
            return l;
        }
        catch (final Throwable ex)
        {
            txojLogger.i18NLogger.warn_lmf1(ex);
            
            return null;
        }
    }
    
    public static final void setLink (Lock inst, Lock link)
    {
        try
        {
            Method m = Lock.class.getDeclaredMethod("setLink", Lock.class);

            m.setAccessible(true);
            m.invoke(inst, link);
            m.setAccessible(false);
        }
        catch (final Throwable ex)
        {
            txojLogger.i18NLogger.warn_lmf2(ex);
        }
    }
}