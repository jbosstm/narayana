/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestATHeuristicRecoveryAfterDelayedCommit extends BaseCrashTest {
	public TestATHeuristicRecoveryAfterDelayedCommit() {
		scriptName ="ATHeuristicRecoveryAfterDelayedCommit";
	}
	
	@Test
	public void MultiParticipantPrepareAndCommitTest() throws Exception {
		testName = "MultiParticipantPrepareAndCommitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.MultiParticipantPrepareAndCommitTest";
		runTest(testClass);
	}
	
	@Test
	public void MultiServicePrepareAndCommitTest() throws Exception {
		testName = "MultiServicePrepareAndCommitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.MultiServicePrepareAndCommitTest";
		runTest(testClass);
	}
}
