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
	public String getTestGroupName()
	{
		return "txcore_recovery";
	}


	@Before public void setUp()
	{
		super.setUp();
	}

	@After public void tearDown()
	{
		try {
		} finally {
			super.tearDown();
		}
	}

	@Test public void Recovery_Crash_AbstractRecord_Test001()
	{
		setTestName("Recovery_Crash_AbstractRecord_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "1", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "1", "$(1)");
		client1.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Recovery_Crash_AbstractRecord_Test002()
	{
		setTestName("Recovery_Crash_AbstractRecord_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "2", "$(1)");
		client1.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Recovery_Crash_AbstractRecord_Test003()
	{
		setTestName("Recovery_Crash_AbstractRecord_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "5", "$(1)");
		client1.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Recovery_Crash_AbstractRecord_Test004()
	{
		setTestName("Recovery_Crash_AbstractRecord_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "10", "$(1)");
		client1.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Recovery_Crash_LockManager_Test001()
	{
		setTestName("Recovery_Crash_LockManager_Test001");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "1", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "1", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_LockManager_Test002()
	{
		setTestName("Recovery_Crash_LockManager_Test002");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "2", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_LockManager_Test003()
	{
		setTestName("Recovery_Crash_LockManager_Test003");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "5", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_LockManager_Test004()
	{
		setTestName("Recovery_Crash_LockManager_Test004");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "10", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_LockManager_Test005()
	{
		setTestName("Recovery_Crash_LockManager_Test005");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "1", "3", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "1", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_LockManager_Test006()
	{
		setTestName("Recovery_Crash_LockManager_Test006");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "3", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "2", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_LockManager_Test007()
	{
		setTestName("Recovery_Crash_LockManager_Test007");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "3", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "5", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_LockManager_Test008()
	{
		setTestName("Recovery_Crash_LockManager_Test008");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "3", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.Client002a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "10", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_StateManager_Test001()
	{
		setTestName("Recovery_Crash_StateManager_Test001");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "1", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "1", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_StateManager_Test002()
	{
		setTestName("Recovery_Crash_StateManager_Test002");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "2", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_StateManager_Test003()
	{
		setTestName("Recovery_Crash_StateManager_Test003");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "5", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_StateManager_Test004()
	{
		setTestName("Recovery_Crash_StateManager_Test004");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "1", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "10", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_StateManager_Test005()
	{
		setTestName("Recovery_Crash_StateManager_Test005");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "1", "3", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "1", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_StateManager_Test006()
	{
		setTestName("Recovery_Crash_StateManager_Test006");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "3", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "2", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_StateManager_Test007()
	{
		setTestName("Recovery_Crash_StateManager_Test007");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "3", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "5", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Crash_StateManager_Test008()
	{
		setTestName("Recovery_Crash_StateManager_Test008");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "3", "0", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.Client002a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "10", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Fail_AbstractRecord_Test001()
	{
		setTestName("Recovery_Fail_AbstractRecord_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "1", "1", "$(1)");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Recovery_Fail_AbstractRecord_Test002()
	{
		setTestName("Recovery_Fail_AbstractRecord_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "3", "1", "1", "$(1)");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Recovery_Fail_AbstractRecord_Test003()
	{
		setTestName("Recovery_Fail_AbstractRecord_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "1", "1", "$(1)");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Recovery_Fail_AbstractRecord_Test004()
	{
		setTestName("Recovery_Fail_AbstractRecord_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "1", "1", "$(1)");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Recovery_Restore_AbstractRecord_Test001()
	{
		setTestName("Recovery_Restore_AbstractRecord_Test001");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "1", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "1", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_AbstractRecord_Test002()
	{
		setTestName("Recovery_Restore_AbstractRecord_Test002");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "2", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_AbstractRecord_Test003()
	{
		setTestName("Recovery_Restore_AbstractRecord_Test003");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "5", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_AbstractRecord_Test004()
	{
		setTestName("Recovery_Restore_AbstractRecord_Test004");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "10", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_LockManager_Test001()
	{
		setTestName("Recovery_Restore_LockManager_Test001");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "1", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "1", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_LockManager_Test002()
	{
		setTestName("Recovery_Restore_LockManager_Test002");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "2", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_LockManager_Test003()
	{
		setTestName("Recovery_Restore_LockManager_Test003");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "5", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_LockManager_Test004()
	{
		setTestName("Recovery_Restore_LockManager_Test004");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.LockManager.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "10", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_StateManager_Test001()
	{
		setTestName("Recovery_Restore_StateManager_Test001");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "1", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "1", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_StateManager_Test002()
	{
		setTestName("Recovery_Restore_StateManager_Test002");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "2", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "2", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_StateManager_Test003()
	{
		setTestName("Recovery_Restore_StateManager_Test003");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "5", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}

	@Test public void Recovery_Restore_StateManager_Test004()
	{
		setTestName("Recovery_Restore_StateManager_Test004");
		Task server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001b.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "10", "$(1)");
		client0.waitFor();
		Task client1 = createTask("client1", org.jboss.jbossts.qa.ArjunaCore.StateManager.CrashRecovery.client.RestoreClient001a.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("100", "10", "$(1)");
		client1.waitFor();
		server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
		Task task2 = createTask("task2", org.jboss.jbossts.qa.Utils.RemoveObjectUidStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task2.perform();
	}
}