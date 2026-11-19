package com.arjuna.wst11.tests.arq;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.arjuna.wst11.tests.WarDeployment;
import com.arjuna.wst11.tests.arq.TestCoordinatorCompletionCoordinatorProcessor.CoordinatorCompletionCoordinatorDetails;

@RunWith(Arquillian.class)
public class BusinessAgreementWithCoordinatorCompletionParticipantTest {
	@Inject
	BusinessAgreementWithCoordinatorCompletionParticipantTestCase test;

	@Deployment
	public static WebArchive createDeployment() {
		return WarDeployment.getDeployment(
				CoordinatorCompletionCoordinatorDetails.class,
				TestCoordinatorCompletionCoordinatorProcessor.class,
				BusinessAgreementWithCoordinatorCompletionParticipantTestCase.class);
	}
	
	@Before
	public void setUp() throws Exception{
		test.setUp();
	}
	
	@Test
	public void testSendCancelled() throws Exception {
		test.testSendCancelled();
	}
	
	@Test
	public void testSendClosed() throws Exception {
		test.testSendClosed();
	}
	
	@Test
	public void testSendCompensated() throws Exception {
		test.testSendCompensated();
	}
	
	@Test
	public void testSendCompleted() throws Exception {
		test.testSendCompleted();
	}
	
	@Test
	public void testSendError() throws Exception {
		test.testSendError();
	}
	
	@Test
	public void testSendExit() throws Exception {
		test.testSendExit();
	}
	
	@Test
	public void testSendFault() throws Exception {
		test.testSendFault();
	}
	
	@Test
	public void testSendGetStatus() throws Exception {
		test.testSendGetStatus();
	}
	
	@Test
	public void testSendStatus() throws Exception {
		test.testSendStatus();
	}
	
	@After
	public void tearDown() throws Exception {
		test.tearDown();
	}
}
