/*
   Copyright The Narayana Authors
   SPDX-License-Identifier: Apache-2.0
 */
package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestATParticipantCrashAndRecover extends BaseCrashTest {
	public TestATParticipantCrashAndRecover() {
		scriptName = "ATParticipantCrashAndRecover";
	}
	
	@Ignore("Not complete")
	@Test
	public void SingleParticipantPrepareAndCommit() throws Exception {
		testName = "SingleParticipantPrepareAndCommit";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.SingleParticipantPrepareAndCommitTest";
		runTest(testClass);
	}
	
	@Ignore("Not complete")
	@Test
	public void MultiParticipantPrepareAndCommitTest() throws Exception {
		testName = "MultiParticipantPrepareAndCommitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.MultiParticipantPrepareAndCommitTest";
		runTest(testClass);
	}
	
	@Ignore("Not complete")
	@Test
	public void MultiServicePrepareAndCommitTest() throws Exception {
		testName = "MultiServicePrepareAndCommitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.at.MultiServicePrepareAndCommitTest";
		runTest(testClass);
	}
}
