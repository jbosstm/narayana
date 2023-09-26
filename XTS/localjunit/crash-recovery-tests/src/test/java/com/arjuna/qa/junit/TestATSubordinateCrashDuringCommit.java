/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestATSubordinateCrashDuringCommit extends BaseCrashTest {
	public TestATSubordinateCrashDuringCommit() {
		scriptName = "ATSubordinateCrashDuringCommit";
	}
	
	@Test
	public void subordinateMultiParticipantPrepareAndCommitTest() throws Exception {
		testName = "subordinate.MultiParticipantPrepareAndCommitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.subordinate.MultiParticipantPrepareAndCommitTest";
		runTest(testClass);
	}
}
