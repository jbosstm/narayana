/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;



import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.BasicAction;

public class BadAction
{
    @Test
    public void test()
    {
        AtomicAction A = new AtomicAction();
        AtomicAction B = new AtomicAction();

        A.begin();
        B.begin();

        A.commit();
        B.commit();

        BasicAction current = BasicAction.Current();
    }
}