/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

/**
 * Memory leak tests gathered from txcore abstractrecord, lockrecord and statemanager.
 * Memory leak tests are centralized here as running them requires a custom environment,
 * see run-tests.xml
 */
public class TestGroup_txcore_memory extends TestGroupBase
{
    @Test public void AbstractRecord_Memory_Test001()
    {
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient001.class, "$(CALLS)", "1", "999");
    }

    @Test public void AbstractRecord_Memory_Test002()
    {
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient001.class, "$(CALLS)", "2", "999");
    }

    @Test public void AbstractRecord_Memory_Test003()
    {
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient001.class, "$(CALLS)", "5", "999");
    }

    @Test public void AbstractRecord_Memory_Test004()
    {
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient001.class, "$(CALLS)", "10", "999");
    }


    @Test public void AbstractRecord_Memory_Test005()
    {
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient002.class, "$(CALLS)", "1", "999");
    }

    @Test public void AbstractRecord_Memory_Test006()
    {
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient002.class, "$(CALLS)", "2", "999");
    }

	@Test public void AbstractRecord_Memory_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient002.class, "$(CALLS)", "5", "999");
	}

	@Test public void AbstractRecord_Memory_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.MemoryClient002.class, "$(CALLS)", "10", "999");
	}

    /////////////////////////////////////////////////////


	@Test public void LockRecord_Memory_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient001.class, "$(CALLS)", "1", "999");
	}

	@Test public void LockRecord_Memory_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient001.class, "$(CALLS)", "2", "999");
	}

	@Test public void LockRecord_Memory_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient001.class, "$(CALLS)", "5", "999");
	}

	@Test public void LockRecord_Memory_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient001.class, "$(CALLS)", "10", "999");
	}

	@Test public void LockRecord_Memory_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient002.class, "$(CALLS)", "1", "999");
	}

	@Test public void LockRecord_Memory_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient002.class, "$(CALLS)", "2", "999");
	}

	@Test public void LockRecord_Memory_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient002.class, "$(CALLS)", "5", "999");
	}

	@Test public void LockRecord_Memory_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient002.class, "$(CALLS)", "10", "999");
	}

	@Test public void LockRecord_Memory_Test009()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient003.class, "$(CALLS)", "1", "999");
	}

	@Test public void LockRecord_Memory_Test010()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient003.class, "$(CALLS)", "2", "999");
	}

	@Test public void LockRecord_Memory_Test011()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient003.class, "$(CALLS)", "5", "999");
	}

	@Test public void LockRecord_Memory_Test012()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient003.class, "$(CALLS)", "10", "999");
	}

	@Test public void LockRecord_Memory_Test013()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient004.class, "$(CALLS)", "1", "999");
	}

	@Test public void LockRecord_Memory_Test014()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient004.class, "$(CALLS)", "2", "999");
	}

	@Test public void LockRecord_Memory_Test015()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient004.class, "$(CALLS)", "5", "999");
	}

	@Test public void LockRecord_Memory_Test016()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.MemoryClient004.class, "$(CALLS)", "10", "999");
	}

    /////////////////////////////////////////////////////
    
	@Test public void StateManager_Memory_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient001.class, "$(CALLS)", "1", "999");
	}

	@Test public void StateManager_Memory_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient001.class, "$(CALLS)", "2", "999");
	}

	@Test public void StateManager_Memory_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient001.class, "$(CALLS)", "5", "999");
	}

	@Test public void StateManager_Memory_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient001.class, "$(CALLS)", "10", "999");
	}

	@Test public void StateManager_Memory_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient002.class, "$(CALLS)", "1", "999");
	}

	@Test public void StateManager_Memory_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient002.class, "$(CALLS)", "2", "999");
	}

	@Test public void StateManager_Memory_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient002.class, "$(CALLS)", "5", "999");
	}

	@Test public void StateManager_Memory_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient002.class, "$(CALLS)", "10", "999");
	}

	@Test public void StateManager_Memory_Test009()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient003.class, "$(CALLS)", "1", "999");
	}

	@Test public void StateManager_Memory_Test010()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient003.class, "$(CALLS)", "2", "999");
	}

	@Test public void StateManager_Memory_Test011()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient003.class, "$(CALLS)", "5", "999");
	}

	@Test public void StateManager_Memory_Test012()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient003.class, "$(CALLS)", "10", "999");
	}

	@Test public void StateManager_Memory_Test013()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient004.class, "$(CALLS)", "1", "999");
	}

	@Test public void StateManager_Memory_Test014()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient004.class, "$(CALLS)", "2", "999");
	}

	@Test public void StateManager_Memory_Test015()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient004.class, "$(CALLS)", "5", "999");
	}

	@Test public void StateManager_Memory_Test016()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.MemoryClient004.class, "$(CALLS)", "10", "999");
    }
}