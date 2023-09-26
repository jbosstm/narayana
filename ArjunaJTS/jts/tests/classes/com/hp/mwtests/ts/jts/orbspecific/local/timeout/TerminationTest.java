/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.local.timeout;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertEquals;

import java.util.Hashtable;

import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Coordinator;
import org.omg.CosTransactions.Status;
import org.omg.CosTransactions.Terminator;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.coordinator.CheckedAction;
import com.arjuna.ats.arjuna.coordinator.CheckedActionFactory;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.ats.internal.jts.orbspecific.TransactionFactoryImple;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

class FakeCheckedAction extends CheckedAction
{
    public void check (boolean isCommit, Uid actUid, Hashtable list)
    {
        called = true;
    }
    
    public boolean called = false;
}

public class TerminationTest
{
    @Test
    public void test()
    {
        boolean commit = true;

        Control myControl = null;
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

            TransactionFactoryImple theOTS = new TransactionFactoryImple();

            System.out.println("Creating transaction with 2 second timeout.");

            myControl = theOTS.create(2);

            assertNotNull( myControl );

            Terminator handle = myControl.get_terminator();

            try
            {
                System.out.println("Sleeping for 5 seconds.");

                Thread.sleep(5000);
            }
            catch (Exception e)
            {
            }

            if (handle != null)
            {
                System.out.print("Attempting to ");

                if (commit)
                    System.out.println("commit transaction. Should fail!");
                else
                    System.out.println("rollback transaction. Should fail!.");

                if (commit)
                    handle.commit(true);
                else
                    handle.rollback();

                assertFalse(commit);

                System.out.println("\nNow attempting to destroy transaction. Should fail!");

                OTSManager.destroyControl(myControl);
            }
            else
                System.err.println("No transaction terminator!");
        }
        catch (UserException e)
        {
            System.err.println("Caught UserException: "+e);
        }
        catch (SystemException e)
        {
            System.err.println("Caught SystemException: "+e);

            try
            {
                Coordinator coord = myControl.get_coordinator();
                Status s = coord.get_status();

                System.err.println("Transaction status: "+com.arjuna.ats.jts.utils.Utility.stringStatus(s));

                coord = null;
            }
            catch (Exception ex)
            {
            }
        }

        try
        {
            CurrentImple current = OTSImpleManager.current();
            FakeCheckedAction act = new FakeCheckedAction();
            
            current.set_timeout(2);
            current.setCheckedAction(act);

            assertEquals(act, current.getCheckedAction());
            
            System.out.println("\nNow creating current transaction with 2 second timeout.");

            current.begin();

            myControl = current.get_control();

            try
            {
                System.out.println("Sleeping for 5 seconds.");

                Thread.sleep(5000);
            }
            catch (Exception e)
            {
            }

            System.out.print("Attempting to ");

            if (commit)
                System.out.println("commit transaction. Should fail!");
            else
                System.out.println("rollback transaction. Should fail!.");

            if (commit)
                current.commit(true);
            else
                current.rollback();

            assertFalse(commit);
            
            assertTrue(act.called);
        }
        catch (UserException e)
        {
            System.err.println("Caught UserException: "+e);
            System.out.println("Test did not completed successfully.");
        }
        catch (SystemException e)
        {
            System.err.println("Caught SystemException: "+e);

            try
            {
                Coordinator coord = myControl.get_coordinator();
                Status s = coord.get_status();

                System.err.println("Transaction status: "+com.arjuna.ats.jts.utils.Utility.stringStatus(s));

                myControl = null;
                coord = null;
            }
            catch (Exception ex)
            {
            }

            System.out.println("Test completed successfully.");
        }

        myOA.destroy();
        myORB.shutdown();
    }
}