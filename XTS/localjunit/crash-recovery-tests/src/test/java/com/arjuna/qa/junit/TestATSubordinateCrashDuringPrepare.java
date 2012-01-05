package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestATSubordinateCrashDuringPrepare extends BaseCrashTest {
	public TestATSubordinateCrashDuringPrepare() {
		scriptName = "ATSubordinateCrashDuringPrepare";
	}
	
	@Test
	public void subordinateMultiParticipantPrepareAndCommitTest() throws Exception {
		testName = "subordinate.MultiParticipantPrepareAndCommitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.subordinate.MultiParticipantPrepareAndCommitTest";
		runTest(testClass);
	}
}
