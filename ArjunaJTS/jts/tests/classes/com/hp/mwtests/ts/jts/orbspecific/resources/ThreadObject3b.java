/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.hp.mwtests.ts.jts.utils.Util;

public class ThreadObject3b extends Thread
{

public ThreadObject3b (int id)
    {
        _id = id;
    }

public void run ()
    {
        for (int i = 0; i < 1000; i++)
        {
            AtomicWorker3.randomOperation(_id, 0);
            Util.highProbYield();
        }
    }

private int _id;
    
}