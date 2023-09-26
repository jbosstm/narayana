/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.txoj.common.resources;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.txoj.lockstore.LockStore;

public class AtomicObjectLockStore extends AtomicObject
{
    public AtomicObjectLockStore()
    {
        super();
    }

    public AtomicObjectLockStore(int om)
    {
        super(om);
    }
    
    public AtomicObjectLockStore(Uid u)
    {
        super(u);
    }
    
    public AtomicObjectLockStore(Uid u, int om)
    {
        super(u, om);
    }
        
    public LockStore getLockStore ()
    {
        return lockStore;
    }
}