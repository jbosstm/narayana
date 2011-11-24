package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestATCrashDuringOnePhaseCommit extends BaseCrashTest {
	public TestATCrashDuringOnePhaseCommit() {
		scriptName ="ATCrashDuringOnePhaseCommit";
	}

	@Test
	public void SingleParticipantPrepareAndCommit() throws Exception {
		testName = "SingleParticipantPrepareAndCommit";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.SingleParticipantPrepareAndCommitTest";
		runTest(testClass);
	}
}
