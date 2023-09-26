/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.hp.mwtests.ts.jts.utils.TaskProgress;
import com.hp.mwtests.ts.jts.utils.Util;

public class DHThreadObject2 extends Thread
{

    public DHThreadObject2 (TaskProgress progress, char c)
    {
        chr = c;
        this.progress = progress;
    }

    public void run ()
    {
        for (int i = 0; i < 100; i++)
        {
            DistributedHammerWorker2.randomOperation(chr, 0);
            progress.tick();
            Util.highProbYield();
        }

        progress.setFinished();
    }

    private char chr;
    private TaskProgress progress;
}