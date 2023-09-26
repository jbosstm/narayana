/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestATCrashDuringSingleParticipantCommit extends BaseCrashTest {
    /**
     * One phase commit is not permitted for XTS. The single participant
     * runs the two phase commit as well.
     */
	public TestATCrashDuringSingleParticipantCommit() {
		scriptName ="ATCrashDuringCommit";
	}

	@Test
	public void SingleParticipantPrepareAndCommit() throws Exception {
		testName = "SingleParticipantPrepareAndCommit";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.SingleParticipantPrepareAndCommitTest";
		runTest(testClass);
	}
}
