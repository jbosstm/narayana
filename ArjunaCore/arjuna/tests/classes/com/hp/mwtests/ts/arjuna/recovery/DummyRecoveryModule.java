/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.recovery;

class DummyRecoveryModule implements com.arjuna.ats.arjuna.recovery.RecoveryModule
{
    public DummyRecoveryModule()
    {
    }

    public void periodicWorkFirstPass()
    {
        System.err.println("DummyRecoveryModule.periodicWorkFirstPass");
    }

    public void periodicWorkSecondPass()
    {
        System.err.println("DummyRecoveryModule.periodicWorkSecondPass");

        _complete = true;
    }

    public final boolean finished()
    {
        return _complete;
    }

    private boolean _complete = false;

}