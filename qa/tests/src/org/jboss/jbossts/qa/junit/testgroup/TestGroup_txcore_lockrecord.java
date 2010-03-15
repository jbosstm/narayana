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
	@Test public void LockRecord_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client001.class, "$(CALLS)", "1");
	}

	@Test public void LockRecord_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client001.class, "$(CALLS)", "2");
	}

	@Test public void LockRecord_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client001.class, "$(CALLS)", "5");
	}

	@Test public void LockRecord_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client001.class, "$(CALLS)", "10");
	}


	@Test public void LockRecord_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, "$(CALLS)", "1");
	}

	@Test public void LockRecord_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, "$(CALLS)", "2");
	}

	@Test public void LockRecord_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, "$(CALLS)", "5");
	}

	@Test public void LockRecord_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client002.class, "$(CALLS)", "10");
	}


	@Test public void LockRecord_Test009()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client003.class, "$(CALLS)", "1");
	}

	@Test public void LockRecord_Test010()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client003.class, "$(CALLS)", "2");
	}

	@Test public void LockRecord_Test011()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client003.class, "$(CALLS)", "5");
	}

	@Test public void LockRecord_Test012()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client003.class, "$(CALLS)", "10");
	}


	@Test public void LockRecord_Test013()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client004.class, "$(CALLS)", "1");
	}

	@Test public void LockRecord_Test014()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client004.class, "$(CALLS)", "2");
	}


	@Test public void LockRecord_Test015()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client004.class, "100", "5");
	}

	@Test public void LockRecord_Test016()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.Client004.class, "$(CALLS)", "10");
	}


	@Test public void LockRecord_Thread_Test001a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test001b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test002a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test002b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test003a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "5", "2");
	}

	@Test public void LockRecord_Thread_Test003b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "5", "2");
	}

	@Test public void LockRecord_Thread_Test004a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "10", "2");
	}

	@Test public void LockRecord_Thread_Test004b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "10", "2");
	}

	@Test public void LockRecord_Thread_Test005a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "1", "5");
	}

	@Test public void LockRecord_Thread_Test005b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "1", "5");
	}

	@Test public void LockRecord_Thread_Test006a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test006b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "2", "5");
	}

	@Test public void LockRecord_Thread_Test007a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "5", "5");
	}

	@Test public void LockRecord_Thread_Test007b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "5", "5");
	}

	@Test public void LockRecord_Thread_Test008a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "10", "5");
	}

	@Test public void LockRecord_Thread_Test008b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "10", "5");
	}

	@Test public void LockRecord_Thread_Test009a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "1", "10");
	}

	@Test public void LockRecord_Thread_Test009b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "1", "10");
	}

	@Test public void LockRecord_Thread_Test010a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "2", "10");
	}

	@Test public void LockRecord_Thread_Test010b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "2", "10");
	}

	@Test public void LockRecord_Thread_Test011a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "5", "10");
	}

	@Test public void LockRecord_Thread_Test011b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "5", "10");
	}

	@Test public void LockRecord_Thread_Test012a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "$(CALLS)", "10", "10");
	}

	@Test public void LockRecord_Thread_Test012b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient001.class, "-newlock", "$(CALLS)", "10", "10");
	}


	@Test public void LockRecord_Thread_Test013a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test013b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test014a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test014b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test015a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "5", "2");
	}

	@Test public void LockRecord_Thread_Test015b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)","5", "2");
	}

	@Test public void LockRecord_Thread_Test016a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "10", "2");
	}

	@Test public void LockRecord_Thread_Test016b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "10", "2");
	}

	@Test public void LockRecord_Thread_Test017a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "1", "5");
	}

	@Test public void LockRecord_Thread_Test017b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test018a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "2", "5");
	}

	@Test public void LockRecord_Thread_Test018b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "2", "5");
	}

	@Test public void LockRecord_Thread_Test019a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "5", "5");
	}

	@Test public void LockRecord_Thread_Test019b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "5", "5");
	}

	@Test public void LockRecord_Thread_Test020a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "10", "5");
	}

	@Test public void LockRecord_Thread_Test020b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "10", "5");
	}

	@Test public void LockRecord_Thread_Test021a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "1", "10");
	}

	@Test public void LockRecord_Thread_Test021b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "1", "10");
	}

	@Test public void LockRecord_Thread_Test022a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "2", "10");
	}

	@Test public void LockRecord_Thread_Test022b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "2", "10");
	}

	@Test public void LockRecord_Thread_Test023a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "5", "10");
	}

	@Test public void LockRecord_Thread_Test023b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "5", "10");
	}

	@Test public void LockRecord_Thread_Test024a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "$(CALLS)", "10", "10");
	}

	@Test public void LockRecord_Thread_Test024b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient002.class, "-newlock", "$(CALLS)", "10", "10");
	}


	@Test public void LockRecord_Thread_Test025a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test025b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test026a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test026b()
    {
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test027a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "5", "2");
	}

	@Test public void LockRecord_Thread_Test027b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "5", "2");
	}

	@Test public void LockRecord_Thread_Test028a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "10", "2");
	}

	@Test public void LockRecord_Thread_Test028b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "10", "2");
	}

	@Test public void LockRecord_Thread_Test029a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "1", "5");
	}

	@Test public void LockRecord_Thread_Test029b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "1", "5");
	}

	@Test public void LockRecord_Thread_Test030a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "2", "5");
	}

	@Test public void LockRecord_Thread_Test030b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "2", "5");
	}

	@Test public void LockRecord_Thread_Test031a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "5", "5");
	}

	@Test public void LockRecord_Thread_Test031b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "5", "5");
	}

	@Test public void LockRecord_Thread_Test032a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "10", "5");
	}

	@Test public void LockRecord_Thread_Test032b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "10", "5");
	}

	@Test public void LockRecord_Thread_Test033a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "1", "10");
	}

	@Test public void LockRecord_Thread_Test033b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "1", "10");
	}

	@Test public void LockRecord_Thread_Test034a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "2", "10");
	}

	@Test public void LockRecord_Thread_Test034b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "2", "10");
	}

	@Test public void LockRecord_Thread_Test035a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "5", "10");
	}

	@Test public void LockRecord_Thread_Test035b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "5", "10");
	}

	@Test public void LockRecord_Thread_Test036a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "$(CALLS)", "10", "10");
	}

	@Test public void LockRecord_Thread_Test036b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient003.class, "-newlock", "$(CALLS)", "10", "10");
	}


	@Test public void LockRecord_Thread_Test037a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test037b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "1", "2");
	}

	@Test public void LockRecord_Thread_Test038a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test038b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "2", "2");
	}

	@Test public void LockRecord_Thread_Test039a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "5", "2");
	}

	@Test public void LockRecord_Thread_Test039b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "5", "2");
	}

	@Test public void LockRecord_Thread_Test040a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "10", "2");
	}

	@Test public void LockRecord_Thread_Test040b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "10", "2");
	}

	@Test public void LockRecord_Thread_Test041a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "1", "5");
	}

	@Test public void LockRecord_Thread_Test041b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "1", "5");
	}

	@Test public void LockRecord_Thread_Test042a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "2", "5");
	}

	@Test public void LockRecord_Thread_Test042b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "2", "5");
	}

	@Test public void LockRecord_Thread_Test043a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "5", "5");
	}

	@Test public void LockRecord_Thread_Test043b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "5", "5");
	}

	@Test public void LockRecord_Thread_Test044a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "10", "5");
	}

	@Test public void LockRecord_Thread_Test044b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "10", "5");
	}

	@Test public void LockRecord_Thread_Test045a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "1", "10");
	}

	@Test public void LockRecord_Thread_Test045b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "1", "10");
	}

	@Test public void LockRecord_Thread_Test046a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "2", "10");
	}

	@Test public void LockRecord_Thread_Test046b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "2", "10");
	}

	@Test public void LockRecord_Thread_Test047a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "5", "10");
	}

	@Test public void LockRecord_Thread_Test047b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "5", "10");
	}

	@Test public void LockRecord_Thread_Test048a()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "$(CALLS)", "10", "10");
	}

	@Test public void LockRecord_Thread_Test048b()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.LockManager.client.WorkerClient004.class, "-newlock", "$(CALLS)", "10", "10");
	}
}