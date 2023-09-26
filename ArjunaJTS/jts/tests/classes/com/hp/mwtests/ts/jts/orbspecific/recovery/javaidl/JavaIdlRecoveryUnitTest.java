/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */



package com.hp.mwtests.ts.jts.orbspecific.recovery.javaidl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.omg.CosTransactions.RecoveryCoordinator;

import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators.JavaIdlRCManager;
import com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators.JavaIdlRCServiceInit;
import com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators.JavaIdlRCShutdown;
import com.arjuna.ats.internal.jts.orbspecific.javaidl.recoverycoordinators.JavaIdlRecoveryInit;
import com.hp.mwtests.ts.jts.resources.TestBase;

public class JavaIdlRecoveryUnitTest extends TestBase
{
    @Test
    public void testORBRCManager () throws Exception
    {
        JavaIdlRCManager manager = new JavaIdlRCManager();
        RecoveryCoordinator rc = manager.makeRC(new Uid(), new Uid(), new Uid(), false);

        assertTrue(rc == null);

        manager.destroy(rc);
        manager.destroyAll(null);
    }

    @Test
    public void testORBRCShutdown () throws Exception
    {
        JavaIdlRCShutdown shutdown = new JavaIdlRCShutdown();

        shutdown.work();
    }

    @Test
    public void testInit () throws Exception
    {
        JavaIdlRCServiceInit init = new JavaIdlRCServiceInit();

        assertFalse(init.startRCservice());

        JavaIdlRCServiceInit.shutdownRCService();

        assertTrue(JavaIdlRCServiceInit.type() != null);
    }

    @Test
    public void testRecoveryInit () throws Exception
    {
        JavaIdlRCServiceInit init = new JavaIdlRCServiceInit();

        assertFalse(init.startRCservice());

        JavaIdlRecoveryInit rinit = new JavaIdlRecoveryInit();

        JavaIdlRCServiceInit.shutdownRCService();
    }
/*
    @Test
    public void testRecoverIOR () throws Exception
    {
        try
        {
            String iorString = RecoverIOR.newObjectKey("foo", "bar");

            assertTrue(iorString != null);

            RecoverIOR.printIORinfo(iorString);
        }
        catch (final Exception ex)
        {
        }
    }*/
}