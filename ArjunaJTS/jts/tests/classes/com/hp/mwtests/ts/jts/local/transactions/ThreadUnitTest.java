/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.jts.thread.OTSThread;
import com.hp.mwtests.ts.jts.resources.TestBase;

class DummyThread extends OTSThread
{
    public void run ()
    {
        super.run();

        if (OTSImpleManager.current().get_control() != null)
            _transactional = true;
        
        terminate();
    }
    
    public boolean transactional ()
    {
        return _transactional;
    }
    
    private boolean _transactional = false;
}


public class ThreadUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        OTSImpleManager.current().begin();
        
        DummyThread t = new DummyThread();
        
        t.start();
        
        t.join();
        
        assertTrue(t.transactional());
    }
}