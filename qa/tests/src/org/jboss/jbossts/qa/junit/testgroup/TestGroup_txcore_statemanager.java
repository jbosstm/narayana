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
	@Test public void StateManager_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client001.class, "$(CALLS)", "1");
	}

	@Test public void StateManager_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client001.class, "$(CALLS)", "2");
	}

	@Test public void StateManager_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client001.class, "$(CALLS)", "5");
	}

	@Test public void StateManager_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client001.class, "$(CALLS)", "10");
	}


	@Test public void StateManager_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client002.class, "$(CALLS)", "1");
	}

	@Test public void StateManager_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client002.class, "$(CALLS)", "2");
	}

	@Test public void StateManager_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client002.class, "$(CALLS)", "5");
	}

	@Test public void StateManager_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client002.class, "$(CALLS)", "10");
	}


	@Test public void StateManager_Test009()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client003.class, "$(CALLS)", "1");
	}

	@Test public void StateManager_Test010()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client003.class, "$(CALLS)", "2");
	}

	@Test public void StateManager_Test011()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client003.class, "$(CALLS)", "5");
	}

	@Test public void StateManager_Test012()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client003.class, "$(CALLS)", "10");
	}


	@Test public void StateManager_Test013()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client004.class, "$(CALLS)", "1");
	}

	@Test public void StateManager_Test014()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client004.class, "$(CALLS)", "2");
	}

	@Test public void StateManager_Test015()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client004.class, "$(CALLS)", "5");
	}

	@Test public void StateManager_Test016()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.Client004.class, "$(CALLS)", "10");
	}


	@Test public void StateManager_Thread_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "1", "2");
	}

	@Test public void StateManager_Thread_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "2", "2");
	}

	@Test public void StateManager_Thread_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "5", "2");
	}

	@Test public void StateManager_Thread_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "10", "2");
	}

	@Test public void StateManager_Thread_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "1", "5");
	}

	@Test public void StateManager_Thread_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "2", "5");
	}

	@Test public void StateManager_Thread_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "5", "5");
	}

	@Test public void StateManager_Thread_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "10", "5");
	}

	@Test public void StateManager_Thread_Test009()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "1", "10");
	}

	@Test public void StateManager_Thread_Test010()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "2", "10");
	}

	@Test public void StateManager_Thread_Test011()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "5", "10");
	}

	@Test public void StateManager_Thread_Test012()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient001.class, "$(CALLS)", "10", "10");
	}


	@Test public void StateManager_Thread_Test013()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "1", "2");
	}

	@Test public void StateManager_Thread_Test014()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "2", "2");
	}

	@Test public void StateManager_Thread_Test015()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "5", "2");
	}

	@Test public void StateManager_Thread_Test016()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "10", "2");
	}

	@Test public void StateManager_Thread_Test017()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "1", "5");
	}

	@Test public void StateManager_Thread_Test018()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "2", "5");
	}

	@Test public void StateManager_Thread_Test019()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "5", "5");
	}

	@Test public void StateManager_Thread_Test020()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "10", "5");
	}

	@Test public void StateManager_Thread_Test021()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "1", "10");
	}

	@Test public void StateManager_Thread_Test022()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "2", "10");
	}

	@Test public void StateManager_Thread_Test023()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "5", "10");
	}

	@Test public void StateManager_Thread_Test024()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient002.class, "$(CALLS)", "10", "10");
	}


	@Test public void StateManager_Thread_Test025()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "1", "2");
	}

	@Test public void StateManager_Thread_Test026()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "2", "2");
	}

	@Test public void StateManager_Thread_Test027()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "5", "2");
	}

	@Test public void StateManager_Thread_Test028()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "10", "2");
	}

	@Test public void StateManager_Thread_Test029()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "1", "5");
	}

	@Test public void StateManager_Thread_Test030()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "2", "5");
	}

	@Test public void StateManager_Thread_Test031()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "5", "5");
	}

	@Test public void StateManager_Thread_Test032()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "10", "5");
	}

	@Test public void StateManager_Thread_Test033()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "1", "10");
	}

	@Test public void StateManager_Thread_Test034()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "2", "10");
	}

	@Test public void StateManager_Thread_Test035()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "5", "10");
	}

	@Test public void StateManager_Thread_Test036()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient003.class, "$(CALLS)", "10", "10");
	}


	@Test public void StateManager_Thread_Test037()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "1", "2");
	}

	@Test public void StateManager_Thread_Test038()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "2", "2");
	}

	@Test public void StateManager_Thread_Test039()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "5", "2");
	}

	@Test public void StateManager_Thread_Test040()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "10", "2");
	}

	@Test public void StateManager_Thread_Test041()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "1", "5");
	}

	@Test public void StateManager_Thread_Test042()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "2", "5");
	}

	@Test public void StateManager_Thread_Test043()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "5", "5");
	}

	@Test public void StateManager_Thread_Test044()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "10", "5");
	}

	@Test public void StateManager_Thread_Test045()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "1", "10");
	}

	@Test public void StateManager_Thread_Test046()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "2", "10");
	}

	@Test public void StateManager_Thread_Test047()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "5", "10");
	}

	@Test public void StateManager_Thread_Test048()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.StateManager.client.WorkerClient004.class, "$(CALLS)", "10", "10");
	}
}