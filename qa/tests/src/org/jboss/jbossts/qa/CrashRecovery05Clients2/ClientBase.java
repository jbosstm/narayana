/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package org.jboss.jbossts.qa.CrashRecovery05Clients2;

import org.jboss.jbossts.qa.Utils.OAInterface;
import org.jboss.jbossts.qa.Utils.ORBInterface;

public class ClientBase {

    private static boolean shutdown;

    protected static void init(String[] args, Object o) {
        if (ORBInterface.getORB() == null) {
            shutdown = true;
            ORBInterface.initORB(args, null);
            OAInterface.initOA();
        }
    }

    protected static void fini() {
        if (shutdown) {
            OAInterface.shutdownOA();
            ORBInterface.shutdownORB();
        }
    }
}
