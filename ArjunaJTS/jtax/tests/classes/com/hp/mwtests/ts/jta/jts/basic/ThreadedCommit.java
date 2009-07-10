/*
 * JBoss, Home of Professional Open Source
 * Copyright 2006, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. 
 * See the copyright.txt in the distribution for a full listing 
 * of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 * 
 * (C) 2005-2006,
 * @author JBoss Inc.
 */
/*
 * Copyright (C) 2001, 2002,
 *
 * Hewlett-Packard Arjuna Labs,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: JTATest.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jta.jts.basic;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import com.arjuna.ats.jta.common.*;

import com.arjuna.ats.internal.jts.ORBManager;

import com.arjuna.orbportability.*;

import org.junit.Test;
import static org.junit.Assert.*;

class TWorker extends Thread
{
    public TWorker (javax.transaction.Transaction tx, TWorker driver)
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

            tm.commit();
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

        jtaPropertyManager.getPropertyManager()
                .setProperty(
                        com.arjuna.ats.jta.common.Environment.JTA_TM_IMPLEMENTATION,
                        "com.arjuna.ats.internal.jta.transaction.jts.TransactionManagerImple");
        jtaPropertyManager.getPropertyManager()
                .setProperty(
                        com.arjuna.ats.jta.common.Environment.JTA_UT_IMPLEMENTATION,
                        "com.arjuna.ats.internal.jta.transaction.jts.UserTransactionImple");

        javax.transaction.TransactionManager tm = com.arjuna.ats.jta.TransactionManager
                .transactionManager();

        if (tm != null)
        {
            System.out.println("Starting top-level transaction.");

            tm.begin();

            javax.transaction.Transaction theTransaction = tm.suspend();

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
