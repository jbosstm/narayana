/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;

import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.Inactive;
import org.omg.CosTransactions.NoTransaction;
import org.omg.CosTransactions.otid_t;
import org.omg.CosTransactions.PropagationContext;
import org.omg.CosTransactions.Status;

import com.arjuna.ArjunaOTS.TransactionType;
import com.arjuna.ArjunaOTS.GlobalTransactionInfo;
import com.arjuna.ArjunaOTS.TransactionInfo;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jts.orbspecific.ControlImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.jts.utils.Utility;
import com.hp.mwtests.ts.jts.resources.TestBase;


public class TransactionFactoryUnitTest extends TestBase
{
    @Test
    public void testBasic () throws Exception
    {
        TransactionFactoryImple factory = new TransactionFactoryImple("test");

        arjPropertyManager.getCoordinatorEnvironmentBean().setEnableStatistics(true);
        
        try
        {
            factory.numberOfTransactions(TransactionType.TransactionTypeActive);
            
//            fail();
        }
        catch (final Inactive ex)
        {
        }
        catch (final NoTransaction ex)
        {
        }
        
        ControlImple tx = factory.createLocal(1000);
        
        assertTrue(tx != null);
        
        org.omg.CosTransactions.otid_t[] txId = null;
        
        try
        {
            txId = factory.numberOfTransactions(TransactionType.TransactionTypeActive);
        }
        catch (final Throwable ex)
        {
            fail();
        }
        
        try
        {
            if (factory.getChildTransactions(txId[0]) != null)
                fail();
        }
        catch (final Throwable ex)
        {
            fail();
        }
        
        org.omg.CosTransactions.Status status = factory.getCurrentStatus(txId[0]);
        
        assertTrue(status == org.omg.CosTransactions.Status.StatusActive);

        assertTrue(factory.getStatus(txId[0]) == org.omg.CosTransactions.Status.StatusActive);
        
        Control proxy = factory.createProxy(tx.get_coordinator(), tx.get_terminator());
        
        assertTrue(proxy != null);
        
        Control propagated = factory.createPropagatedControl(tx.get_coordinator());
        
        assertTrue(propagated != null);
        
        assertTrue(Utility.getUid(proxy).equals(Utility.getUid(propagated)));
        
        GlobalTransactionInfo info = factory.getGlobalInfo();
        
        assertTrue(info != null);
        assertEquals(info.totalNumberOfTransactions, 1);
        assertEquals(info.numberOfHeuristics, 0);
        
        factory.numberOfTransactions(TransactionType.TransactionTypeUnresolved);
        
        try
        {
            tx.getImplHandle().rollback();
        }
        catch (final Throwable ex)
        {
        }
    }
    
    @Test
    public void testContext () throws Exception
    {
        TransactionFactoryImple factory = new TransactionFactoryImple("test");
        ControlImple tx = factory.createLocal(1000);

        org.omg.CosTransactions.otid_t txId = Utility.uidToOtid(tx.get_uid());
        Uid theUid = Utility.otidToUid(txId);
        
        assertEquals(theUid, tx.get_uid());

        assertEquals(factory.getOSStatus(tx.get_uid()), org.omg.CosTransactions.Status.StatusNoTransaction); // no state in OS yet!
        
        PropagationContext ctx = tx.get_coordinator().get_txcontext();       
        Control cont = factory.recreate(ctx);       
        String toString = Utility.getHierarchy(ctx);
        
        System.out.println(toString);
        
        assertTrue(toString != null);
        assertTrue(toString.length() > 1);
        
        assertTrue(Utility.getUid(cont).equals(tx.get_uid()));
        
        try
        {
            tx.getImplHandle().rollback();
        }
        catch (final Throwable ex)
        {
        }
    }
    
    @Test
    public void testCompare () throws Exception
    {
        TransactionFactoryImple factory = new TransactionFactoryImple("test");
        ControlImple tx = factory.createLocal(1000);
        
        Control proxy = factory.getTransaction(Utility.uidToOtid(tx.get_uid()));
        
        assertTrue(Utility.getUid(proxy).equals(tx.get_uid()));
        
        try
        {
            tx.getImplHandle().rollback();
        }
        catch (final Throwable ex)
        {
        }
    }
    
    @Test
    public void testInfo () throws Exception
    {
        TransactionFactoryImple factory = new TransactionFactoryImple("test");
        ControlImple tx = factory.createLocal(1000);        
        TransactionInfo info = factory.getTransactionInfo(Utility.uidToOtid(tx.get_uid()));
        
        assertEquals(info.currentDepth, 1);
        assertEquals(info.timeout, 0);
        assertEquals(info.numberOfThreads, 0);
        
        try
        {
            tx.getImplHandle().rollback();
        }
        catch (final Throwable ex)
        {
        }
    }
}