/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
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
        catch (final Throwable ex) {
            tsLogger.i18NLogger.warn_abstractrecords_smf1(ex);

            return false;
        }
    }
    
    public static final boolean rememberAction (StateManager inst,
            BasicAction act, int recordType, int state)
    {
        try
        {
            Method m = StateManager.class.getDeclaredMethod("rememberAction", BasicAction.class, int.class, int.class);

            m.setAccessible(true);
            Boolean b = (Boolean) m.invoke(inst, act, recordType, state);
            m.setAccessible(false);

            return b.booleanValue();
        }
        catch (final Throwable ex) {
            tsLogger.i18NLogger.warn_abstractrecords_smf3(ex);

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
        catch (final Throwable ex) {
            tsLogger.i18NLogger.warn_abstractrecords_smf2(ex);
        }
    }

    private StateManagerFriend()
    {
    }
}