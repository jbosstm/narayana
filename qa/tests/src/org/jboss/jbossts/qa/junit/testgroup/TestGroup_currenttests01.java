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

public class TestGroup_currenttests01 extends TestGroupBase
{
	public String getTestGroupName()
	{
		return "currenttests01";
	}

	protected Task server0 = null;

	@Before public void setUp()
	{
		super.setUp();
		server0 = createTask("server0", com.arjuna.ats.arjuna.recovery.RecoveryManager.class, Task.TaskType.EXPECT_READY, 600);
		server0.start("-test");
	}

	@After public void tearDown()
	{
		try {
			server0.terminate();
		} finally {
			super.tearDown();
		}
	}

    private void doTest(String testName, Class clazz) {
        setTestName(testName);
        Task client0 = createTask("client0", clazz, Task.TaskType.EXPECT_PASS_FAIL, 600);
        client0.start();
        client0.waitFor();
    }

	@Test public void CurrentTests01_Test001()
	{
        doTest("Test001", org.jboss.jbossts.qa.CurrentTests01.Test01.class);
	}

	@Test public void CurrentTests01_Test002()
	{
        doTest("Test002", org.jboss.jbossts.qa.CurrentTests01.Test02.class);
	}

	@Test public void CurrentTests01_Test003()
	{
        doTest("Test003", org.jboss.jbossts.qa.CurrentTests01.Test03.class);
	}

	@Test public void CurrentTests01_Test004()
	{
        doTest("Test004", org.jboss.jbossts.qa.CurrentTests01.Test04.class);
	}

	@Test public void CurrentTests01_Test005()
	{
        doTest("Test005", org.jboss.jbossts.qa.CurrentTests01.Test05.class);
	}

	@Test public void CurrentTests01_Test006()
	{
        doTest("Test006", org.jboss.jbossts.qa.CurrentTests01.Test06.class);
	}

	@Test public void CurrentTests01_Test007()
	{
        doTest("Test007", org.jboss.jbossts.qa.CurrentTests01.Test07.class);
	}

	@Test public void CurrentTests01_Test008()
	{
        doTest("Test008", org.jboss.jbossts.qa.CurrentTests01.Test08.class);
	}

	@Test public void CurrentTests01_Test009()
	{
        doTest("Test009", org.jboss.jbossts.qa.CurrentTests01.Test09.class);
	}

	@Test public void CurrentTests01_Test010()
	{
        doTest("Test010", org.jboss.jbossts.qa.CurrentTests01.Test10.class);
	}

	@Test public void CurrentTests01_Test011()
	{
        doTest("Test011", org.jboss.jbossts.qa.CurrentTests01.Test11.class);
	}

	@Test public void CurrentTests01_Test012()
	{
        doTest("Test012", org.jboss.jbossts.qa.CurrentTests01.Test12.class);
	}

	@Test public void CurrentTests01_Test013()
	{
        doTest("Test013", org.jboss.jbossts.qa.CurrentTests01.Test13.class);
	}

	@Test public void CurrentTests01_Test014()
	{
        doTest("Test014", org.jboss.jbossts.qa.CurrentTests01.Test14.class);
	}

	@Test public void CurrentTests01_Test015()
	{
        doTest("Test015", org.jboss.jbossts.qa.CurrentTests01.Test15.class);
	}

	@Test public void CurrentTests01_Test016()
	{
        doTest("Test016", org.jboss.jbossts.qa.CurrentTests01.Test16.class);
	}

	@Test public void CurrentTests01_Test017()
	{
        doTest("Test017", org.jboss.jbossts.qa.CurrentTests01.Test17.class);
	}

	@Test public void CurrentTests01_Test018()
	{
        doTest("Test018", org.jboss.jbossts.qa.CurrentTests01.Test18.class);
	}

	@Test public void CurrentTests01_Test019()
	{
        doTest("Test019", org.jboss.jbossts.qa.CurrentTests01.Test19.class);
	}

	@Test public void CurrentTests01_Test020()
	{
        doTest("Test020", org.jboss.jbossts.qa.CurrentTests01.Test20.class);
	}

	@Test public void CurrentTests01_Test021()
	{
        doTest("Test021", org.jboss.jbossts.qa.CurrentTests01.Test21.class);
	}

	@Test public void CurrentTests01_Test022()
	{
        doTest("Test022", org.jboss.jbossts.qa.CurrentTests01.Test22.class);
	}

	@Test public void CurrentTests01_Test023()
	{
        doTest("Test023", org.jboss.jbossts.qa.CurrentTests01.Test23.class);
	}

	@Test public void CurrentTests01_Test024()
	{
        doTest("Test024", org.jboss.jbossts.qa.CurrentTests01.Test24.class);
	}

	@Test public void CurrentTests01_Test025()
	{
        doTest("Test025", org.jboss.jbossts.qa.CurrentTests01.Test25.class);
	}

	@Test public void CurrentTests01_Test026()
	{
        doTest("Test026", org.jboss.jbossts.qa.CurrentTests01.Test26.class);
	}

	@Test public void CurrentTests01_Test027()
	{
        doTest("Test027", org.jboss.jbossts.qa.CurrentTests01.Test27.class);
	}

	@Test public void CurrentTests01_Test028()
	{
        doTest("Test028", org.jboss.jbossts.qa.CurrentTests01.Test28.class);
	}

	@Test public void CurrentTests01_Test029()
	{
        doTest("Test029", org.jboss.jbossts.qa.CurrentTests01.Test29.class);
	}

	@Test public void CurrentTests01_Test030()
	{
        doTest("Test030", org.jboss.jbossts.qa.CurrentTests01.Test30.class);
	}

	@Test public void CurrentTests01_Test031()
	{
        doTest("Test031", org.jboss.jbossts.qa.CurrentTests01.Test31.class);
	}

	@Test public void CurrentTests01_Test032()
	{
        doTest("Test032", org.jboss.jbossts.qa.CurrentTests01.Test32.class);
	}

	@Test public void CurrentTests01_Test033()
	{
        doTest("Test033", org.jboss.jbossts.qa.CurrentTests01.Test33.class);
	}

	@Test public void CurrentTests01_Test034()
	{
        doTest("Test034", org.jboss.jbossts.qa.CurrentTests01.Test34.class);
	}

	@Test public void CurrentTests01_Test035()
	{
        doTest("Test035", org.jboss.jbossts.qa.CurrentTests01.Test35.class);
	}

	@Test public void CurrentTests01_Test036()
	{
        doTest("Test036", org.jboss.jbossts.qa.CurrentTests01.Test36.class);
	}

}