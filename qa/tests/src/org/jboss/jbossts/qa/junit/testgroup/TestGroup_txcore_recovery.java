/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
 * (C) 2009,
 * @author JBoss Inc.
 */
package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

public class TestGroup_txcore_recovery extends TestGroupBase
{
	@After public void tearDown()
	{
        try {
            Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
            task1.perform();
            Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
            task2.perform();
        } finally {
            super.tearDown();
        }
	}

	@Test public void Recovery_Crash_AbstractRecord_Test001()
	{
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001b.class, "100", "1", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001a.class, "100", "1", "$(1)");
	}

	@Test public void Recovery_Crash_AbstractRecord_Test002()
	{
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001b.class, "100", "2", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001a.class, "100", "2", "$(1)");
	}

	@Test public void Recovery_Crash_AbstractRecord_Test003()
	{
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001b.class, "100", "5", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001a.class, "100", "5", "$(1)");
	}

	@Test public void Recovery_Crash_AbstractRecord_Test004()
	{
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001b.class, "100", "10", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001a.class, "100", "10", "$(1)");
	}


	@Test public void Recovery_Crash_LockManager_Test001()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001b.class, "100", "1", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001a.class, "100", "1", "$(1)");
	}

	@Test public void Recovery_Crash_LockManager_Test002()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001b.class, "100", "2", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001a.class, "100", "2", "$(1)");
	}

	@Test public void Recovery_Crash_LockManager_Test003()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001b.class, "100", "5", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001a.class, "100", "5", "$(1)");
	}

	@Test public void Recovery_Crash_LockManager_Test004()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001b.class, "100", "10", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001a.class, "100", "10", "$(1)");
	}

	@Test public void Recovery_Crash_LockManager_Test005()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002b.class, "100", "1", "3", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002a.class, "100", "1", "$(1)");
	}

	@Test public void Recovery_Crash_LockManager_Test006()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002b.class, "100", "2", "3", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002a.class, "100", "2", "$(1)");
	}

	@Test public void Recovery_Crash_LockManager_Test007()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002b.class, "100", "5", "3", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002a.class, "100", "5", "$(1)");
	}

	@Test public void Recovery_Crash_LockManager_Test008()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002b.class, "100", "10", "3", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002a.class, "100", "10", "$(1)");
	}


	@Test public void Recovery_Crash_StateManager_Test001()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001b.class, "100", "1", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001a.class, "100", "1", "$(1)");
	}

	@Test public void Recovery_Crash_StateManager_Test002()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001b.class, "100", "2", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001a.class, "100", "2", "$(1)");
	}

	@Test public void Recovery_Crash_StateManager_Test003()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001b.class, "100", "5", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001a.class, "100", "5", "$(1)");
	}

	@Test public void Recovery_Crash_StateManager_Test004()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001b.class, "100", "10", "1", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001a.class, "100", "10", "$(1)");
	}

	@Test public void Recovery_Crash_StateManager_Test005()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002b.class, "100", "1", "3", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002a.class, "100", "1", "$(1)");
	}

	@Test public void Recovery_Crash_StateManager_Test006()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002b.class, "100", "2", "3", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002a.class, "100", "2", "$(1)");
	}

	@Test public void Recovery_Crash_StateManager_Test007()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002b.class, "100", "5", "3", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002a.class, "100", "5", "$(1)");
	}

	@Test public void Recovery_Crash_StateManager_Test008()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002b.class, "100", "10", "3", "0", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002a.class, "100", "10", "$(1)");
	}


	@Test public void Recovery_Fail_AbstractRecord_Test001()
	{
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001.class, "100", "2", "1", "1", "$(1)");
	}

	@Test public void Recovery_Fail_AbstractRecord_Test002()
	{
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001.class, "100", "3", "1", "1", "$(1)");
	}

	@Test public void Recovery_Fail_AbstractRecord_Test003()
	{
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001.class, "100", "5", "1", "1", "$(1)");
	}

	@Test public void Recovery_Fail_AbstractRecord_Test004()
	{
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001.class, "100", "10", "1", "1", "$(1)");
	}

    
	@Test public void Recovery_Restore_AbstractRecord_Test001()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001b.class, "100", "1", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001a.class, "100", "1", "$(1)");
	}

	@Test public void Recovery_Restore_AbstractRecord_Test002()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001b.class, "100", "2", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001a.class, "100", "2", "$(1)");
	}

	@Test public void Recovery_Restore_AbstractRecord_Test003()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001b.class, "100", "5", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001a.class, "100", "5", "$(1)");
	}

	@Test public void Recovery_Restore_AbstractRecord_Test004()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001b.class, "100", "10", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001a.class, "100", "10", "$(1)");
	}


	@Test public void Recovery_Restore_LockManager_Test001()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001b.class, "100", "1", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001a.class, "100", "1", "$(1)");
	}

	@Test public void Recovery_Restore_LockManager_Test002()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001b.class, "100", "2", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001a.class, "100", "2", "$(1)");
	}

	@Test public void Recovery_Restore_LockManager_Test003()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001b.class, "100", "5", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001a.class, "100", "5", "$(1)");
	}

	@Test public void Recovery_Restore_LockManager_Test004()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001b.class, "100", "10", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001a.class, "100", "10", "$(1)");
	}

	@Test public void Recovery_Restore_StateManager_Test001()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001b.class, "100", "1", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001a.class, "100", "1", "$(1)");
	}

	@Test public void Recovery_Restore_StateManager_Test002()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001b.class, "100", "2", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001a.class, "100", "2", "$(1)");
	}

	@Test public void Recovery_Restore_StateManager_Test003()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001b.class, "100", "5", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001a.class, "100", "5", "$(1)");
	}

	@Test public void Recovery_Restore_StateManager_Test004()
	{
        startServer(com.arjuna.ats.arjuna.recovery.RecoveryManager.class, "-test");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001b.class, "100", "10", "$(1)");
        startAndWaitForClientWithFixedStoreDir(org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001a.class, "100", "10", "$(1)");
	}
}