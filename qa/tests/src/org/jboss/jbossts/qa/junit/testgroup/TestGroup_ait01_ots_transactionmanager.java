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

public class TestGroup_ait01_ots_transactionmanager extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "ait01_ots_transactionmanager";
	}

	protected Task server1 = null;
	protected Task server0 = null;

	@Before public void setUp()
	{
		super.setUp();
		server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		server0.start("-test");
		server1 = createTask("server1", com.arjuna.ats.jts.TransactionServer.class, Task.TaskType.EXPECT_READY, 480);
		server1.start("-test");
		Task setup0 = createTask("setup0", org.jboss.jbossts.qa.Utils.RegisterOTSServer2.class, Task.TaskType.EXPECT_READY, 480);
		setup0.perform();
		Task setup1 = createTask("setup1", org.jboss.jbossts.qa.Utils.SetupOTSServer2.class, Task.TaskType.EXPECT_READY, 480);
		setup1.perform();
	}

	@After public void tearDown()
	{
		try {
			server0.terminate();
			server1.terminate();
		Task cleanup0 = createTask("cleanup0", org.jboss.jbossts.qa.Utils.RemoveServerIORStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		cleanup0.perform("$(1)");
		} finally {
			super.tearDown();
		}
	}

	@Test public void AIT01_OTS_TransactionManager_Test001_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test001_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test002_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test002_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test003_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test003_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test004_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test004_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(3)");
		Task server5 = createTask("server5", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server5.start("$(4)");
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
		server5.terminate();
		server4.terminate();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test005_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test005_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)", "$(2)", "$(3)", "$(4)");
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
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test006_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test006_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test007_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test007_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test008_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test008_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test009_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test009_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(3)");
		Task server5 = createTask("server5", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server5.start("$(4)");
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
		server5.terminate();
		server4.terminate();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test010_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test010_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server04.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)", "$(2)", "$(3)", "$(4)");
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
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test011_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test011_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server05.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)", "$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client01.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test012_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test012_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server06.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)", "$(2)", "$(3)", "$(4)");
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
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test013_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test013_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test014_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test014_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test015_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test015_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test016_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test016_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(3)");
		Task server5 = createTask("server5", org.jboss.jbossts.qa.AITResources01Servers.Server01.class, Task.TaskType.EXPECT_READY, 480);
		server5.start("$(4)");
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
		server5.terminate();
		server4.terminate();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test017_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test017_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server02.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)", "$(2)", "$(3)", "$(4)");
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
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test018_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test018_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		client0.waitFor();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test019_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test019_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		client0.waitFor();
		client1.waitFor();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test020_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test020_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(3)");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(1)");
		Task client1 = createTask("client1", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client1.start("$(2)");
		Task client2 = createTask("client2", org.jboss.jbossts.qa.AITResources01Clients.Client02.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client2.start("$(3)");
		client0.waitFor();
		client1.waitFor();
		client2.waitFor();
		server4.terminate();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test021_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test021_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)");
		Task server3 = createTask("server3", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server3.start("$(2)");
		Task server4 = createTask("server4", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server4.start("$(3)");
		Task server5 = createTask("server5", org.jboss.jbossts.qa.AITResources01Servers.Server03.class, Task.TaskType.EXPECT_READY, 480);
		server5.start("$(4)");
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
		server5.terminate();
		server4.terminate();
		server3.terminate();
		server2.terminate();
	}

	@Test public void AIT01_OTS_TransactionManager_Test022_F()
	{
		setTestName("AIT01_OTS_TransactionManager_Test022_F");
		Task server2 = createTask("server2", org.jboss.jbossts.qa.AITResources01Servers.Server04.class, Task.TaskType.EXPECT_READY, 480);
		server2.start("$(1)", "$(2)", "$(3)", "$(4)");
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
		server2.terminate();
	}

}