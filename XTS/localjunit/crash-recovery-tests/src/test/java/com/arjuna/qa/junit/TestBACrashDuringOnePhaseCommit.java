/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBACrashDuringOnePhaseCommit extends BaseCrashTest {
	public TestBACrashDuringOnePhaseCommit() {
		scriptName = "BACrashDuringOnePhaseCommit";
	}
	
	@Test
	public void SingleCoordinatorCompletionParticipantCloseTest() throws Exception {
		testName = "SingleCoordinatorCompletionParticipantCloseTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.SingleCoordinatorCompletionParticipantCloseTest";
		runTest(testClass);
	}
	
	@Test
	public void SingleParticipantCompletionParticipantCloseTest() throws Exception {
		testName = "SingleParticipantCompletionParticipantCloseTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.SingleParticipantCompletionParticipantCloseTest";
		runTest(testClass);

	}
}
