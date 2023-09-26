/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBASubordinateCrashDuringComplete extends BaseCrashTest {
	public TestBASubordinateCrashDuringComplete() {
		scriptName = "BASubordinateCrashDuringComplete";
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
