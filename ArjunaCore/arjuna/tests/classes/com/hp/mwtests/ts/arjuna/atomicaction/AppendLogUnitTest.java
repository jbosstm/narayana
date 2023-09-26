/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import org.junit.Test;

import com.arjuna.ats.internal.arjuna.coordinator.AppendLogTransaction;

public class AppendLogUnitTest
{
    @Test
    public void test() throws Exception
    {
        AppendLogTransaction alog1 = new AppendLogTransaction();
        AppendLogTransaction alog2 = new AppendLogTransaction();
        
        alog1.begin();
        alog2.begin();
        
        alog1.setLoggedTransaction(alog2);
        
        alog2.commit();
        alog1.commit();
    }
}