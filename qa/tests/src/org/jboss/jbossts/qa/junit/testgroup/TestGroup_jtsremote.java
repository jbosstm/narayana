/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc. and/or its affiliates,
 * and individual contributors as indicated by the @author tags.
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
 * (C) 2010,
 * @author JBoss, by Red Hat.
 */
package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.Task;
import org.jboss.jbossts.qa.junit.TestGroupBase;
import org.junit.Test;

/**
 * QA tests for JTS module.
 */
public class TestGroup_jtsremote  extends TestGroupBase
{
    public TestGroup_jtsremote() {
        isRecoveryManagerNeeded = true;
    }

    @Test public void JTSRemote_DistributedHammerTest1() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");
        Task server2 = createTask("server2", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, 480);
        server2.start("$(2)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.hammer.DistributedHammer1.class, "$(1)", "$(2)");

        server1.terminate();
        server2.terminate();
    }

    @Test public void JTSRemote_DistributedHammerTest2() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");
        Task server2 = createTask("server2", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, 480);
        server2.start("$(2)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.hammer.DistributedHammer2.class, "$(1)", "$(2)");

        server1.terminate();
        server2.terminate();
    }

    @Test public void JTSRemote_DistributedHammerTest3() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");
        Task server2 = createTask("server2", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, 480);
        server2.start("$(2)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.hammer.DistributedHammer3.class, "$(1)", "$(2)");

        server1.terminate();
        server2.terminate();
    }

    @Test public void JTSRemote_ExplicitPropagationTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.ExplicitStackServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.arjuna.ExplicitArjunaClient.class, "$(1)");

        server1.terminate();        
    }

    @Test public void JTSRemote_ImplicitPropagationTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.StackServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.arjuna.ImplicitArjunaClient.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_ImplicitGridTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.ImplGridServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.implicit.ImplicitClient.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_CurrentTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.GridServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.current.CurrentTest.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_ExplicitInterpositionTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.SetGetServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.explicitinterposition.ExplicitInterClient.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_TimeoutTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.SetGetServer.class, Task.TaskType.EXPECT_READY, 480);
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.timeout.TimeoutClient.class, "$(1)");

        server1.terminate();
    }
}