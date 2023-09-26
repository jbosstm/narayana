/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Vote;

import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.coordinator.ArjunaTransactionImple;
import com.arjuna.ats.internal.jts.orbspecific.interposition.ServerControl;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.osi.ServerOSITopLevelAction;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerTopLevelOSIActionUnitTest extends TestBase
{
    @Test
    public void testCommit () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator()); 
        ServerOSITopLevelAction act = new ServerOSITopLevelAction(sc, true);
        
        assertEquals(act.prepare(), Vote.VoteReadOnly);
        
        try
        {
            act.commit();
            
            fail();
        }
        catch (final INVALID_TRANSACTION ex)
        {
        }
        
        assertTrue(act.getReference() != null);
    }
    
    @Test
    public void testCommitOnePhase () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator()); 
        ServerOSITopLevelAction act = new ServerOSITopLevelAction(sc, true);

        act.commit_one_phase();
        
        assertTrue(act.type() != null);
    }
    
    @Test
    public void testRollback () throws Exception
    {
        ControlImple cont = new ControlImple(null, null);
        Control theControl = cont.getControl();
        ArjunaTransactionImple tx = cont.getImplHandle();
        ServerControl sc = new ServerControl(tx.get_uid(), theControl, tx, theControl.get_coordinator(), theControl.get_terminator()); 
        ServerOSITopLevelAction act = new ServerOSITopLevelAction(sc, true);
        
        assertEquals(act.prepare(), Vote.VoteReadOnly);
        
        try
        {
            act.rollback();
            
            fail();
        }
        catch (final INVALID_TRANSACTION ex)
        {
        }
        
        act.forget();
    }
}