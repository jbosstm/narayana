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
 * Copyright (C) 1998, 1999, 2000,
 *
 * Arjuna Solutions Limited,
 * Newcastle upon Tyne,
 * Tyne and Wear,
 * UK.
 *
 * $Id: DefaultTimeout.java 2342 2006-03-30 13:06:17Z  $
 */

package com.hp.mwtests.ts.jts.local.timeout;

import com.arjuna.orbportability.*;

import com.arjuna.ats.jts.common.jtsPropertyManager;

import com.arjuna.ats.jts.OTSManager;

import com.arjuna.ats.internal.jts.ORBManager;

import org.junit.Test;
import static org.junit.Assert.*;

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

	    int sleepTime = jtsPropertyManager.getJTSEnvironmentBean().getDefaultTimeout();
	    	    
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

