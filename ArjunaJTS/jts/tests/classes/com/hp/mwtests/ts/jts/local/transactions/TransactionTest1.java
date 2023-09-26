/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.transactions;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Control;
import org.omg.CosTransactions.Terminator;
import org.omg.CosTransactions.TransactionFactory;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class TransactionTest1
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


        TransactionFactory theFactory = OTSManager.get_factory();
        Control topLevelControl = null;
        Control nestedControl = null;
        Terminator terminator = null;

        /*
       * First with current.
       */

        try
        {
            org.omg.CosTransactions.Current current = OTSManager.get_current();

            current.begin();

            topLevelControl = current.get_control();

            current.begin();

            nestedControl = current.get_control();

            /*
            * Try to commit top-level action without terminating nested
            * action.
            */

            terminator = topLevelControl.get_terminator();

            System.out.println("\nAttempting to terminate out of sequence 'current'. Should fail!");

            try
            {
                terminator.commit(true);
                
                fail();
            }
            catch (TRANSACTION_ROLLEDBACK  e)
            {
                System.err.println("Commit failed! "+e);
            }

            System.out.println("\nNow attempting to terminate 'current' hierarchy. Should fail!");

            /*
            * This should fail since Arjuna will already have aborted the top-level
            * action and all of its children.
            */

            try
            {
                current.commit(true);
                
                fail();
            }
            catch (INVALID_TRANSACTION  e1)
            {
                System.err.println("Nested commit failed! "+e1);
            }
            catch (TRANSACTION_ROLLEDBACK e1)
            {
                System.err.println("Nested commit failed! "+e1);
            }
            catch (Exception e2)
            {
                fail("Nested commit threw unexpected exception: "+e2);
            }

            try
            {
                current.commit(true);
                
                fail();
            }
            catch (TRANSACTION_ROLLEDBACK  e1)
            {
                System.err.println("Top-level commit failed! "+e1);
            }
            catch (INVALID_TRANSACTION  e2)
            {
                System.err.println("Top-level commit failed! "+e2);
            }
            catch (Exception e3)
            {
                fail("Top-level commit threw unexpected exception: "+e3);
            }

            /*
            * Now with the factory.
            */

            System.out.println("\nNow creating new top-level action.");

            topLevelControl = theFactory.create(0);
            nestedControl = topLevelControl.get_coordinator().create_subtransaction();

            terminator = topLevelControl.get_terminator();

            System.out.println("\nAttempting to terminate out of sequence 'factory' action. Should fail!");

            try
            {
                terminator.commit(true);
                
                fail();
            }
            catch (TRANSACTION_ROLLEDBACK  e1)
            {
                System.err.println("Commit failed! "+e1);
            }
            catch (INVALID_TRANSACTION  e2)
            {
                System.err.println("Commit failed! "+e2);
            }

            terminator = nestedControl.get_terminator();

            System.out.println("\nNow attempting to terminate 'factory' nested action. Should fail!");

            try
            {
                terminator.commit(true);
                
                fail();
            }
            catch (TRANSACTION_ROLLEDBACK  e1)
            {
                System.err.println("Commit failed! "+e1);
            }
            catch (INVALID_TRANSACTION  e2)
            {
                System.err.println("Commit failed! "+e2);
            }

            System.out.println("Test completed successfully.");
        }
        catch (Exception e)
        {
            fail("Test failed - received unexpected exception "+e);
        }

        myOA.destroy();
        myORB.shutdown();
    }

}