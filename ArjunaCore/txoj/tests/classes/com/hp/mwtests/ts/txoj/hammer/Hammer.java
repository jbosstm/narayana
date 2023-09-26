/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.hammer;



import org.junit.Test;

import com.hp.mwtests.ts.txoj.common.resources.AtomicObject;
import com.hp.mwtests.ts.txoj.common.resources.HammerThreadedObject;

public class Hammer
{
    @Test
    public void test()
    {
        HammerThreadedObject.object = new AtomicObject();
        HammerThreadedObject object1 = new HammerThreadedObject(2);
        HammerThreadedObject object2 = new HammerThreadedObject(-2);

        object1.start();
        object2.start();

        try
        {
            object1.join();
            object2.join();
        }
        catch (InterruptedException e)
        {
        }
    }
}