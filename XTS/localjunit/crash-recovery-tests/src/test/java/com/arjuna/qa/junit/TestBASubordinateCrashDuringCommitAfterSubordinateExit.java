/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBASubordinateCrashDuringCommitAfterSubordinateExit extends
		BaseCrashTest {
	public TestBASubordinateCrashDuringCommitAfterSubordinateExit() {
		scriptName = "BASubordinateCrashDuringCommitAfterSubordinateExit";
	}
	
	@Test
	public void subordinateMultiParticipantCoordinatorCompletionParticipantCloseAndExitTest() throws Exception {
		testName = "subordinate.MultiParticipantCoordinatorCompletionParticipantCloseAndExitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.subordinate.MultiParticipantCoordinatorCompletionParticipantCloseAndExitTest";
		runTest(testClass);
	}
	
	@Test
	public void subordinateMultiParticipantParticipantCompletionParticipantCloseAndExitTest() throws Exception {
		testName = "subordinate.MultiParticipantParticipantCompletionParticipantCloseAndExitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.subordinate.MultiParticipantParticipantCompletionParticipantCloseAndExitTest";
		runTest(testClass);
	}
}
