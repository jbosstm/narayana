/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.concurrencycontrol;

import com.arjuna.ats.arjuna.ObjectType;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.txoj.LockManager;

public class DummyLockManager extends LockManager
{
    public DummyLockManager ()
    {
        super(new Uid(), ObjectType.ANDPERSISTENT);
    }
}