/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.arjuna;

import org.omg.CORBA.IntHolder;

import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.stack;
import com.hp.mwtests.ts.jts.TestModule.stackHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class ImplicitArjunaClient
{
    public static void main(String[] args) throws Exception
    {
        ServerORB orb = new ServerORB();
        ORB myORB = orb.getORB();
        RootOA myOA = orb.getOA();

        String refFile = args[0];
        CurrentImple current = OTSImpleManager.current();

        stack stackVar = null;   // pointer the grid object that will be used.

        try
        {
            current.begin();

            try
            {
                Services serv = new Services(myORB);

                stackVar = stackHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(refFile)));
            }
            catch (Exception e)
            {
                e.printStackTrace(System.err);
                TestUtility.fail(e.toString());
            }

            System.out.println("pushing 1 onto stack");

            stackVar.push(1);

            System.out.println("pushing 2 onto stack");

            stackVar.push(2);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            TestUtility.fail(e.toString());
        }

        try
        {
            current.commit(false);

            current.begin();

            IntHolder val = new IntHolder(-1);

            if (stackVar.pop(val) == 0)
            {
                System.out.println("popped top of stack "+val.value);

                current.begin();

                stackVar.push(3);

                System.out.println("pushed 3 onto stack. Aborting nested action.");

                current.rollback();

                stackVar.pop(val);

                System.out.println("popped top of stack is "+val.value);

                current.commit(false);

                TestUtility.assertEquals(1, val.value);

            }
            else
            {
                TestUtility.fail("Error getting stack value.");

                current.rollback();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            TestUtility.fail(e.toString());
        }

        myOA.destroy();
        myORB.shutdown();

        System.out.println("Passed");
    }
}