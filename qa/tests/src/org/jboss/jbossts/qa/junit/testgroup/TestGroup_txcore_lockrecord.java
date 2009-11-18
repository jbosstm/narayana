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

public class TestGroup_txcore_lockrecord extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "txcore_lockrecord";
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


	@Test public void LockRecord_Test001()
	{
		setTestName("LockRecord_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test002()
	{
		setTestName("LockRecord_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test003()
	{
		setTestName("LockRecord_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test004()
	{
		setTestName("LockRecord_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test005()
	{
		setTestName("LockRecord_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test006()
	{
		setTestName("LockRecord_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test007()
	{
		setTestName("LockRecord_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test008()
	{
		setTestName("LockRecord_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test009()
	{
		setTestName("LockRecord_Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test010()
	{
		setTestName("LockRecord_Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test011()
	{
		setTestName("LockRecord_Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test012()
	{
		setTestName("LockRecord_Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test013()
	{
		setTestName("LockRecord_Test013");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test014()
	{
		setTestName("LockRecord_Test014");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test015()
	{
		setTestName("LockRecord_Test015");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("100", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Test016()
	{
		setTestName("LockRecord_Test016");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test001a()
	{
		setTestName("LockRecord_Thread_Test001a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test001b()
	{
		setTestName("LockRecord_Thread_Test001b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test002a()
	{
		setTestName("LockRecord_Thread_Test002a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test002b()
	{
		setTestName("LockRecord_Thread_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test003a()
	{
		setTestName("LockRecord_Thread_Test003a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test003b()
	{
		setTestName("LockRecord_Thread_Test003b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test004a()
	{
		setTestName("LockRecord_Thread_Test004a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test004b()
	{
		setTestName("LockRecord_Thread_Test004b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test005a()
	{
		setTestName("LockRecord_Thread_Test005a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test005b()
	{
		setTestName("LockRecord_Thread_Test005b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test006a()
	{
		setTestName("LockRecord_Thread_Test006a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test006b()
	{
		setTestName("LockRecord_Thread_Test006b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test007a()
	{
		setTestName("LockRecord_Thread_Test007a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test007b()
	{
		setTestName("LockRecord_Thread_Test007b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test008a()
	{
		setTestName("LockRecord_Thread_Test008a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test008b()
	{
		setTestName("LockRecord_Thread_Test008b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test009a()
	{
		setTestName("LockRecord_Thread_Test009a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test009b()
	{
		setTestName("LockRecord_Thread_Test009b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test010a()
	{
		setTestName("LockRecord_Thread_Test010a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test010b()
	{
		setTestName("LockRecord_Thread_Test010b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test011a()
	{
		setTestName("LockRecord_Thread_Test011a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test011b()
	{
		setTestName("LockRecord_Thread_Test011b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test012a()
	{
		setTestName("LockRecord_Thread_Test012a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test012b()
	{
		setTestName("LockRecord_Thread_Test012b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test013a()
	{
		setTestName("LockRecord_Thread_Test013a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test013b()
	{
		setTestName("LockRecord_Thread_Test013b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test014a()
	{
		setTestName("LockRecord_Thread_Test014a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test014b()
	{
		setTestName("LockRecord_Thread_Test014b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test015a()
	{
		setTestName("LockRecord_Thread_Test015a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test015b()
	{
		setTestName("LockRecord_Thread_Test015b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test016a()
	{
		setTestName("LockRecord_Thread_Test016a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test016b()
	{
		setTestName("LockRecord_Thread_Test016b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test017a()
	{
		setTestName("LockRecord_Thread_Test017a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test017b()
	{
		setTestName("LockRecord_Thread_Test017b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test018a()
	{
		setTestName("LockRecord_Thread_Test018a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test018b()
	{
		setTestName("LockRecord_Thread_Test018b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test019a()
	{
		setTestName("LockRecord_Thread_Test019a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test019b()
	{
		setTestName("LockRecord_Thread_Test019b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test020a()
	{
		setTestName("LockRecord_Thread_Test020a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test020b()
	{
		setTestName("LockRecord_Thread_Test020b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test021a()
	{
		setTestName("LockRecord_Thread_Test021a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test021b()
	{
		setTestName("LockRecord_Thread_Test021b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test022a()
	{
		setTestName("LockRecord_Thread_Test022a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test022b()
	{
		setTestName("LockRecord_Thread_Test022b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test023a()
	{
		setTestName("LockRecord_Thread_Test023a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test023b()
	{
		setTestName("LockRecord_Thread_Test023b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test024a()
	{
		setTestName("LockRecord_Thread_Test024a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test024b()
	{
		setTestName("LockRecord_Thread_Test024b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test025a()
	{
		setTestName("LockRecord_Thread_Test025a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test025b()
	{
		setTestName("LockRecord_Thread_Test025b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test026a()
	{
		setTestName("LockRecord_Thread_Test026a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test026b()
	{
		setTestName("LockRecord_Thread_Test026b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test027a()
	{
		setTestName("LockRecord_Thread_Test027a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test027b()
	{
		setTestName("LockRecord_Thread_Test027b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test028a()
	{
		setTestName("LockRecord_Thread_Test028a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test028b()
	{
		setTestName("LockRecord_Thread_Test028b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test029a()
	{
		setTestName("LockRecord_Thread_Test029a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test029b()
	{
		setTestName("LockRecord_Thread_Test029b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test030a()
	{
		setTestName("LockRecord_Thread_Test030a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test030b()
	{
		setTestName("LockRecord_Thread_Test030b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test031a()
	{
		setTestName("LockRecord_Thread_Test031a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test031b()
	{
		setTestName("LockRecord_Thread_Test031b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test032a()
	{
		setTestName("LockRecord_Thread_Test032a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test032b()
	{
		setTestName("LockRecord_Thread_Test032b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test033a()
	{
		setTestName("LockRecord_Thread_Test033a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test033b()
	{
		setTestName("LockRecord_Thread_Test033b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test034a()
	{
		setTestName("LockRecord_Thread_Test034a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test034b()
	{
		setTestName("LockRecord_Thread_Test034b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test035a()
	{
		setTestName("LockRecord_Thread_Test035a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test035b()
	{
		setTestName("LockRecord_Thread_Test035b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test036a()
	{
		setTestName("LockRecord_Thread_Test036a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test036b()
	{
		setTestName("LockRecord_Thread_Test036b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test037a()
	{
		setTestName("LockRecord_Thread_Test037a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test037b()
	{
		setTestName("LockRecord_Thread_Test037b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test038a()
	{
		setTestName("LockRecord_Thread_Test038a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test038b()
	{
		setTestName("LockRecord_Thread_Test038b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test039a()
	{
		setTestName("LockRecord_Thread_Test039a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test039b()
	{
		setTestName("LockRecord_Thread_Test039b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test040a()
	{
		setTestName("LockRecord_Thread_Test040a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test040b()
	{
		setTestName("LockRecord_Thread_Test040b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test041a()
	{
		setTestName("LockRecord_Thread_Test041a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test041b()
	{
		setTestName("LockRecord_Thread_Test041b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test042a()
	{
		setTestName("LockRecord_Thread_Test042a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test042b()
	{
		setTestName("LockRecord_Thread_Test042b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test043a()
	{
		setTestName("LockRecord_Thread_Test043a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test043b()
	{
		setTestName("LockRecord_Thread_Test043b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test044a()
	{
		setTestName("LockRecord_Thread_Test044a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test044b()
	{
		setTestName("LockRecord_Thread_Test044b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test045a()
	{
		setTestName("LockRecord_Thread_Test045a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test045b()
	{
		setTestName("LockRecord_Thread_Test045b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test046a()
	{
		setTestName("LockRecord_Thread_Test046a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test046b()
	{
		setTestName("LockRecord_Thread_Test046b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test047a()
	{
		setTestName("LockRecord_Thread_Test047a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test047b()
	{
		setTestName("LockRecord_Thread_Test047b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test048a()
	{
		setTestName("LockRecord_Thread_Test048a");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("-newlock", "$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Thread_Test048b()
	{
		setTestName("LockRecord_Thread_Test048b");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}
}