package org.jboss.jbossts.qa.junit.testgroup;

import org.jboss.jbossts.qa.junit.TestGroupBase;

import org.jboss.jbossts.qa.junit.*;
import org.junit.*;

public class TestGroup_defaulttimeout extends TestGroupBase
{
    public TestGroup_defaulttimeout() {
        isRecoveryManagerNeeded = false;
    }

	@Test public void defaulttimeout_Test01()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.DefaultTimeout.Test01.class);
    }

	@Test public void defaulttimeout_Test02()
	{
        startAndWaitForClient(org.jboss.jbossts.qa.DefaultTimeout.Test02.class);
    }
}