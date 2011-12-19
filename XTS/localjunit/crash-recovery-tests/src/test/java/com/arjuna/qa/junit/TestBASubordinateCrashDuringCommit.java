package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBASubordinateCrashDuringCommit extends BaseCrashTest {
	public TestBASubordinateCrashDuringCommit() {
		scriptName = "BASubordinateCrashDuringCommit";
	}
	
	@Test
	public void subordinateMultiParticipantCoordinatorCompletionParticipantCloseTest() throws Exception {
		testName = "subordinate.MultiParticipantCoordinatorCompletionParticipantCloseTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.subordinate.MultiParticipantCoordinatorCompletionParticipantCloseTest";
		runTest(testClass);
	}
	
	@Test
	public void subordinateMultiParticipantParticipantCompletionParticipantCloseTest() throws Exception {
		testName = "subordinate.MultiParticipantParticipantCompletionParticipantCloseTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.subordinate.MultiParticipantParticipantCompletionParticipantCloseTest";
		runTest(testClass);
	}
}
