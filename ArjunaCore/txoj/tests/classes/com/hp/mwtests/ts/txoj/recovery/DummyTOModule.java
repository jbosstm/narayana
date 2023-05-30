/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.recovery;

import com.arjuna.ats.internal.txoj.recovery.TORecoveryModule;

public class DummyTOModule extends TORecoveryModule
{
    public void intialise ()
    {
        super.initialise();
    }
}