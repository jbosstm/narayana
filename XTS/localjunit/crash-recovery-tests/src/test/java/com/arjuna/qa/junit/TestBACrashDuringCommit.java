package com.arjuna.qa.junit;

import org.jboss.arquillian.junit.Arquillian;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class TestBACrashDuringCommit extends BaseCrashTest {
	public TestBACrashDuringCommit() {
		scriptName = "BACrashDuringCommit";
	}
	
	@Test
	public void MultiParticipantCoordinatorCompletionParticipantCloseTest() throws Exception {
		testName = "MultiParticipantCoordinatorCompletionParticipantCloseTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.MultiParticipantCoordinatorCompletionParticipantCloseTest";
		runTest(testClass);
	}
	
	@Test
	public void MultiParticipantCoordinatorCompletionParticipantCloseAndExitTest() throws Exception {
		testName = "MultiParticipantCoordinatorCompletionParticipantCloseAndExitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.MultiParticipantCoordinatorCompletionParticipantCloseTest";
		runTest(testClass);
	}
	
	@Test
	public void MultiParticipantParticipantCompletionParticipantCloseTest() throws Exception {
		testName = "MultiParticipantParticipantCompletionParticipantCloseTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.MultiParticipantParticipantCompletionParticipantCloseTest";
		runTest(testClass);
	}
	
	@Test
	public void MultiParticipantParticipantCompletionParticipantCloseAndExitTest() throws Exception {
		testName = "MultiParticipantParticipantCompletionParticipantCloseAndExitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.MultiParticipantParticipantCompletionParticipantCloseAndExitTest";
		runTest(testClass);
	}
	
	@Test
	public void MultiServiceCoordinatorCompletionParticipantCloseTest() throws Exception {
		testName = "MultiServiceCoordinatorCompletionParticipantCloseTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.MultiServiceCoordinatorCompletionParticipantCloseTest";
		runTest(testClass);
	}
	
	@Test
	public void MultiServiceCoordinatorCompletionParticipantCloseAndExitTest() throws Exception {
		testName = "MultiServiceCoordinatorCompletionParticipantCloseAndExitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.MultiServiceCoordinatorCompletionParticipantCloseAndExitTest";
		runTest(testClass);
	}
	
	@Test
	public void MultiServiceParticipantCompletionParticipantCloseTest() throws Exception {
		testName = "MultiServiceParticipantCompletionParticipantCloseTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.MultiServiceParticipantCompletionParticipantCloseTest";
		runTest(testClass);
	}
	
	@Test
	public void MultiServiceParticipantCompletionParticipantCloseAndExitTest() throws Exception {
		testName = "MultiServiceParticipantCompletionParticipantCloseAndExitTest";
		String testClass = "org.jboss.jbossts.xts.servicetests.test.ba.MultiServiceParticipantCompletionParticipantCloseAndExitTest";
		runTest(testClass);
	}
	
}
