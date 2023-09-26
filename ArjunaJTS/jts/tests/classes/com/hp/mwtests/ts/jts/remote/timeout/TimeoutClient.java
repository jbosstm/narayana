/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.remote.timeout;

import org.omg.CORBA.INVALID_TRANSACTION;
import org.omg.CORBA.TRANSACTION_ROLLEDBACK;
import org.omg.CosTransactions.Control;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jts.OTSImpleManager;
import com.arjuna.ats.internal.jts.orbspecific.CurrentImple;
import com.hp.mwtests.ts.jts.utils.ServerORB;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import com.arjuna.orbportability.Services;
import com.hp.mwtests.ts.jts.TestModule.SetGet;
import com.hp.mwtests.ts.jts.TestModule.SetGetHelper;
import com.hp.mwtests.ts.jts.resources.TestUtility;

public class TimeoutClient
{
	static int timeout = 8;
	static int mfactor = arjPropertyManager.getCoreEnvironmentBean().getTimeoutFactor();
	
    public static void main(String[] args) throws Exception
    {
        ORB myORB = null;
        RootOA myOA = null;

        try
        {
            ServerORB orb = new ServerORB();
            myORB = orb.getORB();
            myOA = orb.getOA();

            CurrentImple current = OTSImpleManager.current();
            Control theControl = null;

            String objectReference = args[0];

            SetGet SetGetVar = null;

            System.out.println("Setting transaction timeout to " + timeout*mfactor + " seconds.");

            current.set_timeout(timeout*mfactor);

            current.begin();
            current.begin();
            
            long startTime = System.currentTimeMillis();

            try
            {
                Services serv = new Services(myORB);

                SetGetVar = SetGetHelper.narrow(myORB.orb().string_to_object(TestUtility.getService(objectReference)));
            }
            catch (Exception e)
            {
                TestUtility.fail("Bind to object failed: "+e);
                e.printStackTrace(System.err);
            }

            try
            {
                theControl = current.get_control();

                SetGetVar.set((short) 2, theControl);

                theControl = null;

                System.out.println("Set value.");
            }
            catch (Exception e)
            {
                TestUtility.fail("Call to set or get failed: "+e);
                e.printStackTrace(System.err);
            }

            try
            {
            	long timeNow = System.currentTimeMillis();
            	long setTime = (timeNow - startTime);
            	long timeoutTime = (timeout * 1000L * mfactor);
            	long sleepTime =  timeoutTime - setTime; 
            	if (sleepTime > 0) {
            		System.out.println("Now sleeping for " + sleepTime*mfactor + " milliseconds.");

            		Thread.sleep(sleepTime*mfactor);
            	}
            }
            catch (Exception e)
            {
            }

            System.out.println("\ncommitting nested action.");

            try
            {
                current.commit(true);
                TestUtility.fail("commit worked");
            }
            catch (TRANSACTION_ROLLEDBACK  e1)
            {
                System.out.println("Caught TransactionRolledBack");
            }
            catch (INVALID_TRANSACTION  e1)	/* For JacORB */
            {
                System.out.println("Caught InvalidTransaction");
            }

            System.out.println("\ncommitting top-level action");

            try
            {
                current.commit(true);
                TestUtility.fail("commit worked");
            }
            catch (TRANSACTION_ROLLEDBACK  e2)
            {
                System.out.println("Caught TransactionRolledBack");
            }
            catch (INVALID_TRANSACTION  e3)
            {
                System.out.println("Caught InvalidTransaction");
            }
            catch (Exception e)
            {
                TestUtility.fail("Caught other exception: "+e);
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