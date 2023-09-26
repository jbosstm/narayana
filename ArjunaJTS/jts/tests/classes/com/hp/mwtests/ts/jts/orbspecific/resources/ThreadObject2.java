/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.hp.mwtests.ts.jts.utils.Util;

public class ThreadObject2 extends Thread
{

    public ThreadObject2 (char c)
    {
        chr = c;
    }

    public void run ()
    {
        for (int i = 0; i < 100; i++)
        {
            AtomicWorker2.randomOperation(chr, 0);
            Util.highProbYield();
        }
    }
    
    private char chr;
    
}