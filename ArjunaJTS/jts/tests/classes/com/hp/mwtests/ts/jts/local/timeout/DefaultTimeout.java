/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.local.timeout;

import org.junit.Test;

import com.arjuna.ats.arjuna.common.arjPropertyManager;
import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.ats.jts.OTSManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class DefaultTimeout
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

	    int sleepTime = arjPropertyManager.getCoordinatorEnvironmentBean().getDefaultTimeout();
	    	    
	    System.out.println("Thread "+Thread.currentThread()+" starting transaction.");
	    
	    OTSManager.get_current().begin();

	    Thread.sleep(sleepTime*1000*2, 0);

	    System.out.println("Thread "+Thread.currentThread()+" committing transaction.");

	    OTSManager.get_current().commit(false);

	    System.out.println("Transaction committed. Timeout did not go off.");
	    System.out.println("Test did not complete successfully.");
	}
	catch (Exception e)
	{
	    System.out.println("Caught exception: "+e);
	    System.out.println("Timeout went off.");

	    System.out.println("Test completed successfully.");
	}

	myOA.destroy();
	myORB.shutdown();
    }

}