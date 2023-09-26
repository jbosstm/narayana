/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.recovery;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.recovery.contact.RecoveryContactWriter;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class RecoveryContactUnitTest extends TestBase
{
    @Test
    public void test () throws Exception
    {
        RecoveryContactWriter writer = new RecoveryContactWriter();
        OTSImpleManager.current().begin();
        
        writer.connected(OTSImpleManager.get_factory());
        
        writer.disconnected(null);
        
        assertTrue(writer.name() != null);
        
        OTSImpleManager.current().rollback();
    }
}