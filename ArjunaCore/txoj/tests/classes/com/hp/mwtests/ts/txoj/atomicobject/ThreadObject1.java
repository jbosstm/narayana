/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.txoj.atomicobject;




public class ThreadObject1 extends Thread
{
    public ThreadObject1(char c)
    {
        chr = c;
    }

    public void run ()
    {
        for (int i = 0; i < 100; i++)
        {
            AtomicObjectTest2.randomOperation(chr, 0);
            AtomicObjectTest2.highProbYield();
        }
    }

    private char chr;

}