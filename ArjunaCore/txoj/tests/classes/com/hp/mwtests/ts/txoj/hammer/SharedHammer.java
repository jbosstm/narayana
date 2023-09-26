/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.hammer;

import com.arjuna.ats.arjuna.common.Uid;
import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.hp.mwtests.ts.txoj.common.resources.HammerThreadedObject;

public class SharedHammer
{
    public static void main (String[] args)
    {
        boolean creator = true;
        Uid id = null;
        
        for (int i = 0; i < args.length; i++)
        {
            if ("-object".equals(args[i]))
            {
                creator = false;
                
                id = new Uid(args[i+1]);
            }
        }
        
        if (creator)
        {
            HammerThreadedObject.object = new AtomicObject();
            
            System.out.println("Object created with id: "+HammerThreadedObject.object);
        }
        else
            HammerThreadedObject.object = new AtomicObject(id);
        
        HammerThreadedObject object1 = new HammerThreadedObject(1);
        
        object1.start();

        try
        {
            object1.join();
        }
        catch (InterruptedException e)
        {
        }
    }
}