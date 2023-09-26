/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.orbportability.orbspecific.orbinstance;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;

public class SimpleServer
{
    @Test
    public void test() throws Exception
    {
        ORB orb = ORB.getInstance("main_orb");
        ORB orb2 = ORB.getInstance("main_orb_2");
        RootOA oa = RootOA.getRootOA(orb);
        RootOA oa2 = RootOA.getRootOA(orb2);

        orb.initORB(new String[] {},null);
        oa.initOA(new String[] {});

        orb2.initORB(new String[] {},null);
        oa2.initOA(new String[] {});

        SimpleObjectImpl obj = new SimpleObjectImpl();

        assertTrue( oa.objectIsReady(obj) );

        assertTrue( oa2.objectIsReady(obj) );

        oa.destroy();
        orb.shutdown();
        oa2.destroy();
        orb2.shutdown();
    }
}