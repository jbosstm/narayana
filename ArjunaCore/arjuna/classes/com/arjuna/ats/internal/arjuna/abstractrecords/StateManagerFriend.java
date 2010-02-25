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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.  
 *
 * $Id: CadaverActivationRecord.java 2342 2006-03-30 13:06:17Z  $
 */

package com.arjuna.ats.internal.arjuna.abstractrecords;

import java.lang.reflect.Method;

import com.arjuna.ats.arjuna.StateManager;
import com.arjuna.ats.arjuna.coordinator.BasicAction;
import com.arjuna.ats.arjuna.logging.tsLogger;

/**
 * Some StateManager methods really shouldn't be available to users to call inadvertently. In
 * C++ valid users (specific AbstractRecord instances) were friends of StateManager.
 */

/**
 * @message com.arjuna.ats.internal.arjuna.abstractrecords.smf1
 *          [com.arjuna.ats.internal.arjuna.abstractrecords.smf1] - StateManagerFriend.forgetAction
 * @message com.arjuna.ats.internal.arjuna.abstractrecords.smf2
 *          [com.arjuna.ats.internal.arjuna.abstractrecords.smf2] - StateManagerFriend.destroyed
 * @message com.arjuna.ats.internal.arjuna.abstractrecords.smf3
 *          [com.arjuna.ats.internal.arjuna.abstractrecords.smf3] - StateManagerFriend.rememberAction
 */

public class StateManagerFriend
{
    public static final boolean forgetAction (StateManager inst,
            BasicAction act, boolean committed, int recordType)
    {
        try
        {
            Method m = StateManager.class.getDeclaredMethod("forgetAction", BasicAction.class, boolean.class, int.class);

            m.setAccessible(true);
            Boolean b = (Boolean) m.invoke(inst, act, committed, recordType);
            m.setAccessible(false);

            return b.booleanValue();
        }
        catch (final Throwable ex)
        {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N
                .warn("com.arjuna.ats.internal.arjuna.abstractrecords.smf1", ex);
            
            return false;
        }
    }
    
    public static final boolean rememberAction (StateManager inst,
            BasicAction act, int recordType)
    {
        try
        {
            Method m = StateManager.class.getDeclaredMethod("rememberAction", BasicAction.class, int.class);

            m.setAccessible(true);
            Boolean b = (Boolean) m.invoke(inst, act, recordType);
            m.setAccessible(false);

            return b.booleanValue();
        }
        catch (final Throwable ex)
        {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N
                .warn("com.arjuna.ats.internal.arjuna.abstractrecords.smf3", ex);
            
            return false;
        }
    }

    public static final void destroyed (StateManager inst) //throws NoSuchMethodException, InvocationTargetException, IllegalAccessException
    {
        try
        {
            Method m = StateManager.class.getDeclaredMethod("destroyed", (Class[]) null);
    
            m.setAccessible(true);
            m.invoke(inst, (Object[]) null);
            m.setAccessible(false);
        }
        catch (final Throwable ex)
        {
            if (tsLogger.arjLoggerI18N.isWarnEnabled())
                tsLogger.arjLoggerI18N
                .warn("com.arjuna.ats.internal.arjuna.abstractrecords.smf2", ex);
        }
    }

    private StateManagerFriend()
    {
    }
}
