/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.restricted.ServerRestrictedTopLevelAction;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerTopLevelRestrictedUnitTest extends TestBase
{
    @Test
    public void testCommit () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator()); 
        ServerRestrictedTopLevelAction act = new ServerRestrictedTopLevelAction(sc);
        
        assertTrue(act.type() != null);
        assertTrue(act.child() == null);
        assertTrue(act.deepestControl() != null);
    }
}