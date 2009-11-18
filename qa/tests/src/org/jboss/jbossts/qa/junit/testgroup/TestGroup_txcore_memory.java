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

/**
 * Memory leak tests gathered from txcore abstractrecord, lockrecord and statemanager.
 * Memory leak tests are centralized here as running htem requires a custom environment,
 * see run-tests.xml
 */
public class TestGroup_txcore_memory extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "txcore_memory";
	}


	@Before
    public void setUp()
	{
		super.setUp();
	}

	@After
    public void tearDown()
	{
		try {
		} finally {
			super.tearDown();
		}
	}

    /////////////////////////////////////////////////////

    @Test public void AbstractRecord_Memory_Test001()
    {
        setTestName("AbstractRecord_Memory_Test001");
        Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        client0.start("$(CALLS)", "1", "999");
        client0.waitFor();
        Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        task0.perform();
    }

    @Test public void AbstractRecord_Memory_Test002()
    {
        setTestName("AbstractRecord_Memory_Test002");
        Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        client0.start("$(CALLS)", "2", "999");
        client0.waitFor();
        Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        task0.perform();
    }

    @Test public void AbstractRecord_Memory_Test003()
    {
        setTestName("AbstractRecord_Memory_Test003");
        Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        client0.start("$(CALLS)", "5", "999");
        client0.waitFor();
        Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        task0.perform();
    }

    @Test public void AbstractRecord_Memory_Test004()
    {
        setTestName("AbstractRecord_Memory_Test004");
        Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        client0.start("$(CALLS)", "10", "999");
        client0.waitFor();
        Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        task0.perform();
    }

    @Test public void AbstractRecord_Memory_Test005()
    {
        setTestName("AbstractRecord_Memory_Test005");
        Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        client0.start("$(CALLS)", "1", "999");
        client0.waitFor();
        Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        task0.perform();
    }

    @Test public void AbstractRecord_Memory_Test006()
    {
        setTestName("AbstractRecord_Memory_Test006");
        Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        client0.start("$(CALLS)", "2", "999");
        client0.waitFor();
        Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
        task0.perform();
    }

	@Test public void AbstractRecord_Memory_Test007()
	{
		setTestName("AbstractRecord_Memory_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AbstractRecord_Memory_Test008()
	{
		setTestName("AbstractRecord_Memory_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

    /////////////////////////////////////////////////////


	@Test public void LockRecord_Memory_Test001()
	{
		setTestName("LockRecord_Memory_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test002()
	{
		setTestName("LockRecord_Memory_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test003()
	{
		setTestName("LockRecord_Memory_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test004()
	{
		setTestName("LockRecord_Memory_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test005()
	{
		setTestName("LockRecord_Memory_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test006()
	{
		setTestName("LockRecord_Memory_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test007()
	{
		setTestName("LockRecord_Memory_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test008()
	{
		setTestName("LockRecord_Memory_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test009()
	{
		setTestName("LockRecord_Memory_Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test010()
	{
		setTestName("LockRecord_Memory_Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test011()
	{
		setTestName("LockRecord_Memory_Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test012()
	{
		setTestName("LockRecord_Memory_Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test013()
	{
		setTestName("LockRecord_Memory_Test013");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test014()
	{
		setTestName("LockRecord_Memory_Test014");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test015()
	{
		setTestName("LockRecord_Memory_Test015");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void LockRecord_Memory_Test016()
	{
		setTestName("LockRecord_Memory_Test016");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

    /////////////////////////////////////////////////////
    
	@Test public void StateManager_Memory_Test001()
	{
		setTestName("StateManager_Memory_Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test002()
	{
		setTestName("StateManager_Memory_Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test003()
	{
		setTestName("StateManager_Memory_Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test004()
	{
		setTestName("StateManager_Memory_Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient001.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test005()
	{
		setTestName("StateManager_Memory_Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test006()
	{
		setTestName("StateManager_Memory_Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test007()
	{
		setTestName("StateManager_Memory_Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test008()
	{
		setTestName("StateManager_Memory_Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient002.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test009()
	{
		setTestName("StateManager_Memory_Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test010()
	{
		setTestName("StateManager_Memory_Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test011()
	{
		setTestName("StateManager_Memory_Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test012()
	{
		setTestName("StateManager_Memory_Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient003.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test013()
	{
		setTestName("StateManager_Memory_Test013");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "1", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test014()
	{
		setTestName("StateManager_Memory_Test014");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "2", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test015()
	{
		setTestName("StateManager_Memory_Test015");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "5", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void StateManager_Memory_Test016()
	{
		setTestName("StateManager_Memory_Test016");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient004.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("$(CALLS)", "10", "999");
		client0.waitFor();
		Task task0 = createTask("task0", org.jboss.jbossts.qa.ArjunaCore.Utils.EmptyObjectStore.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
    }
}