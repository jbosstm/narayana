/*
 * SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

// Automatically generated by XML2JUnit
public class TestGroup_perfprofile01_e extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "perfprofile01_e_ait01_explicitobject_notran";
	}

	protected Task server0 = null;

	@Before public void setUp()
	{
		super.setUp();
		server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
	}

	@After public void tearDown()
	{
		try {
			server0.terminate();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform("$(1)");
		} finally {
			super.tearDown();
		}
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_NoTran_NoTranNullOper()
	{
		setTestName("AIT01_ExplicitObject_NoTran_NoTranNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_NoTran_NoTranNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "10000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_NoTran_TranCommitNullOper()
	{
		setTestName("AIT01_ExplicitObject_NoTran_TranCommitNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_NoTran_TranCommitNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_NoTran_TranCommitReadLock()
	{
		setTestName("AIT01_ExplicitObject_NoTran_TranCommitReadLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_NoTran_TranCommitReadLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_NoTran_TranCommitWriteLock()
	{
		setTestName("AIT01_ExplicitObject_NoTran_TranCommitWriteLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_NoTran_TranCommitWriteLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_NoTran_TranRollbackNullOper()
	{
		setTestName("AIT01_ExplicitObject_NoTran_TranRollbackNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_NoTran_TranRollbackNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_NoTran_TranRollbackReadLock()
	{
		setTestName("AIT01_ExplicitObject_NoTran_TranRollbackReadLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_NoTran_TranRollbackReadLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_NoTran_TranRollbackWriteLock()
	{
		setTestName("AIT01_ExplicitObject_NoTran_TranRollbackWriteLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_NoTran_TranRollbackWriteLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_NoTranNullOper()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_NoTranNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_NoTranNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_NoTranReadLock()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_NoTranReadLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_NoTranReadLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_NoTranWriteLock()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_NoTranWriteLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_NoTranWriteLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_TranCommitNullOper()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_TranCommitNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_TranCommitNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_TranCommitReadLock()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_TranCommitReadLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_TranCommitReadLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_TranCommitWriteLock()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_TranCommitWriteLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_TranCommitWriteLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_TranRollbackNullOper()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_TranRollbackNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_TranRollbackNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_TranRollbackReadLock()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_TranRollbackReadLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_TranRollbackReadLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranCommit_TranRollbackWriteLock()
	{
		setTestName("AIT01_ExplicitObject_TranCommit_TranRollbackWriteLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranCommit_TranRollbackWriteLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_NoTranNullOper()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_NoTranNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_NoTranNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_NoTranReadLock()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_NoTranReadLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_NoTranReadLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_NoTranWriteLock()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_NoTranWriteLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_NoTranWriteLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_TranCommitNullOper()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_TranCommitNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_TranCommitNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_TranCommitReadLock()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_TranCommitReadLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_TranCommitReadLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_TranCommitWriteLock()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_TranCommitWriteLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_TranCommitWriteLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_TranRollbackNullOper()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_TranRollbackNullOper");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_TranRollbackNullOper.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_TranRollbackReadLock()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_TranRollbackReadLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_TranRollbackReadLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void PerfProfile01_E_AIT01_ExplicitObject_TranRollback_TranRollbackWriteLock()
	{
		setTestName("AIT01_ExplicitObject_TranRollback_TranRollbackWriteLock");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.PerfProfile01Servers.Server_AIT01_ExplicitObject.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.PerfProfile01Clients.Client_ExplicitObject_TranRollback_TranRollbackWriteLock.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("AIT01", "1000", "$(1)");
		client0.waitFor();
		server1.terminate();
	}

}