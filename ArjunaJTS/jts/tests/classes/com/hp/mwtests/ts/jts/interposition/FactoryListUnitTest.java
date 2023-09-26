/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.interposition;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Control;

import org.omg.CORBA.INVALID_TRANSACTION;

import com.arjuna.ats.internal.jts.interposition.FactoryList;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.jts.extensions.Arjuna;
import com.arjuna.ats.jts.utils.Utility;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class FactoryListUnitTest extends TestBase
{
    @SuppressWarnings("static-access")
    @Test
    public void test () throws Exception
    {
        FactoryList factory = new FactoryList();
        
        factory.removeDefault();
        
        try
        {
            assertNull(factory.recreate(null, FactoryList.DEFAULT_ID));
            
            fail();
        }
        catch (final INVALID_TRANSACTION ex)
        {
        }
        catch (final Throwable ex)
        {
            fail();
        }
        
        FactoryList.remove(Arjuna.restrictedXID());
        
        TransactionFactoryImple imple = new TransactionFactoryImple("test");
        ControlImple tx = imple.createLocal(1000);
        
        PropagationContext ctx = tx.get_coordinator().get_txcontext();
        Control cont = FactoryList.recreate(ctx, Arjuna.XID());
        
        assertTrue(Utility.getUid(cont).equals(tx.get_uid()));
    }
}