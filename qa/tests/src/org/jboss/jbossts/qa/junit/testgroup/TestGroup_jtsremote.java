/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.Utils.PerformanceProfileStore;
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
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, getTimeout(960));
        server1.start("$(1)");
        Task server2 = createTask("server2", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, getTimeout(960));
        server2.start("$(2)");

        Task client = createTask("client1", com.hp.mwtests.ts.jts.remote.hammer.DistributedHammer1.class, Task.TaskType.EXPECT_PASS_FAIL, getTimeout(960));
		client.start("$(1)", "$(2)");
		client.waitFor();

        server1.terminate();
        server2.terminate();
    }

    @Test public void JTSRemote_DistributedHammerTest2() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, getTimeout(960));
        server1.start("$(1)");
        Task server2 = createTask("server2", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, getTimeout(960));
        server2.start("$(2)");

        Task client = createTask("client1", com.hp.mwtests.ts.jts.remote.hammer.DistributedHammer2.class, Task.TaskType.EXPECT_PASS_FAIL, getTimeout(960));
		client.start("$(1)", "$(2)");
		client.waitFor();

        server1.terminate();
        server2.terminate();
    }

    @Test public void JTSRemote_DistributedHammerTest3() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, getTimeout(480));
        server1.start("$(1)");
        Task server2 = createTask("server2", com.hp.mwtests.ts.jts.remote.servers.HammerServer.class, Task.TaskType.EXPECT_READY, getTimeout(480));
        server2.start("$(2)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.hammer.DistributedHammer3.class, "$(1)", "$(2)");

        server1.terminate();
        server2.terminate();
    }

    @Test public void JTSRemote_ExplicitPropagationTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.ExplicitStackServer.class, Task.TaskType.EXPECT_READY, getTimeout(480));
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.arjuna.ExplicitArjunaClient.class, "$(1)");

        server1.terminate();        
    }

    @Test public void JTSRemote_ImplicitPropagationTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.StackServer.class, Task.TaskType.EXPECT_READY, getTimeout(480));
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.arjuna.ImplicitArjunaClient.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_ImplicitGridTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.ImplGridServer.class, Task.TaskType.EXPECT_READY, getTimeout(480));
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.implicit.ImplicitClient.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_CurrentTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.GridServer.class, Task.TaskType.EXPECT_READY, getTimeout(480));
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.current.CurrentTest.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_ExplicitInterpositionTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.SetGetServer.class, Task.TaskType.EXPECT_READY, getTimeout(480));
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.explicitinterposition.ExplicitInterClient.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_TimeoutTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.SetGetServer.class, Task.TaskType.EXPECT_READY, getTimeout(480));
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.timeout.TimeoutClient.class, "$(1)");

        server1.terminate();
    }

    @Test public void JTSRemote_PerfTest() {
        Task server1 = createTask("server1", com.hp.mwtests.ts.jts.remote.servers.GridServer.class, Task.TaskType.EXPECT_READY, getTimeout(960));
        server1.start("$(1)");

        startAndWaitForClient(com.hp.mwtests.ts.jts.remote.hammer.PerfHammer.class, "$(1)");

        server1.terminate();
    }
}