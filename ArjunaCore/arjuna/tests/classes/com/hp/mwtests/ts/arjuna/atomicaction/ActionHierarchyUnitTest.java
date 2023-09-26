/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.arjuna.atomicaction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.PrintWriter;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.ActionHierarchy;
import com.arjuna.ats.arjuna.coordinator.ActionType;
import com.arjuna.ats.arjuna.state.InputBuffer;
import com.arjuna.ats.arjuna.state.OutputBuffer;

public class ActionHierarchyUnitTest
{
    @Test
    public void test() throws Exception
    {
        ActionHierarchy ah = new ActionHierarchy(5);
        Uid[] tx = new Uid[5];
        
        assertEquals(ah.getDeepestActionUid(), Uid.nullUid());
        
        for (int i = 0; i < tx.length; i++)
        {
            tx[i] = new Uid();
            
            ah.add(tx[i]);
        }
        
        assertEquals(ah.depth(), tx.length);
        
        assertEquals(ah.getActionUid(0), tx[0]);
        
        Uid deepest = new Uid();
        
        ah.add(deepest, ActionType.TOP_LEVEL);
        
        PrintWriter pw = new PrintWriter(System.err);
        
        ah.print(pw);
        
        assertEquals(ah.getDeepestActionUid(), deepest);
        
        ActionHierarchy cp = new ActionHierarchy(ah);
        
        assertTrue(cp.equals(ah));
        
        cp.copy(ah);
        
        ah.copy(ah);
        
        assertTrue(cp.equals(ah));
        
        OutputBuffer out = new OutputBuffer();
        
        cp.pack(out);
        
        InputBuffer in = new InputBuffer(out.buffer());
        
        ah.unpack(in);
        
        assertTrue(ah.equals(cp));
        
        assertTrue(ah.isAncestor(deepest));
        
        ah.forgetDeepest();
        
        assertTrue(ah.findCommonPrefix(cp) != 0);
    }
}