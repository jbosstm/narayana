/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;



import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.coordinator.ActionStatus;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord.CrashLocation;
import com.hp.mwtests.ts.arjuna.resources.CrashRecord.CrashType;

public class CrashAction
{
    @Test
    public void test()
    {
        AtomicAction A = new AtomicAction();
        
        A.begin();
        
        A.add(new CrashRecord(CrashLocation.NoCrash, CrashType.Normal));
        A.add(new CrashRecord(CrashLocation.CrashInCommit, CrashType.HeuristicHazard));
        
        int outcome = A.commit();
        
        System.out.println("Transaction "+A+" committed with "+ActionStatus.stringForm(outcome));
    }
}