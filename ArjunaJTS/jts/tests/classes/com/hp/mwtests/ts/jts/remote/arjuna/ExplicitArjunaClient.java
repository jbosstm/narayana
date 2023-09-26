/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.arjuna;

import org.omg.CORBA.IntHolder;
import org.omg.CosTransactions.Control;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jts.TestModule.ExplicitStack;
import com.hp.mwtests.ts.jts.TestModule.ExplicitStackHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class ExplicitArjunaClient
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        CurrentImple current = OTSImpleManager.current();
        String refFile = args[0];

        int value = 1;
        Control cont = null;

        try
        {
            System.out.println("Starting initialising top-level transaction.");

            current.begin();

            System.out.println("Initialising transaction name: "+current.get_transaction_name());
        }
        catch (Exception e)
        {
            TestUtility.fail(e.toString());
        }

        ExplicitStack stackVar = null;   // pointer the grid object that will be used.

        try
        {
            stackVar = ExplicitStackHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(refFile)));
        }
        catch (Exception e)
        {
            TestUtility.fail("Bind error: "+e);
        }

        try
        {
            System.out.println("pushing "+value+" onto stack");

            cont = current.get_control();
            stackVar.push(value, cont);

            System.out.println("\npushing "+(value+1)+" onto stack");

            stackVar.push(value+1, cont);

            cont = null;
        }
        catch (Exception e)
        {
            TestUtility.fail(e.toString());
        }

        try
        {
            current.commit(true);

            System.out.println("Committed top-level transaction");
            System.out.println("\nBeginning top-level transaction");

            current.begin();

            System.out.println("Top-level name: "+current.get_transaction_name());

            IntHolder val = new IntHolder(-1);

            cont = current.get_control();

            if (stackVar.pop(val, cont) == 0)
            {
                System.out.println("popped top of stack "+val.value);
                System.out.println("\nbeginning nested transaction");

                current.begin();

                System.out.println("nested name: "+current.get_transaction_name());

                cont = null;
                cont = current.get_control();
                stackVar.push(value+2, cont);

                System.out.println("pushed "+(value+2)+" onto stack. Aborting nested action.");

                cont = null;  // current will destroy this control!
                current.rollback();
                cont = current.get_control();

                System.out.println("current transaction name: "+current.get_transaction_name());
                System.out.println("rolledback nested transaction");

                stackVar.pop(val, cont);

                System.out.println("\npopped top of stack is "+val.value);

                System.out.println("\nTrying to print stack contents - should fail.");

                stackVar.printStack();

                cont = null;
                current.commit(true);

                System.out.println("\nCommitted top-level transaction");

                if (current.get_transaction_name() == null)
                    System.out.println("current transaction name: null");
                else
                    System.out.println("Error - current transaction name: "
                            +current.get_transaction_name());

                TestUtility.assertEquals(value, val.value);

            }
            else
            {
                System.out.println("Error getting stack value.");

                current.rollback();

                System.out.println("\nRolledback top-level transaction.");
            }

            try
            {
                System.out.println("\nPrinting stack contents (should be empty).");

                stackVar.printStack();
            }
            catch (Exception e)
            {
                TestUtility.fail("\nError - could not print.");
            }
        }
        catch (Exception e)
        {
            TestUtility.fail("Caught unexpected exception: "+e);
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}