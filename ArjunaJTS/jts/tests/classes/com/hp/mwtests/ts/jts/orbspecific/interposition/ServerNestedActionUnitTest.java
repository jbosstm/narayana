/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.arjuna.ServerNestedAction;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerNestedActionUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator()); 
        ServerNestedAction act = new ServerNestedAction(sc);
        
        try
        {
            act.prepare();
            
            fail();
        }
        catch (final BAD_OPERATION ex)
        {
        }
        
        act.commit();
        act.rollback();
        act.commit_one_phase();
        
        assertTrue(act.theResource() != null);
    }
    
    @Test
    public void testNestedCommit () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator()); 
        ServerNestedAction act = new ServerNestedAction(sc);
        
        act.commit_subtransaction(null);
    }
    
    @Test
    public void testNestedRollback () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator()); 
        ServerNestedAction act = new ServerNestedAction(sc);
        
        act.rollback_subtransaction();
    }
}