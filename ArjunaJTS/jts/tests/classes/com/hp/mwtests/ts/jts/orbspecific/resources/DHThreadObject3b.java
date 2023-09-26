/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.resources;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.utils.Util;

public class DHThreadObject3b extends Thread
{

    public DHThreadObject3b (int id)
    {
        _id = id;
    }

    public void run ()
    {
        for (int i = 0; i < 1000; i++)
        {
        	CurrentImple current = OTSImpleManager.current();
        	System.out.println("Tripling the timeout from: " + current.get_timeout());
        	current.set_timeout(current.get_timeout() * 3);
        	
            DistributedHammerWorker3.randomOperation(_id, 0);
            Util.highProbYield();
        }
    }

    private int _id;
    
}