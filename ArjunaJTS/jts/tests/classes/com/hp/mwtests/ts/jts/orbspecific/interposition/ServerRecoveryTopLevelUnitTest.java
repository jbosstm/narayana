/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerRecoveryTopLevelAction;
import com.hp.mwtests.ts.jts.resources.TestBase;

class DummyServerRecovery extends ServerRecoveryTopLevelAction
{
    public DummyServerRecovery(ServerControl control)
    {
        super(control);
    }
    
    public void destroyResource ()
    {
        super.destroyResource();
    }
}

public class ServerRecoveryTopLevelUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator()); 
        DummyServerRecovery act = new DummyServerRecovery(sc);
        
        act.destroyResource();
    }
}