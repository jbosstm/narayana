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