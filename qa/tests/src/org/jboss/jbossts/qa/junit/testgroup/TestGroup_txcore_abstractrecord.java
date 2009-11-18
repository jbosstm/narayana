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

public class TestGroup_txcore_abstractrecord extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "txcore_abstractrecord";
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



	@Test public void AbstractRecord_Test001()
	{
		setTestName("AbstractRecord_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Test002()
	{
		setTestName("AbstractRecord_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Test003()
	{
		setTestName("AbstractRecord_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Test004()
	{
		setTestName("AbstractRecord_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Test005()
	{
		setTestName("AbstractRecord_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Test006()
	{
		setTestName("AbstractRecord_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Test007()
	{
		setTestName("AbstractRecord_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Test008()
	{
		setTestName("AbstractRecord_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test001()
	{
		setTestName("AbstractRecord_Thread_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test002()
	{
		setTestName("AbstractRecord_Thread_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test003()
	{
		setTestName("AbstractRecord_Thread_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test004()
	{
		setTestName("AbstractRecord_Thread_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test005()
	{
		setTestName("AbstractRecord_Thread_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test006()
	{
		setTestName("AbstractRecord_Thread_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test007()
	{
		setTestName("AbstractRecord_Thread_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test008()
	{
		setTestName("AbstractRecord_Thread_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test009()
	{
		setTestName("AbstractRecord_Thread_Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test010()
	{
		setTestName("AbstractRecord_Thread_Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test011()
	{
		setTestName("AbstractRecord_Thread_Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test012()
	{
		setTestName("AbstractRecord_Thread_Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test013()
	{
		setTestName("AbstractRecord_Thread_Test013");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test014()
	{
		setTestName("AbstractRecord_Thread_Test014");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test015()
	{
		setTestName("AbstractRecord_Thread_Test015");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test016()
	{
		setTestName("AbstractRecord_Thread_Test016");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test017()
	{
		setTestName("AbstractRecord_Thread_Test017");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test018()
	{
		setTestName("AbstractRecord_Thread_Test018");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test019()
	{
		setTestName("AbstractRecord_Thread_Test019");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test020()
	{
		setTestName("AbstractRecord_Thread_Test020");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "5");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test021()
	{
		setTestName("AbstractRecord_Thread_Test021");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test022()
	{
		setTestName("AbstractRecord_Thread_Test022");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test023()
	{
		setTestName("AbstractRecord_Thread_Test023");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Thread_Test024()
	{
		setTestName("AbstractRecord_Thread_Test024");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "10");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}
}