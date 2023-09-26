/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.destroy;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.hp.mwtests.ts.arjuna.resources.BasicObject;

public class DestroyTest
{
    @Test
    public void test()
    {
        for (int i = 0; i < 100; i++) {
            AtomicAction A = new AtomicAction();

            A.begin();

            BasicObject bo = new BasicObject();

            bo.set(2);

            A.commit();

            AtomicAction B = new AtomicAction();
            AtomicAction C = new AtomicAction();
            
            B.begin();
            C.begin();
            
            bo.destroy();

            C.commit();
            B.abort();

            C = new AtomicAction();

            C.begin();

            bo.destroy();

            C.commit();
        }
    }
}