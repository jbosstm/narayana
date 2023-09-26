/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jta.jts;

import org.junit.After;
import org.junit.Before;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;


public class JTSTestCase
{
    private ORB orb ;
    private RootOA oa ;
    
    @Before
    public void setUp()
        throws Exception
    {
        System.out.println("Before...");

        orb = ORB.getInstance("test");
        oa = OA.getRootOA(orb);
        
        orb.initORB(new String[0], null);
        oa.initOA();

        ORBManager.setORB(orb);
        ORBManager.setPOA(oa);
    }

    @After
    public void tearDown()
        throws Exception
    {
        System.out.println("After...");

        if (oa != null)
        {
            oa.destroy();
        }
        if (orb != null)
        {
            orb.shutdown();
        }
    }
}