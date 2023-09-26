/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.arjuna;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.UserException;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.hp.mwtests.ts.jts.orbspecific.resources.DemoArjunaResource;

public class ArjunaNestingTest
{
    @Test
    public void run() throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        myORB = ORB.getInstance("test");
        myOA = OA.getRootOA(myORB);

        myORB.initORB(new String[] {}, null);
        myOA.initOA();

        ORBManager.setORB(myORB);
        ORBManager.setPOA(myOA);

        boolean doAbort = false;
        boolean registerSubtran = false;
        org.omg.CosTransactions.Current current = OTSManager.get_current();
        DemoArjunaResource sr = new DemoArjunaResource();

        try
        {
            current.begin();
            current.begin();
            current.begin();
        }
        catch (SystemException sysEx)
        {
            fail("Unexpected system exception:" +sysEx);
            sysEx.printStackTrace(System.err);
        }
        catch (UserException se)
        {
            fail("Unexpected user exception:" +se);
            se.printStackTrace(System.err);
        }

        try
        {
            sr.registerResource(registerSubtran);
        }
        catch (SystemException ex1)
        {
            fail("Unexpected system exception: "+ex1);
            ex1.printStackTrace(System.err);
        }
        catch (Exception e)
        {
            fail("call to registerSubtran failed: "+e);
            e.printStackTrace(System.err);
        }

        try
        {
            System.out.println("committing first nested transaction");
            current.commit(true);

            System.out.println("committing second nested transaction");
            current.commit(true);

            if (!doAbort)
            {
                System.out.println("committing top-level transaction");
                current.commit(true);
            }
            else
            {
                System.out.println("aborting top-level transaction");
                current.rollback();
            }
        }
        catch (Exception ex)
        {
            fail("Caught unexpected exception: "+ex);
            ex.printStackTrace(System.err);
        }

        myOA.shutdownObject(sr);

        myOA.destroy();
        myORB.shutdown();
    }

}