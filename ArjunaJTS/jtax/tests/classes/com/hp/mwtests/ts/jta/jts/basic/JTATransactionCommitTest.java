/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts.basic;

import static org.junit.Assert.fail;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.Test;

import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.hp.mwtests.ts.jta.jts.JTSTestCase;



public class JTATransactionCommitTest extends JTSTestCase
{
    @Test
    public void test() throws Exception
    {
        /** Ensure underlying JTA implementation is JTS **/
        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

        try
        {
            TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

            System.out.println("Beginning new transaction");
            tm.begin();

            Transaction tx = tm.suspend();

            System.out.println("Beginning second transaction");
            tm.begin();

            System.out.println("Committing original transaction (via Transaction interface)");
            tx.commit();

            System.out.println("Committing second transaction (via TransactionManager interface)");
            tm.commit();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            fail("Unexpected exception: "+e);
        }
    }
}