/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerControlUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator());
        
        assertTrue(sc.isWrapper());
        assertTrue(sc.get_coordinator() != null);
        assertTrue(sc.get_terminator() != null);
        
        assertEquals(sc.getParentImple(), null);
        assertTrue(sc.forgetHeuristics());
        assertTrue(sc.toString() != null);
        
        sc.destroy();
    }
}