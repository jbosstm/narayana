/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

public class TestGroup_txcore_abstractrecord extends TestGroupBase
{
	@Test public void AbstractRecord_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client001.class, "$(CALLS)", "1");
	}

	@Test public void AbstractRecord_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client001.class, "$(CALLS)", "2");
	}

	@Test public void AbstractRecord_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client001.class, "$(CALLS)", "5");
	}

	@Test public void AbstractRecord_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client001.class, "$(CALLS)", "10");
	}


	@Test public void AbstractRecord_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client002.class, "$(CALLS)", "1");
	}

	@Test public void AbstractRecord_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client002.class, "$(CALLS)", "2");
	}

	@Test public void AbstractRecord_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client002.class, "$(CALLS)", "5");
	}

	@Test public void AbstractRecord_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.Client002.class, "$(CALLS)", "10");
	}
    

	@Test public void AbstractRecord_Thread_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, "$(CALLS)", "1", "2");
	}

	@Test public void AbstractRecord_Thread_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, "$(CALLS)", "2", "2");
	}

	@Test public void AbstractRecord_Thread_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class, "$(CALLS)", "5", "2");
	}

	@Test public void AbstractRecord_Thread_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "10", "2");
	}

	@Test public void AbstractRecord_Thread_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "1", "5");
	}

	@Test public void AbstractRecord_Thread_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "2", "5");
	}

	@Test public void AbstractRecord_Thread_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "5", "5");
	}

	@Test public void AbstractRecord_Thread_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "10", "5");
	}

	@Test public void AbstractRecord_Thread_Test009()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "1", "10");
	}

	@Test public void AbstractRecord_Thread_Test010()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "2", "10");
	}

	@Test public void AbstractRecord_Thread_Test011()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "5", "10");
	}

	@Test public void AbstractRecord_Thread_Test012()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient001.class,  "$(CALLS)", "10", "10");
	}


	@Test public void AbstractRecord_Thread_Test013()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "1", "2");
	}

	@Test public void AbstractRecord_Thread_Test014()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "2", "2");
	}

	@Test public void AbstractRecord_Thread_Test015()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "5", "2");
	}

	@Test public void AbstractRecord_Thread_Test016()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "10", "2");
	}

	@Test public void AbstractRecord_Thread_Test017()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "1", "5");
	}

	@Test public void AbstractRecord_Thread_Test018()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "2", "5");
	}

	@Test public void AbstractRecord_Thread_Test019()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "5", "5");
	}

	@Test public void AbstractRecord_Thread_Test020()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "10", "5");
	}

	@Test public void AbstractRecord_Thread_Test021()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "1", "10");
	}

	@Test public void AbstractRecord_Thread_Test022()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "2", "10");
	}

	@Test public void AbstractRecord_Thread_Test023()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "5", "10");
	}

	@Test public void AbstractRecord_Thread_Test024()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.AbstractRecord.client.WorkerClient002.class,  "$(CALLS)", "10", "10");
	}
}