/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import java.util.Hashtable;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;

public class DummyCheckedAction extends CheckedAction implements CheckedActionFactory
{
    private static CheckedAction instance = new DummyCheckedAction();
    private static boolean _instanceCalled;
    private static boolean _factoryCalled;

    @Override
    public CheckedAction getCheckedAction(Uid txId, String actionType)
    {
        _factoryCalled = true;
        return instance;
    }

    public void check (boolean isCommit, Uid actUid, Hashtable list)
    {
        _instanceCalled = true;
    }

    public static boolean factoryCalled()
    {
        return _factoryCalled;
    }

    public static boolean called ()
    {
        return _instanceCalled;
    }

    public static void reset() {
        _factoryCalled = false;
        _instanceCalled = false;
    }
}