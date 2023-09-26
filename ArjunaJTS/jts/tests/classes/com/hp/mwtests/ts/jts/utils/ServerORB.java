/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package com.hp.mwtests.ts.jts.utils;

import com.arjuna.ats.internal.jts.ORBManager;
import com.arjuna.orbportability.OA;
import com.arjuna.orbportability.ORB;
import com.arjuna.orbportability.RootOA;
import org.omg.CORBA.ORBPackage.InvalidName;

public class ServerORB {
    private ORB myORB = null;
    private RootOA myOA = null;

    public ServerORB() throws InvalidName {
        if (!ORBManager.isInitialised()) {
            myORB = ORB.getInstance("test");
            myOA = OA.getRootOA(myORB);

            myORB.initORB(new String[]{}, null);
            myOA.initOA();

            ORBManager.setORB(myORB);
            ORBManager.setPOA(myOA);
        } else {
            myORB = ORBManager.getORB();
            myOA = OA.getRootOA(myORB);
        }
    }

    public ORB getORB() {
        return myORB;
    }

    public RootOA getOA() {
        return myOA;
    }
}