/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.lifecycle;

import org.junit.Test;

import static org.junit.Assert.fail;

import com.arjuna.ats.internal.jts.lifecycle.ShutdownOTS;
import com.arjuna.ats.internal.jts.lifecycle.StartupOTS;

public class StartStopUnitTest
{
    @Test
    public void run() throws Exception
    {
        StartupOTS start = new StartupOTS();
        ShutdownOTS shutdown = new ShutdownOTS();
        
        try
        {
            shutdown.work();
 
            fail();
        }
        catch (final Throwable ex)
        {
        }
    }
}