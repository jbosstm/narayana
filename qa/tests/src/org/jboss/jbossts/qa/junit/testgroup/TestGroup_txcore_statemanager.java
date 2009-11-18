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

public class TestGroup_txcore_statemanager extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "txcore_statemanager";
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



	@Test public void StateManager_Test001()
	{
		setTestName("StateManager_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test002()
	{
		setTestName("StateManager_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test003()
	{
		setTestName("StateManager_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test004()
	{
		setTestName("StateManager_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test005()
	{
		setTestName("StateManager_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test006()
	{
		setTestName("StateManager_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test007()
	{
		setTestName("StateManager_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test008()
	{
		setTestName("StateManager_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test009()
	{
		setTestName("StateManager_Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test010()
	{
		setTestName("StateManager_Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test011()
	{
		setTestName("StateManager_Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test012()
	{
		setTestName("StateManager_Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test013()
	{
		setTestName("StateManager_Test013");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test014()
	{
		setTestName("StateManager_Test014");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test015()
	{
		setTestName("StateManager_Test015");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Test016()
	{
		setTestName("StateManager_Test016");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test001()
	{
		setTestName("StateManager_Thread_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test002()
	{
		setTestName("StateManager_Thread_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test003()
	{
		setTestName("StateManager_Thread_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test004()
	{
		setTestName("StateManager_Thread_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test005()
	{
		setTestName("StateManager_Thread_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test006()
	{
		setTestName("StateManager_Thread_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test007()
	{
		setTestName("StateManager_Thread_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test008()
	{
		setTestName("StateManager_Thread_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test009()
	{
		setTestName("StateManager_Thread_Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test010()
	{
		setTestName("StateManager_Thread_Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test011()
	{
		setTestName("StateManager_Thread_Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test012()
	{
		setTestName("StateManager_Thread_Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test013()
	{
		setTestName("StateManager_Thread_Test013");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test014()
	{
		setTestName("StateManager_Thread_Test014");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test015()
	{
		setTestName("StateManager_Thread_Test015");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test016()
	{
		setTestName("StateManager_Thread_Test016");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test017()
	{
		setTestName("StateManager_Thread_Test017");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test018()
	{
		setTestName("StateManager_Thread_Test018");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test019()
	{
		setTestName("StateManager_Thread_Test019");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test020()
	{
		setTestName("StateManager_Thread_Test020");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test021()
	{
		setTestName("StateManager_Thread_Test021");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test022()
	{
		setTestName("StateManager_Thread_Test022");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test023()
	{
		setTestName("StateManager_Thread_Test023");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test024()
	{
		setTestName("StateManager_Thread_Test024");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test025()
	{
		setTestName("StateManager_Thread_Test025");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test026()
	{
		setTestName("StateManager_Thread_Test026");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test027()
	{
		setTestName("StateManager_Thread_Test027");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test028()
	{
		setTestName("StateManager_Thread_Test028");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test029()
	{
		setTestName("StateManager_Thread_Test029");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test030()
	{
		setTestName("StateManager_Thread_Test030");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test031()
	{
		setTestName("StateManager_Thread_Test031");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test032()
	{
		setTestName("StateManager_Thread_Test032");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test033()
	{
		setTestName("StateManager_Thread_Test033");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test034()
	{
		setTestName("StateManager_Thread_Test034");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test035()
	{
		setTestName("StateManager_Thread_Test035");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test036()
	{
		setTestName("StateManager_Thread_Test036");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test037()
	{
		setTestName("StateManager_Thread_Test037");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test038()
	{
		setTestName("StateManager_Thread_Test038");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test039()
	{
		setTestName("StateManager_Thread_Test039");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test040()
	{
		setTestName("StateManager_Thread_Test040");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test041()
	{
		setTestName("StateManager_Thread_Test041");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test042()
	{
		setTestName("StateManager_Thread_Test042");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test043()
	{
		setTestName("StateManager_Thread_Test043");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test044()
	{
		setTestName("StateManager_Thread_Test044");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test045()
	{
		setTestName("StateManager_Thread_Test045");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test046()
	{
		setTestName("StateManager_Thread_Test046");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test047()
	{
		setTestName("StateManager_Thread_Test047");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Thread_Test048()
	{
		setTestName("StateManager_Thread_Test048");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}
}