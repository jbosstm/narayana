/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jta.jts.basic;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import org.junit.Test;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jta.common.jtaPropertyManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

class TWorker extends Thread
{
    public TWorker (jakarta.transaction.Transaction tx, TWorker driver)
    {
        _tx = tx;
        _success = true;
        _driver = driver;
    }

    public void run ()
    {
        TransactionManager tm = com.arjuna.ats.jta.TransactionManager.transactionManager();

        try
        {
            tm.resume(_tx);

            if (_driver != null)
            {
                try
                {
                    _driver.join();
                }
                catch (final Exception ex)
                {
                }
            }
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();

            _success = false;
        }
    }

    public final boolean success ()
    {
        return _success;
    }

    private Transaction _tx;
    private boolean _success;
    private TWorker _driver;
}

public class ThreadedCommit
{
    @Test
    public void test() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        jtaPropertyManager.getJTAEnvironmentBean().setTransactionManagerClassName(com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple.class.getName());
        jtaPropertyManager.getJTAEnvironmentBean().setUserTransactionClassName(com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple.class.getName());

        jakarta.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        if (tm != null)
        {
            System.out.println("Starting top-level transaction.");

            tm.begin();

            jakarta.transaction.Transaction theTransaction = tm.suspend();

            TWorker worker1 = new TWorker(theTransaction, null);
            TWorker worker2 = new TWorker(theTransaction, worker1);

            worker2.start();
            worker1.start();

            worker1.join();
            worker2.join();

            assertTrue( worker1.success() );
            assertTrue( worker2.success() );
        }
        else
        {
            fail("Error - could not get transaction manager!");
        }

        myOA.destroy();
        myORB.shutdown();
    }

}