/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.interposition;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.BAD_OPERATION;
import org.omg.CosTransactions.Status;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.interposition.coordinator.ServerTransaction;
import com.arjuna.ats.internal.jts.orbspecific.interposition.resources.ServerSynchronization;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class ServerSynchronizationUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        ServerTransaction sc = new ServerTransaction(new Uid(), null);
        ServerSynchronization sync = new ServerSynchronization(sc);
        
        sync.before_completion();
        sync.after_completion(Status.StatusCommitted);
        
        assertTrue(sync.getSynchronization() != null);
        
        sync.destroy();
    }
    
    @Test
    public void testNull () throws Exception
    {
        ServerSynchronization sync = new ServerSynchronization(null);
        
        try
        {
            sync.before_completion();
            
            fail();
        }
        catch (final BAD_OPERATION ex)
        {
        }
        
        try
        {
            sync.after_completion(Status.StatusCommitted);
            
            fail();
        }
        catch (final BAD_OPERATION ex)
        {
        }
    }
}