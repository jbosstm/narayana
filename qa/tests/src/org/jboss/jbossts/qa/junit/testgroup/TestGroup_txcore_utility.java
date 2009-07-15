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

public class TestGroup_txcore_utility extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "txcore_utility";
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

	@Test public void Utility_Test001()
	{
		setTestName("Test001");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "0");
		client0.waitFor();
	}

	@Test public void Utility_Test002()
	{
		setTestName("Test002");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "10");
		client0.waitFor();
	}

	@Test public void Utility_Test003()
	{
		setTestName("Test003");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "50");
		client0.waitFor();
	}

	@Test public void Utility_Test004()
	{
		setTestName("Test004");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "100");
		client0.waitFor();
	}

	@Test public void Utility_Test005()
	{
		setTestName("Test005");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "250");
		client0.waitFor();
	}

	@Test public void Utility_Test006()
	{
		setTestName("Test006");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "1000");
		client0.waitFor();
	}

	@Test public void Utility_Test007()
	{
		setTestName("Test007");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "10000");
		client0.waitFor();
	}

	@Test public void Utility_Test008()
	{
		setTestName("Test008");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "100000");
		client0.waitFor();
	}

	@Test public void Utility_Test009()
	{
		setTestName("Test009");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "9999999");
		client0.waitFor();
	}

	@Test public void Utility_Test010()
	{
		setTestName("Test010");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "max");
		client0.waitFor();
	}

	@Test public void Utility_Test011()
	{
		setTestName("Test011");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-1");
		client0.waitFor();
	}

	@Test public void Utility_Test012()
	{
		setTestName("Test012");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-10");
		client0.waitFor();
	}

	@Test public void Utility_Test013()
	{
		setTestName("Test013");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-50");
		client0.waitFor();
	}

	@Test public void Utility_Test014()
	{
		setTestName("Test014");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-100");
		client0.waitFor();
	}

	@Test public void Utility_Test015()
	{
		setTestName("Test015");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-250");
		client0.waitFor();
	}

	@Test public void Utility_Test016()
	{
		setTestName("Test016");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-1000");
		client0.waitFor();
	}

	@Test public void Utility_Test017()
	{
		setTestName("Test017");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-10000");
		client0.waitFor();
	}

	@Test public void Utility_Test018()
	{
		setTestName("Test018");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-100000");
		client0.waitFor();
	}

	@Test public void Utility_Test019()
	{
		setTestName("Test019");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "-9999999");
		client0.waitFor();
	}

	@Test public void Utility_Test020()
	{
		setTestName("Test020");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("int", "min");
		client0.waitFor();
	}

	@Test public void Utility_Test021()
	{
		setTestName("Test021");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "0");
		client0.waitFor();
	}

	@Test public void Utility_Test022()
	{
		setTestName("Test022");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "10");
		client0.waitFor();
	}

	@Test public void Utility_Test023()
	{
		setTestName("Test023");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "50");
		client0.waitFor();
	}

	@Test public void Utility_Test024()
	{
		setTestName("Test024");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "100");
		client0.waitFor();
	}

	@Test public void Utility_Test025()
	{
		setTestName("Test025");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "250");
		client0.waitFor();
	}

	@Test public void Utility_Test026()
	{
		setTestName("Test026");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "1000");
		client0.waitFor();
	}

	@Test public void Utility_Test027()
	{
		setTestName("Test027");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "10000");
		client0.waitFor();
	}

	@Test public void Utility_Test028()
	{
		setTestName("Test028");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "100000");
		client0.waitFor();
	}

	@Test public void Utility_Test029()
	{
		setTestName("Test029");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "9999999");
		client0.waitFor();
	}

	@Test public void Utility_Test030()
	{
		setTestName("Test030");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "max");
		client0.waitFor();
	}

	@Test public void Utility_Test031()
	{
		setTestName("Test031");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-1");
		client0.waitFor();
	}

	@Test public void Utility_Test032()
	{
		setTestName("Test032");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-10");
		client0.waitFor();
	}

	@Test public void Utility_Test033()
	{
		setTestName("Test033");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-50");
		client0.waitFor();
	}

	@Test public void Utility_Test034()
	{
		setTestName("Test034");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-100");
		client0.waitFor();
	}

	@Test public void Utility_Test035()
	{
		setTestName("Test035");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-250");
		client0.waitFor();
	}

	@Test public void Utility_Test036()
	{
		setTestName("Test036");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-1000");
		client0.waitFor();
	}

	@Test public void Utility_Test037()
	{
		setTestName("Test037");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-10000");
		client0.waitFor();
	}

	@Test public void Utility_Test038()
	{
		setTestName("Test038");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-100000");
		client0.waitFor();
	}

	@Test public void Utility_Test039()
	{
		setTestName("Test039");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "-9999999");
		client0.waitFor();
	}

	@Test public void Utility_Test040()
	{
		setTestName("Test040");
		Task client0 = createTask("client0", org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, Task.TaskType.EXPECT_PASS_FAIL, 480);
		client0.start("long", "min");
		client0.waitFor();
	}

}