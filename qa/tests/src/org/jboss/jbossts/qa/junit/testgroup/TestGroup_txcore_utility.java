/*
   Copyright The Narayana Authors
   SPDX short identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

public class TestGroup_txcore_utility extends TestGroupBase
{
	@Test public void Utility_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "0");
	}

	@Test public void Utility_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "10");
	}

	@Test public void Utility_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "50");
	}

	@Test public void Utility_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "100");
	}

	@Test public void Utility_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "250");
	}

	@Test public void Utility_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "1000");
	}

	@Test public void Utility_Test007()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "10000");
	}

	@Test public void Utility_Test008()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "100000");
	}

	@Test public void Utility_Test009()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "9999999");
	}

	@Test public void Utility_Test010()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "max");
	}

	@Test public void Utility_Test011()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-1");
	}

	@Test public void Utility_Test012()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-10");
	}

	@Test public void Utility_Test013()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-50");
	}

	@Test public void Utility_Test014()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-100");
	}

	@Test public void Utility_Test015()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-250");
	}

	@Test public void Utility_Test016()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-1000");
	}

	@Test public void Utility_Test017()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-10000");
	}

	@Test public void Utility_Test018()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-100000");
	}

	@Test public void Utility_Test019()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "-9999999");
	}

	@Test public void Utility_Test020()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "int", "min");
	}


	@Test public void Utility_Test021()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "0");
	}

	@Test public void Utility_Test022()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "10");
	}

	@Test public void Utility_Test023()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "inlongt", "50");
	}

	@Test public void Utility_Test024()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "100");
	}

	@Test public void Utility_Test025()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "250");
	}

	@Test public void Utility_Test026()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "1000");
	}

	@Test public void Utility_Test027()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "10000");
	}

	@Test public void Utility_Test028()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "100000");
	}

	@Test public void Utility_Test029()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "9999999");
	}

	@Test public void Utility_Test030()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "max");
	}

	@Test public void Utility_Test031()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-1");
	}

	@Test public void Utility_Test032()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-10");
	}

	@Test public void Utility_Test033()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-50");
	}

	@Test public void Utility_Test034()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-100");
	}

	@Test public void Utility_Test035()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-250");
	}

	@Test public void Utility_Test036()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-1000");
	}

	@Test public void Utility_Test037()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-10000");
	}

	@Test public void Utility_Test038()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-100000");
	}

	@Test public void Utility_Test039()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "-9999999");
	}

	@Test public void Utility_Test040()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.ArjunaCore.Common.UtilityTest.class, "long", "min");
	}

}