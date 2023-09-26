/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.basic;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.transaction.xa.XAResource;

import org.junit.Test;

import com.hp.mwtests.ts.jta.common.XACreator;

public class JTATest
{
    @Test
    public void test() throws Exception
    {
        String xaResource = "com.hp.mwtests.ts.jta.common.DummyCreator";
        String connectionString = null;
        boolean tmCommit = true;

        XACreator creator = (XACreator) Thread.currentThread().getContextClassLoader().loadClass(xaResource).newInstance();
        XAResource theResource = creator.create(connectionString, true);
        XAResource theResource2 = creator.create(connectionString, true);

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        tm.begin();

        jakarta.transaction.Transaction theTransaction = tm.getTransaction();

        assertTrue( theTransaction.enlistResource(theResource) );
        assertTrue( theTransaction.enlistResource(theResource2) );

        /*
        * XA does not support subtransactions.
        * By default we ignore any attempts to create such
        * transactions. Appropriate settings can be made which
        * will cause currently running transactions to also
        * rollback, if required.
        */

        System.out.println("\nTrying to start another transaction - should fail!");

        try
        {
            tm.begin();

            fail("Error - transaction started!");
        }
        catch (Exception e)
        {
            System.out.println("Transaction did not begin: " + e);
        }

        /*
        * Do some work and decide whether to commit or rollback.
        * (Assume commit for example.)
        */

        com.hp.mwtests.ts.jta.common.Synchronization s = new com.hp.mwtests.ts.jta.common.Synchronization();

        tm.getTransaction().registerSynchronization(s);

        System.out.println("\nCommitting transaction.");

        if (tmCommit)
            tm.commit();
        else
            tm.getTransaction().commit();

        assertEquals(com.hp.mwtests.ts.jta.common.Synchronization.AFTER_COMPLETION_STATUS, s.getCurrentStatus());

    }
}