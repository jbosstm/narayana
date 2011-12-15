package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestATSubordinateCrashDuringPrepare extends BaseCrashTest {
	public TestATSubordinateCrashDuringPrepare() {
		scriptName = "ATSubordinateCrashDuringPrepare";
	}
	
	@Ignore("Not stop with byteman when recovering")
	@Test
	public void subordinateMultiParticipantPrepareAndCommitTest() throws Exception {
		testName = "subordinate.MultiParticipantPrepareAndCommitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.subordinate.MultiParticipantPrepareAndCommitTest";
		runTest(testClass);
	}
}
