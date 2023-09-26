/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.arjuna.statemanager;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.arjuna.AtomicAction;
import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.arjuna.LogWriteStateManager;

class TxStateManager extends LogWriteStateManager
{
    public TxStateManager ()
    {
        super(ObjectModel.MULTIPLE);
    }
    
    public boolean modified ()
    {
        return super.modified();
    }
}

public class TxLogStateManagerUnitTest
{
    @Test
    public void test() throws Exception
    {
        AtomicAction A = new AtomicAction();
        TxStateManager tm = new TxStateManager();
        
        arjPropertyManager.getCoordinatorEnvironmentBean().setClassicPrepare(true);
        
        A.begin();
        
        assertTrue(tm.modified());       
        assertTrue(tm.writeOptimisation());
        
        A.commit();
    }
}