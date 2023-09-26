/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.arjuna.ats.jts;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.hp.mwtests.ts.jts.resources.TestBase;

public class TransactionServerUnitTest extends TestBase
{
    @Test
    public void testHelp () throws Exception
    {
        String[] args = { "-help" };
        
        TransactionServer.doWork(args, true);
    }
    
    @Test
    public void testVersion () throws Exception
    {
        String[] args = { "-version" };
        
        TransactionServer.doWork(args, true);
    }
    
    @Test
    public void test () throws Exception
    {
        String[] args = { "-otsname", "foo" };
        
        TransactionServer.doWork(args, true);
    }
}