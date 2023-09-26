/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.local.checked;

import static org.junit.Assert.fail;

import java.util.Hashtable;

import org.junit.Test;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

class MyCheckedAction extends CheckedAction
{
    public synchronized void check (boolean isCommit, Uid actUid, Hashtable list)
    {
        // don't do anything so that no warning message is printed!
    }
}

class TXThread extends Thread
{
    public TXThread (Control c)
    {
        cont = c;
    }

    public void run ()
    {
        try
        {
            System.out.println("Thread "+Thread.currentThread()+" attempting to rollback transaction.");

            cont.get_terminator().rollback();

            System.out.println("Transaction rolled back. Checked transactions disabled.");
        }
        catch (Exception e)
        {
            System.out.println("Caught exception: "+e);
            System.out.println("Checked transactions enabled!");
        }
    }

    private Control cont;

}

public class CheckedTransactions
{
    @Test
    public void test()
    {
        ORB myORB = null;
        RootOA myOA = null;

        try
        {
            myORB = ORB.getInstance("test");
            myOA = OA.getRootOA(myORB);

            myORB.initORB(new String[] {}, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);

            Control tx = null;

            System.out.println("Thread "+Thread.currentThread()+" starting transaction.");

            OTSImpleManager.current().setCheckedAction(new MyCheckedAction());

            OTSImpleManager.current().begin();

            tx = OTSImpleManager.current().get_control();

            TXThread txThread = new TXThread(tx);

            txThread.start();
            txThread.join();

            System.out.println("Thread "+Thread.currentThread()+" committing transaction.");

            OTSImpleManager.current().commit(false);

            System.out.println("Transaction committed. Checked transactions enabled.");
        }
        catch (Exception e)
        {
            System.out.println("Caught exception: "+e);
            fail("Checked transactions disabled!");
        }

        myOA.destroy();
        myORB.shutdown();
    }
}