/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */

package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

public class TestGroup_jtatests01 extends TestGroupBase
{
    public TestGroup_jtatests01() {
        isRecoveryManagerNeeded = true;
    }

	@Test public void JTATests01_Test001()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test01.class, "$(LOCAL_PARAMETER)");
	}

	@Test public void JTATests01_Test002()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test02.class, "$(LOCAL_PARAMETER)");
	}

	@Test public void JTATests01_Test003()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test03.class, "$(LOCAL_PARAMETER)");
	}

	@Test public void JTATests01_Test004()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test04.class, "$(LOCAL_PARAMETER)", "1000");
	}

	@Test public void JTATests01_Test005()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test05.class, "$(LOCAL_PARAMETER)", "1000");
	}

	@Test public void JTATests01_Test006()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.JTA01Tests.Test06.class, "$(LOCAL_PARAMETER)", "32", "1000");
	}

}