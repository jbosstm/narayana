/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

public class TestGroup_txoj extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "txoj";
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
/*
	@Test public void AtomicObjectTest1()
	{
		setTestName("AtomicObjectTest1");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.atomicobject.AtomicObjectTest1.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AtomicObjectTest2()
	{
		setTestName("AtomicObjectTest2");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.atomicobject.AtomicObjectTest2.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void AtomicObjectTest3()
	{
		setTestName("AtomicObjectTest3");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.atomicobject.AtomicObjectTest3.class, Task.TaskType.EXPECT_PASS_FAIL, 1200);
		task0.perform();
	}

	@Test public void AtomicTest()
	{
		setTestName("AtomicTest");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.basic.AtomicTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void BasicActionTest()
	{
		setTestName("BasicActionTest");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.basic.BasicActionTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void BasicRecoverableTest()
	{
		setTestName("BasicRecoverableTest");
		Task RcvMngr = createTask("RcvMngr", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 480);
		RcvMngr.start("-test");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.basic.RecoverableTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
		RcvMngr.terminate();
	}

	@Test public void ConcurrencyTest()
	{
		setTestName("ConcurrencyTest");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.concurrencycontrol.ConcurrencyTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void DestroyTest()
	{
		setTestName("DestroyTest");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.destroy.DestroyTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void HammerTest()
	{
		setTestName("HammerTest");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.hammer.Hammer.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void PerformanceTest1()
	{
		setTestName("PerformanceTest1");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.performance.PerformanceTest1.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}

	@Test public void PersistenceTest()
	{
		setTestName("PersistenceTest");
		Task task0 = createTask("task0", com.hp.mwtests.ts.txoj.basic.PersistenceTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		task0.perform();
	}
*/
}