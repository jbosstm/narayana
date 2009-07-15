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

public class TestGroup_txcore extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "txcore";
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

	@Test public void TX_Statistics_Test001()
	{
		setTestName("TX_Statistics_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test002()
	{
		setTestName("TX_Statistics_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test003()
	{
		setTestName("TX_Statistics_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "3");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test004()
	{
		setTestName("TX_Statistics_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "4");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test005()
	{
		setTestName("TX_Statistics_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test006()
	{
		setTestName("TX_Statistics_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test007()
	{
		setTestName("TX_Statistics_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "3");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test008()
	{
		setTestName("TX_Statistics_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "4");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test009()
	{
		setTestName("TX_Statistics_Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test010()
	{
		setTestName("TX_Statistics_Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test011()
	{
		setTestName("TX_Statistics_Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "3");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test012()
	{
		setTestName("TX_Statistics_Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "4");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test013()
	{
		setTestName("TX_Statistics_Test013");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test014()
	{
		setTestName("TX_Statistics_Test014");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test015()
	{
		setTestName("TX_Statistics_Test015");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "3");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void TX_Statistics_Test016()
	{
		setTestName("TX_Statistics_Test016");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Stats.Client004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "4");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void Uid_Test001()
	{
		setTestName("Uid_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("1", "100");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test002()
	{
		setTestName("Uid_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("1", "1000");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test003()
	{
		setTestName("Uid_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("1", "10000");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test004()
	{
		setTestName("Uid_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("10", "100");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test005()
	{
		setTestName("Uid_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("10", "800");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test006()
	{
		setTestName("Uid_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("10", "1000");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test007()
	{
		setTestName("Uid_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("20", "100");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test008()
	{
		setTestName("Uid_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("20", "1000");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test009()
	{
		setTestName("Uid_Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("20", "2000");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test010()
	{
		setTestName("Uid_Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("25", "100");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test011()
	{
		setTestName("Uid_Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("28", "100");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

	@Test public void Uid_Test012()
	{
		setTestName("Uid_Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UidTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("30", "50");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		Task task1 = createTask("task1", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task1.perform();
	}

}