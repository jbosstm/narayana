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

public class TestGroup_aitresources01 extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "aitresources01";
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
		Task cleanup0 = createTask("cleanup0", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		cleanup0.perform("$(1)");
		} finally {
			super.tearDown();
		}
	}

	@Test public void AITResources01_Test001_F()
	{
		setTestName("Test001_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test002_F()
	{
		setTestName("Test002_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test003_F()
	{
		setTestName("Test003_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test004_F()
	{
		setTestName("Test004_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test005_F()
	{
		setTestName("Test005_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)", "$(3)", "$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test006_F()
	{
		setTestName("Test006_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test007_F()
	{
		setTestName("Test007_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test008_F()
	{
		setTestName("Test008_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test009_F()
	{
		setTestName("Test009_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test010_F()
	{
		setTestName("Test010_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server04.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)", "$(3)", "$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test011_F()
	{
		setTestName("Test011_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server05.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test012_F()
	{
		setTestName("Test012_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server06.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)", "$(3)", "$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test013_F()
	{
		setTestName("Test013_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test014_F()
	{
		setTestName("Test014_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test015_F()
	{
		setTestName("Test015_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test016_F()
	{
		setTestName("Test016_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test017_F()
	{
		setTestName("Test017_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)", "$(3)", "$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test018_F()
	{
		setTestName("Test018_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test019_F()
	{
		setTestName("Test019_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test020_F()
	{
		setTestName("Test020_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test021_F()
	{
		setTestName("Test021_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test022_F()
	{
		setTestName("Test022_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server04.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)", "$(3)", "$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test023_F()
	{
		setTestName("Test023_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server05.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test024_F()
	{
		setTestName("Test024_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server06.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)", "$(3)", "$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test025_M()
	{
		setTestName("Test025_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client03.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "1000", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test026_M()
	{
		setTestName("Test026_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client04.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "1000", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test027_F()
	{
		setTestName("Test027_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client05.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test028_F()
	{
		setTestName("Test028_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client06.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test029_F()
	{
		setTestName("Test029_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client06.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test030_M()
	{
		setTestName("Test030_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client07.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "10", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test031_M()
	{
		setTestName("Test031_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client08.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)", "10", "999", "999");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test032_M()
	{
		setTestName("Test032_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client08.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)", "10", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test033_F()
	{
		setTestName("Test033_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(1)");
		client0.waitFor();
		client1.waitFor();
		Task outcome0 = createTask("outcome0", org.jboss.jbossts.qa.AITResources01Outcomes.Outcome01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		outcome0.perform("2000", "$(1)");
		server1.terminate();
	}

	@Test public void AITResources01_Test034_F()
	{
		setTestName("Test034_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(1)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(1)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(1)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		Task outcome0 = createTask("outcome0", org.jboss.jbossts.qa.AITResources01Outcomes.Outcome01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		outcome0.perform("4000", "$(1)");
		server1.terminate();
	}

	@Test public void AITResources01_Test035_F()
	{
		setTestName("Test035_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(1)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(2)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		Task outcome0 = createTask("outcome0", org.jboss.jbossts.qa.AITResources01Outcomes.Outcome02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		outcome0.perform("2000", "$(1)", "$(2)");
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test036_F()
	{
		setTestName("Test036_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server05.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(1)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(2)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client09.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		Task outcome0 = createTask("outcome0", org.jboss.jbossts.qa.AITResources01Outcomes.Outcome02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		outcome0.perform("2000", "$(1)", "$(2)");
		server1.terminate();
	}

	@Test public void AITResources01_Test037_F()
	{
		setTestName("Test037_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client10.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test038_F()
	{
		setTestName("Test038_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client11.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test039_F()
	{
		setTestName("Test039_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client11.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test040_M()
	{
		setTestName("Test040_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client12.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "10", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test041_M()
	{
		setTestName("Test041_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server07.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client13.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)", "10", "999", "999");
		client0.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test042_M()
	{
		setTestName("Test042_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server08.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client13.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "$(2)", "10", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test043_F()
	{
		setTestName("Test043_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test044_F()
	{
		setTestName("Test044_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test045_F()
	{
		setTestName("Test045_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test046_F()
	{
		setTestName("Test046_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(2)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(3)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
		server1.terminate();
	}

	@Test public void AITResources01_Test047_F()
	{
		setTestName("Test047_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server10.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)", "$(2)", "$(3)", "$(4)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		Task client3 = createTask("client3", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client3.start("$(4)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		client3.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test048_M()
	{
		setTestName("Test048_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client04.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "1000", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test049_F()
	{
		setTestName("Test049_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client14.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test050_M()
	{
		setTestName("Test050_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client15.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test051_F()
	{
		setTestName("Test051_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client14.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test052_M()
	{
		setTestName("Test052_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client15.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test053_F()
	{
		setTestName("Test053_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client16.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test054_M()
	{
		setTestName("Test054_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client17.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test055_F()
	{
		setTestName("Test055_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client16.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test056_M()
	{
		setTestName("Test056_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client17.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test057_F()
	{
		setTestName("Test057_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client16.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test058_M()
	{
		setTestName("Test058_M");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server09.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client17.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)", "4", "250", "999", "999");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test059_F()
	{
		setTestName("Test059_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server11.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

	@Test public void AITResources01_Test060_F()
	{
		setTestName("Test060_F");
		Task server1 = createTask("server1", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client19.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server1.terminate();
	}

}